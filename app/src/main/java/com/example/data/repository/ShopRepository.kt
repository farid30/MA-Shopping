package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Coupon
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.StoreSettings
import com.example.data.model.UserAddress
import com.example.data.model.WishlistItem
import com.example.data.seed.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext

data class CartItemWithProduct(
    val cartItem: CartItem,
    val product: Product
)

class ShopRepository(private val db: AppDatabase) {

    private val productDao = db.productDao()
    private val categoryDao = db.categoryDao()
    private val cartDao = db.cartDao()
    private val wishlistDao = db.wishlistDao()
    private val orderDao = db.orderDao()
    private val reviewDao = db.reviewDao()
    private val couponDao = db.couponDao()
    private val addressDao = db.addressDao()
    private val notificationDao = db.notificationDao()
    private val settingsDao = db.settingsDao()

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        if (settingsDao.getSettingsSync() == null) {
            settingsDao.saveSettings(SeedData.initialSettings)
        }
        val productCount = productDao.getProductCount()
        if (productCount == 0) {
            categoryDao.insertAll(SeedData.initialCategories)
            productDao.insertAll(SeedData.initialProducts)
            couponDao.insertAll(SeedData.initialCoupons)
            addressDao.insertAll(SeedData.initialAddresses)
            reviewDao.insertAll(SeedData.initialReviews)
            SeedData.initialOrders.forEach { orderDao.insertOrder(it) }
            notificationDao.insertAll(SeedData.initialNotifications)
        }
    }

    // Products
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    fun getFeaturedProducts(): Flow<List<Product>> = productDao.getFeaturedProducts()
    fun getBestSellers(): Flow<List<Product>> = productDao.getBestSellers()
    fun getFlashSales(): Flow<List<Product>> = productDao.getFlashSales()
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = productDao.getProductsByCategory(categoryId)
    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)

    suspend fun saveProduct(product: Product) = withContext(Dispatchers.IO) {
        if (product.id == 0L) {
            productDao.insertProduct(product)
        } else {
            productDao.updateProduct(product)
        }
    }

    suspend fun deleteProduct(product: Product) = withContext(Dispatchers.IO) {
        productDao.deleteProduct(product)
    }

    suspend fun updateStock(productId: Long, stock: Int) = withContext(Dispatchers.IO) {
        productDao.updateStock(productId, stock)
    }

    // Categories
    fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()
    suspend fun saveCategory(category: Category) = withContext(Dispatchers.IO) {
        if (category.id == 0L) {
            categoryDao.insertCategory(category)
        } else {
            categoryDao.updateCategory(category)
        }
    }
    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        categoryDao.deleteCategory(category)
    }

    // Cart
    fun getCartItems(): Flow<List<CartItem>> = cartDao.getCartItems()

    suspend fun addToCart(productId: Long, quantity: Int = 1) = withContext(Dispatchers.IO) {
        val existing = cartDao.getCartItemByProduct(productId)
        if (existing != null) {
            cartDao.update(existing.copy(quantity = existing.quantity + quantity))
        } else {
            cartDao.insert(CartItem(productId = productId, quantity = quantity))
        }
    }

    suspend fun updateCartQuantity(cartItemId: Long, quantity: Int) = withContext(Dispatchers.IO) {
        if (quantity <= 0) {
            cartDao.delete(CartItem(id = cartItemId, productId = 0))
        } else {
            val items = cartDao.getCartItems().firstOrNull() ?: emptyList()
            val target = items.find { it.id == cartItemId }
            if (target != null) {
                cartDao.update(target.copy(quantity = quantity))
            }
        }
    }

    suspend fun removeFromCart(cartItemId: Long) = withContext(Dispatchers.IO) {
        cartDao.delete(CartItem(id = cartItemId, productId = 0))
    }

    suspend fun clearCart() = withContext(Dispatchers.IO) {
        cartDao.clearCart()
    }

    // Wishlist
    fun getWishlistItems(): Flow<List<WishlistItem>> = wishlistDao.getWishlistItems()
    fun isWishlisted(productId: Long): Flow<Boolean> = wishlistDao.isWishlisted(productId)

    suspend fun toggleWishlist(productId: Long) = withContext(Dispatchers.IO) {
        val current = wishlistDao.getWishlistItems().firstOrNull() ?: emptyList()
        val exists = current.any { it.productId == productId }
        if (exists) {
            wishlistDao.removeFromWishlist(productId)
        } else {
            wishlistDao.addToWishlist(WishlistItem(productId = productId))
        }
    }

    // Orders
    fun getAllOrders(): Flow<List<Order>> = orderDao.getAllOrders()
    suspend fun getOrderById(orderId: String): Order? = orderDao.getOrderById(orderId)

    suspend fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerEmail: String,
        address: UserAddress,
        cartWithProducts: List<CartItemWithProduct>,
        subtotal: Double,
        deliveryFee: Double,
        vat: Double,
        discount: Double,
        total: Double,
        couponCode: String,
        paymentMethod: String,
        paymentStatus: String = if (paymentMethod == "CASH_ON_DELIVERY") "PENDING" else "PAID"
    ): Order = withContext(Dispatchers.IO) {
        val randomSuffix = (1000..9999).random()
        val orderId = "MA-$randomSuffix"

        val summary = cartWithProducts.joinToString(", ") { "${it.product.nameEn} (${it.cartItem.quantity}x)" }
        val itemsJson = cartWithProducts.joinToString(prefix = "[", postfix = "]") {
            """{"productId":${it.product.id},"title":"${it.product.nameEn}","qty":${it.cartItem.quantity},"price":${it.product.currentPrice}}"""
        }

        val order = Order(
            id = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerEmail = customerEmail,
            addressLine = "${address.building}, ${address.road}",
            buildingNumber = address.building,
            blockNumber = address.block,
            city = address.city,
            governorate = "Bahrain",
            itemsSummary = summary,
            itemsJson = itemsJson,
            subtotalBhd = subtotal,
            deliveryFeeBhd = deliveryFee,
            vatBhd = vat,
            discountBhd = discount,
            totalBhd = total,
            appliedCoupon = couponCode,
            paymentMethod = paymentMethod,
            paymentStatus = paymentStatus,
            orderStatus = "CONFIRMED",
            createdAt = System.currentTimeMillis(),
            estimatedDelivery = "1-2 Business Days"
        )

        orderDao.insertOrder(order)

        cartWithProducts.forEach { item ->
            val updatedStock = maxOf(0, item.product.stock - item.cartItem.quantity)
            productDao.updateStock(item.product.id, updatedStock)
        }

        cartDao.clearCart()

        val notification = AppNotification(
            titleEn = "Order $orderId Confirmed! 🎉",
            titleAr = "تم تأكيد طلبك $orderId بنجاح! 🎉",
            messageEn = "Thank you $customerName! Your order of ${String.format(java.util.Locale.US, "%.3f", total)} BHD is confirmed and being prepared for delivery.",
            messageAr = "شكراً لك $customerName! تم تأكيد طلبك بقيمة ${String.format(java.util.Locale.US, "%.3f", total)} د.ب وجاري تجهيزه للتوصيل.",
            type = "ORDER"
        )
        notificationDao.insertNotification(notification)

        order
    }

    suspend fun updateOrderStatus(orderId: String, status: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, status)

        val statusTextEn = when (status) {
            "CONFIRMED" -> "confirmed"
            "PROCESSING" -> "being packed"
            "SHIPPED" -> "handed to courier"
            "OUT_FOR_DELIVERY" -> "out for delivery to your address"
            "DELIVERED" -> "delivered successfully"
            "CANCELLED" -> "cancelled"
            "REFUNDED" -> "refunded"
            else -> status.lowercase()
        }
        val statusTextAr = when (status) {
            "CONFIRMED" -> "مؤكد"
            "PROCESSING" -> "جاري تجهيزه"
            "SHIPPED" -> "تم شحنه مع شركة التوصيل"
            "OUT_FOR_DELIVERY" -> "في طريقه للتوصيل الآن"
            "DELIVERED" -> "تم التوصيل بنجاح"
            "CANCELLED" -> "تم الإلغاء"
            "REFUNDED" -> "تم استرداد المبلغ"
            else -> status
        }

        notificationDao.insertNotification(
            AppNotification(
                titleEn = "Order $orderId Update: $status",
                titleAr = "تحديث الطلب $orderId: $statusTextAr",
                messageEn = "Your order $orderId is now $statusTextEn.",
                messageAr = "طلبك $orderId أصبح الآن: $statusTextAr.",
                type = "ORDER"
            )
        )
    }

    suspend fun cancelOrder(orderId: String) = withContext(Dispatchers.IO) {
        orderDao.updateOrderStatus(orderId, "CANCELLED")
        notificationDao.insertNotification(
            AppNotification(
                titleEn = "Order $orderId Cancelled",
                titleAr = "تم إلغاء الطلب $orderId",
                messageEn = "Order $orderId has been cancelled per store policy.",
                messageAr = "تم إلغاء الطلب $orderId وفقاً لسياسة المتجر.",
                type = "ORDER"
            )
        )
    }

    // Reviews
    fun getReviewsForProduct(productId: Long): Flow<List<Review>> = reviewDao.getReviewsForProduct(productId)
    suspend fun addReview(productId: Long, authorName: String, rating: Int, comment: String) = withContext(Dispatchers.IO) {
        reviewDao.insertReview(
            Review(
                productId = productId,
                authorName = authorName,
                rating = rating,
                comment = comment,
                dateString = "Just now"
            )
        )
    }

    // Coupons
    fun getAllCoupons(): Flow<List<Coupon>> = couponDao.getAllCoupons()
    suspend fun getCouponByCode(code: String): Coupon? = couponDao.getCouponByCode(code.trim().uppercase())
    suspend fun saveCoupon(coupon: Coupon) = withContext(Dispatchers.IO) {
        couponDao.insertCoupon(coupon)
    }
    suspend fun deleteCoupon(coupon: Coupon) = withContext(Dispatchers.IO) {
        couponDao.deleteCoupon(coupon)
    }

    // Addresses
    fun getAllAddresses(): Flow<List<UserAddress>> = addressDao.getAllAddresses()
    suspend fun saveAddress(address: UserAddress) = withContext(Dispatchers.IO) {
        if (address.isDefault) {
            addressDao.resetDefaults()
        }
        if (address.id == 0L) {
            addressDao.insertAddress(address)
        } else {
            addressDao.updateAddress(address)
        }
    }
    suspend fun deleteAddress(address: UserAddress) = withContext(Dispatchers.IO) {
        addressDao.deleteAddress(address)
    }
    suspend fun setDefaultAddress(id: Long) = withContext(Dispatchers.IO) {
        addressDao.resetDefaults()
        addressDao.setDefault(id)
    }

    // Notifications
    fun getNotifications(): Flow<List<AppNotification>> = notificationDao.getAllNotifications()
    fun getUnreadNotificationCount(): Flow<Int> = notificationDao.getUnreadCount()
    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        notificationDao.markAsRead(id)
    }
    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        notificationDao.markAllAsRead()
    }

    // Settings
    fun getSettings(): Flow<StoreSettings?> = settingsDao.getSettings()
    suspend fun saveSettings(settings: StoreSettings) = withContext(Dispatchers.IO) {
        settingsDao.saveSettings(settings)
    }
}
