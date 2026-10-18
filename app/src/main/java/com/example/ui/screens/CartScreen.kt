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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.Product
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun CartScreen(
    viewModel: ShopViewModel,
    onNavigateToCheckout: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToProduct: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponError by viewModel.couponError.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    var couponInput by remember { mutableStateOf("") }

    val subtotal = cartWithProducts.sumOf { it.product.currentPrice * it.cartItem.quantity }
    val freeShippingThreshold = storeSettings.freeShippingThresholdBhd
    val isFreeShipping = subtotal >= freeShippingThreshold
    val shippingFee = if (isFreeShipping) 0.0 else storeSettings.defaultDeliveryFeeBhd

    var discountAmount = 0.0
    appliedCoupon?.let { coupon ->
        if (subtotal >= coupon.minSpendBhd) {
            discountAmount = if (coupon.isPercent) {
                subtotal * (coupon.discountPercent / 100.0)
            } else {
                coupon.fixedDiscountBhd
            }
        }
    }

    val vatPercentage = storeSettings.vatPercentage
    val taxableAmount = maxOf(0.0, subtotal - discountAmount)
    val vatAmount = taxableAmount * (vatPercentage / 100.0)
    val totalAmount = maxOf(0.0, taxableAmount + shippingFee + vatAmount)

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("cart_screen"),
        bottomBar = {
            if (cartWithProducts.isNotEmpty()) {
                Surface(
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (isArabic) "المبلغ الإجمالي النهائي" else "Grand Total (BHD)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = viewModel.formatPrice(totalAmount),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }

                            Button(
                                onClick = onNavigateToCheckout,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(50.dp)
                                    .testTag("btn_proceed_to_checkout")
                            ) {
                                Text(
                                    text = if (isArabic) "إتمام الطلب" else "Checkout",
                                    color = Navy900,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = Navy900,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (cartWithProducts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Navy900.copy(alpha = 0.06f),
                        modifier = Modifier.size(100.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = null,
                                tint = Navy800,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isArabic) "سلة التسوق فارغة" else "Your Shopping Cart is Empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isArabic) "تصفح أحدث المنتجات والعروض الرائعة وأضف ما يعجبك!" else "Explore our catalog, flash deals, and best-sellers now!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onNavigateToShop,
                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_start_shopping")
                    ) {
                        Text(
                            text = if (isArabic) "ابدأ التسوق الآن" else "Start Shopping",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Free delivery banner / progress
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFreeShipping) EmeraldGreen.copy(alpha = 0.12f) else GoldPrimary.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = if (isFreeShipping) EmeraldGreen else Navy900,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isFreeShipping) {
                                        if (isArabic) "مبروك! مؤهل للتوصيل المجاني داخل البحرين" else "Congrats! You unlocked FREE Bahrain Delivery!"
                                    } else {
                                        val remaining = freeShippingThreshold - subtotal
                                        if (isArabic) "أضف ${viewModel.formatPrice(remaining)} للحصول على توصيل مجاني!"
                                        else "Add ${viewModel.formatPrice(remaining)} more for FREE Bahrain delivery!"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFreeShipping) EmeraldGreen else Navy900
                                )
                            }

                            if (!isFreeShipping) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val progress = (subtotal / freeShippingThreshold).toFloat().coerceIn(0f, 1f)
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = GoldPrimary,
                                    trackColor = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }

                // Cart Items
                items(cartWithProducts, key = { it.cartItem.id }) { itemWithProduct ->
                    val product = itemWithProduct.product
                    val cartItem = itemWithProduct.cartItem

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToProduct(product) }
                            .testTag("cart_item_${cartItem.id}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.nameEn,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFF1F5F9))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) product.nameAr else product.nameEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    color = Navy900
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = viewModel.formatPrice(product.currentPrice),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy800
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable {
                                                if (cartItem.quantity > 1) {
                                                    viewModel.updateCartQuantity(cartItem.id, cartItem.quantity - 1)
                                                } else {
                                                    viewModel.removeFromCart(cartItem.id)
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Remove,
                                                contentDescription = "Decrease",
                                                tint = Navy900,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "${cartItem.quantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clickable {
                                                if (cartItem.quantity < product.stockQuantity) {
                                                    viewModel.updateCartQuantity(cartItem.id, cartItem.quantity + 1)
                                                }
                                            }
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Increase",
                                                tint = Navy900,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            IconButton(
                                onClick = { viewModel.removeFromCart(cartItem.id) },
                                modifier = Modifier.testTag("btn_remove_cart_item_${cartItem.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove",
                                    tint = RoseRed
                                )
                            }
                        }
                    }
                }

                // Coupon Entry Section
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = if (isArabic) "كود الخصم / الكوبون الترويجي" else "Promo Code / Discount Coupon",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            if (appliedCoupon != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(EmeraldGreen.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Percent,
                                            contentDescription = null,
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${appliedCoupon!!.code} (${appliedCoupon!!.descriptionEn})",
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen,
                                            fontSize = 13.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeCoupon() },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove coupon",
                                            tint = RoseRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = couponInput,
                                        onValueChange = { couponInput = it.uppercase() },
                                        placeholder = { Text(if (isArabic) "رمز الكوبون (مثل BAHRAIN10)" else "Coupon code (e.g. BAHRAIN10)") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(52.dp)
                                    )

                                    Button(
                                        onClick = {
                                            viewModel.applyCoupon(couponInput.trim())
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(52.dp)
                                    ) {
                                        Text(if (isArabic) "تطبيق" else "Apply", color = Color.White)
                                    }
                                }

                                if (couponError != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = couponError!!,
                                        color = RoseRed,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Price Summary Breakdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isArabic) "ملخص التكلفة" else "Order Summary",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            SummaryRow(
                                title = if (isArabic) "المجموع الفرعي" else "Subtotal",
                                value = viewModel.formatPrice(subtotal)
                            )

                            if (discountAmount > 0) {
                                Spacer(modifier = Modifier.height(6.dp))
                                SummaryRow(
                                    title = if (isArabic) "خصم الكوبون" else "Discount",
                                    value = "-${viewModel.formatPrice(discountAmount)}",
                                    valueColor = EmeraldGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            SummaryRow(
                                title = if (isArabic) "رسوم التوصيل (مملكة البحرين)" else "Delivery Fee (Bahrain)",
                                value = if (shippingFee == 0.0) {
                                    if (isArabic) "مجاني" else "FREE"
                                } else {
                                    viewModel.formatPrice(shippingFee)
                                },
                                valueColor = if (shippingFee == 0.0) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            SummaryRow(
                                title = if (isArabic) "ضريبة القيمة المضافة (${vatPercentage.toInt()}%)" else "VAT (${vatPercentage.toInt()}%)",
                                value = viewModel.formatPrice(vatAmount)
                            )

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "المبلغ النهائي" else "Final Total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                                Text(
                                    text = viewModel.formatPrice(totalAmount),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    title: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = valueColor)
    }
}
