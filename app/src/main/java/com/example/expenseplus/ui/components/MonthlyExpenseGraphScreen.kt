package com.example.expenseplus.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.time.YearMonth
import android.graphics.Paint
import java.util.SortedMap
import java.util.TreeMap
import java.time.LocalDate
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyExpenseGraphScreen(expenses: List<Expense>, navController: NavController) {
    var selectedYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var yearExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monthly Expense Graph") },
                actions = {
                    ExposedDropdownMenuBox(
                        expanded = yearExpanded,
                        onExpandedChange = { yearExpanded = !yearExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedYear.toString(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Year") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = yearExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .width(100.dp) // Shrink the width
                        )
                        ExposedDropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            val existingYears = expenses.map { it.date.year }.distinct()
                            val yearsToShow = (existingYears + 2024).distinct().sortedDescending()
                            if (yearsToShow.isNotEmpty()) {
                                yearsToShow.forEach { year ->
                                    DropdownMenuItem(text = { Text(year.toString()) }, onClick = {
                                        selectedYear = year
                                        yearExpanded = false
                                    })
                                }
                            } else {
                                DropdownMenuItem(text = { Text(LocalDate.now().year.toString()) }, onClick = {
                                    selectedYear = LocalDate.now().year
                                    yearExpanded = false
                                })
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val allMonths = (1..12).map { month -> YearMonth.of(selectedYear, month) }

            val monthlyExpensesData = expenses
                .filter { it.date.year == selectedYear }
                .groupBy { YearMonth.from(it.date) }
                .mapValues { (_, monthlyExpenses) -> monthlyExpenses.sumOf { it.amount } }

            val monthlyExpensesForGraph = TreeMap<YearMonth, Double>()
            allMonths.forEach { yearMonth ->
                monthlyExpensesForGraph[yearMonth] = monthlyExpensesData[yearMonth] ?: 0.0
            }

            if (monthlyExpensesForGraph.values.all { it == 0.0 }) {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No expenses to display graph for $selectedYear.", style = MaterialTheme.typography.titleMedium)
                }
            } else {
                MonthlyBarGraph(
                    monthlyExpenses = monthlyExpensesForGraph,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Monthly Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, bottom = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(monthlyExpensesForGraph.entries.toList(), key = { it.key }) { (yearMonth, amount) ->
                        MonthExpenseItem(
                            yearMonth = yearMonth,
                            amount = amount,
                            onMonthClick = { clickedYearMonth ->
                                navController.navigate("monthly_transactions_screen/${clickedYearMonth.year}/${clickedYearMonth.monthValue}")
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthExpenseItem(yearMonth: YearMonth, amount: Double, onMonthClick: (YearMonth) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onMonthClick(yearMonth) },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = yearMonth.month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault()) + " ${yearMonth.year}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "₹%.2f".format(amount),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
fun MonthlyBarGraph(monthlyExpenses: SortedMap<YearMonth, Double>, modifier: Modifier = Modifier) {
    val maxExpense = monthlyExpenses.values.maxOrNull() ?: 1.0 // Prevent division by zero
    val startColor = MaterialTheme.colorScheme.primary
    val endColor = MaterialTheme.colorScheme.tertiary
    val textColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val paddingHorizontal = 20.dp.toPx()
        val paddingBottom = 40.dp.toPx()
        val paddingTop = 20.dp.toPx()
        val paddingLeft = 40.dp.toPx() // Space for Y-axis labels

        val graphHeight = size.height - paddingTop - paddingBottom
        val graphWidth = size.width - paddingLeft - paddingHorizontal

        val barCount = monthlyExpenses.size
        val barWidth = (graphWidth / barCount) * 0.7f // 70% bar width, 30% space
        val spaceBetweenBars = (graphWidth / barCount) * 0.3f

        val textPaint = Paint().apply {
            color = textColor.toArgb()
            textSize = 12.sp.toPx()
            textAlign = Paint.Align.RIGHT
        }

        // Draw Y-axis labels and grid lines
        val yAxisLabelCount = 5
        val yAxisStep = maxExpense / (yAxisLabelCount - 1)

        for (i in 0 until yAxisLabelCount) {
            val yValue = i * yAxisStep
            val yPos = graphHeight + paddingTop - (yValue / maxExpense).toFloat() * graphHeight

            // Draw horizontal grid line
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(paddingLeft, yPos),
                end = Offset(paddingLeft + graphWidth, yPos),
                strokeWidth = 1f
            )
            // Draw Y-axis label
            drawContext.canvas.nativeCanvas.drawText(
                "₹%.0f".format(yValue),
                paddingLeft - 5.dp.toPx(),
                yPos + 5.dp.toPx(),
                textPaint
            )
        }

        // Draw X-axis line (optional, as grid lines provide the reference)
        drawLine(
            color = Color.Gray,
            start = Offset(paddingLeft, graphHeight + paddingTop),
            end = Offset(paddingLeft + graphWidth, graphHeight + paddingTop),
            strokeWidth = 2f
        )

        // X-axis and bars
        var currentX = paddingLeft + (spaceBetweenBars / 2)

        monthlyExpenses.forEach { (yearMonth, expense) ->
            val barHeight = (expense / maxExpense).toFloat() * graphHeight
            val topOffset = graphHeight + paddingTop - barHeight

            // Draw bar with gradient and rounded corners
            drawRect(
                brush = Brush.verticalGradient(colors = listOf(startColor, endColor)),
                topLeft = Offset(currentX, topOffset),
                size = Size(barWidth, barHeight)
            )

            // Draw month label
            drawContext.canvas.nativeCanvas.drawText(
                yearMonth.month.getDisplayName(java.time.format.TextStyle.SHORT, java.util.Locale.getDefault()),
                currentX + barWidth / 2,
                graphHeight + paddingTop + 20.dp.toPx(), // Below X-axis
                Paint().apply {
                    color = textColor.toArgb()
                    textAlign = Paint.Align.CENTER
                    textSize = 10.sp.toPx()
                }
            )

            // Draw expense amount on top of the bar
            if (expense > 0) {
                drawContext.canvas.nativeCanvas.drawText(
                    "₹%.0f".format(expense),
                    currentX + barWidth / 2,
                    topOffset - 10.dp.toPx(), // Above the bar
                    Paint().apply {
                        color = textColor.toArgb()
                        textAlign = Paint.Align.CENTER
                        textSize = 8.sp.toPx()
                    }
                )
            }

            currentX += barWidth + spaceBetweenBars
        }
    }
}
