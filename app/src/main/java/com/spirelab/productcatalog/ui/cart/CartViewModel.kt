package com.spirelab.productcatalog.ui.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.spirelab.productcatalog.ProductCatalogApplication
import com.spirelab.productcatalog.data.repository.CartRepository
import com.spirelab.productcatalog.domain.CartCalculator
import com.spirelab.productcatalog.domain.model.CartItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totalItems: Int = 0,
    val totalPrice: BigDecimal = BigDecimal.ZERO,
    val isLoading: Boolean = true,
)

/** Reads and writes only Room through [CartRepository]; nothing here touches the network. */
class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    val uiState: StateFlow<CartUiState> = cartRepository.cartItems
        .map { items ->
            CartUiState(
                items = items,
                totalItems = CartCalculator.totalQuantity(items),
                totalPrice = CartCalculator.totalPrice(items),
                isLoading = false,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CartUiState())

    fun increment(productId: Int) {
        viewModelScope.launch { cartRepository.increment(productId) }
    }

    fun decrement(productId: Int) {
        viewModelScope.launch { cartRepository.decrement(productId) }
    }

    fun remove(productId: Int) {
        viewModelScope.launch { cartRepository.remove(productId) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as ProductCatalogApplication).container
                CartViewModel(container.cartRepository)
            }
        }
    }
}
