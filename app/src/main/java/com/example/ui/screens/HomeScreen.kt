package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SaifVegetableRepository
import com.example.model.VegetableCategory
import com.example.model.VegetableItem
import com.example.ui.components.LanguageToggleChip
import com.example.ui.components.StoreStatusChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    repository: SaifVegetableRepository,
    onOpenCart: () -> Unit,
    onOpenAiSupport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vegetables by repository.vegetables.collectAsState()
    val cart by repository.cart.collectAsState()
    val isHindi by repository.isHindi.collectAsState()
    val isStoreOpen = repository.isStoreOpen()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(VegetableCategory.ALL) }

    val categories = VegetableCategory.values()

    val filteredVegetables = remember(vegetables, searchQuery, selectedCategory) {
        vegetables.filter { veg ->
            val matchCat = selectedCategory == VegetableCategory.ALL || veg.category == selectedCategory
            val matchQuery = searchQuery.trim().isEmpty() ||
                    veg.nameEn.contains(searchQuery.trim(), ignoreCase = true) ||
                    veg.nameHi.contains(searchQuery.trim(), ignoreCase = true)
            matchCat && matchQuery
        }
    }

    val totalCartItems = remember(cart) { cart.values.count { it > 0.0 } }
    val totalCartWeightKg = remember(cart) { cart.values.sum() }
    val cartSubtotal = remember(cart, vegetables) {
        val vegMap = vegetables.associateBy { it.id }
        cart.entries.sumOf { (id, qty) ->
            (vegMap[id]?.effectivePricePerKg ?: 0.0) * qty
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(visible = totalCartItems > 0) {
                Surface(
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenCart() }
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$totalCartItems ${if (isHindi) "सब्जियां" else "Items"} • ${String.format("%.1f", totalCartWeightKg)} kg",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Subtotal: ₹${cartSubtotal.toInt()}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }

                        Button(
                            onClick = onOpenCart,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("home_view_cart_button")
                        ) {
                            Text(
                                text = if (isHindi) "टोकरी देखें" else "View Cart",
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = PrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Vidyavihar, Mumbai",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "Saif Vegetable • Fresh APMC Direct",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    LanguageToggleChip(
                        isHindi = isHindi,
                        onToggle = { repository.toggleLanguage() }
                    )
                }
            }

            // Store Status & Service Area Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StoreStatusChip(isOpen = isStoreOpen, isHindi = isHindi)

                Surface(
                    color = LightGreenContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isHindi) "मुंबई विशेष • न्यूनतम ₹100" else "Mumbai Only • Min ₹100",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OnGreenContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isHindi) "39 सब्जियों में खोजें (उदा. टमाटर, ब्रोकोली)..." else "Search 39 fresh vegetables (e.g. Tomato, Spinach)...",
                        fontSize = 13.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = PrimaryGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("product_search_input")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Pills
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = cat == selectedCategory
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = if (isHindi) cat.titleHi else cat.titleEn,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("category_chip_${cat.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Product Cards Grid
            if (filteredVegetables.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isHindi) "कोई सब्जी नहीं मिली" else "No vegetables match your search",
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("vegetables_grid"),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredVegetables, key = { it.id }) { veg ->
                        val currentQty = cart[veg.id] ?: 0.0
                        VegetableProductCard(
                            vegetable = veg,
                            currentQty = currentQty,
                            isHindi = isHindi,
                            onAddQty = { delta -> repository.addToCart(veg.id, delta) },
                            onSetQty = { qty -> repository.updateCartQuantity(veg.id, qty) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VegetableProductCard(
    vegetable: VegetableItem,
    currentQty: Double,
    isHindi: Boolean,
    onAddQty: (Double) -> Unit,
    onSetQty: (Double) -> Unit
) {
    var selectedWeightKg by remember { mutableStateOf(1.0) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DividerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vegetable_card_${vegetable.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Vegetable Image on Clean White Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = vegetable.drawableResId),
                    contentDescription = vegetable.nameEn,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                )

                // Organic Badge
                Surface(
                    color = Color(0xFFE8F5E9).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "100% Farm Fresh",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vegetable Title (English & Hindi)
            Text(
                text = if (isHindi) "${vegetable.nameHi} (${vegetable.nameEn})" else "${vegetable.nameEn} • ${vegetable.nameHi}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Price and Unit
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = "₹${vegetable.effectivePricePerKg.toInt()}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = PrimaryGreen
                )
                Text(
                    text = " / kg",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Weight selector chips (0.5 kg, 1 kg, 2 kg)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(0.5, 1.0, 2.0).forEach { wt ->
                    val isSelected = selectedWeightKg == wt
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) LightGreenContainer else MaterialTheme.colorScheme.surfaceVariant)
                            .border(
                                1.dp,
                                if (isSelected) PrimaryGreen else Color.Transparent,
                                RoundedCornerShape(6.dp)
                            )
                            .clickable { selectedWeightKg = wt }
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (wt < 1.0) "500g" else "${wt.toInt()}kg",
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) PrimaryDarkGreen else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Add to Cart or Stepper
            if (currentQty <= 0.0) {
                Button(
                    onClick = { onAddQty(selectedWeightKg) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("add_button_${vegetable.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isHindi) "जोड़ें" else "Add",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryGreen.copy(alpha = 0.12f))
                        .border(1.dp, PrimaryGreen.copy(alpha = 0.3f), RoundedCornerShape(8.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { onAddQty(-0.5) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "Decrease",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${String.format("%.1f", currentQty)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryDarkGreen
                    )

                    IconButton(
                        onClick = { onAddQty(0.5) },
                        modifier = Modifier.size(32.dp)
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
}
