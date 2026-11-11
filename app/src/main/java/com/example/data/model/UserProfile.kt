package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserPrivacy(
    val globalLocationSharing: Boolean = true,
    val ghostMode: Boolean = false,
    val friendsOnlyLocation: Boolean = false,
    val showPersonalPhone: Boolean = false,
    val showParentPhone: Boolean = false,
    val blockedFriendIds: List<String> = emptyList()
)

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val email: String = "",
    val avatar: String = "",
    val shsSchool: String = "",
    val course: String = "",
    val stream: String = "",
    val gradYear: Int = 2026,
    val personalPhone: String = "",
    val parentPhone: String = "",
    val city: String = "Accra",
    val country: String = "Ghana",
    val lat: Double = 5.6037,
    val lng: Double = -0.1870,
    val isOnline: Boolean = true,
    val isOnboarded: Boolean = false,
    val privacy: UserPrivacy = UserPrivacy(),
    @ServerTimestamp val lastActive: Timestamp? = null
)
