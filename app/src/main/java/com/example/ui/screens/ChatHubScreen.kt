package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Badge
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyaltyGold
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateDark950
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ChatHubScreen(
    currentUser: UserProfile,
    firestoreRepository: FirestoreRepository,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedChannelIndex by remember { mutableStateOf(0) }

    val schoolChannelId = remember(currentUser.shsSchool) {
        "school_${currentUser.shsSchool.lowercase().replace(" ", "_")}"
    }
    val streamChannelId = remember(currentUser.shsSchool, currentUser.stream) {
        "stream_${currentUser.shsSchool}_${currentUser.stream}".lowercase().replace(" ", "_")
    }

    val activeChannelId = if (selectedChannelIndex == 0) schoolChannelId else streamChannelId
    val activeChannelTitle = if (selectedChannelIndex == 0) {
        "${currentUser.shsSchool} Official Network"
    } else {
        "${currentUser.stream} • Class of ${currentUser.gradYear}"
    }

    val messages by firestoreRepository.observeChannelMessages(activeChannelId)
        .collectAsState(initial = emptyList())

    var messageText by remember { mutableStateOf("") }
    var isInTownFlag by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark950)
            .testTag("chat_hub_screen")
    ) {
        // Channel Tabs Header
        TabRow(
            selectedTabIndex = selectedChannelIndex,
            containerColor = SlateDark900,
            contentColor = MaterialTheme.colorScheme.onSurface,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedChannelIndex]),
                    color = ElectricIndigo
                )
            }
        ) {
            Tab(
                selected = selectedChannelIndex == 0,
                onClick = { selectedChannelIndex = 0 },
                text = {
                    Text(
                        text = "School Channel",
                        fontWeight = if (selectedChannelIndex == 0) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("channel_tab_school")
            )
            Tab(
                selected = selectedChannelIndex == 1,
                onClick = { selectedChannelIndex = 1 },
                text = {
                    Text(
                        text = "Stream Channel",
                        fontWeight = if (selectedChannelIndex == 1) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                },
                modifier = Modifier.testTag("channel_tab_stream")
            )
        }

        // Active Channel Sub-Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SlateDark900)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(RoyaltyGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = RoyaltyGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = activeChannelTitle,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Official live feed • ${messages.size} messages",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Message Feed List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (messages.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateDark900)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = " 👋 Welcome to $activeChannelTitle!",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Be the first alumnus to post a message or broadcast 'In Town 📍' status to fellow Royals!",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(messages) { msg ->
                    val isSelf = msg.senderId == currentUser.userId
                    ChatMessageCard(message = msg, isSelf = isSelf)
                }
            }
        }

        // Message Input Bar
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = SlateDark900),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Message $activeChannelTitle...") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SlateDark950,
                            unfocusedContainerColor = SlateDark950
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                val textToSend = messageText
                                messageText = ""
                                val town = isInTownFlag
                                isInTownFlag = false
                                coroutineScope.launch {
                                    firestoreRepository.sendMessage(
                                        channelId = activeChannelId,
                                        content = textToSend,
                                        isInTown = town,
                                        senderProfile = currentUser
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ElectricIndigo)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send Message",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // In Town Toggle Pill
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isInTownFlag) EmeraldGreen.copy(alpha = 0.2f) else SlateDark800)
                        .clickable { isInTownFlag = !isInTownFlag }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "In Town",
                        tint = if (isInTownFlag) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isInTownFlag) "In Town Flag Attached 📍" else "Attach 'In Town 📍' Flag",
                        fontSize = 11.sp,
                        fontWeight = if (isInTownFlag) FontWeight.Bold else FontWeight.Medium,
                        color = if (isInTownFlag) EmeraldGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageCard(message: ChatMessage, isSelf: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isSelf) Arrangement.End else Arrangement.Start
    ) {
        if (!isSelf) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ElectricIndigo),
                contentAlignment = Alignment.Center
            ) {
                val initials = message.senderName.take(2).uppercase()
                Text(text = initials, color = MaterialTheme.colorScheme.onPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(0.82f),
            colors = CardDefaults.cardColors(
                containerColor = if (isSelf) ElectricIndigo.copy(alpha = 0.3f) else SlateDark900
            ),
            shape = RoundedCornerShape(16.dp),
            border = if (isSelf) androidx.compose.foundation.BorderStroke(1.dp, ElectricIndigo) else null
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = message.senderName.ifEmpty { "Alumnus" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isSelf) RoyaltyGold else MaterialTheme.colorScheme.onBackground
                    )
                    val dateStr = message.timestamp?.toDate()?.let {
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(it)
                    } ?: "Just now"
                    Text(
                        text = dateStr,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (message.senderStream.isNotBlank()) {
                    Text(
                        text = message.senderStream,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.content,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (message.isInTown) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldGreen.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📍 In Town Broadcast",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }
    }
}
