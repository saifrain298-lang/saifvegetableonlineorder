package com.example.model

enum class VegetableCategory(val titleEn: String, val titleHi: String) {
    ALL("All Veggies", "सभी सब्जियां"),
    ESSENTIALS("Daily Essentials", "दैनिक आवश्यक"),
    LEAFY("Fresh Leafies", "हरी पत्तेदार"),
    GOURDS("Gourds & Melons", "लौकी व कद्दू वर्ग"),
    ROOTS("Root Vegetables", "जड़ वाली सब्जियां"),
    BEANS_SPROUTS("Beans & Sprouts", "फलियां व अंकुरित"),
    EXOTIC("Exotics & Peppers", "शिमला मिर्च व विदेशी")
}

data class VegetableItem(
    val id: String,
    val nameEn: String,
    val nameHi: String,
    val category: VegetableCategory,
    val drawableResId: Int,
    val pricePerKg: Double,
    val costPerKg: Double,
    val discountPercent: Int = 0,
    val stockKg: Double = 200.0, // Seeded at 200 kg
    val lowStockThreshold: Double = 20.0,
    val unit: String = "kg",
    val descriptionEn: String,
    val descriptionHi: String,
    val isAvailable: Boolean = true,
    val isOrganicCertified: Boolean = true,
    val isVerifiedPrice: Boolean = true,
    val rating: Double? = null, // No fabricated ratings; null until genuine reviews exist
    val reviewCount: Int = 0
) {
    val effectivePricePerKg: Double
        get() = if (discountPercent > 0) pricePerKg * (1.0 - discountPercent / 100.0) else pricePerKg
}

data class CartItem(
    val vegetableId: String,
    val quantityKg: Double
)

data class OrderItemSnapshot(
    val vegetableId: String,
    val nameEn: String,
    val nameHi: String,
    val quantityKg: Double,
    val pricePerKgSnapshot: Double,
    val costPerKgSnapshot: Double,
    val itemTotal: Double
)

enum class OrderStatus(val titleEn: String, val titleHi: String) {
    PLACED("Order Placed", "ऑर्डर दर्ज हुआ"),
    PREPARING("Cleaning & Sorting", "सफाई व पैकिंग जारी"),
    DISPATCHED("Out for Delivery", "डिलीवरी के लिए रवाना"),
    DELIVERED("Delivered", "सफलतापूर्वक डिलीवर"),
    CANCELLED("Cancelled", "रद्द किया गया")
}

enum class PaymentMethod(val titleEn: String, val titleHi: String) {
    UPI("UPI / QR Code / GPay", "यूपीआई / क्यूआर कोड"),
    CASH_ON_DELIVERY("Cash on Delivery (COD)", "डिलीवरी पर नकद (COD)")
}

enum class PaymentStatus(val titleEn: String, val titleHi: String) {
    PENDING("Payment Pending", "भुगतान लंबित"),
    VERIFIED("Verified & Paid", "सत्यापित व प्राप्त"),
    REFUNDED("Refunded", "धनवापसी पूर्ण")
}

data class Order(
    val orderId: String,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    val mumbaiPincode: String,
    val items: List<OrderItemSnapshot>,
    val subtotal: Double,
    val deliveryFee: Double,
    val discount: Double,
    val total: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val orderStatus: OrderStatus,
    val timestamp: Long,
    val createdAtFormatted: String,
    val upiTransactionRef: String? = null,
    val cancellationReason: String? = null
)

data class StockMovement(
    val id: String,
    val vegetableId: String,
    val vegetableName: String,
    val quantityChangeKg: Double,
    val reason: String, // "OPENING_STOCK", "ORDER_DEDUCTION", "ORDER_CANCELLATION_RESTORE", "OWNER_REPLENISHMENT", "OWNER_ADJUSTMENT"
    val orderId: String? = null,
    val timestamp: Long,
    val actor: String = "Saif Hakim Rain (Owner)"
)

data class ExpenseRecord(
    val id: String,
    val title: String,
    val category: String, // Transport, Mandi Handling, Packing, Electricity, Fuel
    val amount: Double,
    val timestamp: Long,
    val note: String = ""
)

data class BusinessSettings(
    val businessName: String = "Saif Vegetable",
    val ownerName: String = "Saif Hakim Rain",
    val email: String = "saifrain298@gmail.com",
    val address: String = "Premier Exotica 2, opposite Don Bosco Institute, Vidyavihar, Mumbai, Maharashtra, India",
    val serviceArea: String = "Mumbai, Maharashtra, India",
    val phone: String = "+91 84529 53630",
    val whatsapp: String = "+91 84529 53630",
    val upiId: String = "8452953630@ptsbi",
    val isUpiVerified: Boolean = false, // Verification required badge until owner confirms
    val minOrderValue: Double = 100.0,
    val deliveryCharge: Double = 10.0,
    val openHourIST: Int = 7,
    val closeHourIST: Int = 23,
    val isStoreOpenManual: Boolean = true,
    val ownerPasscode: String = "1234"
)

data class CustomerUser(
    val id: String = "cust_default",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val pincode: String = "400077",
    val email: String = "",
    val isLoggedIn: Boolean = false
)
