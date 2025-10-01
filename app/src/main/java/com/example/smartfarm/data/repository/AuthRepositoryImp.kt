package com.example.smartfarm.data.repository

import android.content.SharedPreferences
import android.util.Log
import com.example.smartfarm.data.model.User
import com.example.smartfarm.data.remote.model.LoginRequest
import com.example.smartfarm.data.remote.retrofit.config.LoginConfig
import com.example.smartfarm.data.remote.retrofit.config.RegisterConfig
import com.example.smartfarm.data.remote.retrofit.service.LoginApi
import com.example.smartfarm.data.remote.retrofit.service.RegisterApi
import com.example.smartfarm.util.FireStoreCollection
import com.example.smartfarm.util.SharedPrefConstants
import com.example.smartfarm.util.UiState
import com.example.smartfarm.util.UiState.*
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class AuthRepositoryImp (
    val auth: FirebaseAuth,
    val database: FirebaseFirestore,
    val appPreferences: SharedPreferences,
    val gson: Gson,
) : AuthRepository {

    override fun registerUser(
        email: String,
        password: String,
        user: User,
        result: (UiState<String>) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                it.user?.let { firebaseUser ->
                    user.firebase_id = firebaseUser.uid

                    // Get Firebase token for API authentication
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val firebaseIdToken = firebaseUser.getIdToken(false).await().token ?: ""

                            // Update Firestore user document
                            database.collection(FireStoreCollection.USER).document(user.firebase_id)
                                .set(user)
                                .addOnSuccessListener {
                                    // Register to custom API
                                    registerToCustomApi(firebaseIdToken, user.firebase_id, user, result)
                                }
                                .addOnFailureListener { exception ->
                                    result.invoke(UiState.Failure(exception.message.toString()))
                                }
                        } catch (e: Exception) {
                            CoroutineScope(Dispatchers.Main).launch {
                                result.invoke(UiState.Failure(e.message.toString()))
                            }
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                result.invoke(UiState.Failure(exception.message.toString()))
            }
    }

    override fun registerToCustomApi(
        token: String,
        firebaseId: String,
        user: User,
        result: (UiState<String>) -> Unit
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Create RegisterApi instance using RegisterConfig
                val registerApiService = RegisterConfig.getApiService(token)

                // Set the firebase_id in the user object before sending
                val userWithFirebaseId = user.copy(firebase_id = firebaseId)

                // Now pass only the user object with the firebase_id included
                val response = registerApiService.registerUser("Bearer $token", userWithFirebaseId)

                CoroutineScope(Dispatchers.Main).launch {
                    result.invoke(Success("User registered successfully"))
                }
            } catch (e: Exception) {
                CoroutineScope(Dispatchers.Main).launch {
                    result.invoke(Failure(e.message.toString()))
                }
            }
        }
    }

    override fun updateUserInfo(user: User, result: (UiState<String>) -> Unit) {
        val document = database.collection(FireStoreCollection.USER).document(user.firebase_id)
        document
            .set(user)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("User has been update successfully")
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.Failure(
                        it.localizedMessage
                    )
                )
            }
    }

        override fun loginUser(
            email: String,
            password: String,
            result: (UiState<String>) -> Unit) {
            auth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                // First get Firebase idToken to use as bearer token
                                val firebaseIdToken = task.result.user?.getIdToken(false)?.await()?.token ?: ""

                                // Create LoginApi instance using LoginConfig
                                val loginApiService = LoginConfig.getApiService(firebaseIdToken)

                                val loginRequest = LoginRequest(
                                    email = email,
                                    password = password,
                                    returnSecureToken = true
                                )
                                val apiKey = "AIzaSyAd0LKEMPkF9-shWzHXgrR9CMBfOxcBkzE"
                                val apiResponse = loginApiService.signInWithPassword(apiKey, loginRequest).execute().body()
                                // Store session after getting idToken
                                storeSession(id = task.result.user?.uid ?: "") { user ->
                                    CoroutineScope(Dispatchers.Main).launch {
                                        if (user == null) {
                                            result.invoke(UiState.Failure("Failed to store local session"))
                                        } else {
                                            if (apiResponse != null) {
                                                result.invoke(UiState.Success("Login successfully! idToken: ${apiResponse?.idToken}"))
                                            } else {
                                                result.invoke(UiState.Failure("Login Failed, account not found!"))
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AuthRepositoryImp", "Error during login API call", e)
                                CoroutineScope(Dispatchers.Main).launch {
                                    result.invoke(UiState.Failure("Login API call failed: ${e.message}"))
                                }
                            }
                        }
                    }
                }.addOnFailureListener {
                    result.invoke(UiState.Failure("Authentication failed, Check email and password"))
                }
        }

        override fun forgotPassword(email: String, result: (UiState<String>) -> Unit) {
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        result.invoke(UiState.Success("Email has been sent"))

                    } else {
                        result.invoke(UiState.Failure(task.exception?.message))
                    }
                }.addOnFailureListener {
                    result.invoke(UiState.Failure("Authentication failed, Check email"))
                }
        }

        override fun logout(result: () -> Unit) {
            auth.signOut()
            appPreferences.edit().putString(SharedPrefConstants.USER_SESSION,null).apply()
            result.invoke()
        }

        override fun storeSession(id: String, result: (User?) -> Unit) {
            database.collection(FireStoreCollection.USER).document(id)
                .get()
                .addOnCompleteListener {
                    if (it.isSuccessful){
                        val user = it.result.toObject(User::class.java)
                        appPreferences.edit().putString(SharedPrefConstants.USER_SESSION,gson.toJson(user)).apply()
                        result.invoke(user)
                    }else{
                        result.invoke(null)
                    }
                }
                .addOnFailureListener {
                    result.invoke(null)
                }
        }

        override fun getSession(result: (User?) -> Unit) {
            val user_str = appPreferences.getString(SharedPrefConstants.USER_SESSION,null)
            if (user_str == null){
                result.invoke(null)
            }else{
                val user = gson.fromJson(user_str,User::class.java)
                result.invoke(user)
            }
        }
}