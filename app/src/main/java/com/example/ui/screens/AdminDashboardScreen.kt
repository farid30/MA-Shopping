package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Category
import com.example.data.model.Coupon
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: ShopViewModel,
    onBackToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val allProducts by viewModel.allProducts.collectAsState()
    val allCategories by viewModel.allCategories.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val allCoupons by viewModel.allCoupons.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Products, 2: Orders, 3: Custom Shop & Settings, 4: Coupons

    val totalSalesAmount = orders.filter { it.orderStatus != "Cancelled" }.sumOf { it.totalBhd }
    val totalOrdersCount = orders.size
    val pendingOrdersCount = orders.count { it.orderStatus == "Pending" || it.orderStatus == "Confirmed" || it.orderStatus == "Processing" }
    val lowStockProducts = allProducts.filter { it.stockQuantity in 1..5 }

    var showProductEditorDialog by remember { mutableStateOf(false) }
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showCouponDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_dashboard_screen"),
        topBar = {
            Surface(
                color = Navy900,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBackToStore, modifier = Modifier.testTag("btn_exit_admin")) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                            }
                            Column {
                                Text(
                                    text = if (isArabic) "لوحة إدارة ${storeSettings.shopNameAr}" else "${storeSettings.shopNameEn} Admin Portal",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isArabic) "إدارة العمليات والمنتجات والمبيعات" else "Store Operations & Live Orders",
                                    fontSize = 11.sp,
                                    color = GoldPrimary
                                )
                            }
                        }

                        Button(
                            onClick = onBackToStore,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = Navy900, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "المتجر" else "Store", fontSize = 11.sp, color = Navy900, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Admin Tab Navigation
                    val tabTitles = if (isArabic) {
                        listOf("نظرة عامة", "المنتجات", "الطلبات", "هوية المتجر", "الكوبونات")
                    } else {
                        listOf("Overview", "Products", "Orders", "Custom Shop", "Coupons")
                    }

                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Navy900,
                        contentColor = GoldPrimary
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTab == index) GoldPrimary else Color.LightGray
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> AdminOverviewTab(
                viewModel = viewModel,
                isArabic = isArabic,
                totalRevenue = totalSalesAmount,
                totalOrders = totalOrdersCount,
                pendingOrders = pendingOrdersCount,
                lowStockCount = lowStockProducts.size,
                allProductsCount = allProducts.size,
                padding = padding,
                onQuickAddProduct = {
                    editingProduct = null
                    showProductEditorDialog = true
                },
                onGoToShopSettings = { selectedTab = 3 }
            )
            1 -> AdminProductsTab(
                viewModel = viewModel,
                isArabic = isArabic,
                products = allProducts,
                padding = padding,
                onAddProduct = {
                    editingProduct = null
                    showProductEditorDialog = true
                },
                onEditProduct = { prod ->
                    editingProduct = prod
                    showProductEditorDialog = true
                },
                onDeleteProduct = { prod -> viewModel.adminDeleteProduct(prod) }
            )
            2 -> AdminOrdersTab(
                viewModel = viewModel,
                isArabic = isArabic,
                orders = orders,
                padding = padding
            )
            3 -> AdminCustomShopTab(
                viewModel = viewModel,
                isArabic = isArabic,
                padding = padding
            )
            4 -> AdminCouponsTab(
                viewModel = viewModel,
                isArabic = isArabic,
                coupons = allCoupons,
                padding = padding,
                onAddCoupon = { showCouponDialog = true },
                onDeleteCoupon = { coupon -> viewModel.adminDeleteCoupon(coupon) }
            )
        }
    }

    // Product Editor Dialog (Add / Edit)
    if (showProductEditorDialog) {
        ProductEditDialog(
            product = editingProduct,
            categories = allCategories,
            isArabic = isArabic,
            onDismiss = { showProductEditorDialog = false },
            onSave = { savedProduct ->
                viewModel.adminSaveProduct(savedProduct)
                showProductEditorDialog = false
            }
        )
    }

    // Coupon Dialog
    if (showCouponDialog) {
        CouponCreateDialog(
            isArabic = isArabic,
            onDismiss = { showCouponDialog = false },
            onSave = { newCoupon ->
                viewModel.adminSaveCoupon(newCoupon)
                showCouponDialog = false
            }
        )
    }
}

