package com.codecash.app

import java.io.Serializable

data class Transaction(
    val id: String,
    val merchantName: String,
    val category: String,
    val date: String,
    val amount: Double,
    val iconInitials: String
) : Serializable
