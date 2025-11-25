package com.example.habithero.data

import android.net.Uri
import com.example.habithero.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await


class FirestoreRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private fun uid(): String = auth.currentUser?.uid
        ?: throw IllegalStateException("No signed in user")


    suspend fun getCurrentUser(): User {
        val u = auth.currentUser ?: return User()
        val snap = db.collection("users").document(u.uid).get().await()
        val name = snap.getString("name") ?: (u.displayName ?: "")
        val photo = snap.getString("photoUrl") ?: (u.photoUrl?.toString())
        return User(id = u.uid, name = name, email = u.email ?: "", photoUrl = photo)
    }


    suspend fun updateName(newName: String) {
        db.collection("users").document(uid())
            .set(mapOf("name" to newName), com.google.firebase.firestore.SetOptions.merge())
            .await()
    }


    suspend fun uploadProfileImage(localUri: Uri): String {
        val ref = storage.reference.child("profileImages/${uid()}.jpg")
        ref.putFile(localUri).await()
        val url = ref.downloadUrl.await().toString()
        db.collection("users").document(uid())
            .set(mapOf("photoUrl" to url), com.google.firebase.firestore.SetOptions.merge())
            .await()
        return url
    }


    fun signOut() = auth.signOut()
}