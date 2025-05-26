package com.example.supplyline_mro_suite.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "chemicals")
data class Chemical(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,

    @ColumnInfo(name = "part_number")
    @SerializedName("part_number")
    val partNumber: String,

    @ColumnInfo(name = "lot_number")
    @SerializedName("lot_number")
    val lotNumber: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("manufacturer")
    val manufacturer: String,

    @SerializedName("category")
    val category: String, // "Sealant", "Paint", "Adhesive", "Solvent", "Lubricant", "Other"

    @SerializedName("location")
    val location: String,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("unit")
    val unit: String, // "ml", "L", "g", "kg", "oz", "lb"

    @ColumnInfo(name = "expiration_date")
    @SerializedName("expiration_date")
    val expirationDate: String,

    @ColumnInfo(name = "minimum_stock_level")
    @SerializedName("minimum_stock_level")
    val minimumStockLevel: Double,

    @SerializedName("status")
    val status: String, // "Good", "Expiring", "Expired", "Low Stock", "Archived"

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @ColumnInfo(name = "is_archived")
    @SerializedName("is_archived")
    val isArchived: Boolean = false
)

@Entity(tableName = "chemical_issuances")
data class ChemicalIssuance(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,

    @ColumnInfo(name = "chemical_id")
    @SerializedName("chemical_id")
    val chemicalId: Int,

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    val userId: Int,

    @ColumnInfo(name = "quantity_issued")
    @SerializedName("quantity_issued")
    val quantityIssued: Double,

    @ColumnInfo(name = "issue_date")
    @SerializedName("issue_date")
    val issueDate: String,

    @SerializedName("location")
    val location: String,

    @SerializedName("purpose")
    val purpose: String? = null,

    @ColumnInfo(name = "issued_by")
    @SerializedName("issued_by")
    val issuedBy: String,

    @SerializedName("notes")
    val notes: String? = null
)

data class ChemicalWithIssuances(
    val chemical: Chemical,
    val issuances: List<ChemicalIssuance> = emptyList(),
    val totalIssued: Double = 0.0,
    val remainingQuantity: Double = 0.0
)

data class IssueRequest(
    @SerializedName("chemical_id")
    val chemicalId: Int,

    @SerializedName("quantity")
    val quantity: Double,

    @SerializedName("location")
    val location: String,

    @SerializedName("purpose")
    val purpose: String? = null,

    @SerializedName("notes")
    val notes: String? = null
)
