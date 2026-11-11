package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyaltyGold
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateDark950
import kotlinx.coroutines.launch

enum class RadiusFilter(val label: String, val maxKm: Double) {
    FIVE_KM("≤ 5 km", 5.0),
    TEN_KM("≤ 10 km", 10.0),
    TWENTY_FIVE_KM("≤ 25 km", 25.0),
    SAME_CITY("Same City", 100.0),
    ALL_REGIONS("All Regions", 20000.0)
}

@Composable
fun NearbyRadarScreen(
    currentUser: UserProfile,
    firestoreRepository: FirestoreRepository,
    onEnableSharing: () -> Unit,
    onConnectRequested: (UserProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedRadius by remember { mutableStateOf(RadiusFilter.ALL_REGIONS) }

    val allUsers by firestoreRepository.observeAllUsers().collectAsState(initial = emptyList())

    val isSharingEnabled = currentUser.privacy.globalLocationSharing && !currentUser.privacy.ghostMode

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark950)
            .padding(16.dp)
            .testTag("nearby_radar_screen")
    ) {
        // Radar Banner Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Radar,
                        contentDescription = "Radar",
                        tint = EmeraldGreen
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Nearby Alumni Radar",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Proximity sorting via Haversine calculation",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (!isSharingEnabled) {
            // Two-Way Privacy Gate Lock Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
                    .testTag("two_way_privacy_lock_card"),
                colors = CardDefaults.cardColors(containerColor = SlateDark900),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, RoyaltyGold.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(RoyaltyGold.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = RoyaltyGold,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Two-Way Privacy Gate Locked 🔒",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "To protect student & alumni safety, you cannot view nearby classmates unless you turn ON 'Nearby Location Sharing' for your own account.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onEnableSharing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("enable_nearby_sharing_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
                    ) {
                        Text(
                            text = "Enable Location Sharing to Unlock Radar",
                            fontWeight = FontWeight.Bold,
                            color = SlateDark950
                        )
                    }
                }
            }
        } else {
            // Radius Filters
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(RadiusFilter.entries) { radius ->
                    val isSelected = radius == selectedRadius
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedRadius = radius },
                        label = { Text(radius.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ElectricIndigo,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = SlateDark900,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("radius_${radius.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Nearby Users List calculated with Haversine formula
            val nearbyList = remember(allUsers, currentUser, selectedRadius) {
                allUsers.filter { u ->
                    if (u.userId == currentUser.userId) return@filter false
                    if (u.privacy.ghostMode) return@filter false
                    if (!u.privacy.globalLocationSharing && !u.privacy.friendsOnlyLocation) return@filter false
                    if (currentUser.privacy.blockedFriendIds.contains(u.userId)) return@filter false
                    val distKm = FirestoreRepository.calculateDistanceKm(
                        currentUser.lat, currentUser.lng, u.lat, u.lng
                    )
                    distKm <= selectedRadius.maxKm
                }.sortedBy { u ->
                    FirestoreRepository.calculateDistanceKm(currentUser.lat, currentUser.lng, u.lat, u.lng)
                }
            }

            if (nearbyList.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    colors = CardDefaults.cardColors(containerColor = SlateDark900)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No alumni found within ${selectedRadius.label}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try expanding your search radius to 'All Regions' or invite classmates to join Royals Connect!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(nearbyList) { user ->
                        val distKm = FirestoreRepository.calculateDistanceKm(
                            currentUser.lat, currentUser.lng, user.lat, user.lng
                        )
                        val distFormatted = FirestoreRepository.formatDistance(distKm)

                        NearbyUserCard(
                            user = user,
                            distanceStr = distFormatted,
                            onWaveClicked = {
                                Toast.makeText(context, "Waved 👋 to ${user.fullName}!", Toast.LENGTH_SHORT).show()
                            },
                            onConnectClicked = {
                                onConnectRequested(user)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyUserCard(
    user: UserProfile,
    distanceStr: String,
    onWaveClicked: () -> Unit,
    onConnectClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nearby_user_card_${user.userId}"),
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "📍 ${user.city} • ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = distanceStr,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Button(
                    onClick = onWaveClicked,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SlateDark800)
                ) {
                    Text("Wave 👋", fontSize = 11.sp)
                }

                Button(
                    onClick = onConnectClicked,
                    modifier = Modifier.height(36.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricIndigo)
                ) {
                    Text("Connect", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
