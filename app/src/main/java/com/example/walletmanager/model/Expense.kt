package com.example.walletapp

import java.io.Serializable

data class Expense(
    val amount: String = "",
    val description: String = "",
    val date: String = "",
    val time: String? = null,
    val timestamp: Long = System.currentTimeMillis()

) : Serializable
