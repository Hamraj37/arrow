package com.arrow37.ui.puzzle

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.arrow37.ui.theme.ArrowTheme
import com.arrow37.data.Arrow
import com.arrow37.data.Direction
import com.arrow37.data.GameState
import com.arrow37.data.Point
import com.arrow37.ui.theme.ArrowBlue
import com.arrow37.ui.theme.RedLives
import com.arrow37.viewmodel.GameViewModel

@Composable
fun PuzzleScreen(
    viewModel: GameViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    PuzzleContent(
        state = state,
        onBack = onBack,
        onSettings = onSettings,
        onUndo = { viewModel.undo() },
        onRedo = { viewModel.redo() },
        onReset = { viewModel.reset() },
        onArrowClick = { viewModel.onArrowTapped(it) },
        onNextLevel = { viewModel.nextLevel() },
        onToggleGrid = { viewModel.toggleGrid() },
        onToggleSound = { viewModel.toggleSound() },
        onToggleVibration = { viewModel.toggleVibration() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PuzzleContent(
    state: GameState,
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onUndo: () -> Unit = {},
    onRedo: () -> Unit = {},
    onReset: () -> Unit = {},
    onArrowClick: (String) -> Unit = {},
    onNextLevel: () -> Unit = {},
    onToggleGrid: () -> Unit = {},
    onToggleSound: () -> Unit = {},
    onToggleVibration: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.levelName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                        )
                        Row(horizontalArrangement = Arrangement.Center) {
                            repeat(3) { index ->
                                Icon(
                                    imageVector = Icons.Rounded.Favorite,
                                    contentDescription = null,
                                    tint = if (index < state.lives) RedLives else Color.Gray.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp).padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                        }
                        IconButton(onClick = onSettings) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onUndo, enabled = state.history.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = "Undo", 
                            tint = if (state.history.isNotEmpty()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(onClick = onRedo, enabled = state.redoStack.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Redo, 
                            contentDescription = "Redo", 
                            tint = if (state.redoStack.isNotEmpty()) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = onToggleGrid,
                    shape = CircleShape,
                    containerColor = if (state.showGrid) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (state.showGrid) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    Icon(
                        if (state.showGrid) Icons.Rounded.GridOn else Icons.Rounded.GridOn,
                        contentDescription = "Toggle Grid"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            PuzzleGrid(
                arrows = state.arrows,
                gridSize = state.gridSize,
                showGrid = state.showGrid,
                onArrowClick = onArrowClick
            )
            
            if (state.isGameOver) {
                AlertDialog(
                    onDismissRequest = onReset,
                    title = { Text("Game Over") },
                    text = { Text("You ran out of lives!") },
                    confirmButton = {
                        Button(onClick = onReset) {
                            Text("Try Again")
                        }
                    }
                )
            }

            if (state.isLevelCleared) {
                AlertDialog(
                    onDismissRequest = onNextLevel,
                    title = { Text("Level Cleared!") },
                    text = { Text("Congratulations!") },
                    confirmButton = {
                        Button(onClick = onNextLevel) {
                            Text("Next Level")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PuzzleGrid(
    arrows: List<Arrow>,
    gridSize: Int,
    showGrid: Boolean,
    onArrowClick: (String) -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        val size = min(maxWidth, maxHeight)
        val cellSize = size / gridSize
        val density = androidx.compose.ui.platform.LocalDensity.current
        val cellSizePx = with(density) { cellSize.toPx() }
        val gridPx = cellSizePx * gridSize

        // Grid Container to keep everything centered and square
        Box(
            modifier = Modifier.size(size)
        ) {
            // Grid Background
            if (showGrid) {
                val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidthPx = 1.dp.toPx()
                    for (i in 0..gridSize) {
                        val pos = i * cellSizePx
                        drawLine(gridColor, Offset(pos, 0f), Offset(pos, gridPx), strokeWidth = strokeWidthPx)
                        drawLine(gridColor, Offset(0f, pos), Offset(gridPx, pos), strokeWidth = strokeWidthPx)
                    }
                }
            }

            // Click detection layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(arrows, gridSize) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                event.changes.forEach { change ->
                                    if (change.pressed && !change.previousPressed) {
                                        val offset = change.position
                                        val x = offset.x / cellSizePx
                                        val y = offset.y / cellSizePx
                                        
                                        // Find arrow that occupies this cell
                                        val clickedArrow = arrows.find { arrow ->
                                            !arrow.isEscaped && arrow.body.any { p -> 
                                                x >= p.x && x < p.x + 1f && y >= p.y && y < p.y + 1f
                                            }
                                        }
                                        clickedArrow?.let { onArrowClick(it.id) }
                                    }
                                }
                            }
                        }
                    }
            )

            Canvas(modifier = Modifier.fillMaxSize()) {
                val arrowColor = primary
                arrows.forEach { arrow ->
                    if (!arrow.isEscaped) {
                        drawArrow(arrow, cellSizePx, arrowColor)
                    }
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArrow(
    arrow: Arrow,
    cellSizePx: Float,
    color: Color
) {
    if (arrow.body.isEmpty()) return

    val strokeWidth = cellSizePx * 0.35f
    val arrowHeadSize = cellSizePx * 0.7f
    
    // 1. Draw the body line
    val path = Path()
    // The head of the arrow is body[0]
    val headPoint = arrow.body.first()
    val tailPoint = arrow.body.last()
    
    // Draw from tail towards the head
    path.moveTo(tailPoint.x * cellSizePx + cellSizePx / 2, tailPoint.y * cellSizePx + cellSizePx / 2)
    
    for (i in arrow.body.size - 2 downTo 0) {
        val p = arrow.body[i]
        path.lineTo(p.x * cellSizePx + cellSizePx / 2, p.y * cellSizePx + cellSizePx / 2)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = strokeWidth,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )

    // 2. Draw the head triangle
    val headX = headPoint.x * cellSizePx + cellSizePx / 2
    val headY = headPoint.y * cellSizePx + cellSizePx / 2
    
    val headPath = Path()
    // Shift the head triangle forward so it sits at the leading edge of the cell
    val shift = cellSizePx * 0.15f
    val tx = headX + arrow.direction.dx * shift
    val ty = headY + arrow.direction.dy * shift
    
    val hw = arrowHeadSize * 0.65f // half-width
    val hl = arrowHeadSize * 0.8f // half-length
    
    when (arrow.direction) {
        Direction.UP -> {
            headPath.moveTo(tx - hw, ty + hl * 0.4f)
            headPath.lineTo(tx, ty - hl * 0.6f)
            headPath.lineTo(tx + hw, ty + hl * 0.4f)
        }
        Direction.DOWN -> {
            headPath.moveTo(tx - hw, ty - hl * 0.4f)
            headPath.lineTo(tx, ty + hl * 0.6f)
            headPath.lineTo(tx + hw, ty - hl * 0.4f)
        }
        Direction.LEFT -> {
            headPath.moveTo(tx + hl * 0.4f, ty - hw)
            headPath.lineTo(tx - hl * 0.6f, ty)
            headPath.lineTo(tx + hl * 0.4f, ty + hw)
        }
        Direction.RIGHT -> {
            headPath.moveTo(tx - hl * 0.4f, ty - hw)
            headPath.lineTo(tx + hl * 0.6f, ty)
            headPath.lineTo(tx - hl * 0.4f, ty + hw)
        }
    }
    headPath.close()

    drawPath(
        path = headPath,
        color = color
    )
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp,navigation=buttons")
@Composable
fun PuzzleScreenPreview() {
    ArrowTheme {
        PuzzleContent(
            state = GameState(
                levelName = "Preview Level",
                lives = 3,
                gridSize = 8,
                arrows = listOf(
                    Arrow(
                        id = "1",
                        head = Point(4f, 4f),
                        direction = Direction.UP,
                        body = listOf(Point(4f, 4f), Point(4f, 5f), Point(4f, 6f))
                    ),
                    Arrow(
                        id = "2",
                        head = Point(2f, 2f),
                        direction = Direction.RIGHT,
                        body = listOf(Point(2f, 2f), Point(1f, 2f))
                    )
                )
            )
        )
    }
}
