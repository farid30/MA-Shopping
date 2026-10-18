package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.UserAddress
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun CheckoutScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    onOrderPlaced: (Order) -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val addresses by viewModel.addresses.collectAsState()
    val cartWithProducts by viewModel.cartWithProducts.collectAsState()
    val appliedCoupon by viewModel.appliedCoupon.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    var selectedAddressId by remember(addresses) {
        mutableStateOf(addresses.firstOrNull { it.isDefault }?.id ?: addresses.firstOrNull()?.id ?: 0L)
    }

    var selectedPaymentMethod by remember { mutableStateOf("BENEFIT_PAY") } // BENEFIT_PAY, COD, CARD
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var confirmedOrder by remember { mutableStateOf<Order?>(null) }

    // Card Input fields
    var cardNumber by remember { mutableStateOf("•••• •••• •••• 4242") }
    var cardHolder by remember { mutableStateOf("FARID AL-SAYED") }
    var cardExpiry by remember { mutableStateOf("08/28") }
    var cardCvv by remember { mutableStateOf("•••") }

    val subtotal = cartWithProducts.sumOf { it.product.currentPrice * it.cartItem.quantity }
    val freeShipping = subtotal >= storeSettings.freeShippingThresholdBhd
    val deliveryFee = if (freeShipping) 0.0 else storeSettings.defaultDeliveryFeeBhd

    var discount = 0.0
    appliedCoupon?.let { coupon ->
        if (subtotal >= coupon.minSpendBhd) {
            discount = if (coupon.isPercent) subtotal * (coupon.discountPercent / 100.0) else coupon.fixedDiscountBhd
        }
    }

    val taxable = maxOf(0.0, subtotal - discount)
    val vat = taxable * (storeSettings.vatPercentage / 100.0)
    val total = maxOf(0.0, taxable + deliveryFee + vat)

    if (confirmedOrder != null) {
        OrderConfirmationView(
            order = confirmedOrder!!,
            viewModel = viewModel,
            isArabic = isArabic,
            onBackToHome = onBack,
            onTrackOrder = {
                viewModel.selectOrderForTracking(confirmedOrder)
            },
            onViewInvoice = {
                viewModel.selectOrderForInvoice(confirmedOrder)
            }
        )
        return
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("checkout_screen"),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_checkout")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (isArabic) "إتمام الشراء والدفع" else "Checkout & Payment",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            }
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
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
                            text = if (isArabic) "إجمالي المبلغ المطلوب" else "Total Payable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = viewModel.formatPrice(total),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                    }

                    Button(
                        onClick = {
                            val chosenAddress = addresses.find { it.id == selectedAddressId } ?: addresses.firstOrNull()
                            if (chosenAddress != null) {
                                viewModel.placeOrder(
                                    address = chosenAddress,
                                    paymentMethod = selectedPaymentMethod,
                                    onSuccess = { order ->
                                        confirmedOrder = order
                                        onOrderPlaced(order)
                                    }
                                )
                            } else {
                                viewModel.showSnackbar(if (isArabic) "يرجى إضافة عنوان توصيل أولاً" else "Please add a delivery address first")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("btn_confirm_order")
                    ) {
                        Text(
                            text = if (isArabic) "تأكيد الطلب الآن" else "Confirm Order",
                            fontWeight = FontWeight.Bold,
                            color = Navy900,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Step 1: Delivery Address
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Navy800)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "1. عنوان التوصيل داخل البحرين" else "1. Bahrain Delivery Address",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Navy900
                                )
                            }

                            TextButton(
                                onClick = { showAddAddressDialog = true },
                                modifier = Modifier.testTag("btn_add_address")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isArabic) "إضافة جديد" else "Add New")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (addresses.isEmpty()) {
                            Text(
                                text = if (isArabic) "لا توجد عناوين محفوظة. اضغط إضافة جديد." else "No saved addresses. Tap Add New.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            addresses.forEach { addr ->
                                val isSelected = addr.id == selectedAddressId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .border(
                                            width = if (isSelected) 1.5.dp else 1.dp,
                                            color = if (isSelected) GoldPrimary else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedAddressId = addr.id }
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedAddressId = addr.id },
                                        colors = RadioButtonDefaults.colors(selectedColor = Navy900)
                                    )
                                    Column(modifier = Modifier.padding(start = 8.dp)) {
                                        Text(
                                            text = "${addr.title} - ${addr.recipientName} (${addr.phone})",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Navy900
                                        )
                                        Text(
                                            text = "${addr.building}, ${addr.road}, Block ${addr.block}, ${addr.city}, ${addr.governorate}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Step 2: Payment Method
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Payments, contentDescription = null, tint = Navy800)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "2. طريقة الدفع المعتمدة" else "2. Payment Method",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Navy900
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // BenefitPay Option
                        PaymentOptionRow(
                            title = if (isArabic) "بنفت بي (BenefitPay)" else "BenefitPay (Instant Bahrain QR/Mobile)",
                            subtitle = if (isArabic) "الدفع الأسرع والأكثر أماناً داخل مملكة البحرين" else "Fastest, secure mobile payment across Bahrain",
                            badge = "Recommended",
                            isSelected = selectedPaymentMethod == "BENEFIT_PAY",
                            onClick = { selectedPaymentMethod = "BENEFIT_PAY" }
                        )

                        // Cash on Delivery
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentOptionRow(
                            title = if (isArabic) "الدفع عند الاستلام (COD)" else "Cash on Delivery (COD)",
                            subtitle = if (isArabic) "ادفع نقداً أو ببطاقة الصراف لمندوب التوصيل" else "Pay cash or card directly to courier at your door",
                            badge = null,
                            isSelected = selectedPaymentMethod == "COD",
                            onClick = { selectedPaymentMethod = "COD" }
                        )

                        // Credit / Debit Card
                        Spacer(modifier = Modifier.height(8.dp))
                        PaymentOptionRow(
                            title = if (isArabic) "بطاقة ائتمانية / مدى / فيزا" else "Debit / Credit Card",
                            subtitle = if (isArabic) "Visa, MasterCard, JCB بتشفير بنكي 256-bit" else "Visa, MasterCard, JCB with 256-bit SSL encryption",
                            badge = null,
                            isSelected = selectedPaymentMethod == "CARD",
                            onClick = { selectedPaymentMethod = "CARD" }
                        )

                        if (selectedPaymentMethod == "BENEFIT_PAY") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = if (isArabic) "تعليمات بنفت بي (BenefitPay):" else "BenefitPay Payment Instructions:",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF1E40AF)
                                    )
                                    Text(
                                        text = if (isArabic) "سيتم توجيه المعاملة لرقم حساب متجر ${storeSettings.shopNameAr} المرخص برقم ${storeSettings.commercialRegistrationNumber}."
                                        else "Transaction will be routed to official merchant account for ${storeSettings.shopNameEn} (CR: ${storeSettings.commercialRegistrationNumber}).",
                                        fontSize = 11.sp,
                                        color = Color(0xFF1E3A8A)
                                    )
                                }
                            }
                        }

                        if (selectedPaymentMethod == "CARD") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                            ) {
                                OutlinedTextField(
                                    value = cardHolder,
                                    onValueChange = { cardHolder = it },
                                    label = { Text("Cardholder Name") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = cardNumber,
                                    onValueChange = { cardNumber = it },
                                    label = { Text("Card Number") },
                                    leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = cardExpiry,
                                        onValueChange = { cardExpiry = it },
                                        label = { Text("MM/YY") },
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = cardCvv,
                                        onValueChange = { cardCvv = it },
                                        label = { Text("CVV") },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Step 3: Order Items summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isArabic) "عناصر الطلب (${cartWithProducts.size})" else "Order Items (${cartWithProducts.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        cartWithProducts.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${item.cartItem.quantity}x ${if (isArabic) item.product.nameAr else item.product.nameEn}",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1
                                )
                                Text(
                                    text = viewModel.formatPrice(item.product.currentPrice * item.cartItem.quantity),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Address Dialog
    if (showAddAddressDialog) {
        var title by remember { mutableStateOf("Home") }
        var recipientName by remember { mutableStateOf(viewModel.currentUserName.value) }
        var phone by remember { mutableStateOf(viewModel.currentUserPhone.value) }
        var city by remember { mutableStateOf("Manama") }
        var block by remember { mutableStateOf("328") }
        var road by remember { mutableStateOf("Road 2814") }
        var building by remember { mutableStateOf("Building 450") }
        var flat by remember { mutableStateOf("Flat 12") }

        AlertDialog(
            onDismissRequest = { showAddAddressDialog = false },
            title = { Text(if (isArabic) "إضافة عنوان توصيل في البحرين" else "Add Bahrain Address") },
            text = {
                LazyColumn(modifier = Modifier.height(340.dp)) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Title (Home, Office)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = recipientName,
                            onValueChange = { recipientName = it },
                            label = { Text("Recipient Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Bahrain Mobile Number (+973)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = city,
                            onValueChange = { city = it },
                            label = { Text("Area / City (e.g. Riffa, Seef, Muharraq)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = block,
                                onValueChange = { block = it },
                                label = { Text("Block") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = road,
                                onValueChange = { road = it },
                                label = { Text("Road") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = building,
                                onValueChange = { building = it },
                                label = { Text("Building") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = flat,
                                onValueChange = { flat = it },
                                label = { Text("Flat / Apt") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newAddress = UserAddress(
                            label = title,
                            recipientName = recipientName,
                            phone = phone,
                            building = building,
                            road = road,
                            block = block,
                            city = city,
                            isDefault = addresses.isEmpty()
                        )
                        viewModel.saveAddress(newAddress)
                        showAddAddressDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(if (isArabic) "حفظ العنوان" else "Save Address", color = Navy900, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAddressDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun PaymentOptionRow(
    title: String,
    subtitle: String,
    badge: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) Navy900 else Color(0xFFE2E8F0),
                shape = RoundedCornerShape(8.dp)
            )
            .background(if (isSelected) Navy900.copy(alpha = 0.04f) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = Navy900)
        )
        Column(modifier = Modifier.padding(start = 8.dp).weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Navy900)
                if (badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = GoldPrimary) {
                        Text(text = badge, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Navy900, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                    }
                }
            }
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun OrderConfirmationView(
    order: Order,
    viewModel: ShopViewModel,
    isArabic: Boolean,
    onBackToHome: () -> Unit,
    onTrackOrder: () -> Unit,
    onViewInvoice: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp)
            .testTag("order_confirmation_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Surface(
                shape = CircleShape,
                color = EmeraldGreen.copy(alpha = 0.15f),
                modifier = Modifier.size(90.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isArabic) "تم استلام طلبك بنجاح!" else "Order Confirmed!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Navy900
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isArabic) "رقم الطلب: ${order.orderNumber}" else "Order ID: ${order.orderNumber}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = GoldPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isArabic) "شكراً لتسوقك معنا. سيصلك مندوبنا خلال 24 ساعة في مملكة البحرين."
                else "Thank you for shopping with us! Estimated delivery within 24 hours across Bahrain.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "طريقة الدفع:" else "Payment:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(order.paymentMethod, fontWeight = FontWeight.Bold, color = Navy900)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "المبلغ الإجمالي:" else "Total Amount:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(viewModel.formatPrice(order.totalAmount), fontWeight = FontWeight.Bold, color = Navy900)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(if (isArabic) "عنوان التوصيل:" else "Delivery To:", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${order.shippingCity}, ${order.shippingBlock}", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onViewInvoice,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "الفاتورة" else "Invoice")
                }

                Button(
                    onClick = onTrackOrder,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isArabic) "تتبع الشحنة" else "Track Order", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(onClick = onBackToHome) {
                Text(if (isArabic) "العودة للرئيسية" else "Continue Shopping", color = Navy800, fontWeight = FontWeight.Bold)
            }
        }
    }
}
