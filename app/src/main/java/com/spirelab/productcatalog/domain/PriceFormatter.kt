package com.spirelab.productcatalog.domain

import java.math.BigDecimal
import java.math.RoundingMode

/** DummyJSON prices are USD; always render two decimal places. */
object PriceFormatter {

    fun format(amount: BigDecimal): String =
        "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString()

    fun format(amount: Double): String = format(amount.toBigDecimal())
}
