package com.example.smartfarm.data.remote.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LoginResponse(
    val kind: String?,
    val localId: String?,
    val email: String?,
    val displayName: String?,
    val idToken: String?,
    val registered: Boolean?,
    val refreshToken: String?,
    val expiresIn: String?
) : Parcelable
