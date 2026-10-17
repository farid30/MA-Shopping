package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.RoseRed
import com.example.ui.viewmodel.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    order: Order,
    viewModel: ShopViewModel,
    onBackClick: () -> Unit,
    onViewInvoiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    // Steps: PENDING -> CONFIRMED -> PROCESSING -> SHIPPED -> OUT_FOR_DELIVERY -> DELIVERED
    val steps = listOf(
        Triple("PENDING", if (isArabic) "تم تقديم الطلب" else "Order Placed", if (isArabic) "تم استلام طلبك بالنظام" else "Received in system"),
        Triple("CONFIRMED", if (isArabic) "تم تأكيد الطلب" else "Order Confirmed", if (isArabic) "الدفع وتفاصيل العنوان مؤكدة" else "Verified & confirmed"),
        Triple("PROCESSING", if (isArabic) "جاري التجهيز والتغليف" else "Processing", if (isArabic) "في مستودع MA Shopping بالمنامة" else "Packaging in Manama warehouse"),
        Triple("SHIPPED", if (isArabic) "تم تسليم الشحنة" else "Shipped", if (isArabic) "مع شركة الشحن السريع" else "Handed to courier"),
        Triple("OUT_FOR_DELIVERY", if (isArabic) "في الطريق للتوصيل" else "Out for Delivery", if (isArabic) "المندوب في منطقتك اليوم" else "Courier is on the way to you"),
        Triple("DELIVERED", if (isArabic) "تم التوصيل بنجاح" else "Delivered", if (isArabic) "تم الاستلام بواسطة العميل" else "Package delivered")
    )

    val currentStepIndex = when (order.orderStatus) {
        "PENDING" -> 0
        "CONFIRMED" -> 1
        "PROCESSING" -> 2
        "SHIPPED" -> 3
        "OUT_FOR_DELIVERY" -> 4
        "DELIVERED" -> 5
        else -> -1
    }

    val isCancelled = order.orderStatus == "CANCELLED"
    val isRefunded = order.orderStatus == "REFUNDED"
    val canCancel = order.orderStatus == "PENDING" || order.orderStatus == "CONFIRMED"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isArabic) "تتبع حالة الطلب #${order.id}" else "Track Order #${order.id}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("tracking_back_btn")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onViewInvoiceClick) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = "Invoice")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // Status Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            isCancelled -> RoseRed.copy(alpha = 0.12f)
                            order.orderStatus == "DELIVERED" -> EmeraldGreen.copy(alpha = 0.12f)
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isCancelled -> RoseRed
                                order.orderStatus == "DELIVERED" -> EmeraldGreen
                                else -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isCancelled) Icons.Default.Cancel else Icons.Default.LocalShipping,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (isCancelled) {
                                    if (isArabic) "تم إلغاء الطلب" else "Order Cancelled"
                                } else if (isRefunded) {
                                    if (isArabic) "تم استرداد المبلغ" else "Order Refunded"
                                } else {
                                    if (isArabic) "الحالة الحالية: ${order.orderStatus}" else "Status: ${order.orderStatus.replace('_', ' ')}"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = if (isArabic) "الموعد المتوقع: ${order.estimatedDelivery}" else "Estimated: ${order.estimatedDelivery}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
            }

            // Visual Tracking Stepper
            if (!isCancelled && !isRefunded) {
                item {
                    Text(
                        text = if (isArabic) "مراحل الشحنة والتوصيل" else "Live Tracking Steps",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                items(steps.size) { index ->
                    val step = steps[index]
                    val isCompleted = index <= currentStepIndex
                    val isCurrent = index == currentStepIndex

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                shape = CircleShape,
                                color = when {
                                    isCurrent -> GoldPrimary
                                    isCompleted -> EmeraldGreen
                                    else -> Color.LightGray.copy(alpha = 0.5f)
                                },
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    if (isCompleted) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    } else {
                                        Text(text = "${index + 1}", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (index < steps.size - 1) {
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(36.dp)
                                        .background(if (isCompleted && index < currentStepIndex) EmeraldGreen else Color.LightGray.copy(alpha = 0.4f))
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = step.second,
                                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = step.third,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Order Destination & Items Card
            item {
                Spacer(modifier = Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = if (isArabic) "عنوان التوصيل" else "Delivery Address", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Text(
                            text = "${order.addressLine}, ${order.city}, Bahrain",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = "${order.customerName} • ${order.customerPhone}",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(text = if (isArabic) "المنتجات بالطلب:" else "Items in Order:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = order.itemsSummary, fontSize = 12.sp, color = Color.DarkGray)

                        Divider(modifier = Modifier.padding(vertical = 4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = if (isArabic) "الإجمالي المدفوع:" else "Total Paid:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(text = viewModel.formatPrice(order.totalBhd), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons
                Button(
                    onClick = onViewInvoiceClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = if (isArabic) "عرض وتحميل الفاتورة الضريبية" else "Download / Print Tax Invoice")
                }

                if (canCancel) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = { showCancelConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseRed),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = if (isArabic) "إلغاء الطلب" else "Cancel Order")
                    }
                }
            }
        }
    }

    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text(if (isArabic) "تأكيد إلغاء الطلب" else "Cancel Order Confirmation") },
            text = {
                Text(
                    if (isArabic) "هل أنت متأكد من رغبتك في إلغاء الطلب #${order.id}؟ إذا تم الدفع مسبقاً فسيتم إرجاع المبلغ وفقاً لسياسة المتجر."
                    else "Are you sure you want to cancel order #${order.id}? Any pre-paid amount will be refunded per store policy."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelOrder(order.id)
                        showCancelConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseRed)
                ) {
                    Text(if (isArabic) "نعم، إلغاء الطلب" else "Yes, Cancel Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text(if (isArabic) "تراجع" else "Keep Order")
                }
            }
        )
    }
}
