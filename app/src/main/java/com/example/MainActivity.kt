package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.ui.components.AppHeader
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InvoiceDialog
import com.example.ui.screens.NotificationDialog
import com.example.ui.screens.OrderConfirmationScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.OrderTrackingScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.SupportChatDialog
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.ShopViewModel

enum class MainNavigationTab {
    HOME,
    CATEGORIES,
    WISHLIST,
    CART,
    ACCOUNT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ShopViewModel = viewModel()
            val isArabic by viewModel.isArabic.collectAsState()
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    MainAppContent(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: ShopViewModel) {
    val isArabic by viewModel.isArabic.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val selectedOrderForTracking by viewModel.selectedOrder.collectAsState()
    val invoiceOrder by viewModel.invoiceOrder.collectAsState()
    val snackbarMsg by viewModel.snackbarMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    var currentTab by remember { mutableStateOf(MainNavigationTab.HOME) }

    // Screen states
    var isCheckingOut by remember { mutableStateOf(false) }
    var confirmedOrder by remember { mutableStateOf<Order?>(null) }
    var isViewingOrderHistory by remember { mutableStateOf(false) }
    var showSupportChat by remember { mutableStateOf(false) }
    var showNotificationCenter by remember { mutableStateOf(false) }

    val totalCartItemsCount = cartWithProducts.sumOf { it.cartItem.quantity }

    // Handle back button on sub-screens
    BackHandler(
        enabled = selectedProduct != null ||
                isCheckingOut ||
                confirmedOrder != null ||
                isViewingOrderHistory ||
                selectedOrderForTracking != null ||
                isAdminMode ||
                currentTab != MainNavigationTab.HOME
    ) {
        when {
            selectedOrderForTracking != null -> viewModel.selectOrderForTracking(null)
            confirmedOrder != null -> confirmedOrder = null
            isCheckingOut -> isCheckingOut = false
            selectedProduct != null -> viewModel.selectProduct(null)
            isViewingOrderHistory -> isViewingOrderHistory = false
            isAdminMode -> viewModel.setAdminMode(false)
            currentTab != MainNavigationTab.HOME -> currentTab = MainNavigationTab.HOME
        }
    }

    // Modal Overlays
    if (showSupportChat) {
        SupportChatDialog(
            viewModel = viewModel,
            onDismiss = { showSupportChat = false }
        )
    }

    if (showNotificationCenter) {
        NotificationDialog(
            viewModel = viewModel,
            onDismiss = { showNotificationCenter = false }
        )
    }

    invoiceOrder?.let { order ->
        InvoiceDialog(
            order = order,
            viewModel = viewModel,
            onDismiss = { viewModel.selectOrderForInvoice(null) }
        )
    }

    // Main UI Router
    when {
        isAdminMode -> {
            AdminDashboardScreen(
                viewModel = viewModel,
                onExitAdmin = { viewModel.setAdminMode(false) },
                onViewInvoice = { order -> viewModel.selectOrderForInvoice(order) }
            )
        }

        selectedOrderForTracking != null -> {
            OrderTrackingScreen(
                order = selectedOrderForTracking!!,
                viewModel = viewModel,
                onBackClick = { viewModel.selectOrderForTracking(null) },
                onViewInvoiceClick = { viewModel.selectOrderForInvoice(selectedOrderForTracking) }
            )
        }

        confirmedOrder != null -> {
            OrderConfirmationScreen(
                order = confirmedOrder!!,
                viewModel = viewModel,
                onTrackOrderClick = {
                    val target = confirmedOrder
                    confirmedOrder = null
                    viewModel.selectOrderForTracking(target)
                },
                onViewInvoiceClick = { viewModel.selectOrderForInvoice(confirmedOrder) },
                onContinueShoppingClick = {
                    confirmedOrder = null
                    currentTab = MainNavigationTab.HOME
                }
            )
        }

        isCheckingOut -> {
            CheckoutScreen(
                viewModel = viewModel,
                onBackClick = { isCheckingOut = false },
                onOrderPlaced = { order ->
                    isCheckingOut = false
                    confirmedOrder = order
                }
            )
        }

        selectedProduct != null -> {
            ProductDetailScreen(
                product = selectedProduct!!,
                viewModel = viewModel,
                onBackClick = { viewModel.selectProduct(null) },
                onNavigateToCart = {
                    viewModel.selectProduct(null)
                    currentTab = MainNavigationTab.CART
                }
            )
        }

        isViewingOrderHistory -> {
            OrderHistoryScreen(
                viewModel = viewModel,
                onBackClick = { isViewingOrderHistory = false },
                onTrackOrderClick = { order ->
                    viewModel.selectOrderForTracking(order)
                },
                onViewInvoiceClick = { order ->
                    viewModel.selectOrderForInvoice(order)
                }
            )
        }

        else -> {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                topBar = {
                    AppHeader(
                        isArabic = isArabic,
                        unreadNotificationsCount = unreadNotificationsCount,
                        isAdminMode = isAdminMode,
                        onLanguageToggle = { viewModel.toggleLanguage() },
                        onNotificationsClick = { showNotificationCenter = true },
                        onAdminToggleClick = { viewModel.toggleAdminMode() }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        modifier = Modifier.testTag("bottom_nav_bar")
                    ) {
                        // Home Tab
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.HOME,
                            onClick = { currentTab = MainNavigationTab.HOME },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainNavigationTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text(text = if (isArabic) "الرئيسية" else "Home", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_home_tab")
                        )

                        // Categories Tab
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.CATEGORIES,
                            onClick = { currentTab = MainNavigationTab.CATEGORIES },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainNavigationTab.CATEGORIES) Icons.Filled.GridView else Icons.Outlined.GridView,
                                    contentDescription = "Categories"
                                )
                            },
                            label = { Text(text = if (isArabic) "التصنيفات" else "Categories", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_categories_tab")
                        )

                        // Wishlist Tab
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.WISHLIST,
                            onClick = { currentTab = MainNavigationTab.WISHLIST },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainNavigationTab.WISHLIST) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Wishlist"
                                )
                            },
                            label = { Text(text = if (isArabic) "المفضلة" else "Wishlist", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_wishlist_tab")
                        )

                        // Cart Tab with Badge
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.CART,
                            onClick = { currentTab = MainNavigationTab.CART },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        if (totalCartItemsCount > 0) {
                                            Badge {
                                                Text(text = totalCartItemsCount.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (currentTab == MainNavigationTab.CART) Icons.Filled.ShoppingCart else Icons.Outlined.ShoppingCart,
                                        contentDescription = "Cart"
                                    )
                                }
                            },
                            label = { Text(text = if (isArabic) "السلة" else "Cart", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_cart_tab")
                        )

                        // Account Tab
                        NavigationBarItem(
                            selected = currentTab == MainNavigationTab.ACCOUNT,
                            onClick = { currentTab = MainNavigationTab.ACCOUNT },
                            icon = {
                                Icon(
                                    imageVector = if (currentTab == MainNavigationTab.ACCOUNT) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "Account"
                                )
                            },
                            label = { Text(text = if (isArabic) "حسابي" else "Account", fontSize = 11.sp) },
                            modifier = Modifier.testTag("nav_account_tab")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        MainNavigationTab.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                onNavigateToCategory = { catId ->
                                    viewModel.selectCategory(catId)
                                    currentTab = MainNavigationTab.CATEGORIES
                                },
                                onNavigateToProduct = { product ->
                                    viewModel.selectProduct(product)
                                },
                                onNavigateToSupportChat = {
                                    showSupportChat = true
                                }
                            )
                        }

                        MainNavigationTab.CATEGORIES -> {
                            CategoriesScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { product ->
                                    viewModel.selectProduct(product)
                                }
                            )
                        }

                        MainNavigationTab.WISHLIST -> {
                            WishlistScreen(
                                viewModel = viewModel,
                                onNavigateToProduct = { product: Product ->
                                    viewModel.selectProduct(product)
                                },
                                onNavigateToHome = {
                                    currentTab = MainNavigationTab.HOME
                                }
                            )
                        }

                        MainNavigationTab.CART -> {
                            CartScreen(
                                viewModel = viewModel,
                                onNavigateToCheckout = {
                                    isCheckingOut = true
                                },
                                onNavigateToHome = {
                                    currentTab = MainNavigationTab.HOME
                                }
                            )
                        }

                        MainNavigationTab.ACCOUNT -> {
                            AccountScreen(
                                viewModel = viewModel,
                                onNavigateToOrders = {
                                    isViewingOrderHistory = true
                                },
                                onNavigateToSupportChat = {
                                    showSupportChat = true
                                },
                                onNavigateToNotifications = {
                                    showNotificationCenter = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
