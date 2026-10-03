package com.spirelab.productcatalog.domain

import com.spirelab.productcatalog.domain.model.CartItem
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Cart arithmetic. Uses BigDecimal so totals like 0.1 + 0.2 do not pick up floating-point
 * noise; results are rounded to cents.
 */
object CartCalculator {

    fun totalQuantity(items: List<CartItem>): Int = items.sumOf { it.quantity.coerceAtLeast(0) }

    fun lineTotal(item: CartItem): BigDecimal =
        unroundedLineTotal(item).setScale(2, RoundingMode.HALF_UP)

    fun totalPrice(items: List<CartItem>): BigDecimal =
        items.fold(BigDecimal.ZERO) { sum, item -> sum + unroundedLineTotal(item) }
            .setScale(2, RoundingMode.HALF_UP)

    private fun unroundedLineTotal(item: CartItem): BigDecimal =
        item.price.toBigDecimal() * item.quantity.coerceAtLeast(0).toBigDecimal()
}
