package com.arrow37.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arrow37.ui.theme.ArrowTheme
import com.arrow37.ui.theme.LocalAppStrings
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(
    maxLevel: Int,
    currentLevel: Int,
    levelStars: Map<Int, Int>,
    onLevelSelect: (Int) -> Unit,
    onBack: () -> Unit
) {
    val strings = LocalAppStrings.current
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        strings.selectLevel, 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 2.sp
                        )
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 80.dp),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).fillMaxSize()
        ) {
            items(maxLevel) { index ->
                val level = index + 1
                LevelItem(
                    level = level,
                    isCurrent = level == currentLevel,
                    stars = levelStars[level] ?: 0,
                    onClick = { onLevelSelect(level) }
                )
            }
        }
    }
}

@Composable
fun LevelItem(
    level: Int,
    isCurrent: Boolean,
    stars: Int,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrent) Color(0xFF4FC3F7) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = level.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            
            // Fixed height container for stars to prevent text jumping
            Box(
                modifier = Modifier.height(28.dp),
                contentAlignment = Alignment.Center
            ) {
                if (stars > 0) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) { index ->
                            Icon(
                                imageVector = if (index < stars) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                contentDescription = null,
                                tint = if (index < stars) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.2f),
                                modifier = Modifier
                                    .size(20.dp)
                                    .padding(horizontal = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LevelSelectionPreview() {
    ArrowTheme {
        LevelSelectionScreen(
            maxLevel = 15,
            currentLevel = 5,
            levelStars = mapOf(1 to 3, 2 to 2, 3 to 3, 4 to 1),
            onLevelSelect = {},
            onBack = {}
        )
    }
}
