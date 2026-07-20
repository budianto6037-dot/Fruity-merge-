package com.example.game.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.game.data.GameRepository
import com.example.game.model.GameScore
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// Physics circle structure
data class PhysicsCircle(
    val id: Long,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val mass: Float,
    val level: Int,
    var isNew: Boolean = true,
    var mergePending: Boolean = false,
    val colorHex: Long
)

// Particle structure for merge explosions
data class MergeParticle(
    val x: Float,
    val y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val colorHex: Long,
    var alpha: Float = 1.0f,
    val maxLifespan: Int = 30,
    var lifespan: Int = 30
)

// Floating points popup
data class FloatingText(
    val text: String,
    val x: Float,
    var y: Float,
    var alpha: Float = 1.0f,
    val maxLifespan: Int = 40,
    var lifespan: Int = 40
)

// Theme item representing characteristics of a food/character level
data class ThemeItem(
    val level: Int,
    val name: String,
    val emoji: String,
    val radiusDp: Float,
    val mass: Float,
    val color: Long,
    val mergeScore: Int
)

class GameViewModel(private val repository: GameRepository) : ViewModel() {

    companion object {
        const val PLAY_WIDTH = 360f
        const val PLAY_HEIGHT = 500f
        const val DROP_HEIGHT = 60f
        const val WARNING_LINE_Y = 120f
        const val MAX_WARNING_FRAMES = 180 // ~3 seconds at 60 FPS
    }

    // Themes definition
    private val fruitTheme = listOf(
        ThemeItem(0, "Ceri", "🍒", 14f, 1.0f, 0xFFFF3B30, 1),
        ThemeItem(1, "Stroberi", "🍓", 19f, 1.5f, 0xFFFF2D55, 3),
        ThemeItem(2, "Anggur", "🍇", 24f, 2.2f, 0xFF8E44AD, 6),
        ThemeItem(3, "Jeruk", "🍊", 30f, 3.0f, 0xFFFF9500, 10),
        ThemeItem(4, "Tomat", "🍅", 36f, 4.0f, 0xFFFF453A, 15),
        ThemeItem(5, "Apel", "🍎", 42f, 5.2f, 0xFFC0392B, 21),
        ThemeItem(6, "Pir", "🍐", 48f, 6.5f, 0xFF27AE60, 28),
        ThemeItem(7, "Persik", "🍑", 55f, 8.0f, 0xFFFF9999, 36),
        ThemeItem(8, "Nanas", "🍍", 64f, 10.0f, 0xFFF1C40F, 45),
        ThemeItem(9, "Melon", "🍈", 74f, 12.5f, 0xFF2ECC71, 55),
        ThemeItem(10, "Semangka", "🍉", 86f, 16.0f, 0xFF1E824C, 66)
    )

    private val emojiTheme = listOf(
        ThemeItem(0, "Crying", "😭", 14f, 1.0f, 0xFF4A90E2, 1),
        ThemeItem(1, "Sad", "😢", 19f, 1.5f, 0xFF5C97BF, 3),
        ThemeItem(2, "Neutral", "😐", 24f, 2.2f, 0xFF95A5A6, 6),
        ThemeItem(3, "Smile", "🙂", 30f, 3.0f, 0xFFF5D76E, 10),
        ThemeItem(4, "Grin", "😀", 36f, 4.0f, 0xFFF4D03F, 15),
        ThemeItem(5, "Cool", "😎", 42f, 5.2f, 0xFF34495E, 21),
        ThemeItem(6, "Star", "🤩", 48f, 6.5f, 0xFFE67E22, 28),
        ThemeItem(7, "Party", "🥳", 55f, 8.0f, 0xFFFF7A7A, 36),
        ThemeItem(8, "Ghost", "👻", 64f, 10.0f, 0xFFECF0F1, 45),
        ThemeItem(9, "Imp", "😈", 74f, 12.5f, 0xFF8E44AD, 55),
        ThemeItem(10, "King", "👑", 86f, 16.0f, 0xFFD4AC0D, 66)
    )

