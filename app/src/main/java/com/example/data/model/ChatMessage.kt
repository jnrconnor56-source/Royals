package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class ChatMessage(
    @DocumentId val id: String = "",
    val channelId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderAvatar: String = "",
    val senderStream: String = "",
    val content: String = "",
    val isInTown: Boolean = false,
    val reactions: Map<String, List<String>> = emptyMap(),
    @ServerTimestamp val timestamp: Timestamp? = null
)
