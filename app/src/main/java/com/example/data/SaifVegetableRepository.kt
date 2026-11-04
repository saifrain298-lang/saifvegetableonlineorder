package com.example.data

import android.content.Context
import com.example.R
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.*

class SaifVegetableRepository private constructor() {

    private val _isHindi = MutableStateFlow(false)
    val isHindi: StateFlow<Boolean> = _isHindi.asStateFlow()

    private val _businessSettings = MutableStateFlow(BusinessSettings())
    val businessSettings: StateFlow<BusinessSettings> = _businessSettings.asStateFlow()

    private val _customerUser = MutableStateFlow(
        CustomerUser(
            id = "cust_001",
            name = "",
            phone = "",
            address = "",
            pincode = "400077",
            isLoggedIn = false
        )
    )
    val customerUser: StateFlow<CustomerUser> = _customerUser.asStateFlow()

    private val _vegetables = MutableStateFlow<List<VegetableItem>>(emptyList())
    val vegetables: StateFlow<List<VegetableItem>> = _vegetables.asStateFlow()

    private val _cart = MutableStateFlow<Map<String, Double>>(emptyMap())
    val cart: StateFlow<Map<String, Double>> = _cart.asStateFlow()

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _stockMovements = MutableStateFlow<List<StockMovement>>(emptyList())
    val stockMovements: StateFlow<List<StockMovement>> = _stockMovements.asStateFlow()

    private val _expenses = MutableStateFlow<List<ExpenseRecord>>(emptyList())
    val expenses: StateFlow<List<ExpenseRecord>> = _expenses.asStateFlow()

    init {
        seedVegetableCatalogue()
        seedOpeningStockLedger()
    }

