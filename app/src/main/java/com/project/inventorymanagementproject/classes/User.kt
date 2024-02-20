package com.project.inventorymanagementproject.classes

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.type.Date
import com.google.type.DateTime
import kotlinx.parcelize.Parcelize
import java.io.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.Objects

@Parcelize
data class User(
    private val firstName:String = "",
    private val lastName: String = "",
    private val email: String = "",
    private val department: String = "",
    private val isAdmin: Boolean = false,
    private var userProfilePicture: String = ""
) : Parcelable{

    public fun getFirstName(): String {
        return this.firstName
    }

    public fun getLastName(): String {
        return this.lastName
    }

    public fun getEmail(): String{
        return this.email
    }

    public fun getDepartment(): String {
        return this.department
    }

    public fun getIsAdmin(): Boolean {
        return this.isAdmin
    }

    public fun getUserProfilePicture(): String{
        return this.userProfilePicture
    }

    public fun setUserProfilePicture(uri: String){
        this.userProfilePicture = uri
    }
}