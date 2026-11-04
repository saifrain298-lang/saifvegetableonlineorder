package com.example

import com.example.data.SaifVegetableRepository
import com.example.model.PaymentMethod
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SaifVegetableFinancialTest {

    private lateinit var repository: SaifVegetableRepository

    @Before
    fun setup() {
        repository = SaifVegetableRepository.getInstance()
        repository.resetForTesting()
    }

    @Test
    fun testInitialCatalogue_HasExactly39VegetablesWith200KgStock() {
        val vegs = repository.vegetables.value
        assertEquals("Catalogue must have exactly 39 products", 39, vegs.size)
        val totalOpeningStock = vegs.sumOf { it.stockKg }
        assertEquals("Starting inventory must be exactly 7,800 kg (200kg * 39)", 7800.0, totalOpeningStock, 0.01)
    }

    @Test
    fun testScenario1_TwoKgOrderDecreasesStockByExactlyTwoKg() {
        val tomato = repository.vegetables.value.find { it.id == "veg_10" }!!
        val initialTomatoStock = tomato.stockKg

        // Add 2 kg tomato to cart
        repository.addToCart("veg_10", 2.0)
        // Add additional items to reach min order ₹100
        repository.addToCart("veg_01", 1.0) // Broccoli ₹114

        val initialOrderCount = repository.orders.value.size
        val result = repository.placeOrder(
            customerName = "Rahul Sharma",
            customerPhone = "9820011223",
            customerAddress = "Flat 101, Premier Exotica, Vidyavihar",
            pincode = "400077",
            paymentMethod = PaymentMethod.UPI
        )

        assertTrue("Order placement must succeed", result.isSuccess)
        val order = result.getOrNull()!!

        // Verify stock decreased by exactly 2 kg for tomato
        val updatedTomato = repository.vegetables.value.find { it.id == "veg_10" }!!
        assertEquals(initialTomatoStock - 2.0, updatedTomato.stockKg, 0.001)

        // Verify order appears once in ledger
        assertEquals(initialOrderCount + 1, repository.orders.value.size)
        val recordedOrder = repository.orders.value.find { it.orderId == order.orderId }
        assertNotNull(recordedOrder)
    }

    @Test
    fun testScenario2_DuplicateCancellationDoesNotRestoreStockTwice() {
        // Place an order for 2 kg Potato (veg_08)
        val potato = repository.vegetables.value.find { it.id == "veg_08" }!!
        val initialStock = potato.stockKg

        repository.addToCart("veg_08", 2.0)
        repository.addToCart("veg_01", 1.0) // Reach min order
        val orderResult = repository.placeOrder(
            customerName = "Amit Patel",
            customerPhone = "9820033445",
            customerAddress = "Ghatkopar East, Mumbai",
            pincode = "400077",
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY
        )
        val order = orderResult.getOrNull()!!
        val postOrderStock = repository.vegetables.value.find { it.id == "veg_08" }!!.stockKg
        assertEquals(initialStock - 2.0, postOrderStock, 0.001)

        // First cancellation restores stock
        val cancel1 = repository.cancelOrder(order.orderId, "First cancel call")
        assertTrue(cancel1.isSuccess)
        val postCancel1Stock = repository.vegetables.value.find { it.id == "veg_08" }!!.stockKg
        assertEquals(initialStock, postCancel1Stock, 0.001)

        // Second cancellation (repeated or duplicate request) must be idempotent!
        val cancel2 = repository.cancelOrder(order.orderId, "Duplicate retry call")
        assertTrue(cancel2.isSuccess)
        val postCancel2Stock = repository.vegetables.value.find { it.id == "veg_08" }!!.stockKg
        assertEquals("Stock must NOT be restored twice upon repeated cancellation", initialStock, postCancel2Stock, 0.001)
    }

    @Test
    fun testScenario3_ChangeInTodayPurchaseCostDoesNotRewriteHistoricalCostBasis() {
        // Place order for Baby Corn (veg_07)
        repository.addToCart("veg_07", 1.0) // price ~110, cost ~78
        val order = repository.placeOrder(
            customerName = "Sneha Kulkarni",
            customerPhone = "9820055667",
            customerAddress = "Kurla West, Mumbai",
            pincode = "400070",
            paymentMethod = PaymentMethod.UPI
        ).getOrNull()!!

        val recordedItem = order.items.find { it.vegetableId == "veg_07" }!!
        val historicalCostSnapshot = recordedItem.costPerKgSnapshot

        // Now owner edits wholesale purchase cost in mandi (e.g. increases from 78 to 95)
        repository.updateProductPrice("veg_07", newPrice = 130.0, newCost = 95.0, newDiscount = 0)

        // Verify modern vegetable has new cost
        val updatedVeg = repository.vegetables.value.find { it.id == "veg_07" }!!
        assertEquals(95.0, updatedVeg.costPerKg, 0.001)

        // Historical order MUST retain immutable snapshot
        val retainedOrder = repository.orders.value.find { it.orderId == order.orderId }!!
        val retainedItem = retainedOrder.items.find { it.vegetableId == "veg_07" }!!
        assertEquals("Historical order cost basis must NOT be rewritten", historicalCostSnapshot, retainedItem.costPerKgSnapshot, 0.001)
    }

    @Test
    fun testScenario4_AccountingPAndLReconcilesAccurately() {
        val pnl = repository.calculatePAndL()

        // Net Sales = Gross Sales - Discounts
        assertEquals(pnl.grossProductSales - pnl.discountsGiven, pnl.netProductSales, 0.001)
        // Gross Profit = Net Sales - COGS
        assertEquals(pnl.netProductSales - pnl.costOfGoodsSold, pnl.grossProfit, 0.001)
        // Net Operating Profit = Gross Profit + Delivery Fees - Operating Expenses
        val expectedNet = pnl.grossProfit + pnl.deliveryChargesCollected - pnl.totalOperatingExpenses
        assertEquals(expectedNet, pnl.netOperatingProfit, 0.001)
    }
}
