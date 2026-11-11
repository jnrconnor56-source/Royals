package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class FriendRequest(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val senderSchool: String = "",
    val senderStream: String = "",
    val receiverId: String = "",
    val receiverName: String = "",
    val initialNote: String = "",
    val status: String = "pending", // "pending", "accepted", "declined"
    @ServerTimestamp val timestamp: Timestamp? = null
)

data class SchoolChannel(
    val id: String,
    val name: String,
    val type: String, // "SCHOOL" or "STREAM"
    val description: String,
    val unreadCount: Int = 0
)
