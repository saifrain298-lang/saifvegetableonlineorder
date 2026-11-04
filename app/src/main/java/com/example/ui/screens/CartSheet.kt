package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaifVegetableRepository
import com.example.model.*
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartBottomSheet(
    repository: SaifVegetableRepository,
    onDismiss: () -> Unit,
    onOrderPlaced: (Order) -> Unit,
    onRequireLogin: () -> Unit
) {
    val context = LocalContext.current
    val cart by repository.cart.collectAsState()
    val vegetables by repository.vegetables.collectAsState()
    val settings by repository.businessSettings.collectAsState()
    val customer by repository.customerUser.collectAsState()
    val isHindi by repository.isHindi.collectAsState()
    val isStoreOpen = repository.isStoreOpen()

    val vegMap = remember(vegetables) { vegetables.associateBy { it.id } }

    var selectedPayment by remember { mutableStateOf(PaymentMethod.UPI) }
    var orderError by remember { mutableStateOf<String?>(null) }
    var confirmedOrder by remember { mutableStateOf<Order?>(null) }

    val cartItemsList = remember(cart, vegMap) {
        cart.mapNotNull { (id, qty) ->
            vegMap[id]?.let { veg -> Triple(veg, qty, veg.effectivePricePerKg * qty) }
        }
    }

    val subtotal = remember(cartItemsList) { cartItemsList.sumOf { it.third } }
    val deliveryFee = settings.deliveryCharge
    val minOrder = settings.minOrderValue
    val grandTotal = if (cartItemsList.isNotEmpty()) subtotal + deliveryFee else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isHindi) "आपकी ताजी टोकरी" else "Your Fresh Cart",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            if (confirmedOrder != null) {
                val order = confirmedOrder!!
                // Success Confirmation Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = LightGreenContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHindi) "ऑर्डर सफलतापूर्वक दर्ज हुआ!" else "Order Placed Successfully!",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = OnGreenContainer
                        )
                        Text(
                            text = "Order ID: ${order.orderId}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryDarkGreen,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (isHindi)
                                "सैफ वेजिटेबल, विद्याविहार से आपका ऑर्डर तैयार किया जा रहा है।"
                            else
                                "Your farm-fresh order is being prepared by Saif Hakim Rain in Vidyavihar, Mumbai.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Button(
                            onClick = {
                                val msg = "Hello Saif Vegetable! I placed order ${order.orderId} for ₹${order.total.toInt()} to ${order.customerAddress}. Please confirm delivery timing."
                                launchWhatsApp(context, settings.whatsapp, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Share Order on WhatsApp", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                onOrderPlaced(order)
                                onDismiss()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (isHindi) "ऑर्डर विवरण देखें" else "Track in Order Details")
                        }
                    }
                }
            } else if (cartItemsList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ShoppingBasket,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(60.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isHindi) "आपकी टोकरी खाली है" else "Your cart is empty",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = if (isHindi) "39 ताजी सब्जियों में से चुनें" else "Choose from 39 fresh farm-picked veggies",
                            fontSize = 13.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Cart Item List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .padding(vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItemsList) { (veg, qty, itemTotal) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = veg.drawableResId),
                                contentDescription = veg.nameEn,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) veg.nameHi else veg.nameEn,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "₹${veg.effectivePricePerKg.toInt()}/kg",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "Subtotal: ₹${itemTotal.toInt()}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = PrimaryGreen
                                )
                            }

                            // Stepper
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(PrimaryGreen.copy(alpha = 0.1f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                IconButton(
                                    onClick = { repository.addToCart(veg.id, -0.5) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Decrease",
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "${String.format("%.1f", qty)} kg",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp)
                                )
                                IconButton(
                                    onClick = { repository.addToCart(veg.id, 0.5) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Increase",
                                        tint = PrimaryGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Minimum Order Warning
                if (subtotal < minOrder) {
                    val remaining = minOrder - subtotal
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = AccentOrange)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isHindi)
                                    "न्यूनतम ऑर्डर ₹${minOrder.toInt()} है। कृपया ₹${remaining.toInt()} का सामान और जोड़ें।"
                                else
                                    "Minimum order is ₹${minOrder.toInt()}. Add ₹${remaining.toInt()} more to checkout.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFBF360C)
                            )
                        }
                    }
                }

                // Payment Method Selector
                Text(
                    text = if (isHindi) "भुगतान विकल्प" else "Payment Method",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPayment == PaymentMethod.UPI,
                        onClick = { selectedPayment = PaymentMethod.UPI },
                        label = { Text("UPI / QR / GPay") },
                        leadingIcon = {
                            Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedPayment == PaymentMethod.CASH_ON_DELIVERY,
                        onClick = { selectedPayment = PaymentMethod.CASH_ON_DELIVERY },
                        label = { Text("Cash on Delivery") },
                        leadingIcon = {
                            Icon(Icons.Default.Money, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Price Summary Breakdown
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Items Subtotal", fontSize = 12.sp, color = TextSecondary)
                        Text("₹${subtotal.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Fee (Mumbai Only)", fontSize = 12.sp, color = TextSecondary)
                        Text("₹${deliveryFee.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = DividerColor)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Grand Total", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("₹${grandTotal.toInt()}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryGreen)
                    }
                }

                if (orderError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = orderError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Checkout Button
                Button(
                    onClick = {
                        if (!customer.isLoggedIn) {
                            onRequireLogin()
                            return@Button
                        }
                        if (!isStoreOpen) {
                            orderError = if (isHindi)
                                "दुकान अभी बंद है (व्यापारिक समय: सुबह 7:00 से रात 11:00 बजे IST)।"
                            else
                                "Store is closed. Operating hours are 7:00 AM - 11:00 PM IST."
                            return@Button
                        }
                        if (subtotal < minOrder) {
                            orderError = "Minimum order value is ₹${minOrder.toInt()}."
                            return@Button
                        }

                        val result = repository.placeOrder(
                            customerName = customer.name,
                            customerPhone = customer.phone,
                            customerAddress = customer.address,
                            pincode = customer.pincode,
                            paymentMethod = selectedPayment
                        )

                        result.onSuccess { ord ->
                            confirmedOrder = ord
                            orderError = null
                        }.onFailure { err ->
                            orderError = err.message ?: "Failed to place order."
                        }
                    },
                    enabled = subtotal >= minOrder && isStoreOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("cart_place_order_button")
                ) {
                    Text(
                        text = if (!customer.isLoggedIn) {
                            if (isHindi) "ऑर्डर के लिए लॉगिन करें" else "Sign In to Place Order"
                        } else if (!isStoreOpen) {
                            if (isHindi) "दुकान अभी बंद है" else "Store Currently Closed"
                        } else {
                            if (isHindi) "ऑर्डर दर्ज करें • ₹${grandTotal.toInt()}" else "Confirm Order • ₹${grandTotal.toInt()}"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
