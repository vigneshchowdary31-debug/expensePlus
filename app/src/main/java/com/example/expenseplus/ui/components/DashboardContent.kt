package com.example.expenseplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.example.expenseplus.data.expenseCategories
import java.net.URLEncoder
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    expenses: List<Expense>,
    onDeleteExpense: (Expense) -> Unit,
    onEditExpense: (Expense) -> Unit,
    navController: NavController,
    innerPadding: PaddingValues
) {
    var selectedFilterCategory by remember { mutableStateOf("All Categories") }
    var showEditExpenseDialog by remember { mutableStateOf(false) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }

    val currentMonthExpenses = expenses.filter {
        val currentMonth = YearMonth.now()
        YearMonth.from(it.date) == currentMonth
    }

    val totalExpense = currentMonthExpenses.sumOf { it.amount }
    val expensesByCategory = currentMonthExpenses.groupBy { it.category }
        .mapValues { (_, expensesInCat) -> expensesInCat.sumOf { it.amount } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Expense",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "₹%.2f".format(totalExpense),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Expenses by Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (expensesByCategory.isEmpty()) {
            Text("No expenses yet.", modifier = Modifier.padding(bottom = 16.dp))
        } else {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(expensesByCategory.toList(), key = { it.first }) { (category, amount) ->
                    CategoryExpenseCard(category = category, amount = amount, onCategoryClick = { clickedCategory ->
                        val encodedCategory = URLEncoder.encode(clickedCategory, "UTF-8")
                        navController.navigate("category_detail_screen/$encodedCategory")
                    })
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        Text(
            text = "Recent Transactions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedFilterCategory,
                onValueChange = {},
                readOnly = true,
                label = { Text("Filter by Category") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                val categories = listOf("All Categories") + expenseCategories
                categories.forEach { category ->
                    DropdownMenuItem(text = { Text(category) }, onClick = {
                        selectedFilterCategory = category
                        expanded = false
                    })
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        val filteredExpenses = if (selectedFilterCategory == "All Categories") {
            currentMonthExpenses
        } else {
            currentMonthExpenses.filter { it.category == selectedFilterCategory }
        }

        if (filteredExpenses.isEmpty()) {
            Text("No expenses yet.", modifier = Modifier.padding(bottom = 16.dp))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = {
                            if (it == SwipeToDismissBoxValue.EndToStart) {
                                onDeleteExpense(expense)
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val color = when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.EndToStart -> Color.Red
                                else -> Color.Transparent
                            }
                            Box(modifier = Modifier
                                .fillMaxSize()
                                .background(color)
                                .padding(horizontal = 20.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(Icons.Filled.Delete, "Delete Expense", tint = Color.White)
                            }
                        },
                        enableDismissFromEndToStart = true
                    ) {
                        ExpenseCard(
                            expense = expense,
                            modifier = Modifier.clickable {
                                expenseToEdit = expense
                                showEditExpenseDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showEditExpenseDialog && expenseToEdit != null) {
        Dialog(onDismissRequest = { showEditExpenseDialog = false; expenseToEdit = null }) {
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