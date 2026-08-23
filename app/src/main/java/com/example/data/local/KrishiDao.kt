package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface KrishiDao {

    // Farmer Profile Operations
    @Query("SELECT * FROM farmer_profiles WHERE id = :farmerId LIMIT 1")
    fun getFarmerProfile(farmerId: String = "FARMER-8842"): Flow<FarmerProfileEntity?>

    @Query("SELECT * FROM farmer_profiles LIMIT 1")
    suspend fun getFarmerProfileDirect(): FarmerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: FarmerProfileEntity)

    @Update
    suspend fun updateProfile(profile: FarmerProfileEntity)

    // Help Chatbot History Operations
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllChatMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages")
    suspend fun clearChatMessages()

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessageById(messageId: String)
}