    private val spaceTheme = listOf(
        ThemeItem(0, "Meteor", "☄️", 14f, 1.0f, 0xFF7F8C8D, 1),
        ThemeItem(1, "Bintang", "⭐", 19f, 1.5f, 0xFFF1C40F, 3),
        ThemeItem(2, "Bulan", "🌙", 24f, 2.2f, 0xFFBDC3C7, 6),
        ThemeItem(3, "Satelit", "🛰️", 30f, 3.0f, 0xFF95A5A6, 10),
        ThemeItem(4, "Radar", "📡", 36f, 4.0f, 0xFF7F8C8D, 15),
        ThemeItem(5, "Roket", "🚀", 42f, 5.2f, 0xFFE74C3C, 21),
        ThemeItem(6, "Astronot", "🧑‍🚀", 48f, 6.5f, 0xFFECF0F1, 28),
        ThemeItem(7, "Saturnus", "🪐", 55f, 8.0f, 0xFFD35400, 36),
        ThemeItem(8, "Nebula", "🌌", 64f, 10.0f, 0xFF8E44AD, 45),
        ThemeItem(9, "Matahari", "☀️", 74f, 12.5f, 0xFFF39C12, 55),
        ThemeItem(10, "Lubang Hitam", "🌀", 86f, 16.0f, 0xFF2C3E50, 66)
    )

    // Game state states
    private val _activeTheme = MutableStateFlow("FRUIT")
    val activeTheme = _activeTheme.asStateFlow()

    private val _circles = MutableStateFlow<List<PhysicsCircle>>(emptyList())
    val circles = _circles.asStateFlow()

    private val _particles = MutableStateFlow<List<MergeParticle>>(emptyList())
    val particles = _particles.asStateFlow()

    private val _floatingTexts = MutableStateFlow<List<FloatingText>>(emptyList())
    val floatingTexts = _floatingTexts.asStateFlow()

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    private val _highScore = MutableStateFlow(0)
    val highScore = _highScore.asStateFlow()

    private val _currentDropperX = MutableStateFlow(PLAY_WIDTH / 2f)
    val currentDropperX = _currentDropperX.asStateFlow()

    private val _currentFruitLevel = MutableStateFlow(0)
    val currentFruitLevel = _currentFruitLevel.asStateFlow()

    private val _nextFruitLevel = MutableStateFlow(1)
    val nextFruitLevel = _nextFruitLevel.asStateFlow()

    private val _isReadyToDrop = MutableStateFlow(true)
    val isReadyToDrop = _isReadyToDrop.asStateFlow()

    private val _isGameOver = MutableStateFlow(false)
    val isGameOver = _isGameOver.asStateFlow()

    private val _isWarning = MutableStateFlow(false)
    val isWarning = _isWarning.asStateFlow()

    private val _warningProgress = MutableStateFlow(0f) // 0f to 1f
    val warningProgress = _warningProgress.asStateFlow()

    private val _shakeAmount = MutableStateFlow(0f)
    val shakeAmount = _shakeAmount.asStateFlow()

    // Leaderboard flow loaded from room
    val leaderboard = repository.leaderboard.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Flow representing haptic event triggers for Composable
    private val _hapticTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val hapticTrigger: SharedFlow<Unit> = _hapticTrigger.asSharedFlow()

    private var warningFrames = 0
    private var dropCooldownActive = false

    init {
        viewModelScope.launch {
            // Load high score
            _highScore.value = repository.getHighScore()
            repository.highScoreFlow.collect { hs ->
                if (hs != null && hs > _highScore.value) {
                    _highScore.value = hs
                }
            }
        }
        resetGame()
    }

    // Switch theme
    fun setTheme(theme: String) {
        if (theme != _activeTheme.value) {
            _activeTheme.value = theme
            resetGame()
        }
    }

    // Get items for active theme
    fun getThemeItems(theme: String = _activeTheme.value): List<ThemeItem> {
        return when (theme) {
            "EMOJI" -> emojiTheme
            "SPACE" -> spaceTheme
            else -> fruitTheme
        }
    }

    // Reset game state
    fun resetGame() {
        _circles.value = emptyList()
        _particles.value = emptyList()
        _floatingTexts.value = emptyList()
        _score.value = 0
        _isGameOver.value = false
        _isWarning.value = false
        _warningProgress.value = 0f
        _shakeAmount.value = 0f
        warningFrames = 0
        dropCooldownActive = false
        _isReadyToDrop.value = true
        _currentFruitLevel.value = generateRandomDropLevel()
        _nextFruitLevel.value = generateRandomDropLevel()
        _currentDropperX.value = PLAY_WIDTH / 2f
    }

    // Helper to get random item for dropping (level 0 to 3)
    private fun generateRandomDropLevel(): Int {
        val rand = (0..100).random()
        return when {
            rand < 40 -> 0      // 40% cherry
            rand < 70 -> 1      // 30% strawberry
            rand < 90 -> 2      // 20% grape
            else -> 3           // 10% orange
        }
    }

