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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.Navy800
import com.example.ui.theme.Navy900
import com.example.ui.viewmodel.ShopViewModel

@Composable
fun AccountScreen(
    viewModel: ShopViewModel,
    onNavigateToOrders: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic by viewModel.isArabic.collectAsState()
    val userName by viewModel.currentUserName.collectAsState()
    val userEmail by viewModel.currentUserEmail.collectAsState()
    val userPhone by viewModel.currentUserPhone.collectAsState()
    val storeSettings by viewModel.storeSettings.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showCustomShopDialog by remember { mutableStateOf(false) }
    var showApkGuideDialog by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("account_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = userName.take(1).uppercase(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Navy900
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = userPhone,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.testTag("btn_edit_profile")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = Navy800)
                    }
                }
            }
        }

        // Custom Shop Name & Details Generator (USER REQUIREMENT 4 & 6)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_shop_details_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = GoldPrimary.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Store, contentDescription = null, tint = Navy900, modifier = Modifier.size(20.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "هوية المتجر وتفاصيل المتجر" else "Store Identity & Shop Details",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Navy900
                                )
                                Text(
                                    text = if (isArabic) "تخصيص الاسم والبيانات الرسمية" else "Customize name, address & CR",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.generateRandomShopConcept() },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("btn_randomize_shop")
                        ) {
                            Icon(Icons.Default.Casino, contentDescription = null, tint = Navy900, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isArabic) "توليد تلقائي" else "Generate", fontSize = 11.sp, color = Navy900, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Active Shop Details Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = storeSettings.shopNameEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Navy900
                                )
                                Text(
                                    text = storeSettings.shopNameAr,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = GoldPrimary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isArabic) storeSettings.shopTaglineAr else storeSettings.shopTaglineEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Divider(modifier = Modifier.padding(vertical = 8.dp))

                            Text(
                                text = "📍 ${if (isArabic) storeSettings.shopAddressAr else storeSettings.shopAddressEn}",
                                fontSize = 11.sp,
                                color = Navy800
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📞 ${storeSettings.storeContactPhone}  |  ✉️ ${storeSettings.storeContactEmail}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "🏛️ CR: ${storeSettings.commercialRegistrationNumber}  |  TRN: ${storeSettings.vatRegistrationNumber}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Niche category chips
                    Text(
                        text = if (isArabic) "✨ توليد اسم وهوية المتجر حسب التخصص:" else "✨ Generate Custom Shop by Niche:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(viewModel.shopNiches) { niche ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = GoldPrimary.copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary),
                                modifier = Modifier.clickable { viewModel.generateAndApplyCustomShop(niche.key) }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Navy900, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) niche.labelAr else niche.labelEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Navy900
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Shop Presets horizontal chips
                    Text(
                        text = if (isArabic) "أو اختر مفهوماً جاهزاً للمتجر:" else "Or select a Bahrain store concept:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(viewModel.shopPresets) { preset ->
                            val isCurrent = storeSettings.shopNameEn == preset.nameEn
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCurrent) Navy900 else Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) Navy900 else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { viewModel.applyShopConcept(preset) }
                            ) {
                                Text(
                                    text = if (isArabic) preset.nameAr else preset.nameEn,
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isCurrent) Color.White else Navy900,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showCustomShopDialog = true },
                        modifier = Modifier.fillMaxWidth().testTag("btn_custom_shop_details")
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isArabic) "تعديل بيانات واسم المتجر يدوياً" else "Edit Custom Shop Details Manually")
                    }
                }
            }
        }

        // Android APK Download & Installation Center (USER REQUIREMENT 5)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apk_download_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldGreen.copy(alpha = 0.7f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = EmeraldGreen,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Android, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "ملف تثبيت التطبيق (APK Ready)" else "Android APK Download Files",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Navy900
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isArabic) "جاهز في ملف التصدير (ZIP) وللتثبيت المباشر" else "Included in Export ZIP & ready to install",
                                        fontSize = 11.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        shadowElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📦 apk/app-debug.apk  (23.4 MB, v1.0)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Navy900
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📦 apk/MA-Shopping-v1.0.apk  |  ./app-debug.apk",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isArabic)
                                    "عند تصدير هذا المشروع (Export ZIP) ستجد ملفات APK مباشرة داخل مجلد apk/ لتثبيته فوراً على أي هاتف أو جهاز لوحي بنظام أندرويد."
                                else
                                    "When exporting this project as a ZIP, the APK files are in the 'apk/' folder ready to install directly on any Android phone or tablet.",
                                fontSize = 11.sp,
                                color = Navy800
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showApkGuideDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            modifier = Modifier.weight(1f).testTag("btn_apk_install_guide")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "طريقة التثبيت" else "Install Guide", fontSize = 12.sp, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString("apk/app-debug.apk"))
                                viewModel.showSnackbar(if (isArabic) "تم نسخ مسار ملف APK: apk/app-debug.apk" else "Copied APK path: apk/app-debug.apk")
                            },
                            modifier = Modifier.weight(1f).testTag("btn_copy_apk_path")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "نسخ المسار" else "Copy Path", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Account Quick Actions
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    AccountOptionRow(
                        icon = Icons.Default.ReceiptLong,
                        title = if (isArabic) "طلباتي ومتابعة الشحنات" else "My Orders & Tracking",
                        subtitle = if (isArabic) "مراجعة الطلبات وتتبع التوصيل" else "View past orders & invoices",
                        onClick = onNavigateToOrders
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    AccountOptionRow(
                        icon = Icons.Default.Notifications,
                        title = if (isArabic) "الإشعارات والعروض" else "Notifications & Alerts",
                        subtitle = if (isArabic) "العروض الخاصة وحالة الشحنات" else "Order updates and promos",
                        onClick = onNavigateToNotifications
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    AccountOptionRow(
                        icon = Icons.Default.HeadsetMic,
                        title = if (isArabic) "المحادثة مع خدمة العملاء" else "Customer Support Chat",
                        subtitle = if (isArabic) "مساعدة فورية حول التوصيل والدفع" else "Instant support for Bahrain orders",
                        onClick = onNavigateToSupport
                    )

                    Divider(modifier = Modifier.padding(horizontal = 16.dp))

                    // Language toggle row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleLanguage() }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Language, contentDescription = null, tint = Navy900)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isArabic) "اللغة (English / العربية)" else "Language (English / العربية)",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Navy900
                                )
                                Text(
                                    text = if (isArabic) "العربية (الحالية)" else "English (Active)",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GoldPrimary.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = if (isArabic) "EN" else "عربي",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Navy900,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Admin Access Switch
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onNavigateToAdmin)
                    .testTag("admin_dashboard_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Navy900)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = GoldPrimary,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = Navy900)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isArabic) "لوحة تحكم إدارة المتجر" else "Admin Dashboard & Control",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = if (isArabic) "إدارة المنتجات، المخزون، الطلبات والمبيعات" else "Manage products, inventory & orders",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = GoldPrimary)
                }
            }
        }

        // Store Compliance & Info
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${storeSettings.shopNameEn} - Version 1.0",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
                Text(
                    text = "Regulated under Ministry of Industry and Commerce, Kingdom of Bahrain",
                    fontSize = 10.sp,
                    color = Color.LightGray
                )
            }
        }
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var nameInput by remember { mutableStateOf(userName) }
        var emailInput by remember { mutableStateOf(userEmail) }
        var phoneInput by remember { mutableStateOf(userPhone) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(if (isArabic) "تعديل الملف الشخصي" else "Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Bahrain Mobile Number (+973)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(nameInput, emailInput, phoneInput)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900)
                ) {
                    Text(if (isArabic) "حفظ التغييرات" else "Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // Custom Shop Name & Details Dialog (USER REQUIREMENT 4 & 6)
    if (showCustomShopDialog) {
        var nameEnInput by remember { mutableStateOf(storeSettings.shopNameEn) }
        var nameArInput by remember { mutableStateOf(storeSettings.shopNameAr) }
        var taglineEnInput by remember { mutableStateOf(storeSettings.shopTaglineEn) }
        var taglineArInput by remember { mutableStateOf(storeSettings.shopTaglineAr) }
        var addressEnInput by remember { mutableStateOf(storeSettings.shopAddressEn) }
        var addressArInput by remember { mutableStateOf(storeSettings.shopAddressAr) }
        var phoneInput by remember { mutableStateOf(storeSettings.storeContactPhone) }
        var emailInput by remember { mutableStateOf(storeSettings.storeContactEmail) }
        var crInput by remember { mutableStateOf(storeSettings.commercialRegistrationNumber) }
        var vatInput by remember { mutableStateOf(storeSettings.vatRegistrationNumber) }

        AlertDialog(
            onDismissRequest = { showCustomShopDialog = false },
            title = {
                Text(
                    text = if (isArabic) "تخصيص اسم وبيانات المتجر" else "Custom Shop Name & Details",
                    fontWeight = FontWeight.Bold,
                    color = Navy900
                )
            },
            text = {
                LazyColumn(modifier = Modifier.height(380.dp)) {
                    item {
                        Button(
                            onClick = {
                                val generated = viewModel.generateCustomShopDetails()
                                nameEnInput = generated.nameEn
                                nameArInput = generated.nameAr
                                taglineEnInput = generated.taglineEn
                                taglineArInput = generated.taglineAr
                                addressEnInput = generated.addressEn
                                addressArInput = generated.addressAr
                                phoneInput = generated.phone
                                emailInput = generated.email
                                crInput = generated.crNumber
                                vatInput = generated.vatNumber
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                            modifier = Modifier.fillMaxWidth().testTag("btn_autofill_custom_shop")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Navy900, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "توليد وتعبئة بيانات جديدة تلقائياً" else "Generate & Auto-Fill Details", color = Navy900, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isArabic) "أو اختر تخصص المتجر للتوليد الفوري:" else "Or pick a niche to auto-generate:",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(viewModel.shopNiches) { n ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Navy900.copy(alpha = 0.08f),
                                    modifier = Modifier.clickable {
                                        val g = viewModel.generateCustomShopDetails(n.key)
                                        nameEnInput = g.nameEn
                                        nameArInput = g.nameAr
                                        taglineEnInput = g.taglineEn
                                        taglineArInput = g.taglineAr
                                        addressEnInput = g.addressEn
                                        addressArInput = g.addressAr
                                        phoneInput = g.phone
                                        emailInput = g.email
                                        crInput = g.crNumber
                                        vatInput = g.vatNumber
                                    }
                                ) {
                                    Text(
                                        text = if (isArabic) n.labelAr else n.labelEn,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Navy900,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = nameEnInput,
                            onValueChange = { nameEnInput = it },
                            label = { Text("Shop Name (English)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_shop_name_en")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = nameArInput,
                            onValueChange = { nameArInput = it },
                            label = { Text("Shop Name (Arabic)") },
                            modifier = Modifier.fillMaxWidth().testTag("input_shop_name_ar")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = taglineEnInput,
                            onValueChange = { taglineEnInput = it },
                            label = { Text("Shop Tagline (English)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = taglineArInput,
                            onValueChange = { taglineArInput = it },
                            label = { Text("Shop Tagline (Arabic)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = addressEnInput,
                            onValueChange = { addressEnInput = it },
                            label = { Text("Bahrain Address (English)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = addressArInput,
                            onValueChange = { addressArInput = it },
                            label = { Text("Bahrain Address (Arabic)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = phoneInput,
                            onValueChange = { phoneInput = it },
                            label = { Text("Contact Phone") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Contact Email") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = crInput,
                                onValueChange = { crInput = it },
                                label = { Text("CR Number") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = vatInput,
                                onValueChange = { vatInput = it },
                                label = { Text("VAT TRN") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = storeSettings.copy(
                            shopNameEn = nameEnInput,
                            shopNameAr = nameArInput,
                            shopTaglineEn = taglineEnInput,
                            shopTaglineAr = taglineArInput,
                            shopAddressEn = addressEnInput,
                            shopAddressAr = addressArInput,
                            storeContactPhone = phoneInput,
                            storeContactEmail = emailInput,
                            commercialRegistrationNumber = crInput,
                            vatRegistrationNumber = vatInput
                        )
                        viewModel.adminSaveSettings(updated)
                        showCustomShopDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                    modifier = Modifier.testTag("btn_save_custom_shop")
                ) {
                    Text(if (isArabic) "حفظ هوية المتجر" else "Save Shop Details", color = Navy900, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomShopDialog = false }) {
                    Text(if (isArabic) "إلغاء" else "Cancel")
                }
            }
        )
    }

    // APK Installation Guide Dialog
    if (showApkGuideDialog) {
        AlertDialog(
            onDismissRequest = { showApkGuideDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Android, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isArabic) "دليل تثبيت وتنزيل ملف APK" else "Android APK Download & Install Guide",
                        fontWeight = FontWeight.Bold,
                        color = Navy900
                    )
                }
            },
            text = {
                LazyColumn(modifier = Modifier.height(340.dp)) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "📂 File: apk/app-debug.apk",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Navy900
                                )
                                Text(
                                    text = "📊 Size: ~23.4 MB | Package: com.aistudio.mashopping.kxmp",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isArabic) "خطوات التثبيت على الهاتف:" else "Installation Steps on Android Phone:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Navy900
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isArabic)
                                "1. قم بتصدير المشروع كملف ZIP من قائمة أعلى يمين الاستوديو (Export Project).\n" +
                                "2. افتح الملف المضغوط وستجد مجلد 'apk' وبداخله 'app-debug.apk'.\n" +
                                "3. انقل ملف APK إلى هاتفك عبر USB أو Google Drive أو واتساب.\n" +
                                "4. اضغط على الملف في الهاتف للتثبيت.\n" +
                                "5. إذا ظهر تنبيه 'تثبيت تطبيقات غير معروفة'، فعّل الخيار من الإعدادات ثم اضغط 'تثبيت'.\n" +
                                "6. افتح تطبيق MA Shopping واستمتع بالتجربة!"
                            else
                                "1. Click 'Export Project' in AI Studio top menu to download the project ZIP.\n" +
                                "2. Extract the ZIP and open the 'apk/' folder containing 'app-debug.apk'.\n" +
                                "3. Transfer 'app-debug.apk' to your phone via USB cable, Google Drive, or messaging.\n" +
                                "4. Tap the APK file on your Android device.\n" +
                                "5. If prompted with 'Install unknown apps', enable 'Allow from this source'.\n" +
                                "6. Tap 'Install' and launch the MA Shopping app!",
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Navy800
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    putExtra(Intent.EXTRA_TEXT, "MA Shopping APK is available at 'apk/app-debug.apk'. Transfer and install on any Android 7.0+ device.")
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share APK Info"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "مشاركة تفاصيل ملف APK" else "Share APK Details")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showApkGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Navy900)
                ) {
                    Text(if (isArabic) "فهمت" else "Got it", color = Color.White)
                }
            }
        )
    }
}

@Composable
private fun AccountOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(icon, contentDescription = null, tint = Navy900)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, color = Navy900)
                Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
    }
}
