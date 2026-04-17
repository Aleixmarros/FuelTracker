package com.aleixmaro.fueltracker.ui.util
import java.text.SimpleDateFormat
import java.util.*

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(timestamp))
}

fun formatLiters(value: Double): String {
    return "%.2f L".format(value)
}

fun formatMoney(value: Double): String {
    return "%.2f €".format(value)
}


