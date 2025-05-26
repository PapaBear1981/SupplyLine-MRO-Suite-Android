package com.example.supplyline_mro_suite.di

import android.content.Context
import androidx.room.Room
import com.example.supplyline_mro_suite.data.local.SupplyLineDatabase
import com.example.supplyline_mro_suite.data.local.dao.*
// Hilt temporarily disabled
// import dagger.Module
// import dagger.Provides
// import dagger.hilt.InstallIn
// import dagger.hilt.android.qualifiers.ApplicationContext
// import dagger.hilt.components.SingletonComponent
// import javax.inject.Singleton

// @Module
// @InstallIn(SingletonComponent::class)
object DatabaseModule {

    // @Provides
    // @Singleton
    fun provideSupplyLineDatabase(/* @ApplicationContext */ context: Context): SupplyLineDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            SupplyLineDatabase::class.java,
            "supplyline_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    // @Provides
    fun provideUserDao(database: SupplyLineDatabase): UserDao {
        return database.userDao()
    }

    // @Provides
    fun provideToolDao(database: SupplyLineDatabase): ToolDao {
        return database.toolDao()
    }

    // @Provides
    fun provideToolCheckoutDao(database: SupplyLineDatabase): ToolCheckoutDao {
        return database.toolCheckoutDao()
    }

    // @Provides
    fun provideChemicalDao(database: SupplyLineDatabase): ChemicalDao {
        return database.chemicalDao()
    }

    // @Provides
    fun provideChemicalIssuanceDao(database: SupplyLineDatabase): ChemicalIssuanceDao {
        return database.chemicalIssuanceDao()
    }
}
