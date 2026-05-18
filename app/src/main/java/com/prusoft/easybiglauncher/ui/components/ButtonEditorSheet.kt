package com.prusoft.easybiglauncher.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.prusoft.easybiglauncher.data.LauncherItem

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.prusoft.easybiglauncher.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonEditorSheet(
    item: LauncherItem,
    onSave: (String, String?, String?) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var label by remember { mutableStateOf(item.customLabel ?: item.label ?: "") }
    var color by remember { mutableStateOf(item.customColor ?: "#2196F3") }
    var imageUri by remember { mutableStateOf(item.customImageUri) }

    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(stringResource(R.string.edit_button), style = MaterialTheme.typography.titleLarge)
            
            TextField(value = label, onValueChange = { label = it }, label = { Text(stringResource(R.string.name_label)) }, modifier = Modifier.fillMaxWidth())

            Text(stringResource(R.string.select_color))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#009688", "#4CAF50").forEach { c ->
                    Box(modifier = Modifier.size(40.dp).background(Color(android.graphics.Color.parseColor(c)), CircleShape).border(2.dp, if(color == c) Color.Black else Color.Transparent, CircleShape).clickable { color = c })
                }
            }

            Button(onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                Text(stringResource(R.string.change_photo))
            }

            Button(onClick = { onSave(label, color, imageUri) }, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                Text(stringResource(R.string.save_btn), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            
            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
            ) {
                Text(stringResource(R.string.delete_item_btn), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
