package com.spirelab.productcatalog.domain

import com.spirelab.productcatalog.domain.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CartCalculatorTest {

    private fun item(id: Int, price: Double, quantity: Int) =
        CartItem(productId = id, title = "Product $id", price = price, thumbnail = "", quantity = quantity)

    @Test
    fun `empty cart has zero totals`() {
        assertEquals(0, CartCalculator.totalQuantity(emptyList()))
        assertEquals(BigDecimal("0.00"), CartCalculator.totalPrice(emptyList()))
    }

    @Test
    fun `total quantity sums quantities across products`() {
        val items = listOf(item(1, 10.0, 2), item(2, 5.0, 3))

        assertEquals(5, CartCalculator.totalQuantity(items))
    }

    @Test
    fun `total price is sum of price times quantity`() {
        val items = listOf(item(1, 9.99, 2), item(2, 19.99, 3))

        // 19.98 + 59.97
        assertEquals(BigDecimal("79.95"), CartCalculator.totalPrice(items))
    }

    @Test
    fun `total price has no floating point drift`() {
        val items = listOf(item(1, 0.1, 1), item(2, 0.2, 1))

        assertEquals(BigDecimal("0.30"), CartCalculator.totalPrice(items))
    }

    @Test
    fun `line total multiplies unit price by quantity`() {
        assertEquals(BigDecimal("29.97"), CartCalculator.lineTotal(item(1, 9.99, 3)))
    }

    @Test
    fun `totals follow quantity changes`() {
        val initial = listOf(item(1, 12.5, 1), item(2, 3.25, 4))
        assertEquals(5, CartCalculator.totalQuantity(initial))
        assertEquals(BigDecimal("25.50"), CartCalculator.totalPrice(initial))

        val incremented = initial.map { if (it.productId == 1) it.copy(quantity = 3) else it }
        assertEquals(7, CartCalculator.totalQuantity(incremented))
        assertEquals(BigDecimal("50.50"), CartCalculator.totalPrice(incremented))

        val removed = incremented.filterNot { it.productId == 2 }
        assertEquals(3, CartCalculator.totalQuantity(removed))
        assertEquals(BigDecimal("37.50"), CartCalculator.totalPrice(removed))
    }

    @Test
    fun `negative quantities never reduce totals`() {
        val items = listOf(item(1, 10.0, -2), item(2, 4.0, 1))

        assertEquals(1, CartCalculator.totalQuantity(items))
        assertEquals(BigDecimal("4.00"), CartCalculator.totalPrice(items))
    }

    @Test
    fun `prices are formatted with two decimals`() {
        assertEquals("$5.00", PriceFormatter.format(5.0))
        assertEquals("$9.99", PriceFormatter.format(9.99))
        assertEquals("$79.95", PriceFormatter.format(BigDecimal("79.95")))
        assertEquals("$1299.90", PriceFormatter.format(1299.9))
    }
}