    private fun seedVegetableCatalogue() {
        // Exactly 39 Indian vegetables with authentic pricing (INR) and verified crop images
        val initialList = listOf(
            // Row 0
            VegetableItem(
                id = "veg_01",
                nameEn = "Broccoli",
                nameHi = "ब्रोकोली",
                category = VegetableCategory.EXOTIC,
                drawableResId = R.drawable.veg_broccoli,
                pricePerKg = 120.0,
                costPerKg = 85.0,
                discountPercent = 5,
                stockKg = 200.0,
                descriptionEn = "Crisp, nutrient-dense fresh broccoli florets.",
                descriptionHi = "ताजी, हरी और पोषक तत्वों से भरपूर ब्रोकोली।"
            ),
            VegetableItem(
                id = "veg_02",
                nameEn = "Yellow Capsicum",
                nameHi = "पीली शिमला मिर्च",
                category = VegetableCategory.EXOTIC,
                drawableResId = R.drawable.veg_yellow_capsicum,
                pricePerKg = 140.0,
                costPerKg = 100.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Sweet, glossy yellow bell peppers perfect for stir-fry.",
                descriptionHi = "मीठी और चमकदार पीली शिमला मिर्च, सलाद व पास्ता के लिए उत्तम।"
            ),
            VegetableItem(
                id = "veg_03",
                nameEn = "Green Capsicum",
                nameHi = "हरी शिमला मिर्च",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_green_capsicum,
                pricePerKg = 60.0,
                costPerKg = 42.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Crispy green bell peppers rich in antioxidants.",
                descriptionHi = "कुरकुरी हरी शिमला मिर्च, ग्रेवी और फ्राई के लिए।"
            ),
            VegetableItem(
                id = "veg_04",
                nameEn = "Red Capsicum",
                nameHi = "लाल शिमला मिर्च",
                category = VegetableCategory.EXOTIC,
                drawableResId = R.drawable.veg_red_capsicum,
                pricePerKg = 150.0,
                costPerKg = 110.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Vibrant, ripe red bell peppers with mild sweetness.",
                descriptionHi = "चमकीली लाल शिमला मिर्च, विटामिन सी से भरपूर।"
            ),
            VegetableItem(
                id = "veg_05",
                nameEn = "Lettuce",
                nameHi = "सलाद पत्ता",
                category = VegetableCategory.LEAFY,
                drawableResId = R.drawable.veg_lettuce,
                pricePerKg = 80.0,
                costPerKg = 55.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Hydroponic & fresh crisp lettuce leaves for gourmet salads.",
                descriptionHi = "ताजे, कुरकुरे सलाद पत्ते, सलाद और बर्गर के लिए।"
            ),
            VegetableItem(
                id = "veg_06",
                nameEn = "Curry Leaves",
                nameHi = "करी पत्ता",
                category = VegetableCategory.LEAFY,
                drawableResId = R.drawable.veg_curry_leaves,
                pricePerKg = 70.0,
                costPerKg = 45.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Aromatic fresh curry leaves, essential for Indian tadka.",
                descriptionHi = "खुशबूदार ताजे करी पत्ते, हर तड़के की जान।"
            ),

            // Row 1
            VegetableItem(
                id = "veg_07",
                nameEn = "Baby Corn",
                nameHi = "बेबी कॉर्न",
                category = VegetableCategory.EXOTIC,
                drawableResId = R.drawable.veg_baby_corn,
                pricePerKg = 110.0,
                costPerKg = 78.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Tender, crunchy baby corn spears hand-picked fresh.",
                descriptionHi = "नरम और कुरकुरे बेबी कॉर्न।"
            ),
            VegetableItem(
                id = "veg_08",
                nameEn = "Potato",
                nameHi = "आलू",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_potato,
                pricePerKg = 35.0,
                costPerKg = 24.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "High-starch, cleaned Agra potatoes with no green patches.",
                descriptionHi = "साफ-सुथरे नए आलू, बिना किसी हरे दाग के।"
            ),
            VegetableItem(
                id = "veg_09",
                nameEn = "Onion",
                nameHi = "प्याज",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_onion,
                pricePerKg = 40.0,
                costPerKg = 28.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Nashik red onions, sun-dried and perfectly pungent.",
                descriptionHi = "नासिक के लाल प्याज, बेहतरीन स्वाद और लंबे समय तक सुरक्षित।"
            ),
            VegetableItem(
                id = "veg_10",
                nameEn = "Tomato",
                nameHi = "टमाटर",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_tomato,
                pricePerKg = 45.0,
                costPerKg = 30.0,
                discountPercent = 5,
                stockKg = 200.0,
                descriptionEn = "Firm, vine-ripened red country tomatoes.",
                descriptionHi = "पके हुए, लाल देसी टमाटर, रसीले और स्वादिष्ट।"
            ),
            VegetableItem(
                id = "veg_11",
                nameEn = "Brinjal",
                nameHi = "बैंगन",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_brinjal,
                pricePerKg = 48.0,
                costPerKg = 32.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Glossy purple tender eggplants for bharta or curries.",
                descriptionHi = "चमकदार बैंगनी बैंगन, भरते और सब्जी के लिए।"
            ),
            VegetableItem(
                id = "veg_12",
                nameEn = "Lady Finger",
                nameHi = "भिंडी",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_lady_finger,
                pricePerKg = 55.0,
                costPerKg = 38.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Crisp, tender green bhindi, snapped to verify tenderness.",
                descriptionHi = "नरम और हरी भिंडी, रोज की सब्जी के लिए।"
            ),

            // Row 2
            VegetableItem(
                id = "veg_13",
                nameEn = "Cabbage",
                nameHi = "पत्ता गोभी",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_cabbage,
                pricePerKg = 30.0,
                costPerKg = 18.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Firm, compact green cabbage head.",
                descriptionHi = "ठोस और ताजी पत्ता गोभी।"
            ),
            VegetableItem(
                id = "veg_14",
                nameEn = "Cauliflower",
                nameHi = "फूल गोभी",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_cauliflower,
                pricePerKg = 45.0,
                costPerKg = 30.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Creamy white, pest-free compact cauliflower heads.",
                descriptionHi = "साफ, सफेद और घनी फूल गोभी।"
            ),
            VegetableItem(
                id = "veg_15",
                nameEn = "Spinach",
                nameHi = "पालक",
                category = VegetableCategory.LEAFY,
                drawableResId = R.drawable.veg_spinach,
                pricePerKg = 40.0,
                costPerKg = 25.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Tender green palak leaves, washed and trimmed.",
                descriptionHi = "जड़ों से कटी हुई साफ और ताजी पालक।"
            ),
            VegetableItem(
                id = "veg_16",
                nameEn = "Fenugreek Leaves (Methi)",
                nameHi = "मेथी",
                category = VegetableCategory.LEAFY,
                drawableResId = R.drawable.veg_fenugreek_methi,
                pricePerKg = 45.0,
                costPerKg = 28.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Fresh aromatic methi leaves for theplas and subzi.",
                descriptionHi = "सुगंधित ताजी हरी मेथी।"
            ),
            VegetableItem(
                id = "veg_17",
                nameEn = "Coriander Leaves",
                nameHi = "हरा धनिया",
                category = VegetableCategory.LEAFY,
                drawableResId = R.drawable.veg_coriander,
                pricePerKg = 60.0,
                costPerKg = 40.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Fragrant, freshly plucked coriander bunches.",
                descriptionHi = "ताजा और खुशबूदार हरा धनिया।"
            ),
            VegetableItem(
                id = "veg_18",
                nameEn = "Beetroot",
                nameHi = "चुकंदर",
                category = VegetableCategory.ROOTS,
                drawableResId = R.drawable.veg_beetroot,
                pricePerKg = 50.0,
                costPerKg = 34.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Deep red, sweet earthy beetroots packed with iron.",
                descriptionHi = "गहरे लाल और मीठे चुकंदर, सलाद व जूस के लिए।"
            ),

            // Row 3
            VegetableItem(
                id = "veg_19",
                nameEn = "Pumpkin",
                nameHi = "कद्दू",
                category = VegetableCategory.GOURDS,
                drawableResId = R.drawable.veg_pumpkin,
                pricePerKg = 35.0,
                costPerKg = 22.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Golden sweet pumpkin with rich orange flesh.",
                descriptionHi = "पीला मीठा कद्दू (भोपला), सब्जी और हलवे के लिए।"
            ),
            VegetableItem(
                id = "veg_20",
                nameEn = "Bottle Gourd (Lauki)",
                nameHi = "लौकी",
                category = VegetableCategory.GOURDS,
                drawableResId = R.drawable.veg_bottle_gourd,
                pricePerKg = 38.0,
                costPerKg = 24.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Tender, seedless bottle gourd full of natural hydration.",
                descriptionHi = "नरम, पतली हरी लौकी, कोफ्ता और सब्जी के लिए।"
            ),
            VegetableItem(
                id = "veg_21",
                nameEn = "Bitter Gourd (Karela)",
                nameHi = "करेला",
                category = VegetableCategory.GOURDS,
                drawableResId = R.drawable.veg_bitter_gourd,
                pricePerKg = 52.0,
                costPerKg = 35.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Small prickly karela, excellent for diabetic wellness.",
                descriptionHi = "ताजा और छोटा करेला, सेहत और स्वाद से भरपूर।"
            ),
            VegetableItem(
                id = "veg_22",
                nameEn = "Ridge Gourd (Turai)",
                nameHi = "तुरई",
                category = VegetableCategory.GOURDS,
                drawableResId = R.drawable.veg_ridge_gourd,
                pricePerKg = 55.0,
                costPerKg = 38.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Fresh slender ridge gourd, light on digestion.",
                descriptionHi = "नरम और मीठी तुरई।"
            ),
            VegetableItem(
                id = "veg_23",
                nameEn = "Green Chilli",
                nameHi = "हरी मिर्च",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_green_chilli,
                pricePerKg = 70.0,
                costPerKg = 48.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Spicy, fresh green chillies with intense aroma.",
                descriptionHi = "तीखी और ताजी हरी मिर्च।"
            ),
            VegetableItem(
                id = "veg_24",
                nameEn = "Tinda (Round Gourd)",
                nameHi = "टिंडा",
                category = VegetableCategory.GOURDS,
                drawableResId = R.drawable.veg_tinda,
                pricePerKg = 58.0,
                costPerKg = 40.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Tender Indian round gourds with soft edible seeds.",
                descriptionHi = "ताजे और कोमल टिंडे।"
            ),

            // Row 4
            VegetableItem(
                id = "veg_25",
                nameEn = "Cucumber",
                nameHi = "खीरा",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_cucumber,
                pricePerKg = 35.0,
                costPerKg = 22.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Cool, crisp salad cucumbers, never bitter.",
                descriptionHi = "ठंडा और कुरकुरा खीरा, बिना कड़वाहट।"
            ),
            VegetableItem(
                id = "veg_26",
                nameEn = "Carrot",
                nameHi = "गाजर",
                category = VegetableCategory.ROOTS,
                drawableResId = R.drawable.veg_carrot,
                pricePerKg = 50.0,
                costPerKg = 32.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Crunchy sweet red carrots for salads or halwa.",
                descriptionHi = "मीठी और रसीली लाल गाजर।"
            ),
            VegetableItem(
                id = "veg_27",
                nameEn = "Radish",
                nameHi = "मूली",
                category = VegetableCategory.ROOTS,
                drawableResId = R.drawable.veg_radish,
                pricePerKg = 35.0,
                costPerKg = 20.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Crisp white mooli with edible tender leaves.",
                descriptionHi = "ताजी और सफेद मूली पत्तों सहित।"
            ),
            VegetableItem(
                id = "veg_28",
                nameEn = "Pointed Gourd (Parwal)",
                nameHi = "परवल",
                category = VegetableCategory.GOURDS,
                drawableResId = R.drawable.veg_pointed_gourd,
                pricePerKg = 65.0,
                costPerKg = 45.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Green striped parwal, crisp and delicate.",
                descriptionHi = "हरी धारियों वाला ताजा परवल।"
            ),
            VegetableItem(
                id = "veg_29",
                nameEn = "Sweet Potato",
                nameHi = "शकरकंद",
                category = VegetableCategory.ROOTS,
                drawableResId = R.drawable.veg_sweet_potato,
                pricePerKg = 55.0,
                costPerKg = 38.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Naturally sweet, earthy roots rich in complex fiber.",
                descriptionHi = "प्राकृतिक रूप से मीठा शकरकंद।"
            ),
            VegetableItem(
                id = "veg_30",
                nameEn = "Elephant Foot Yam (Suran)",
                nameHi = "सूरन / जिमीकंद",
                category = VegetableCategory.ROOTS,
                drawableResId = R.drawable.veg_elephant_foot_yam,
                pricePerKg = 60.0,
                costPerKg = 40.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "High-grade mature suran yam, firm and rich in minerals.",
                descriptionHi = "बढ़िया किस्म का सूरन (जिमीकंद)।"
            ),

            // Row 5
            VegetableItem(
                id = "veg_31",
                nameEn = "Taro Root (Arbi)",
                nameHi = "अरबी",
                category = VegetableCategory.ROOTS,
                drawableResId = R.drawable.veg_taro_root,
                pricePerKg = 50.0,
                costPerKg = 34.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Small, firm arbi roots perfect for roasting.",
                descriptionHi = "सूखी और मसालेदार सब्जी के लिए अरबी।"
            ),
            VegetableItem(
                id = "veg_32",
                nameEn = "Ginger",
                nameHi = "अदरक",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_ginger,
                pricePerKg = 120.0,
                costPerKg = 85.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Juicy, fibrous fresh ginger root with strong punch.",
                descriptionHi = "ताजी और रसदार अदरक, चाय और भोजन के लिए।"
            ),
            VegetableItem(
                id = "veg_33",
                nameEn = "Garlic",
                nameHi = "लहसुन",
                category = VegetableCategory.ESSENTIALS,
                drawableResId = R.drawable.veg_garlic,
                pricePerKg = 140.0,
                costPerKg = 100.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Plump, papery white garlic cloves with bold aroma.",
                descriptionHi = "सफेद और मोटे दानों वाला लहसुन।"
            ),
            VegetableItem(
                id = "veg_34",
                nameEn = "Green Peas",
                nameHi = "हरी मटर",
                category = VegetableCategory.BEANS_SPROUTS,
                drawableResId = R.drawable.veg_green_peas,
                pricePerKg = 80.0,
                costPerKg = 56.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Sweet, plump green pea pods fresh from farm.",
                descriptionHi = "मीठी और भरी हुई हरी मटर की फलियां।"
            ),
            VegetableItem(
                id = "veg_35",
                nameEn = "French Beans",
                nameHi = "फ्रेंच बीन्स",
                category = VegetableCategory.BEANS_SPROUTS,
                drawableResId = R.drawable.veg_french_beans,
                pricePerKg = 75.0,
                costPerKg = 52.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Crisp, stringless slender green beans.",
                descriptionHi = "नरम और ताजी फ्रेंच बीन्स।"
            ),
            VegetableItem(
                id = "veg_36",
                nameEn = "Cluster Beans (Gawar)",
                nameHi = "ग्वार फली",
                category = VegetableCategory.BEANS_SPROUTS,
                drawableResId = R.drawable.veg_cluster_beans,
                pricePerKg = 60.0,
                costPerKg = 42.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Tender young gawar pods, packed with natural fibre.",
                descriptionHi = "नरम और ताजी ग्वार फली।"
            ),
            VegetableItem(
                id = "veg_37",
                nameEn = "Drumstick Pods",
                nameHi = "सहजन की फली",
                category = VegetableCategory.BEANS_SPROUTS,
                drawableResId = R.drawable.veg_drumstick,
                pricePerKg = 70.0,
                costPerKg = 48.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Fleshy moringa drumstick pods for sambar and curries.",
                descriptionHi = "सांभर और सब्जी के लिए गूदेदार सहजन फली।"
            ),
            VegetableItem(
                id = "veg_38",
                nameEn = "Flat Beans",
                nameHi = "पापड़ी / सेम",
                category = VegetableCategory.BEANS_SPROUTS,
                drawableResId = R.drawable.veg_flat_beans,
                pricePerKg = 65.0,
                costPerKg = 45.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Broad, tender flat beans for traditional undhiyu.",
                descriptionHi = "ताजी सेम / पापड़ी की फलियां।"
            ),
            VegetableItem(
                id = "veg_39",
                nameEn = "Green Gram Sprouts",
                nameHi = "अंकुरित मूंग",
                category = VegetableCategory.BEANS_SPROUTS,
                drawableResId = R.drawable.veg_sprout_moong,
                pricePerKg = 90.0,
                costPerKg = 60.0,
                discountPercent = 0,
                stockKg = 200.0,
                descriptionEn = "Freshly sprouted whole green moong, high in plant protein.",
                descriptionHi = "प्रोटीन से भरपूर ताजा अंकुरित हरा मूंग।"
            )
        )
        _vegetables.value = initialList
    }

