package com.spirelab.productcatalog.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.imageLoader
import coil.request.ImageRequest
import com.spirelab.productcatalog.domain.PriceFormatter
import com.spirelab.productcatalog.domain.model.Product
import com.spirelab.productcatalog.ui.components.CartActionButton
import com.spirelab.productcatalog.ui.components.ErrorState
import com.spirelab.productcatalog.ui.components.LoadingState
import com.spirelab.productcatalog.ui.components.ProductImage
import com.spirelab.productcatalog.ui.components.RatingLabel
import java.util.Locale

private const val BRAND_FALLBACK = "Not specified"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsScreen(
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductDetailsViewModel = viewModel(factory = ProductDetailsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // The cart shows the thumbnail; warm the image cache so it is available offline later.
    val context = LocalContext.current
    val thumbnail = state.product?.thumbnail
    LaunchedEffect(thumbnail) {
        if (!thumbnail.isNullOrBlank()) {
            context.imageLoader.enqueue(ImageRequest.Builder(context).data(thumbnail).build())
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Product Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = { CartActionButton(count = state.cartCount, onClick = onCartClick) },
            )
        },
        bottomBar = {
            val product = state.product
            if (product != null) {
                AddToCartBar(
                    inStock = product.stock > 0,
                    quantityInCart = state.quantityInCart,
                    onAddToCart = viewModel::addToCart,
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        val modifier = Modifier
            .fillMaxSize()
            .padding(padding)
        val product = state.product
        val error = state.error
        when {
            product != null -> ProductDetailsBody(product, modifier)
            error != null -> ErrorState(message = error, onRetry = viewModel::retry, modifier = modifier)
            else -> LoadingState(modifier)
        }
    }
}

@Composable
private fun ProductDetailsBody(product: Product, modifier: Modifier = Modifier) {
    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        ProductImage(
            url = product.imageUrl,
            contentDescription = product.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = PriceFormatter.format(product.price),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                RatingLabel(rating = product.rating)
            }
            Spacer(Modifier.height(16.dp))
            Text(text = "Description", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                text = product.description.ifBlank { "No description available." },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
            HorizontalDivider()
            InfoRow(label = "Category", value = formatCategory(product.category))
            InfoRow(label = "Brand", value = product.brand ?: BRAND_FALLBACK)
            InfoRow(
                label = "Stock",
                value = if (product.stock > 0) "${product.stock} available" else "Out of stock",
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(16.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider()
}

@Composable
private fun AddToCartBar(inStock: Boolean, quantityInCart: Int, onAddToCart: () -> Unit) {
    Surface(tonalElevation = 3.dp, shadowElevation = 8.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            if (quantityInCart > 0) {
                Text(
                    text = "In your cart: $quantityInCart",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Button(
                onClick = onAddToCart,
                enabled = inStock,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
            ) {
                Icon(Icons.Filled.AddShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (inStock) "Add to Cart" else "Out of Stock")
            }
        }
    }
}

private fun formatCategory(category: String): String =
    if (category.isBlank()) {
        BRAND_FALLBACK
    } else {
        category.replace('-', ' ').replaceFirstChar { it.titlecase(Locale.getDefault()) }
    }