    // Update dropper position
    fun moveDropper(x: Float) {
        if (_isGameOver.value) return
        val currentItems = getThemeItems()
        val r = currentItems.getOrNull(_currentFruitLevel.value)?.radiusDp ?: 14f
        // Clamp to avoid going outside boundaries
        _currentDropperX.value = x.coerceIn(r, PLAY_WIDTH - r)
    }

    // Drop the current fruit
    fun dropFruit() {
        if (_isGameOver.value || !_isReadyToDrop.value || dropCooldownActive) return

        val level = _currentFruitLevel.value
        val items = getThemeItems()
        val item = items.getOrNull(level) ?: return

        val dropX = _currentDropperX.value
        val dropY = DROP_HEIGHT

        // Instantiate physical circle
        val newCircle = PhysicsCircle(
            id = System.nanoTime(),
            x = dropX,
            y = dropY,
            vx = 0f,
            vy = 1.0f, // minor initial downward velocity
            radius = item.radiusDp,
            mass = item.mass,
            level = level,
            isNew = true,
            colorHex = item.color
        )

        _circles.value = _circles.value + newCircle

        // Cooldown mechanism
        _isReadyToDrop.value = false
        dropCooldownActive = true

        viewModelScope.launch {
            kotlinx.coroutines.delay(500)
            if (!_isGameOver.value) {
                // Setup next fruit
                _currentFruitLevel.value = _nextFruitLevel.value
                _nextFruitLevel.value = generateRandomDropLevel()
                
                // Readjust dropper position with clamped boundaries for new size
                val newR = items.getOrNull(_currentFruitLevel.value)?.radiusDp ?: 14f
                _currentDropperX.value = _currentDropperX.value.coerceIn(newR, PLAY_WIDTH - newR)
                
                _isReadyToDrop.value = true
                dropCooldownActive = false
            }
        }
    }