    private fun seedOpeningStockLedger() {
        // Record 200 kg per vegetable = 7,800 kg starting inventory as OPENING_STOCK
        val time = System.currentTimeMillis()
        val initialMovements = _vegetables.value.map { veg ->
            StockMovement(
                id = "sm_init_${veg.id}",
                vegetableId = veg.id,
                vegetableName = veg.nameEn,
                quantityChangeKg = 200.0,
                reason = "OPENING_STOCK",
                orderId = null,
                timestamp = time,
                actor = "System Opening Inventory"
            )
        }
        _stockMovements.value = initialMovements

        // Seed some initial baseline operating expenses for realistic accounting
        _expenses.value = listOf(
            ExpenseRecord(
                id = "exp_01",
                title = "Vashi APMC Mandi Transport",
                category = "Transport",
                amount = 450.0,
                timestamp = time - 86400000L,
                note = "Wholesale produce vehicle transport to Vidyavihar"
            ),
            ExpenseRecord(
                id = "exp_02",
                title = "Biodegradable Vegetable Bags",
                category = "Packaging",
                amount = 220.0,
                timestamp = time - 86400000L,
                note = "Eco-friendly retail carry pouches"
            )
        )
    }

    fun toggleLanguage() {
        _isHindi.value = !_isHindi.value
    }

