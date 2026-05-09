package com.example.finditapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val uid: String = "", 
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String? = null,
    val joinedDate: Long = System.currentTimeMillis()
)
