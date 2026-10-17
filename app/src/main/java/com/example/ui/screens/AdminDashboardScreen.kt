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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.Coupon
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: ShopViewModel,
    onExitAdmin: () -> Unit,
    onViewInvoice: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val allCoupons by viewModel.allCoupons.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Overview", "Orders", "Products", "Categories", "Coupons", "Settings")

    // Modals
    var showAddProductDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryNameEn by remember { mutableStateOf("") }
    var newCategoryNameAr by remember { mutableStateOf("") }

    var showAddCouponDialog by remember { mutableStateOf(false) }
    var newCouponCode by remember { mutableStateOf("") }
    var newCouponPercent by remember { mutableStateOf("10") }
    var newCouponMinSpend by remember { mutableStateOf("10.0") }

    // KPI Metrics calculations
    val totalRevenueBhd = orders.filter { it.orderStatus != "CANCELLED" }.sumOf { it.totalBhd }
    val totalOrders = orders.size
    val pendingOrdersCount = orders.count { it.orderStatus == "PENDING" || it.orderStatus == "PROCESSING" }
    val lowStockCount = allProducts.count { it.stock <= storeSettings.lowStockThreshold }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GoldPrimary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "MA Shopping Admin", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    TextButton(onClick = onExitAdmin, modifier = Modifier.testTag("exit_admin_btn")) {
                        Text(text = if (isArabic) "خروج لمتجر العملاء" else "Exit to Store", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (selectedTabIndex == 2) { // Products tab
                FloatingActionButton(
                    onClick = {
                        editingProduct = null
                        showAddProductDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.testTag("admin_add_product_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Product")
                }
            } else if (selectedTabIndex == 3) { // Categories tab
                FloatingActionButton(
                    onClick = { showAddCategoryDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Category")
                }
            } else if (selectedTabIndex == 4) { // Coupons tab
                FloatingActionButton(
                    onClick = { showAddCouponDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Coupon")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> AdminOverviewTab(
                    viewModel = viewModel,
                    totalRevenueBhd = totalRevenueBhd,
                    totalOrders = totalOrders,
                    pendingOrdersCount = pendingOrdersCount,
                    lowStockCount = lowStockCount,
                    products = allProducts,
                    orders = orders
                )
                1 -> AdminOrdersTab(
                    viewModel = viewModel,
                    orders = orders,
                    onViewInvoice = onViewInvoice
                )
                2 -> AdminProductsTab(
                    viewModel = viewModel,
                    products = allProducts,
                    onEditProduct = {
                        editingProduct = it
                        showAddProductDialog = true
                    }
                )
                3 -> AdminCategoriesTab(
                    viewModel = viewModel,
                    categories = allCategories
                )
                4 -> AdminCouponsTab(
                    viewModel = viewModel,
                    coupons = allCoupons
                )
                5 -> AdminSettingsTab(
                    viewModel = viewModel,
                    settings = storeSettings
                )
            }
        }
    }

    // Add / Edit Product Dialog
    if (showAddProductDialog) {
        ProductEditorDialog(
            product = editingProduct,
            categories = allCategories,
            isArabic = isArabic,
            onDismiss = {
                showAddProductDialog = false
                editingProduct = null
            },
            onSave = { savedProd ->
                viewModel.adminSaveProduct(savedProd)
                showAddProductDialog = false
                editingProduct = null
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showAddCategoryDialog = false },
            title = { Text("Add New Category") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCategoryNameEn,
                        onValueChange = { newCategoryNameEn = it },
                        label = { Text("English Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCategoryNameAr,
                        onValueChange = { newCategoryNameAr = it },
                        label = { Text("Arabic Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryNameEn.isNotBlank()) {
                            viewModel.adminSaveCategory(
                                Category(
                                    nameEn = newCategoryNameEn,
                                    nameAr = newCategoryNameAr.ifBlank { newCategoryNameEn },
                                    iconKey = "other",
                                    productCount = 0
                                )
                            )
                            showAddCategoryDialog = false
                            newCategoryNameEn = ""
                            newCategoryNameAr = ""
                        }
                    }
                ) {
                    Text("Save Category")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Coupon Dialog
    if (showAddCouponDialog) {
        AlertDialog(
            onDismissRequest = { showAddCouponDialog = false },
            title = { Text("Create Discount Coupon") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newCouponCode,
                        onValueChange = { newCouponCode = it.uppercase() },
                        label = { Text("Coupon Code (e.g. SALE15)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCouponPercent,
                        onValueChange = { newCouponPercent = it },
                        label = { Text("Discount Percentage (%)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newCouponMinSpend,
                        onValueChange = { newCouponMinSpend = it },
                        label = { Text("Minimum Spend (BHD)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCouponCode.isNotBlank()) {
                            val percent = newCouponPercent.toDoubleOrNull() ?: 10.0
                            val minSpend = newCouponMinSpend.toDoubleOrNull() ?: 5.0
                            viewModel.adminSaveCoupon(
                                Coupon(
                                    code = newCouponCode.trim(),
                                    discountPercent = percent,
                                    isPercent = true,
                                    minSpendBhd = minSpend,
                                    descriptionEn = "$percent% off on orders over ${minSpend} BHD",
                                    descriptionAr = "خصم $percent% على الطلبات فوق ${minSpend} د.ب",
                                    isActive = true
                                )
                            )
                            showAddCouponDialog = false
                            newCouponCode = ""
                        }
                    }
                ) {
                    Text("Save Coupon")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCouponDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    viewModel: ShopViewModel,
    totalRevenueBhd: Double,
    totalOrders: Int,
    pendingOrdersCount: Int,
    lowStockCount: Int,
    products: List<Product>,
    orders: List<Order>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(text = "Store KPI Analytics", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(
                    title = "Total Revenue",
                    value = viewModel.formatPrice(totalRevenueBhd),
                    icon = Icons.Default.TrendingUp,
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Total Orders",
                    value = totalOrders.toString(),
                    icon = Icons.Default.ShoppingCart,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AdminMetricCard(
                    title = "Pending Orders",
                    value = pendingOrdersCount.toString(),
                    icon = Icons.Default.LocalShipping,
                    color = GoldPrimary,
                    modifier = Modifier.weight(1f)
                )
                AdminMetricCard(
                    title = "Low Stock Alerts",
                    value = lowStockCount.toString(),
                    icon = Icons.Default.Warning,
                    color = if (lowStockCount > 0) RoseRed else EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Top Selling Products
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Top Best Sellers", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        items(products.filter { it.isBestSeller }.take(5)) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = product.nameEn, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(text = "${viewModel.formatPrice(product.currentPrice)} • Stock: ${product.stock}", fontSize = 11.sp, color = Color.Gray)
                    }
                    Surface(
                        color = EmeraldGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(text = "Hot Seller", color = EmeraldGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Black, color = color)
        }
    }
}

@Composable
fun AdminOrdersTab(
    viewModel: ShopViewModel,
    orders: List<Order>,
    onViewInvoice: (Order) -> Unit
) {
    var statusFilter by remember { mutableStateOf("ALL") }

    val filteredOrders = if (statusFilter == "ALL") orders else orders.filter { it.orderStatus == statusFilter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED").forEach { status ->
                    FilterChip(
                        selected = statusFilter == status,
                        onClick = { statusFilter = status },
                        label = { Text(status.replace('_', ' '), fontSize = 11.sp) }
                    )
                }
            }
        }

        items(filteredOrders, key = { it.id }) { order ->
            var expandedMenu by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Order #${order.id}", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        Text(text = viewModel.formatPrice(order.totalBhd), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Text(text = "${order.customerName} • ${order.customerPhone}", fontSize = 12.sp, color = Color.Gray)
                    Text(text = "${order.buildingNumber}, ${order.addressLine}, ${order.city}", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "Items: ${order.itemsSummary}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            OutlinedButton(
                                onClick = { expandedMenu = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = "Status: ${order.orderStatus.replace('_', ' ')}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                listOf("PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED", "REFUNDED").forEach { newStatus ->
                                    DropdownMenuItem(
                                        text = { Text(newStatus.replace('_', ' ')) },
                                        onClick = {
                                            viewModel.adminUpdateOrderStatus(order.id, newStatus)
                                            expandedMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { onViewInvoice(order) },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Invoice", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductsTab(
    viewModel: ShopViewModel,
    products: List<Product>,
    onEditProduct: (Product) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = if (searchQuery.isBlank()) products else products.filter {
        it.nameEn.contains(searchQuery, ignoreCase = true) || it.brand.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search products to edit or manage stock...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        items(filtered, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = product.nameEn, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text(text = "${viewModel.formatPrice(product.currentPrice)} • Category: ${product.categoryNameEn}", fontSize = 11.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(4.dp))

                        // Stock editor
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Stock: ${product.stock}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (product.stock <= 5) RoseRed else EmeraldGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { viewModel.adminUpdateStock(product.id, maxOf(0, product.stock - 1)) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("-", fontWeight = FontWeight.Black)
                            }
                            IconButton(
                                onClick = { viewModel.adminUpdateStock(product.id, product.stock + 1) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Text("+", fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Row {
                        IconButton(onClick = { onEditProduct(product) }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { viewModel.adminDeleteProduct(product) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RoseRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCategoriesTab(
    viewModel: ShopViewModel,
    categories: List<Category>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories, key = { it.id }) { cat ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "${cat.nameEn} / ${cat.nameAr}", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(text = "Icon key: ${cat.iconKey}", fontSize = 11.sp, color = Color.Gray)
                    }

                    IconButton(onClick = { viewModel.adminDeleteCategory(cat) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RoseRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCouponsTab(
    viewModel: ShopViewModel,
    coupons: List<Coupon>
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(coupons, key = { it.code }) { coupon ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = coupon.code, fontWeight = FontWeight.Black, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
                        Text(text = if (coupon.isPercent) "${coupon.discountPercent}% Off" else "${coupon.fixedDiscountBhd} BHD Off", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = "Min Spend: ${coupon.minSpendBhd} BHD", fontSize = 11.sp, color = Color.Gray)
                    }

                    IconButton(onClick = { viewModel.adminDeleteCoupon(coupon) }) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RoseRed, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminSettingsTab(
    viewModel: ShopViewModel,
    settings: StoreSettings
) {
    var vatRate by remember { mutableStateOf(settings.vatPercentage.toString()) }
    var deliveryFee by remember { mutableStateOf(settings.defaultDeliveryFeeBhd.toString()) }
    var freeShipThreshold by remember { mutableStateOf(settings.freeShippingThresholdBhd.toString()) }
    var phone by remember { mutableStateOf(settings.storeContactPhone) }
    var benefitNumber by remember { mutableStateOf(settings.benefitPayNumber) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(text = "Store Rates & Tax Rules (Bahrain)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
        }

        item {
            OutlinedTextField(
                value = vatRate,
                onValueChange = { vatRate = it },
                label = { Text("VAT Percentage (%)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = deliveryFee,
                onValueChange = { deliveryFee = it },
                label = { Text("Default Delivery Fee (BHD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = freeShipThreshold,
                onValueChange = { freeShipThreshold = it },
                label = { Text("Free Shipping Threshold (BHD)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = benefitNumber,
                onValueChange = { benefitNumber = it },
                label = { Text("BenefitPay Receiver Mobile / IBAN") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Store Support Hotline Phone") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    val updated = settings.copy(
                        vatPercentage = vatRate.toDoubleOrNull() ?: 10.0,
                        defaultDeliveryFeeBhd = deliveryFee.toDoubleOrNull() ?: 1.5,
                        freeShippingThresholdBhd = freeShipThreshold.toDoubleOrNull() ?: 25.0,
                        benefitPayNumber = benefitNumber,
                        storeContactPhone = phone
                    )
                    viewModel.adminSaveSettings(updated)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Save Store Settings")
            }
        }
    }
}

@Composable
fun ProductEditorDialog(
    product: Product?,
    categories: List<Category>,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var nameEn by remember { mutableStateOf(product?.nameEn.orEmpty()) }
    var nameAr by remember { mutableStateOf(product?.nameAr.orEmpty()) }
    var brand by remember { mutableStateOf(product?.brand.orEmpty()) }
    var priceStr by remember { mutableStateOf(product?.originalPriceBhd?.toString() ?: "10.000") }
    var discountPriceStr by remember { mutableStateOf(product?.discountPriceBhd?.toString() ?: "10.000") }
    var stockStr by remember { mutableStateOf(product?.stock?.toString() ?: "10") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl.orEmpty().ifBlank { "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600" }) }
    var descEn by remember { mutableStateOf(product?.descriptionEn.orEmpty()) }
    var selectedCatId by remember { mutableStateOf(product?.categoryId ?: categories.firstOrNull()?.id ?: 1L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) "Add New Product" else "Edit Product") },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = nameEn,
                        onValueChange = { nameEn = it },
                        label = { Text("English Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = nameAr,
                        onValueChange = { nameAr = it },
                        label = { Text("Arabic Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("Brand (Apple, Samsung, etc.)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it },
                            label = { Text("Original BHD") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = discountPriceStr,
                            onValueChange = { discountPriceStr = it },
                            label = { Text("Discount BHD") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Quantity") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = imageUrl,
                        onValueChange = { imageUrl = it },
                        label = { Text("Image URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = descEn,
                        onValueChange = { descEn = it },
                        label = { Text("Description") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameEn.isNotBlank()) {
                        val category = categories.find { it.id == selectedCatId } ?: categories.firstOrNull()
                        val newProduct = Product(
                            id = product?.id ?: 0L,
                            nameEn = nameEn,
                            nameAr = nameAr.ifBlank { nameEn },
                            descriptionEn = descEn.ifBlank { nameEn },
                            descriptionAr = descEn.ifBlank { nameEn },
                            categoryId = selectedCatId,
                            categoryNameEn = category?.nameEn ?: "General",
                            categoryNameAr = category?.nameAr ?: "عام",
                            originalPriceBhd = priceStr.toDoubleOrNull() ?: 10.0,
                            discountPriceBhd = discountPriceStr.toDoubleOrNull() ?: 10.0,
                            stock = stockStr.toIntOrNull() ?: 10,
                            imageUrl = imageUrl,
                            brand = brand,
                            rating = product?.rating ?: 5.0,
                            reviewCount = product?.reviewCount ?: 1
                        )
                        onSave(newProduct)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
