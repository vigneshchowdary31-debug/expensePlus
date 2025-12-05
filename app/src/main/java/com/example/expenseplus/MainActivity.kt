package com.example.expenseplus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.example.expenseplus.ui.components.Expense
import com.example.expenseplus.ui.components.MainScreen
import com.example.expenseplus.ui.theme.ExpensePlusTheme
import java.time.LocalDate
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expenseplus.ui.components.ExpenseCategoryDetailScreen
import com.example.expenseplus.ui.components.MonthlyExpenseGraphScreen
import com.example.expenseplus.ui.components.MonthlyTransactionsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExpensePlusTheme {
                val navController = rememberNavController()
                val expenses = remember { mutableStateListOf<Expense>() }

                NavHost(navController = navController, startDestination = "main_screen") {
                    composable("main_screen") {
                        MainScreen(
                            expenses = expenses,
                            onAddExpenseClick = { newExpense -> expenses.add(newExpense) },
                            onDeleteExpense = { expenseToDelete -> expenses.remove(expenseToDelete) },
                            onEditExpense = { updatedExpense ->
                                val index = expenses.indexOfFirst { it.id == updatedExpense.id }
                                if (index != -1) {
                                    expenses[index] = updatedExpense
                                }
                            },
                            navController = navController
                        )
                    }
                    composable(
                        "category_detail_screen/{category}",
                        arguments = listOf(navArgument("category") { type = NavType.StringType })
                    ) { backStackEntry ->
                        val encodedCategory = backStackEntry.arguments?.getString("category")
                        if (encodedCategory != null) {
                            val category = java.net.URLDecoder.decode(encodedCategory, "UTF-8")
                            ExpenseCategoryDetailScreen(category = category, expenses = expenses, onBackClick = { navController.popBackStack() })
                        }
                    }
                    composable(
                        "monthly_transactions_screen/{year}/{month}",
                        arguments = listOf(
                            navArgument("year") { type = NavType.IntType },
                            navArgument("month") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val year = backStackEntry.arguments?.getInt("year")
                        val month = backStackEntry.arguments?.getInt("month")
                        if (year != null && month != null) {
                            MonthlyTransactionsScreen(year = year, month = month, expenses = expenses, navController = navController)
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ExpensePlusTheme {
        val sampleExpenses = remember { mutableStateListOf(
            Expense(amount = 10.0, category = "Food", remarks = "Dinner", date = LocalDate.now()),
            Expense(amount = 10.0, category = "Travel", remarks = "Bus fare", date = LocalDate.now())
        ) }
        MainScreen(
            expenses = sampleExpenses,
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
}
