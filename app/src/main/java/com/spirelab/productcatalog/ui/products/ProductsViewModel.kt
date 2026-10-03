package com.spirelab.productcatalog.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spirelab.productcatalog.ProductCatalogApplication
import com.spirelab.productcatalog.data.remote.ErrorMessages
import com.spirelab.productcatalog.data.repository.CartRepository
import com.spirelab.productcatalog.data.repository.ProductRepository
import com.spirelab.productcatalog.domain.model.Product
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ProductsUiState(
    /** What is currently typed in the search field (updated on every keystroke). */
    val query: String = "",
    val products: List<Product> = emptyList(),
    /** The trimmed query the shown [products] belong to; empty means the full catalog. */
    val resultsQuery: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val cartCount: Int = 0,
)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ProductsViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    cartRepository: CartRepository,
) : ViewModel() {

    // Kept in SavedStateHandle so the search survives process death.
    private val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")
    private val retryRequests = MutableStateFlow(0)

    /**
     * Search pipeline: debounce keystrokes (an empty query loads immediately), skip repeats,
     * and let flatMapLatest cancel any in-flight request when a newer query arrives.
     */
    private val results: StateFlow<ResultsState> =
        combine(
            query
                .map { it.trim() }
                .debounce { if (it.isEmpty()) 0L else SEARCH_DEBOUNCE_MS }
                .distinctUntilChanged(),
            retryRequests,
        ) { trimmedQuery, _ -> trimmedQuery }
            .flatMapLatest { trimmedQuery ->
                flow {
                    emit(LoadEvent.Started)
                    emit(load(trimmedQuery))
                }
            }
            .scan(ResultsState()) { state, event -> state.reduce(event) }
            .stateIn(viewModelScope, SharingStarted.Eagerly, ResultsState())

    val uiState: StateFlow<ProductsUiState> =
        combine(query, results, cartRepository.totalQuantity) { query, results, cartCount ->
            ProductsUiState(
                query = query,
                products = results.products,
                resultsQuery = results.query,
                isLoading = results.isLoading,
                error = results.error,
                cartCount = cartCount,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductsUiState())

    fun onQueryChange(newQuery: String) {
        savedStateHandle[QUERY_KEY] = newQuery
    }

    fun retry() {
        retryRequests.update { it + 1 }
    }

    private suspend fun load(trimmedQuery: String): LoadEvent {
        val result = if (trimmedQuery.isEmpty()) {
            productRepository.getProducts()
        } else {
            productRepository.searchProducts(trimmedQuery)
        }
        return result.fold(
            onSuccess = { LoadEvent.Loaded(trimmedQuery, it) },
            onFailure = { LoadEvent.Failed(ErrorMessages.from(it, fallback = "Unable to load products")) },
        )
    }

    private data class ResultsState(
        val query: String = "",
        val products: List<Product> = emptyList(),
        val isLoading: Boolean = true,
        val error: String? = null,
    ) {
        fun reduce(event: LoadEvent): ResultsState = when (event) {
            // Keep the previous results visible while the next query loads.
            LoadEvent.Started -> copy(isLoading = true, error = null)
            is LoadEvent.Loaded -> ResultsState(query = event.query, products = event.products, isLoading = false)
            is LoadEvent.Failed -> copy(products = emptyList(), isLoading = false, error = event.message)
        }
    }

    private sealed interface LoadEvent {
        data object Started : LoadEvent
        data class Loaded(val query: String, val products: List<Product>) : LoadEvent
        data class Failed(val message: String) : LoadEvent
    }

    companion object {
        const val SEARCH_DEBOUNCE_MS = 400L
        private const val QUERY_KEY = "query"

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as ProductCatalogApplication).container
                ProductsViewModel(
                    createSavedStateHandle(),
                    container.productRepository,
                    container.cartRepository,
                )
            }
        }
    }
}
