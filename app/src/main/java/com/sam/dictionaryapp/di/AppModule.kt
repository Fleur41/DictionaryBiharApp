package com.sam.dictionaryapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.sam.dictionaryapp.data.local.AppDatabase
import com.sam.dictionaryapp.data.remote.DictionaryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import androidx.datastore.preferences.core.Preferences
//import java.util.prefs.Preferences

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    //Network
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val baseUrl = "https://api.dictionaryapi.dev"
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    @Provides
    @Singleton
    fun provideDictionaryApi(retrofit: Retrofit): DictionaryApi {
        return retrofit.create(DictionaryApi::class.java)
    }

    // Database
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase{
        return Room.databaseBuilder(context, AppDatabase::class.java, "dictionary.db").build()
    }

    @Provides
    fun provideSavedWordDao(database: AppDatabase) = database.savedWordDao()

    // DataStore
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences>{
        return context.dataStore
    }
}