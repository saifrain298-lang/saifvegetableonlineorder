package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.CustomerUser
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerAuthDialog(
    currentUser: CustomerUser,
    isHindi: Boolean,
    onDismiss: () -> Unit,
    onSaveProfile: (CustomerUser) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.name) }
    var phone by remember { mutableStateOf(currentUser.phone) }
    var address by remember { mutableStateOf(currentUser.address) }
    var pincode by remember { mutableStateOf(currentUser.pincode.ifEmpty { "400077" }) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(PrimaryGreen.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (isHindi) "सैफ वेजिटेबल में आपका स्वागत है" else "Welcome to Saif Vegetable",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (isHindi)
                        "ताजी सब्जियां ऑर्डर करने के लिए अपना विवरण दर्ज करें (मुंबई सेवा क्षेत्र)"
                    else
                        "Enter your details for fresh home delivery in Mumbai",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it; errorMessage = null },
                    label = { Text(if (isHindi) "पूरा नाम" else "Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_name_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = {
                        if (it.length <= 10 && it.all { ch -> ch.isDigit() }) {
                            phone = it
                            errorMessage = null
                        }
                    },
                    label = { Text(if (isHindi) "मोबाइल नंबर (10 अंक)" else "Mobile Number (10 digits)") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryGreen) },
                    prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_phone_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it; errorMessage = null },
                    label = { Text(if (isHindi) "डिलीवरी का पता (मुंबई)" else "Delivery Address (Mumbai)") },
                    leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = PrimaryGreen) },
                    placeholder = { Text(if (isHindi) "जैसे: फ्लैट 402, विद्याविहार..." else "e.g., Flat 402, Premier Exotica, Vidyavihar") },
                    maxLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_address_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pincode,
                    onValueChange = {
                        if (it.length <= 6 && it.all { ch -> ch.isDigit() }) {
                            pincode = it
                            errorMessage = null
                        }
                    },
                    label = { Text(if (isHindi) "मुंबई पिनकोड (400xxx)" else "Mumbai Pincode (400xxx)") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_pincode_input")
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (name.trim().isEmpty()) {
                            errorMessage = if (isHindi) "कृपया अपना नाम दर्ज करें" else "Please enter your full name"
                            return@Button
                        }
                        if (phone.trim().length != 10) {
                            errorMessage = if (isHindi) "कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें" else "Please enter a valid 10-digit mobile number"
                            return@Button
                        }
                        if (address.trim().isEmpty()) {
                            errorMessage = if (isHindi) "कृपया डिलीवरी का पता दर्ज करें" else "Please enter your delivery address"
                            return@Button
                        }
                        if (!pincode.startsWith("400") || pincode.length != 6) {
                            errorMessage = if (isHindi) "सेवा केवल मुंबई (400xxx) के लिए उपलब्ध है" else "Service is only available in Mumbai (Pincode starting with 400xxx)"
                            return@Button
                        }

                        // Save customer profile directly without OTP
                        onSaveProfile(
                            CustomerUser(
                                id = "cust_${phone.trim()}",
                                name = name.trim(),
                                phone = phone.trim(),
                                address = address.trim(),
                                pincode = pincode.trim(),
                                isLoggedIn = true
                            )
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("auth_submit_button")
                ) {
                    Text(
                        text = if (isHindi) "विवरण सहेजें और जारी रखें" else "Save Details & Continue",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("auth_browse_guest_button")
                ) {
                    Text(
                        text = if (isHindi) "अतिथि के रूप में ब्राउज़ करें" else "Browse as Guest",
                        fontSize = 13.sp,
                        color = SecondaryGreen
                    )
                }
            }
        }
    }
}
