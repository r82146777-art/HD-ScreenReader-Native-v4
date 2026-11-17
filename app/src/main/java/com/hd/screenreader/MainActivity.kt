package com.hd.screenreader

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.Context
import android.view.Gravity
import android.widget.*
import android.view.ViewGroup

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("hd_reader", Context.MODE_PRIVATE) }
    private lateinit var serviceStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
        }
        root.addView(TextView(this).apply {
            text = "صفحه‌خوان HD"
            textSize = 27f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 16)
        })
        serviceStatus = TextView(this).apply { textSize = 16f; setPadding(0, 8, 0, 12) }
        root.addView(serviceStatus)

        val enabled = Switch(this).apply {
            text = "خواندن عناصر صفحه"
            isChecked = prefs.getBoolean("read_text", true)
            setOnCheckedChangeListener { _, value -> prefs.edit().putBoolean("read_text", value).apply() }
        }
        root.addView(enabled)

        val clickFeedback = Switch(this).apply {
            text = "بازخورد کوتاه هنگام کلیک"
            isChecked = prefs.getBoolean("click_feedback", true)
            setOnCheckedChangeListener { _, value -> prefs.edit().putBoolean("click_feedback", value).apply() }
        }
        root.addView(clickFeedback)

        val concise = Switch(this).apply {
            text = "خواندن کوتاه و بدون تکرار"
            isChecked = prefs.getBoolean("concise", true)
            setOnCheckedChangeListener { _, value -> prefs.edit().putBoolean("concise", value).apply() }
        }
        root.addView(concise)

        val explore = Switch(this).apply {
            text = "کاوش لمسی (Touch Exploration)"
            isChecked = prefs.getBoolean("explore", true)
            setOnCheckedChangeListener { _, value -> prefs.edit().putBoolean("explore", value).apply() }
        }
        root.addView(explore)

        val computerSounds = Switch(this).apply {
            text = "افکت‌های صوتی رایانه‌ای"
            isChecked = prefs.getBoolean("computer_sounds", true)
            setOnCheckedChangeListener { _, value -> prefs.edit().putBoolean("computer_sounds", value).apply() }
        }
        root.addView(computerSounds)

        // Speed control
        root.addView(TextView(this).apply {
            text = "سرعت گفتار"
            textSize = 16f
            setPadding(0, 16, 0, 4)
        })
        val speedSeek = SeekBar(this).apply {
            max = 20
            progress = prefs.getInt("speech_rate", 10)
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) prefs.edit().putInt("speech_rate", progress).apply()
                }
                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }
        root.addView(speedSeek)

        fun addButton(label: String, action: () -> Unit) {
            root.addView(Button(this).apply {
                text = label
                setOnClickListener { action() }
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                    topMargin = 8
                }
            })
        }

        addButton("باز کردن تنظیمات دسترس‌پذیری") {
            try {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            } catch (e: Exception) {
                Toast.makeText(this, "خطا در باز کردن تنظیمات", Toast.LENGTH_SHORT).show()
            }
        }
        addButton("تنظیمات TTS سیستم") {
            try { startActivity(Intent("com.android.settings.TTS_SETTINGS")) }
            catch (e: Exception) {
                try { startActivity(Intent(Settings.ACTION_SETTINGS)) } catch (_: Exception) {}
            }
        }
        addButton("آزمایش افکت‌های رایانه‌ای") {
            ReaderAccessibilityService.playDemo(this)
        }
        addButton("راهنمای کاوش لمسی") {
            AlertDialog.Builder(this)
                .setTitle("کاوش لمسی")
                .setMessage("کاوش لمسی با حرکت انگشت روی صفحه، عنصر زیر انگشت را می‌خواند. فعال‌سازی این قابلیت ممکن است با TalkBack یا صفحه‌خوان دیگری تداخل داشته باشد. برای آزمایش، فقط یکی از صفحه‌خوان‌ها را فعال نگه دارید. حرکت و انتخاب نهایی همچنان تابع کنترل‌های دسترس‌پذیری اندروید است.")
                .setPositiveButton("متوجه شدم", null).show()
        }
        addButton("راهنمای جلوگیری از توقف") {
            AlertDialog.Builder(this)
                .setTitle("پایداری صفحه‌خوان")
                .setMessage("۱. سرویس HD را در دسترس‌پذیری روشن نگه دارید.\n۲. در تنظیمات باتری گوشی، مصرف باتری برنامه را روی «بدون محدودیت» بگذارید، اگر این گزینه وجود دارد.\n۳. برنامه را از فهرست برنامه‌های اخیر نبندید و گزینه توقف اجباری را نزنید.\n۴. اگر گوشی پس از راه‌اندازی مجدد سرویس را خاموش کرد، وضعیت دسترس‌پذیری را بررسی و دوباره روشن کنید.\n\nهیچ برنامه‌ای نمی‌تواند در برابر توقف اجباری کاربر، خاموشی گوشی یا محدودیت‌های سیستم تضمین صددرصدی بدهد.")
                .setPositiveButton("متوجه شدم", null).show()
        }
        root.addView(TextView(this).apply {
            text = "این برنامه صدای اختصاصی ندارد؛ از موتور TTS انتخاب‌شده در اندروید استفاده می‌کند. بسته‌های آفلاین فارسی و انگلیسی را در تنظیمات TTS نصب کنید."
            textSize = 14f
            setPadding(0, 16, 0, 0)
        })
        setContentView(ScrollView(this).apply { addView(root) })
    }

    override fun onResume() {
        super.onResume()
        serviceStatus.text = if (ReaderAccessibilityService.isEnabled(this))
            "وضعیت: سرویس صفحه‌خوان در تنظیمات اندروید فعال است."
        else "وضعیت: برای خواندن سایر برنامه‌ها، سرویس دسترس‌پذیری را فعال کنید."
    }
}
