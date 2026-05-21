package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.clickable
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
                                text = "${stringResource(R.string.page_label)} ${page.pageOrder + 1}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = stringResource(R.string.grid_label, page.rowCount, page.columnCount),
                                fontSize = 18.sp
                            )
                        }
                        
                        if (page.pageOrder == 0) {
                            Button(
                                onClick = { },
                                enabled = false,
                                modifier = Modifier.height(60.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            ) {
                                Text(stringResource(R.string.default_page), fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.deletePage(page) },
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

            item {
                var showAddPageMenu by remember { mutableStateOf(false) }

                Button(
                    onClick = { showAddPageMenu = true },
                    modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.add_page), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                if (showAddPageMenu) {
                    var selectedLayout by remember { mutableStateOf(Pair(3, 2)) }

                    AlertDialog(
                        onDismissRequest = { showAddPageMenu = false },
                        title = { Text(stringResource(R.string.page_layout_title)) },
                        text = {
                            Column {
                                val layouts = listOf(Pair(2, 3) to "2 x 3", Pair(3, 3) to "3 x 3", Pair(3, 4) to "3 x 4", Pair(3, 2) to "3 x 2")
                                layouts.forEach { (layout, name) ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { selectedLayout = layout }.padding(12.dp)
                                    ) {
                                        RadioButton(
                                            selected = selectedLayout == layout,
                                            onClick = { selectedLayout = layout }
                                        )
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(name, fontSize = 20.sp)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.addPage(selectedLayout.first, selectedLayout.second)
                                    showAddPageMenu = false
                                },
                                modifier = Modifier.height(60.dp)
                            ) {
                                Text(stringResource(R.string.create_page_btn), fontSize = 20.sp)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddPageMenu = false }) {
                                Text(stringResource(R.string.cancel), fontSize = 20.sp)
                            }
                        }
                    )
                }
            }
        }
    }
}
