package com.spirelab.productcatalog.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.spirelab.productcatalog.ui.cart.CartScreen
import com.spirelab.productcatalog.ui.categories.CategoriesScreen
import com.spirelab.productcatalog.ui.details.ProductDetailsScreen
import com.spirelab.productcatalog.ui.products.ProductsScreen

object Routes {
    const val PRODUCTS = "products"
    const val CATEGORIES = "categories"
    const val CART = "cart"
    const val PRODUCT_ID_ARG = "productId"
    const val DETAILS = "details/{$PRODUCT_ID_ARG}"

    fun details(productId: Int) = "details/$productId"
}

private fun NavBackStackEntry.isResumed() = lifecycle.currentState == Lifecycle.State.RESUMED

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.PRODUCTS) {
        composable(Routes.PRODUCTS) { entry ->
            ProductsScreen(
                onProductClick = { id ->
                    if (entry.isResumed()) navController.navigate(Routes.details(id))
                },
                onCartClick = {
                    if (entry.isResumed()) navController.navigate(Routes.CART) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.CATEGORIES) { entry ->
            CategoriesScreen(
                onBackClick = { if (entry.isResumed()) navController.popBackStack() },
                onProductClick = { id ->
                    if (entry.isResumed()) navController.navigate(Routes.details(id))
                },
                onCartClick = {
                    if (entry.isResumed()) navController.navigate(Routes.CART) { launchSingleTop = true }
                },
            )
        }
        composable(
            route = Routes.DETAILS,
            arguments = listOf(navArgument(Routes.PRODUCT_ID_ARG) { type = NavType.IntType }),
        ) { entry ->
            ProductDetailsScreen(
                onBack = { if (entry.isResumed()) navController.popBackStack() },
                onCartClick = {
                    if (entry.isResumed()) navController.navigate(Routes.CART) { launchSingleTop = true }
                },
            )
        }
        composable(Routes.CART) { entry ->
            CartScreen(
                onBack = { if (entry.isResumed()) navController.popBackStack() },
                onBrowseProducts = {
                    if (entry.isResumed()) navController.popBackStack(Routes.PRODUCTS, inclusive = false)
                },
            )
        }
    }
}
