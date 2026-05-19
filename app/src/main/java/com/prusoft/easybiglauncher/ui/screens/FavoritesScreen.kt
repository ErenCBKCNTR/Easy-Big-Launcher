package com.prusoft.easybiglauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.prusoft.easybiglauncher.R
import org.json.JSONArray
import org.json.JSONObject

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.prusoft.easybiglauncher.utils.FavoritesUtils
import com.prusoft.easybiglauncher.utils.FavoriteContact
import com.prusoft.easybiglauncher.data.SecurityRepository
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FavoritesScreen() {
    val context = LocalContext.current
    val securityRepository = remember { SecurityRepository(context as android.app.Application) }
    val isHomeFavLockEnabled by securityRepository.isHomeFavLockEnabled.collectAsState(initial = false)
    
    var favorites by remember { mutableStateOf<List<FavoriteContact>>(emptyList()) }
    var favoriteToDelete by remember { mutableStateOf<FavoriteContact?>(null) }
    
    LaunchedEffect(Unit) {
        try {
            favorites = FavoritesUtils.getFavorites(context).distinctBy { it.number }
        } catch (e: Exception) {
            favorites = emptyList()
        }
    }

    if (favorites.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_favorites_yet), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            try {
                items(items = favorites, key = { it.hashCode() }) { fav ->
                    Card(
                        modifier = Modifier.fillMaxWidth().combinedClickable(
                            onClick = {
                                try {
                                    val safeNumber = fav.number ?: ""
                                    val intent = Intent(Intent.ACTION_CALL).apply {
                                        data = Uri.parse("tel:$safeNumber")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$safeNumber")
                                        }
                                        context.startActivity(dialIntent)
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                        onLongClick = {
                            if (!isHomeFavLockEnabled) {
                                favoriteToDelete = fav
                            }
                        }
                    ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = fav.name ?: "Unknown", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = fav.number ?: "Unknown", fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                    }
                }
                }
            } catch (e: Exception) {
                item {
                    Text("Favoriler yüklenirken hata oluştu.", fontSize = 20.sp, color = Color.Red)
                }
            }
        }
    }
    
    if (favoriteToDelete != null) {
        com.prusoft.easybiglauncher.ui.components.BigConfirmDialog(
            title = stringResource(R.string.delete_favorite_title),
            message = stringResource(R.string.delete_favorite_desc, favoriteToDelete!!.name ?: "Unknown"),
            onConfirm = {
                favoriteToDelete?.let {
                    FavoritesUtils.removeFavorite(context, it.number ?: "")
                    favorites = FavoritesUtils.getFavorites(context).distinctBy { f -> f.number }
                }
                favoriteToDelete = null
            },
            onCancel = { favoriteToDelete = null }
        )
    }
}
