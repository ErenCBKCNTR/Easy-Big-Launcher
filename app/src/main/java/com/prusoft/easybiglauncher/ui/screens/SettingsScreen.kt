package com.prusoft.easybiglauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.app.role.RoleManager
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.ui.components.PinPadDialog
import com.prusoft.easybiglauncher.utils.LauncherUtils
import com.prusoft.easybiglauncher.utils.BatteryOptimizationManager
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color

@Composable
fun SettingsWrapper(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val savedPin by viewModel.securityRepository.savedPin.collectAsState(initial = null)
    
    var isAuthenticated by remember { mutableStateOf(!isProtectionEnabled) }
    var showPinDialog by remember { mutableStateOf(false) }

    if (isAuthenticated) {
        SettingsScreen(navController = navController, viewModel = viewModel)
    } else if (showPinDialog) {
        PinPadDialog(
            onPinDismiss = { navController.popBackStack() },
            onPinEntered = { pin ->
                if (pin == savedPin) {
                    isAuthenticated = true
                    showPinDialog = false
                }
            }
        )
    } else {
        LaunchedEffect(Unit) { showPinDialog = true }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPref = remember { context.getSharedPreferences("sos_prefs", Context.MODE_PRIVATE) }
    var sosMessage by remember { mutableStateOf(sharedPref.getString("sos_message", "Yardıma ihtiyacım var!") ?: "") }
    var sosNumber by remember { mutableStateOf(sharedPref.getString("sos_number", "") ?: "") }
    
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val isTtsEnabled by viewModel.securityRepository.isTtsEnabled.collectAsState(initial = false)
    val savedPin by viewModel.securityRepository.savedPin.collectAsState(initial = null)
    var isDefault by remember { mutableStateOf(LauncherUtils.isDefaultLauncher(context)) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isDefault = LauncherUtils.isDefaultLauncher(context)
    }

    var isIgnoringBattery by remember { mutableStateOf(BatteryOptimizationManager.isIgnoringBatteryOptimizations(context)) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var currentCategory by remember { mutableIntStateOf(0) } // 0: Main, 1: Language & Sound, 2: Security, 3: SOS, 4: Home Management

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(text = when(currentCategory) {
                        1 -> "Dil ve Ses"
                        2 -> "Güvenlik ve Şifre"
                        3 -> "Acil Durum (SOS)"
                        4 -> "Ana Ekran Yönetimi"
                        else -> stringResource(id = R.string.settings_title)
                    }) 
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (currentCategory != 0) currentCategory = 0 else navController.popBackStack() 
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(id = R.string.go_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (currentCategory == 0) {
                // Main Menu
                SettingsMenuButton("1. Dil ve Ses") { currentCategory = 1 }
                SettingsMenuButton("2. Güvenlik ve Şifre") { currentCategory = 2 }
                SettingsMenuButton("3. Acil Durum (SOS)") { currentCategory = 3 }
                SettingsMenuButton("4. Ana Ekran Yönetimi") { currentCategory = 4 }
                SettingsMenuButton("5. Tıbbi Kimlik Bilgileri") { currentCategory = 5 }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { 
                        try {
                            val intent = Intent(Intent.ACTION_DELETE).apply {
                                data = Uri.parse("package:${context.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Uygulamayı Kaldır (Uninstall)", fontSize = 22.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            } else {
                when(currentCategory) {
                    1 -> {
                        // Language & Sound
                        Text(text = stringResource(id = R.string.language_option), style = MaterialTheme.typography.titleLarge)
                        Button(onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) { Text("English", fontSize = 24.sp) }
                        Button(onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr")) }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) { Text("Türkçe", fontSize = 24.sp) }
                        
                        Divider()
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(stringResource(R.string.voice_feedback), fontSize = 24.sp, modifier = Modifier.weight(1f))
                            Switch(checked = isTtsEnabled, onCheckedChange = { 
                                scope.launch { viewModel.securityRepository.setTtsEnabled(it) }
                            })
                        }
                    }
                    2 -> {
                        // Security
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(stringResource(R.string.protection_mode), fontSize = 24.sp, modifier = Modifier.weight(1f))
                            Switch(checked = isProtectionEnabled, onCheckedChange = { checked ->
                                if (checked && savedPin == null) {
                                    showSetPinDialog = true
                                } else {
                                    scope.launch { viewModel.securityRepository.setProtectionEnabled(checked) }
                                }
                            })
                        }
                        
                        Button(onClick = { showSetPinDialog = true }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) {
                            Text("Şifre Değiştir", fontSize = 24.sp)
                        }

                        Divider()
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = stringResource(R.string.battery_optimization_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(text = stringResource(R.string.battery_optimization_desc), style = MaterialTheme.typography.bodyMedium)
                            Button(
                                onClick = { BatteryOptimizationManager.requestIgnoreBatteryOptimizations(context) },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isIgnoringBattery) Color(0xFF4CAF50) else Color.Red)
                            ) {
                                Text(
                                    text = if (isIgnoringBattery) stringResource(R.string.battery_protection_active) else stringResource(R.string.battery_protection_inactive),
                                    fontSize = 20.sp, fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    3 -> {
                        // SOS
                        Text(text = stringResource(R.string.sos_settings), style = MaterialTheme.typography.titleLarge)
                        var isLowBatterySosEnabled by remember { mutableStateOf(sharedPref.getBoolean("low_battery_sos_enabled", false)) }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(stringResource(R.string.low_battery_sos), fontSize = 24.sp, modifier = Modifier.weight(1f))
                            Switch(checked = isLowBatterySosEnabled, onCheckedChange = { 
                                isLowBatterySosEnabled = it
                                sharedPref.edit().putBoolean("low_battery_sos_enabled", it).apply() 
                            })
                        }
                        
                        var sendLocationSosEnabled by remember { mutableStateOf(sharedPref.getBoolean("sos_send_location", false)) }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text("Konumumu da Gönder", fontSize = 24.sp, modifier = Modifier.weight(1f))
                            Switch(checked = sendLocationSosEnabled, onCheckedChange = { 
                                sendLocationSosEnabled = it
                                sharedPref.edit().putBoolean("sos_send_location", it).apply() 
                            })
                        }
                        
                        val contactPickerLauncher = rememberLauncherForActivityResult(
                            androidx.activity.result.contract.ActivityResultContracts.PickContact()
                        ) { uri ->
                            uri?.let {
                                val cursor = context.contentResolver.query(it, null, null, null, null)
                                if (cursor != null && cursor.moveToFirst()) {
                                    val id = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.Contacts._ID))
                                    val phones = context.contentResolver.query(
                                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                        null,
                                        android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                        arrayOf(id),
                                        null
                                    )
                                    if (phones != null && phones.moveToFirst()) {
                                        val num = phones.getString(phones.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))
                                        sosNumber = num
                                        sharedPref.edit().putString("sos_number", num).apply()
                                        phones.close()
                                    }
                                    cursor.close()
                                }
                            }
                        }

                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = sosNumber,
                                onValueChange = { sosNumber = it; sharedPref.edit().putString("sos_number", it).apply() },
                                label = { Text("Acil Durum Telefon Numarası") },
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                                Icon(Icons.Default.Person, contentDescription = "Rehberden Seç", modifier = Modifier.size(48.dp))
                            }
                        }
                        
                        TextField(
                            value = sosMessage,
                            onValueChange = { sosMessage = it; sharedPref.edit().putString("sos_message", it).apply() },
                            label = { Text(stringResource(R.string.sos_message_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp)
                        )
                    }
                    4 -> {
                        // Home Management
                        Button(
                            onClick = { navController.navigate("manage_pages") },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Text(stringResource(R.string.manage_pages), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        
                        Button(onClick = { 
                            val intent = LauncherUtils.getRoleRequestIntent(context)
                            if (intent != null) launcher.launch(intent)
                            else LauncherUtils.requestSetDefaultLauncher(context)
                        }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) {
                            Text(if (isDefault) "${stringResource(R.string.set_default)} (${stringResource(R.string.active_status)})" else stringResource(R.string.set_default), fontSize = 20.sp)
                        }
                        
                        Divider()
                        Text(text = stringResource(R.string.add_page), style = MaterialTheme.typography.titleLarge)
                        Button(onClick = { viewModel.addPage(3, 2) }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) { Text(stringResource(R.string.add_page_3x2), fontSize = 22.sp) }
                    }
                    5 -> {
                        // Medical ID
                        val medName by viewModel.securityRepository.medName.collectAsState(initial = "")
                        val medSurname by viewModel.securityRepository.medSurname.collectAsState(initial = "")
                        val medAge by viewModel.securityRepository.medAge.collectAsState(initial = "")
                        val medAddress by viewModel.securityRepository.medAddress.collectAsState(initial = "")
                        val medBlood by viewModel.securityRepository.medBlood.collectAsState(initial = "")
                        val medChronic by viewModel.securityRepository.medChronic.collectAsState(initial = "")

                        Text(text = stringResource(R.string.medical_id_short), style = MaterialTheme.typography.titleLarge)
                        
                        TextField(value = medName, onValueChange = { scope.launch { viewModel.securityRepository.setMedicalInfo(it, medSurname, medAge, medAddress, medBlood, medChronic) } }, label = { Text("Ad") }, modifier = Modifier.fillMaxWidth())
                        TextField(value = medSurname, onValueChange = { scope.launch { viewModel.securityRepository.setMedicalInfo(medName, it, medAge, medAddress, medBlood, medChronic) } }, label = { Text("Soyad") }, modifier = Modifier.fillMaxWidth())
                        TextField(value = medAge, onValueChange = { scope.launch { viewModel.securityRepository.setMedicalInfo(medName, medSurname, it, medAddress, medBlood, medChronic) } }, label = { Text("Yaş") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        TextField(value = medBlood, onValueChange = { scope.launch { viewModel.securityRepository.setMedicalInfo(medName, medSurname, medAge, medAddress, it, medChronic) } }, label = { Text("Kan Grubu") }, modifier = Modifier.fillMaxWidth())
                        TextField(value = medChronic, onValueChange = { scope.launch { viewModel.securityRepository.setMedicalInfo(medName, medSurname, medAge, medAddress, medBlood, it) } }, label = { Text("Kronik Rahatsızlıklar/İlaçlar") }, modifier = Modifier.fillMaxWidth())
                        TextField(value = medAddress, onValueChange = { scope.launch { viewModel.securityRepository.setMedicalInfo(medName, medSurname, medAge, it, medBlood, medChronic) } }, label = { Text("Adres") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            if (showSetPinDialog) {
                PinPadDialog(
                    onPinDismiss = { showSetPinDialog = false },
                    onPinEntered = { pin ->
                        scope.launch {
                            viewModel.securityRepository.setPin(pin)
                            viewModel.securityRepository.setProtectionEnabled(true)
                        }
                        showSetPinDialog = false
                    },
                    isSettingNewPin = true
                )
            }
        }
    }
}

@Composable
fun SettingsMenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
    ) {
        Text(text, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, lineHeight = 30.sp)
    }
}
