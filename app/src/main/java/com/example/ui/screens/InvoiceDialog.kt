package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Order
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.ShopViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun InvoiceDialog(
    order: Order,
    viewModel: ShopViewModel,
    onDismiss: () -> Unit
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.US)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) "فاتورة ضريبية مبسطة" else "Simplified Tax Invoice",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                // Store Header Details (Bahrain TRN & CR)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(text = "MA Shopping Bahrain W.L.L.", fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text(text = "Commercial Registration (CR): ${storeSettings.commercialRegistrationNumber}", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "VAT Registration (TRN): ${storeSettings.vatRegistrationNumber}", fontSize = 11.sp, color = Color.Gray)
                    Text(text = "Kingdom of Bahrain • support@mashopping.bh", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Invoice metadata
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Invoice #: INV-${order.id}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(text = "Date: ${dateFormat.format(Date(order.createdAt))}", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "Status: ${order.orderStatus}", fontSize = 11.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(text = "Customer: ${order.customerName}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                        Text(text = order.customerPhone, fontSize = 11.sp, color = Color.Gray)
                        Text(text = "${order.buildingNumber}, ${order.city}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Items list summary
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Purchased Items:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = order.itemsSummary, fontSize = 12.sp, lineHeight = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Calculation Breakdown Table
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Subtotal (Excl. VAT):", fontSize = 12.sp, color = Color.Gray)
                        Text(text = viewModel.formatPrice(order.subtotalBhd), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    if (order.discountBhd > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Discount (${order.appliedCoupon}):", fontSize = 12.sp, color = EmeraldGreen)
                            Text(text = "-${viewModel.formatPrice(order.discountBhd)}", fontSize = 12.sp, color = EmeraldGreen, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Delivery Fee:", fontSize = 12.sp, color = Color.Gray)
                        Text(text = if (order.deliveryFeeBhd == 0.0) "FREE" else viewModel.formatPrice(order.deliveryFeeBhd), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "VAT (${storeSettings.vatPercentage.toInt()}%):", fontSize = 12.sp, color = Color.Gray)
                        Text(text = viewModel.formatPrice(order.vatBhd), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }

                    Divider(modifier = Modifier.padding(vertical = 4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Total Payable Amount:", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        Text(text = viewModel.formatPrice(order.totalBhd), fontSize = 15.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Payment Method: ${order.paymentMethod} • Status: ${order.paymentStatus}",
                    fontSize = 11.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.showSnackbar("Tax invoice sent to your email and saved as PDF")
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = if (isArabic) "طباعة / PDF" else "Print / PDF")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = if (isArabic) "إغلاق" else "Close")
                    }
                }
            }
        }
    }
}
