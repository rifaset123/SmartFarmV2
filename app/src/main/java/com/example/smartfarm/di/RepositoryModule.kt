package com.example.smartfarm.di

import android.content.SharedPreferences
import com.example.smartfarm.data.remote.retrofit.service.LoginApi
import com.example.smartfarm.data.remote.retrofit.service.RegisterApi
import com.example.smartfarm.data.repository.AuthRepository
import com.example.smartfarm.data.repository.AuthRepositoryImp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepositoryModule {

//    @Provides
//    @Singleton
//    fun provideNoteRepository(
//        database: FirebaseFirestore,
//        storageReference: StorageReference
//    ): NoteRepository{
//        return NoteRepositoryImp(database,storageReference)
//    }
//
//    @Provides
//    @Singleton
//    fun provideTaskRepository(
//        database: FirebaseDatabase
//    ): TaskRepository{
//        return TaskRepositoryImp(database)
//    }

    @Provides
    @Singleton
    fun provideAuthRepository(
        database: FirebaseFirestore,
        firebaseAuth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson,
    ): AuthRepository {
        return AuthRepositoryImp(firebaseAuth, database, appPreferences, gson)
    }
}