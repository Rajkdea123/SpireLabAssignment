package com.spirelab.productcatalog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class CartDao {

    @Query("SELECT * FROM cart_items ORDER BY addedAt ASC")
    abstract fun observeCart(): Flow<List<CartEntity>>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items")
    abstract fun observeTotalQuantity(): Flow<Int>

    @Query("SELECT COALESCE(SUM(quantity), 0) FROM cart_items WHERE productId = :productId")
    abstract fun observeQuantity(productId: Int): Flow<Int>

    /** Returns the new row id, or -1 if the product is already in the cart. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertIfAbsent(item: CartEntity): Long

    @Query("UPDATE cart_items SET quantity = quantity + 1 WHERE productId = :productId")
    abstract suspend fun increment(productId: Int): Int

    @Query("UPDATE cart_items SET quantity = quantity - 1 WHERE productId = :productId AND quantity > 1")
    abstract suspend fun decrementIfAboveOne(productId: Int): Int

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    protected abstract suspend fun setQuantity(productId: Int, quantity: Int): Int

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    abstract suspend fun remove(productId: Int): Int

    @Query("DELETE FROM cart_items")
    abstract suspend fun clear()

    /** Adding a product that is already in the cart bumps its quantity instead of adding a row. */
    @Transaction
    open suspend fun addOrIncrement(item: CartEntity) {
        if (insertIfAbsent(item) == -1L) increment(item.productId)
    }

    /** Quantity never drops below 1: decrementing a line with quantity 1 removes it. */
    @Transaction
    open suspend fun decrementOrRemove(productId: Int) {
        if (decrementIfAboveOne(productId) == 0) remove(productId)
    }

    @Transaction
    open suspend fun updateQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) remove(productId) else setQuantity(productId, quantity)
    }
}
