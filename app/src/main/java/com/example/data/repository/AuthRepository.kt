package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class AuthRepository(private val context: Context) {
    private val auth: FirebaseAuth = Firebase.auth
    private val credentialManager: CredentialManager = CredentialManager.create(context)

    private val _currentUserFlow = MutableStateFlow(auth.currentUser)
    val currentUserFlow: StateFlow<com.google.firebase.auth.FirebaseUser?> = _currentUserFlow.asStateFlow()

    init {
        auth.addAuthStateListener { firebaseAuth ->
            _currentUserFlow.value = firebaseAuth.currentUser
        }
    }

    fun getWebClientId(): String? {
        return try {
            context.getString(R.string.default_web_client_id)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Missing default_web_client_id resource", e)
            null
        }
    }

    fun attemptAutoSignIn(
        onSuccess: () -> Unit,
        onUnauthenticated: () -> Unit,
        scope: CoroutineScope
    ) {
        if (auth.currentUser != null) {
            onSuccess()
            return
        }
        val clientId = getWebClientId() ?: run {
            onUnauthenticated()
            return
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(true)
            .setServerClientId(clientId)
            .setAutoSelectEnabled(true)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onSuccess()
                } else {
                    onUnauthenticated()
                }
            } catch (e: Exception) {
                onUnauthenticated()
            }
        }
    }

    fun signInWithGoogle(
        activity: Activity,
        scope: CoroutineScope,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancelled: () -> Unit
    ) {
        val clientId = getWebClientId() ?: run {
            onError("Google Sign-In configuration missing in google-services.json")
            return
        }

        val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
        val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

        scope.launch {
            try {
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                    auth.signInWithCredential(authCredential).await()
                    onSuccess()
                } else {
                    onError("Unexpected credential format received.")
                }
            } catch (e: GetCredentialCancellationException) {
                Log.w("AuthRepository", "Google Sign-In flow cancelled: ${e.message}")
                onCancelled()
            } catch (e: Exception) {
                Log.e("AuthRepository", "Google Sign-In failed", e)
                onError(e.localizedMessage ?: "Authentication failed")
            }
        }
    }

    fun signOut(scope: CoroutineScope, onComplete: () -> Unit) {
        scope.launch {
            withContext(NonCancellable) {
                try {
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (e: Exception) {
                    Log.w("AuthRepository", "Failed to clear credential state: ${e.message}")
                } finally {
                    auth.signOut()
                    onComplete()
                }
            }
        }
    }
}
