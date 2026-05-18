package com.prusoft.easybiglauncher

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prusoft.easybiglauncher.ui.theme.AccessibilityLauncherTheme
import com.prusoft.easybiglauncher.utils.TTSManager
import androidx.compose.ui.res.stringResource
import com.prusoft.easybiglauncher.R
import kotlinx.coroutines.delay

class AlarmActivity : ComponentActivity() {

    private lateinit var ttsManager: TTSManager
    private var reminderTitle: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setupFlags()
        
        reminderTitle = intent.getStringExtra("reminder_title") ?: "HATIRLATICI"
        ttsManager = TTSManager.getInstance(this)

        setContent {
            AccessibilityLauncherTheme {
                AlarmScreen(
                    title = reminderTitle,
                    onDismiss = {
                        finish()
                    }
                )
            }
        }

        // Start speaking after UI is ready
        startSpeaking()
    }

    private fun setupFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    private fun startSpeaking() {
        val textToSpeak = "$reminderTitle zamanı geldi. Lütfen kontrol edin."
        repeatSpeaking(textToSpeak)
    }

    private fun repeatSpeaking(text: String) {
        // Simple loop to repeat TTS
        val scope = mutableStateOf(true) // Just a placeholder for scoping if needed
        android.os.Handler(mainLooper).post(object : Runnable {
            override fun run() {
                if (!isFinishing) {
                    ttsManager.speak(text)
                    android.os.Handler(mainLooper).postDelayed(this, 5000)
                }
            }
        })
    }

    override fun onDestroy() {
        ttsManager.shutdown()
        super.onDestroy()
    }
}

@Composable
fun AlarmScreen(title: String, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFB71C1C)) // Dark Red
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "!!! DİKKAT !!!",
            fontSize = 40.sp,
            fontWeight = FontWeight.Black,
            color = Color.Yellow,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = title.uppercase(),
            fontSize = 54.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            lineHeight = 60.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "SAATİ GELDİ",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
        ) {
            Text(
                text = stringResource(R.string.alarm_dismiss),
                fontSize = 44.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFFB71C1C)
            )
        }
    }
}
