package com.example.supplyline_mro_suite.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "tools",
    indices = [
        Index(value = ["tool_number"], unique = true),
        Index(value = ["serial_number"], unique = true),
        Index(value = ["status"]),
        Index(value = ["category"]),
        Index(value = ["location"]),
        Index(value = ["calibration_due_date"])
    ]
)
data class Tool(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,

    @ColumnInfo(name = "tool_number")
    @SerializedName("tool_number")
    val toolNumber: String,

    @ColumnInfo(name = "serial_number")
    @SerializedName("serial_number")
    val serialNumber: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("category")
    val category: String, // "CL415", "RJ85", "Q400", "Engine", "CNC", "Sheetmetal", "General"

    @SerializedName("location")
    val location: String,

    @SerializedName("status")
    val status: String, // "Available", "Checked Out", "Maintenance", "Retired"

    @SerializedName("condition")
    val condition: String? = null,

    @SerializedName("notes")
    val notes: String? = null,

    @ColumnInfo(name = "created_at")
    @SerializedName("created_at")
    val createdAt: String? = null,

    @ColumnInfo(name = "updated_at")
    @SerializedName("updated_at")
    val updatedAt: String? = null,

    @ColumnInfo(name = "requires_calibration")
    @SerializedName("requires_calibration")
    val requiresCalibration: Boolean = false,

    @ColumnInfo(name = "calibration_due_date")
    @SerializedName("calibration_due_date")
    val calibrationDueDate: String? = null,

    @ColumnInfo(name = "calibration_interval_days")
    @SerializedName("calibration_interval_days")
    val calibrationIntervalDays: Int? = null
)

@Entity(
    tableName = "tool_checkouts",
    foreignKeys = [
        ForeignKey(
            entity = Tool::class,
            parentColumns = ["id"],
            childColumns = ["tool_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["tool_id"]),
        Index(value = ["user_id"]),
        Index(value = ["checkout_date"]),
        Index(value = ["is_active"])
    ]
)
data class ToolCheckout(
    @PrimaryKey
    @SerializedName("id")
    val id: Int = 0,

    @ColumnInfo(name = "tool_id")
    @SerializedName("tool_id")
    val toolId: Int,

    @ColumnInfo(name = "user_id")
    @SerializedName("user_id")
    val userId: Int,

    @ColumnInfo(name = "checkout_date")
    @SerializedName("checkout_date")
    val checkoutDate: String,

    @ColumnInfo(name = "expected_return_date")
    @SerializedName("expected_return_date")
    val expectedReturnDate: String,

    @ColumnInfo(name = "actual_return_date")
    @SerializedName("actual_return_date")
    val actualReturnDate: String? = null,

    @ColumnInfo(name = "return_condition")
    @SerializedName("return_condition")
    val returnCondition: String? = null,

    @ColumnInfo(name = "return_notes")
    @SerializedName("return_notes")
    val returnNotes: String? = null,

    @ColumnInfo(name = "checked_out_by")
    @SerializedName("checked_out_by")
    val checkedOutBy: String,

    @ColumnInfo(name = "returned_by")
    @SerializedName("returned_by")
    val returnedBy: String? = null,

    @ColumnInfo(name = "is_active")
    @SerializedName("is_active")
    val isActive: Boolean = true
)

data class ToolWithCheckout(
    val tool: Tool,
    val checkout: ToolCheckout? = null,
    val checkedOutUser: User? = null
)

data class CheckoutRequest(
    @SerializedName("tool_id")
    val toolId: Int,

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("expected_return_date")
    val expectedReturnDate: String,

    @SerializedName("notes")
    val notes: String? = null
)

// Data classes for advanced queries
data class ToolWithCheckoutInfo(
    val id: Int,
    @ColumnInfo(name = "tool_number") val toolNumber: String,
    @ColumnInfo(name = "serial_number") val serialNumber: String,
    val description: String,
    val category: String,
    val location: String,
    val status: String,
    val condition: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: String?,
    @ColumnInfo(name = "requires_calibration") val requiresCalibration: Boolean,
    @ColumnInfo(name = "calibration_due_date") val calibrationDueDate: String?,
    @ColumnInfo(name = "calibration_interval_days") val calibrationIntervalDays: Int?,
    @ColumnInfo(name = "checkout_date") val checkoutDate: String?,
    @ColumnInfo(name = "expected_return_date") val expectedReturnDate: String?,
    @ColumnInfo(name = "checked_out_to_name") val checkedOutToName: String?
)

data class ToolUsageStats(
    val id: Int,
    @ColumnInfo(name = "tool_number") val toolNumber: String,
    @ColumnInfo(name = "serial_number") val serialNumber: String,
    val description: String,
    val category: String,
    val location: String,
    val status: String,
    val condition: String?,
    val notes: String?,
    @ColumnInfo(name = "created_at") val createdAt: String?,
    @ColumnInfo(name = "updated_at") val updatedAt: String?,
    @ColumnInfo(name = "requires_calibration") val requiresCalibration: Boolean,
    @ColumnInfo(name = "calibration_due_date") val calibrationDueDate: String?,
    @ColumnInfo(name = "calibration_interval_days") val calibrationIntervalDays: Int?,
    @ColumnInfo(name = "checkout_count") val checkoutCount: Int
)

data class ReturnRequest(
    @SerializedName("checkout_id")
    val checkoutId: Int,

    @SerializedName("condition")
    val condition: String,

    @SerializedName("notes")
    val notes: String? = null
)
