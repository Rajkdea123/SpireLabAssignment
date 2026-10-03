package com.spirelab.productcatalog.domain.model

data class Product(
    val id: Int,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val rating: Double,
    val stock: Int,
    val brand: String?,
    val thumbnail: String,
    val images: List<String>,
) {
    /** Best image for a large display: the first full-size image, falling back to the thumbnail. */
    val imageUrl: String get() = images.firstOrNull() ?: thumbnail
}