    fun isStoreOpen(): Boolean {
        val settings = _businessSettings.value
        if (!settings.isStoreOpenManual) return false

        // Calculate IST hour
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        return hour in settings.openHourIST until settings.closeHourIST
    }

    fun addToCart(vegetableId: String, quantityKg: Double) {
        val current = _cart.value.toMutableMap()
        val veg = _vegetables.value.find { it.id == vegetableId } ?: return
        val existing = current[vegetableId] ?: 0.0
        val target = existing + quantityKg
        if (target <= 0.0) {
            current.remove(vegetableId)
        } else {
            // Check stock boundary
            current[vegetableId] = minOf(target, veg.stockKg)
        }
        _cart.value = current
    }

    fun updateCartQuantity(vegetableId: String, exactQuantityKg: Double) {
        val current = _cart.value.toMutableMap()
        val veg = _vegetables.value.find { it.id == vegetableId } ?: return
        if (exactQuantityKg <= 0.0) {
            current.remove(vegetableId)
        } else {
            current[vegetableId] = minOf(exactQuantityKg, veg.stockKg)
        }
        _cart.value = current
    }

    fun removeFromCart(vegetableId: String) {
        val current = _cart.value.toMutableMap()
        current.remove(vegetableId)
        _cart.value = current
    }

    fun clearCart() {
        _cart.value = emptyMap()
    }

