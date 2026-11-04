package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.SaifVegetableRepository
import com.example.ui.components.launchDialer
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*

data class ChatMessage(
    val sender: String, // "AI" or "USER"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val actionType: String? = null // "WHATSAPP", "CALL", null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeAiBotDialog(
    repository: SaifVegetableRepository,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isHindi by repository.isHindi.collectAsState()
    val vegetables by repository.vegetables.collectAsState()
    val settings by repository.businessSettings.collectAsState()
    val orders by repository.orders.collectAsState()

    var inputQuery by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = "AI",
                text = if (isHindi)
                    "नमस्ते! मैं सैफ वेजिटेबल का सैफ एआई सहायक (Saif AI Assistant) हूँ। मैं आपको ताजी सब्जियों की कीमत, स्टॉक उपलब्धता, डिलीवरी नियम (केवल मुंबई) या आपके ऑर्डर की स्थिति बताने में मदद कर सकता हूँ। आप क्या जानना चाहते हैं?"
                else
                    "Hello! I am the Saif AI Assistant for Saif Vegetable. I can help you with fresh vegetable prices, current stock availability, Mumbai delivery guidelines, store hours (7 AM - 11 PM IST), or order tracking. How can I assist you today?"
            )
        )
    }

    val quickQuestions = if (isHindi) {
        listOf(
            "आज टमाटर और प्याज की कीमत?",
            "डिलीवरी शुल्क और न्यूनतम ऑर्डर?",
            "क्या आप मुंबई के बाहर डिलीवरी करते हैं?",
            "दुकान का समय क्या है?",
            "मालिक सैफ हाकिम से सीधे बात करें"
        )
    } else {
        listOf(
            "Tomato & Onion Price?",
            "Delivery charges & Minimum order?",
            "Do you deliver outside Mumbai?",
            "Store hours & address?",
            "Connect with owner Saif Hakim"
        )
    }

    fun handleSend(query: String) {
        val q = query.trim()
        if (q.isEmpty()) return

        messages.add(ChatMessage(sender = "USER", text = q))
        inputQuery = ""

        val lower = q.lowercase()

        // 1. Owner or Direct Human Escalation
        if (lower.contains("owner") || lower.contains("saif") || lower.contains("human") || lower.contains("call") || lower.contains("बात") || lower.contains("संपर्क")) {
            val responseText = if (isHindi)
                "आप सीधे सैफ वेजिटेबल के मालिक सैफ हाकिम रैन से संपर्क कर सकते हैं:\n• पता: प्रीमियर एक्सोटिका 2, विद्याविहार, मुंबई\n• फोन/व्हाट्सएप: ${settings.phone}\n• ईमेल: ${settings.email}\nनीचे दिए गए बटन से सीधे व्हाट्सएप या कॉल करें।"
            else
                "You can directly reach owner Saif Hakim Rain:\n• Address: Premier Exotica 2, Vidyavihar, Mumbai\n• Phone/WhatsApp: ${settings.phone}\n• Email: ${settings.email}\nClick below to connect via WhatsApp or phone call."
            messages.add(ChatMessage(sender = "AI", text = responseText, actionType = "WHATSAPP"))
            return
        }

        // 2. Vegetable Price & Stock Query Grounded in Live Database
        val matchingVeg = vegetables.find { veg ->
            lower.contains(veg.nameEn.lowercase()) ||
                    lower.contains(veg.nameHi.lowercase()) ||
                    (lower.contains("potato") && veg.nameEn == "Potato") ||
                    (lower.contains("aloo") && veg.nameEn == "Potato") ||
                    (lower.contains("kanda") && veg.nameEn == "Onion") ||
                    (lower.contains("onion") && veg.nameEn == "Onion") ||
                    (lower.contains("tamatar") && veg.nameEn == "Tomato") ||
                    (lower.contains("tomato") && veg.nameEn == "Tomato") ||
                    (lower.contains("broccoli") && veg.nameEn == "Broccoli") ||
                    (lower.contains("spinach") && veg.nameEn == "Spinach") ||
                    (lower.contains("palak") && veg.nameEn == "Spinach") ||
                    (lower.contains("mirchi") && veg.nameEn == "Green Chilli") ||
                    (lower.contains("adrak") && veg.nameEn == "Ginger") ||
                    (lower.contains("lahsun") && veg.nameEn == "Garlic")
        }

        if (matchingVeg != null) {
            val stockText = if (matchingVeg.stockKg > 0) {
                if (isHindi) "उपलब्ध स्टॉक: ${matchingVeg.stockKg} kg" else "In stock: ${matchingVeg.stockKg} kg available"
            } else {
                if (isHindi) "वर्तमान में स्टॉक समाप्त" else "Currently out of stock"
            }
            val responseText = if (isHindi)
                "${matchingVeg.nameHi} (${matchingVeg.nameEn}):\n• कीमत: ₹${matchingVeg.effectivePricePerKg.toInt()} प्रति किलो\n• $stockText\n• 100% साफ और ताजी गुणवत्ता।"
            else
                "${matchingVeg.nameEn} (${matchingVeg.nameHi}):\n• Price: ₹${matchingVeg.effectivePricePerKg.toInt()}/kg\n• $stockText\n• 100% Farm-fresh and hand-sorted quality."
            messages.add(ChatMessage(sender = "AI", text = responseText))
            return
        }

        // 3. Delivery & Minimum Order Policy
        if (lower.contains("delivery") || lower.contains("charge") || lower.contains("minimum") || lower.contains("शुल्क") || lower.contains("न्यूनतम") || lower.contains("order")) {
            val responseText = if (isHindi)
                "सैफ वेजिटेबल डिलीवरी नियम:\n• न्यूनतम ऑर्डर मूल्य: ₹${settings.minOrderValue.toInt()}\n• फ्लैट डिलीवरी शुल्क: ₹${settings.deliveryCharge.toInt()}\n• सेवा क्षेत्र: केवल मुंबई, महाराष्ट्र (पिनकोड 400xxx)\n• भुगतान: यूपीआई / क्यूआर कोड या डिलीवरी पर नकद (COD)।"
            else
                "Saif Vegetable Delivery Policy:\n• Minimum Order Value: ₹${settings.minOrderValue.toInt()}\n• Flat Delivery Fee: ₹${settings.deliveryCharge.toInt()}\n• Service Area: Exclusively Mumbai, Maharashtra (Pincodes starting 400xxx)\n• Payment: UPI / QR Code or Cash on Delivery (COD)."
            messages.add(ChatMessage(sender = "AI", text = responseText))
            return
        }

        // 4. Mumbai Location restriction
        if (lower.contains("outside") || lower.contains("pune") || lower.contains("delhi") || lower.contains("thane") || lower.contains("बाहर") || lower.contains("क्षेत्र")) {
            val responseText = if (isHindi)
                "क्षमा करें, ताजी सब्जियों की गुणवत्ता बनाए रखने के लिए हमारी डिलीवरी सेवा केवल मुंबई महानगर क्षेत्र (विद्याविहार, कुर्ला, घाटकोपर, पवई आदि) में उपलब्ध है।"
            else
                "To ensure supreme freshness, Saif Vegetable delivers strictly within Mumbai, Maharashtra (Vidyavihar, Ghatkopar, Kurla, Chembur, Powai, etc.). We do not deliver outside Mumbai."
            messages.add(ChatMessage(sender = "AI", text = responseText))
            return
        }

        // 5. Store Timings & Operating Hours
        if (lower.contains("time") || lower.contains("hour") || lower.contains("open") || lower.contains("close") || lower.contains("समय") || lower.contains("खुल")) {
            val isOpen = repository.isStoreOpen()
            val statusStr = if (isOpen) {
                if (isHindi) "वर्तमान में दुकान खुली है" else "Store is currently OPEN"
            } else {
                if (isHindi) "वर्तमान में दुकान बंद है" else "Store is currently CLOSED"
            }
            val responseText = if (isHindi)
                "कार्यकाल और समय:\n• प्रतिदिन सुबह 7:00 बजे से रात 11:00 बजे तक (IST)\n• स्थिति: $statusStr\n• दुकान बंद होने के दौरान ऑर्डर स्वीकार नहीं किए जाते।"
            else
                "Store Operating Hours:\n• Daily: 7:00 AM – 11:00 PM India Standard Time (IST)\n• Live Status: $statusStr\n• Orders are strictly blocked when closed to maintain fresh morning dispatch."
            messages.add(ChatMessage(sender = "AI", text = responseText))
            return
        }

        // 6. Order Tracking by Order ID
        if (lower.contains("sv-") || lower.contains("track") || lower.contains("स्थिति") || lower.contains("ट्रैक")) {
            val foundOrder = orders.find { ord ->
                lower.contains(ord.orderId.lowercase())
            }
            if (foundOrder != null) {
                val statusTitle = if (isHindi) foundOrder.orderStatus.titleHi else foundOrder.orderStatus.titleEn
                val responseText = if (isHindi)
                    "ऑर्डर विवरण (${foundOrder.orderId}):\n• स्थिति: $statusTitle\n• कुल राशि: ₹${foundOrder.total.toInt()}\n• भुगतान: ${foundOrder.paymentMethod.titleHi} (${foundOrder.paymentStatus.titleHi})\n• डिलीवरी पता: ${foundOrder.customerAddress}"
                else
                    "Order Details (${foundOrder.orderId}):\n• Status: $statusTitle\n• Grand Total: ₹${foundOrder.total.toInt()}\n• Payment: ${foundOrder.paymentMethod.titleEn} (${foundOrder.paymentStatus.titleEn})\n• Destination: ${foundOrder.customerAddress}"
                messages.add(ChatMessage(sender = "AI", text = responseText))
                return
            } else if (lower.contains("sv-")) {
                val responseText = if (isHindi)
                    "यह ऑर्डर आईडी डेटाबेस में नहीं मिला। कृपया 'Order Details' टैब में अपने दर्ज ऑर्डर जांचें।"
                else
                    "Order ID was not found in active records. Please check the 'Order Details' tab for your placed orders."
                messages.add(ChatMessage(sender = "AI", text = responseText))
                return
            }
        }

        // Default Grounded Answer
        val defaultAnswer = if (isHindi)
            "सैफ वेजिटेबल 39 प्रकार की शुद्ध देसी व विदेशी ताजी सब्जियां सीधे मुंबई के घरों में पहुंचाता है। न्यूनतम ऑर्डर ₹${settings.minOrderValue.toInt()} है और डिलीवरी चार्ज ₹${settings.deliveryCharge.toInt()} है। आप किसी विशिष्ट सब्जी (जैसे आलू, टमाटर, ब्रोकोली) की कीमत पूछ सकते हैं या मालिक से सीधे बात कर सकते हैं।"
        else
            "Saif Vegetable delivers 39 fresh farm-graded vegetables across Mumbai with a ₹${settings.minOrderValue.toInt()} minimum order and flat ₹${settings.deliveryCharge.toInt()} delivery fee. Ask about any specific vegetable (e.g. 'Broccoli price', 'Tomato stock'), store timings, or click to chat with Saif Hakim Rain."
        messages.add(ChatMessage(sender = "AI", text = defaultAnswer))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PrimaryGreen)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Bot",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHindi) "सैफ एआई सहायक" else "Saif AI Assistant",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isHindi) "सैफ वेजिटेबल डेटाबेस से सीधे जुड़ा" else "Live grounded to Saif Vegetable database",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 11.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Chat Messages List
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages) { msg ->
                        val isUser = msg.sender == "USER"
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                        ) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 14.dp,
                                    topEnd = 14.dp,
                                    bottomStart = if (isUser) 14.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 14.dp
                                ),
                                color = if (isUser) PrimaryGreen else LightGreenContainer,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = msg.text,
                                        color = if (isUser) Color.White else OnGreenContainer,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )

                                    if (msg.actionType == "WHATSAPP") {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Button(
                                                onClick = {
                                                    launchWhatsApp(context, settings.whatsapp, "Hello Saif Hakim Rain, I need assistance regarding Saif Vegetable.")
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("WhatsApp", fontSize = 11.sp)
                                            }
                                            OutlinedButton(
                                                onClick = { launchDialer(context, settings.phone) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Call", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Suggestion Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(quickQuestions) { question ->
                        Surface(
                            onClick = { handleSend(question) },
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor)
                        ) {
                            Text(
                                text = question,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = DividerColor)

                // Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputQuery,
                        onValueChange = { inputQuery = it },
                        placeholder = {
                            Text(
                                text = if (isHindi) "सब्जी की कीमत या प्रश्न पूछें..." else "Ask price, stock, or delivery...",
                                fontSize = 13.sp
                            )
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { handleSend(inputQuery) }),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ai_bot_input")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { handleSend(inputQuery) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen)
                            .testTag("ai_bot_send_button")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                    }
                }
            }
        }
    }
}
