package com.example.game.data

import com.example.game.model.GameScore
import kotlinx.coroutines.flow.Flow

class GameRepository(private val gameScoreDao: GameScoreDao) {
    val leaderboard: Flow<List<GameScore>> = gameScoreDao.getLeaderboard()
    val highScoreFlow: Flow<Int?> = gameScoreDao.getHighScoreFlow()

    suspend fun getHighScore(): Int {
        return gameScoreDao.getHighScore() ?: 0
    }

    suspend fun insertScore(score: GameScore) {
        gameScoreDao.insertScore(score)
    }

    suspend fun clearAllScores() {
        gameScoreDao.clearAllScores()
    }
}