    @Synchronized
    fun placeOrder(
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        pincode: String,
        paymentMethod: PaymentMethod
    ): Result<Order> {
        if (!isStoreOpen()) {
            return Result.failure(IllegalStateException("Store is currently closed. Operating hours: 7:00 AM - 11:00 PM IST."))
        }

        val cartMap = _cart.value
        if (cartMap.isEmpty()) {
            return Result.failure(IllegalStateException("Cart is empty."))
        }

        // Validate Mumbai service area: pincodes 400xxx or Mumbai address mention
        val isMumbaiPin = pincode.trim().startsWith("400")
        val isMumbaiAddress = customerAddress.contains("Mumbai", ignoreCase = true) ||
                customerAddress.contains("Vidyavihar", ignoreCase = true) ||
                customerAddress.contains("Ghatkopar", ignoreCase = true) ||
                customerAddress.contains("Kurla", ignoreCase = true) ||
                customerAddress.contains("Powai", ignoreCase = true)

        if (!isMumbaiPin && !isMumbaiAddress) {
            return Result.failure(IllegalArgumentException("Service area restricted to Mumbai, Maharashtra (Pincodes starting with 400)."))
        }

        // Verify stock sufficiency for all items atomically
        val currentVegs = _vegetables.value.associateBy { it.id }
        for ((vegId, reqQty) in cartMap) {
            val veg = currentVegs[vegId]
                ?: return Result.failure(IllegalStateException("Product $vegId not found in catalog."))
            if (veg.stockKg < reqQty) {
                return Result.failure(IllegalStateException("Insufficient stock for ${veg.nameEn}. Available: ${veg.stockKg} kg, requested: $reqQty kg."))
            }
        }

        // Create immutable snapshots with current cost snapshot
        var subtotal = 0.0
        val itemSnapshots = mutableListOf<OrderItemSnapshot>()
        for ((vegId, reqQty) in cartMap) {
            val veg = currentVegs[vegId]!!
            val priceSnapshot = veg.effectivePricePerKg
            val costSnapshot = veg.costPerKg
            val itemTotal = priceSnapshot * reqQty
            subtotal += itemTotal
            itemSnapshots.add(
                OrderItemSnapshot(
                    vegetableId = veg.id,
                    nameEn = veg.nameEn,
                    nameHi = veg.nameHi,
                    quantityKg = reqQty,
                    pricePerKgSnapshot = priceSnapshot,
                    costPerKgSnapshot = costSnapshot,
                    itemTotal = itemTotal
                )
            )
        }

        val minOrder = _businessSettings.value.minOrderValue
        if (subtotal < minOrder) {
            return Result.failure(IllegalStateException("Minimum order value is ₹${minOrder.toInt()}. Current subtotal is ₹${subtotal.toInt()}."))
        }

        val deliveryFee = _businessSettings.value.deliveryCharge
        val discount = 0.0
        val total = subtotal + deliveryFee - discount

        val time = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US)
        val formattedDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(Date(time))
        val orderId = "SV-${sdf.format(Date(time))}-${(100..999).random()}"

