package com.spirelab.productcatalog.ui.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.spirelab.productcatalog.data.remote.dto.CategoryDto
import com.spirelab.productcatalog.domain.model.Product
import com.spirelab.productcatalog.ui.components.ErrorState
import com.spirelab.productcatalog.ui.components.LoadingState
import com.spirelab.productcatalog.ui.products.ProductsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    onBackClick: () -> Unit,
    onProductClick: (Int) -> Unit,
    onCartClick: () -> Unit,
    viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val groups = CategoryGroup.ALL
    var selectedGroup by androidx.compose.runtime.mutableStateOf(groups.first())
    var selectedCategorySlug by androidx.compose.runtime.mutableStateOf<String?>(null)
    var groupThumbnails by androidx.compose.runtime.mutableStateOf<Map<String, String>>(emptyMap())

    // Load group thumbnails
    androidx.compose.runtime.LaunchedEffect(Unit) {
        val thumbnails = mutableMapOf<String, String>()
        groups.forEach { group ->
            group.slugs.firstOrNull()?.let { slug ->
                try {
                    val thumbnail = viewModel.loadCategoryThumbnail(slug)
                    if (thumbnail.isNotEmpty()) {
                        thumbnails[slug] = thumbnail
                    }
                } catch (e: Exception) {
                    // Ignore errors
                }
            }
        }
        groupThumbnails = thumbnails
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categories") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.FavoriteBorder, contentDescription = "Favorites")
                    }
                    BadgedBox(
                        badge = {
                            if (state.cartCount > 0) {
                                Badge {
                                    Text(state.cartCount.toString())
                                }
                            }
                        },
                    ) {
                        IconButton(onClick = onCartClick) {
                            Icon(
                                Icons.Outlined.ShoppingBag,
                                contentDescription = "Cart",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            // Left rail
            CategoryRail(
                groups = groups,
                selectedGroup = selectedGroup,
                onGroupSelect = { selectedGroup = it; selectedCategorySlug = it.slugs.firstOrNull() },
                groupThumbnails = groupThumbnails,
            )

            Divider(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )

            // Right pane
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background),
            ) {
                val groupCategories = state.categories.filter { it.slug in selectedGroup.slugs }
                
                // Sub-category cards (if more than one category in group)
                if (selectedGroup.slugs.size > 1) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(groupCategories) { category ->
                            SubCategoryCard(
                                category = category,
                                isSelected = category.slug == selectedCategorySlug,
                                onClick = { selectedCategorySlug = category.slug },
                            )
                        }
                    }
                    Divider()
                }

                // Circular category row
                CircleCategoryRow(
                    categories = groupCategories,
                    selectedSlug = selectedCategorySlug,
                    onCategorySelect = { selectedCategorySlug = it },
                    modifier = Modifier.padding(16.dp),
                )

                // Products grid
                Box(modifier = Modifier.fillMaxSize()) {
                    val error = state.error
                    when {
                        error != null -> ErrorState(message = error, onRetry = { viewModel.retry() })
                        state.isLoading && state.products.isEmpty() -> LoadingState()
                        state.products.isEmpty() -> EmptyCategories()
                        else -> CategoryProductGrid(
                            products = state.products,
                            onProductClick = onProductClick,
                            onFavoriteClick = {},
                            onAddClick = {},
                        )
                    }
                    if (state.isLoading && state.products.isNotEmpty()) {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.TopCenter),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubCategoryCard(
    category: CategoryDto,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        modifier = Modifier
            .width(120.dp)
            .height(60.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface),
            ) {
                coil.compose.AsyncImage(
                    model = "https://dummyjson.com/products/category/${category.slug}?limit=1&select=thumbnail",
                    contentDescription = category.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
            )
        }
    }
}

@Composable
private fun CategoryProductGrid(
    products: List<Product>,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onAddClick: (Int) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { product ->
            CategoryProductCard(
                product = product,
                onClick = { onProductClick(product.id) },
                onFavoriteClick = { onFavoriteClick(product.id) },
                onAddClick = { onAddClick(product.id) },
            )
        }
    }
}

@Composable
private fun EmptyCategories() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No products available",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
