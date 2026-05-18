package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun MedicalIdScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val medName by viewModel.securityRepository.medName.collectAsState(initial = "")
    val medSurname by viewModel.securityRepository.medSurname.collectAsState(initial = "")
    val medAge by viewModel.securityRepository.medAge.collectAsState(initial = "")
    val medAddress by viewModel.securityRepository.medAddress.collectAsState(initial = "")
    val medBlood by viewModel.securityRepository.medBlood.collectAsState(initial = "")
    val medChronic by viewModel.securityRepository.medChronic.collectAsState(initial = "")

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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MedicalInfoCard("Ad Soyad", "$medName $medSurname")
            MedicalInfoCard("Yaş", medAge)
            MedicalInfoCard("Kan Grubu", medBlood, Color.Red)
            MedicalInfoCard("Kronik Hastalıklar ve İlaçlar", medChronic)
            MedicalInfoCard("Adres", medAddress)
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
            val textValue = if (value.isBlank()) "Belirtilmemiş" else value
            Text(text = textValue, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface)
        }
    }
}
