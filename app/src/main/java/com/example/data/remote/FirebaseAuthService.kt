package com.example.data.remote

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.config.AppConfig
import com.example.model.User
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

sealed class AuthResult<out T> {
    data class Success<out T>(val data: T) : AuthResult<T>()
    data class Error(val message: String) : AuthResult<Nothing>()
    object Loading : AuthResult<Nothing>()
}

class FirebaseAuthService {

    private val auth: FirebaseAuth?
        get() = try { FirebaseAuth.getInstance() } catch (_: Exception) { null }

    private val firestore: FirebaseFirestore?
        get() = try { FirebaseFirestore.getInstance() } catch (_: Exception) { null }

    val currentUser: FirebaseUser?
        get() = try { auth?.currentUser } catch (_: Exception) { null }

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val currentAuth = auth
        if (currentAuth == null) {
            trySend(null)
            awaitClose { }
            return@callbackFlow
        }
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        try {
            currentAuth.addAuthStateListener(listener)
        } catch (_: Exception) {
            trySend(null)
        }
        awaitClose {
            try {
                currentAuth.removeAuthStateListener(listener)
            } catch (_: Exception) {}
        }
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): AuthResult<User> {
        val currentAuth = auth ?: return AuthResult.Error("خدمة المصادقة غير مهيأة")
        return try {
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return AuthResult.Error("يرجى إدخال بريد إلكتروني صالح")
            }
            if (password.length < 6) {
                return AuthResult.Error("كلمة المرور يجب أن لا تقل عن 6 أحرف أو أرقام")
            }
            if (name.isBlank()) {
                return AuthResult.Error("يرجى إدخال اسم المستخدم")
            }

            val authResult = currentAuth.createUserWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: return AuthResult.Error("فشل إنشاء الحساب")

            val role = if (email.trim().equals(AppConfig.ADMIN_EMAIL, ignoreCase = true)) "admin" else "user"
            val user = User(
                uid = firebaseUser.uid,
                name = name.trim(),
                email = email.trim(),
                photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                role = role,
                createdAt = System.currentTimeMillis()
            )

            try {
                firestore?.collection("users")?.document(user.uid)?.set(user)?.await()
            } catch (_: Exception) {}

            AuthResult.Success(user)
        } catch (e: Exception) {
            val message = parseAuthErrorMessage(e)
            AuthResult.Error(message)
        }
    }

    suspend fun loginWithEmail(email: String, password: String): AuthResult<User> {
        val currentAuth = auth ?: return AuthResult.Error("خدمة المصادقة غير مهيأة")
        return try {
            if (email.isBlank() || password.isBlank()) {
                return AuthResult.Error("يرجى ملء جميع الحقول")
            }

            val authResult = currentAuth.signInWithEmailAndPassword(email.trim(), password).await()
            val firebaseUser = authResult.user ?: return AuthResult.Error("فشل تسجيل الدخول")

            val user = getUserProfile(firebaseUser.uid) ?: run {
                val role = if (email.trim().equals(AppConfig.ADMIN_EMAIL, ignoreCase = true)) "admin" else "user"
                User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: email.substringBefore("@"),
                    email = firebaseUser.email ?: email.trim(),
                    photoUrl = firebaseUser.photoUrl?.toString() ?: "",
                    role = role
                )
            }

            AuthResult.Success(user)
        } catch (e: Exception) {
            val message = parseAuthErrorMessage(e)
            AuthResult.Error(message)
        }
    }

    suspend fun sendPasswordReset(email: String): AuthResult<Unit> {
        val currentAuth = auth ?: return AuthResult.Error("خدمة المصادقة غير مهيأة")
        return try {
            if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                return AuthResult.Error("يرجى إدخال بريد إلكتروني صالح")
            }
            currentAuth.sendPasswordResetEmail(email.trim()).await()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(parseAuthErrorMessage(e))
        }
    }

    suspend fun signInWithGoogle(context: Context, serverClientId: String? = null): AuthResult<User> {
        val currentAuth = auth ?: return AuthResult.Error("خدمة المصادقة غير مهيأة")
        return try {
            val credentialManager = CredentialManager.create(context)

            val clientId = serverClientId?.ifBlank { null } ?: run {
                val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
                if (resId != 0) context.getString(resId) else "104890768616-google-web-client-id.apps.googleusercontent.com"
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId(clientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result: GetCredentialResponse = credentialManager.getCredential(
                context = context,
                request = request
            )

            val credential = result.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = currentAuth.signInWithCredential(authCredential).await()
                val firebaseUser = authResult.user ?: return AuthResult.Error("تعذر تسجيل الدخول بحساب Google")

                val role = if (firebaseUser.email?.equals(AppConfig.ADMIN_EMAIL, ignoreCase = true) == true) "admin" else "user"
                val user = User(
                    uid = firebaseUser.uid,
                    name = firebaseUser.displayName ?: googleIdTokenCredential.displayName ?: "مستخدم Google",
                    email = firebaseUser.email ?: googleIdTokenCredential.id,
                    photoUrl = firebaseUser.photoUrl?.toString() ?: googleIdTokenCredential.profilePictureUri?.toString() ?: "",
                    role = role
                )

                try {
                    firestore?.collection("users")?.document(user.uid)?.set(user)?.await()
                } catch (_: Exception) {}

                AuthResult.Success(user)
            } else {
                AuthResult.Error("نوع بيانات الاعتماد غير مدعوم")
            }
        } catch (e: GetCredentialCancellationException) {
            AuthResult.Error("تم إلغاء عملية تسجيل الدخول")
        } catch (e: Exception) {
            val errorMsg = e.message ?: ""
            if (errorMsg.contains("No credentials available", ignoreCase = true) || errorMsg.contains("Developer console is not set up", ignoreCase = true)) {
                // If Play Services has no accounts registered yet or test environment
                AuthResult.Error("لم يتم العثور على حساب Google مسجل في الجهاز أو جاري ضبط خدمات Google")
            } else {
                AuthResult.Error(parseAuthErrorMessage(e))
            }
        }
    }

    suspend fun getUserProfile(uid: String): User? {
        val db = firestore ?: return currentUser?.let { fbUser ->
            val email = fbUser.email ?: ""
            val role = if (email.equals(AppConfig.ADMIN_EMAIL, ignoreCase = true)) "admin" else "user"
            User(
                uid = uid,
                name = fbUser.displayName ?: "مستخدم",
                email = email,
                photoUrl = fbUser.photoUrl?.toString() ?: "",
                role = role
            )
        }
        return try {
            val doc = db.collection("users").document(uid).get().await()
            if (doc.exists()) {
                val role = doc.getString("role") ?: "user"
                val email = doc.getString("email") ?: ""
                val effectiveRole = if (email.equals(AppConfig.ADMIN_EMAIL, ignoreCase = true)) "admin" else role
                User(
                    uid = uid,
                    name = doc.getString("name") ?: "مستخدم",
                    email = email,
                    photoUrl = doc.getString("photoUrl") ?: "",
                    role = effectiveRole,
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else {
                currentUser?.let { fbUser ->
                    val email = fbUser.email ?: ""
                    val role = if (email.equals(AppConfig.ADMIN_EMAIL, ignoreCase = true)) "admin" else "user"
                    User(
                        uid = uid,
                        name = fbUser.displayName ?: "مستخدم",
                        email = email,
                        photoUrl = fbUser.photoUrl?.toString() ?: "",
                        role = role
                    )
                }
            }
        } catch (e: Exception) {
            currentUser?.let { fbUser ->
                val email = fbUser.email ?: ""
                val role = if (email.equals(AppConfig.ADMIN_EMAIL, ignoreCase = true)) "admin" else "user"
                User(
                    uid = uid,
                    name = fbUser.displayName ?: "مستخدم",
                    email = email,
                    photoUrl = fbUser.photoUrl?.toString() ?: "",
                    role = role
                )
            }
        }
    }

    suspend fun signOut(context: Context) {
        try {
            auth?.signOut()
            val credentialManager = CredentialManager.create(context)
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (_: Exception) {}
    }

    private fun parseAuthErrorMessage(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("The email address is badly formatted", ignoreCase = true) -> "صيغة البريد الإلكتروني غير صحيحة"
            msg.contains("The email address is already in use", ignoreCase = true) -> "البريد الإلكتروني مسجل مسبقاً"
            msg.contains("There is no user record", ignoreCase = true) || msg.contains("user-not-found", ignoreCase = true) -> "الحساب غير موجود، يرجى إنشاء حساب جديد"
            msg.contains("wrong-password", ignoreCase = true) || msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) -> "كلمة المرور غير صحيحة"
            msg.contains("network-request-failed", ignoreCase = true) -> "تعذر الاتصال بالشبكة، تحقق من اتصالك بالإنترنت"
            msg.contains("too-many-requests", ignoreCase = true) -> "محاولات كثيرة جداً، يرجى المحاولة لاحقاً"
            else -> "حدث خطأ: ${e.localizedMessage ?: "يرجى المحاولة مرة أخرى"}"
        }
    }
}
