package com.spirelab.productcatalog.ui.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spirelab.productcatalog.domain.PriceFormatter
import com.spirelab.productcatalog.domain.model.Product
import com.spirelab.productcatalog.ui.components.ErrorState
import com.spirelab.productcatalog.ui.components.LoadingState
import com.spirelab.productcatalog.ui.components.MessageState
import com.spirelab.productcatalog.ui.components.ProductImage
import com.spirelab.productcatalog.ui.components.RatingLabel

@Composable
fun ProductsScreen(
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductsContent(
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onCategorySelect = viewModel::onCategorySelect,
        onRetry = viewModel::retry,
        onProductClick = onProductClick,
        onCartClick = onCartClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProductsContent(
    state: ProductsUiState,
    onQueryChange: (String) -> Unit,
    onCategorySelect: (String?) -> Unit,
    onRetry: () -> Unit,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            CatalogHeader(
                query = state.query,
                onQueryChange = onQueryChange,
                categories = state.categories,
                selectedCategorySlug = state.selectedCategorySlug,
                onCategorySelect = onCategorySelect,
                cartCount = state.cartCount,
                onCartClick = onCartClick,
            )
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.error != null -> ErrorState(message = state.error, onRetry = onRetry)
                    state.isLoading && state.products.isEmpty() -> LoadingState()
                    state.products.isEmpty() -> EmptyProducts(state.resultsQuery)
                    else -> ProductGrid(products = state.products, onProductClick = onProductClick)
                }
                if (state.isLoading && state.products.isNotEmpty()) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
                }
            }
        }
    }
}

@Composable
private fun EmptyProducts(resultsQuery: String) {
    if (resultsQuery.isEmpty()) {
        MessageState(icon = Icons.Filled.Inventory2, title = "No products available")
    } else {
        MessageState(
            icon = Icons.Filled.SearchOff,
            title = "No results for \"$resultsQuery\"",
            message = "Try a different search term.",
        )
    }
}

@Composable
private fun ProductGrid(products: List<Product>, onProductClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(products, key = { it.id }) { product ->
            ProductCard(product = product, onClick = { onProductClick(product.id) })
        }
    }
}

@Composable
private fun ProductCard(product: Product, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        ProductImage(
            url = product.thumbnail,
            contentDescription = product.title,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = PriceFormatter.format(product.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                RatingLabel(rating = product.rating)
            }
        }
    }
}
