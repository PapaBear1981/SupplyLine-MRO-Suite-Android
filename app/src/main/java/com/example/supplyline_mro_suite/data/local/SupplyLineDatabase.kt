package com.example.supplyline_mro_suite.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import android.content.Context
import com.example.supplyline_mro_suite.data.local.dao.*
import com.example.supplyline_mro_suite.data.model.*

@Database(
    entities = [
        User::class,
        Tool::class,
        ToolCheckout::class,
        Chemical::class,
        ChemicalIssuance::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class SupplyLineDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun toolDao(): ToolDao
    abstract fun toolCheckoutDao(): ToolCheckoutDao
    abstract fun chemicalDao(): ChemicalDao
    abstract fun chemicalIssuanceDao(): ChemicalIssuanceDao

    companion object {
        @Volatile
        private var INSTANCE: SupplyLineDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add indices for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tools_tool_number` ON `tools` (`tool_number`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tools_serial_number` ON `tools` (`serial_number`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tools_status` ON `tools` (`status`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tools_category` ON `tools` (`category`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tools_location` ON `tools` (`location`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tools_calibration_due_date` ON `tools` (`calibration_due_date`)")

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_users_employee_number` ON `users` (`employee_number`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_users_department` ON `users` (`department`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_users_is_active` ON `users` (`is_active`)")

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemicals_part_number_lot_number` ON `chemicals` (`part_number`, `lot_number`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemicals_status` ON `chemicals` (`status`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemicals_category` ON `chemicals` (`category`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemicals_location` ON `chemicals` (`location`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemicals_expiration_date` ON `chemicals` (`expiration_date`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemicals_is_archived` ON `chemicals` (`is_archived`)")

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tool_checkouts_tool_id` ON `tool_checkouts` (`tool_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tool_checkouts_user_id` ON `tool_checkouts` (`user_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tool_checkouts_checkout_date` ON `tool_checkouts` (`checkout_date`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_tool_checkouts_is_active` ON `tool_checkouts` (`is_active`)")

                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemical_issuances_chemical_id` ON `chemical_issuances` (`chemical_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemical_issuances_user_id` ON `chemical_issuances` (`user_id`)")
                database.execSQL("CREATE INDEX IF NOT EXISTS `index_chemical_issuances_issue_date` ON `chemical_issuances` (`issue_date`)")
            }
        }

        fun getDatabase(context: Context): SupplyLineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SupplyLineDatabase::class.java,
                    "supplyline_database"
                )
                .addMigrations(MIGRATION_1_2)
                .addCallback(DatabaseCallback())
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Database created for the first time
                // You can add initial data here if needed
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                // Database opened
                // Enable foreign key constraints
                db.execSQL("PRAGMA foreign_keys=ON")
            }
        }
    }
}
