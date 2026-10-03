package com.spirelab.productcatalog

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

class ProductCatalogApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    /**
     * The DummyJSON CDN sends `Cache-Control: no-store`, which would stop Coil from writing
     * images to its disk cache. Product images are static, so ignore those headers; this lets
     * cart thumbnails keep rendering offline once they have been loaded.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .respectCacheHeaders(false)
            .crossfade(true)
            .build()
}
