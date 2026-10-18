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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun OrderHistoryScreen(
    viewModel: ShopViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val selectedOrderForTracking by viewModel.selectedOrder.collectAsState()
    val invoiceOrder by viewModel.invoiceOrder.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("order_history_screen"),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_orders")) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = if (isArabic) "سجل طلباتي ومتابعة الشحنات" else "My Orders & Tracking",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            }
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Surface(shape = CircleShape, color = Navy900.copy(alpha = 0.08f), modifier = Modifier.size(80.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = Navy800, modifier = Modifier.size(40.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isArabic) "لا توجد طلبات سابقة" else "No Orders Found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isArabic) "عند إتمام أي عملية شراء ستتمكن من متابعتها ومراجعة فاتورتها هنا."
                        else "When you make a purchase, you can track delivery and print official tax invoices here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.orderNumber }) { order ->
                    OrderCard(
                        order = order,
                        viewModel = viewModel,
                        isArabic = isArabic,
                        onTrack = { viewModel.selectOrderForTracking(order) },
                        onInvoice = { viewModel.selectOrderForInvoice(order) },
                        onCancel = { viewModel.cancelOrder(order.orderNumber) }
                    )
                }
            }
        }
    }

    // Tracking Dialog
    selectedOrderForTracking?.let { trackingOrder ->
        OrderTrackingDialog(
            order = trackingOrder,
            isArabic = isArabic,
            onDismiss = { viewModel.selectOrderForTracking(null) }
        )
    }

    // Invoice Dialog
    invoiceOrder?.let { invOrder ->
        OrderInvoiceDialog(
            order = invOrder,
            viewModel = viewModel,
            isArabic = isArabic,
            onDismiss = { viewModel.selectOrderForInvoice(null) }
        )
    }
}

@Composable
fun OrderCard(
    order: Order,
    viewModel: ShopViewModel,
    isArabic: Boolean,
    onTrack: () -> Unit,
    onInvoice: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = order.orderNumber,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                    color = Navy900
                )
                OrderStatusChip(status = order.orderStatus, isArabic = isArabic)
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${if (isArabic) "التاريخ:" else "Date:"} ${order.orderDate}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${if (isArabic) "عنوان التوصيل:" else "Delivery to:"} ${order.shippingCity}, Block ${order.shippingBlock}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isArabic) "طريقة الدفع:" else "Payment:"} ${order.paymentMethod}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = viewModel.formatPrice(order.totalAmount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onInvoice,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "فاتورة" else "Invoice", fontSize = 12.sp)
                }

                Button(
                    onClick = onTrack,
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(38.dp)
                ) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = if (isArabic) "تتبع" else "Track", color = Color.White, fontSize = 12.sp)
                }

                if (order.orderStatus == "Pending" || order.orderStatus == "Confirmed") {
                    TextButton(
                        onClick = onCancel,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(if (isArabic) "إلغاء" else "Cancel", color = RoseRed, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusChip(status: String, isArabic: Boolean) {
    val (bgColor, textColor, label) = when (status) {
        "Delivered" -> Triple(EmeraldGreen.copy(alpha = 0.15f), EmeraldGreen, if (isArabic) "تم التوصيل" else "Delivered")
        "Out for Delivery" -> Triple(GoldPrimary.copy(alpha = 0.2f), Color(0xFFB45309), if (isArabic) "مع المندوب" else "Out for Delivery")
        "Shipped" -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), if (isArabic) "تم الشحن" else "Shipped")
        "Processing" -> Triple(Color(0xFFE0E7FF), Color(0xFF4338CA), if (isArabic) "قيد التجهيز" else "Processing")
        "Confirmed" -> Triple(Color(0xFFFEF3C7), Color(0xFFD97706), if (isArabic) "مؤكد" else "Confirmed")
        "Cancelled" -> Triple(RoseRed.copy(alpha = 0.15f), RoseRed, if (isArabic) "ملغي" else "Cancelled")
        else -> Triple(Color(0xFFF1F5F9), Navy900, if (isArabic) "قيد المراجعة" else "Pending")
    }

    Surface(shape = RoundedCornerShape(6.dp), color = bgColor) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun OrderTrackingDialog(
    order: Order,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val steps = listOf("Confirmed", "Processing", "Shipped", "Out for Delivery", "Delivered")
    val currentStepIndex = steps.indexOf(order.orderStatus).let { if (it >= 0) it else 0 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Navy900)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${if (isArabic) "تتبع شحنة" else "Live Tracking:"} ${order.orderNumber}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Text(
                    text = "${if (isArabic) "الوجهة:" else "Destination:"} ${order.shippingAddress}, ${order.shippingCity}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${if (isArabic) "المستلم:" else "Recipient:"} ${order.customerName} (${order.customerPhone})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                steps.forEachIndexed { index, step ->
                    val isDone = index <= currentStepIndex
                    val isCurrent = index == currentStepIndex
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isDone -> EmeraldGreen
                                else -> Color(0xFFCBD5E1)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isDone) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                } else {
                                    Text("${index + 1}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            val stepName = when (step) {
                                "Confirmed" -> if (isArabic) "تم تأكيد الطلب من المتجر" else "Order Confirmed by Store"
                                "Processing" -> if (isArabic) "تجهيز وتغليف المنتجات في المستودع" else "Packing & QA at Warehouse"
                                "Shipped" -> if (isArabic) "تم تسليم الشحنة لشركة التوصيل" else "Handed to Courier Express"
                                "Out for Delivery" -> if (isArabic) "المندوب في طريقه إلى عنوانك" else "Out for Delivery to Your Door"
                                "Delivered" -> if (isArabic) "تم تسليم الطلب بنجاح للعميل" else "Successfully Delivered"
                                else -> step
                            }
                            Text(
                                text = stepName,
                                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isDone) Navy900 else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Navy900)
            ) {
                Text(if (isArabic) "إغلاق" else "Close", color = Color.White)
            }
        }
    )
}

