package com.example.supplyline_mro_suite.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "tools")
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

@Entity(tableName = "tool_checkouts")
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

data class ReturnRequest(
    @SerializedName("checkout_id")
    val checkoutId: Int,

    @SerializedName("condition")
    val condition: String,

    @SerializedName("notes")
    val notes: String? = null
)
