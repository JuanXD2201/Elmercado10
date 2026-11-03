package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    fun initialize(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val app = try {
                    FirebaseApp.initializeApp(context)
                } catch (e: Exception) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:1081934380157:android:eed9a9a21e06e60f420514")
                        .setApiKey("AIzaSyDZr7XpjInu_Jhoc430TatWcXjFmyyG5FE")
                        .setProjectId("el-mercado10")
                        .setDatabaseUrl("https://el-mercado10-default-rtdb.firebaseio.com")
                        .setStorageBucket("el-mercado10.firebasestorage.app")
                        .setGcmSenderId("1081934380157")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                }
                isInitialized = app != null
            } else {
                isInitialized = true
            }
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization fallback notice: ${e.message}")
            isInitialized = false
        }
    }

    fun isConfigured(context: Context? = null): Boolean {
        return try {
            if (!isInitialized && context != null) {
                initialize(context)
            }
            FirebaseApp.getApps(context ?: FirebaseApp.getInstance().applicationContext).isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseAuth no disponible: ${e.message}")
            null
        }

    val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore no disponible: ${e.message}")
            null
        }

    val storage: FirebaseStorage?
        get() = try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseStorage no disponible: ${e.message}")
            null
        }
}
