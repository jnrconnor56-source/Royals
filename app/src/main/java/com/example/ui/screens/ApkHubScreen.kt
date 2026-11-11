package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserProfile
import com.example.data.model.UserPrivacy
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreRepository
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.RoyaltyGold
import com.example.ui.theme.SlateDark800
import com.example.ui.theme.SlateDark900
import com.example.ui.theme.SlateDark950
import kotlinx.coroutines.launch

@Composable
fun ApkHubScreen(
    currentUser: UserProfile,
    firestoreRepository: FirestoreRepository,
    authRepository: AuthRepository,
    onEditProfileClicked: () -> Unit,
    onSignOutClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var privacy by remember(currentUser) { mutableStateOf(currentUser.privacy) }

    val updatePrivacy = { newPrivacy: UserPrivacy ->
        privacy = newPrivacy
        coroutineScope.launch {
            firestoreRepository.updatePrivacySettings(newPrivacy)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SlateDark950)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("apk_hub_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateDark900),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ElectricIndigo),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.fullName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser.fullName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${currentUser.shsSchool} • ${currentUser.stream}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Class of ${currentUser.gradYear} • 📍 ${currentUser.city}, ${currentUser.country}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onEditProfileClicked,
                    modifier = Modifier.testTag("edit_profile_button")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile", tint = RoyaltyGold)
                }
            }
        }

        // Granular Privacy Settings Panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateDark900),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Granular Safety & Privacy Settings",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. Global Location Sharing
                PrivacyToggleRow(
                    title = "Public / Global Location Sharing",
                    description = "Master toggle allowing your city pin to appear on the live global map.",
                    checked = privacy.globalLocationSharing,
                    onCheckedChange = { updatePrivacy(privacy.copy(globalLocationSharing = it)) },
                    testTag = "toggle_global_location"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Ghost Mode
                PrivacyToggleRow(
                    title = "Ghost Mode Stealth Cloak 👻",
                    description = "Instantly hides your location from all maps and radar screens.",
                    checked = privacy.ghostMode,
                    onCheckedChange = { updatePrivacy(privacy.copy(ghostMode = it)) },
                    testTag = "toggle_ghost_mode"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Friends-Only Location
                PrivacyToggleRow(
                    title = "Friends-Only Location Mode",
                    description = "Restricts pin visibility strictly to confirmed alumni friends.",
                    checked = privacy.friendsOnlyLocation,
                    onCheckedChange = { updatePrivacy(privacy.copy(friendsOnlyLocation = it)) },
                    testTag = "toggle_friends_only"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Personal Phone Visibility
                PrivacyToggleRow(
                    title = "Show Personal Phone Number",
                    description = "Masked as 'Hidden by User' unless enabled & confirmed friend.",
                    checked = privacy.showPersonalPhone,
                    onCheckedChange = { updatePrivacy(privacy.copy(showPersonalPhone = it)) },
                    testTag = "toggle_personal_phone"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // 5. Parent Phone Visibility
                PrivacyToggleRow(
                    title = "Show Parent / Emergency Phone",
                    description = "Masked as 'Hidden by User' unless enabled & confirmed friend.",
                    checked = privacy.showParentPhone,
                    onCheckedChange = { updatePrivacy(privacy.copy(showParentPhone = it)) },
                    testTag = "toggle_parent_phone"
                )
            }
        }

        // Capacitor & Native APK Export Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = SlateDark900),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Android, contentDescription = null, tint = ElectricIndigo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Capacitor 6 & Android APK Hub",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "App ID: com.royalsconnect.app\nWebDir: dist • Scheme: https\nSigned APK Target: release / debugConfig",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Build Native APK via AI Studio Top Bar → Export → APK / AAB Bundle or run standard Gradle assembleDebug.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Sign Out Button
        OutlinedButton(
            onClick = {
                authRepository.signOut(coroutineScope) {
                    onSignOutClicked()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("sign_out_button"),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Sign Out of Royals Connect", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PrivacyToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SlateDark950,
                checkedTrackColor = EmeraldGreen,
                uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                uncheckedTrackColor = SlateDark800
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
