package com.example.supplyline_mro_suite.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.supplyline_mro_suite.data.local.dao.*
import com.example.supplyline_mro_suite.data.remote.ApiService
import com.example.supplyline_mro_suite.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideToolRepository(
        toolDao: ToolDao,
        toolCheckoutDao: ToolCheckoutDao,
        apiService: ApiService
    ): ToolRepository {
        return ToolRepository(toolDao, toolCheckoutDao, apiService)
    }

    @Provides
    @Singleton
    fun provideChemicalRepository(
        chemicalDao: ChemicalDao,
        chemicalIssuanceDao: ChemicalIssuanceDao,
        apiService: ApiService
    ): ChemicalRepository {
        return ChemicalRepository(chemicalDao, chemicalIssuanceDao, apiService)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        userDao: UserDao,
        apiService: ApiService,
        dataStore: DataStore<Preferences>
    ): UserRepository {
        return UserRepository(userDao, apiService, dataStore)
    }
}
