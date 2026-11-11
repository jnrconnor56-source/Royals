package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.ChatMessage
import com.example.data.model.FriendRequest
import com.example.data.model.UserProfile
import com.example.data.model.UserPrivacy
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class FirestoreRepository(context: Context) {
    private val databaseId = context.getString(R.string.firestore_database_id)
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)
    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in to perform Firestore operations.")
    }

    // --- USER PROFILE ---
    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val docRef = db.collection("users").document(userId)
        docRef.snapshots().collect { snapshot ->
            val profile = snapshot.toObject<UserProfile>()
            emit(profile)
        }
    }.catch { e ->
        Log.e("FirestoreRepo", "Error observing user profile", e)
        emit(null)
    }

    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val snapshot = db.collection("users").document(userId).get().await()
            snapshot.toObject<UserProfile>()
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Error fetching profile", e)
            null
        }
    }

    suspend fun saveUserProfile(profile: UserProfile) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val payload = mapOf(
            "fullName" to profile.fullName,
            "username" to profile.username,
            "email" to profile.email,
            "avatar" to profile.avatar,
            "shsSchool" to profile.shsSchool,
            "course" to profile.course,
            "stream" to profile.stream,
            "gradYear" to profile.gradYear,
            "personalPhone" to profile.personalPhone,
            "parentPhone" to profile.parentPhone,
            "city" to profile.city,
            "country" to profile.country,
            "lat" to profile.lat,
            "lng" to profile.lng,
            "isOnline" to true,
            "isOnboarded" to true,
            "privacy" to mapOf(
                "globalLocationSharing" to profile.privacy.globalLocationSharing,
                "ghostMode" to profile.privacy.ghostMode,
                "friendsOnlyLocation" to profile.privacy.friendsOnlyLocation,
                "showPersonalPhone" to profile.privacy.showPersonalPhone,
                "showParentPhone" to profile.privacy.showParentPhone,
                "blockedFriendIds" to profile.privacy.blockedFriendIds
            ),
            "lastActive" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    suspend fun updatePrivacySettings(privacy: UserPrivacy) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val privacyMap = mapOf(
            "globalLocationSharing" to privacy.globalLocationSharing,
            "ghostMode" to privacy.ghostMode,
            "friendsOnlyLocation" to privacy.friendsOnlyLocation,
            "showPersonalPhone" to privacy.showPersonalPhone,
            "showParentPhone" to privacy.showParentPhone,
            "blockedFriendIds" to privacy.blockedFriendIds
        )
        docRef.update("privacy", privacyMap).await()
    }

    suspend fun toggleBlockFriend(friendId: String, currentBlocked: List<String>) {
        val updated = if (currentBlocked.contains(friendId)) {
            currentBlocked.filter { it != friendId }
        } else {
            currentBlocked + friendId
        }
        val uid = requireUserId()
        db.collection("users").document(uid).update("privacy.blockedFriendIds", updated).await()
    }

    // --- LIVE ALL USERS OBSERVER FOR MAP & NEARBY ---
    fun observeAllUsers(): Flow<List<UserProfile>> = flow {
        val query = db.collection("users")
        query.snapshots().collect { snapshot ->
            val users = snapshot.toObjects<UserProfile>()
            emit(users)
        }
    }.catch { e ->
        Log.e("FirestoreRepo", "Error observing users", e)
        emit(emptyList())
    }

    // --- CHANNELS & MESSAGES ---
    fun observeChannelMessages(channelId: String): Flow<List<ChatMessage>> = flow {
        val query = db.collection("channels")
            .document(channelId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
        
        query.snapshots().collect { snapshot ->
            val messages = snapshot.toObjects<ChatMessage>()
            emit(messages)
        }
    }.catch { e ->
        Log.e("FirestoreRepo", "Error observing messages for channel $channelId", e)
        emit(emptyList())
    }

    suspend fun sendMessage(
        channelId: String,
        content: String,
        isInTown: Boolean,
        senderProfile: UserProfile
    ) {
        val uid = requireUserId()
        val docRef = db.collection("channels").document(channelId).collection("messages").document()
        val payload = mapOf(
            "channelId" to channelId,
            "senderId" to uid,
            "senderName" to senderProfile.fullName,
            "senderAvatar" to senderProfile.avatar,
            "senderStream" to "${senderProfile.shsSchool} (${senderProfile.stream})",
            "content" to content,
            "isInTown" to isInTown,
            "reactions" to emptyMap<String, List<String>>(),
            "timestamp" to FieldValue.serverTimestamp()
        )
        docRef.set(payload).await()
    }

    // --- FRIEND REQUESTS & 1-MESSAGE PROTOCOL ---
    suspend fun sendFriendRequest(
        receiverId: String,
        receiverName: String,
        note: String,
        senderProfile: UserProfile
    ): Result<Unit> {
        val uid = requireUserId()
        if (uid == receiverId) {
            return Result.failure(IllegalArgumentException("You cannot connect with yourself."))
        }
        if (note.length > 280) {
            return Result.failure(IllegalArgumentException("Initial note cannot exceed 280 characters."))
        }

        try {
            // Check existing pending request
            val existing = db.collection("friendRequests")
                .whereEqualTo("senderId", uid)
                .whereEqualTo("receiverId", receiverId)
                .get()
                .await()
            
            if (!existing.isEmpty) {
                val req = existing.documents.first().toObject<FriendRequest>()
                if (req?.status == "pending") {
                    return Result.failure(IllegalStateException("1-Message Protocol: A request is already pending for this alumnus."))
                }
            }

            val reqRef = db.collection("friendRequests").document()
            val payload = mapOf(
                "senderId" to uid,
                "senderName" to senderProfile.fullName,
                "senderAvatar" to senderProfile.avatar,
                "senderSchool" to senderProfile.shsSchool,
                "senderStream" to senderProfile.stream,
                "receiverId" to receiverId,
                "receiverName" to receiverName,
                "initialNote" to note,
                "status" to "pending",
                "timestamp" to FieldValue.serverTimestamp()
            )
            reqRef.set(payload).await()
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FirestoreRepo", "Failed to send friend request", e)
            return Result.failure(e)
        }
    }

    fun observeFriendRequests(userId: String): Flow<List<FriendRequest>> = flow {
        val query = db.collection("friendRequests")
        query.snapshots().collect { snapshot ->
            val allRequests = snapshot.toObjects<FriendRequest>()
            val userReqs = allRequests.filter { it.senderId == userId || it.receiverId == userId }
            emit(userReqs)
        }
    }.catch { e ->
        Log.e("FirestoreRepo", "Error observing friend requests", e)
        emit(emptyList())
    }

    suspend fun respondToFriendRequest(requestId: String, accept: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("friendRequests").document(requestId)
        val status = if (accept) "accepted" else "declined"
        docRef.update("status", status).await()

        if (accept) {
            val reqSnap = docRef.get().await()
            val req = reqSnap.toObject<FriendRequest>()
            if (req != null) {
                val friendshipRef = db.collection("friends").document("${req.senderId}_${req.receiverId}")
                val friendshipPayload = mapOf(
                    "user1Id" to req.senderId,
                    "user2Id" to req.receiverId,
                    "timestamp" to FieldValue.serverTimestamp()
                )
                friendshipRef.set(friendshipPayload).await()
            }
        }
    }

    // --- HAVERSINE DISTANCE CALCULATOR ---
    companion object {
        fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 6371.0 // Earth radius in kilometers
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return r * c
        }

        fun formatDistance(km: Double): String {
            return if (km < 1.0) {
                val meters = (km * 1000).toInt()
                "$meters m away"
            } else {
                val formatted = String.format("%.1f", km)
                "$formatted km away"
            }
        }
    }
}
