package com.accessibility.launcher.ui.screens

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
import com.accessibility.launcher.data.ItemType
import com.accessibility.launcher.data.LauncherItem
import com.accessibility.launcher.viewmodel.LauncherViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val pages by viewModel.pages.collectAsState(initial = emptyList())
    val pagerState = rememberPagerState(pageCount = { pages.size })

    if (pages.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Button(onClick = { navController.navigate("settings") }) {
                Text("Ayarlara git ve sayfa ekle")
            }
        }
    } else {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
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
                    GridItem(item) {
                        if (item.itemType == ItemType.EMPTY) {
                            // Dialog for adding app
                        } else {
                            // Launch App
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GridItem(item: LauncherItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = if (item.itemType == ItemType.EMPTY) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (item.itemType == ItemType.EMPTY) {
                Icon(Icons.Default.Add, contentDescription = "Ekle")
            } else {
                Text(text = item.label ?: "Uygulama")
            }
        }
    }
}