@Composable
fun AdminOverviewTab(
    viewModel: ShopViewModel,
    isArabic: Boolean,
    totalRevenue: Double,
    totalOrders: Int,
    pendingOrders: Int,
    lowStockCount: Int,
    allProductsCount: Int,
    padding: PaddingValues,
    onQuickAddProduct: () -> Unit,
    onGoToShopSettings: () -> Unit
) {
    val storeSettings by viewModel.storeSettings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Shop details banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isArabic) storeSettings.shopNameAr else storeSettings.shopNameEn,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = "CR: ${storeSettings.commercialRegistrationNumber} | VAT: ${storeSettings.vatRegistrationNumber}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedButton(
                        onClick = onGoToShopSettings,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "هوية المتجر" else "Branding", fontSize = 12.sp)
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = if (isArabic) "إجمالي المبيعات" else "Total Revenue",
                    value = viewModel.formatPrice(totalRevenue),
                    subtitle = if (isArabic) "الطلبات المكتملة والمؤكدة" else "Completed & confirmed",
                    color = EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = if (isArabic) "إجمالي الطلبات" else "Total Orders",
                    value = "$totalOrders",
                    subtitle = if (isArabic) "$pendingOrders قيد المعالجة" else "$pendingOrders pending",
                    color = Navy900,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = if (isArabic) "المنتجات النشطة" else "Active Products",
                    value = "$allProductsCount",
                    subtitle = if (isArabic) "في 8 تصنيفات" else "Across 8 categories",
                    color = Color(0xFF1E3A8A),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = if (isArabic) "تنبيه نقص المخزون" else "Low Stock Alerts",
                    value = "$lowStockCount",
                    subtitle = if (isArabic) "أقل من 5 قطع" else "Less than 5 items",
                    color = if (lowStockCount > 0) RoseRed else EmeraldGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (isArabic) "إجراءات إدارية سريعة" else "Admin Quick Actions",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onQuickAddProduct,
                            colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "إضافة منتج" else "Add Product", color = Color.White, fontSize = 12.sp)
                        }

                        Button(
                            onClick = { viewModel.generateRandomShopConcept() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "توليد هوية" else "New Concept", color = Navy900, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = subtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}

@Composable
fun AdminProductsTab(
    viewModel: ShopViewModel,
    isArabic: Boolean,
    products: List<Product>,
    padding: PaddingValues,
    onAddProduct: () -> Unit,
    onEditProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${if (isArabic) "قائمة المنتجات" else "Products Catalog"} (${products.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Button(
                    onClick = onAddProduct,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("btn_admin_add_product")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "منتج جديد" else "Add New", color = Color.White)
                }
            }
        }

        items(products, key = { it.id }) { product ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AsyncImage(
                        model = product.imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) product.nameAr else product.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Navy900,
                            maxLines = 1
                        )
                        Text(
                            text = "${viewModel.formatPrice(product.currentPrice)} • ${if (isArabic) product.categoryNameAr else product.categoryNameEn}",
                            fontSize = 12.sp,
                            color = Navy800
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (isArabic) "المخزون:" else "Stock:"} ${product.stockQuantity}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (product.stockQuantity < 5) RoseRed else EmeraldGreen
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { onEditProduct(product) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Navy900, modifier = Modifier.size(18.dp))
                        }
                        IconButton(onClick = { onDeleteProduct(product) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseRed, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrdersTab(
    viewModel: ShopViewModel,
    isArabic: Boolean,
    orders: List<Order>,
    padding: PaddingValues
) {
    val statuses = listOf("Pending", "Confirmed", "Processing", "Shipped", "Out for Delivery", "Delivered", "Cancelled")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${if (isArabic) "إدارة وتحديث طلبات العملاء" else "Customer Orders Management"} (${orders.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )
        }

        items(orders, key = { it.orderNumber }) { order ->
            var expandedMenu by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = order.orderNumber, fontWeight = FontWeight.Bold, color = Navy900)
                            Text(text = "${order.customerName} (${order.customerPhone})", fontSize = 12.sp, color = Color.Gray)
                        }
                        OrderStatusChip(status = order.orderStatus, isArabic = isArabic)
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "📍 ${order.shippingAddress}, ${order.shippingCity}",
                        fontSize = 11.sp,
                        color = Navy800
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total: ${viewModel.formatPrice(order.totalAmount)} (${order.paymentMethod})", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(text = order.orderDate, fontSize = 11.sp, color = Color.Gray)
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Update Status Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isArabic) "تغيير حالة الشحنة:" else "Update Delivery Status:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box {
                            OutlinedButton(
                                onClick = { expandedMenu = true },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(order.orderStatus, fontSize = 11.sp)
                            }

                            DropdownMenu(
                                expanded = expandedMenu,
                                onDismissRequest = { expandedMenu = false }
                            ) {
                                statuses.forEach { statusOption ->
                                    DropdownMenuItem(
                                        text = { Text(statusOption) },
                                        onClick = {
                                            viewModel.adminUpdateOrderStatus(order.orderNumber, statusOption)
                                            expandedMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// USER REQUIREMENT 4 & 6: Custom Shop Name and Shop Details Editor
@Composable
fun AdminCustomShopTab(
    viewModel: ShopViewModel,
    isArabic: Boolean,
    padding: PaddingValues
) {
    val storeSettings by viewModel.storeSettings.collectAsState()
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    var nameEn by remember(storeSettings) { mutableStateOf(storeSettings.shopNameEn) }
    var nameAr by remember(storeSettings) { mutableStateOf(storeSettings.shopNameAr) }
    var taglineEn by remember(storeSettings) { mutableStateOf(storeSettings.shopTaglineEn) }
    var taglineAr by remember(storeSettings) { mutableStateOf(storeSettings.shopTaglineAr) }
    var addressEn by remember(storeSettings) { mutableStateOf(storeSettings.shopAddressEn) }
    var addressAr by remember(storeSettings) { mutableStateOf(storeSettings.shopAddressAr) }
    var phone by remember(storeSettings) { mutableStateOf(storeSettings.storeContactPhone) }
    var email by remember(storeSettings) { mutableStateOf(storeSettings.storeContactEmail) }
    var crNumber by remember(storeSettings) { mutableStateOf(storeSettings.commercialRegistrationNumber) }
    var vatNumber by remember(storeSettings) { mutableStateOf(storeSettings.vatRegistrationNumber) }
    var vatPercent by remember(storeSettings) { mutableStateOf(storeSettings.vatPercentage.toString()) }
    var deliveryFee by remember(storeSettings) { mutableStateOf(storeSettings.defaultDeliveryFeeBhd.toString()) }
    var freeDeliveryMin by remember(storeSettings) { mutableStateOf(storeSettings.freeShippingThresholdBhd.toString()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "مولّد ومخصّص هوية المتجر" else "Custom Shop Name & Branding",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Button(
                            onClick = {
                                val g = viewModel.generateAndApplyCustomShop(null)
                                nameEn = g.nameEn
                                nameAr = g.nameAr
                                taglineEn = g.taglineEn
                                taglineAr = g.taglineAr
                                addressEn = g.addressEn
                                addressAr = g.addressAr
                                phone = g.phone
                                email = g.email
                                crNumber = g.crNumber
                                vatNumber = g.vatNumber
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "توليد وتطبيق فوري" else "Generate & Apply", color = Navy900, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "يمكنك تغيير اسم المتجر، الشعار، العنوان بالبحرين، السجل التجاري ورقم الضريبة."
                        else "Update store branding, legal CR numbers, Bahrain address, contact details, and VAT rules.",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (isArabic) "✨ توليد فوري حسب تخصص المتجر:" else "✨ Instant Generate by Niche:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(viewModel.shopNiches) { niche ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.6f)),
                                modifier = Modifier.clickable {
                                    val g = viewModel.generateAndApplyCustomShop(niche.key)
                                    nameEn = g.nameEn
                                    nameAr = g.nameAr
                                    taglineEn = g.taglineEn
                                    taglineAr = g.taglineAr
                                    addressEn = g.addressEn
                                    addressAr = g.addressAr
                                    phone = g.phone
                                    email = g.email
                                    crNumber = g.crNumber
                                    vatNumber = g.vatNumber
                                }
                            ) {
                                Text(
                                    text = if (isArabic) niche.labelAr else niche.labelEn,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Form Fields
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Store Names & Taglines", fontWeight = FontWeight.Bold, color = Navy900)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Shop Name (English)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = nameAr, onValueChange = { nameAr = it }, label = { Text("Shop Name (Arabic)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = taglineEn, onValueChange = { taglineEn = it }, label = { Text("Tagline (English)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = taglineAr, onValueChange = { taglineAr = it }, label = { Text("Tagline (Arabic)") }, modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Official Bahrain Contact & Registry", fontWeight = FontWeight.Bold, color = Navy900)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = addressEn, onValueChange = { addressEn = it }, label = { Text("Address (English)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = addressAr, onValueChange = { addressAr = it }, label = { Text("Address (Arabic)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = crNumber, onValueChange = { crNumber = it }, label = { Text("CR Number") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = vatNumber, onValueChange = { vatNumber = it }, label = { Text("VAT TRN") }, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Financial & Delivery Settings", fontWeight = FontWeight.Bold, color = Navy900)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = vatPercent, onValueChange = { vatPercent = it }, label = { Text("VAT %") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = deliveryFee, onValueChange = { deliveryFee = it }, label = { Text("Delivery (BHD)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = freeDeliveryMin, onValueChange = { freeDeliveryMin = it }, label = { Text("Free Min (BHD)") }, modifier = Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val updated = storeSettings.copy(
                                shopNameEn = nameEn,
                                shopNameAr = nameAr,
                                shopTaglineEn = taglineEn,
                                shopTaglineAr = taglineAr,
                                shopAddressEn = addressEn,
                                shopAddressAr = addressAr,
                                storeContactPhone = phone,
                                storeContactEmail = email,
                                commercialRegistrationNumber = crNumber,
                                vatRegistrationNumber = vatNumber,
                                vatPercentage = vatPercent.toDoubleOrNull() ?: 10.0,
                                defaultDeliveryFeeBhd = deliveryFee.toDoubleOrNull() ?: 1.500,
                                freeShippingThresholdBhd = freeDeliveryMin.toDoubleOrNull() ?: 20.000
                            )
                            viewModel.adminSaveSettings(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(if (isArabic) "حفظ هوية وإعدادات المتجر" else "Save Custom Shop Details", color = Navy900, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Android APK Download & Export Files Card (USER REQUIREMENT 5)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_apk_export_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF10B981),
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "ملف تطبيق الأندرويد (APK)" else "Android APK Download Package",
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900,
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Text(
                                    text = if (isArabic) "جاهز في ملف التصدير ZIP وللتثبيت" else "Included in Export ZIP & ready to install",
                                    fontSize = 11.sp,
                                    color = Color(0xFF047857),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "📦 Location: apk/app-debug.apk", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Navy900)
                            Text(text = "📦 Mirror: apk/MA-Shopping-v1.0.apk  |  ./app-debug.apk", fontSize = 11.sp, color = Color.Gray)
                            Text(text = "📊 Size: ~23.4 MB | App ID: com.aistudio.mashopping.kxmp", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { viewModel.downloadOrShareApk(context) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_admin_download_apk")
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "تحميل ملف APK وحفظه في التنزيلات" else "Download APK & Save to Device",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString("apk/app-debug.apk"))
                            viewModel.showSnackbar(if (isArabic) "تم نسخ مسار ملف APK: apk/app-debug.apk" else "Copied: apk/app-debug.apk")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("btn_admin_copy_apk")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "نسخ مسار ملف APK في المشروع" else "Copy APK File Path", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCouponsTab(
    viewModel: ShopViewModel,
    isArabic: Boolean,
    coupons: List<Coupon>,
    padding: PaddingValues,
    onAddCoupon: () -> Unit,
    onDeleteCoupon: (Coupon) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) "إدارة الكوبونات والخصومات" else "Promotional Coupons",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
                Button(
                    onClick = onAddCoupon,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isArabic) "كوبون جديد" else "Add Coupon", color = Color.White)
                }
            }
        }

        items(coupons, key = { it.code }) { coupon ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(shape = RoundedCornerShape(6.dp), color = EmeraldGreen.copy(alpha = 0.15f)) {
                            Icon(Icons.Default.Percent, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.padding(8.dp).size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(text = coupon.code, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                            Text(
                                text = if (coupon.isPercent) "${coupon.discountPercent.toInt()}% OFF" else "${viewModel.formatPrice(coupon.fixedDiscountBhd)} OFF",
                                fontSize = 12.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Min spend: ${viewModel.formatPrice(coupon.minSpendBhd)}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }

                    IconButton(onClick = { onDeleteCoupon(coupon) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = RoseRed)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductEditDialog(
    product: Product?,
    categories: List<Category>,
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (Product) -> Unit
) {
    var nameEn by remember { mutableStateOf(product?.nameEn ?: "") }
    var nameAr by remember { mutableStateOf(product?.nameAr ?: "") }
    var brand by remember { mutableStateOf(product?.brand ?: "") }
    var categoryId by remember { mutableStateOf(product?.categoryId ?: categories.firstOrNull()?.id ?: 1L) }
    var priceText by remember { mutableStateOf(product?.currentPrice?.toString() ?: "10.000") }
    var originalPriceText by remember { mutableStateOf(product?.originalPrice?.toString() ?: "15.000") }
    var discountText by remember { mutableStateOf(product?.discountPercent?.toString() ?: "0") }
    var stockText by remember { mutableStateOf(product?.stockQuantity?.toString() ?: "20") }
    var imageUrl by remember { mutableStateOf(product?.imageUrl ?: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600") }
    var descEn by remember { mutableStateOf(product?.descriptionEn ?: "") }
    var descAr by remember { mutableStateOf(product?.descriptionAr ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (product == null) (if (isArabic) "إضافة منتج جديد" else "Add New Product") else (if (isArabic) "تعديل بيانات المنتج" else "Edit Product")) },
        text = {
            LazyColumn(modifier = Modifier.height(380.dp).fillMaxWidth()) {
                item {
                    OutlinedTextField(value = nameEn, onValueChange = { nameEn = it }, label = { Text("Product Title (EN)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = nameAr, onValueChange = { nameAr = it }, label = { Text("Product Title (AR)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand (Apple, Samsung, etc)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price (BHD)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = originalPriceText, onValueChange = { originalPriceText = it }, label = { Text("Original") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = discountText, onValueChange = { discountText = it }, label = { Text("Discount %") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = stockText, onValueChange = { stockText = it }, label = { Text("Stock Qty") }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = imageUrl, onValueChange = { imageUrl = it }, label = { Text("Image URL") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = descEn, onValueChange = { descEn = it }, label = { Text("Description (EN)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = descAr, onValueChange = { descAr = it }, label = { Text("Description (AR)") }, modifier = Modifier.fillMaxWidth())
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cat = categories.find { it.id == categoryId } ?: categories.firstOrNull()
                    val toSave = product?.copy(
                        nameEn = nameEn,
                        nameAr = nameAr,
                        brand = brand,
                        categoryId = categoryId,
                        categoryNameEn = cat?.nameEn ?: "Electronics",
                        categoryNameAr = cat?.nameAr ?: "إلكترونيات",
                        originalPriceBhd = originalPriceText.toDoubleOrNull() ?: 15.0,
                        discountPriceBhd = priceText.toDoubleOrNull() ?: 10.0,
                        stock = stockText.toIntOrNull() ?: 10,
                        imageUrl = imageUrl,
                        descriptionEn = descEn,
                        descriptionAr = descAr
                    ) ?: Product(
                        nameEn = nameEn,
                        nameAr = nameAr,
                        brand = brand,
                        categoryId = categoryId,
                        categoryNameEn = cat?.nameEn ?: "Electronics",
                        categoryNameAr = cat?.nameAr ?: "إلكترونيات",
                        originalPriceBhd = originalPriceText.toDoubleOrNull() ?: 15.0,
                        discountPriceBhd = priceText.toDoubleOrNull() ?: 10.0,
                        stock = stockText.toIntOrNull() ?: 10,
                        imageUrl = imageUrl,
                        descriptionEn = descEn,
                        descriptionAr = descAr
                    )
                    onSave(toSave)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900)
            ) {
                Text(if (isArabic) "حفظ المنتج" else "Save Product", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}

@Composable
fun CouponCreateDialog(
    isArabic: Boolean,
    onDismiss: () -> Unit,
    onSave: (Coupon) -> Unit
) {
    var code by remember { mutableStateOf("MA25") }
    var discountPercent by remember { mutableStateOf("25") }
    var minSpend by remember { mutableStateOf("15.000") }
    var descEn by remember { mutableStateOf("25% OFF on electronics orders") }
    var descAr by remember { mutableStateOf("خصم 25% على الإلكترونيات") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isArabic) "إنشاء كوبون ترويجي جديد" else "Create New Coupon") },
        text = {
            Column {
                OutlinedTextField(value = code, onValueChange = { code = it.uppercase() }, label = { Text("Coupon Code") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = discountPercent, onValueChange = { discountPercent = it }, label = { Text("Discount %") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = minSpend, onValueChange = { minSpend = it }, label = { Text("Min Spend (BHD)") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = descEn, onValueChange = { descEn = it }, label = { Text("Description (EN)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val coupon = Coupon(
                        code = code.trim(),
                        descriptionEn = descEn,
                        descriptionAr = descAr,
                        discountPercent = discountPercent.toDoubleOrNull() ?: 10.0,
                        fixedDiscountBhd = 0.0,
                        minSpendBhd = minSpend.toDoubleOrNull() ?: 10.0,
                        isPercent = true,
                        isActive = true
                    )
                    onSave(coupon)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Navy900)
            ) {
                Text(if (isArabic) "حفظ الكوبون" else "Create Coupon", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
