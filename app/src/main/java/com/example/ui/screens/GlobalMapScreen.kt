package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.InteractiveMapCanvas
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateDark950

@Composable
fun GlobalMapScreen(
    currentUser: UserProfile,
    firestoreRepository: FirestoreRepository,
    onConnectRequested: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val users by firestoreRepository.observeAllUsers().collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserForDetails by remember { mutableStateOf<UserProfile?>(null) }

    val filteredUsers = remember(users, searchQuery) {
        if (searchQuery.isBlank()) users
        else users.filter { u ->
            u.fullName.contains(searchQuery, ignoreCase = true) ||
                    u.shsSchool.contains(searchQuery, ignoreCase = true) ||
                    u.stream.contains(searchQuery, ignoreCase = true) ||
                    u.city.contains(searchQuery, ignoreCase = true)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("global_map_screen")
    ) {
        // Map Engine
        InteractiveMapCanvas(
            users = filteredUsers,
            currentUser = currentUser,
            onUserSelected = { user ->
                selectedUserForDetails = user
            }
        )

        // Floating Search Bar at top
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            shape = RoundedCornerShape(24.dp),
            color = SlateDark900.copy(alpha = 0.95f),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search classmates by school, stream, city...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("map_search_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedBorderColor = androidx.compose.ui.graphics.Color.Transparent
                    )
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            }
        }

        // Classmate Detail Dialog Popup on marker click
        selectedUserForDetails?.let { user ->
            Dialog(onDismissRequest = { selectedUserForDetails = null }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .testTag("classmate_detail_dialog"),
                    colors = CardDefaults.cardColors(containerColor = SlateDark900),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ElectricIndigo),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.fullName.take(2).uppercase(),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = user.fullName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${user.shsSchool} (${user.stream})",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Class of ${user.gradYear} • 📍 ${user.city}, ${user.country}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (user.userId != currentUser.userId) {
                            Button(
                                onClick = {
                                    val target = selectedUserForDetails
                                    selectedUserForDetails = null
                                    target?.let { onConnectRequested(it) }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("map_dialog_connect_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                            ) {
                                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send 1-Message Request ✉️", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
