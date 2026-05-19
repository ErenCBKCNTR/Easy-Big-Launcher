package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.DisposableEffect
import android.view.WindowManager
import android.app.Activity
import androidx.compose.material3.*
import androidx.compose.runtime.*
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel

fun findActivity(context: Context): Activity? {
    var currentContext = context
    while (currentContext is ContextWrapper) {
        if (currentContext is Activity) {
            return currentContext
        }
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicalIdScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        val window = findActivity(context)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    val medName by viewModel.securityRepository.medName.collectAsState(initial = "")
    val medSurname by viewModel.securityRepository.medSurname.collectAsState(initial = "")
    val medAge by viewModel.securityRepository.medAge.collectAsState(initial = "")
    val medAddress by viewModel.securityRepository.medAddress.collectAsState(initial = "")
    val medBlood by viewModel.securityRepository.medBlood.collectAsState(initial = "")
    val medChronic by viewModel.securityRepository.medChronic.collectAsState(initial = "")
    val medContactName by viewModel.securityRepository.medContactName.collectAsState(initial = "")
    val medContactNumber by viewModel.securityRepository.medContactNumber.collectAsState(initial = "")
    val medContactRelation by viewModel.securityRepository.medContactRelation.collectAsState(initial = "")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.medical_id_short)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column {
                    // Red Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE53935))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.medical_id_short).uppercase(),
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Main Content
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Top Section: Name
                        val fullName = "$medName $medSurname".trim()
                        val nameToShow = fullName.ifEmpty { stringResource(R.string.not_specified) }
                        
                        Text(text = stringResource(R.string.medical_firstname) + " " + stringResource(R.string.medical_lastname), fontSize = 16.sp, color = Color.Gray)
                        Text(text = nameToShow, fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Top Section: Age and Blood Type
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stringResource(R.string.medical_age), fontSize = 16.sp, color = Color.Gray)
                                Text(
                                    text = medAge.ifBlank { stringResource(R.string.not_specified) },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stringResource(R.string.medical_blood), fontSize = 16.sp, color = Color.Gray)
                                Text(
                                    text = medBlood.ifBlank { stringResource(R.string.not_specified) },
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFD32F2F)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Middle Section: Chronic Illnesses & Address
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(text = stringResource(R.string.medical_chronic), fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = medChronic.ifBlank { stringResource(R.string.not_specified) },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                Text(text = stringResource(R.string.medical_address), fontSize = 16.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = medAddress.ifBlank { stringResource(R.string.not_specified) },
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Bottom Section: Emergency Contact
                        Text(
                            text = stringResource(R.string.medical_contact_title),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)
                        
                        Text(text = stringResource(R.string.medical_contact_name), fontSize = 16.sp, color = Color.Gray)
                        Text(
                            text = medContactName.ifBlank { stringResource(R.string.not_specified) },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(text = stringResource(R.string.medical_contact_number), fontSize = 16.sp, color = Color.Gray)
                        Text(
                            text = medContactNumber.ifBlank { stringResource(R.string.not_specified) },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(text = stringResource(R.string.medical_contact_relation), fontSize = 16.sp, color = Color.Gray)
                        Text(
                            text = medContactRelation.ifBlank { stringResource(R.string.not_specified) },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
