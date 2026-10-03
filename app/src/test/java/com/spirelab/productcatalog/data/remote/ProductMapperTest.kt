package com.spirelab.productcatalog.data.remote

import com.spirelab.productcatalog.data.remote.dto.ProductDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProductMapperTest {

    private val dto = ProductDto(
        id = 1,
        title = "Essence Mascara Lash Princess",
        description = "Popular mascara",
        category = "beauty",
        price = 9.99,
        rating = 2.56,
        stock = 99,
        brand = "Essence",
        thumbnail = "https://cdn.dummyjson.com/thumb.webp",
        images = listOf("https://cdn.dummyjson.com/1.webp"),
    )

    @Test
    fun `maps all fields`() {
        val product = dto.toDomain()

        assertEquals(1, product.id)
        assertEquals("Essence Mascara Lash Princess", product.title)
        assertEquals(9.99, product.price, 0.0)
        assertEquals(2.56, product.rating, 0.0)
        assertEquals(99, product.stock)
        assertEquals("Essence", product.brand)
        assertEquals("https://cdn.dummyjson.com/1.webp", product.imageUrl)
    }

    @Test
    fun `missing or blank brand maps to null`() {
        assertNull(dto.copy(brand = null).toDomain().brand)
        assertNull(dto.copy(brand = "  ").toDomain().brand)
    }

    @Test
    fun `missing fields fall back to safe defaults`() {
        val product = ProductDto(
            id = 7, title = null, description = null, category = null, price = null,
            rating = null, stock = null, brand = null, thumbnail = "thumb", images = null,
        ).toDomain()

        assertEquals("", product.title)
        assertEquals(0.0, product.price, 0.0)
        assertEquals(0, product.stock)
        assertEquals("thumb", product.imageUrl)
    }
}
