package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val discountPriceBhd: Double,
    val stock: Int,
    val rating: Double = 4.8,
    val reviewCount: Int = 12,
    val isFeatured: Boolean = false,
    val isBestSeller: Boolean = false,
    val isFlashSale: Boolean = false,
    val imageUrl: String,
    val additionalImages: String = "",
    val brand: String = "",
    val specs: String = ""
) {
    val hasDiscount: Boolean
        get() = discountPriceBhd < originalPriceBhd

    val discountPercentage: Int
        get() = if (originalPriceBhd > 0 && hasDiscount) {
            (((originalPriceBhd - discountPriceBhd) / originalPriceBhd) * 100).toInt()
        } else 0

    val currentPrice: Double
        get() = if (hasDiscount) discountPriceBhd else originalPriceBhd

    val stockQuantity: Int
        get() = stock

    val originalPrice: Double
        get() = originalPriceBhd

    val discountPercent: Int
        get() = discountPercentage

    val ratingCount: Int
        get() = reviewCount

    val imageUrls: String
        get() = if (additionalImages.isNotBlank()) "$imageUrl,$additionalImages" else imageUrl
}

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nameEn: String,
    val nameAr: String,
    val iconKey: String,
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
    val id: String,
    val customerName: String,
    val customerPhone: String,
    val customerEmail: String,
    val addressLine: String,
    val buildingNumber: String,
    val blockNumber: String,
    val city: String,
    val governorate: String,
    val itemsSummary: String,
    val itemsJson: String,
    val subtotalBhd: Double,
    val deliveryFeeBhd: Double,
    val vatBhd: Double,
    val discountBhd: Double,
    val totalBhd: Double,
    val appliedCoupon: String = "",
    val paymentMethod: String,
    val paymentStatus: String,
    val orderStatus: String,
    val createdAt: Long = System.currentTimeMillis(),
    val estimatedDelivery: String = "1-2 Business Days",
    val notes: String = ""
) {
    val orderNumber: String
        get() = id

    val orderDate: String
        get() = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(createdAt))

    val totalAmount: Double
        get() = totalBhd

    val subtotalAmount: Double
        get() = subtotalBhd

    val deliveryFee: Double
        get() = deliveryFeeBhd

    val vatAmount: Double
        get() = vatBhd

    val discountAmount: Double
        get() = discountBhd

    val shippingCity: String
        get() = city

    val shippingBlock: String
        get() = blockNumber

    val shippingAddress: String
        get() = if (addressLine.isNotBlank()) addressLine else "$buildingNumber, Block $blockNumber, $city"
}

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productId: Long,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val dateString: String
) {
    val createdAt: String
        get() = dateString
}

@Entity(tableName = "coupons")
data class Coupon(
    @PrimaryKey
    val code: String,
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
    val label: String,
    val recipientName: String,
    val phone: String,
    val building: String,
    val road: String,
    val block: String,
    val city: String,
    val isDefault: Boolean = false
) {
    val title: String
        get() = label

    val governorate: String
        get() = "Capital"

    val flat: String
        get() = ""
}

@Entity(tableName = "app_notifications")
data class AppNotification(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val titleEn: String,
    val titleAr: String,
    val messageEn: String,
    val messageAr: String,
    val type: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
) {
    val createdAt: String
        get() = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(timestamp))
}

@Entity(tableName = "store_settings")
data class StoreSettings(
    @PrimaryKey
    val id: Int = 1,
    val shopNameEn: String = "MA Shopping",
    val shopNameAr: String = "إم إيه للتسوق",
    val shopTaglineEn: String = "Bahrain's Premier Online Store",
    val shopTaglineAr: String = "وجهتك الأولى للتسوق في البحرين",
    val shopAddressEn: String = "Building 450, Road 2814, Block 328, Manama, Bahrain",
    val shopAddressAr: String = "مبنى 450، طريق 2814، مجمع 328، المنامة، مملكة البحرين",
    val defaultDeliveryFeeBhd: Double = 1.500,
    val freeShippingThresholdBhd: Double = 25.000,
    val vatPercentage: Double = 10.0,
    val storeContactPhone: String = "+973 1700 8899",
    val storeContactEmail: String = "support@mashopping.bh",
    val benefitPayNumber: String = "+973 3999 1234",
    val lowStockThreshold: Int = 5,
    val commercialRegistrationNumber: String = "CR-984321-BH",
    val vatRegistrationNumber: String = "TRN-200084930100003",
    val currencyCode: String = "BHD",
    val currencySymbolAr: String = "د.ب",
    val instagramHandle: String = "@mashopping.bh",
    val operatingHours: String = "Sat - Thu: 9:00 AM - 10:00 PM"
)
