package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "leads")
data class Lead(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val address: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val area: String,
    val notes: String,
    val recontactDate: Long, // Date and Time stored as milliseconds
    val createdAt: Long = System.currentTimeMillis()
)