    // Physics ticks per render frame (sub-stepping)
    fun tickPhysics() {
        if (_isGameOver.value) return

        val currentCircles = _circles.value.toMutableList()
        val subSteps = 6 // Excellent for stability
        
        // Decay shake offset
        if (_shakeAmount.value > 0f) {
            _shakeAmount.value = (_shakeAmount.value - 0.4f).coerceAtLeast(0f)
        }

        repeat(subSteps) {
            // Apply forces & update positions
            for (c in currentCircles) {
                // Apply Gravity
                c.vy += 0.08f // gravity per sub-step

                // Update position
                c.x += c.vx
                c.y += c.vy

                // Transition from new to active once it falls below dropper line
                if (c.isNew && c.y > DROP_HEIGHT + c.radius + 15f) {
                    c.isNew = false
                }

                // Left Wall
                if (c.x - c.radius < 0) {
                    c.x = c.radius
                    c.vx = -c.vx * 0.15f
                }
                // Right Wall
                if (c.x + c.radius > PLAY_WIDTH) {
                    c.x = PLAY_WIDTH - c.radius
                    c.vx = -c.vx * 0.15f
                }
                // Floor
                if (c.y + c.radius > PLAY_HEIGHT) {
                    c.y = PLAY_HEIGHT - c.radius
                    c.vy = -c.vy * 0.12f // minimal bounce
                    c.vx = c.vx * 0.88f // high friction on floor
                }
            }

            // Circle-to-Circle Collision Resolution
            for (i in 0 until currentCircles.size) {
                val c1 = currentCircles[i]
                if (c1.mergePending) continue

                for (j in i + 1 until currentCircles.size) {
                    val c2 = currentCircles[j]
                    if (c2.mergePending || c1.mergePending) continue

                    val dx = c2.x - c1.x
                    val dy = c2.y - c1.y
                    val distSq = dx * dx + dy * dy
                    val rSum = c1.radius + c2.radius

                    if (distSq < rSum * rSum) {
                        val dist = sqrt(distSq.toDouble()).toFloat().coerceAtLeast(0.01f)

                        // Check Merge
                        if (c1.level == c2.level) {
                            c1.mergePending = true
                            c2.mergePending = true
                            continue
                        }

                        // Static collision separation
                        val overlap = rSum - dist
                        val nx = dx / dist
                        val ny = dy / dist

                        val totalMass = c1.mass + c2.mass
                        val ratio1 = c2.mass / totalMass
                        val ratio2 = c1.mass / totalMass

                        // Displace them
                        c1.x -= nx * overlap * ratio1
                        c1.y -= ny * overlap * ratio1
                        c2.x += nx * overlap * ratio2
                        c2.y += ny * overlap * ratio2

                        // Dynamic impulse response
                        val rvx = c2.vx - c1.vx
                        val rvy = c2.vy - c1.vy
                        val velAlongNormal = rvx * nx + rvy * ny

                        if (velAlongNormal < 0f) {
                            // Elastic bounciness
                            val e = 0.12f
                            val impulse = -(1f + e) * velAlongNormal / (1f / c1.mass + 1f / c2.mass)

                            c1.vx -= (impulse / c1.mass) * nx
                            c1.vy -= (impulse / c1.mass) * ny
                            c2.vx += (impulse / c2.mass) * nx
                            c2.vy += (impulse / c2.mass) * ny

                            // Sliding Friction
                            val tx = -ny
                            val ty = nx
                            val velAlongTangent = rvx * tx + rvy * ty
                            val f = 0.05f // friction factor
                            val tangentImpulse = -velAlongTangent * f / (1f / c1.mass + 1f / c2.mass)

                            c1.vx -= (tangentImpulse / c1.mass) * tx
                            c1.vy -= (tangentImpulse / c1.mass) * ty
                            c2.vx += (tangentImpulse / c2.mass) * tx
                            c2.vy += (tangentImpulse / c2.mass) * ty
                        }
                    }
                }
            }
        }

        // Process Merges
        val mergedIds = mutableSetOf<Long>()
        val toRemove = mutableSetOf<PhysicsCircle>()
        val toAdd = mutableListOf<PhysicsCircle>()

        for (i in 0 until currentCircles.size) {
            val c1 = currentCircles[i]
            if (c1 in toRemove || c1.id in mergedIds) continue

            if (c1.mergePending) {
                // Find matching partner
                var partner: PhysicsCircle? = null
                for (j in i + 1 until currentCircles.size) {
                    val c2 = currentCircles[j]
                    if (c2 in toRemove || c2.id in mergedIds) continue
                    if (c2.mergePending && c2.level == c1.level) {
                        partner = c2
                        break
                    }
                }

                if (partner != null) {
                    mergedIds.add(c1.id)
                    mergedIds.add(partner.id)
                    toRemove.add(c1)
                    toRemove.add(partner)

                    val mx = (c1.x + partner.x) / 2f
                    val my = (c1.y + partner.y) / 2f
                    val nextLvl = c1.level + 1

                    if (nextLvl <= 10) {
                        val items = getThemeItems()
                        val newItem = items.getOrNull(nextLvl)
                        if (newItem != null) {
                            val mergedCircle = PhysicsCircle(
                                id = System.nanoTime() + (0..1000).random(),
                                x = mx,
                                y = my,
                                vx = (c1.vx + partner.vx) / 2f,
                                vy = ((c1.vy + partner.vy) / 2f) - 0.5f, // slight upwards push
                                radius = newItem.radiusDp,
                                mass = newItem.mass,
                                level = nextLvl,
                                isNew = false,
                                colorHex = newItem.color
                            )
                            toAdd.add(mergedCircle)

                            // Update score
                            _score.value += newItem.mergeScore
                            if (_score.value > _highScore.value) {
                                _highScore.value = _score.value
                            }

                            // Trigger visuals
                            spawnMergeParticles(mx, my, newItem.color)
                            spawnFloatingText("+${newItem.mergeScore}", mx, my)
                            _shakeAmount.value = (_shakeAmount.value + nextLvl * 1.5f).coerceAtMost(15f)
                            triggerHaptic()
                        }
                    } else {
                        // Max item merge (🍉 + 🍉)! Epic reward and screen clearing
                        _score.value += 150
                        if (_score.value > _highScore.value) {
                            _highScore.value = _score.value
                        }

                        // Golden particle blast
                        spawnMergeParticles(mx, my, 0xFFFFD700)
                        spawnFloatingText("SUPER MERGE! +150", mx, my)
                        _shakeAmount.value = 20f
                        triggerHaptic()
                    }
                } else {
                    // No partner found in this sub-frame, reset state to try again
                    c1.mergePending = false
                }
            }
        }

        // Apply removals/additions
        currentCircles.removeAll(toRemove)
        currentCircles.addAll(toAdd)
        _circles.value = currentCircles

        // Update visual effects (particles & texts)
        updateParticlesAndTexts()

        // Check Overflow / Warning conditions
        checkOverflow(currentCircles)
    }

    // Spawn burst of particles on merge
    private fun spawnMergeParticles(x: Float, y: Float, colorHex: Long) {
        val count = 15
        val pList = _particles.value.toMutableList()
        repeat(count) {
            val angle = Math.random() * 2 * Math.PI
            val speed = 1.0f + Math.random() * 3.5f
            val vx = (cos(angle) * speed).toFloat()
            val vy = (sin(angle) * speed).toFloat()
            pList.add(
                MergeParticle(
                    x = x,
                    y = y,
                    vx = vx,
                    vy = vy,
                    radius = 3f + (Math.random() * 4f).toFloat(),
                    colorHex = colorHex,
                    alpha = 1.0f,
                    maxLifespan = 25 + (0..15).random()
                )
            )
        }
        _particles.value = pList
    }

