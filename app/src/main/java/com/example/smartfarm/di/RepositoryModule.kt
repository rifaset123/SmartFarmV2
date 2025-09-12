package com.example.smartfarm.di

import android.content.SharedPreferences
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
        auth: FirebaseAuth,
        appPreferences: SharedPreferences,
        gson: Gson
    ): AuthRepository {
        return AuthRepositoryImp(auth, database, appPreferences, gson)
    }
}