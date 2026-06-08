package com.arrow37.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.arrow37.data.*
import com.arrow37.persistence.GameDataStore
import com.arrow37.audio.SoundManager
import com.arrow37.audio.HapticManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val dataStore = GameDataStore(application)
    private val soundManager = SoundManager(application)
    private val hapticManager = HapticManager(application)
    private val _uiState = MutableStateFlow(GameState())
    val uiState: StateFlow<GameState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedLevel = dataStore.currentLevel.first()
            _uiState.update { it.copy(level = savedLevel) }
            generateLevel()
        }
        viewModelScope.launch {
            dataStore.isSoundEnabled.collect { enabled ->
                _uiState.update { it.copy(isSoundEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            dataStore.isVibrationEnabled.collect { enabled ->
                _uiState.update { it.copy(isVibrationEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            dataStore.isDarkMode.collect { enabled ->
                _uiState.update { it.copy(isDarkMode = enabled) }
            }
        }
        viewModelScope.launch {
            dataStore.useNativeRefreshRate.collect { enabled ->
                _uiState.update { it.copy(useNativeRefreshRate = enabled) }
            }
        }
    }

    /**
     * Generates a guaranteed solvable level with regular (straight) arrow shapes.
     */
    fun generateLevel() {
        val currentState = _uiState.value
        val level = currentState.level
        val random = Random(level.toLong() * 98765)
        
        // Slightly larger grid size scaling
        val size = (6 + (level / 2)).coerceAtMost(22)
        
        val arrows = mutableListOf<Arrow>()
        val occupied = mutableSetOf<Pair<Int, Int>>()

        // Increased density target: 85% of the grid
        val targetCellCount = (size * size * 0.85f).toInt()
        var currentCellCount = 0
        
        var attempts = 0
        while (currentCellCount < targetCellCount && attempts < 15000) {
            attempts++
            
            val dir = Direction.entries[random.nextInt(Direction.entries.size)]
            // Longer arrows allowed as level increases
            val maxLength = (4 + level / 2).coerceAtMost(size - 2)
            val length = if (level < 3) 2 else random.nextInt(2, maxLength.coerceAtLeast(3))
            
            val headX = random.nextInt(size)
            val headY = random.nextInt(size)
            
            val body = mutableListOf<Point>()
            var fits = true
            val cells = mutableListOf<Pair<Int, Int>>()
            
            for (i in 0 until length) {
                val bx = headX - dir.dx * i
                val by = headY - dir.dy * i
                
                if (bx !in 0 until size || by !in 0 until size || (bx to by) in occupied) {
                    fits = false
                    break
                }
                body.add(Point(bx.toFloat(), by.toFloat()))
                cells.add(bx to by)
            }
            
            if (!fits) continue

            // SOLVABILITY CHECK: Path in front of the head must be clear of ALREADY PLACED arrows
            // This ensures that the puzzle is solvable by removing arrows in REVERSE order of generation.
            var clearPath = true
            var tx = headX + dir.dx
            var ty = headY + dir.dy
            while (tx in 0 until size && ty in 0 until size) {
                if ((tx to ty) in occupied) {
                    clearPath = false
                    break
                }
                tx += dir.dx
                ty += dir.dy
            }

            if (clearPath) {
                occupied.addAll(cells)
                currentCellCount += cells.size
                arrows.add(Arrow(UUID.randomUUID().toString(), Point(headX.toFloat(), headY.toFloat()), dir, body))
            }
        }

        _uiState.update { 
            it.copy(
                arrows = arrows,
                lives = 3,
                gridSize = size,
                levelName = "Level $level",
                isGameOver = false,
                isLevelCleared = false,
                history = emptyList()
            ) 
        }
    }

    fun nextLevel() {
        val next = _uiState.value.level + 1
        _uiState.update { it.copy(level = next) }
        viewModelScope.launch {
            dataStore.saveLevel(next)
        }
        generateLevel()
    }

    fun onArrowTapped(arrowId: String) {
        val state = _uiState.value
        if (state.isGameOver || state.isLevelCleared) return
        
        val arrow = state.arrows.find { it.id == arrowId } ?: return
        if (arrow.isEscaped || arrow.isEscaping) return

        if (state.isSoundEnabled) soundManager.playTap()
        if (state.isVibrationEnabled) hapticManager.vibrateTap()
        viewModelScope.launch {
            moveArrow(arrow)
        }
    }

    private suspend fun moveArrow(originalArrow: Arrow) {
        val stateAtStart = _uiState.value
        val gridSize = stateAtStart.gridSize
        
        var currentBody = originalArrow.body
        var currentHead = originalArrow.head
        var collision = false
        
        _uiState.update { state ->
            state.copy(arrows = state.arrows.map { 
                if (it.id == originalArrow.id) it.copy(isEscaping = true) else it 
            })
        }

        val step = 0.25f
        while (true) {
            val nextBody = currentBody.map { Point(it.x + originalArrow.direction.dx * step, it.y + originalArrow.direction.dy * step) }
            val nextHead = Point(currentHead.x + originalArrow.direction.dx * step, currentHead.y + originalArrow.direction.dy * step)
            
            // Real-time Collision detection against live positions of other stationary arrows
            // We ignore other "escaping" arrows to prevent frustrating multi-touch crashes
            val currentState = _uiState.value
            
            // Only check collision if the head is still inside the grid
            val isHeadInGrid = nextHead.x >= -0.1f && nextHead.x < gridSize - 0.9f && 
                               nextHead.y >= -0.1f && nextHead.y < gridSize - 0.9f
            
            val hitOther = if (isHeadInGrid) {
                currentState.arrows.any { other ->
                    // Ignore: self, already escaped, and other arrows currently in motion (escaping)
                    if (other.id == originalArrow.id || other.isEscaped || other.isEscaping) return@any false
                    
                    other.body.any { op -> 
                        val dx = nextHead.x - op.x
                        val dy = nextHead.y - op.y
                        val distSq = dx * dx + dy * dy
                        distSq < 0.40f 
                    }
                }
            } else false
            
            if (hitOther) {
                collision = true
                break
            }
            
            currentBody = nextBody
            currentHead = nextHead
            
            _uiState.update { state ->
                state.copy(arrows = state.arrows.map { 
                    if (it.id == originalArrow.id) it.copy(body = currentBody, head = currentHead) else it 
                })
            }

            // Check if arrow is fully outside the grid
            val buffer = 1.0f 
            val anyPartInGrid = currentBody.any { 
                it.x >= -buffer && it.x < gridSize + buffer - 1 && 
                it.y >= -buffer && it.y < gridSize + buffer - 1
            }
            
            if (!anyPartInGrid) break
            delay(12)
        }

        if (collision) {
            if (_uiState.value.isSoundEnabled) soundManager.playCollision()
            if (_uiState.value.isVibrationEnabled) hapticManager.vibrateCollision()
            delay(50)
            _uiState.update { state ->
                val newLives = state.lives - 1
                if (newLives <= 0) {
                    if (state.isSoundEnabled) soundManager.playGameOver()
                    if (state.isVibrationEnabled) hapticManager.vibrateGameOver()
                }
                state.copy(
                    arrows = state.arrows.map { 
                        if (it.id == originalArrow.id) it.copy(body = originalArrow.body, head = originalArrow.head, isEscaping = false) else it
                    },
                    lives = newLives,
                    isGameOver = newLives <= 0
                )
            }
        } else {
            if (_uiState.value.isSoundEnabled) soundManager.playEscape()
            if (_uiState.value.isVibrationEnabled) hapticManager.vibrateEscape()
            _uiState.update { state ->
                val updatedArrows = state.arrows.map { 
                    if (it.id == originalArrow.id) it.copy(isEscaping = false, isEscaped = true) else it 
                }
                val cleared = updatedArrows.all { it.isEscaped }
                if (cleared) {
                    if (state.isSoundEnabled) soundManager.playWin()
                    if (state.isVibrationEnabled) hapticManager.vibrateWin()
                }
                state.copy(
                    arrows = updatedArrows,
                    isLevelCleared = cleared,
                    history = state.history + listOf(stateAtStart.arrows),
                    redoStack = emptyList() // Clear redo stack on new move
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    fun undo() {
        _uiState.update { state ->
            if (state.history.isEmpty()) return@update state
            val lastArrows = state.history.last()
            state.copy(
                arrows = lastArrows,
                history = state.history.dropLast(1),
                redoStack = state.redoStack + listOf(state.arrows),
                isGameOver = false,
                isLevelCleared = false
            )
        }
    }

    fun redo() {
        _uiState.update { state ->
            if (state.redoStack.isEmpty()) return@update state
            val nextArrows = state.redoStack.last()
            state.copy(
                arrows = nextArrows,
                history = state.history + listOf(state.arrows),
                redoStack = state.redoStack.dropLast(1),
                isGameOver = false,
                isLevelCleared = false
            )
        }
    }
    
    fun reset() {
        generateLevel()
    }

    fun toggleGrid() {
        _uiState.update { it.copy(showGrid = !it.showGrid) }
    }

    fun toggleSound() {
        val newEnabled = !_uiState.value.isSoundEnabled
        _uiState.update { it.copy(isSoundEnabled = newEnabled) }
        viewModelScope.launch {
            dataStore.saveSoundEnabled(newEnabled)
        }
    }

    fun toggleVibration() {
        val newEnabled = !_uiState.value.isVibrationEnabled
        _uiState.update { it.copy(isVibrationEnabled = newEnabled) }
        viewModelScope.launch {
            dataStore.saveVibrationEnabled(newEnabled)
        }
    }

    fun toggleDarkMode() {
        val newEnabled = !_uiState.value.isDarkMode
        _uiState.update { it.copy(isDarkMode = newEnabled) }
        viewModelScope.launch {
            dataStore.saveDarkMode(newEnabled)
        }
    }

    fun toggleNativeRefreshRate() {
        val newEnabled = !_uiState.value.useNativeRefreshRate
        _uiState.update { it.copy(useNativeRefreshRate = newEnabled) }
        viewModelScope.launch {
            dataStore.saveNativeRefreshRate(newEnabled)
        }
    }
}
