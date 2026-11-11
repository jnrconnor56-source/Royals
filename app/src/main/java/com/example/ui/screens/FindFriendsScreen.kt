package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.FriendRequest
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyaltyGold
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateDark950
import kotlinx.coroutines.launch

@Composable
fun FindFriendsScreen(
    currentUser: UserProfile,
    firestoreRepository: FirestoreRepository,
    targetUserForDialog: UserProfile? = null,
    onDialogClosed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedSubTab by remember { mutableStateOf(0) } // 0: Discover, 1: Inbox Requests

    var searchQuery by remember { mutableStateOf("") }
    val allUsers by firestoreRepository.observeAllUsers().collectAsState(initial = emptyList())
    val friendRequests by firestoreRepository.observeFriendRequests(currentUser.userId)
        .collectAsState(initial = emptyList())

    var requestTargetUser by remember { mutableStateOf<UserProfile?>(targetUserForDialog) }
    var initialNoteText by remember { mutableStateOf("") }

    LaunchedEffect(targetUserForDialog) {
        requestTargetUser = targetUserForDialog
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark950)
            .testTag("find_friends_screen")
    ) {
        // Navigation Tabs
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = SlateDark900,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedSubTab]),
                    color = ElectricIndigo
                )
            }
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Discover Alumni", fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.testTag("friends_tab_discover")
            )
            val pendingIncomingCount = friendRequests.count { it.receiverId == currentUser.userId && it.status == "pending" }
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = {
                    Text(
                        text = if (pendingIncomingCount > 0) "Requests Inbox ($pendingIncomingCount)" else "Requests Inbox",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("friends_tab_inbox")
            )
        }

        if (selectedSubTab == 0) {
            // Discover Alumni Tab
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, school, stream...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alumni_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SlateDark900,
                        unfocusedContainerColor = SlateDark900
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val alumniList = remember(allUsers, searchQuery, currentUser) {
                    allUsers.filter { u ->
                        u.userId != currentUser.userId &&
                                (searchQuery.isBlank() ||
                                        u.fullName.contains(searchQuery, ignoreCase = true) ||
                                        u.shsSchool.contains(searchQuery, ignoreCase = true) ||
                                        u.stream.contains(searchQuery, ignoreCase = true))
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(alumniList) { user ->
                        AlumniCard(
                            user = user,
                            onConnectClicked = { requestTargetUser = user }
                        )
                    }
                }
            }
        } else {
            // Inbox Requests Tab
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Incoming Connection Requests",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val incomingReqs = friendRequests.filter { it.receiverId == currentUser.userId }
                if (incomingReqs.isEmpty()) {
                    item {
                        Text(
                            text = "No connection requests received yet.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(incomingReqs) { req ->
                        IncomingRequestCard(
                            request = req,
                            onAccept = {
                                coroutineScope.launch {
                                    firestoreRepository.respondToFriendRequest(req.id, true)
                                    Toast.makeText(context, "Connection accepted!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onDecline = {
                                coroutineScope.launch {
                                    firestoreRepository.respondToFriendRequest(req.id, false)
                                }
                            }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Outgoing Connection Requests",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                val outgoingReqs = friendRequests.filter { it.senderId == currentUser.userId }
                if (outgoingReqs.isEmpty()) {
                    item {
                        Text(
                            text = "No outgoing requests sent.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(outgoingReqs) { req ->
                        OutgoingRequestCard(request = req)
                    }
                }
            }
        }

        // 1-Message Request Protocol Modal
        requestTargetUser?.let { targetUser ->
            Dialog(onDismissRequest = {
                requestTargetUser = null
                onDialogClosed()
            }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("one_message_request_modal"),
                    colors = CardDefaults.cardColors(containerColor = SlateDark900),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "1-Message Connection Protocol ✉️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "You can attach EXACTLY ONE initial note to ${targetUser.fullName}. Further messaging is locked until accepted.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = initialNoteText,
                            onValueChange = { if (it.length <= 280) initialNoteText = it },
                            placeholder = { Text("Write your 1-message connection note...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("one_message_note_input"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = SlateDark950,
                                unfocusedContainerColor = SlateDark950
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "${initialNoteText.length}/280 characters",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.End)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = {
                                    requestTargetUser = null
                                    onDialogClosed()
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = SlateDark800)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    val note = initialNoteText
                                    val target = targetUser
                                    requestTargetUser = null
                                    onDialogClosed()
                                    coroutineScope.launch {
                                        val res = firestoreRepository.sendFriendRequest(
                                            receiverId = target.userId,
                                            receiverName = target.fullName,
                                            note = note,
                                            senderProfile = currentUser
                                        )
                                        if (res.isSuccess) {
                                            Toast.makeText(context, "Request sent!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, res.exceptionOrNull()?.message ?: "Error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("send_one_message_request_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                            ) {
                                Text("Send Request", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AlumniCard(user: UserProfile, onConnectClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alumni_card_${user.userId}"),
        colors = CardDefaults.cardColors(containerColor = SlateDark900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.fullName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${user.shsSchool} (${user.stream})",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "📍 ${user.city}, ${user.country}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onConnectClicked,
                modifier = Modifier.testTag("connect_btn_${user.userId}"),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun IncomingRequestCard(request: FriendRequest, onAccept: () -> Unit, onDecline: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDark900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = request.senderName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(RoyaltyGold.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = request.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyaltyGold
                    )
                }
            }

            Text(
                text = "${request.senderSchool} (${request.senderStream})",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.primary
            )

            if (request.initialNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(SlateDark950)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "\"${request.initialNote}\"",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (request.status == "pending") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onDecline,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = SlateDark800)
                    ) {
                        Text("Decline")
                    }
                    Button(
                        onClick = onAccept,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text("Accept", fontWeight = FontWeight.Bold, color = SlateDark950)
                    }
                }
            }
        }
    }
}

@Composable
fun OutgoingRequestCard(request: FriendRequest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SlateDark900),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "To: ${request.receiverName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Status: ${request.status}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (request.status == "accepted") EmeraldGreen else RoyaltyGold
                )
            }
            if (request.initialNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Note: ${request.initialNote}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
