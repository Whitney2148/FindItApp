package com.example.finditapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lost_items")
data class LostItemEntity(

    @PrimaryKey
    val id: String = "",

    val title: String = "",
    val description: String = "",
    val locationFound: String = "",
    val finderContact: String = "",
    val imageUri: String? = null,
    val reporterId: String = "", 
    val isClaimed: Boolean = false,
    val type: String = "FOUND" 
)
