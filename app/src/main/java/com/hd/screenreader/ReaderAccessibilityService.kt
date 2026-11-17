package com.hd.screenreader

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.speech.tts.TextToSpeech
import android.media.AudioManager
import android.media.ToneGenerator
import android.content.BroadcastReceiver
import java.util.Locale

class ReaderAccessibilityService : AccessibilityService(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var ready = false
    private var lastText = ""
    private var lastAt = 0L
    private var tone: ToneGenerator? = null
    private var screenReceiver: BroadcastReceiver? = null
    private var lastHoverTone = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        tts = TextToSpeech(applicationContext, this)
        tone = ToneGenerator(AudioManager.STREAM_SYSTEM, 42)
        registerScreenReceiver()
    }

    private fun registerScreenReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val p = getSharedPreferences("hd_reader", Context.MODE_PRIVATE)
                if (!p.getBoolean("computer_sounds", true)) return
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        tone?.startTone(ToneGenerator.TONE_PROP_NACK, 100)
                        if (ready) tts?.speak("صفحه خاموش شد", TextToSpeech.QUEUE_FLUSH, null, "screen-off")
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        tone?.startTone(ToneGenerator.TONE_PROP_ACK, 80)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(receiver, filter)
        screenReceiver = receiver
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            ready = true
            tts?.language = Locale("fa")
            applySpeechRate()
        }
    }

    private fun applySpeechRate() {
        val rate = getSharedPreferences("hd_reader", Context.MODE_PRIVATE).getInt("speech_rate", 10)
        // 0 = very slow, 10 = normal, 20 = very fast
        val speed = 0.5f + (rate / 20f) * 1.5f
        tts?.setSpeechRate(speed)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !ready) return
        val p = getSharedPreferences("hd_reader", Context.MODE_PRIVATE)
        if (!p.getBoolean("read_text", true)) return

        val now = SystemClock.elapsedRealtime()
        val text = extractText(event)
        if (text.isBlank()) return

        val concise = p.getBoolean("concise", true)
        if (concise && text == lastText && now - lastAt < 400) return
        lastText = text
        lastAt = now

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_HOVER_ENTER ||
            event.eventType == AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED) {
            if (p.getBoolean("computer_sounds", true) && now - lastHoverTone > 80) {
                tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
                lastHoverTone = now
            }
        }

        if (event.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED && p.getBoolean("click_feedback", true)) {
            tone?.startTone(ToneGenerator.TONE_PROP_ACK, 50)
        }

        applySpeechRate()
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "evt-${event.eventType}")
    }

    private fun extractText(event: AccessibilityEvent): String {
        val sb = StringBuilder()
        event.text?.forEach { if (!it.isNullOrBlank()) sb.append(it).append(" ") }
        if (sb.isEmpty()) {
            event.contentDescription?.let { if (it.isNotBlank()) sb.append(it) }
        }
        if (sb.isEmpty() && event.source != null) {
            try {
                val src = event.source
                src?.text?.let { if (it.isNotBlank()) sb.append(it) }
                if (sb.isEmpty()) src?.contentDescription?.let { if (it.isNotBlank()) sb.append(it) }
            } catch (_: Exception) {}
        }
        return sb.toString().trim()
    }

    override fun onInterrupt() {
        tts?.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        tts?.shutdown()
        tone?.release()
        try { screenReceiver?.let { unregisterReceiver(it) } } catch (_: Exception) {}
    }

    companion object {
        fun isEnabled(context: Context): Boolean {
            val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as android.view.accessibility.AccessibilityManager
            val enabled = am.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_SPOKEN)
            return enabled.any { it.resolveInfo.serviceInfo.packageName == context.packageName }
        }

        fun playDemo(context: Context) {
            val tg = ToneGenerator(AudioManager.STREAM_SYSTEM, 60)
            tg.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                tg.startTone(ToneGenerator.TONE_PROP_ACK, 100)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ tg.release() }, 150)
            }, 120)
        }
    }
}
