package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.data.GameDatabase
import com.example.game.data.GameRepository
import com.example.game.model.GameScore
import com.example.game.viewmodel.GameViewModel
import com.example.game.viewmodel.GameViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.min

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = false) {
                val context = LocalContext.current
                val database = remember { GameDatabase.getDatabase(context) }
                val repository = remember { GameRepository(database.gameScoreDao()) }
                val viewModel: GameViewModel = viewModel(factory = GameViewModelFactory(repository))

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFDF7FF) // Elegant soft light pastel Artistic Flair background
                ) {
                    SuikaMergeApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun SuikaMergeApp(viewModel: GameViewModel) {
    val score by viewModel.score.collectAsState()
    val highScore by viewModel.highScore.collectAsState()
    val isGameOver by viewModel.isGameOver.collectAsState()
    val isWarning by viewModel.isWarning.collectAsState()
    val warningProgress by viewModel.warningProgress.collectAsState()
    val activeTheme by viewModel.activeTheme.collectAsState()
    val circles by viewModel.circles.collectAsState()
    val particles by viewModel.particles.collectAsState()
    val floatingTexts by viewModel.floatingTexts.collectAsState()
    val shakeAmount by viewModel.shakeAmount.collectAsState()

    val currentDropperX by viewModel.currentDropperX.collectAsState()
    val currentFruitLevel by viewModel.currentFruitLevel.collectAsState()
    val nextFruitLevel by viewModel.nextFruitLevel.collectAsState()
    val isReadyToDrop by viewModel.isReadyToDrop.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()

    val haptic = LocalHapticFeedback.current

    // Dialog state controllers
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }

    // Connect ViewModel haptic trigger flow
    LaunchedEffect(Unit) {
        viewModel.hapticTrigger.collect {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // Main game tick loop
    LaunchedEffect(isGameOver) {
        if (!isGameOver) {
            while (true) {
                viewModel.tickPhysics()
                kotlinx.coroutines.delay(16) // Solid 60 FPS update loop
            }
        }
    }

    // Apply screen shake
    val shakeX = if (shakeAmount > 0) ((Math.random() * shakeAmount - shakeAmount / 2).toFloat()).dp else 0.dp
    val shakeY = if (shakeAmount > 0) ((Math.random() * shakeAmount - shakeAmount / 2).toFloat()).dp else 0.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Header Section ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEADDFF), CircleShape)
                        .padding(end = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🍉",
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Fruity Merge",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D1B1E),
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = "Fisika Drop & Gabung",
                        fontSize = 11.sp,
                        color = Color(0xFF49454F),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Info Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFF3EDF7), CircleShape)
                        .border(1.dp, Color(0xFFCAC4D0), CircleShape)
                        .clip(CircleShape)
                        .clickable { showInfoDialog = true }
                        .testTag("info_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Panduan Bermain",
                        tint = Color(0xFF49454F),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Palette Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFF3EDF7), CircleShape)
                        .border(1.dp, Color(0xFFCAC4D0), CircleShape)
                        .clip(CircleShape)
                        .clickable { showThemeDialog = true }
                        .testTag("change_theme_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Pilih Tema",
                        tint = Color(0xFF6750A4),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Restart Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color(0xFFF3EDF7), CircleShape)
                        .border(1.dp, Color(0xFFCAC4D0), CircleShape)
                        .clip(CircleShape)
                        .clickable { viewModel.resetGame() }
                        .testTag("reset_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Mulai Ulang",
                        tint = Color(0xFF381E72),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Score HUD Panel ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Current Score Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEADDFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "SKOR", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    Text(
                        text = "$score",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF6750A4)
                    )
                }
            }

            // High Score Card
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEADDFF))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "TERBAIK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                        Text(
                            text = "$highScore",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF381E72)
                        )
                    }
                    IconButton(
                        onClick = { showLeaderboardDialog = true },
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE8DEF8), CircleShape)
                            .testTag("view_leaderboard_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Riwayat Skor",
                            tint = Color(0xFF1D192B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Next Fruit Preview Card
            Card(
                modifier = Modifier.width(90.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEADDFF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "BERIKUTNYA", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF49454F))
                    Spacer(modifier = Modifier.height(4.dp))
                    val nextItem = viewModel.getThemeItems().getOrNull(nextFruitLevel)
                    if (nextItem != null) {
                        Text(
                            text = nextItem.emoji,
                            fontSize = 24.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // --- Warning Flash Indicator ---
        AnimatedVisibility(
            visible = isWarning,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            val warningAlpha by rememberInfiniteTransition("warning_trans").animateFloat(
                initialValue = 0.3f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "warning_alpha"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFFFB4AB).copy(alpha = warningAlpha * 0.4f + 0.15f))
                    .border(1.5.dp, Color(0xFF93000A), RoundedCornerShape(16.dp))
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚠️ WADAH MELUAP! SEGERA GABUNGKAN! (${(3 * (1f - warningProgress)).toInt() + 1}s)",
                    color = Color(0xFF93000A),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Main Game Arena Frame ---
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .offset(x = shakeX, y = shakeY)
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        colors = if (isWarning) listOf(Color(0xFFFFB4AB), Color(0xFF93000A))
                        else listOf(Color(0xFFD0BCFF), Color(0xFF6750A4))
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .background(Color(0xFFF5EEFA), RoundedCornerShape(32.dp)) // sleek soft pastel container background
                .clip(RoundedCornerShape(32.dp))
        ) {
            val textMeasurer = rememberTextMeasurer()

            // Custom Gesture Input for Aiming and Dropping using standard detectDragGestures
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(isReadyToDrop, isGameOver) {
                        if (isGameOver) return@pointerInput
                        detectDragGestures(
                            onDragStart = { offset ->
                                val canvasWidth = size.width.toFloat()
                                val scale = canvasWidth / GameViewModel.PLAY_WIDTH
                                viewModel.moveDropper(offset.x / scale)
                            },
                            onDrag = { change, dragAmount ->
                                val canvasWidth = size.width.toFloat()
                                val scale = canvasWidth / GameViewModel.PLAY_WIDTH
                                change.consume()
                                viewModel.moveDropper(change.position.x / scale)
                            },
                            onDragEnd = {
                                viewModel.dropFruit()
                            }
                        )
                    }
                    .testTag("game_canvas_container")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    
                    // Responsive Scale Factor
                    val scale = canvasWidth / GameViewModel.PLAY_WIDTH
                    
                    // 1. Draw Dotted Aim Guide Line
                    if (isReadyToDrop && !isGameOver) {
                        val activeItem = viewModel.getThemeItems().getOrNull(currentFruitLevel)
                        if (activeItem != null) {
                            val guideX = currentDropperX * scale
                            val startY = GameViewModel.DROP_HEIGHT * scale
                            
                            // Simple collision-raycast approximation to find guide depth
                            var maxGuideY = GameViewModel.PLAY_HEIGHT * scale
                            for (c in circles) {
                                if (c.isNew) continue
                                val cx = c.x * scale
                                val cy = c.y * scale
                                val cr = c.radius * scale
                                // If vertical dropper path overlaps horizontally with fruit width bounds
                                if (guideX + (activeItem.radiusDp * scale) > cx - cr && 
                                    guideX - (activeItem.radiusDp * scale) < cx + cr) {
                                    val intersectY = cy - cr
                                    if (intersectY in startY..maxGuideY) {
                                        maxGuideY = intersectY
                                    }
                                }
                            }

                            drawLine(
                                color = Color(activeItem.color).copy(alpha = 0.4f),
                                start = Offset(guideX, startY),
                                end = Offset(guideX, maxGuideY),
                                strokeWidth = 2.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                            )
                        }
                    }

                    // 2. Draw Overflow / Warning Line
                    val warningLineY = GameViewModel.WARNING_LINE_Y * scale
                    drawLine(
                        color = if (isWarning) Color(0xFF93000A) else Color(0xFF6750A4).copy(alpha = 0.4f),
                        start = Offset(0f, warningLineY),
                        end = Offset(canvasWidth, warningLineY),
                        strokeWidth = if (isWarning) 3.dp.toPx() else 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 10f), 0f)
                    )

                    // 3. Draw Dropper Fruit (Ready at Top)
                    if (isReadyToDrop && !isGameOver) {
                        val activeItem = viewModel.getThemeItems().getOrNull(currentFruitLevel)
                        if (activeItem != null) {
                            val dropX = currentDropperX * scale
                            val dropY = GameViewModel.DROP_HEIGHT * scale
                            val r = activeItem.radiusDp * scale

                            // Draw glossy ball background
                            drawCircle(
                                color = Color(activeItem.color),
                                radius = r,
                                center = Offset(dropX, dropY)
                            )
                            // Gloss highlight
                            drawCircle(
                                color = Color.White.copy(alpha = 0.3f),
                                radius = r * 0.25f,
                                center = Offset(dropX - r * 0.35f, dropY - r * 0.35f)
                            )
                            // 3D Shadow Overlay
                            drawCircle(
                                color = Color.Black.copy(alpha = 0.15f),
                                radius = r * 0.75f,
                                center = Offset(dropX + r * 0.2f, dropY + r * 0.2f),
                                style = Stroke(width = r * 0.15f)
                            )

                            // Draw Emoji centered
                            val emojiFontSize = (activeItem.radiusDp * 1.15f) * scale
                            val textLayout = textMeasurer.measure(
                                text = activeItem.emoji,
                                style = TextStyle(fontSize = emojiFontSize.sp)
                            )
                            drawText(
                                textLayoutResult = textLayout,
                                topLeft = Offset(dropX - textLayout.size.width / 2f, dropY - textLayout.size.height / 2f)
                            )
                        }
                    }

                    // 4. Draw Active Falling/Stacked Game Fruits
                    for (c in circles) {
                        val cx = c.x * scale
                        val cy = c.y * scale
                        val cr = c.radius * scale
                        val item = viewModel.getThemeItems().getOrNull(c.level) ?: continue

                        // Glossy ball background
                        drawCircle(
                            color = Color(c.colorHex),
                            radius = cr,
                            center = Offset(cx, cy)
                        )
                        // 3D Spherical Highlights
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f),
                            radius = cr * 0.25f,
                            center = Offset(cx - cr * 0.32f, cy - cr * 0.32f)
                        )
                        // Dark Shadow ring for depth
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.15f),
                            radius = cr * 0.75f,
                            center = Offset(cx + cr * 0.2f, cy + cr * 0.2f),
                            style = Stroke(width = cr * 0.15f)
                        )

                        // Draw Fruit Emoji
                        val emojiFontSize = (item.radiusDp * 1.15f) * scale
                        val textLayout = textMeasurer.measure(
                            text = item.emoji,
                            style = TextStyle(fontSize = emojiFontSize.sp)
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(cx - textLayout.size.width / 2f, cy - textLayout.size.height / 2f)
                        )
                    }

                    // 5. Draw Explosive Merge Particles
                    for (p in particles) {
                        drawCircle(
                            color = Color(p.colorHex).copy(alpha = p.alpha),
                            radius = p.radius * scale,
                            center = Offset(p.x * scale, p.y * scale)
                        )
                    }

                    // 6. Draw Floating Score Popup Text
                    for (t in floatingTexts) {
                        val textLayout = textMeasurer.measure(
                            text = t.text,
                            style = TextStyle(
                                color = Color(0xFF381E72).copy(alpha = t.alpha), // High contrast deep purple text
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black
                            )
                        )
                        drawText(
                            textLayoutResult = textLayout,
                            topLeft = Offset(t.x * scale - textLayout.size.width / 2f, t.y * scale - textLayout.size.height / 2f)
                        )
                    }
                }
            }

            // --- Game Over Fullscreen Overlay ---
            if (isGameOver) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1D1B1E).copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text(
                            text = "PERMAINAN SELESAI",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFB4AB),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Skor Akhir Anda",
                            fontSize = 14.sp,
                            color = Color(0xFFEADDFF),
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "$score",
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        Button(
                            onClick = { viewModel.resetGame() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6750A4),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(52.dp)
                                .testTag("restart_button")
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Main Lagi", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- Evolution Merge Path (Cheat Sheet Footer) ---
        Column(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            Text(
                text = "Alur Penggabungan (Gabung ke level berikutnya!):",
                fontSize = 11.sp,
                color = Color(0xFF49454F),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .background(Color(0xFFF3EDF7), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFFEADDFF), RoundedCornerShape(16.dp))
                    .padding(vertical = 10.dp, horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val items = viewModel.getThemeItems()
                items.forEachIndexed { index, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = item.emoji, fontSize = 22.sp)
                            Text(
                                text = "Lvl ${item.level}",
                                fontSize = 9.sp,
                                color = Color(0xFF49454F),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (index < items.size - 1) {
                            Text(
                                text = "➔",
                                fontSize = 12.sp,
                                color = Color(0xFF6750A4),
                                modifier = Modifier.padding(horizontal = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // --- Bottom/Modal Dialogs ---

    // 1. Theme Selection Dialog
    if (showThemeDialog) {
        Dialog(onDismissRequest = { showThemeDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEADDFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎨 Pilih Tema Game",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D1B1E)
                    )
                    Text(
                        text = "Sesuaikan tampilan buah dan item game!",
                        fontSize = 12.sp,
                        color = Color(0xFF49454F),
                        modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
                    )

                    // Theme Buttons
                    listOf(
                        "FRUIT" to "🍒 Buah Segar (Klasik)",
                        "EMOJI" to "😊 Slime Emoji (Imut)",
                        "SPACE" to "🚀 Luar Angkasa (Fiksi Ilmiah)"
                    ).forEach { (themeCode, themeName) ->
                        Button(
                            onClick = {
                                viewModel.setTheme(themeCode)
                                showThemeDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (activeTheme == themeCode) Color(0xFF6750A4) else Color(0xFFF3EDF7),
                                contentColor = if (activeTheme == themeCode) Color.White else Color(0xFF1D1B1E)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .height(48.dp)
                                .testTag("theme_btn_$themeCode")
                        ) {
                            Text(
                                text = themeName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showThemeDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Tutup", color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    // 2. Leaderboard Dialog
    if (showLeaderboardDialog) {
        Dialog(onDismissRequest = { showLeaderboardDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEADDFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🏆 Papan Peringkat",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1D1B1E)
                        )
                        IconButton(
                            onClick = { viewModel.clearLeaderboard() },
                            modifier = Modifier.testTag("clear_leaderboard_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Hapus Riwayat",
                                tint = Color(0xFF93000A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (leaderboard.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(text = "📭", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Belum ada riwayat skor.\nMainkan game dan raih rekor pertamamu!",
                                fontSize = 13.sp,
                                color = Color(0xFF49454F),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(leaderboard) { index, scoreEntry ->
                                val dateStr = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                                    .format(Date(scoreEntry.timestamp))
                                val medal = when (index) {
                                    0 -> "🥇 "
                                    1 -> "🥈 "
                                    2 -> "🥉 "
                                    else -> "#${index + 1} "
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF3EDF7), RoundedCornerShape(16.dp))
                                        .border(1.dp, Color(0xFFEADDFF), RoundedCornerShape(16.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = medal,
                                            fontWeight = FontWeight.Bold,
                                            color = if (index < 3) Color(0xFFFFD700) else Color(0xFF49454F),
                                            fontSize = 15.sp
                                        )
                                        Column {
                                            Text(
                                                text = "Skor: ${scoreEntry.score}",
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF1D1B1E),
                                                fontSize = 14.sp
                                            )
                                            Text(
                                                text = "Tema: ${scoreEntry.theme} | $dateStr",
                                                color = Color(0xFF49454F),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                    // Display max fruit achieved in that game
                                    val themeItems = viewModel.getThemeItems(scoreEntry.theme)
                                    val maxFruitAchieved = themeItems.getOrNull(scoreEntry.highestLevel)
                                    if (maxFruitAchieved != null) {
                                        Text(
                                            text = maxFruitAchieved.emoji,
                                            fontSize = 20.sp,
                                            modifier = Modifier.padding(end = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showLeaderboardDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Tutup", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }

    // 3. Info / How to play Dialog
    if (showInfoDialog) {
        Dialog(onDismissRequest = { showInfoDialog = false }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEADDFF)),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📖 Cara Bermain",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1D1B1E)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val infoText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                            append("1. Geser & Aim:\n")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF1D1B1E))) {
                            append("Sentuh dan geser jari Anda ke kiri atau kanan pada area permainan untuk mengarahkan jalur jatuhnya buah.\n\n")
                        }

                        withStyle(style = SpanStyle(color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                            append("2. Jatuhkan Buah:\n")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF1D1B1E))) {
                            append("Lepaskan sentuhan jari Anda untuk menjatuhkan buah ke dalam wadah.\n\n")
                        }

                        withStyle(style = SpanStyle(color = Color(0xFF6750A4), fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                            append("3. Gabung & Evolusi:\n")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF1D1B1E))) {
                            append("Dua buah yang identik/sama tingkatannya jika bersentuhan akan melebur menjadi buah baru yang lebih besar dan bernilai skor tinggi!\n\n")
                        }

                        withStyle(style = SpanStyle(color = Color(0xFF93000A), fontWeight = FontWeight.Bold, fontSize = 12.sp)) {
                            append("4. Aturan Kalah:\n")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFF1D1B1E))) {
                            append("Jangan biarkan tumpukan buah meluap melebihi garis batas di bagian atas selama lebih dari 3 detik, atau permainan selesai!")
                        }
                    }

                    Text(
                        text = infoText,
                        fontSize = 11.5.sp,
                        lineHeight = 17.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF3EDF7), RoundedCornerShape(16.dp))
                            .border(1.dp, Color(0xFFEADDFF), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6750A4),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text("Saya Mengerti", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
    }
}
