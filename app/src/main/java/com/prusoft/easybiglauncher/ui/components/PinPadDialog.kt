package com.prusoft.easybiglauncher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun PinPadDialog(
    onPinDismiss: () -> Unit,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onPinDismiss) {
        Surface(shape = MaterialTheme.shapes.medium, modifier = Modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "PIN: ${"*".repeat(pin.length)}", style = MaterialTheme.typography.headlineMedium)
                Spacer(modifier = Modifier.height(16.dp))
                
                // Pin Pad
                for (row in listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"))) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (digit in row) {
                            Button(onClick = { if (pin.length < 4) pin += digit }, modifier = Modifier.size(64.dp), shape = CircleShape) {
                                Text(digit, style = MaterialTheme.typography.headlineMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                // Bottom row
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Button(onClick = { pin = "" }, modifier = Modifier.size(64.dp), shape = CircleShape) { Text("Sil") }
                    Button(onClick = { if (pin.length < 4) pin += "0" }, modifier = Modifier.size(64.dp), shape = CircleShape) { Text("0") }
                    Button(onClick = { onPinEntered(pin) }, modifier = Modifier.size(64.dp), shape = CircleShape) { Text("OK") }
                }
            }
        }
    }
}
