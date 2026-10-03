package com.spirelab.productcatalog.data.remote.dto

/**
 * Mirrors the DummyJSON product object. Every field is nullable because Gson ignores Kotlin
 * nullability and list requests use `select`, which omits unrequested fields.
 */
data class ProductDto(
    val id: Int,
    val title: String?,
    val description: String?,
    val category: String?,
    val price: Double?,
    val rating: Double?,
    val stock: Int?,
    val brand: String?,
    val thumbnail: String?,
    val images: List<String>?,
)

data class ProductsResponseDto(
    val products: List<ProductDto>?,
    val total: Int?,
    val skip: Int?,
    val limit: Int?,
)
