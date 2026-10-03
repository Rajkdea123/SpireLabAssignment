package com.spirelab.productcatalog.ui.categories

data class CategoryGroup(
    val name: String,
    val slugs: List<String>,
) {
    companion object {
        val ALL = listOf(
            CategoryGroup(
                name = "Beauty and Care",
                slugs = listOf("beauty", "skin-care", "fragrances"),
            ),
            CategoryGroup(
                name = "Men",
                slugs = listOf("mens-shirts", "mens-shoes", "mens-watches"),
            ),
            CategoryGroup(
                name = "Women",
                slugs = listOf("womens-dresses", "tops", "womens-shoes", "womens-bags", "womens-jewellery", "womens-watches"),
            ),
            CategoryGroup(
                name = "Accessories",
                slugs = listOf("sunglasses", "mobile-accessories", "sports-accessories"),
            ),
            CategoryGroup(
                name = "Electronics",
                slugs = listOf("smartphones", "laptops", "tablets"),
            ),
            CategoryGroup(
                name = "Home and Living",
                slugs = listOf("furniture", "home-decoration", "kitchen-accessories", "groceries"),
            ),
            CategoryGroup(
                name = "Automotive",
                slugs = listOf("motorcycle", "vehicle"),
            ),
        )
    }
}
