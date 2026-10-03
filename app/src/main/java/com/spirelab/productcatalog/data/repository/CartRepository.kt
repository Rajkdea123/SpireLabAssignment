package com.spirelab.productcatalog.data.repository

import com.spirelab.productcatalog.data.local.CartDao
import com.spirelab.productcatalog.data.local.CartEntity
import com.spirelab.productcatalog.domain.model.CartItem
import com.spirelab.productcatalog.domain.model.Product
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Room-only cart storage. Deliberately has no network dependency so the cart works offline. */
class CartRepository(private val dao: CartDao) {

    val cartItems: Flow<List<CartItem>> =
        dao.observeCart().map { entities -> entities.map { it.toDomain() } }

    val totalQuantity: Flow<Int> = dao.observeTotalQuantity()

    fun observeQuantity(productId: Int): Flow<Int> = dao.observeQuantity(productId)

    suspend fun addToCart(product: Product) {
        dao.addOrIncrement(
            CartEntity(
                productId = product.id,
                title = product.title,
                price = product.price,
                thumbnail = product.thumbnail,
                quantity = 1,
                addedAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun increment(productId: Int) {
        dao.increment(productId)
    }

    suspend fun decrement(productId: Int) {
        dao.decrementOrRemove(productId)
    }

    suspend fun remove(productId: Int) {
        dao.remove(productId)
    }

    private fun CartEntity.toDomain() = CartItem(
        productId = productId,
        title = title,
        price = price,
        thumbnail = thumbnail,
        quantity = quantity,
    )
}
