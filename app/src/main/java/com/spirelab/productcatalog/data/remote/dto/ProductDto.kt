package com.spirelab.productcatalog.data.remote.dto

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
    val discountPercentage: Double?,
)

data class ProductsResponseDto(
    val products: List<ProductDto>?,
    val total: Int?,
    val skip: Int?,
    val limit: Int?,
)

data class CategoryDto(
    val slug: String,
    val name: String,
    val url: String,
)
