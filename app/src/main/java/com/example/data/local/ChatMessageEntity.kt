package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sender: String, // "USER" or "BOT"
    val text: String,
    val hindiText: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isVoice: Boolean = false,
    val category: String = "GENERAL" // "MSP", "SLOT", "MOISTURE", "PAYMENT", "GATE", "GENERAL"
)
