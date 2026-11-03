package com.example.data.repository

import android.util.Log
import com.example.data.firebase.FirebaseManager
import com.example.domain.model.Usuario
import com.example.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseAuthRepository(
    private val auth: FirebaseAuth? = FirebaseManager.auth,
    private val firestore: FirebaseFirestore? = FirebaseManager.firestore
) : AuthRepository {

    companion object {
        private const val TAG = "FirebaseAuthRepository"
    }

    override val currentUserFlow: Flow<Usuario?> = callbackFlow {
        val authInstance = auth
        if (authInstance == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        var firestoreListener: ListenerRegistration? = null

        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            firestoreListener?.remove()
            firestoreListener = null

            val user = firebaseAuth.currentUser
            if (user != null) {
                val db = firestore
                if (db != null) {
                    firestoreListener = db.collection("usuarios").document(user.uid)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.w(TAG, "SnapshotListener notice: ${error.message}")
                                trySend(
                                    Usuario(
                                        uid = user.uid,
                                        correo = user.email ?: "",
                                        nombre = user.displayName ?: "Usuario",
                                        tipoUsuario = "comprador"
                                    )
                                )
                                return@addSnapshotListener
                            }
                            if (snapshot != null && snapshot.exists()) {
                                val usuario = snapshot.toObject(Usuario::class.java)
                                trySend(usuario)
                            } else {
                                // Fallback si aún no se termina de escribir en Firestore
                                trySend(
                                    Usuario(
                                        uid = user.uid,
                                        correo = user.email ?: "",
                                        nombre = user.displayName ?: "Usuario",
                                        tipoUsuario = "comprador"
                                    )
                                )
                            }
                        }
                } else {
                    trySend(
                        Usuario(
                            uid = user.uid,
                            correo = user.email ?: "",
                            nombre = user.displayName ?: "Usuario"
                        )
                    )
                }
            } else {
                trySend(null)
            }
        }

        authInstance.addAuthStateListener(authStateListener)
        awaitClose {
            authInstance.removeAuthStateListener(authStateListener)
            firestoreListener?.remove()
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return auth?.currentUser != null
    }

    override fun getCurrentUserId(): String? {
        return auth?.currentUser?.uid
    }

    override suspend fun signIn(email: String, password: String): Result<Usuario> {
        val authInstance = auth
            ?: return Result.failure(Exception("Firebase Authentication no está inicializado."))
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            val authResult = authInstance.signInWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al obtener UID del usuario autenticado.")
            
            val doc = db.collection("usuarios").document(uid).get().await()
            val usuario = if (doc.exists()) {
                doc.toObject(Usuario::class.java) ?: Usuario(
                    uid = uid,
                    correo = email.trim(),
                    nombre = authResult.user?.displayName ?: "Usuario"
                )
            } else {
                // Si el usuario existe en Authentication pero aún no tenía documento en Firestore, lo creamos
                val nuevoPerfil = Usuario(
                    uid = uid,
                    correo = email.trim(),
                    nombre = authResult.user?.displayName ?: "Usuario",
                    tipoUsuario = "comprador",
                    fechaRegistro = System.currentTimeMillis()
                )
                try {
                    db.collection("usuarios").document(uid).set(nuevoPerfil).await()
                } catch (e: Exception) {
                    Log.w(TAG, "No se pudo sincronizar documento inicial: ${e.message}")
                }
                nuevoPerfil
            }
            Result.success(usuario)
        } catch (e: Exception) {
            Log.e(TAG, "Error en signIn: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signUp(
        nombre: String,
        email: String,
        telefono: String,
        password: String,
        tipoUsuario: String
    ): Result<Usuario> {
        val authInstance = auth
            ?: return Result.failure(Exception("Firebase Authentication no está inicializado."))
        val db = firestore
            ?: return Result.failure(Exception("Cloud Firestore no está inicializado."))

        return try {
            val authResult = authInstance.createUserWithEmailAndPassword(email.trim(), password).await()
            val uid = authResult.user?.uid ?: throw Exception("Error al crear la cuenta en Firebase Auth.")

            // Actualizar DisplayName en el perfil de Firebase Auth
            try {
                val profileChange = UserProfileChangeRequest.Builder()
                    .setDisplayName(nombre.trim())
                    .build()
                authResult.user?.updateProfile(profileChange)?.await()
            } catch (e: Exception) {
                Log.w(TAG, "Aviso actualizando displayName de Auth: ${e.message}")
            }

            val role = if (tipoUsuario.equals("vendedor", ignoreCase = true)) "vendedor" else "comprador"
            val nuevoUsuario = Usuario(
                uid = uid,
                nombre = nombre.trim(),
                correo = email.trim(),
                tipoUsuario = role,
                fechaRegistro = System.currentTimeMillis(),
                fotoPerfil = "",
                telefono = telefono.trim()
            )

            db.collection("usuarios").document(uid).set(nuevoUsuario).await()
            Result.success(nuevoUsuario)
        } catch (e: Exception) {
            Log.e(TAG, "Error en signUp: ${e.message}", e)
            Result.failure(e)
        }
    }

    override suspend fun signOut() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Error en signOut: ${e.message}", e)
        }
    }
}
