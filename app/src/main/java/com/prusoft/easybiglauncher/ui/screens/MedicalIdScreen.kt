package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MedicalInfoCard(stringResource(R.string.medical_firstname) + " " + stringResource(R.string.medical_lastname), "$medName $medSurname")
            MedicalInfoCard(stringResource(R.string.medical_age), medAge)
            MedicalInfoCard(stringResource(R.string.medical_blood), medBlood, Color.Red)
            MedicalInfoCard(stringResource(R.string.medical_chronic), medChronic)
            MedicalInfoCard(stringResource(R.string.medical_address), medAddress)
            
            Text(stringResource(R.string.medical_contact_title), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Red, modifier = Modifier.padding(top = 16.dp))
            MedicalInfoCard(stringResource(R.string.medical_contact_name), medContactName)
            MedicalInfoCard(stringResource(R.string.medical_contact_number), medContactNumber)
            MedicalInfoCard(stringResource(R.string.medical_contact_relation), medContactRelation)
        }
    }
}

@Composable
fun MedicalInfoCard(title: String, value: String, color: Color = Color.Unspecified) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            val textValue = if (value.isBlank()) stringResource(R.string.not_specified) else value
            Text(text = textValue, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface)
        }
    }
}
