package com.prusoft.easybiglauncher.ui.screens

import android.widget.ImageView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.utils.AppManager
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllAppsScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<com.prusoft.easybiglauncher.utils.AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        val loadedApps = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            AppManager.getInstalledApps(context)
        }
        apps = loadedApps
        isLoading = false
    }
    
    var searchQuery by remember { mutableStateOf("") }
    val filteredApps = remember(searchQuery, apps) {
        if (searchQuery.isEmpty()) apps
        else apps.filter { it.name.toString().contains(searchQuery, ignoreCase = true) }
    }
    
    val pendingItem by viewModel.pendingAssignmentItem.collectAsState()

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.btn_all_apps)) },
                    navigationIcon = {
                        IconButton(onClick = { 
                            viewModel.setPendingAssignmentItem(null)
                            navController.popBackStack() 
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.go_back))
                        }
                    }
                )
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_hint), fontSize = 20.sp, color = Color.LightGray) },
                    singleLine = true,
                    textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White),
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray,
                        cursorColor = Color.White,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp), color = Color.White)
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(filteredApps) { app ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { 
                                if (pendingItem != null) {
                                    viewModel.assignAppToItem(pendingItem!!, app.packageName, app.name.toString())
                                    viewModel.setPendingAssignmentItem(null)
                                    navController.popBackStack()
                                } else {
                                    AppManager.launchApp(context, app.packageName) 
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx)
                            },
                            update = { view ->
                                view.setImageDrawable(app.icon)
                            },
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.width(24.dp))
                        Text(text = app.name.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Divider(thickness = 2.dp)
                }
            }
        }
    }
}