@Composable
fun OrderInvoiceDialog(
    order: Order,
    viewModel: ShopViewModel,
    isArabic: Boolean,
    onDismiss: () -> Unit
) {
    val storeSettings by viewModel.storeSettings.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isArabic) "فاتورة ضريبية رسمية مبسطة" else "Simplified Tax Invoice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Navy900
                )
                Text(
                    text = if (isArabic) storeSettings.shopNameAr else storeSettings.shopNameEn,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GoldPrimary
                )
            }
        },
        text = {
            LazyColumn(modifier = Modifier.height(380.dp).fillMaxWidth()) {
                item {
                    // Store Details
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(text = "CR No: ${storeSettings.commercialRegistrationNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "VAT TRN: ${storeSettings.vatRegistrationNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Address: ${if (isArabic) storeSettings.shopAddressAr else storeSettings.shopAddressEn}", fontSize = 11.sp)
                            Text(text = "Contact: ${storeSettings.storeContactPhone} | ${storeSettings.storeContactEmail}", fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Invoice Metadata
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Invoice #: INV-${order.orderNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = order.orderDate, fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(text = "Billed to: ${order.customerName} (${order.customerPhone})", fontSize = 12.sp)
                    Text(text = "Delivery to: ${order.shippingAddress}, ${order.shippingCity}", fontSize = 12.sp)

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Items list
                    Text(text = "Line Items:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    order.itemsJson.split(";").filter { it.isNotBlank() }.forEach { line ->
                        Text(text = "• $line", fontSize = 11.sp, modifier = Modifier.padding(vertical = 2.dp))
                    }

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Totals
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Subtotal:", fontSize = 12.sp)
                        Text(viewModel.formatPrice(order.subtotalAmount), fontSize = 12.sp)
                    }
                    if (order.discountAmount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Discount:", fontSize = 12.sp, color = EmeraldGreen)
                            Text("-${viewModel.formatPrice(order.discountAmount)}", fontSize = 12.sp, color = EmeraldGreen)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Delivery Fee:", fontSize = 12.sp)
                        Text(if (order.deliveryFee == 0.0) "FREE" else viewModel.formatPrice(order.deliveryFee), fontSize = 12.sp)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("VAT (${storeSettings.vatPercentage.toInt()}%):", fontSize = 12.sp)
                        Text(viewModel.formatPrice(order.vatAmount), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Amount (BHD):", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                        Text(viewModel.formatPrice(order.totalAmount), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Navy900)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Payment Status:", fontSize = 12.sp)
                        Text("PAID (${order.paymentMethod})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldGreen)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    viewModel.showSnackbar(if (isArabic) "تم حفظ الفاتورة الضريبية" else "Official Tax Invoice Downloaded")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text(if (isArabic) "طباعة / حفظ PDF" else "Print / Save PDF", color = Navy900, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إغلاق" else "Close")
            }
        }
    )
}
