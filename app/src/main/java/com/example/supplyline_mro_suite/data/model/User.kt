package com.example.supplyline_mro_suite.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "users",
    indices = [
        Index(value = ["employee_number"], unique = true),
        Index(value = ["department"]),
        Index(value = ["is_active"])
    ]
)
data class User(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,

    @ColumnInfo(name = "employee_number")
    @SerializedName("employee_number")
    val employeeNumber: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("department")
    val department: String, // "Maintenance", "Materials", "Admin"

    @ColumnInfo(name = "is_admin")
    @SerializedName("is_admin")
    val isAdmin: Boolean = false,

    @ColumnInfo(name = "avatar_url")
    @SerializedName("avatar_url")
    val avatarUrl: String? = null,

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String? = null,

    @ColumnInfo(name = "last_login")
    @SerializedName("last_login")
    val lastLogin: String? = null,

    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    val isActive: Boolean = true
)

data class LoginRequest(
    @SerializedName("employee_number")
    val employeeNumber: String,

    @SerializedName("password")
    val password: String
)

data class LoginResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("user")
    val user: User? = null,

    @SerializedName("token")
    val token: String? = null
)
