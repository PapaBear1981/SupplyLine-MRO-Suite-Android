package com.example.supplyline_mro_suite.data.local

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
    version = 1,
    exportSchema = false
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
        
        fun getDatabase(context: Context): SupplyLineDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SupplyLineDatabase::class.java,
                    "supplyline_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
