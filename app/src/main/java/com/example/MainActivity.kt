package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.LocalPattern
import com.example.data.model.TheiaLog
import com.example.data.model.TheiaVaultNote
import com.example.ui.theme.*
import com.example.ui.viewmodel.ConnectionMode
import com.example.ui.viewmodel.TheiaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private val viewModel: TheiaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                TheiaAppScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun TheiaAppScreen(viewModel: TheiaViewModel) {
    var activeTab by remember { mutableStateOf(0) }

    // State bindings
    val connectionMode by viewModel.connectionMode.collectAsStateWithLifecycle()
    val promptInput by viewModel.promptInput.collectAsStateWithLifecycle()
    val transcription by viewModel.transcription.collectAsStateWithLifecycle()
    val responseOutput by viewModel.responseOutput.collectAsStateWithLifecycle()
    val thoughtDump by viewModel.thoughtDump.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val latencyMs by viewModel.latencyMs.collectAsStateWithLifecycle()
    val engineUsed by viewModel.engineUsed.collectAsStateWithLifecycle()

    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val patterns by viewModel.patterns.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = TheiaBackground,
        bottomBar = {
            TheiaBottomNavigation(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Top App Bar
            TheiaTopAppBar(
                connectionMode = connectionMode,
                onModeChange = { viewModel.setConnectionMode(it) }
            )

            // Content Area based on Tab Selector
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    0 -> HUDHubContent(
                        transcription = transcription,
                        responseOutput = responseOutput,
                        thoughtDump = thoughtDump,
                        isRecording = isRecording,
                        isLoading = isLoading,
                        latencyMs = latencyMs,
                        engineUsed = engineUsed,
                        promptInput = promptInput,
                        onPromptChange = { viewModel.setPromptInput(it) },
                        onSendText = { viewModel.submitTextPrompt() },
                        onMicToggle = { viewModel.toggleRecording() }
                    )
                    1 -> TheiaVaultContent(
                        notes = notes,
                        onCreateNote = { title, content, category ->
                            viewModel.createVaultNote(title, content, category)
                        },
                        onDeleteNote = { viewModel.deleteVaultNote(it) }
                    )
                    2 -> SystemInsightsContent(
                        patterns = patterns,
                        logs = logs,
                        onClearLogs = { viewModel.clearAllLogs() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TheiaTopAppBar(
    connectionMode: ConnectionMode,
    onModeChange: (ConnectionMode) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = TheiaSurfaceVariant),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Soundwave Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(TheiaPrimary, Color.Transparent)
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Sensors,
                    contentDescription = "Theia Soundwave Link",
                    tint = TheiaPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Branding labels
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "THEIA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TheiaPrimary,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Bilişsel Protez v3.0",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TheiaSecondary
                )
            }

            // Connection selection dropdown/row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(TheiaSurfaceCard, RoundedCornerShape(12.dp))
                    .border(1.dp, TheiaOutline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                ConnectionModeButton(
                    mode = ConnectionMode.ONLINE,
                    active = connectionMode == ConnectionMode.ONLINE,
                    icon = Icons.Default.Wifi,
                    onClick = { onModeChange(ConnectionMode.ONLINE) }
                )
                ConnectionModeButton(
                    mode = ConnectionMode.ONLINE_METERED,
                    active = connectionMode == ConnectionMode.ONLINE_METERED,
                    icon = Icons.Default.SignalCellularAlt,
                    onClick = { onModeChange(ConnectionMode.ONLINE_METERED) }
                )
                ConnectionModeButton(
                    mode = ConnectionMode.OFFLINE,
                    active = connectionMode == ConnectionMode.OFFLINE,
                    icon = Icons.Default.CloudOff,
                    onClick = { onModeChange(ConnectionMode.OFFLINE) }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // User Portrait (İsmail)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(2.dp, TheiaPrimary, CircleShape)
                    .background(TheiaSurfaceCard, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "İK",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TheiaPrimary
                )
                // Simulated green online led on user picture corner
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFF00FF7F), CircleShape)
                        .border(1.dp, TheiaBackground, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            }
        }
    }
}

