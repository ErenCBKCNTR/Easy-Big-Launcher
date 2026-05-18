package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.data.ItemType
import com.prusoft.easybiglauncher.data.LauncherItem
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import com.prusoft.easybiglauncher.ui.components.PinPadDialog
import com.prusoft.easybiglauncher.ui.components.StatusBarWidget
import com.prusoft.easybiglauncher.ui.components.ButtonEditorSheet
import com.prusoft.easybiglauncher.utils.NotificationTracker
import androidx.compose.foundation.combinedClickable

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val pages by viewModel.pages.collectAsState(initial = emptyList())
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val missedCalls by NotificationTracker.missedCalls.collectAsState()
    val unreadSms by NotificationTracker.unreadSmsCount.collectAsState()
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val savedPin by viewModel.securityRepository.savedPin.collectAsState(initial = null)
    
    var showPinDialog by remember { mutableStateOf(false) }
    var showEditSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<LauncherItem?>(null) }

    if (showPinDialog && itemToEdit != null) {
        PinPadDialog(
            onPinDismiss = { showPinDialog = false },
            onPinEntered = { pin ->
                if (pin == savedPin) {
                    showPinDialog = false
                    showEditSheet = true
                }
            }
        )
    }

    if (showEditSheet && itemToEdit != null) {
        ButtonEditorSheet(
            item = itemToEdit!!,
            onSave = { label, color, image ->
                viewModel.repository.updateItem(itemToEdit!!.copy(customLabel = label, customColor = color, customImageUri = image))
                showEditSheet = false
            },
            onDismiss = { showEditSheet = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarWidget()
        
        if (pages.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                Button(onClick = { navController.navigate("settings") }) {
                    Text("Ayarlara git ve sayfa ekle")
                }
            }
        } else {
            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize().weight(1f)) { pageIndex ->
                val page = pages[pageIndex]
                val items by viewModel.repository.getItemsForPage(page.id).collectAsState(initial = emptyList())

                LazyVerticalGrid(
                    columns = GridCells.Fixed(page.columnCount),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(items.size) { index ->
                        val item = items[index]
                        GridItem(item, 
                            badgeCount = when {
                                item.packageName == "com.android.dialer" || item.packageName == "com.google.android.dialer" -> missedCalls
                                item.packageName == "com.android.messaging" || item.packageName == "com.google.android.apps.messaging" -> unreadSms
                                else -> 0
                            },
                            onClick = {
                                if (item.itemType == ItemType.EMPTY) {
                                    if (!isProtectionEnabled) {
                                        // TODO: Add App/Contact
                                    }
                                } else {
                                    // Launch App/Contact
                                }
                            },
                            onLongClick = {
                                if (item.itemType != ItemType.EMPTY) {
                                    if (isProtectionEnabled) {
                                        itemToEdit = item
                                        showPinDialog = true
                                    } else {
                                        // TODO: Remove/Edit
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(item: LauncherItem, badgeCount: Int = 0, onClick: () -> Unit, onLongClick: () -> Unit) {
    Box(modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)) {
        BigButton(
            text = if (item.itemType == ItemType.EMPTY) "Ekle" else (item.customLabel ?: item.label ?: "Uygulama"),
            icon = if (item.itemType == ItemType.EMPTY) Icons.Default.Add else Icons.Default.Apps,
            backgroundColor = if (item.itemType == ItemType.EMPTY) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
            contentColor = if (item.itemType == ItemType.EMPTY) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
            badgeCount = badgeCount,
            customColor = item.customColor,
            customImageUri = item.customImageUri,
            onClick = onClick
        )
    }
}
