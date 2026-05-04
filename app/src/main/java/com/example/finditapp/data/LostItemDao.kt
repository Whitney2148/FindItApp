package com.example.finditapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LostItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: LostItemEntity)

    @Query("SELECT * FROM lost_items")
    fun getAllItems(): Flow<List<LostItemEntity>>

    @Query("""
        SELECT * FROM lost_items 
        WHERE title LIKE '%' || :query || '%' 
        OR description LIKE '%' || :query || '%'
    """)
    fun searchItems(query: String): Flow<List<LostItemEntity>>

    @Update
    suspend fun updateItem(item: LostItemEntity)

    @Delete
    suspend fun deleteItem(item: LostItemEntity)
}
