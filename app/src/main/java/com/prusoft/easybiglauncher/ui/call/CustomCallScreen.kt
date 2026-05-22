package com.prusoft.easybiglauncher.ui.call

import android.content.Context
import android.os.Build
import android.telecom.TelecomManager
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prusoft.easybiglauncher.R
import java.util.Locale

/**
 * Easy BIG Launcher için Yaşlı Dostu Özel Arama ve Cevaplama Ekranı.
 * Gelen aramalar ve aktif aramalar için devasa butonlar ve yüksek kontrast sunar.
 */
@RequiresApi(Build.VERSION_CODES.M)
@Composable
fun CustomCallScreen(
    callerName: String,
    callerNumber: String,
    isCallActiveInitially: Boolean = false,
    onDeclineCall: () -> Unit,
    onAcceptCall: () -> Unit,
    onEndCall: () -> Unit
) {
    var isCallActive by remember { mutableStateOf(isCallActiveInitially) }
    var isSpeakerOn by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var showDialpad by remember { mutableStateOf(false) }
    var dialpadText by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)) // Koyu yüksek kontrastlı arka plan
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Üst Başlık ve Çağrı Durumu
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = "EASY BIG LAUNCHER ARAMA",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B),
                    letterSpacing = 1.5.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = callerName.ifEmpty { "Bilinmeyen Numara" },
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                
                Text(
                    text = callerNumber,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Orta Alan: Duruma Göre Değişen Profil veya Tuş Takımı
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (showDialpad) {
                    // Büyük Tuş Takımı Görünümü
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = dialpadText,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        DialpadGrid(
                            onKeyPressed = { key -> dialpadText += key },
                            onBackspace = { if (dialpadText.isNotEmpty()) dialpadText = dialpadText.dropLast(1) },
                            onClose = { showDialpad = false }
                        )
                    }
                } else {
                    // Büyük Profil Çemberi (Yüksek Kontrastlı)
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .border(4.dp, Color(0xFF475569), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_user_placeholder),
                            contentDescription = "Profil",
                            modifier = Modifier.size(80.dp),
                            tint = Color.White
                        )
                    }
                }
            }

            // Alt Alan: Kontroller ve Tetikleyiciler
            if (!isCallActive) {
                // DURUM 1: GELEN ARAMA BUTONLARI
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Reddet Butonu
                    Button(
                        onClick = onDeclineCall,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(24.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_call_decline),
                                contentDescription = "Reddet",
                                modifier = Modifier.size(32.dp)
                            )
                            Text(text = "REDDET", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }

                    // Cevapla Butonu
                    Button(
                        onClick = {
                            isCallActive = true
                            onAcceptCall()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(84.dp)
                            .border(2.dp, Color(0xFF10B981), RoundedCornerShape(24.dp))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_call_accept),
                                contentDescription = "Cevapla",
                                modifier = Modifier.size(32.dp)
                            )
                            Text(text = "CEVAPLA", fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            } else {
                // DURUM 2: AKTİF ÇAĞRI KONTROLLERİ
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hoparlör Butonu
                        OutlinedButton(
                            onClick = { isSpeakerOn = !isSpeakerOn },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSpeakerOn) Color(0xFF1E293B) else Color.Transparent
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                    contentDescription = "Hoparlör",
                                    tint = if (isSpeakerOn) Color(0xFF3B82F6) else Color.White
                                )
                                Text(text = "HOPARLÖR", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Sessiz Butonu
                        OutlinedButton(
                            onClick = { isMuted = !isMuted },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(72.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isMuted) Color(0xFF1E293B) else Color.Transparent
                            )
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Sessiz",
                                    tint = if (isMuted) Color(0xFFEF4444) else Color.White
                                )
                                Text(text = "SESSİZ", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Tuş Takımı Aç/Kapat Butonu
                    Button(
                        onClick = { showDialpad = !showDialpad },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    ) {
                        Text(
                            text = if (showDialpad) "KORUMALI EKRANA DÖN" else "BÜYÜK TUŞ TAKIMI",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Kırmızı Kapatma Butonu
                    Button(
                        onClick = onEndCall,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .border(2.dp, Color(0xFFEF4444), RoundedCornerShape(24.dp))
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_call_decline),
                                contentDescription = "Aramayı Sonlandır",
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = "ARAMAYI SONLANDIR", fontSize = 16.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialpadGrid(
    onKeyPressed: (String) -> Unit,
    onBackspace: () -> Unit,
    onClose: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        val keys = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )
        
        for (row in keys) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (key in row) {
                    Button(
                        onClick = { onKeyPressed(key) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(68.dp)
                    ) {
                        Text(text = key, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onBackspace,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF78350F)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("SİL", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onClose,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(54.dp)
            ) {
                Text("KAPAT", fontWeight = FontWeight.Bold)
            }
        }
    }
}
