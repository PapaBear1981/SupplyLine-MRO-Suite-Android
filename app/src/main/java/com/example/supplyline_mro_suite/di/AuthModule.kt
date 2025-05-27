package com.example.supplyline_mro_suite.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.supplyline_mro_suite.data.auth.AuthRepository
import com.example.supplyline_mro_suite.data.auth.AuthenticationValidator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideAuthenticationValidator(): AuthenticationValidator {
        return AuthenticationValidator()
    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        dataStore: DataStore<Preferences>,
        validator: AuthenticationValidator
    ): AuthRepository {
        return AuthRepository(dataStore, validator)
    }
}
