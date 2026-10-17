package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.ui.components.CategoryItem
import com.example.ui.components.ProductCard
import com.example.ui.theme.GoldPrimary
import com.example.ui.viewmodel.ProductSortOption
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun CategoriesScreen(
    viewModel: ShopViewModel,
    onNavigateToProduct: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()
    val minRatingFilter by viewModel.minRatingFilter.collectAsState()
    val filteredProducts by viewModel.filteredProducts.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val wishlistedIds = wishlistItems.map { it.productId }.toSet()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("categories_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // Categories Horizontal Selector
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text(
                    text = if (isArabic) "تصفح حسب التصنيف" else "Browse by Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedCategoryId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectCategory(null) }
                                .padding(vertical = 4.dp)
                                .testTag("cat_all_chip")
                        ) {
                            Text(
                                text = if (isArabic) "جميع المنتجات" else "All Products",
                                color = if (selectedCategoryId == null) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            )
                        }
                    }

                    items(allCategories) { category ->
                        CategoryItem(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            isArabic = isArabic,
                            onClick = {
                                if (selectedCategoryId == category.id) {
                                    viewModel.selectCategory(null)
                                } else {
                                    viewModel.selectCategory(category.id)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Sorting & Rating Filter Chips Row
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sort,
                        contentDescription = "Sort",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )

                    FilterChip(
                        selected = sortOption == ProductSortOption.DEFAULT,
                        onClick = { viewModel.setSortOption(ProductSortOption.DEFAULT) },
                        label = { Text(if (isArabic) "الافتراضي" else "Default", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = sortOption == ProductSortOption.PRICE_LOW_TO_HIGH,
                        onClick = { viewModel.setSortOption(ProductSortOption.PRICE_LOW_TO_HIGH) },
                        label = { Text(if (isArabic) "السعر: الأقل للأعلى" else "Price: Low to High", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = sortOption == ProductSortOption.PRICE_HIGH_TO_LOW,
                        onClick = { viewModel.setSortOption(ProductSortOption.PRICE_HIGH_TO_LOW) },
                        label = { Text(if (isArabic) "السعر: الأعلى للأقل" else "Price: High to Low", fontSize = 11.sp) }
                    )

                    FilterChip(
                        selected = sortOption == ProductSortOption.BEST_RATING,
                        onClick = { viewModel.setSortOption(ProductSortOption.BEST_RATING) },
                        label = { Text(if (isArabic) "الأعلى تقييماً" else "Top Rated", fontSize = 11.sp) }
                    )

                    // 4+ Star rating filter
                    FilterChip(
                        selected = minRatingFilter >= 4.0,
                        onClick = {
                            if (minRatingFilter >= 4.0) viewModel.setMinRatingFilter(0.0)
                            else viewModel.setMinRatingFilter(4.0)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        label = { Text("4.0+ ★", fontSize = 11.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = if (isArabic) "عرض ${filteredProducts.size} منتج" else "Showing ${filteredProducts.size} products",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Product Grid
        if (filteredProducts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isArabic) "لا توجد منتجات في هذا التصنيف حالياً" else "No products found for this filter",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            items(filteredProducts.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    for (product in rowItems) {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductCard(
                                product = product,
                                isArabic = isArabic,
                                isWishlisted = wishlistedIds.contains(product.id),
                                onProductClick = {
                                    viewModel.selectProduct(product)
                                    onNavigateToProduct(product)
                                },
                                onAddToCartClick = { viewModel.addToCart(product.id) },
                                onWishlistToggle = { viewModel.toggleWishlist(product.id) }
                            )
                        }
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
