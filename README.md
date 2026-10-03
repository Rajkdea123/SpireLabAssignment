# Product Catalog

An Android app for the Spire Lab assessment. It lists products from the [DummyJSON Products API](https://dummyjson.com/docs/products), lets you search them and open their details, and keeps a shopping cart in Room that works fully offline.

## Features

- **Product listing**: a 2-column grid. Each card shows the image, title, price and rating. The screen has loading, empty, error and retry states.
- **Search**: uses the real API (`/products/search?q=`). It waits 400 ms after typing stops, skips repeat queries and cancels an older request when a newer one starts. Clearing the field shows the full catalog again.
- **Product details**: shows the image, title, description, price, rating, category, brand ("Not specified" when missing) and stock, plus an **Add to Cart** button. The button is disabled when the product is out of stock.
- **Cart**: stored in Room. You can increase or decrease quantity and remove items. Decreasing a quantity of 1 removes the item. Adding the same product again increases its quantity instead of adding a second row. The screen shows **Total Items** (sum of quantities) and **Total Price**.
- **Cart badge**: the cart icon in the top bar shows a live count that updates from Room.
- **Error handling**: errors are shown as user-friendly messages: "No internet connection", "Request timed out", "Product not found", or a general fallback. Raw exception text is never shown.

## Tech stack

Kotlin 2.0.21, Jetpack Compose (Material 3, BOM 2024.12.01), Navigation Compose 2.8.5, Lifecycle/ViewModel 2.8.7, Coroutines and Flow/StateFlow, Retrofit 2.11 with the Gson converter, OkHttp 4.12, Room 2.6.1 (KSP), Coil 2.7. Builds with AGP 8.7.3, Gradle 8.11.1 and JDK 17. compileSdk/targetSdk 35, minSdk 26.

## Architecture

The app uses MVVM with repositories. A small `AppContainer` built in the `Application` class wires up the dependencies by hand (no Hilt).

```
com.spirelab.productcatalog
├── AppContainer / ProductCatalogApplication / MainActivity
├── data
│   ├── local        CartEntity, CartDao, AppDatabase (Room, file-backed singleton)
│   ├── remote       ProductApi (Retrofit), dto/, ProductMapper, ErrorMessages
│   └── repository   ProductRepository (network), CartRepository (Room only)
├── domain           model/Product, model/CartItem, CartCalculator, PriceFormatter
├── navigation       AppNavHost, Routes
├── theme            Material 3 theme
└── ui
    ├── products     ProductsScreen, ProductsViewModel
    ├── details      ProductDetailsScreen, ProductDetailsViewModel
    ├── cart         CartScreen, CartViewModel
    └── components   ProductImage, loading/empty/error states, rating, cart badge
```

- Composables only render immutable UI state and pass events up. ViewModels expose `StateFlow`, the screens collect it with `collectAsStateWithLifecycle()`, and all work runs in `viewModelScope`.
- DTOs are mapped to domain models inside the data layer, so Retrofit types never reach Compose.
- `ProductRepository` returns `Result<T>` and lets coroutine cancellation propagate. This matters for search, where `flatMapLatest` cancels outdated requests.
- The product ID for the details screen comes from `SavedStateHandle`, and so does the search query. Both survive process death.
- Cart totals are calculated in `CartCalculator` with `BigDecimal`, rounded to cents and unit-tested.

## Build and run

Requirements: JDK 17 and the Android SDK (platform 35, build-tools 35.0.0).

1. If it doesn't exist yet, create `local.properties` pointing at your SDK:
   `sdk.dir=C\:\\Users\\<you>\\AppData\\Local\\Android\\Sdk`
2. Build and test:

```bash
./gradlew assembleDebug        # Windows: gradlew.bat assembleDebug
./gradlew test
./gradlew installDebug         # with a device or emulator connected
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## API

The base URL is `https://dummyjson.com/`.

| Use | Request |
| --- | --- |
| Product list | `GET /products?limit=0&select=title,price,rating,thumbnail` |
| Search | `GET /products/search?q={query}&limit=0&select=...` |
| Details | `GET /products/{id}` |

`limit=0` returns the whole catalog (194 products), so the list needs no pagination. `select` cuts the list payload down to the fields the cards show.

OkHttp timeouts are 15 s for connect and 20 s for read and write.

## Local storage and offline behavior

The cart is stored in the `cart_items` Room table in `product_catalog.db`. This is an on-disk database, not an in-memory one. `productId` is the primary key, and each row keeps a snapshot of `title`, `price`, `thumbnail` and `quantity`, so the cart never needs product data from the network.

`CartRepository` depends only on `CartDao`; it has no Retrofit dependency. Once items are in the cart, all of the following work in airplane mode: opening the cart, viewing items, changing quantities, removing items, and the totals. The cart also survives closing and reopening the app or the process being killed.

Cart images come from the stored thumbnail URL. The DummyJSON CDN sends `Cache-Control: no-store`, so the app's Coil `ImageLoader` is set to ignore cache headers, and the details screen prefetches the thumbnail. Because of this, items added to the cart keep their image offline. If an image still isn't in the cache, a placeholder icon appears, and the text, quantities and totals keep working.

## Known limitations

- The product list and search are not cached, so they need a network connection. Only the cart works offline.
- The cart keeps the price as it was when the product was added. It is not refreshed if the API price changes later.
- Quantity has no stock limit, because the cart stores only what it needs to work offline.
- There is no checkout or payment flow, and the cart has no "clear all" button.
- Tests cover the pure logic: cart totals, price formatting, DTO mapping and error mapping. There are no instrumented Room or UI tests.