@Composable
fun ConnectionModeButton(
    mode: ConnectionMode,
    active: Boolean,
    icon: ImageVector,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(32.dp)
            .background(
                if (active) TheiaPrimary.copy(alpha = 0.15f) else Color.Transparent,
                RoundedCornerShape(8.dp)
            )
            .testTag("mode_${mode.name.lowercase()}")
    ) {
        Icon(
            imageVector = icon,
            contentDescription = mode.name,
            tint = if (active) TheiaPrimary else TheiaSecondary.copy(alpha = 0.6f),
            modifier = Modifier.size(16.dp)
        )
    }
}

@Composable
fun HUDHubContent(
    transcription: String,
    responseOutput: String,
    thoughtDump: List<String>,
    isRecording: Boolean,
    isLoading: Boolean,
    latencyMs: Long,
    engineUsed: String,
    promptInput: String,
    onPromptChange: (String) -> Unit,
    onSendText: () -> Unit,
    onMicToggle: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Latency tracker HUD on top
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = TheiaSurfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (isLoading) Color.Yellow else TheiaPrimary, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isLoading) "Hesaplanıyor..." else "Engine: $engineUsed",
                            color = TheiaSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "Gecikme: ${latencyMs}ms",
                        color = if (latencyMs > 2000) Color(0xFFFFB4AB) else TheiaPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Concentric Voice Visualization ring
        item {
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Pulse Animation effect on record
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = if (isRecording) 1.25f else 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale"
                )
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.1f,
                    targetValue = if (isRecording) 0.35f else 0.15f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )

                // Background glow circle
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(TheiaPrimary.copy(alpha = pulseAlpha), Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Animated concentric radar ring
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(TheiaPrimary.copy(alpha = 0.1f), TheiaPrimary.copy(alpha = 0.5f))
                            ),
                            shape = CircleShape
                        )
                )

                Box(
                    modifier = Modifier
                        .size(165.dp)
                        .border(
                            width = 1.dp,
                            color = TheiaPrimary.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                )

                // Floating glass Mic control
                OutlinedButton(
                    onClick = onMicToggle,
                    modifier = Modifier
                        .size(100.dp)
                        .background(TheiaSurfaceCard.copy(alpha = 0.85f), CircleShape)
                        .border(2.dp, TheiaPrimary, CircleShape)
                        .testTag("mic_toggle_button"),
                    shape = CircleShape,
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Dictation control",
                        tint = if (isRecording) Color.Red else TheiaPrimary,
                        modifier = Modifier.size(44.dp)
                    )
                }

                // Interactive Audio sound waves when active
                if (isRecording) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..5) {
                            val waveHeight by infiniteTransition.animateFloat(
                                initialValue = 8f,
                                targetValue = Random.nextInt(25, 55).toFloat(),
                                animationSpec = infiniteRepeatable(
                                    animation = tween(150 + i * 40, easing = FastOutSlowInEasing),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "wave"
                            )
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.5.dp)
                                    .width(3.dp)
                                    .height(waveHeight.dp)
                                    .background(TheiaPrimary, RoundedCornerShape(1.5.dp))
                            )
                        }
                    }
                }
            }
        }

        // Live transcription readout
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TheiaSurfaceVariant, RoundedCornerShape(16.dp))
                    .border(1.dp, TheiaOutline.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "LIVE SPEECH TRANSCRIPTION",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = TheiaPrimary.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = transcription.ifEmpty { "Gözlüğü tetiklemek için sesli tuşa basın veya aşağıdaki konsolu kullanın..." },
                        fontSize = 14.sp,
                        color = if (transcription.isEmpty()) TheiaSecondary.copy(alpha = 0.5f) else TheiaOnSurface,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // ADHD-friendly Response summary card
        item {
            AnimatedVisibility(
                visible = responseOutput.isNotEmpty(),
                enter = fadeIn() + slideInVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TheiaSurfaceVariant),
                    border = BorderStroke(1.2.dp, TheiaPrimary.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Theia response",
                                tint = TheiaPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "THEIA BİLİŞSEL ÖZET",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TheiaPrimary,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // High fidelity responsive clean summary text
                        Text(
                            text = responseOutput,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.Normal,
                            color = TheiaOnSurface
                        )
                    }
                }
            }
        }

        // Text Console manual input alternative
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TheiaSurfaceVariant, RoundedCornerShape(16.dp))
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = promptInput,
                    onValueChange = onPromptChange,
                    placeholder = { Text("Konsol girdisi yazın...", color = TheiaSecondary.copy(alpha = 0.5f)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("text_input_field"),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TheiaOnSurface,
                        unfocusedTextColor = TheiaOnSurface
                    ),
                    maxLines = 2,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        onSendText()
                        keyboardController?.hide()
                    })
                )

                IconButton(
                    onClick = {
                        onSendText()
                        keyboardController?.hide()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(TheiaPrimary, CircleShape)
                        .testTag("send_text_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send text prompt",
                        tint = TheiaBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Terminal style "Düşünce Dökümü" panel
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF060E20), RoundedCornerShape(16.dp))
                    .border(1.dp, TheiaOutline.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Execution sequence",
                            tint = LogColorForEngine(engineUsed),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LOKAL KARAR AKIŞ PANELİ (NPM)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TheiaSecondary,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Text(
                        text = "Living Log",
                        fontSize = 9.sp,
                        color = TheiaPrimary.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    thoughtDump.forEach { step ->
                        Text(
                            text = "> $step",
                            color = if (step.startsWith("Error") || step.contains("fail", true)) Color(0xFFFFB4AB) else Color(0xFFAEB9D0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Bottom space spacer
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun TheiaVaultContent(
    notes: List<TheiaVaultNote>,
    onCreateNote: (String, String, String) -> Unit,
    onDeleteNote: (TheiaVaultNote) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("daily_notes") }
    var showDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Book,
                    contentDescription = "Empty memory",
                    tint = TheiaOutline,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "TheiaVault Boş",
                    fontSize = 16.sp,
                    color = TheiaOnSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "İsmail'in kararları için henüz kişisel data yok.",
                    fontSize = 13.sp,
                    color = TheiaSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "THEIAVAULT KİŞİSEL HAFIZA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TheiaPrimary,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }

                items(notes) { note ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = TheiaSurfaceVariant),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = when (note.category) {
                                                    "frequent_notes" -> TheiaPrimary
                                                    "daily_notes" -> Color(0xFF87CEFA)
                                                    else -> Color(0xFFFFB4AB)
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = note.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TheiaOnSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteNote(note) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete notes",
                                        tint = Color(0xFFFFB4AB),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = note.content,
                                fontSize = 13.sp,
                                color = TheiaSecondary,
                                lineHeight = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(
                                        Date(note.timestamp)
                                    ),
                                    fontSize = 10.sp,
                                    color = TheiaSecondary.copy(alpha = 0.5f)
                                )

                                Text(
                                    text = note.category.uppercase(),
                                    fontSize = 9.sp,
                                    color = TheiaPrimary.copy(alpha = 0.6f),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier
                                        .background(TheiaPrimary.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Offset to prevent overlap with FAB
                }
            }
        }

        // Create Note Floating button
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 16.dp, end = 16.dp)
                .testTag("new_note_add_button"),
            containerColor = TheiaPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add new reference", tint = TheiaBackground)
        }

        // Add Note Material Dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                containerColor = TheiaSurfaceVariant,
                title = { Text("TheiaVault Bilgi Ekle", color = TheiaPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Başlık", color = TheiaSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TheiaOnSurface,
                                unfocusedTextColor = TheiaOnSurface,
                                focusedBorderColor = TheiaPrimary,
                                unfocusedBorderColor = TheiaOutline
                            )
                        )

                        OutlinedTextField(
                            value = contentInput,
                            onValueChange = { contentInput = it },
                            label = { Text("Not Detayı", color = TheiaSecondary) },
                            modifier = Modifier.height(100.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TheiaOnSurface,
                                unfocusedTextColor = TheiaOnSurface,
                                focusedBorderColor = TheiaPrimary,
                                unfocusedBorderColor = TheiaOutline
                            )
                        )

                        // Category row selection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("daily_notes", "frequent_notes").forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.replace("_", " ").uppercase(), fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = TheiaPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = TheiaPrimary,
                                        labelColor = TheiaSecondary
                                    )
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = TheiaPrimary),
                        onClick = {
                            if (titleInput.isNotBlank() && contentInput.isNotBlank()) {
                                onCreateNote(titleInput, contentInput, selectedCategory)
                                titleInput = ""
                                contentInput = ""
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Yazdır", color = TheiaBackground)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("İptal", color = TheiaSecondary)
                    }
                }
            )
        }
    }
}

