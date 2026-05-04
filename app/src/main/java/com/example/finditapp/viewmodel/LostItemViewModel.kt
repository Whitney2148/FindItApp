package com.example.finditapp.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.asLiveData
import com.example.finditapp.data.AppDatabase
import com.example.finditapp.data.LostItemEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class LostItemViewModel(application: Application) : AndroidViewModel(application) {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val dao = AppDatabase.getDatabase(application).lostItemDao()

    // UI observes this: data comes directly from local Room database
    val allItems: LiveData<List<LostItemEntity>> = dao.getAllItems().asLiveData()

    init {
        startSync()
    }

    /**
     * Listens to Firestore changes and synchronizes the local Room database.
     */
    private fun startSync() {
        db.collection("lost_items").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener

            snapshot?.documentChanges?.forEach { change ->
                val item = change.document.toObject(LostItemEntity::class.java)
                    .copy(id = change.document.id)

                viewModelScope.launch(Dispatchers.IO) {
                    when (change.type) {
                        DocumentChange.Type.ADDED,
                        DocumentChange.Type.MODIFIED -> {
                            dao.insertItem(item)
                        }
                        DocumentChange.Type.REMOVED -> {
                            dao.deleteItem(item)
                        }
                    }
                }
            }
        }
    }

    fun insert(item: LostItemEntity, localImageUri: Uri?) = viewModelScope.launch(Dispatchers.IO) {
        val id = item.id.ifEmpty { UUID.randomUUID().toString() }
        val itemWithId = item.copy(id = id)

        // 1. Save to Room immediately for instant UI feedback
        dao.insertItem(itemWithId)

        try {
            var itemToSync = itemWithId

            // 2. Upload image to Storage if provided
            if (localImageUri != null) {
                val fileName = "images/$id.jpg"
                val ref = storage.reference.child(fileName)
                ref.putFile(localImageUri).await()
                val downloadUri = ref.downloadUrl.await()

                itemToSync = itemWithId.copy(imageUri = downloadUri.toString())

                // Update local Room with the permanent remote image URL
                dao.insertItem(itemToSync)
            }

            // 3. Synchronize with Firestore
            db.collection("lost_items").document(id).set(itemToSync).await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun delete(item: LostItemEntity) = viewModelScope.launch(Dispatchers.IO) {
        if (item.id.isNotEmpty()) {
            try {
                // 1. Remove from local Room immediately
                dao.deleteItem(item)
                // 2. Remove from Firestore
                db.collection("lost_items").document(item.id).delete().await()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
