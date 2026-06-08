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
        
        // Balanced grid size scaling
        val size = (6 + (level / 5)).coerceAtMost(18)
        
        val arrows = mutableListOf<Arrow>()
        val occupied = mutableSetOf<Pair<Int, Int>>()

        // Density target: 65% of the grid for better clarity and fewer overlaps
        val targetCellCount = (size * size * 0.65f).toInt()
        var currentCellCount = 0
        
        var attempts = 0
        while (currentCellCount < targetCellCount && attempts < 12000) {
            attempts++
            
            val dir = Direction.entries[random.nextInt(Direction.entries.size)]
            val length = if (level < 3) 2 else random.nextInt(2, 5)
            
            val headX = random.nextInt(size)
            val headY = random.nextInt(size)
            
            // STRICT OVERLAP CHECK for the head
            if ((headX to headY) in occupied) continue

            val body = mutableListOf<Point>()
            val cells = mutableListOf<Pair<Int, Int>>()
            
            var currX = headX
            var currY = headY
            body.add(Point(currX.toFloat(), currY.toFloat()))
            cells.add(currX to currY)
            
            // Define the "danger zone": cells that the head will pass through to exit
            val exitPathCells = mutableSetOf<Pair<Int, Int>>()
            var ex = headX + dir.dx
            var ey = headY + dir.dy
            while (ex in 0 until size && ey in 0 until size) {
                exitPathCells.add(ex to ey)
                ex += dir.dx
                ey += dir.dy
            }

            // ENFORCE "NECK": The first segment after the head should ideally be 
            // opposite to the exit direction to give a clear visual orientation.
            val neckX = headX - dir.dx
            val neckY = headY - dir.dy
            if (neckX in 0 until size && neckY in 0 until size && (neckX to neckY) !in occupied) {
                currX = neckX
                currY = neckY
                body.add(Point(currX.toFloat(), currY.toFloat()))
                cells.add(currX to currY)
            }

            var fits = true
            for (i in body.size until length) {
                val neighbors = listOf(
                    currX + 1 to currY,
                    currX - 1 to currY,
                    currX to currY + 1,
                    currX to currY - 1
                ).filter { (nx, ny) ->
                    nx in 0 until size && ny in 0 until size && 
                    (nx to ny) !in occupied && 
                    (nx to ny) !in cells &&
                    (nx to ny) !in exitPathCells // DO NOT grow body in front of the head
                }
                
                if (neighbors.isEmpty()) {
                    fits = (body.size >= 2) // Accept shorter arrow if we at least have a neck
                    break
                }
                
                // Very high bias for straightness (90%)
                val backDirX = -dir.dx
                val backDirY = -dir.dy
                val preferred = (currX + backDirX) to (currY + backDirY)
                
                val next = if (preferred in neighbors && random.nextFloat() < 0.9f) {
                    preferred
                } else {
                    neighbors[random.nextInt(neighbors.size)]
                }
                
                currX = next.first
                currY = next.second
                body.add(Point(currX.toFloat(), currY.toFloat()))
                cells.add(currX to currY)
            }
            
            if (!fits) continue

            // ROBUST SOLVABILITY CHECK: Exit path must be clear of all OTHER arrows
            var clearPath = true
            for (p in body) {
                var tx = p.x.toInt() + dir.dx
                var ty = p.y.toInt() + dir.dy
                while (tx in 0 until size && ty in 0 until size) {
                    // It's okay to pass through its own body cells during generation, 
                    // but NOT through already placed arrows in 'occupied'
                    if ((tx to ty) in occupied) {
                        clearPath = false
                        break
                    }
                    tx += dir.dx
                    ty += dir.dy
                }
                if (!clearPath) break
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
                history = emptyList(),
                redoStack = emptyList()
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
        val pathReference = originalArrow.body
        val direction = originalArrow.direction
        
        var totalMoved = 0f
        var collision = false
        val step = 0.20f
        
        _uiState.update { state ->
            state.copy(arrows = state.arrows.map { 
                if (it.id == originalArrow.id) it.copy(isEscaping = true) else it 
            })
        }

        while (true) {
            totalMoved += step
            
            // Snake-like movement: segments follow the path defined by the original body and direction
            val nextBody = List(pathReference.size) { i ->
                val u = i.toFloat() - totalMoved
                if (u <= 0f) {
                    // Head and parts following the exit direction
                    Point(
                        pathReference[0].x + direction.dx * (-u),
                        pathReference[0].y + direction.dy * (-u)
                    )
                } else {
                    // Parts still slithering through the original curved body
                    val k = u.toInt()
                    val f = u - k
                    if (k + 1 < pathReference.size) {
                        val p1 = pathReference[k]
                        val p2 = pathReference[k + 1]
                        Point(
                            p1.x + (p2.x - p1.x) * f,
                            p1.y + (p2.y - p1.y) * f
                        )
                    } else {
                        // Extrapolate beyond tail if necessary (rare)
                        val last = pathReference.last()
                        val prev = if (pathReference.size > 1) pathReference[pathReference.size - 2] else last
                        Point(
                            last.x + (last.x - prev.x) * (u - (pathReference.size - 1)),
                            last.y + (last.y - prev.y) * (u - (pathReference.size - 1))
                        )
                    }
                }
            }
            val nextHead = nextBody.first()

            val currentState = _uiState.value
            val hitOther = currentState.arrows.any { other ->
                if (other.id == originalArrow.id || other.isEscaped || other.isEscaping) return@any false
                
                nextBody.any { bp ->
                    other.body.any { op -> 
                        val dx = bp.x - op.x
                        val dy = bp.y - op.y
                        (dx * dx + dy * dy) < 0.45f
                    }
                }
            }
            
            if (hitOther) {
                collision = true
                break
            }
            
            _uiState.update { state ->
                state.copy(arrows = state.arrows.map { 
                    if (it.id == originalArrow.id) it.copy(body = nextBody, head = nextHead) else it 
                })
            }

            // Check if arrow is fully outside the grid
            val buffer = 1.0f 
            val anyPartInGrid = nextBody.any { 
                it.x >= -buffer && it.x < gridSize + buffer && 
                it.y >= -buffer && it.y < gridSize + buffer
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
