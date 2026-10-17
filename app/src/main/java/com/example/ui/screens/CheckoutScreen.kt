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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.UserAddress
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    viewModel: ShopViewModel,
    onBackClick: () -> Unit,
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

    // Payment methods: "BENEFIT_PAY", "CREDIT_CARD", "CASH_ON_DELIVERY"
    var selectedPaymentMethod by remember { mutableStateOf("BENEFIT_PAY") }

    // Card Form Inputs (Simulated secure payment)
    var cardHolder by remember { mutableStateOf("Farid Al-Sayed") }
    var cardNumber by remember { mutableStateOf("4532 •••• •••• 8912") }
    var cardExpiry by remember { mutableStateOf("08/28") }
    var cardCvv by remember { mutableStateOf("•••") }

    // BenefitPay Simulated Inputs
    var benefitMobile by remember { mutableStateOf("+973 3948 2011") }

    // Add New Address Dialog State
    var showAddAddressDialog by remember { mutableStateOf(false) }
    var newAddressLabel by remember { mutableStateOf("Home") }
    var newRecipientName by remember { mutableStateOf("Farid Al-Sayed") }
    var newPhone by remember { mutableStateOf("+973 3948 2011") }
    var newBuilding by remember { mutableStateOf("") }
    var newRoad by remember { mutableStateOf("") }
    var newBlock by remember { mutableStateOf("") }
    var newCity by remember { mutableStateOf("Manama") }

    val subtotal = cartWithProducts.sumOf { it.product.currentPrice * it.cartItem.quantity }
    val isFreeShipping = subtotal >= storeSettings.freeShippingThresholdBhd
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isArabic) "إتمام الطلب والدفع" else "Checkout & Payment",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("checkout_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
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
                            text = if (isArabic) "المبلغ النهائي (د.ب)" else "Final Amount",
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
                        onClick = {
                            val chosenAddress = addresses.find { it.id == selectedAddressId }
                                ?: addresses.firstOrNull()
                                ?: UserAddress(
                                    label = "Default",
                                    recipientName = "Customer",
                                    phone = "+973 3999 0000",
                                    building = "Bldg 10",
                                    road = "Road 10",
                                    block = "Block 300",
                                    city = "Manama"
                                )

                            viewModel.placeOrder(
                                address = chosenAddress,
                                paymentMethod = selectedPaymentMethod,
                                onSuccess = { order ->
                                    onOrderPlaced(order)
                                }
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("confirm_order_button")
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "تأكيد الطلب الآن" else "Confirm & Pay",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Step 1: Delivery Address
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "1. عنوان التوصيل في البحرين" else "1. Delivery Address (Bahrain)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = { showAddAddressDialog = true }) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = if (isArabic) "عنوان جديد" else "Add New", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            items(addresses) { address ->
                val isSelected = address.id == selectedAddressId

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedAddressId = address.id }
                        .then(
                            if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            else Modifier
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedAddressId = address.id }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = address.label,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                if (address.isDefault) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = EmeraldGreen.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = if (isArabic) "افتراضي" else "Default",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "${address.building}, ${address.road}, ${address.block}, ${address.city}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Text(
                                text = "${address.recipientName} • ${address.phone}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Step 2: Payment Method
            item {
                Spacer(modifier = Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isArabic) "2. طريقة الدفع الآمنة" else "2. Payment Method",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // BenefitPay Option
                PaymentMethodCard(
                    title = "BenefitPay (بنفت بي)",
                    subtitle = if (isArabic) "الدفع السريع والفوري برقم الهاتف أو رمز QR في البحرين" else "Instant Fawri+ Mobile QR / Number Transfer in Bahrain",
                    icon = Icons.Default.PhoneAndroid,
                    badgeText = "Most Popular in BH 🇧🇭",
                    isSelected = selectedPaymentMethod == "BENEFIT_PAY",
                    onClick = { selectedPaymentMethod = "BENEFIT_PAY" }
                )

                if (selectedPaymentMethod == "BENEFIT_PAY") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "تفاصيل تحويل BenefitPay" else "BenefitPay Transfer Details",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "• Store IBAN / Mobile: ${storeSettings.benefitPayNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "• Account Name: MA Shopping W.L.L. (CR-984321-BH)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isArabic) "سيتم تأكيد عملية الدفع تلقائياً فور إرسال الطلب." else "Payment status will be verified instantly upon order submission.",
                                fontSize = 11.sp,
                                color = EmeraldGreen,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Credit / Debit Card Option
                Spacer(modifier = Modifier.height(6.dp))
                PaymentMethodCard(
                    title = if (isArabic) "بطاقة ائتمان / مدى / فيزا وماستركارد" else "Credit / Debit Card (Visa, Mastercard)",
                    subtitle = if (isArabic) "دفع آمن ومشفر 256-bit SSL" else "Secure 256-bit SSL Encrypted Payment",
                    icon = Icons.Default.CreditCard,
                    badgeText = "Instant",
                    isSelected = selectedPaymentMethod == "CREDIT_CARD",
                    onClick = { selectedPaymentMethod = "CREDIT_CARD" }
                )

                if (selectedPaymentMethod == "CREDIT_CARD") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = cardHolder,
                                onValueChange = { cardHolder = it },
                                label = { Text(if (isArabic) "اسم حامل البطاقة" else "Cardholder Name", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = cardNumber,
                                onValueChange = { cardNumber = it },
                                label = { Text(if (isArabic) "رقم البطاقة" else "Card Number", fontSize = 12.sp) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cardExpiry,
                                    onValueChange = { cardExpiry = it },
                                    label = { Text("MM/YY", fontSize = 12.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = cardCvv,
                                    onValueChange = { cardCvv = it },
                                    label = { Text("CVV", fontSize = 12.sp) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isArabic) "لا نقوم بحفظ أرقام البطاقة أو رموز الأمان أبداً" else "Card numbers and security codes are never stored on device",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // Cash on Delivery Option
                Spacer(modifier = Modifier.height(6.dp))
                PaymentMethodCard(
                    title = if (isArabic) "الدفع نقداً عند الاستلام (COD)" else "Cash on Delivery (COD)",
                    subtitle = if (isArabic) "ادفع نقداً لمندوب التوصيل عند استلام الشحنة" else "Pay in cash to the courier upon delivery at your door",
                    icon = Icons.Default.LocalAtm,
                    badgeText = "Cash",
                    isSelected = selectedPaymentMethod == "CASH_ON_DELIVERY",
                    onClick = { selectedPaymentMethod = "CASH_ON_DELIVERY" }
                )
            }

            // Step 3: Order Summary Review
            item {
                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = if (isArabic) "3. ملخص الحساب والرسوم" else "3. Order & Charges Breakdown",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                            Text(
                                text = if (isFreeShipping) (if (isArabic) "مجاني" else "FREE") else viewModel.formatPrice(deliveryFee),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isFreeShipping) EmeraldGreen else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = if (isArabic) "الخصم (${appliedCoupon?.code})" else "Discount (${appliedCoupon?.code})", fontSize = 13.sp, color = EmeraldGreen)
                                Text(text = "-${viewModel.formatPrice(discount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if (isArabic) "ضريبة القيمة المضافة (${storeSettings.vatPercentage.toInt()}%)" else "VAT (${storeSettings.vatPercentage.toInt()}%)", fontSize = 13.sp, color = Color.Gray)
                            Text(text = viewModel.formatPrice(vat), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if (isArabic) "المجموع الكلي النهائي" else "Final Grand Total", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(text = viewModel.formatPrice(total), fontSize = 17.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Add Address Dialog
    if (showAddAddressDialog) {
        AlertDialog(
            onDismissRequest = { showAddAddressDialog = false },
            title = { Text(text = if (isArabic) "إضافة عنوان توصيل جديد" else "Add Delivery Address") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newAddressLabel,
                        onValueChange = { newAddressLabel = it },
                        label = { Text("Label (Home, Office, Apartment)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newRecipientName,
                        onValueChange = { newRecipientName = it },
                        label = { Text("Recipient Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number (+973)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newBuilding,
                            onValueChange = { newBuilding = it },
                            label = { Text("Building/Flat") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newRoad,
                            onValueChange = { newRoad = it },
                            label = { Text("Road") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newBlock,
                            onValueChange = { newBlock = it },
                            label = { Text("Block") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newCity,
                            onValueChange = { newCity = it },
                            label = { Text("City (Manama, Riffa...)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBuilding.isNotBlank() && newRoad.isNotBlank()) {
                            viewModel.saveAddress(
                                UserAddress(
                                    label = newAddressLabel,
                                    recipientName = newRecipientName,
                                    phone = newPhone,
                                    building = newBuilding,
                                    road = newRoad,
                                    block = newBlock,
                                    city = newCity,
                                    isDefault = false
                                )
                            )
                            showAddAddressDialog = false
                        }
                    }
                ) {
                    Text(text = if (isArabic) "حفظ العنوان" else "Save Address")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddAddressDialog = false }) {
                    Text(text = if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun PaymentMethodCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeText: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = badgeText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
