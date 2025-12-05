package com.example.expenseplus.ui.components

import java.time.LocalDate
import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    var amount: Double,
    var category: String,
    var remarks: String?,
    var date: LocalDate
)
