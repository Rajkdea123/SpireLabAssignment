package com.spirelab.productcatalog.data.remote

import com.spirelab.productcatalog.data.remote.dto.CategoryDto
import com.spirelab.productcatalog.data.remote.dto.ProductDto
import com.spirelab.productcatalog.data.remote.dto.ProductsResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    /** `limit = 0` asks DummyJSON for every product; `select` trims the payload to list fields. */
    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("select") select: String,
    ): ProductsResponseDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
        @Query("limit") limit: Int,
        @Query("select") select: String,
    ): ProductsResponseDto

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: Int): ProductDto

    @GET("products/categories")
    suspend fun getCategories(): List<CategoryDto>

    @GET("products/category/{slug}")
    suspend fun getProductsByCategory(
        @Path("slug") slug: String,
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
        @Query("select") select: String,
    ): ProductsResponseDto

    @GET("products/category/{slug}")
    suspend fun getCategoryThumbnail(
        @Path("slug") slug: String,
        @Query("limit") limit: Int,
        @Query("select") select: String,
    ): ProductsResponseDto

    companion object {
        const val BASE_URL = "https://dummyjson.com/"
        const val ALL_PRODUCTS = 0
        const val LIST_FIELDS = "title,price,rating,thumbnail,category,discountPercentage,brand"
    }
}
