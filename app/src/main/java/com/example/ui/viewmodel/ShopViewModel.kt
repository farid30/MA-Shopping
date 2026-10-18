package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.Category
import com.example.data.model.Coupon
import com.example.data.model.Order
import com.example.data.model.Product
import com.example.data.model.Review
import com.example.data.model.StoreSettings
import com.example.data.model.UserAddress
import com.example.data.repository.CartItemWithProduct
import com.example.data.repository.ShopRepository
import com.example.data.seed.SeedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "CUSTOMER" or "SUPPORT"
    val text: String,
    val time: String = "Now"
)

data class ShopConceptPreset(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val taglineEn: String,
    val taglineAr: String,
    val addressEn: String,
    val addressAr: String,
    val phone: String,
    val email: String,
    val crNumber: String,
    val vatNumber: String,
    val currencyCode: String = "BHD",
    val currencySymbolAr: String = "د.ب",
    val instagram: String = "@mashopping.bh"
)

enum class ProductSortOption {
    DEFAULT,
    PRICE_LOW_TO_HIGH,
    PRICE_HIGH_TO_LOW,
    BEST_RATING,
    NEWEST
}

class ShopViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShopRepository(AppDatabase.getDatabase(application))

    // UI Preferences
    private val _isArabic = MutableStateFlow(false)
    val isArabic: StateFlow<Boolean> = _isArabic.asStateFlow()

    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode: StateFlow<Boolean> = _isAdminMode.asStateFlow()

    // Customer Profile
    private val _currentUserName = MutableStateFlow("Farid Al-Sayed")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("farid@mashopping.bh")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserPhone = MutableStateFlow("+973 3948 2011")
    val currentUserPhone: StateFlow<String> = _currentUserPhone.asStateFlow()

    // Products & Categories
    val allProducts: StateFlow<List<Product>> = repository.getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategories: StateFlow<List<Category>> = repository.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredProducts: StateFlow<List<Product>> = repository.getFeaturedProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestSellers: StateFlow<List<Product>> = repository.getBestSellers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashSales: StateFlow<List<Product>> = repository.getFlashSales()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _sortOption = MutableStateFlow(ProductSortOption.DEFAULT)
    val sortOption: StateFlow<ProductSortOption> = _sortOption.asStateFlow()

    private val _minRatingFilter = MutableStateFlow(0.0)
    val minRatingFilter: StateFlow<Double> = _minRatingFilter.asStateFlow()

    val filteredProducts = combine(
        allProducts,
        searchQuery,
        selectedCategoryId,
        sortOption,
        minRatingFilter
    ) { products, query, catId, sort, minRating ->
        var list = products

        if (catId != null) {
            list = list.filter { it.categoryId == catId }
        }

        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.nameEn.lowercase().contains(q) ||
                        it.nameAr.contains(q) ||
                        it.brand.lowercase().contains(q) ||
                        it.categoryNameEn.lowercase().contains(q) ||
                        it.categoryNameAr.contains(q)
            }
        }

        if (minRating > 0) {
            list = list.filter { it.rating >= minRating }
        }

        when (sort) {
            ProductSortOption.PRICE_LOW_TO_HIGH -> list.sortedBy { it.currentPrice }
            ProductSortOption.PRICE_HIGH_TO_LOW -> list.sortedByDescending { it.currentPrice }
            ProductSortOption.BEST_RATING -> list.sortedByDescending { it.rating }
            ProductSortOption.NEWEST -> list.sortedByDescending { it.id }
            ProductSortOption.DEFAULT -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wishlist
    val wishlistItems = repository.getWishlistItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Cart
    private val rawCartItems = repository.getCartItems()
    val cartWithProducts: StateFlow<List<CartItemWithProduct>> = combine(
        rawCartItems,
        allProducts
    ) { cartItems, products ->
        cartItems.mapNotNull { item ->
            val product = products.find { it.id == item.productId }
            product?.let { CartItemWithProduct(item, it) }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Applied Coupon
    private val _appliedCoupon = MutableStateFlow<Coupon?>(null)
    val appliedCoupon: StateFlow<Coupon?> = _appliedCoupon.asStateFlow()

    private val _couponError = MutableStateFlow<String?>(null)
    val couponError: StateFlow<String?> = _couponError.asStateFlow()

    // Store Settings
    val storeSettings: StateFlow<StoreSettings> = repository.getSettings()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            SeedData.initialSettings
        ).combine(MutableStateFlow(Unit)) { s, _ -> s ?: SeedData.initialSettings }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SeedData.initialSettings)

    // Orders
    val orders: StateFlow<List<Order>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Addresses
    val addresses: StateFlow<List<UserAddress>> = repository.getAllAddresses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Notifications
    val notifications: StateFlow<List<AppNotification>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.getUnreadNotificationCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val allCoupons: StateFlow<List<Coupon>> = repository.getAllCoupons()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Navigation and Modal State
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder.asStateFlow()

    private val _invoiceOrder = MutableStateFlow<Order?>(null)
    val invoiceOrder: StateFlow<Order?> = _invoiceOrder.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    // Support Chat State
    private val _chatMessages = MutableStateFlow(
        listOf(
            ChatMessage(
                sender = "SUPPORT",
                text = "Marhaba! Welcome to MA Shopping customer care. How can we assist you with your orders or delivery today in Bahrain?"
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
        }
    }

    // Language & Mode Actions
    fun toggleLanguage() {
        _isArabic.value = !_isArabic.value
    }

    fun toggleAdminMode() {
        _isAdminMode.value = !_isAdminMode.value
        val msg = if (_isAdminMode.value) "Switched to Admin Dashboard" else "Switched to Customer Store"
        showSnackbar(msg)
    }

    fun setAdminMode(isAdmin: Boolean) {
        _isAdminMode.value = isAdmin
    }

    fun updateProfile(name: String, email: String, phone: String) {
        _currentUserName.value = name
        _currentUserEmail.value = email
        _currentUserPhone.value = phone
        showSnackbar(if (_isArabic.value) "تم حفظ الملف الشخصي" else "Profile updated successfully")
    }

    val shopPresets = listOf(
        ShopConceptPreset(
            id = "preset_general",
            nameEn = "MA Shopping",
            nameAr = "إم إيه للتسوق",
            taglineEn = "Bahrain's Premier Online Store",
            taglineAr = "وجهتك الأولى للتسوق في البحرين",
            addressEn = "Building 450, Road 2814, Block 328, Manama, Bahrain",
            addressAr = "مبنى 450، طريق 2814، مجمع 328، المنامة، مملكة البحرين",
            phone = "+973 1700 8899",
            email = "support@mashopping.bh",
            crNumber = "CR-984321-BH",
            vatNumber = "TRN-200084930100003",
            currencyCode = "BHD",
            currencySymbolAr = "د.ب",
            instagram = "@mashopping.bh"
        ),
        ShopConceptPreset(
            id = "preset_tech",
            nameEn = "MA Tech Hub",
            nameAr = "إم إيه لتكنولوجيا المستقبل",
            taglineEn = "Smart Electronics & Premium Mobile Store",
            taglineAr = "أحدث الهواتف الذكية والإلكترونيات الفاخرة بالبحرين",
            addressEn = "Seef District, Al-Moayyed Tower, Fl 12, Manama",
            addressAr = "ضاحية السيف، برج المؤيد، طابق 12، المنامة",
            phone = "+973 1755 4920",
            email = "tech@mashopping.bh",
            crNumber = "CR-874211-BH",
            vatNumber = "TRN-200049210000003",
            currencyCode = "BHD",
            currencySymbolAr = "د.ب",
            instagram = "@matech.bh"
        ),
        ShopConceptPreset(
            id = "preset_luxury",
            nameEn = "MA Luxury Boutique",
            nameAr = "إم إيه بوتيك الفخامة",
            taglineEn = "High-End Watches, Perfumes & Designer Style",
            taglineAr = "ساعات فاخرة، عطور العود الملكية وأزياء راقية",
            addressEn = "Moda Mall, Bahrain World Trade Center, Manama",
            addressAr = "مودا مول، مركز البحرين التجاري العالمي، المنامة",
            phone = "+973 1722 9001",
            email = "vip@mashopping.bh",
            crNumber = "CR-652199-BH",
            vatNumber = "TRN-200065219900003",
            currencyCode = "BHD",
            currencySymbolAr = "د.ب",
            instagram = "@maluxury.bh"
        ),
        ShopConceptPreset(
            id = "preset_souq",
            nameEn = "MA Gulf Souq",
            nameAr = "سوق الخليج إم إيه",
            taglineEn = "Everything for Family, Home & Daily Living",
            taglineAr = "كل ما تحتاجه الأسرة والمنزل مع توصيل سريع",
            addressEn = "Oasis Mall, Juffair / Riffa Avenue, Bahrain",
            addressAr = "الواحة مول، الجفير / شارع الرفاع، البحرين",
            phone = "+973 1780 3344",
            email = "souq@mashopping.bh",
            crNumber = "CR-410982-BH",
            vatNumber = "TRN-200041098200003",
            currencyCode = "BHD",
            currencySymbolAr = "د.ب",
            instagram = "@gulfsouq.bh"
        ),
        ShopConceptPreset(
            id = "preset_express",
            nameEn = "MA Express Market",
            nameAr = "إم إيه إكسبرس ماركت",
            taglineEn = "Ultra Fast 1-Hour Delivery Across Bahrain",
            taglineAr = "توصيل فائق السرعة لجميع مناطق المملكة",
            addressEn = "Central Logistics Hub, Hidd Industrial Zone, Bahrain",
            addressAr = "مركز التوزيع الرئيسي، منطقة الحد الصناعية، البحرين",
            phone = "+973 3999 1234",
            email = "express@mashopping.bh",
            crNumber = "CR-990142-BH",
            vatNumber = "TRN-200099014200003",
            currencyCode = "BHD",
            currencySymbolAr = "د.ب",
            instagram = "@maexpress.bh"
        )
    )

    data class ShopNiche(
        val key: String,
        val labelEn: String,
        val labelAr: String,
        val namesEn: List<String>,
        val namesAr: List<String>,
        val taglinesEn: List<String>,
        val taglinesAr: List<String>
    )

    val shopNiches = listOf(
        ShopNiche(
            key = "tech",
            labelEn = "Tech & Gadgets",
            labelAr = "التكنولوجيا والإلكترونيات",
            namesEn = listOf("Pearl Tech Hub", "Bahrain Silicon Valley", "NextGen Mobile & Electronics", "FutureTech Bahrain", "Dilmun Digital Oasis"),
            namesAr = listOf("واحة التكنولوجيا الذكية", "سيليكون البحرين للإلكترونيات", "نكست جن للهواتف والأجهزة", "فيوتشر تك البحرين", "دلمون للأجهزة الرقمية"),
            taglinesEn = listOf("Smart electronics, gadgets & genuine mobile accessories in Bahrain", "Your premium destination for the latest smartphones and gaming laptops"),
            taglinesAr = listOf("أحدث الأجهزة الذكية وملحقات الهواتف الأصلية في البحرين", "وجهتك الموثوقة لأحدث الهواتف الذكية وحواسيب الألعاب الاحترافية")
        ),
        ShopNiche(
            key = "luxury",
            labelEn = "Luxury & Perfumes",
            labelAr = "العطور والفخامة",
            namesEn = listOf("Royal Dilmun Perfumes", "Danat Al Bahrain Jewels", "Manama Prestige Boutique", "Oudh & Amber Palace", "Elite Gulf Luxury"),
            namesAr = listOf("عطور دلمون الملكية", "مجوهرات دانات البحرين", "بوتيك المنامة برستيج", "قصر العود والعنبر", "نخبة الخليج للفخامة"),
            taglinesEn = listOf("Authentic Royal Cambodian Oudh, French perfumes & Swiss timepieces", "Elegance defined by Bahraini heritage and bespoke craftsmanship"),
            taglinesAr = listOf("أرقى دهن العود الكمبودي الملكي، العطور الباريسية والساعات السويسرية", "فخامة وأناقة مستوحاة من عراقة التراث البحريني")
        ),
        ShopNiche(
            key = "fashion",
            labelEn = "Fashion & Abaya",
            labelAr = "الأزياء والعبايات",
            namesEn = listOf("Dana Couture Bahrain", "Al Reem Haute Elegance", "Luluah Modest & Modern", "Riffa Silk Atelier", "Zallaq Fashion House"),
            namesAr = listOf("دانة كوتور البحرين", "دار الريم للأزياء الراقية", "لؤلؤة للأناقة المعاصرة", "مشغل الرفاع للحرير", "دار الزلاق للأزياء"),
            taglinesEn = listOf("Designer abayas, evening wear & contemporary Bahraini fashion", "Chic silhouettes tailored for modern Gulf lifestyle"),
            taglinesAr = listOf("تصاميم عبايات راقية، فساتين مناسبات وأزياء خليجية عصرية", "أناقة محتشمة وفاخرة تلبي ذوق المرأة الخليجية")
        ),
        ShopNiche(
            key = "souq",
            labelEn = "Traditional Souq",
            labelAr = "السوق التقليدي والمنزل",
            namesEn = listOf("Bab Al Bahrain Grand Souq", "Muharraq Heritage Emporium", "Gulf Traditional Provisions", "Al Nakheel Home & Spices", "Manama Central Bazaar"),
            namesAr = listOf("سوق باب البحرين الكبير", "متجر المحرق للتراث والتقاليد", "تموينات الخليج الأصيلة", "النخيل للمستلزمات والبهارات", "بازار المنامة المركزي"),
            taglinesEn = listOf("Authentic Bahraini spices, halwa, home essentials and giftware", "Everything your family needs with same-day express doorstep delivery"),
            taglinesAr = listOf("بهارات بحرينية أصلية، حلوى شويطر، مستلزمات المنزل والهدايا", "كل ما يحتاجه بيتك وعائلتك مع توصيل سريع ومباشر لباب المنزل")
        ),
        ShopNiche(
            key = "coffee",
            labelEn = "Coffee & Roastery",
            labelAr = "القهوة والمحمصة المختصة",
            namesEn = listOf("Dilmun Specialty Roasters", "Qahwa Artisans Bahrain", "The Arabian Bean Co.", "Capital Single Origin Coffee", "Falcon Brew Roastery"),
            namesAr = listOf("محمصة دلمون المختصة", "حرفيو القهوة البحرينية", "شركة البن العربي الفاخر", "كابيتال للقهوة المختصة", "محمصة صقر الخليج"),
            taglinesEn = listOf("Artisanal roasted beans, specialty espresso beans and brewing equipment", "From world origins straight to your cup in Bahrain"),
            taglinesAr = listOf("حبوب بن محمصة بعناية، محاصيل قهوة مختصة وأدوات باريستا احترافية", "من أرقى مزارع العالم مباشرة إلى فنجانك في البحرين")
        ),
        ShopNiche(
            key = "sports",
            labelEn = "Sports & Fitness",
            labelAr = "الرياضة واللياقة البدنية",
            namesEn = listOf("Iron Gulf Performance", "Bahrain Active Gear", "Apex Fitness & Gym Supply", "Oasis Sports Nutrition", "Al Dana Athletic Hub"),
            namesAr = listOf("أيرون جلف للأداء الرياضي", "بحرين أكتيف للمعدات", "أبيكس لتجهيزات اللياقة البدنية", "واحة المكملات الرياضية", "نادي الدانة للرياضة"),
            taglinesEn = listOf("Gym equipment, sports apparel and certified fitness nutrition in Bahrain", "Fuel your athletic ambitions with top global brands"),
            taglinesAr = listOf("أجهزة رياضية، ملابس تدريب وأجود المكملات الغذائية المعتمدة في البحرين", "حقق أهدافك الرياضية مع أفضل العلامات العالمية")
        )
    )

    private val bahrainAreas = listOf(
        Pair("Seef District, Road 2804, Building 1420, Manama", "ضاحية السيف، طريق 2804، مبنى 1420، المنامة"),
        Pair("Amwaj Islands, Lagoon Avenue, Building 88, Muharraq", "جزر أمواج، ممشى اللاغون، مبنى 88، المحرق"),
        Pair("Riffa Views, Park Avenue, Road 3912, Block 939, Riffa", "رفاع فيوز، شارع الحدائق، طريق 3912، مجمع 939، الرفاع"),
        Pair("Saar Central, Avenue 25, Block 525, Northern Governorate", "سار سنترال، شارع 25، مجمع 525، المحافظة الشمالية"),
        Pair("Diplomatic Area, Building 210, Road 1704, Block 317, Manama", "المنطقة الدبلوماسية، مبنى 210، طريق 1704، مجمع 317، المنامة"),
        Pair("Juffair Boulevard, Road 4015, Building 772, Capital", "جفير بوليفارد، طريق 4015، مبنى 772، محافظة العاصمة"),
        Pair("Busaiteen Promenade, Road 2210, Block 228, Muharraq", "ممشى البسيتين، طريق 2210، مجمع 228، المحرق"),
        Pair("Zallaq Beach Highway, Block 1056, Southern Governorate", "شارع شاطئ الزلاق، مجمع 1056، المحافظة الجنوبية")
    )

    fun generateCustomShopDetails(nicheKey: String? = null): ShopConceptPreset {
        val niche = if (nicheKey != null) {
            shopNiches.find { it.key == nicheKey } ?: shopNiches.random()
        } else {
            shopNiches.random()
        }

        val idx = (0 until niche.namesEn.size).random()
        val nameEn = niche.namesEn[idx]
        val nameAr = niche.namesAr[idx]
        val taglineEn = niche.taglinesEn.random()
        val taglineAr = niche.taglinesAr.random()
        val addressPair = bahrainAreas.random()

        val randomCr = "CR-${(100000..999999).random()}-BH"
        val randomVat = "TRN-2000${(10000000..99999999).random()}003"
        val randomPhone = if (kotlin.random.Random.nextBoolean()) {
            "+973 17${(100000..999999).random()}"
        } else {
            "+973 3${(2000000..9999999).random()}"
        }
        val cleanDomain = nameEn.lowercase().replace("[^a-z0-9]".toRegex(), "")
        val email = "contact@$cleanDomain.bh"
        val instagram = "@$cleanDomain.bh"

        return ShopConceptPreset(
            id = "custom_${System.currentTimeMillis()}",
            nameEn = nameEn,
            nameAr = nameAr,
            taglineEn = taglineEn,
            taglineAr = taglineAr,
            addressEn = addressPair.first,
            addressAr = addressPair.second,
            phone = randomPhone,
            email = email,
            crNumber = randomCr,
            vatNumber = randomVat,
            currencyCode = "BHD",
            currencySymbolAr = "د.ب",
            instagram = instagram
        )
    }

    fun generateAndApplyCustomShop(nicheKey: String? = null): ShopConceptPreset {
        val preset = generateCustomShopDetails(nicheKey)
        applyShopConcept(preset)
        return preset
    }

    fun applyShopConcept(preset: ShopConceptPreset) {
        val current = storeSettings.value
        val updated = current.copy(
            shopNameEn = preset.nameEn,
            shopNameAr = preset.nameAr,
            shopTaglineEn = preset.taglineEn,
            shopTaglineAr = preset.taglineAr,
            shopAddressEn = preset.addressEn,
            shopAddressAr = preset.addressAr,
            storeContactPhone = preset.phone,
            storeContactEmail = preset.email,
            commercialRegistrationNumber = preset.crNumber,
            vatRegistrationNumber = preset.vatNumber,
            currencyCode = preset.currencyCode,
            currencySymbolAr = preset.currencySymbolAr,
            instagramHandle = preset.instagram
        )
        adminSaveSettings(updated)
        showSnackbar(if (_isArabic.value) "تم تطبيق هوية المتجر: ${preset.nameAr}" else "Applied shop branding: ${preset.nameEn}")
    }

    fun generateRandomShopConcept() {
        generateAndApplyCustomShop(null)
    }

    fun formatPrice(amount: Double): String {
        val formatted = String.format(Locale.US, "%.3f", amount)
        val settings = storeSettings.value
        val symbol = if (_isArabic.value) settings.currencySymbolAr else settings.currencyCode
        return "$formatted $symbol"
    }

    // Search and Category
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun setSortOption(option: ProductSortOption) {
        _sortOption.value = option
    }

    fun setMinRatingFilter(rating: Double) {
        _minRatingFilter.value = rating
    }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun selectOrderForTracking(order: Order?) {
        _selectedOrder.value = order
    }

    fun selectOrderForInvoice(order: Order?) {
        _invoiceOrder.value = order
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }

    fun showSnackbar(message: String) {
        _snackbarMessage.value = message
    }

    // Cart Operations
    fun addToCart(productId: Long, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(productId, quantity)
            showSnackbar(if (_isArabic.value) "تمت الإضافة إلى سلة التسوق" else "Added to shopping cart")
        }
    }

    fun updateCartQuantity(cartItemId: Long, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(cartItemId, quantity)
        }
    }

    fun removeFromCart(cartItemId: Long) {
        viewModelScope.launch {
            repository.removeFromCart(cartItemId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Coupon Calculation
    fun applyCoupon(code: String) {
        viewModelScope.launch {
            _couponError.value = null
            val coupon = repository.getCouponByCode(code)
            if (coupon == null || !coupon.isActive) {
                _couponError.value = if (_isArabic.value) "كوبون غير صالح أو منتهي الصلاحية" else "Invalid or expired coupon code"
                return@launch
            }

            val currentSubtotal = cartWithProducts.value.sumOf { it.product.currentPrice * it.cartItem.quantity }
            if (currentSubtotal < coupon.minSpendBhd) {
                _couponError.value = if (_isArabic.value)
                    "الحد الأدنى للاستفادة من الكوبون هو ${formatPrice(coupon.minSpendBhd)}"
                else
                    "Minimum spend for this coupon is ${formatPrice(coupon.minSpendBhd)}"
                return@launch
            }

            _appliedCoupon.value = coupon
            showSnackbar(if (_isArabic.value) "تم تطبيق الخصم بنجاح!" else "Coupon ${coupon.code} applied!")
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
        _couponError.value = null
    }

    // Wishlist
    fun toggleWishlist(productId: Long) {
        viewModelScope.launch {
            repository.toggleWishlist(productId)
        }
    }

    fun isProductInWishlist(productId: Long): Boolean {
        return wishlistItems.value.any { it.productId == productId }
    }

    // Checkout & Order Placement
    fun placeOrder(
        address: UserAddress,
        paymentMethod: String,
        onSuccess: (Order) -> Unit
    ) {
        viewModelScope.launch {
            val items = cartWithProducts.value
            if (items.isEmpty()) return@launch

            val subtotal = items.sumOf { it.product.currentPrice * it.cartItem.quantity }
            val settings = storeSettings.value
            val deliveryFee = if (subtotal >= settings.freeShippingThresholdBhd) 0.0 else settings.defaultDeliveryFeeBhd

            var discount = 0.0
            val coupon = _appliedCoupon.value
            if (coupon != null && subtotal >= coupon.minSpendBhd) {
                discount = if (coupon.isPercent) {
                    subtotal * (coupon.discountPercent / 100.0)
                } else {
                    coupon.fixedDiscountBhd
                }
            }

            val vat = (subtotal - discount) * (settings.vatPercentage / 100.0)
            val total = maxOf(0.0, (subtotal - discount) + deliveryFee + vat)

            val order = repository.placeOrder(
                customerName = _currentUserName.value,
                customerPhone = _currentUserPhone.value,
                customerEmail = _currentUserEmail.value,
                address = address,
                cartWithProducts = items,
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                vat = vat,
                discount = discount,
                total = total,
                couponCode = coupon?.code ?: "",
                paymentMethod = paymentMethod
            )

            _appliedCoupon.value = null
            onSuccess(order)
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId)
            showSnackbar(if (_isArabic.value) "تم إلغاء الطلب بنجاح" else "Order $orderId cancelled")
        }
    }

    // Reviews
    fun getReviews(productId: Long) = repository.getReviewsForProduct(productId)

    fun submitReview(productId: Long, rating: Int, comment: String) {
        viewModelScope.launch {
            repository.addReview(
                productId = productId,
                authorName = _currentUserName.value.ifBlank { "Verified Buyer" },
                rating = rating,
                comment = comment
            )
            showSnackbar(if (_isArabic.value) "شكراً لك! تم إضافة تقييمك" else "Thank you! Your review has been added")
        }
    }

    // Addresses
    fun saveAddress(address: UserAddress) {
        viewModelScope.launch {
            repository.saveAddress(address)
            showSnackbar(if (_isArabic.value) "تم حفظ العنوان بنجاح" else "Address saved successfully")
        }
    }

    fun deleteAddress(address: UserAddress) {
        viewModelScope.launch {
            repository.deleteAddress(address)
        }
    }

    fun setDefaultAddress(addressId: Long) {
        viewModelScope.launch {
            repository.setDefaultAddress(addressId)
        }
    }

    // Notifications
    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    // Support Chat
    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(sender = "CUSTOMER", text = text)
        _chatMessages.value = _chatMessages.value + userMsg

        val currentStoreName = storeSettings.value.shopNameEn
        val currentPhone = storeSettings.value.storeContactPhone

        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            val replyText = when {
                text.contains("order", ignoreCase = true) || text.contains("طلب", ignoreCase = true) ->
                    "You can track your order live under Account -> Order History. Delivery across Bahrain is typically within 24 hours!"
                text.contains("benefit", ignoreCase = true) || text.contains("payment", ignoreCase = true) || text.contains("دفع", ignoreCase = true) ->
                    "We support BenefitPay, Cash on Delivery, and all Credit/Debit cards with 0% extra fees."
                text.contains("return", ignoreCase = true) || text.contains("cancel", ignoreCase = true) || text.contains("إرجاع", ignoreCase = true) ->
                    "Items can be returned or exchanged within 14 days in original sealed condition per Bahrain consumer protection regulations."
                else ->
                    "Thank you for contacting $currentStoreName! A customer care specialist is reviewing your message. For instant WhatsApp assistance, reach us at $currentPhone."
            }
            _chatMessages.value = _chatMessages.value + ChatMessage(sender = "SUPPORT", text = replyText)
        }
    }

    // Admin Operations
    fun adminUpdateOrderStatus(orderId: String, status: String) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status)
            showSnackbar("Order $orderId status updated to $status")
        }
    }

    fun adminSaveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
            showSnackbar(if (product.id == 0L) "Product created successfully" else "Product updated successfully")
        }
    }

    fun adminDeleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
            showSnackbar("Product '${product.nameEn}' deleted")
        }
    }

    fun adminUpdateStock(productId: Long, stock: Int) {
        viewModelScope.launch {
            repository.updateStock(productId, stock)
            showSnackbar("Stock updated to $stock")
        }
    }

    fun adminSaveCategory(category: Category) {
        viewModelScope.launch {
            repository.saveCategory(category)
            showSnackbar("Category saved")
        }
    }

    fun adminDeleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            showSnackbar("Category deleted")
        }
    }

    fun adminSaveCoupon(coupon: Coupon) {
        viewModelScope.launch {
            repository.saveCoupon(coupon)
            showSnackbar("Coupon '${coupon.code}' saved")
        }
    }

    fun adminDeleteCoupon(coupon: Coupon) {
        viewModelScope.launch {
            repository.deleteCoupon(coupon)
            showSnackbar("Coupon deleted")
        }
    }

    fun adminSaveSettings(settings: StoreSettings) {
        viewModelScope.launch {
            repository.saveSettings(settings)
            showSnackbar("Store settings saved successfully")
        }
    }
}
