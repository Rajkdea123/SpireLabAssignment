package com.spirelab.productcatalog.data.repository

import com.spirelab.productcatalog.data.remote.ProductApi
import com.spirelab.productcatalog.data.remote.dto.CategoryDto
import com.spirelab.productcatalog.data.remote.toDomain
import com.spirelab.productcatalog.domain.model.Product
import kotlinx.coroutines.CancellationException

class ProductRepository(private val api: ProductApi) {

    suspend fun getProducts(): Result<List<Product>> = safeApiCall {
        api.getProducts(limit = ProductApi.ALL_PRODUCTS, select = ProductApi.LIST_FIELDS)
            .products.orEmpty()
            .map { it.toDomain() }
    }

    suspend fun searchProducts(query: String): Result<List<Product>> = safeApiCall {
        api.searchProducts(query = query, limit = ProductApi.ALL_PRODUCTS, select = ProductApi.LIST_FIELDS)
            .products.orEmpty()
            .map { it.toDomain() }
    }

    suspend fun getProduct(id: Int): Result<Product> = safeApiCall {
        api.getProduct(id).toDomain()
    }

    suspend fun getCategories(): Result<List<CategoryDto>> = safeApiCall {
        api.getCategories()
    }

    suspend fun getProductsByCategory(slug: String): Result<List<Product>> = safeApiCall {
        api.getProductsByCategory(slug = slug, limit = ProductApi.ALL_PRODUCTS, skip = 0, select = ProductApi.LIST_FIELDS)
            .products.orEmpty()
            .map { it.toDomain() }
    }

    suspend fun getCategoryThumbnail(slug: String): Result<String> = safeApiCall {
        api.getCategoryThumbnail(slug = slug, limit = 1, select = "thumbnail")
            .products?.firstOrNull()?.thumbnail.orEmpty()
    }

    /** Wraps failures in [Result] while letting coroutine cancellation propagate. */
    private inline fun <T> safeApiCall(block: () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
}