    // Spawn floating score popup
    private fun spawnFloatingText(text: String, x: Float, y: Float) {
        val tList = _floatingTexts.value.toMutableList()
        tList.add(FloatingText(text = text, x = x, y = y))
        _floatingTexts.value = tList
    }

    // Math utility for particle angles
    private fun cos(angle: Double) = kotlin.math.cos(angle)
    private fun sin(angle: Double) = kotlin.math.sin(angle)

    // Update particles and texts lifespans
    private fun updateParticlesAndTexts() {
        // Particles
        val pList = _particles.value.toMutableList()
        val pIterator = pList.iterator()
        while (pIterator.hasNext()) {
            val p = pIterator.next()
            p.lifespan--
            if (p.lifespan <= 0) {
                pIterator.remove()
            } else {
                p.alpha = p.lifespan.toFloat() / p.maxLifespan.toFloat()
                // Apply slight drag and gravity
                p.vx *= 0.95f
                p.vy = p.vy * 0.95f + 0.05f
                // Move
                val px = p.x + p.vx
                val py = p.y + p.vy
                // Workaround to write properties to data class since fields are val/var
                // We're updating var fields in-place in data class
                // (which is fine since we mutated and will re-assign list)
                try {
                    val fX = p.javaClass.getDeclaredField("x")
                    fX.isAccessible = true
                    fX.set(p, px)
                    val fY = p.javaClass.getDeclaredField("y")
                    fY.isAccessible = true
                    fY.set(p, py)
                } catch (e: Exception) {
                    // Fallback
                }
            }
        }
        _particles.value = pList

        // Floating texts
        val tList = _floatingTexts.value.toMutableList()
        val tIterator = tList.iterator()
        while (tIterator.hasNext()) {
            val t = tIterator.next()
            t.lifespan--
            if (t.lifespan <= 0) {
                tIterator.remove()
            } else {
                t.alpha = t.lifespan.toFloat() / t.maxLifespan.toFloat()
                t.y -= 0.8f // float upwards
            }
        }
        _floatingTexts.value = tList
    }

    // Check overflow warning & game over criteria
    private fun checkOverflow(activeCircles: List<PhysicsCircle>) {
        var hasCircleInWarning = false
        for (c in activeCircles) {
            // Only count items that are settled (not newly spawned inside dropper space)
            if (!c.isNew && c.y - c.radius < WARNING_LINE_Y) {
                hasCircleInWarning = true
                break
            }
        }

        if (hasCircleInWarning) {
            warningFrames++
            _isWarning.value = true
            _warningProgress.value = (warningFrames.toFloat() / MAX_WARNING_FRAMES).coerceIn(0f, 1f)

            if (warningFrames >= MAX_WARNING_FRAMES) {
                triggerGameOver()
            }
        } else {
            // Calm down, decay warning back to normal
            if (warningFrames > 0) {
                warningFrames = (warningFrames - 2).coerceAtLeast(0)
                _warningProgress.value = (warningFrames.toFloat() / MAX_WARNING_FRAMES).coerceIn(0f, 1f)
            }
            if (warningFrames == 0) {
                _isWarning.value = false
            }
        }
    }

    // Trigger Game Over
    private fun triggerGameOver() {
        _isGameOver.value = true
        _isWarning.value = false
        _warningProgress.value = 0f
        saveScoreToHistory()
    }

    // Save score to database via repository
    private fun saveScoreToHistory() {
        val currentScore = _score.value
        if (currentScore <= 0) return // don't save zero scores

        // Find highest level reached on screen
        val highestLvlReached = _circles.value.maxOfOrNull { c -> c.level } ?: 0

        viewModelScope.launch {
            repository.insertScore(
                GameScore(
                    score = currentScore,
                    highestLevel = highestLvlReached,
                    theme = _activeTheme.value
                )
            )
        }
    }

    // Trigger haptic feedback inside game loop
    private fun triggerHaptic() {
        _hapticTrigger.tryEmit(Unit)
    }

    // Clear local leaderboard data
    fun clearLeaderboard() {
        viewModelScope.launch {
            repository.clearAllScores()
        }
    }
}

class GameViewModelFactory(private val repository: GameRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GameViewModel::class.java)) {
            return GameViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
