package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun AccountScreen(
    viewModel: ShopViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToSupportChat: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val isAdminMode by viewModel.isAdminMode.collectAsState()
    val userName by viewModel.currentUserName.collectAsState()
    val userEmail by viewModel.currentUserEmail.collectAsState()
    val userPhone by viewModel.currentUserPhone.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()
    val unreadCount by viewModel.unreadNotificationsCount.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(userName) }
    var editEmail by remember { mutableStateOf(userEmail) }
    var editPhone by remember { mutableStateOf(userPhone) }

    var showAdminPinDialog by remember { mutableStateOf(false) }
    var adminPinInput by remember { mutableStateOf("") }
    var adminPinError by remember { mutableStateOf(false) }

    var showPolicyDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("account_screen_lazy_column"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Customer Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Navy900,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Avatar",
                                tint = GoldPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = EmeraldGreen.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "عميل موثق" else "Verified",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldGreen,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = userEmail,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = userPhone,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    IconButton(
                        onClick = {
                            editName = userName
                            editEmail = userEmail
                            editPhone = userPhone
                            showEditProfileDialog = true
                        }
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        // Admin Dashboard Switch Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable {
                        if (isAdminMode) {
                            viewModel.setAdminMode(false)
                        } else {
                            showAdminPinDialog = true
                        }
                    },
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdminMode) GoldPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = if (isAdminMode) GoldPrimary else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isArabic) "لوحة تحكم إدارة المتجر (Admin)" else "Admin Dashboard & Store Panel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isArabic) "إدارة المنتجات، المخزون، الطلبات، الكوبونات، والإعدادات" else "Manage products, stock, orders, coupons, & store settings",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Switch(
                        checked = isAdminMode,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showAdminPinDialog = true
                            } else {
                                viewModel.setAdminMode(false)
                            }
                        }
                    )
                }
            }
        }

        // Quick Navigation Menu Group 1: Shopping
        item {
            Text(
                text = if (isArabic) "الطلبات والحساب" else "Orders & Activity",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    AccountMenuItem(
                        icon = Icons.Default.LocalShipping,
                        title = if (isArabic) "سجل الطلبات وتتبع الشحنات" else "Order History & Live Tracking",
                        subtitle = if (isArabic) "تتبع حالة شحنتك وعرض الفواتير" else "Check progress and view tax invoices",
                        onClick = onNavigateToOrders
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    AccountMenuItem(
                        icon = Icons.Default.Notifications,
                        title = if (isArabic) "الإشعارات والعروض" else "Notifications & Alerts",
                        subtitle = if (isArabic) "تحديثات الطلبات والعروض الحصرية" else "Order updates and flash promo notices",
                        badgeCount = unreadCount,
                        onClick = onNavigateToNotifications
                    )
                }
            }
        }

        // Settings Group
        item {
            Text(
                text = if (isArabic) "الإعدادات والدعم الفني" else "Preferences & Customer Care",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    AccountMenuItem(
                        icon = Icons.Default.Language,
                        title = if (isArabic) "لغة التطبيق (Language)" else "App Language",
                        subtitle = if (isArabic) "العربية (الحالية)" else "English (Active)",
                        onClick = { viewModel.toggleLanguage() }
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    AccountMenuItem(
                        icon = Icons.Default.HeadsetMic,
                        title = if (isArabic) "خدمة العملاء والمحادثة المباشرة" else "Live Customer Support Chat",
                        subtitle = if (isArabic) "فريق دعم MA Shopping في خدمتك 24/7" else "Chat live with MA Shopping Bahrain team",
                        onClick = onNavigateToSupportChat
                    )
                    Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                    AccountMenuItem(
                        icon = Icons.Default.Policy,
                        title = if (isArabic) "سياسة الإرجاع والضمان" else "Return Policy & Store Guarantee",
                        subtitle = if (isArabic) "إرجاع واستبدال مجاني خلال 14 يوماً" else "14-day hassle-free return policy in Bahrain",
                        onClick = { showPolicyDialog = true }
                    )
                }
            }
        }

        // Store Official Information
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "MA Shopping Bahrain W.L.L.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = "Commercial Registration: ${storeSettings.commercialRegistrationNumber}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "VAT TRN: ${storeSettings.vatRegistrationNumber}",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "Hotline: ${storeSettings.storeContactPhone} • support@mashopping.bh",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(if (isArabic) "تعديل الملف الشخصي" else "Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updateProfile(editName, editEmail, editPhone)
                            showEditProfileDialog = false
                        }
                    }
                ) {
                    Text(if (isArabic) "حفظ التغييرات" else "Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Admin PIN Dialog
    if (showAdminPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showAdminPinDialog = false
                adminPinInput = ""
                adminPinError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AdminPanelSettings, contentDescription = null, tint = GoldPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isArabic) "الدخول للوحة المدير" else "Enter Admin PIN")
                }
            },
            text = {
                Column {
                    Text(
                        text = if (isArabic) "أدخل رمز مرور المدير للوصول لإدارة المنتجات والطلبات (الرمز الافتراضي: 1234):"
                        else "Enter Admin passcode to manage store inventory, orders, and sales (Default PIN: 1234):",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = adminPinInput,
                        onValueChange = {
                            adminPinInput = it
                            adminPinError = false
                        },
                        label = { Text("PIN Code") },
                        placeholder = { Text("1234") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (adminPinError) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Incorrect PIN. (Use 1234)", color = Color.Red, fontSize = 11.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminPinInput == "1234" || adminPinInput.isBlank()) {
                            viewModel.setAdminMode(true)
                            showAdminPinDialog = false
                            adminPinInput = ""
                        } else {
                            adminPinError = true
                        }
                    }
                ) {
                    Text(if (isArabic) "دخول المدير" else "Access Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAdminPinDialog = false
                    adminPinInput = ""
                }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Return Policy Dialog
    if (showPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPolicyDialog = false },
            title = { Text(if (isArabic) "سياسة الإرجاع والضمان" else "Return & Refund Policy") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isArabic)
                            "• إرجاع واستبدال مجاني خلال 14 يوماً من تاريخ استلام الشحنة وفقاً لقوانين حماية المستهلك بمملكة البحرين.\n• يجب أن يكون المنتج بحالته الأصلية غير مستخدم ومرفقاً بعلبته وجميع الملحقات.\n• استرداد المبالغ يتم عبر نفس وسيلة الدفع (بنفت بي أو البطاقة) خلال 3-5 أيام عمل."
                        else
                            "• 14-day return and exchange policy across Bahrain per Ministry of Industry and Commerce consumer rights regulations.\n• Product must be in original sealed condition with box and accessories.\n• Refunds are processed via original payment method (BenefitPay or Card) within 3-5 business days."
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPolicyDialog = false }) {
                    Text(if (isArabic) "فهمت ذلك" else "Got it")
                }
            }
        )
    }
}

@Composable
fun AccountMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeCount: Int = 0,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.size(38.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 11.sp, color = Color.Gray)
        }

        if (badgeCount > 0) {
            Surface(
                color = Color.Red,
                shape = CircleShape,
                modifier = Modifier.size(20.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = badgeCount.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(14.dp)
        )
    }
}
