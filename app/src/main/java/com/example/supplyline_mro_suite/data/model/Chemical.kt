package com.example.supplyline_mro_suite.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "chemicals",
    indices = [
        Index(value = ["part_number", "lot_number"], unique = true),
        Index(value = ["status"]),
        Index(value = ["category"]),
        Index(value = ["location"]),
        Index(value = ["expiration_date"]),
        Index(value = ["is_archived"])
    ]
)
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

@Entity(
    tableName = "chemical_issuances",
    foreignKeys = [
        ForeignKey(
            entity = Chemical::class,
            parentColumns = ["id"],
            childColumns = ["chemical_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["chemical_id"]),
        Index(value = ["user_id"]),
        Index(value = ["issue_date"])
    ]
)
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

data class ChemicalWithUsage(
    val id: Int,
    @ColumnInfo(name = "part_number") val partNumber: String,
    @ColumnInfo(name = "lot_number") val lotNumber: String,
    val description: String,
    val manufacturer: String,
    val category: String,
    val location: String,
    val quantity: Double,
    val unit: String,
    @ColumnInfo(name = "expiration_date") val expirationDate: String,
    @ColumnInfo(name = "minimum_stock_level") val minimumStockLevel: Double,
    val status: String,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: String?,
    @ColumnInfo(name = "is_archived") val isArchived: Boolean,
    @ColumnInfo(name = "total_issued") val totalIssued: Double,
    @ColumnInfo(name = "remaining_quantity") val remainingQuantity: Double
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
