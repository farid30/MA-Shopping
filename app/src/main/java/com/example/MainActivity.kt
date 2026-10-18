package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Product
import com.example.ui.components.AppHeader
import com.example.ui.screens.AccountScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CartScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.CheckoutScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NotificationScreen
import com.example.ui.screens.OrderHistoryScreen
import com.example.ui.screens.ProductDetailScreen
import com.example.ui.screens.SupportChatScreen
import com.example.ui.screens.WishlistScreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.MAShoppingTheme
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.ShopViewModel

enum class Screen {
    HOME,
    CATEGORIES,
    WISHLIST,
    CART,
    ACCOUNT,
    CHECKOUT,
    ORDERS,
    NOTIFICATIONS,
    SUPPORT_CHAT,
    ADMIN_DASHBOARD
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: ShopViewModel = viewModel()
            val isArabic by viewModel.isArabic.collectAsState()

            MAShoppingTheme {
                CompositionLocalProvider(
                    LocalLayoutDirection provides if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr
                ) {
                    ShopApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun ShopApp(viewModel: ShopViewModel) {
    val isArabic by viewModel.isArabic.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val selectedProduct by viewModel.selectedProduct.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()

    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val snackbarHostState = remember { SnackbarHostState() }

    val totalCartItemsCount = cartWithProducts.sumOf { it.cartItem.quantity }
    val totalWishlistCount = wishlistItems.size

    // Handle incoming snackbar messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissSnackbar()
        }
    }

    // Sync admin mode toggle with screen
    LaunchedEffect(isAdminMode) {
        if (isAdminMode && currentScreen != Screen.ADMIN_DASHBOARD) {
            currentScreen = Screen.ADMIN_DASHBOARD
        } else if (!isAdminMode && currentScreen == Screen.ADMIN_DASHBOARD) {
            currentScreen = Screen.HOME
        }
    }

    // Handle Back Press for nested flows
    BackHandler(enabled = selectedProduct != null || currentScreen != Screen.HOME) {
        if (selectedProduct != null) {
            viewModel.selectProduct(null)
        } else {
            when (currentScreen) {
                Screen.CHECKOUT -> currentScreen = Screen.CART
                Screen.ORDERS, Screen.NOTIFICATIONS, Screen.SUPPORT_CHAT -> currentScreen = Screen.ACCOUNT
                Screen.ADMIN_DASHBOARD -> {
                    viewModel.setAdminMode(false)
                    currentScreen = Screen.HOME
                }
                Screen.CATEGORIES, Screen.WISHLIST, Screen.CART, Screen.ACCOUNT -> currentScreen = Screen.HOME
                Screen.HOME -> { /* Exit app */ }
            }
        }
    }

    // If Product Detail Modal is open
    if (selectedProduct != null) {
        ProductDetailScreen(
            product = selectedProduct!!,
            viewModel = viewModel,
            onBack = { viewModel.selectProduct(null) },
            onNavigateToCart = {
                viewModel.selectProduct(null)
                currentScreen = Screen.CART
            }
        )
        return
    }

    // If Admin Mode is active
    if (currentScreen == Screen.ADMIN_DASHBOARD) {
        AdminDashboardScreen(
            viewModel = viewModel,
            onBackToStore = {
                viewModel.setAdminMode(false)
                currentScreen = Screen.HOME
            }
        )
        return
    }

    val showBottomNav = currentScreen in listOf(
        Screen.HOME,
        Screen.CATEGORIES,
        Screen.WISHLIST,
        Screen.CART,
        Screen.ACCOUNT
    )

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            if (showBottomNav) {
                AppHeader(
                    isArabic = isArabic,
                    unreadNotificationsCount = unreadNotificationsCount,
                    isAdminMode = isAdminMode,
                    storeSettings = storeSettings,
                    onLanguageToggle = { viewModel.toggleLanguage() },
                    onNotificationsClick = { currentScreen = Screen.NOTIFICATIONS },
                    onAdminToggleClick = {
                        viewModel.setAdminMode(true)
                        currentScreen = Screen.ADMIN_DASHBOARD
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 6.dp,
                    modifier = Modifier.testTag("bottom_nav_bar")
                ) {
                    // Home
                    NavigationBarItem(
                        selected = currentScreen == Screen.HOME,
                        onClick = { currentScreen = Screen.HOME },
                        icon = {
                            Icon(Icons.Default.Home, contentDescription = "Home")
                        },
                        label = { Text(if (isArabic) "الرئيسية" else "Home", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = Navy900,
                            indicatorColor = GoldPrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("nav_home")
                    )

                    // Categories
                    NavigationBarItem(
                        selected = currentScreen == Screen.CATEGORIES,
                        onClick = {
                            viewModel.selectCategory(null)
                            currentScreen = Screen.CATEGORIES
                        },
                        icon = {
                            Icon(Icons.Default.Category, contentDescription = "Categories")
                        },
                        label = { Text(if (isArabic) "الأقسام" else "Categories", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = Navy900,
                            indicatorColor = GoldPrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("nav_categories")
                    )

                    // Wishlist
                    NavigationBarItem(
                        selected = currentScreen == Screen.WISHLIST,
                        onClick = { currentScreen = Screen.WISHLIST },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalWishlistCount > 0) {
                                        Badge(containerColor = GoldPrimary, contentColor = Navy900) {
                                            Text("$totalWishlistCount", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Wishlist")
                            }
                        },
                        label = { Text(if (isArabic) "المفضلة" else "Wishlist", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = Navy900,
                            indicatorColor = GoldPrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("nav_wishlist")
                    )

                    // Cart
                    NavigationBarItem(
                        selected = currentScreen == Screen.CART,
                        onClick = { currentScreen = Screen.CART },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (totalCartItemsCount > 0) {
                                        Badge(containerColor = GoldPrimary, contentColor = Navy900) {
                                            Text("$totalCartItemsCount", fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            ) {
                                Icon(Icons.Default.ShoppingCart, contentDescription = "Cart")
                            }
                        },
                        label = { Text(if (isArabic) "السلة" else "Cart", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = Navy900,
                            indicatorColor = GoldPrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("nav_cart")
                    )

                    // Account / Profile
                    NavigationBarItem(
                        selected = currentScreen == Screen.ACCOUNT,
                        onClick = { currentScreen = Screen.ACCOUNT },
                        icon = {
                            Icon(Icons.Default.Person, contentDescription = "Account")
                        },
                        label = { Text(if (isArabic) "حسابي" else "Account", fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Navy900,
                            selectedTextColor = Navy900,
                            indicatorColor = GoldPrimary.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("nav_account")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToCategory = { catId ->
                        viewModel.selectCategory(catId)
                        currentScreen = Screen.CATEGORIES
                    },
                    onNavigateToProduct = { product ->
                        viewModel.selectProduct(product)
                    },
                    onNavigateToSupportChat = {
                        currentScreen = Screen.SUPPORT_CHAT
                    }
                )

                Screen.CATEGORIES -> CategoriesScreen(
                    viewModel = viewModel,
                    onNavigateToProduct = { product ->
                        viewModel.selectProduct(product)
                    }
                )

                Screen.WISHLIST -> WishlistScreen(
                    viewModel = viewModel,
                    onNavigateToProduct = { product ->
                        viewModel.selectProduct(product)
                    },
                    onNavigateToHome = {
                        currentScreen = Screen.HOME
                    }
                )

                Screen.CART -> CartScreen(
                    viewModel = viewModel,
                    onNavigateToCheckout = {
                        currentScreen = Screen.CHECKOUT
                    },
                    onNavigateToShop = {
                        currentScreen = Screen.HOME
                    },
                    onNavigateToProduct = { product ->
                        viewModel.selectProduct(product)
                    }
                )

                Screen.CHECKOUT -> CheckoutScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.CART },
                    onOrderPlaced = {
                        // Handled in confirmation view inside CheckoutScreen
                    }
                )

                Screen.ACCOUNT -> AccountScreen(
                    viewModel = viewModel,
                    onNavigateToOrders = { currentScreen = Screen.ORDERS },
                    onNavigateToNotifications = { currentScreen = Screen.NOTIFICATIONS },
                    onNavigateToSupport = { currentScreen = Screen.SUPPORT_CHAT },
                    onNavigateToAdmin = {
                        viewModel.setAdminMode(true)
                        currentScreen = Screen.ADMIN_DASHBOARD
                    }
                )

                Screen.ORDERS -> OrderHistoryScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.ACCOUNT }
                )

                Screen.NOTIFICATIONS -> NotificationScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.HOME }
                )

                Screen.SUPPORT_CHAT -> SupportChatScreen(
                    viewModel = viewModel,
                    onBack = { currentScreen = Screen.HOME }
                )

                Screen.ADMIN_DASHBOARD -> {
                    // Handled above
                }
            }
        }
    }
}
