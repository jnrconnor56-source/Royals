package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
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
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.BottomNavBar
import com.example.ui.components.NavigationTab
import com.example.ui.components.OnboardingModal
import com.example.ui.components.SystemStatusBarHeader
import com.example.ui.screens.ApkHubScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatHubScreen
import com.example.ui.screens.FindFriendsScreen
import com.example.ui.screens.GlobalMapScreen
import com.example.ui.screens.NearbyRadarScreen
import com.example.ui.theme.RoyalsConnectTheme
import com.example.ui.theme.SlateDark950
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val authRepository = AuthRepository(applicationContext)
        val firestoreRepository = FirestoreRepository(applicationContext)

        setContent {
            RoyalsConnectTheme {
                RoyalsConnectApp(
                    authRepository = authRepository,
                    firestoreRepository = firestoreRepository
                )
            }
        }
    }
}

@Composable
fun RoyalsConnectApp(
    authRepository: AuthRepository,
    firestoreRepository: FirestoreRepository
) {
    val coroutineScope = rememberCoroutineScope()
    val authUser by authRepository.currentUserFlow.collectAsState()

    if (authUser == null) {
        AuthScreen(
            authRepository = authRepository,
            onAuthSuccess = {}
        )
    } else {
        val uid = authUser!!.uid
        val liveProfile by firestoreRepository.observeUserProfile(uid).collectAsState(initial = null)

        var showOnboarding by remember { mutableStateOf(false) }

        LaunchedEffect(liveProfile) {
            if (liveProfile == null) {
                val newProfile = UserProfile(
                    userId = uid,
                    fullName = authUser?.displayName ?: "SHS Alumnus",
                    email = authUser?.email ?: "",
                    avatar = authUser?.photoUrl?.toString() ?: "",
                    isOnboarded = false
                )
                firestoreRepository.saveUserProfile(newProfile)
                showOnboarding = true
            } else if (!liveProfile!!.isOnboarded) {
                showOnboarding = true
            }
        }

        val currentProfile = liveProfile ?: UserProfile(
            userId = uid,
            fullName = authUser?.displayName ?: "SHS Alumnus",
            email = authUser?.email ?: ""
        )

        if (showOnboarding) {
            OnboardingModal(
                existingProfile = currentProfile,
                onComplete = { updated ->
                    showOnboarding = false
                    coroutineScope.launch {
                        firestoreRepository.saveUserProfile(updated)
                    }
                }
            )
        }

        var currentTab by remember { mutableStateOf(NavigationTab.CHAT) }
        var targetUserForConnectDialog by remember { mutableStateOf<UserProfile?>(null) }

        BackHandler(enabled = currentTab != NavigationTab.CHAT) {
            currentTab = NavigationTab.CHAT
        }

        Scaffold(
            topBar = {
                SystemStatusBarHeader()
            },
            bottomBar = {
                BottomNavBar(
                    selectedTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            },
            containerColor = SlateDark950
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(SlateDark950)
            ) {
                when (currentTab) {
                    NavigationTab.CHAT -> {
                        ChatHubScreen(
                            currentUser = currentProfile,
                            firestoreRepository = firestoreRepository
                        )
                    }
                    NavigationTab.NEARBY -> {
                        NearbyRadarScreen(
                            currentUser = currentProfile,
                            firestoreRepository = firestoreRepository,
                            onEnableSharing = {
                                val updatedPrivacy = currentProfile.privacy.copy(
                                    globalLocationSharing = true,
                                    ghostMode = false
                                )
                                coroutineScope.launch {
                                    firestoreRepository.updatePrivacySettings(updatedPrivacy)
                                }
                            },
                            onConnectRequested = { target ->
                                targetUserForConnectDialog = target
                                currentTab = NavigationTab.FRIENDS
                            }
                        )
                    }
                    NavigationTab.MAP -> {
                        GlobalMapScreen(
                            currentUser = currentProfile,
                            firestoreRepository = firestoreRepository,
                            onConnectRequested = { target ->
                                targetUserForConnectDialog = target
                                currentTab = NavigationTab.FRIENDS
                            }
                        )
                    }
                    NavigationTab.FRIENDS -> {
                        FindFriendsScreen(
                            currentUser = currentProfile,
                            firestoreRepository = firestoreRepository,
                            targetUserForDialog = targetUserForConnectDialog,
                            onDialogClosed = { targetUserForConnectDialog = null }
                        )
                    }
                    NavigationTab.APK -> {
                        ApkHubScreen(
                            currentUser = currentProfile,
                            firestoreRepository = firestoreRepository,
                            authRepository = authRepository,
                            onEditProfileClicked = { showOnboarding = true },
                            onSignOutClicked = {}
                        )
                    }
                }
            }
        }
    }
}
