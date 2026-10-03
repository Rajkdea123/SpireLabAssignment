package com.spirelab.productcatalog

import android.content.Context
import com.spirelab.productcatalog.data.local.AppDatabase
import com.spirelab.productcatalog.data.remote.ProductApi
import com.spirelab.productcatalog.data.repository.CartRepository
import com.spirelab.productcatalog.data.repository.ProductRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Manual dependency container. Created once in [ProductCatalogApplication]; everything here
 * is a process-wide singleton.
 */
class AppContainer(context: Context) {

    private val appContext = context.applicationContext

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
                    )
                }
            }
            .build()
    }

    private val productApi: ProductApi by lazy {
        Retrofit.Builder()
            .baseUrl(ProductApi.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApi::class.java)
    }

    private val database: AppDatabase by lazy { AppDatabase.getInstance(appContext) }

    val productRepository: ProductRepository by lazy { ProductRepository(productApi) }

    val cartRepository: CartRepository by lazy { CartRepository(database.cartDao()) }
}
