package com.example.expenseplus.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import java.time.LocalDate

data class TabItem(val title: String, val icon: ImageVector)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    expenses: List<Expense>,
    onAddExpenseClick: (Expense) -> Unit,
    onDeleteExpense: (Expense) -> Unit,
    onEditExpense: (Expense) -> Unit,
    navController: NavController
) {
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabs = listOf(
        TabItem("Home", Icons.Filled.Home),
        TabItem("Insights", Icons.AutoMirrored.Filled.List)
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    var showEditExpenseDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTabIndex) {
        pagerState.animateScrollToPage(selectedTabIndex)
    }
    LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
        if (!pagerState.isScrollInProgress) {
            selectedTabIndex = pagerState.currentPage
        }
    }

    Scaffold(
        floatingActionButton = {
            if (selectedTabIndex == 0) { // Show only on Home tab
                FloatingActionButton(onClick = { showAddExpenseDialog = true }) {
                    Icon(Icons.Filled.Add, "Add new expense")
                }
            }
        },
        bottomBar = {
            // 🔥 Evenly spread tabs using TabRow + weight(1f)
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, item ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(item.title) },
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        // Modifier.weight(1f) removed as it is not applicable in TabRow scope
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) { page ->
                when (page) {
                    0 -> DashboardContent(
                        expenses,
                        onDeleteExpense = onDeleteExpense,
                        onEditExpense = { exp ->
                            expenseToEdit = exp
                            showEditExpenseDialog = true
                        },
                        navController = navController,
                        innerPadding = innerPadding
                    )
                    1 -> MonthlyExpenseGraphScreen(expenses, navController)
                }
            }
        }
    }

    if (showAddExpenseDialog) {
        Dialog(onDismissRequest = { showAddExpenseDialog = false }) {
            AddExpenseScreen(onAddExpense = { amount, category, remarks, date ->
                onAddExpenseClick(
                    Expense(
                        amount = amount,
                        category = category,
                        remarks = remarks,
                        date = date
                    )
                )
                showAddExpenseDialog = false
            })
        }
    }

    if (showEditExpenseDialog && expenseToEdit != null) {
        Dialog(onDismissRequest = {
            showEditExpenseDialog = false
            expenseToEdit = null
        }) {
            EditExpenseScreen(
                expense = expenseToEdit!!,
                onEditExpense = { updatedExpense ->
                    onEditExpense(updatedExpense)
                    showEditExpenseDialog = false
                    expenseToEdit = null
                },
                onCancelEdit = {
                    showEditExpenseDialog = false
                    expenseToEdit = null
                }
            )
        }
    }
}

@Composable
fun CategoryExpenseCard(category: String, amount: Double, onCategoryClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .clickable { onCategoryClick(category) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹%.2f".format(amount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun ExpenseCard(expense: Expense, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expense.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "₹${expense.amount}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Date: ${expense.date}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                expense.remarks?.let { remarks ->
                    if (remarks.isNotBlank()) {
                        Text(
                            "Remarks: $remarks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewMainScreen() {
    val sampleExpenses = remember {
        mutableStateListOf(
            Expense(
                amount = 100.0,
                category = "Food",
                remarks = "Lunch with friends",
                date = LocalDate.now()
            ),
            Expense(
                amount = 50.0,
                category = "Travel",
                remarks = null,
                date = LocalDate.now()
            ),
            Expense(
                amount = 1200.0,
                category = "EMI",
                remarks = "Car Loan",
                date = LocalDate.now().minusMonths(1)
            ),
            Expense(
                amount = 200.0,
                category = "Other",
                remarks = "New gadget",
                date = LocalDate.now().minusDays(1)
            ),
            Expense(
                amount = 75.0,
                category = "Bills",
                remarks = "Electricity bill",
                date = LocalDate.now()
            ),
            Expense(
                amount = 300.0,
                category = "Food",
                remarks = "Dinner with family",
                date = LocalDate.now()
            )
        )
    }

    MainScreen(
        sampleExpenses,
        onAddExpenseClick = { newExpense -> sampleExpenses.add(newExpense) },
        onDeleteExpense = { expense -> sampleExpenses.remove(expense) },
        onEditExpense = { updatedExpense ->
            val index = sampleExpenses.indexOfFirst { it.id == updatedExpense.id }
            if (index != -1) {
                sampleExpenses[index] = updatedExpense
            }
        },
        navController = rememberNavController()
    )
}
