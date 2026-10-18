package com.example

import com.example.data.model.Coupon
import com.example.data.model.Product
import com.example.data.model.StoreSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ShopBusinessLogicTest {

    @Test
    fun testBhdPriceFormattingAndDiscounts() {
        val product = Product(
            id = 1,
            nameEn = "iPhone 15 Pro",
            nameAr = "آيفون 15 برو",
            descriptionEn = "Titanium design",
            descriptionAr = "تصميم تيتانيوم",
            categoryId = 2,
            categoryNameEn = "Mobile Phones",
            categoryNameAr = "هواتف ذكية",
            originalPriceBhd = 420.000,
            discountPriceBhd = 389.900,
            stock = 15,
            imageUrl = "https://example.com/iphone.jpg"
        )

        assertTrue(product.hasDiscount)
        assertEquals(389.900, product.currentPrice, 0.001)
        assertEquals(7, product.discountPercentage)
    }

    @Test
    fun testCouponAndVatCalculation() {
        val subtotal = 100.000
        val coupon = Coupon(
            code = "SAVE10",
            discountPercent = 10.0,
            minSpendBhd = 20.0,
            isPercent = true
        )

        val discount = subtotal * (coupon.discountPercent / 100.0)
        assertEquals(10.000, discount, 0.001)

        val taxable = subtotal - discount
        val vat = taxable * 0.10 // 10% Bahrain VAT
        assertEquals(9.000, vat, 0.001)

        val total = taxable + vat // free delivery above threshold
        assertEquals(99.000, total, 0.001)
    }

    @Test
    fun testStoreSettingsDefaults() {
        val settings = StoreSettings()
        assertEquals("MA Shopping", settings.shopNameEn)
        assertEquals("إم إيه للتسوق", settings.shopNameAr)
        assertEquals("BHD", settings.currencyCode)
        assertEquals("د.ب", settings.currencySymbolAr)
        assertEquals(10.0, settings.vatPercentage, 0.01)
    }
}
