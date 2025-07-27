package com.example.walletapp

import java.io.Serializable

data class Expense(
    val amount: String = "",
    val description: String = "",
    val date: String = ""
) : Serializable
