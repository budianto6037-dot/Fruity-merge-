package com.example.game.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.game.model.GameScore
import kotlinx.coroutines.flow.Flow

@Dao
interface GameScoreDao {
    @Query("SELECT * FROM game_scores ORDER BY score DESC, timestamp DESC LIMIT 50")
    fun getLeaderboard(): Flow<List<GameScore>>

    @Query("SELECT MAX(score) FROM game_scores")
    fun getHighScoreFlow(): Flow<Int?>

    @Query("SELECT MAX(score) FROM game_scores")
    suspend fun getHighScore(): Int?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: GameScore)

    @Query("DELETE FROM game_scores")
    suspend fun clearAllScores()
}
