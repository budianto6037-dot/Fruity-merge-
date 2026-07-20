package com.example.game.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_scores")
data class GameScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val score: Int,
    val highestLevel: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val theme: String = "FRUIT"
)
