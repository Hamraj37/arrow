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
import com.arrow37.ui.ads.UnityBanner
import com.arrow37.ui.ads.UnityAdsManager
import android.app.Activity
import androidx.compose.ui.platform.LocalContext

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
    val context = LocalContext.current
    val activity = context as? Activity

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
                        if (state.showGrid) Icons.Rounded.GridOn else Icons.Rounded.GridOff,
                        contentDescription = "Toggle Grid"
                    )
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            UnityBanner(placementId = "Banner_Android")
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
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
                            Button(onClick = {
                                if (activity != null) {
                                    UnityAdsManager.showRewardedAd(activity) {
                                        onReset()
                                    }
                                } else {
                                    onReset()
                                }
                            }) {
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
                            Button(onClick = {
                                if (activity != null) {
                                    UnityAdsManager.showRewardedAd(activity) {
                                        onNextLevel()
                                    }
                                } else {
                                    onNextLevel()
                                }
                            }) {
                                Text("Next Level")
                            }
                        }
                    )
                }
            }

            UnityBanner(placementId = "Banner_Android")
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

    val strokeWidth = cellSizePx * 0.28f
    val headWidth = cellSizePx * 0.6f
    val headLength = cellSizePx * 0.5f
    
    val headPoint = arrow.body.first()
    val tailPoint = arrow.body.last()
    
    val headX = headPoint.x * cellSizePx + cellSizePx / 2
    val headY = headPoint.y * cellSizePx + cellSizePx / 2

    // 1. Draw the body line
    val path = Path()
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
    val headPath = Path()
    
    // Tip of the arrow - placed near the cell edge
    val tipX = headX + arrow.direction.dx * (cellSizePx * 0.42f)
    val tipY = headY + arrow.direction.dy * (cellSizePx * 0.42f)
    
    // Base center of the head triangle
    val baseX = tipX - arrow.direction.dx * headLength
    val baseY = tipY - arrow.direction.dy * headLength
    
    // Orthogonal vector for the base width
    val ux = -arrow.direction.dy * (headWidth / 2f)
    val uy = arrow.direction.dx * (headWidth / 2f)
    
    headPath.moveTo(tipX, tipY)
    headPath.lineTo(baseX + ux, baseY + uy)
    headPath.lineTo(baseX - ux, baseY - uy)
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
                    Arrow("1", Point(4f, 4f), Direction.UP, listOf(Point(4f, 4f), Point(4f, 5f), Point(4f, 6f))),
                    Arrow("2", Point(2f, 2f), Direction.RIGHT, listOf(Point(2f, 2f), Point(1f, 2f))),
                    Arrow("3", Point(0f, 1f), Direction.DOWN, listOf(Point(0f, 1f), Point(0f, 0f))),
                    Arrow("4", Point(6f, 1f), Direction.LEFT, listOf(Point(6f, 1f), Point(7f, 1f))),
                    Arrow("5", Point(1f, 5f), Direction.DOWN, listOf(Point(1f, 5f), Point(1f, 4f))),
                    Arrow("6", Point(5f, 7f), Direction.RIGHT, listOf(Point(5f, 7f), Point(4f, 7f), Point(3f, 7f))),
                    Arrow("7", Point(6f, 6f), Direction.UP, listOf(Point(6f, 6f), Point(6f, 7f))),
                    Arrow("8", Point(0f, 7f), Direction.LEFT, listOf(Point(0f, 7f), Point(1f, 7f))),
                    Arrow("9", Point(3f, 3f), Direction.RIGHT, listOf(Point(3f, 3f), Point(2f, 3f))),
                    Arrow("10", Point(5f, 3f), Direction.LEFT, listOf(Point(5f, 3f), Point(6f, 3f))),
                    Arrow("11", Point(2f, 5f), Direction.UP, listOf(Point(2f, 5f), Point(2f, 6f))),
                    Arrow("12", Point(6f, 5f), Direction.LEFT, listOf(Point(6f, 5f), Point(7f, 5f))),
                    Arrow("13", Point(3f, 1f), Direction.RIGHT, listOf(Point(3f, 1f), Point(2f, 1f), Point(1f, 1f))),
                    Arrow("14", Point(4f, 0f), Direction.RIGHT, listOf(Point(4f, 0f), Point(3f, 0f), Point(2f, 0f))),
                    Arrow("15", Point(5f, 0f), Direction.LEFT, listOf(Point(5f, 0f), Point(6f, 0f))),
                    Arrow("16", Point(0f, 3f), Direction.UP, listOf(Point(0f, 3f), Point(0f, 4f))),
                    Arrow("17", Point(4f, 2f), Direction.RIGHT, listOf(Point(4f, 2f), Point(3f, 2f))),
                    Arrow("18", Point(5f, 2f), Direction.LEFT, listOf(Point(5f, 2f), Point(6f, 2f))),
                    Arrow("19", Point(7f, 7f), Direction.UP, listOf(Point(7f, 7f))),
                    Arrow("20", Point(0f, 6f), Direction.RIGHT, listOf(Point(0f, 6f))),
                    Arrow("21", Point(3f, 4f), Direction.DOWN, listOf(Point(3f, 4f))),
                    Arrow("22", Point(5f, 4f), Direction.DOWN, listOf(Point(5f, 4f))),
                    Arrow("23", Point(4f, 1f), Direction.UP, listOf(Point(4f, 1f))),
                    Arrow("24", Point(5f, 1f), Direction.UP, listOf(Point(5f, 1f)))
                )
            )
        )
    }
}
