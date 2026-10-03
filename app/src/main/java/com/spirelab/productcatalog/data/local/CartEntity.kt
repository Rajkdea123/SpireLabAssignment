package com.spirelab.productcatalog.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A cart line. Stores a snapshot of the product fields the cart needs so the cart can render
 * fully offline without asking the network for product data.
 */
@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val productId: Int,
    val title: String,
    val price: Double,
    val thumbnail: String,
    val quantity: Int,
    val addedAt: Long,
)
