package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaifVegetableRepository
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryGreen
import com.example.ui.theme.SecondaryGreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = SaifVegetableRepository.getInstance()

        setContent {
            MyApplicationTheme {
                SaifVegetableApp(repository = repository)
            }
        }
    }
}

@Composable
fun SaifVegetableApp(repository: SaifVegetableRepository) {
    var currentScreen by remember { mutableStateOf(0) } // 0: Home, 1: About, 2: Dashboard, 3: Orders
    val customer by repository.customerUser.collectAsState()
    val isHindi by repository.isHindi.collectAsState()

    var showAuthDialog by remember { mutableStateOf(!customer.isLoggedIn) }
    var showCartSheet by remember { mutableStateOf(false) }
    var showAiBotDialog by remember { mutableStateOf(false) }

    // Back handling: pop to Home screen if on secondary tabs
    BackHandler(enabled = currentScreen != 0) {
        currentScreen = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                windowInsets = WindowInsets.navigationBars,
                modifier = Modifier.testTag("main_bottom_nav")
            ) {
                NavigationBarItem(
                    selected = currentScreen == 0,
                    onClick = { currentScreen = 0 },
                    icon = {
                        Icon(
                            if (currentScreen == 0) Icons.Filled.Storefront else Icons.Outlined.Storefront,
                            contentDescription = "Home"
                        )
                    },
                    label = {
                        Text(
                            text = if (isHindi) "होम" else "Home",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == 0) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_home_tab")
                )

                NavigationBarItem(
                    selected = currentScreen == 1,
                    onClick = { currentScreen = 1 },
                    icon = {
                        Icon(
                            if (currentScreen == 1) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = "About"
                        )
                    },
                    label = {
                        Text(
                            text = if (isHindi) "परिचय" else "About",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == 1) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_about_tab")
                )

                NavigationBarItem(
                    selected = currentScreen == 2,
                    onClick = { currentScreen = 2 },
                    icon = {
                        Icon(
                            if (currentScreen == 2) Icons.Filled.AdminPanelSettings else Icons.Outlined.AdminPanelSettings,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = {
                        Text(
                            text = if (isHindi) "डैशबोर्ड" else "Dashboard",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == 2) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_dashboard_tab")
                )

                NavigationBarItem(
                    selected = currentScreen == 3,
                    onClick = {
                        if (!customer.isLoggedIn) {
                            showAuthDialog = true
                        }
                        currentScreen = 3
                    },
                    icon = {
                        Icon(
                            if (currentScreen == 3) Icons.Filled.ReceiptLong else Icons.Outlined.ReceiptLong,
                            contentDescription = "Order Details"
                        )
                    },
                    label = {
                        Text(
                            text = if (isHindi) "ऑर्डर" else "Orders",
                            fontSize = 11.sp,
                            fontWeight = if (currentScreen == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryGreen,
                        selectedTextColor = PrimaryGreen,
                        indicatorColor = PrimaryGreen.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("nav_orders_tab")
                )
            }
        },
        floatingActionButton = {
            // Floating Safe AI Assistant Button
            FloatingActionButton(
                onClick = { showAiBotDialog = true },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .offset(y = (-4).dp)
                    .testTag("floating_ai_bot_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.SmartToy, contentDescription = "Saif AI Bot", modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isHindi) "सैफ एआई" else "Saif AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                0 -> HomeScreen(
                    repository = repository,
                    onOpenCart = { showCartSheet = true },
                    onOpenAiSupport = { showAiBotDialog = true }
                )
                1 -> AboutScreen(repository = repository)
                2 -> DashboardScreen(repository = repository)
                3 -> OrderDetailsScreen(
                    repository = repository,
                    onNavigateToShop = { currentScreen = 0 },
                    onRequireLogin = { showAuthDialog = true }
                )
            }
        }
    }

    // First visit / Checkout Auth Dialog
    if (showAuthDialog) {
        CustomerAuthDialog(
            currentUser = customer,
            isHindi = isHindi,
            onDismiss = { showAuthDialog = false },
            onSaveProfile = { user ->
                repository.updateCustomerProfile(user)
                showAuthDialog = false
            }
        )
    }

    // Persistent Cart Bottom Sheet
    if (showCartSheet) {
        CartBottomSheet(
            repository = repository,
            onDismiss = { showCartSheet = false },
            onOrderPlaced = {
                currentScreen = 3 // Switch to Order Details tab
            },
            onRequireLogin = {
                showAuthDialog = true
            }
        )
    }

    // Safe AI Support Dialog
    if (showAiBotDialog) {
        SafeAiBotDialog(
            repository = repository,
            onDismiss = { showAiBotDialog = false }
        )
    }
}
