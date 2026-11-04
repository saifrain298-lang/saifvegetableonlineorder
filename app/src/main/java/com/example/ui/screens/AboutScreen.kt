package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun AboutScreen(
    repository: SaifVegetableRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by repository.businessSettings.collectAsState()
    val isHindi by repository.isHindi.collectAsState()
    val isStoreOpen = repository.isStoreOpen()

    val websiteUrl = "https://ais-dev-y2nw5fuzaehiyzh3k2sxfw-712942562588.asia-east1.run.app"
    val apkDestination = "https://ais-dev-y2nw5fuzaehiyzh3k2sxfw-712942562588.asia-east1.run.app/saif-vegetable.apk"
    val upiPaymentUri = "upi://pay?pa=${settings.upiId}&pn=Saif%20Vegetable&mc=5411&cu=INR"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Page Title & Store Status
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isHindi) "हमारे बारे में" else "About Saif Vegetable",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (isHindi) "सत्यापित व्यावसायिक जानकारी" else "Verified Business Profile & Delivery Info",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                StoreStatusChip(isOpen = isStoreOpen, isHindi = isHindi)
            }
        }

        // Verified Business Details Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(PrimaryGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = PrimaryGreen)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = settings.businessName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Proprietor: ${settings.ownerName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryDarkGreen
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = DividerColor)

                    BusinessDetailRow(
                        icon = Icons.Default.Email,
                        label = "Email",
                        value = settings.email,
                        onClick = {
                            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${settings.email}"))
                            context.startActivity(intent)
                        }
                    )

                    BusinessDetailRow(
                        icon = Icons.Default.Place,
                        label = "Address",
                        value = settings.address,
                        onClick = { launchMaps(context, settings.address) }
                    )

                    BusinessDetailRow(
                        icon = Icons.Default.LocalShipping,
                        label = "Service Area",
                        value = settings.serviceArea,
                        subtext = "Express home delivery across Mumbai"
                    )

                    BusinessDetailRow(
                        icon = Icons.Default.AccessTime,
                        label = "Business Hours",
                        value = "7:00 AM – 11:00 PM IST (Daily)",
                        subtext = "Orders blocked automatically outside operational window"
                    )

                    BusinessDetailRow(
                        icon = Icons.Default.CurrencyRupee,
                        label = "Pricing & Fees",
                        value = "Min Order: ₹${settings.minOrderValue.toInt()} • Delivery: ₹${settings.deliveryCharge.toInt()} flat"
                    )

                    BusinessDetailRow(
                        icon = Icons.Default.QrCodeScanner,
                        label = "UPI Payment ID",
                        value = settings.upiId,
                        badge = if (settings.isUpiVerified) "Verified" else "Verification Required in Dashboard"
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row (Call, WhatsApp, Maps)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { launchDialer(context, settings.phone) },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("about_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                launchWhatsApp(
                                    context,
                                    settings.whatsapp,
                                    "Hello Saif Hakim Rain, I would like to inquire about ordering vegetables from Saif Vegetable."
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(42.dp)
                                .testTag("about_whatsapp_button")
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { launchMaps(context, settings.address) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("about_maps_button")
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Maps", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Section 11: Three Verified QR Codes
        item {
            Text(
                text = if (isHindi) "सत्यापित क्यूआर कोड व डिजिटल लिंक" else "Official QR Codes & Platform Access",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var selectedQrTab by remember { mutableStateOf(0) }
                    TabRow(
                        selectedTabIndex = selectedQrTab,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        contentColor = PrimaryGreen,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedQrTab == 0,
                            onClick = { selectedQrTab = 0 },
                            text = { Text("1. Website", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedQrTab == 1,
                            onClick = { selectedQrTab = 1 },
                            text = { Text("2. Android APK", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedQrTab == 2,
                            onClick = { selectedQrTab = 2 },
                            text = { Text("3. UPI Payment", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedQrTab) {
                        0 -> {
                            DeterministicQrCode(data = websiteUrl, sizeDp = 180.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Customer Website Live URL",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = websiteUrl,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Live Site", fontSize = 12.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Order 100% fresh vegetables daily at Mumbai wholesale rates from Saif Vegetable! Visit our official website: $websiteUrl or call/WhatsApp +91 8452953630."
                                            )
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Saif Vegetable Domain"))
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Share Domain", fontSize = 12.sp)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Short & Unique Brand Domains:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("saifveg.in", "saifvegetable.store", "saifvegetable.ai").forEach { dom ->
                                    SuggestionChip(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "Visit Saif Vegetable Online: https://$dom (Proprietor: Saif Hakim Rain, +91 84529 53630)"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share $dom"))
                                        },
                                        label = { Text(dom, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) }
                                    )
                                }
                            }
                        }
                        1 -> {
                            DeterministicQrCode(data = apkDestination, sizeDp = 180.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Official Android Build & APK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Package: com.aistudio.saifvegetable.kzvrqd",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Surface(
                                color = LightGreenContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 6.dp)
                            ) {
                                Text(
                                    text = "Ready to Install & Build on Android",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OnGreenContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            TextButton(onClick = {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(apkDestination)))
                            }) {
                                Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Download APK Directly (23.8 MB)")
                            }
                        }
                        2 -> {
                            DeterministicQrCode(data = upiPaymentUri, sizeDp = 180.dp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "UPI Direct Pay QR",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "UPI ID: ${settings.upiId}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryDarkGreen
                            )
                            if (!settings.isUpiVerified) {
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.padding(top = 6.dp)
                                ) {
                                    Text(
                                        text = "Owner Verification Required in Dashboard",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = AccentOrange,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // FAQs Section
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isHindi) "अक्सर पूछे जाने वाले प्रश्न (FAQ)" else "Frequently Asked Questions",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    FaqItem(
                        question = if (isHindi) "सब्जियां कहां से प्राप्त की जाती हैं?" else "Where are the vegetables sourced from?",
                        answer = if (isHindi)
                            "सैफ वेजिटेबल रोजाना सुबह वाशी एपीएमसी मंडी से सीधे चुनी हुई ए-ग्रेड ताजी सब्जियां लाता है।"
                        else
                            "Saif Vegetable procures top-grade fresh produce daily at 5:00 AM directly from Vashi APMC Mandi, hand-sorted for quality."
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DividerColor)
                    FaqItem(
                        question = if (isHindi) "डिलीवरी का समय क्या है?" else "What is the delivery timeline?",
                        answer = if (isHindi)
                            "सुबह 7:00 से 11:00 बजे तक रखे गए ऑर्डर उसी दिन दोपहर तक, और शाम के ऑर्डर अगली सुबह डिलीवर किए जाते हैं।"
                        else
                            "Orders placed before 11:00 AM are delivered same-day afternoon; evening orders arrive first thing next morning."
                    )
                }
            }
        }
    }
}

@Composable
fun BusinessDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    subtext: String? = null,
    badge: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtext != null) {
                Text(
                    text = subtext,
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 1.dp)
                )
            }
            if (badge != null) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = badge,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        if (onClick != null) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun FaqItem(question: String, answer: String) {
    Column {
        Text(text = question, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(text = answer, fontSize = 12.sp, color = TextSecondary, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