        val order = Order(
            orderId = orderId,
            customerName = customerName,
            customerPhone = customerPhone,
            customerAddress = customerAddress,
            mumbaiPincode = pincode,
            items = itemSnapshots,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            discount = discount,
            total = total,
            paymentMethod = paymentMethod,
            paymentStatus = if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY) PaymentStatus.PENDING else PaymentStatus.PENDING,
            orderStatus = OrderStatus.PLACED,
            timestamp = time,
            createdAtFormatted = formattedDate
        )

        // Atomically deduct inventory & record stock movement ledger
        val updatedVegs = _vegetables.value.map { veg ->
            val orderQty = cartMap[veg.id]
            if (orderQty != null && orderQty > 0) {
                veg.copy(stockKg = veg.stockKg - orderQty)
            } else {
                veg
            }
        }
        _vegetables.value = updatedVegs

        val newMovements = cartMap.map { (vegId, qty) ->
            StockMovement(
                id = "sm_ord_${orderId}_$vegId",
                vegetableId = vegId,
                vegetableName = currentVegs[vegId]?.nameEn ?: vegId,
                quantityChangeKg = -qty,
                reason = "ORDER_DEDUCTION",
                orderId = orderId,
                timestamp = time,
                actor = customerName
            )
        }
        _stockMovements.value = newMovements + _stockMovements.value
        _orders.value = listOf(order) + _orders.value
        clearCart()

        return Result.success(order)
    }

    @Synchronized
    fun cancelOrder(orderId: String, reason: String = "Customer request"): Result<Unit> {
        val existing = _orders.value.find { it.orderId == orderId }
            ?: return Result.failure(IllegalArgumentException("Order $orderId not found."))

        // Idempotency check: never restore stock twice
        if (existing.orderStatus == OrderStatus.CANCELLED) {
            return Result.success(Unit)
        }

        if (existing.orderStatus == OrderStatus.DELIVERED) {
            return Result.failure(IllegalStateException("Delivered orders cannot be cancelled."))
        }

        val time = System.currentTimeMillis()

        // Atomically restore stock
        val orderItemMap = existing.items.associate { it.vegetableId to it.quantityKg }
        val updatedVegs = _vegetables.value.map { veg ->
            val returnQty = orderItemMap[veg.id]
            if (returnQty != null && returnQty > 0) {
                veg.copy(stockKg = veg.stockKg + returnQty)
            } else {
                veg
            }
        }
        _vegetables.value = updatedVegs

        // Record restoration in stock movement ledger
        val newMovements = existing.items.map { item ->
            StockMovement(
                id = "sm_cancel_${orderId}_${item.vegetableId}",
                vegetableId = item.vegetableId,
                vegetableName = item.nameEn,
                quantityChangeKg = item.quantityKg,
                reason = "ORDER_CANCELLATION_RESTORE",
                orderId = orderId,
                timestamp = time,
                actor = "Saif Hakim Rain (System)"
            )
        }
        _stockMovements.value = newMovements + _stockMovements.value

        // Update order status
        _orders.value = _orders.value.map {
            if (it.orderId == orderId) {
                it.copy(
                    orderStatus = OrderStatus.CANCELLED,
                    cancellationReason = reason,
                    paymentStatus = if (it.paymentStatus == PaymentStatus.VERIFIED) PaymentStatus.REFUNDED else it.paymentStatus
                )
            } else {
                it
            }
        }

        return Result.success(Unit)
    }

    fun updateOrderStatus(orderId: String, newStatus: OrderStatus) {
        _orders.value = _orders.value.map {
            if (it.orderId == orderId) it.copy(orderStatus = newStatus) else it
        }
    }

    fun verifyOrderPayment(orderId: String, isVerified: Boolean) {
        _orders.value = _orders.value.map {
            if (it.orderId == orderId) {
                it.copy(paymentStatus = if (isVerified) PaymentStatus.VERIFIED else PaymentStatus.PENDING)
            } else {
                it
            }
        }
    }

    fun updateProductPrice(id: String, newPrice: Double, newCost: Double, newDiscount: Int) {
        _vegetables.value = _vegetables.value.map {
            if (it.id == id) {
                it.copy(
                    pricePerKg = newPrice,
                    costPerKg = newCost,
                    discountPercent = newDiscount
                )
            } else {
                it
            }
        }
    }

    @Synchronized
    fun replenishStock(id: String, addKg: Double, actor: String = "Saif Hakim Rain (Owner)") {
        val veg = _vegetables.value.find { it.id == id } ?: return
        val time = System.currentTimeMillis()
        _vegetables.value = _vegetables.value.map {
            if (it.id == id) it.copy(stockKg = it.stockKg + addKg) else it
        }
        val movement = StockMovement(
            id = "sm_rep_${id}_$time",
            vegetableId = id,
            vegetableName = veg.nameEn,
            quantityChangeKg = addKg,
            reason = "OWNER_REPLENISHMENT",
            orderId = null,
            timestamp = time,
            actor = actor
        )
        _stockMovements.value = listOf(movement) + _stockMovements.value
    }

    @Synchronized
    fun setExactStock(id: String, targetKg: Double, actor: String = "Saif Hakim Rain (Owner)") {
        val veg = _vegetables.value.find { it.id == id } ?: return
        val clampedKg = targetKg.coerceAtLeast(0.0)
        val diff = clampedKg - veg.stockKg
        val time = System.currentTimeMillis()
        _vegetables.value = _vegetables.value.map {
            if (it.id == id) it.copy(stockKg = clampedKg) else it
        }
        val movement = StockMovement(
            id = "sm_set_${id}_$time",
            vegetableId = id,
            vegetableName = veg.nameEn,
            quantityChangeKg = diff,
            reason = if (clampedKg == 0.0) "MARKED_OUT_OF_STOCK" else "MANUAL_STOCK_ADJUSTMENT",
            orderId = null,
            timestamp = time,
            actor = actor
        )
        _stockMovements.value = listOf(movement) + _stockMovements.value
    }

    fun addExpense(title: String, category: String, amount: Double, note: String = "") {
        val exp = ExpenseRecord(
            id = "exp_${System.currentTimeMillis()}",
            title = title,
            category = category,
            amount = amount,
            timestamp = System.currentTimeMillis(),
            note = note
        )
        _expenses.value = listOf(exp) + _expenses.value
    }

    fun updateBusinessSettings(newSettings: BusinessSettings) {
        _businessSettings.value = newSettings
    }

    fun updateCustomerProfile(user: CustomerUser) {
        _customerUser.value = user
    }

    // Accounting Engine Calculations
    data class ProfitAndLossReport(
        val totalOrders: Int,
        val completedOrders: Int,
        val pendingOrders: Int,
        val cancelledOrders: Int,
        val grossProductSales: Double,
        val discountsGiven: Double,
        val netProductSales: Double,
        val costOfGoodsSold: Double,
        val grossProfit: Double,
        val deliveryChargesCollected: Double,
        val totalOperatingExpenses: Double,
        val netOperatingProfit: Double,
        val isMissingCostData: Boolean = false
    )

    fun calculatePAndL(): ProfitAndLossReport {
        val allOrders = _orders.value
        val validOrders = allOrders.filter { it.orderStatus != OrderStatus.CANCELLED }

        val grossSales = validOrders.sumOf { ord ->
            ord.items.sumOf { it.pricePerKgSnapshot * it.quantityKg }
        }
        val discounts = validOrders.sumOf { it.discount }
        val netSales = grossSales - discounts

        val cogs = validOrders.sumOf { ord ->
            ord.items.sumOf { it.costPerKgSnapshot * it.quantityKg }
        }
        val grossProfit = netSales - cogs

        val deliveryCharges = validOrders.sumOf { it.deliveryFee }
        val totalExpenses = _expenses.value.sumOf { it.amount }
        val netProfit = grossProfit + deliveryCharges - totalExpenses

        return ProfitAndLossReport(
            totalOrders = allOrders.size,
            completedOrders = allOrders.count { it.orderStatus == OrderStatus.DELIVERED },
            pendingOrders = allOrders.count { it.orderStatus in listOf(OrderStatus.PLACED, OrderStatus.PREPARING, OrderStatus.DISPATCHED) },
            cancelledOrders = allOrders.count { it.orderStatus == OrderStatus.CANCELLED },
            grossProductSales = grossSales,
            discountsGiven = discounts,
            netProductSales = netSales,
            costOfGoodsSold = cogs,
            grossProfit = grossProfit,
            deliveryChargesCollected = deliveryCharges,
            totalOperatingExpenses = totalExpenses,
            netOperatingProfit = netProfit,
            isMissingCostData = false
        )
    }

    fun resetForTesting() {
        _cart.value = emptyMap()
        _orders.value = emptyList()
        seedVegetableCatalogue()
        seedOpeningStockLedger()
    }

    companion object {
        @Volatile
        private var instance: SaifVegetableRepository? = null

        fun getInstance(): SaifVegetableRepository {
            return instance ?: synchronized(this) {
                instance ?: SaifVegetableRepository().also { instance = it }
            }
        }
    }
}
