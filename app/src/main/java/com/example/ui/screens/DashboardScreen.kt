package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaifVegetableRepository
import com.example.model.*
import com.example.ui.components.launchWhatsApp
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: SaifVegetableRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings by repository.businessSettings.collectAsState()
    val isHindi by repository.isHindi.collectAsState()
    val vegetables by repository.vegetables.collectAsState()
    val orders by repository.orders.collectAsState()
    val stockMovements by repository.stockMovements.collectAsState()
    val expenses by repository.expenses.collectAsState()

    var isOwnerAuthenticated by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    // Dashboard Sub-Tabs
    var activeTab by remember { mutableStateOf(0) } // 0: Overview & P&L, 1: 39 Veggies Stock & Prices, 2: Orders, 3: Expenses & Audit, 4: Settings

    // Edit Product Dialog State
    var editTargetVeg by remember { mutableStateOf<VegetableItem?>(null) }
    var editPriceStr by remember { mutableStateOf("") }
    var editCostStr by remember { mutableStateOf("") }
    var editDiscountStr by remember { mutableStateOf("") }

    // Replenish Stock Dialog State
    var replenishTargetVeg by remember { mutableStateOf<VegetableItem?>(null) }
    var replenishAmountStr by remember { mutableStateOf("50") }
    var isSetExactStockMode by remember { mutableStateOf(false) }

    // Add Expense Dialog State
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var newExpenseTitle by remember { mutableStateOf("") }
    var newExpenseAmount by remember { mutableStateOf("") }
    var newExpenseCategory by remember { mutableStateOf("Transport") }

    if (!isOwnerAuthenticated) {
        // Owner Passcode Lock Screen
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(PrimaryGreen.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Saif Vegetable Owner Dashboard",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Authorized access for Saif Hakim Rain",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp, bottom = 18.dp)
                    )

                    OutlinedTextField(
                        value = enteredPin,
                        onValueChange = {
                            enteredPin = it
                            pinError = false
                        },
                        label = { Text("Enter Owner PIN (Default: 1234)") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("owner_pin_input")
                    )

                    if (pinError) {
                        Text(
                            text = "Incorrect PIN. Default is 1234.",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (enteredPin == settings.ownerPasscode) {
                                isOwnerAuthenticated = true
                                pinError = false
                            } else {
                                pinError = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("owner_login_button")
                    ) {
                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Unlock Dashboard", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val pnl = repository.calculatePAndL()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Commercial Executive Dashboard",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Saif Hakim Rain • Vidyavihar Store",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    Row {
                        IconButton(onClick = { isOwnerAuthenticated = false }) {
                            Icon(Icons.Default.Logout, contentDescription = "Lock", tint = TextMuted)
                        }
                    }
                }

                ScrollableTabRow(
                    selectedTabIndex = activeTab,
                    edgePadding = 16.dp,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = PrimaryGreen
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = { Text("P&L Overview", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = { Text("39 Veggies (${vegetables.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 2,
                        onClick = { activeTab = 2 },
                        text = { Text("Orders (${orders.size})", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 3,
                        onClick = { activeTab = 3 },
                        text = { Text("Expenses & Audit", fontSize = 12.sp) }
                    )
                    Tab(
                        selected = activeTab == 4,
                        onClick = { activeTab = 4 },
                        text = { Text("Store Settings", fontSize = 12.sp) }
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
            when (activeTab) {
                0 -> OverviewPnLTab(
                    pnl = pnl,
                    onShareWhatsApp = {
                        val report = "Saif Vegetable Financial Report:\n• Gross Sales: ₹${pnl.grossProductSales.toInt()}\n• COGS: ₹${pnl.costOfGoodsSold.toInt()}\n• Gross Profit: ₹${pnl.grossProfit.toInt()}\n• Delivery Collected: ₹${pnl.deliveryChargesCollected.toInt()}\n• Operating Expenses: ₹${pnl.totalOperatingExpenses.toInt()}\n• Net Operating Profit: ₹${pnl.netOperatingProfit.toInt()}\n• Total Orders: ${pnl.totalOrders} (Delivered: ${pnl.completedOrders})"
                        launchWhatsApp(context, settings.whatsapp, report)
                    },
                    onExportCsv = {
                        val csv = "Metric,Value\nGross Sales,${pnl.grossProductSales}\nCOGS,${pnl.costOfGoodsSold}\nGross Profit,${pnl.grossProfit}\nDelivery Charges,${pnl.deliveryChargesCollected}\nExpenses,${pnl.totalOperatingExpenses}\nNet Operating Profit,${pnl.netOperatingProfit}"
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, csv)
                            type = "text/csv"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export P&L CSV"))
                    }
                )

                1 -> InventoryTab(
                    vegetables = vegetables,
                    onEditVeg = { veg ->
                        editTargetVeg = veg
                        editPriceStr = veg.pricePerKg.toInt().toString()
                        editCostStr = veg.costPerKg.toInt().toString()
                        editDiscountStr = veg.discountPercent.toString()
                    },
                    onReplenishVeg = { veg ->
                        replenishTargetVeg = veg
                        replenishAmountStr = "50"
                    }
                )

                2 -> OrdersManagementTab(
                    orders = orders,
                    repository = repository
                )

                3 -> ExpensesAndAuditTab(
                    expenses = expenses,
                    movements = stockMovements,
                    onAddExpenseClick = { showAddExpenseDialog = true }
                )

                4 -> StoreSettingsTab(
                    settings = settings,
                    onUpdateSettings = { repository.updateBusinessSettings(it) }
                )
            }
        }
    }

    // Edit Product Price/Cost Dialog
    if (editTargetVeg != null) {
        val veg = editTargetVeg!!
        AlertDialog(
            onDismissRequest = { editTargetVeg = null },
            title = { Text("Edit ${veg.nameEn} Pricing", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Historical orders keep immutable cost snapshots. Only future sales use these values.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    OutlinedTextField(
                        value = editPriceStr,
                        onValueChange = { editPriceStr = it },
                        label = { Text("Selling Price / kg (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editCostStr,
                        onValueChange = { editCostStr = it },
                        label = { Text("Wholesale Purchase Cost / kg (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editDiscountStr,
                        onValueChange = { editDiscountStr = it },
                        label = { Text("Discount % (0-50)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = editPriceStr.toDoubleOrNull() ?: veg.pricePerKg
                        val c = editCostStr.toDoubleOrNull() ?: veg.costPerKg
                        val d = editDiscountStr.toIntOrNull() ?: veg.discountPercent
                        repository.updateProductPrice(veg.id, p, c, d)
                        editTargetVeg = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editTargetVeg = null }) { Text("Cancel") }
            }
        )
    }

    // Manual Stock Quantity Management Dialog
    if (replenishTargetVeg != null) {
        val veg = replenishTargetVeg!!
        AlertDialog(
            onDismissRequest = { replenishTargetVeg = null },
            title = {
                Column {
                    Text(
                        text = "Stock Management: ${veg.nameEn}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                    Text(
                        text = "Current Available: ${String.format("%.1f", veg.stockKg)} kg",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Mode Toggle: Add Manually vs Set Exact
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSetExactStockMode = false },
                            color = if (!isSetExactStockMode) PrimaryGreen else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "➕ Add (+kg)",
                                color = if (!isSetExactStockMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { isSetExactStockMode = true },
                            color = if (isSetExactStockMode) SecondaryGreen else Color.Transparent,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "🎯 Set Exact (kg)",
                                color = if (isSetExactStockMode) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }

                    OutlinedTextField(
                        value = replenishAmountStr,
                        onValueChange = { replenishAmountStr = it },
                        label = {
                            Text(if (isSetExactStockMode) "Set Exact Stock (kg)" else "Enter Quantity to Add (kg)")
                        },
                        placeholder = { Text("e.g. 25.0") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Quick Presets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(5, 10, 25, 50, 100).forEach { qty ->
                            SuggestionChip(
                                onClick = {
                                    replenishAmountStr = qty.toString()
                                    if (isSetExactStockMode) isSetExactStockMode = false
                                },
                                label = { Text("+$qty", fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isSetExactStockMode = true
                            replenishAmountStr = "0"
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Mark Out of Stock (0 kg)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inputKg = replenishAmountStr.toDoubleOrNull() ?: 0.0
                        if (isSetExactStockMode) {
                            repository.setExactStock(veg.id, inputKg)
                        } else {
                            if (inputKg > 0.0) {
                                repository.replenishStock(veg.id, inputKg)
                            }
                        }
                        replenishTargetVeg = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text(if (isSetExactStockMode) "Save Exact Stock" else "Add Stock Manually")
                }
            },
            dismissButton = {
                TextButton(onClick = { replenishTargetVeg = null }) { Text("Cancel") }
            }
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showAddExpenseDialog = false },
            title = { Text("Record Operating Expense", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newExpenseTitle,
                        onValueChange = { newExpenseTitle = it },
                        label = { Text("Expense Description") },
                        placeholder = { Text("e.g. Mandi diesel transport, carry bags") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newExpenseAmount,
                        onValueChange = { newExpenseAmount = it },
                        label = { Text("Amount in ₹") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newExpenseCategory,
                        onValueChange = { newExpenseCategory = it },
                        label = { Text("Category (Transport, Packaging, Labor)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = newExpenseAmount.toDoubleOrNull() ?: 0.0
                        if (newExpenseTitle.isNotEmpty() && amt > 0.0) {
                            repository.addExpense(newExpenseTitle, newExpenseCategory, amt)
                        }
                        showAddExpenseDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Record Expense")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddExpenseDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun OverviewPnLTab(
    pnl: SaifVegetableRepository.ProfitAndLossReport,
    onShareWhatsApp: () -> Unit,
    onExportCsv: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High Level Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Net Operating Profit",
                    value = "₹${pnl.netOperatingProfit.toInt()}",
                    color = PrimaryGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Gross Profit",
                    value = "₹${pnl.grossProfit.toInt()}",
                    color = SecondaryGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard(
                    title = "Net Product Sales",
                    value = "₹${pnl.netProductSales.toInt()}",
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Cost of Goods (COGS)",
                    value = "₹${pnl.costOfGoodsSold.toInt()}",
                    color = AccentOrange,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Complete Accounting Reconciliation Ledger
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Accounting-Grade Profit & Loss Statement",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Calculated from immutable historical order item snapshots",
                        fontSize = 11.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    PnlRow("Gross Product Sales (Qty × Price)", "₹${pnl.grossProductSales.toInt()}")
                    PnlRow("Discounts Given", "- ₹${pnl.discountsGiven.toInt()}")
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                    PnlRow("Net Product Sales", "₹${pnl.netProductSales.toInt()}", isBold = true)
                    PnlRow("Cost of Goods Sold (COGS)", "- ₹${pnl.costOfGoodsSold.toInt()}", color = StatusRed)
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 4.dp))
                    PnlRow("Gross Profit", "₹${pnl.grossProfit.toInt()}", isBold = true, color = PrimaryGreen)
                    PnlRow("+ Delivery Fees Collected", "+ ₹${pnl.deliveryChargesCollected.toInt()}")
                    PnlRow("- Recorded Operating Expenses", "- ₹${pnl.totalOperatingExpenses.toInt()}", color = StatusRed)
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 6.dp))
                    PnlRow("Final Net Business Profit", "₹${pnl.netOperatingProfit.toInt()}", isBold = true, isHighlighted = true)
                }
            }
        }

        // Order Counts Breakdown
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "Order Fulfilment Metrics", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CountBadge("Total", pnl.totalOrders, MaterialTheme.colorScheme.onSurface)
                        CountBadge("Completed", pnl.completedOrders, StatusGreen)
                        CountBadge("Pending", pnl.pendingOrders, StatusOrange)
                        CountBadge("Cancelled", pnl.cancelledOrders, StatusRed)
                    }
                }
            }
        }

        // Share & Export Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onShareWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp Report", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onExportCsv,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Export CSV", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, fontSize = 11.sp, color = TextSecondary)
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun PnlRow(label: String, value: String, isBold: Boolean = false, isHighlighted: Boolean = false, color: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = if (isHighlighted) 13.sp else 12.sp,
            fontWeight = if (isBold || isHighlighted) FontWeight.Bold else FontWeight.Normal,
            color = if (isHighlighted) PrimaryDarkGreen else TextSecondary
        )
        Text(
            text = value,
            fontSize = if (isHighlighted) 15.sp else 12.sp,
            fontWeight = if (isBold || isHighlighted) FontWeight.Bold else FontWeight.SemiBold,
            color = color ?: (if (isHighlighted) PrimaryGreen else MaterialTheme.colorScheme.onSurface)
        )
    }
}

@Composable
fun CountBadge(title: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count.toString(), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(text = title, fontSize = 11.sp, color = TextMuted)
    }
}

@Composable
fun InventoryTab(
    vegetables: List<VegetableItem>,
    onEditVeg: (VegetableItem) -> Unit,
    onReplenishVeg: (VegetableItem) -> Unit
) {
    var invSearch by remember { mutableStateOf("") }
    var filterLowStockOnly by remember { mutableStateOf(false) }

    val filtered = remember(vegetables, invSearch, filterLowStockOnly) {
        vegetables.filter { veg ->
            val matchSearch = invSearch.isEmpty() ||
                    veg.nameEn.contains(invSearch, ignoreCase = true) ||
                    veg.nameHi.contains(invSearch, ignoreCase = true)
            val matchLow = !filterLowStockOnly || veg.stockKg <= veg.lowStockThreshold
            matchSearch && matchLow
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = invSearch,
                onValueChange = { invSearch = it },
                placeholder = { Text("Filter 39 vegetables...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            FilterChip(
                selected = filterLowStockOnly,
                onClick = { filterLowStockOnly = !filterLowStockOnly },
                label = { Text("Low Stock", fontSize = 11.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Total Initial Stock: 7,800 kg (200 kg × 39) • Currently Available: ${String.format("%.1f", vegetables.sumOf { it.stockKg })} kg",
            fontSize = 11.sp,
            color = TextSecondary,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { veg ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${veg.nameEn} • ${veg.nameHi}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Sell: ₹${veg.pricePerKg.toInt()}/kg • Cost: ₹${veg.costPerKg.toInt()}/kg",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "Stock: ${String.format("%.1f", veg.stockKg)} kg",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (veg.stockKg <= veg.lowStockThreshold) StatusRed else StatusGreen
                                )
                                if (veg.stockKg <= veg.lowStockThreshold) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("(Low)", fontSize = 10.sp, color = StatusRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { onReplenishVeg(veg) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(LightGreenContainer)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Add Stock", tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                            }

                            IconButton(
                                onClick = { onEditVeg(veg) },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Price", tint = TextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrdersManagementTab(orders: List<Order>, repository: SaifVegetableRepository) {
    if (orders.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No customer orders recorded yet.", color = TextMuted)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(orders, key = { it.orderId }) { order ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(order.orderId, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = order.orderStatus.titleEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = when (order.orderStatus) {
                                OrderStatus.DELIVERED -> StatusGreen
                                OrderStatus.CANCELLED -> StatusRed
                                else -> StatusOrange
                            }
                        )
                    }

                    Text(
                        text = "Customer: ${order.customerName} • ${order.customerPhone}",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = "Address: ${order.customerAddress} (${order.mumbaiPincode})",
                        fontSize = 11.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Total: ₹${order.total.toInt()} (${order.paymentMethod.titleEn})",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = PrimaryGreen
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Owner Fulfillment Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (order.orderStatus == OrderStatus.PLACED) {
                            Button(
                                onClick = { repository.updateOrderStatus(order.orderId, OrderStatus.PREPARING) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Pack Order", fontSize = 11.sp)
                            }
                        }
                        if (order.orderStatus == OrderStatus.PREPARING) {
                            Button(
                                onClick = { repository.updateOrderStatus(order.orderId, OrderStatus.DISPATCHED) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Dispatch", fontSize = 11.sp)
                            }
                        }
                        if (order.orderStatus == OrderStatus.DISPATCHED) {
                            Button(
                                onClick = { repository.updateOrderStatus(order.orderId, OrderStatus.DELIVERED) },
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Mark Delivered", fontSize = 11.sp)
                            }
                        }

                        // Payment verification toggle
                        OutlinedButton(
                            onClick = {
                                val newStatus = order.paymentStatus != PaymentStatus.VERIFIED
                                repository.verifyOrderPayment(order.orderId, newStatus)
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier.height(30.dp)
                        ) {
                            Text(
                                text = if (order.paymentStatus == PaymentStatus.VERIFIED) "Paid (Verified)" else "Verify UPI",
                                fontSize = 11.sp,
                                color = if (order.paymentStatus == PaymentStatus.VERIFIED) StatusGreen else AccentOrange
                            )
                        }

                        // Cancel and restore inventory
                        if (order.orderStatus != OrderStatus.CANCELLED && order.orderStatus != OrderStatus.DELIVERED) {
                            OutlinedButton(
                                onClick = { repository.cancelOrder(order.orderId, "Owner cancelled") },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusRed),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Cancel", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExpensesAndAuditTab(
    expenses: List<ExpenseRecord>,
    movements: List<StockMovement>,
    onAddExpenseClick: () -> Unit
) {
    var subTab by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = subTab == 0,
                    onClick = { subTab = 0 },
                    label = { Text("Expenses (${expenses.size})") }
                )
                FilterChip(
                    selected = subTab == 1,
                    onClick = { subTab = 1 },
                    label = { Text("Stock Ledger (${movements.size})") }
                )
            }

            if (subTab == 0) {
                Button(
                    onClick = onAddExpenseClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (subTab == 0) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expenses, key = { it.id }) { exp ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(exp.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Category: ${exp.category}", fontSize = 11.sp, color = TextSecondary)
                            }
                            Text("₹${exp.amount.toInt()}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusRed)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(movements, key = { it.id }) { mov ->
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${mov.vegetableName} (${mov.reason})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Actor: ${mov.actor}${if (mov.orderId != null) " • ${mov.orderId}" else ""}",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                            Text(
                                text = "${if (mov.quantityChangeKg > 0) "+" else ""}${mov.quantityChangeKg} kg",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (mov.quantityChangeKg > 0) StatusGreen else StatusRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StoreSettingsTab(
    settings: BusinessSettings,
    onUpdateSettings: (BusinessSettings) -> Unit
) {
    var minOrderStr by remember { mutableStateOf(settings.minOrderValue.toInt().toString()) }
    var deliveryFeeStr by remember { mutableStateOf(settings.deliveryCharge.toInt().toString()) }
    var upiStr by remember { mutableStateOf(settings.upiId) }
    var isUpiVerified by remember { mutableStateOf(settings.isUpiVerified) }
    var isStoreOpenManual by remember { mutableStateOf(settings.isStoreOpenManual) }
    var passcodeStr by remember { mutableStateOf(settings.ownerPasscode) }
    var saveSuccess by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Store Operations & Master Switch", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Manual Store Open Switch", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Immediately toggle online ordering on/off", fontSize = 11.sp, color = TextMuted)
                        }
                        Switch(
                            checked = isStoreOpenManual,
                            onCheckedChange = { isStoreOpenManual = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = minOrderStr,
                        onValueChange = { minOrderStr = it },
                        label = { Text("Minimum Order Value (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = deliveryFeeStr,
                        onValueChange = { deliveryFeeStr = it },
                        label = { Text("Delivery Charge (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = upiStr,
                        onValueChange = { upiStr = it },
                        label = { Text("UPI ID (ends in @ptsbi)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = passcodeStr,
                        onValueChange = { passcodeStr = it },
                        label = { Text("Owner Dashboard Passcode / PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("UPI Destination Verification", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("Mark UPI payment destination verified", fontSize = 11.sp, color = TextMuted)
                        }
                        Checkbox(
                            checked = isUpiVerified,
                            onCheckedChange = { isUpiVerified = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val minVal = minOrderStr.toDoubleOrNull() ?: settings.minOrderValue
                            val feeVal = deliveryFeeStr.toDoubleOrNull() ?: settings.deliveryCharge
                            onUpdateSettings(
                                settings.copy(
                                    minOrderValue = minVal,
                                    deliveryCharge = feeVal,
                                    upiId = upiStr.trim(),
                                    isUpiVerified = isUpiVerified,
                                    isStoreOpenManual = isStoreOpenManual,
                                    ownerPasscode = passcodeStr.trim().ifEmpty { settings.ownerPasscode }
                                )
                            )
                            saveSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Business Settings")
                    }

                    if (saveSuccess) {
                        Text(
                            text = "Settings updated successfully!",
                            color = StatusGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}