@Composable
fun SystemInsightsContent(
    patterns: List<LocalPattern>,
    logs: List<TheiaLog>,
    onClearLogs: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ADHD Optimization Patterns list (Page 11 of PDF)
        item {
            Text(
                text = "NÖRAL PATTERN MOTORS (OPTİMİZASYON)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TheiaPrimary,
                modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
            )
        }

        items(patterns) { p ->
            Card(
                colors = CardDefaults.cardColors(containerColor = TheiaSurfaceVariant),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = p.patternId.replace("_", " ").uppercase(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TheiaOnSurface
                        )
                        Text(
                            text = "Tetikleyici: ${p.triggerConditions}",
                            fontSize = 11.sp,
                            color = TheiaPrimary
                        )
                        Text(
                            text = p.adaptation,
                            fontSize = 11.sp,
                            color = TheiaSecondary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Conf: ${(p.confidence * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = TheiaPrimary
                        )

                        Text(
                            text = if (p.isActive) "Active" else "Inactive",
                            fontSize = 9.sp,
                            color = if (p.isActive) Color(0xFF00FF7F) else TheiaSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Real Success Activity metrics tracking list
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BİLİŞSEL BAĞLANTI METRİK LOGLARI",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TheiaPrimary
                )

                TextButton(onClick = onClearLogs) {
                    Text("LOGLARI TEMİZLE", fontSize = 10.sp, color = Color(0xFFFFB4AB))
                }
            }
        }

        if (logs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = TheiaSurfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Henüz sistem metrik kaydı yok. Gözlüğü tetikleyin veya prompt gönderin.",
                        fontSize = 12.sp,
                        color = TheiaSecondary,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(logs) { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = TheiaSurfaceVariant),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Prompt: \"${log.prompt}\"",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TheiaOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )

                            Text(
                                text = "${log.latencyMs}ms",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (log.latencyMs < 1000) Color(0xFF00FF7F) else Color(0xFFFFB4AB),
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Intent: ${log.matchedIntent} | ${log.engineUsed}",
                                fontSize = 10.sp,
                                color = TheiaSecondary
                            )

                            Text(
                                text = if (log.isCacheHit) "L2 Cache Hit" else "L3 Server Miss",
                                fontSize = 9.sp,
                                color = if (log.isCacheHit) TheiaPrimary else TheiaSecondary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun TheiaBottomNavigation(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = TheiaSurfaceVariant,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = activeTab == 0,
            onClick = { onTabSelected(0) },
            label = { Text("HUD Hub", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Sensors, contentDescription = "Active Hub") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TheiaBackground,
                selectedTextColor = TheiaPrimary,
                indicatorColor = TheiaPrimary,
                unselectedIconColor = TheiaSecondary,
                unselectedTextColor = TheiaSecondary
            )
        )

        NavigationBarItem(
            selected = activeTab == 1,
            onClick = { onTabSelected(1) },
            label = { Text("TheiaVault", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Book, contentDescription = "Hafıza") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TheiaBackground,
                selectedTextColor = TheiaPrimary,
                indicatorColor = TheiaPrimary,
                unselectedIconColor = TheiaSecondary,
                unselectedTextColor = TheiaSecondary
            )
        )

        NavigationBarItem(
            selected = activeTab == 2,
            onClick = { onTabSelected(2) },
            label = { Text("Insights", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
            icon = { Icon(Icons.Default.Equalizer, contentDescription = "Görüntüleme") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = TheiaBackground,
                selectedTextColor = TheiaPrimary,
                indicatorColor = TheiaPrimary,
                unselectedIconColor = TheiaSecondary,
                unselectedTextColor = TheiaSecondary
            )
        )
    }
}

fun LogColorForEngine(engine: String): Color {
    return if (engine.contains("L2")) TheiaPrimary else Color(0xFFD3E4FE)
}
