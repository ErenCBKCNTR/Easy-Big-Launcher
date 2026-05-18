package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagePagesScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val pages by viewModel.pages.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.manage_pages)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(pages) { page ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Sayfa ${page.pageOrder + 1}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${page.rowCount}x${page.columnCount} Izgara",
                                fontSize = 18.sp
                            )
                        }
                        
                        Button(
                            onClick = { viewModel.deletePage(page) },
                            enabled = pages.size > 1,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                            modifier = Modifier.height(60.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.delete_page), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            if (pages.size <= 1) {
                item {
                    Text(
                        text = stringResource(R.string.cannot_delete_last_page),
                        color = Color.Red,
                        modifier = Modifier.padding(8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
