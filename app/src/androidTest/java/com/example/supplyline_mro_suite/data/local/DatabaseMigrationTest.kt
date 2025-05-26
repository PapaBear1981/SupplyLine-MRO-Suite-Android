package com.example.supplyline_mro_suite.data.local

import androidx.room.Room
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationTest {

    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        SupplyLineDatabase::class.java,
        listOf(),
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You cannot use DAO classes because they expect the latest schema.
            execSQL("INSERT INTO users (id, employee_number, name, department, is_admin, is_active) VALUES (1, 'EMP001', 'John Doe', 'Maintenance', 0, 1)")
            execSQL("INSERT INTO tools (id, tool_number, serial_number, description, category, location, status, requires_calibration) VALUES (1, 'T001', 'SN001', 'Test Tool', 'General', 'A1', 'Available', 0)")
            execSQL("INSERT INTO chemicals (id, part_number, lot_number, description, manufacturer, category, location, quantity, unit, expiration_date, minimum_stock_level, status, is_archived) VALUES (1, 'PN001', 'LOT001', 'Test Chemical', 'Manufacturer', 'Sealant', 'A1', 100.0, 'ml', '2025-12-31', 10.0, 'Good', 0)")
            
            close()
        }

        // Re-open the database with version 2 and provide MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, SupplyLineDatabase.MIGRATION_1_2)

        // MigrationTestHelper automatically verifies the schema changes,
        // but you can also verify that the data was preserved
        val cursor = db.query("SELECT * FROM users WHERE id = 1")
        assert(cursor.moveToFirst())
        assert(cursor.getString(cursor.getColumnIndexOrThrow("employee_number")) == "EMP001")
        cursor.close()
    }

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(TEST_DB, 1).apply {
            close()
        }

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        Room.databaseBuilder(
            ApplicationProvider.getApplicationContext(),
            SupplyLineDatabase::class.java,
            TEST_DB
        ).addMigrations(SupplyLineDatabase.MIGRATION_1_2).build().apply {
            openHelper.writableDatabase.close()
        }
    }
}
