package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceSpeechHelper(private val context: Context) {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechRecognizedText = MutableStateFlow<String?>(null)
    val speechRecognizedText: StateFlow<String?> = _speechRecognizedText.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    init {
        initTTS()
    }

    private fun initTTS() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isTtsInitialized = true
                    textToSpeech?.let { tts ->
                        tts.language = Locale("hi", "IN")
                        tts.setSpeechRate(0.95f)
                        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                            }

                            override fun onDone(utteranceId: String?) {
                                _isSpeaking.value = false
                            }

                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                            }
                        })
                    }
                } else {
                    Log.e("VoiceSpeechHelper", "TTS Initialization failed: status $status")
                }
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "TTS Init error", e)
        }
    }

    fun speak(text: String, isHindi: Boolean = false) {
        if (!isTtsInitialized || textToSpeech == null) {
            initTTS()
        }

        try {
            textToSpeech?.let { tts ->
                tts.stop()
                val locale = if (isHindi) Locale("hi", "IN") else Locale.ENGLISH
                val result = tts.setLanguage(locale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.language = Locale.ENGLISH
                }
                _isSpeaking.value = true
                val utteranceId = "KRISHI_TTS_${System.currentTimeMillis()}"
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            }
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "TTS speak error", e)
            _isSpeaking.value = false
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
            _isSpeaking.value = false
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Stop speaking error", e)
        }
    }

    fun startListening(isHindi: Boolean = false, onResult: (String) -> Unit) {
        _speechError.value = null
        _speechRecognizedText.value = null

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _speechError.value = "Speech recognition is not available on this device"
            _isListening.value = false
            return
        }

        try {
            stopListening()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected"
                            else -> "Recognition error #$error"
                        }
                        _speechError.value = errorMsg
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val recognized = matches[0]
                            _speechRecognizedText.value = recognized
                            onResult(recognized)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, if (isHindi) "hi-IN" else "en-IN")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, if (isHindi) "hi-IN" else "en-IN")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                putExtra(RecognizerIntent.EXTRA_PROMPT, if (isHindi) "बोलें, हम सुन रहे हैं..." else "Speak your question...")
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "SpeechRecognizer error", e)
            _isListening.value = false
            _speechError.value = e.message
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
            speechRecognizer = null
            _isListening.value = false
        } catch (e: Exception) {
            Log.e("VoiceSpeechHelper", "Stop listening error", e)
        }
    }

    fun shutdown() {
        stopSpeaking()
        stopListening()
        textToSpeech?.shutdown()
        textToSpeech = null
    }
}
