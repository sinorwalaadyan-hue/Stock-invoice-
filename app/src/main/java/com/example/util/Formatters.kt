package com.example.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object Formatters {
    private val indianLocale = Locale("en", "IN")
    private val currencyFormat = NumberFormat.getCurrencyInstance(indianLocale).apply {
        maximumFractionDigits = 0
    }
    private val currencyWithDecimalFormat = NumberFormat.getCurrencyInstance(indianLocale).apply {
        maximumFractionDigits = 2
    }
    private val numberFormat = NumberFormat.getNumberInstance(indianLocale)

    fun formatCurrency(amount: Double, showDecimals: Boolean = false): String {
        return try {
            if (showDecimals) {
                currencyWithDecimalFormat.format(amount)
            } else {
                currencyFormat.format(amount)
            }
        } catch (e: Exception) {
            "₹" + String.format(Locale.US, "%.0f", amount)
        }
    }

    fun formatNumber(number: Number): String {
        return try {
            numberFormat.format(number)
        } catch (e: Exception) {
            number.toString()
        }
    }

    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "N/A"
        }
    }

    fun formatDateTime(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "N/A"
        }
    }

    fun formatTimeOnly(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "N/A"
        }
    }
}
