package com.spirelab.productcatalog.ui.details

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
import com.spirelab.productcatalog.navigation.Routes
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProductDetailsUiState(
    val product: Product? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
    /** Quantity of this product already in the cart (live from Room). */
    val quantityInCart: Int = 0,
    val cartCount: Int = 0,
)

class ProductDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
) : ViewModel() {

    // Read from SavedStateHandle so the id survives process death and recreation.
    private val productId: Int = savedStateHandle.get<Int>(Routes.PRODUCT_ID_ARG) ?: INVALID_ID

    private val loadState = MutableStateFlow(ProductDetailsUiState())
    private var loadJob: Job? = null

    private val messageChannel = Channel<String>(Channel.BUFFERED)

    /** One-off messages for a snackbar (e.g. "Added to cart"). */
    val messages: Flow<String> = messageChannel.receiveAsFlow()

    val uiState: StateFlow<ProductDetailsUiState> =
        combine(
            loadState,
            cartRepository.observeQuantity(productId),
            cartRepository.totalQuantity,
        ) { state, inCart, cartCount ->
            state.copy(quantityInCart = inCart, cartCount = cartCount)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductDetailsUiState())

    init {
        load()
    }

    fun retry() = load()

    fun addToCart() {
        val product = loadState.value.product ?: return
        viewModelScope.launch {
            cartRepository.addToCart(product)
            messageChannel.send("${product.title} added to cart")
        }
    }

    private fun load() {
        if (productId <= 0) {
            loadState.value = ProductDetailsUiState(isLoading = false, error = ErrorMessages.NOT_FOUND)
            return
        }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            loadState.update { it.copy(isLoading = true, error = null) }
            productRepository.getProduct(productId).fold(
                onSuccess = { product ->
                    loadState.update { it.copy(product = product, isLoading = false, error = null) }
                },
                onFailure = { error ->
                    loadState.update {
                        it.copy(
                            isLoading = false,
                            error = ErrorMessages.from(error, fallback = "Unable to load product details"),
                        )
                    }
                },
            )
        }
    }

    companion object {
        private const val INVALID_ID = -1

        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as ProductCatalogApplication).container
                ProductDetailsViewModel(
                    createSavedStateHandle(),
                    container.productRepository,
                    container.cartRepository,
                )
            }
        }
    }
}
