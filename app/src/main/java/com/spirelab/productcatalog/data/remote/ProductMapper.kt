package com.spirelab.productcatalog.data.remote

import com.spirelab.productcatalog.data.remote.dto.ProductDto
import com.spirelab.productcatalog.domain.model.Product

fun ProductDto.toDomain(): Product = Product(
    id = id,
    title = title.orEmpty(),
    description = description.orEmpty(),
    category = category.orEmpty(),
    price = price ?: 0.0,
    rating = rating ?: 0.0,
    stock = stock ?: 0,
    brand = brand?.takeIf { it.isNotBlank() },
    thumbnail = thumbnail.orEmpty(),
    images = images.orEmpty(),
    discountPercentage = discountPercentage ?: 0.0,
)
