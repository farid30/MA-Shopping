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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Discount
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun CartScreen(
    viewModel: ShopViewModel,
    onNavigateToCheckout: () -> Unit,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val couponError by viewModel.couponError.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    var couponInputText by remember { mutableStateOf("") }

    val subtotal = cartWithProducts.sumOf { it.product.currentPrice * it.cartItem.quantity }
    val freeShippingThreshold = storeSettings.freeShippingThresholdBhd
    val isFreeShipping = subtotal >= freeShippingThreshold
    val deliveryFee = if (isFreeShipping) 0.0 else storeSettings.defaultDeliveryFeeBhd

    var discount = 0.0
    appliedCoupon?.let { coupon ->
        if (subtotal >= coupon.minSpendBhd) {
            discount = if (coupon.isPercent) {
                subtotal * (coupon.discountPercent / 100.0)
            } else {
                coupon.fixedDiscountBhd
            }
        }
    }

    val vat = (subtotal - discount) * (storeSettings.vatPercentage / 100.0)
    val total = maxOf(0.0, (subtotal - discount) + deliveryFee + vat)

    if (cartWithProducts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("empty_cart_container"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBag,
                            contentDescription = "Empty Cart",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isArabic) "سلة التسوق فارغة حالياً" else "Your Shopping Cart is Empty",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isArabic) "تصفح أحدث المنتجات والعروض الحصرية في البحرين" else "Explore Bahrain's latest deals and best-selling tech",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onNavigateToHome,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("start_shopping_btn")
                ) {
                    Text(
                        text = if (isArabic) "ابدأ التسوق الآن" else "Start Shopping",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("cart_items_lazy_column"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Free Delivery Progress Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isFreeShipping) EmeraldGreen.copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isFreeShipping) Icons.Default.CheckCircle else Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = if (isFreeShipping) EmeraldGreen else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isFreeShipping) {
                                        if (isArabic) "مبروك! مؤهل للتوصيل المجاني في البحرين 🎉" else "Congratulations! You've unlocked FREE delivery in Bahrain 🎉"
                                    } else {
                                        val remaining = freeShippingThreshold - subtotal
                                        if (isArabic) "أضف ${viewModel.formatPrice(remaining)} أخرى للتوصيل المجاني!"
                                        else "Add ${viewModel.formatPrice(remaining)} more for FREE Delivery!"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFreeShipping) EmeraldGreen else MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }

                            if (!isFreeShipping) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val progress = (subtotal / freeShippingThreshold).coerceIn(0.0, 1.0).toFloat()
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                // Cart Product Items
                items(cartWithProducts, key = { it.cartItem.id }) { itemWithProduct ->
                    val product = itemWithProduct.product
                    val cartItem = itemWithProduct.cartItem

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("cart_item_card_${product.id}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = if (isArabic) product.nameAr else product.nameEn,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) product.nameAr else product.nameEn,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = viewModel.formatPrice(product.currentPrice),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Quantity Controls & Remove
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        IconButton(
                                            onClick = { viewModel.updateCartQuantity(cartItem.id, cartItem.quantity - 1) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(16.dp))
                                        }

                                        Text(
                                            text = cartItem.quantity.toString(),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp)
                                        )

                                        IconButton(
                                            onClick = {
                                                if (cartItem.quantity < product.stock) {
                                                    viewModel.updateCartQuantity(cartItem.id, cartItem.quantity + 1)
                                                }
                                            },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    IconButton(
                                        onClick = { viewModel.removeFromCart(cartItem.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove",
                                            tint = RoseRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Coupon / Promo Code Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Discount,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "كود الخصم والكوبونات" else "Apply Discount Coupon",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            if (appliedCoupon != null) {
                                Surface(
                                    color = EmeraldGreen.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = EmeraldGreen)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = "${appliedCoupon?.code} " + (if (isArabic) "مفعل" else "Applied"),
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = EmeraldGreen
                                                )
                                                Text(
                                                    text = if (isArabic) appliedCoupon?.descriptionAr.orEmpty() else appliedCoupon?.descriptionEn.orEmpty(),
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }

                                        IconButton(onClick = { viewModel.removeCoupon() }) {
                                            Icon(imageVector = Icons.Default.Close, contentDescription = "Remove coupon", tint = Color.Gray)
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = couponInputText,
                                        onValueChange = { couponInputText = it.uppercase() },
                                        placeholder = { Text(if (isArabic) "أدخل الكود (مثال: MASHOP10)" else "Enter code (e.g. MASHOP10)", fontSize = 12.sp) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("coupon_input_field")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            if (couponInputText.isNotBlank()) {
                                                viewModel.applyCoupon(couponInputText)
                                            }
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.testTag("apply_coupon_btn")
                                    ) {
                                        Text(text = if (isArabic) "تطبيق" else "Apply")
                                    }
                                }

                                if (couponError != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = couponError.orEmpty(),
                                        color = RoseRed,
                                        fontSize = 11.sp
                                    )
                                }

                                // Quick suggestion chips for active coupons
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("MASHOP10", "BAHRAIN25", "WELCOME").forEach { code ->
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    couponInputText = code
                                                    viewModel.applyCoupon(code)
                                                }
                                        ) {
                                            Text(
                                                text = code,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Order Price Breakdown Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = if (isArabic) "ملخص الحساب" else "Order Summary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "المجموع الفرعي" else "Subtotal", fontSize = 13.sp, color = Color.Gray)
                                Text(text = viewModel.formatPrice(subtotal), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "رسوم التوصيل" else "Delivery Charges", fontSize = 13.sp, color = Color.Gray)
                                if (isFreeShipping) {
                                    Text(
                                        text = if (isArabic) "مجاني" else "FREE",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                } else {
                                    Text(text = viewModel.formatPrice(deliveryFee), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            if (discount > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = if (isArabic) "خصم الكوبون" else "Coupon Discount", fontSize = 13.sp, color = EmeraldGreen)
                                    Text(text = "-${viewModel.formatPrice(discount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "ضريبة القيمة المضافة (${storeSettings.vatPercentage.toInt()}%)" else "VAT (${storeSettings.vatPercentage.toInt()}%)",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                Text(text = viewModel.formatPrice(vat), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }

                            Divider(color = MaterialTheme.colorScheme.surfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "الإجمالي النهائي" else "Final Total",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = viewModel.formatPrice(total),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Sticky Checkout Bar
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isArabic) "الإجمالي (شامل الضريبة)" else "Total (Inc. VAT)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = viewModel.formatPrice(total),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Button(
                        onClick = onNavigateToCheckout,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("checkout_button")
                    ) {
                        Text(
                            text = if (isArabic) "متابعة الشراء والدفع" else "Proceed to Checkout",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
