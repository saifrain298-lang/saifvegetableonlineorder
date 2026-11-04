package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaifVegetableRepository
import com.example.model.Order
import com.example.model.OrderStatus
import com.example.model.PaymentStatus
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*

@Composable
fun OrderDetailsScreen(
    repository: SaifVegetableRepository,
    onNavigateToShop: () -> Unit,
    onRequireLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customer by repository.customerUser.collectAsState()
    val orders by repository.orders.collectAsState()
    val isHindi by repository.isHindi.collectAsState()
    val settings by repository.businessSettings.collectAsState()

    var cancelTargetOrder by remember { mutableStateOf<Order?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // Screen Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (isHindi) "ऑर्डर विवरण व इतिहास" else "Order Details & Tracking",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (customer.isLoggedIn) "Customer: ${customer.name} (+91 ${customer.phone})" else "Add your details to view personalized orders",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            if (!customer.isLoggedIn) {
                Button(
                    onClick = onRequireLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(if (isHindi) "विवरण भरें" else "Enter Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (isHindi) "कोई पिछला ऑर्डर नहीं मिला" else "No orders placed yet",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isHindi) "39 ताजी सब्जियों में से ऑर्डर करें" else "Place your first order from 39 fresh veggies",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )
                    Button(
                        onClick = onNavigateToShop,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isHindi) "सब्जियां खरीदें" else "Shop Fresh Veggies")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(orders, key = { it.orderId }) { order ->
                    CustomerOrderCard(
                        order = order,
                        isHindi = isHindi,
                        onShareWhatsApp = {
                            val msg = "Hello Saif Vegetable! Please share delivery update for my order ${order.orderId} (₹${order.total.toInt()})."
                            launchWhatsApp(context, settings.whatsapp, msg)
                        },
                        onCancelClick = { cancelTargetOrder = order }
                    )
                }
            }
        }
    }

    // Cancellation Confirmation Dialog
    if (cancelTargetOrder != null) {
        val target = cancelTargetOrder!!
        AlertDialog(
            onDismissRequest = { cancelTargetOrder = null },
            title = {
                Text(
                    text = if (isHindi) "ऑर्डर रद्द करें?" else "Cancel Order?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isHindi)
                        "क्या आप वाकई ऑर्डर ${target.orderId} को रद्द करना चाहते हैं? आरक्षित स्टॉक इन्वेंट्री में तुरंत वापस जोड़ दिया जाएगा।"
                    else
                        "Are you sure you want to cancel order ${target.orderId}? Reserved vegetable stock will be automatically restored to inventory."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        repository.cancelOrder(target.orderId, "Customer cancelled via app")
                        cancelTargetOrder = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
                ) {
                    Text(if (isHindi) "हां, रद्द करें" else "Yes, Cancel Order")
                }
            },
            dismissButton = {
                TextButton(onClick = { cancelTargetOrder = null }) {
                    Text(if (isHindi) "वापस जाएं" else "Back")
                }
            }
        )
    }
}

@Composable
fun CustomerOrderCard(
    order: Order,
    isHindi: Boolean,
    onShareWhatsApp: () -> Unit,
    onCancelClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val statusColor = when (order.orderStatus) {
        OrderStatus.PLACED -> StatusBlue
        OrderStatus.PREPARING -> AccentOrange
        OrderStatus.DISPATCHED -> PrimaryGreen
        OrderStatus.DELIVERED -> SecondaryGreen
        OrderStatus.CANCELLED -> StatusRed
    }

    val canCancel = order.orderStatus in listOf(OrderStatus.PLACED, OrderStatus.PREPARING)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("order_card_${order.orderId}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Order ID & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = order.orderId,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = order.createdAtFormatted,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = if (isHindi) order.orderStatus.titleHi else order.orderStatus.titleEn,
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Status Stepper (if not cancelled)
            if (order.orderStatus != OrderStatus.CANCELLED) {
                OrderTimelineBar(currentStatus = order.orderStatus, isHindi = isHindi)
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Summary Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${order.items.size} ${if (isHindi) "सब्जियां" else "Veggies"} • ${order.paymentMethod.titleEn}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = "Total: ₹${order.total.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = PrimaryGreen
                )
            }

            // Expanded Item Breakdown
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isHindi) "सब्जी सूची (मूल्य स्नैपशॉट सहित):" else "Itemized Breakdown (Cost Snapshot):",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    order.items.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${if (isHindi) item.nameHi else item.nameEn} (${item.quantityKg} kg @ ₹${item.pricePerKgSnapshot.toInt()}/kg)",
                                fontSize = 12.sp
                            )
                            Text(
                                text = "₹${item.itemTotal.toInt()}",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Delivery Fee (Mumbai)", fontSize = 12.sp, color = TextSecondary)
                        Text("₹${order.deliveryFee.toInt()}", fontSize = 12.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Payment Verification", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            text = if (isHindi) order.paymentStatus.titleHi else order.paymentStatus.titleEn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (order.paymentStatus == PaymentStatus.VERIFIED) StatusGreen else StatusOrange
                        )
                    }

                    Text(
                        text = "Delivery Address: ${order.customerAddress} (${order.mumbaiPincode})",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (expanded) (if (isHindi) "विवरण छिपाएं" else "Hide Details") else (if (isHindi) "विवरण देखें" else "View Details"),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (canCancel) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text(if (isHindi) "रद्द करें" else "Cancel", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = onShareWhatsApp,
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isHindi) "व्हाट्सएप" else "WhatsApp", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun OrderTimelineBar(currentStatus: OrderStatus, isHindi: Boolean) {
    val steps = listOf(
        OrderStatus.PLACED to (if (isHindi) "दर्ज" else "Placed"),
        OrderStatus.PREPARING to (if (isHindi) "सफाई/पैकिंग" else "Packing"),
        OrderStatus.DISPATCHED to (if (isHindi) "रवाना" else "Dispatched"),
        OrderStatus.DELIVERED to (if (isHindi) "डिलीवर" else "Delivered")
    )

    val currentIdx = when (currentStatus) {
        OrderStatus.PLACED -> 0
        OrderStatus.PREPARING -> 1
        OrderStatus.DISPATCHED -> 2
        OrderStatus.DELIVERED -> 3
        OrderStatus.CANCELLED -> -1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { idx, (_, title) ->
            val isDone = idx <= currentIdx
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isDone) PrimaryGreen else DividerColor),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDone) PrimaryGreen else TextMuted,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
