package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.GeminiContent
import com.example.api.GeminiPart
import com.example.api.GeminiRequest
import com.example.api.RetrofitClient
import com.example.data.db.TheiaDatabase
import com.example.data.model.LocalPattern
import com.example.data.model.TheiaLog
import com.example.data.model.TheiaVaultNote
import com.example.data.repository.TheiaRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class ConnectionMode {
    ONLINE,
    ONLINE_METERED,
    OFFLINE
}

class TheiaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TheiaRepository

    // Reactive lists from Database
    val notes: StateFlow<List<TheiaVaultNote>>
    val patterns: StateFlow<List<LocalPattern>>
    val logs: StateFlow<List<TheiaLog>>

    // UI Interactive States
    private val _connectionMode = MutableStateFlow(ConnectionMode.ONLINE)
    val connectionMode = _connectionMode.asStateFlow()

    private val _promptInput = MutableStateFlow("")
    val promptInput = _promptInput.asStateFlow()

    private val _transcription = MutableStateFlow("")
    val transcription = _transcription.asStateFlow()

    private val _responseOutput = MutableStateFlow("Merhaba İsmail. Sana nasıl yardımcı olabilirim?")
    val responseOutput = _responseOutput.asStateFlow()

    private val _thoughtDump = MutableStateFlow<List<String>>(
        listOf(
            "System initialized.",
            "NPM Engine Active: waiting for user input.",
            "L2 Local Database Connected.",
            "Ready for ADHD-friendly executive coaching."
        )
    )
    val thoughtDump = _thoughtDump.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording = _isRecording.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _latencyMs = MutableStateFlow(0L)
    val latencyMs = _latencyMs.asStateFlow()

    private val _engineUsed = MutableStateFlow("Idle")
    val engineUsed = _engineUsed.asStateFlow()

    init {
        val database = TheiaDatabase.getDatabase(application)
        val dao = database.theiaDao()
        repository = TheiaRepository(dao)

        notes = repository.allNotes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        patterns = repository.allPatterns.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        logs = repository.allLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate data if empty
        viewModelScope.launch(Dispatchers.IO) {
            prepopulateDatabaseIfEmpty()
        }
    }

    fun setPromptInput(text: String) {
        _promptInput.value = text
    }

    fun setConnectionMode(mode: ConnectionMode) {
        _connectionMode.value = mode
        val message = when (mode) {
            ConnectionMode.ONLINE -> "Online (Wi-Fi) - Full features active"
            ConnectionMode.ONLINE_METERED -> "Online (4G/5G) - Saving cellular traffic"
            ConnectionMode.OFFLINE -> "Offline moddasın. Sadece lokal komutları işleyeceğim."
        }
        addThoughtStep("Connection Mode changed to $mode - $message")
    }

    private fun addThoughtStep(step: String) {
        val current = _thoughtDump.value.toMutableList()
        if (current.size > 15) current.removeAt(0)
        current.add(step)
        _thoughtDump.value = current
    }

    private suspend fun prepopulateDatabaseIfEmpty() {
        // Simple manual check as we can't block easily and want full reactive logs
        delay(300) // Small breather to let Flows sync
        if (notes.value.isEmpty()) {
            repository.insertNote(
                TheiaVaultNote(
                    title = "ADHD Odaklanma Stratejileri",
                    content = "1. Pomodoro metodunu 15 dakikalık kısa aralıklarla uygula.\n2. Telefon bildirimlerini tamamen kapat veya Theia moduna al.\n3. Her seferde tek bir mikro-adıma odaklan.",
                    category = "frequent_notes"
                )
            )
            repository.insertNote(
                TheiaVaultNote(
                    title = "Theia MVP Architecture",
                    content = "Theia, İsmail Karabulut'un kararlarını kolaylaştırmak için L2 (Local) ve L3 (Cloud) katmanlarını akıllıca yönetir. Gecikme hedefi her zaman <2s.",
                    category = "frequent_notes"
                )
            )
            repository.insertNote(
                TheiaVaultNote(
                    title = "Alışveriş Listesi",
                    content = "Nutritional supplements, Omega-3, yeşil çay, protein bar, bitter çikolata.",
                    category = "daily_notes"
                )
            )
        }

        if (patterns.value.isEmpty()) {
            repository.insertPattern(
                LocalPattern(
                    patternId = "morning_focused_brief",
                    patternType = "time_based",
                    triggerConditions = "09:00 - 12:00",
                    adaptation = "Short response, max 2 sentences",
                    confidence = 0.89f,
                    isActive = true
                )
            )
            repository.insertPattern(
                LocalPattern(
                    patternId = "afternoon_energy_boost",
                    patternType = "time_based",
                    triggerConditions = "13:00 - 17:00",
                    adaptation = "High motivation tone, bulleted micro-steps",
                    confidence = 0.85f,
                    isActive = true
                )
            )
            repository.insertPattern(
                LocalPattern(
                    patternId = "night_winding_down",
                    patternType = "time_based",
                    triggerConditions = "21:00 - 24:00",
                    adaptation = "Calm tone, warm wind-down support",
                    confidence = 0.92f,
                    isActive = true
                )
            )
        }
    }

    // Voice Recording Simulation
    fun toggleRecording() {
        if (_isRecording.value) {
            // Stop recording, transcribe & execute
            _isRecording.value = false
            simulateRecordingFinish()
        } else {
            // Start recording
            _isRecording.value = true
            _transcription.value = "Sesini dinliyorum, İsmail..."
            _promptInput.value = ""
            addThoughtStep("[1] L1 Ray-Ban Meta Gözlük: Ses kaydı başlatıldı.")
        }
    }

    private fun simulateRecordingFinish() {
        viewModelScope.launch {
            _isLoading.value = true
            addThoughtStep("[2] L2 Telefon: Whisper STT ses sentezine gönderildi.")
            delay(500)

            // Dynamic random prompt matching İsmail's daily ADHD struggles
            val samplePrompts = listOf(
                "Bugün ne yapmalıyım? Bana kısa bir liste çıkar.",
                "Zamanlayıcı başlat, 10 dakika sonra hatırlat.",
                "Not kaydet: Akşama kod inceleme toplantısını unutma.",
                "Motive et, çalışma isteğim azaldı.",
                "Takvimimde bugün ne var?",
                "Theia MVP hakkında ne yazmıştım?"
            )
            val selectedPrompt = samplePrompts[Random.nextInt(samplePrompts.size)]
            _transcription.value = selectedPrompt
            _promptInput.value = selectedPrompt

            processUserPrompt(selectedPrompt)
        }
    }

    fun submitTextPrompt() {
        val text = _promptInput.value.trim()
        if (text.isEmpty()) return
        _transcription.value = text
        _promptInput.value = ""
        viewModelScope.launch {
            processUserPrompt(text)
        }
    }

    private suspend fun processUserPrompt(prompt: String) {
        _isLoading.value = true
        val startTime = System.currentTimeMillis()
        addThoughtStep("Input Received: \"$prompt\"")

        // 1. Intent Classification
        val intent = classifyQueryIntent(prompt)
        addThoughtStep("[3] Intent Classifier parsed: '$intent'")

        // 2. Decision Logic: Local vs Cloud
        val useServer = shouldServerBeUsed(intent)
        val engine = if (useServer) "L3 Cloud (Gemini RAG)" else "L2 Local (Whisper/Piper)"
        _engineUsed.value = engine
        addThoughtStep("[4] Execution Strategy: Routed to $engine")

        var finalResponse = ""
        var isCache = false

        if (!useServer) {
            // Local Processing
            isCache = true
            delay(400) // Simulating fast local response (<500ms)
            finalResponse = handleLocalIntent(intent, prompt)
        } else {
            // Server Room (Gemini API with RAG)
            addThoughtStep("[5] Accessing TheiaVault references...")
            val contextNotes = searchVaultContext(prompt)
            if (contextNotes.isNotEmpty()) {
                addThoughtStep("RAG Found: Active reference to \"${contextNotes[0].title}\"")
            } else {
                addThoughtStep("RAG Found: No specific semantic matches found. Proceeding with base memory.")
            }

            finalResponse = callCloudGeminiService(prompt, contextNotes)
        }

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        _latencyMs.value = duration
        _responseOutput.value = finalResponse
        _isLoading.value = false

        addThoughtStep("[6] Response generated in ${duration}ms.")

        // Save activity log to state
        repository.addLog(
            TheiaLog(
                prompt = prompt,
                response = finalResponse,
                latencyMs = duration,
                matchedIntent = intent,
                isCacheHit = isCache,
                engineUsed = engine
            )
        )
    }

    private fun classifyQueryIntent(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("timer") || lower.contains("zamanlayıcı") || lower.contains("dakika") || lower.contains("hatırlat") -> "timer"
            lower.contains("not kaydet") || lower.contains("not al") || lower.contains("vault'a ekle") || lower.contains("yaz:") -> "note_quick"
            lower.contains("motive") || lower.contains("affirmation") || lower.contains("motivasyon") || lower.contains("modum düştü") -> "affirmation"
            lower.contains("takvim") || lower.contains("toplantı") || lower.contains("ajanda") || lower.contains("calendar") -> "calendar_read"
            lower.contains("ne yazmıştım") || lower.contains("hakkında bilgi") || lower.contains("arama") || lower.contains("vault") -> "rag_query"
            else -> "task_planning"
        }
    }

    private fun shouldServerBeUsed(intent: String): Boolean {
        if (_connectionMode.value == ConnectionMode.OFFLINE) return false
        if (intent == "timer" || intent == "note_quick" || intent == "affirmation") return false
        return true
    }

    // L2 Local handlers
    private suspend fun handleLocalIntent(intent: String, prompt: String): String {
        return when (intent) {
            "timer" -> {
                val number = prompt.filter { it.isDigit() }.toIntOrNull() ?: 5
                addThoughtStep("L2 Engine: Triggering device alarm clock.")
                "$number dakikalık zamanlayıcı başarıyla başlatıldı. Akışını bozmadan işine odaklan İsmail!"
            }
            "note_quick" -> {
                val noteText = prompt.replace("not kaydet", "", ignoreCase = true)
                    .replace("not al", "", ignoreCase = true).trim()
                val cleanNote = noteText.ifEmpty { "İsmail'in hızlı notu" }
                repository.insertNote(
                    TheiaVaultNote(
                        title = "Hızlı Not (${System.currentTimeMillis() / 1000})",
                        content = cleanNote,
                        category = "quick_note"
                    )
                )
                addThoughtStep("L2 Engine: Note successfully persisted in local Room db. Synced pending.")
                "\"$cleanNote\" notu TheiaVault altına başarıyla kaydedildi. Wi-Fi bağlantısında buluta yedeklenecek."
            }
            "affirmation" -> {
                val quotes = listOf(
                    "Mükemmel olmasına gerek yok İsmail, sadece başla. Mikro-adımların senin süper gücün!",
                    "Akışını bozma. Dikkatini dağıtan her şeyi 15 dakika boyunca kapat ve sadece şu anki göreve odaklan.",
                    "Theia senin için burada. Sonuç ne olursa olsun, bir adım atmış olmak harika bir kazanımdır.",
                    "Sıradaki en küçük, en önemsiz adımı seç ve yap. Gerisi çorap söküğü gibi gelecek."
                )
                quotes.random()
            }
            "calendar_read" -> {
                "Sistem takviminde bugün 15:30'da \"Theia MVP Tasarım Gözden Geçirme\" toplantın bulunuyor. Başka bir etkinlik yok."
            }
            else -> {
                "Bağlantı bulunmuyor veya görev lokal limitler dahilinde. Lütfen çevrimiçi olun veya basit bir komut verin."
            }
        }
    }

    // Vault search for simple RAG implementation
    private suspend fun searchVaultContext(prompt: String): List<TheiaVaultNote> {
        val words = prompt.lowercase().split(" ", ",", ".")
        val allLocalNotes = repository.searchNotes(prompt)
        if (allLocalNotes.isNotEmpty()) return allLocalNotes

        // fallback keyword search
        val matches = mutableListOf<TheiaVaultNote>()
        val completeNotes = notes.value
        for (note in completeNotes) {
            val titleMatches = words.any { note.title.lowercase().contains(it) }
            val contentMatches = words.any { note.content.lowercase().contains(it) }
            if ((titleMatches || contentMatches) && matches.size < 2) {
                matches.add(note)
            }
        }
        return matches
    }

    // L3 Cloud direct API
    private suspend fun callCloudGeminiService(prompt: String, context: List<TheiaVaultNote>): String {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            addThoughtStep("Gemini API Key missing or default. Simulating local fallback generative model.")
            return simulateLocalGenerativeModel(prompt, context)
        }

        val systemPrompt = """
            You are 'Theia', the ADHD-Focused Personal Executive Assistant of İsmail Karabulut.
            You are a cognitive prosthesis, NOT a standard chatbot.
            You speak fluent Turkish. 
            Keep your answers extremely:
            1. Brief, scannable, and highly structure-focused (under 3 short sentences).
            2. Split complex suggestions into 2-5 minute micro-steps.
            3. Empathetic and positive to keep İsmail motivated. Avoid long lists or giant walls of text that cause distraction.
            
            Use the following context from İsmail's personal notes to answer questions when helpful:
            ${context.joinToString("\n\n") { "Başlık: ${it.title}\nİçerik: ${it.content}" }}
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = prompt)))
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemPrompt))),
            generationConfig = com.example.api.GeminiGenerationConfig(temperature = 0.7f, maxOutputTokens = 350)
        )

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Cevap üretilemedi. Lütfen bağlantınızı kontrol edip tekrar deneyin."
        } catch (e: Exception) {
            addThoughtStep("API Call failed: ${e.message}. Falling back to offline simulator...")
            simulateLocalGenerativeModel(prompt, context)
        }
    }

    private fun simulateLocalGenerativeModel(prompt: String, context: List<TheiaVaultNote>): String {
        return when {
            prompt.lowercase().contains("bugün ne yapmalıyım") -> {
                "Bugün için 3 basit odak önerim:\n• MVP planını aç ve Room DB kodlarını gözden geçir (5 dk)\n• Gözlük bağlantısını test et (2 dk)\n• Pomodoro açarak ilk 15 dakikalık seansa başla."
            }
            context.isNotEmpty() -> {
                "Bulduğum not kırıntılarına göre:\n${context[0].title}: \"${context[0].content.take(80)}...\"\n\nİsmail, bu bilgi ışığında devam etmeye odaklanalım mı?"
            }
            else -> {
                "İsmail, harika bir adım attın. Şu an bunu düşünerek vakit kaybetmeyelim, bunu senin yerine takip ediyorum. Sıradaki küçük eylemin nedir?"
            }
        }
    }

    // CRUD Notes for manual user interaction
    fun createVaultNote(title: String, content: String, category: String = "general") {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(
                TheiaVaultNote(
                    title = title,
                    content = content,
                    category = category
                )
            )
            addThoughtStep("TheiaVault: Added custom note \"$title\"")
        }
    }

    fun deleteVaultNote(note: TheiaVaultNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
            addThoughtStep("TheiaVault: Deleted note \"${note.title}\"")
        }
    }

    fun clearAllLogs() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearLogs()
            _thoughtDump.value = listOf("System logs reset.", "NPM Motor active.")
        }
    }
}
