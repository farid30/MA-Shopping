package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameAr: String,
    val descriptionEn: String,
    val descriptionAr: String,
    val categoryId: Long,
    val categoryNameEn: String,
    val categoryNameAr: String,
    val originalPriceBhd: Double,
    val discountPriceBhd: Double, // If equals originalPriceBhd, no discount
    val stock: Int,
    val rating: Double = 4.8,
    val reviewCount: Int = 12,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isFlashSale: Boolean = false,
    val imageUrl: String,
    val additionalImages: String = "", // Comma-separated image URLs
    val brand: String = "",
    val specs: String = "" // Specification bullet points
) {
    val hasDiscount: Boolean
        get() = discountPriceBhd < originalPriceBhd

    val discountPercentage: Int
        get() = if (originalPriceBhd > 0 && hasDiscount) {
            (((originalPriceBhd - discountPriceBhd) / originalPriceBhd) * 100).toInt()
        } else 0

    val currentPrice: Double
        get() = if (hasDiscount) discountPriceBhd else originalPriceBhd
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameAr: String,
    val iconKey: String, // e.g., "phone", "tv", "laptop", "fashion", "kitchen", "beauty", "tools", "home", "other"
    val bannerUrl: String = "",
    val productCount: Int = 0
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val quantity: Int = 1,
    val selectedVariant: String = ""
)

@Entity(tableName = "wishlist_items")
data class WishlistItem(
    @PrimaryKey
    val productId: Long,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey
    val id: String, // e.g., "MA-8041"
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val addressLine: String,
    val buildingNumber: String,
    val blockNumber: String,
    val city: String, // Manama, Muharraq, Riffa, etc.
    val governorate: String, // Capital, Muharraq, Northern, Southern
    val itemsSummary: String, // Short summary of items
    val itemsJson: String, // Serialized list of items with qty, price, title
    val subtotalBhd: Double,
    val deliveryFeeBhd: Double,
    val vatBhd: Double,
    val discountBhd: Double,
    val totalBhd: Double,
    val appliedCoupon: String = "",
    val paymentMethod: String, // "CASH_ON_DELIVERY", "BENEFIT_PAY", "CREDIT_CARD"
    val paymentStatus: String, // "PAID", "PENDING", "REFUNDED"
    val orderStatus: String, // "PENDING", "CONFIRMED", "PROCESSING", "SHIPPED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED", "REFUNDED"
    val createdAt: Long = System.currentTimeMillis(),
    val estimatedDelivery: String = "1-2 Business Days",
    val notes: String = ""
)

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val dateString: String
)

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey
    val code: String, // e.g. "MASHOP10"
    val discountPercent: Double = 0.0,
    val fixedDiscountBhd: Double = 0.0,
    val isPercent: Boolean = true,
    val minSpendBhd: Double = 0.0,
    val descriptionEn: String = "",
    val descriptionAr: String = "",
    val isActive: Boolean = true
)

@Entity(tableName = "addresses")
data class UserAddress(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String, // Home, Office, Apartment
    val recipientName: String,
    val phone: String,
    val building: String,
    val road: String,
    val block: String,
    val city: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleEn: String,
    val titleAr: String,
    val messageEn: String,
    val messageAr: String,
    val type: String, // "ORDER", "PROMO", "SYSTEM"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "store_settings")
data class StoreSettings(
    @PrimaryKey
    val id: Int = 1,
    val defaultDeliveryFeeBhd: Double = 1.500,
    val freeShippingThresholdBhd: Double = 25.000,
    val vatPercentage: Double = 10.0,
    val storeContactPhone: String = "+973 1700 8899",
    val storeContactEmail: String = "support@mashopping.bh",
    val benefitPayNumber: String = "+973 3999 1234",
    val lowStockThreshold: Int = 5,
    val commercialRegistrationNumber: String = "CR-984321-BH",
    val vatRegistrationNumber: String = "TRN-200084930100003"
)
