package com.prusoft.easybiglauncher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close

@Composable
fun PinPadDialog(
    onPinDismiss: () -> Unit,
    onPinEntered: (String) -> Unit,
    isSettingNewPin: Boolean = false
) {
    var firstEntry by remember { mutableStateOf("") }
    var secondEntry by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onPinDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth().wrapContentHeight(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isSettingNewPin) {
                        if (isVerifying) "Şifreyi Tekrar Girin" else "Yeni Şifre Belirleyin"
                    } else "Şifre Girin",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Visualization of entered digits
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (i in 0 until 4) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(
                                    if (pin.length > i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    CircleShape
                                )
                        )
                    }
                }
                
                if (error.isNotEmpty()) {
                    Text(text = error, color = Color.Red, fontSize = 16.sp, modifier = Modifier.padding(top = 8.dp))
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Pin Pad 1-9
                val numbers = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9")
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (row in numbers) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            for (digit in row) {
                                PinButton(digit = digit, modifier = Modifier.weight(1f)) {
                                    if (pin.length < 4) {
                                        pin += digit
                                        if (pin.length == 4) {
                                            if (isSettingNewPin) {
                                                if (isVerifying) {
                                                    if (pin == firstEntry) {
                                                        onPinEntered(pin)
                                                    } else {
                                                        error = "Şifreler eşleşmiyor!"
                                                        pin = ""
                                                        isVerifying = false
                                                        firstEntry = ""
                                                    }
                                                } else {
                                                    firstEntry = pin
                                                    pin = ""
                                                    isVerifying = true
                                                    error = ""
                                                }
                                            } else {
                                                onPinEntered(pin)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Bottom row: İptal, 0, Sil
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PinButton(icon = Icons.Default.Close, modifier = Modifier.weight(1f), isSpecial = true) {
                            onPinDismiss()
                        }
                        PinButton(digit = "0", modifier = Modifier.weight(1f)) {
                            if (pin.length < 4) {
                                pin += "0"
                                if (pin.length == 4) {
                                    if (isSettingNewPin) {
                                        if (isVerifying) {
                                            if (pin == firstEntry) {
                                                onPinEntered(pin)
                                            } else {
                                                error = "Şifreler eşleşmiyor!"
                                                pin = ""
                                                isVerifying = false
                                                firstEntry = ""
                                            }
                                        } else {
                                            firstEntry = pin
                                            pin = ""
                                            isVerifying = true
                                            error = ""
                                        }
                                    } else {
                                        onPinEntered(pin)
                                    }
                                }
                            }
                        }
                        PinButton(icon = Icons.Default.Backspace, modifier = Modifier.weight(1f), isSpecial = true) {
                            if (pin.isNotEmpty()) pin = pin.dropLast(1)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PinButton(
    modifier: Modifier = Modifier,
    digit: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    isSpecial: Boolean = false,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(90.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = if (isSpecial) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant) else ButtonDefaults.buttonColors()
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(36.dp)
            )
        } else if (digit != null) {
            Text(
                text = digit,
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
