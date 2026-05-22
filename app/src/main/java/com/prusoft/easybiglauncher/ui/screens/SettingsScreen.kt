package com.prusoft.easybiglauncher.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background

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
    
    val sosMessageDefault = stringResource(R.string.sos_message_label)
    var sosMessage by remember { mutableStateOf(sharedPref.getString("sos_message", sosMessageDefault) ?: "") }
    var sosNumber by remember { mutableStateOf(sharedPref.getString("sos_number", "") ?: "") }
    
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val isHomeFavLockEnabled by viewModel.securityRepository.isHomeFavLockEnabled.collectAsState(initial = false)
    val clockTapAction by viewModel.securityRepository.clockTapAction.collectAsState(initial = 2)
    val savedPin by viewModel.securityRepository.savedPin.collectAsState(initial = null)
    var isDefault by remember { mutableStateOf(LauncherUtils.isDefaultLauncher(context)) }
    
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        isDefault = LauncherUtils.isDefaultLauncher(context)
    }

    var isIgnoringBattery by remember { mutableStateOf(BatteryOptimizationManager.isIgnoringBatteryOptimizations(context)) }
    var showSetPinDialog by remember { mutableStateOf(false) }
    var currentCategory by remember { mutableIntStateOf(0) } // 0: Main, 1: Language & Sound, 2: Security, 3: SOS, 4: Home Management, 5: Medical

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(text = when(currentCategory) {
                        1 -> stringResource(R.string.settings_category_1)
                        2 -> stringResource(R.string.settings_category_2)
                        3 -> stringResource(R.string.settings_category_3)
                        4 -> stringResource(R.string.settings_category_4)
                        5 -> stringResource(R.string.settings_category_5)
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
                SettingsMenuButton(stringResource(R.string.settings_category_1)) { currentCategory = 1 }
                SettingsMenuButton(stringResource(R.string.settings_category_2)) { currentCategory = 2 }
                SettingsMenuButton(stringResource(R.string.settings_category_3)) { currentCategory = 3 }
                SettingsMenuButton(stringResource(R.string.settings_category_4)) { currentCategory = 4 }
                SettingsMenuButton(stringResource(R.string.settings_category_5)) { currentCategory = 5 }
                
                Spacer(modifier = Modifier.weight(1f))
                
                var showResetConfirm by remember { mutableStateOf(false) }

                Button(
                    onClick = {
                        val i = Intent(Intent.ACTION_SENDTO)
                        i.data = Uri.parse("mailto:prusoft16@gmail.com")
                        context.startActivity(Intent.createChooser(i, "Send Email"))
                    },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.contact_us), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showResetConfirm = true },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                ) {
                    Text(stringResource(R.string.reset_app_settings), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }

                if (showResetConfirm) {
                    AlertDialog(
                        onDismissRequest = { showResetConfirm = false },
                        title = { Text(stringResource(R.string.reset_app_settings)) },
                        text = { Text(stringResource(R.string.reset_app_confirm)) },
                        confirmButton = {
                            Button(onClick = {
                                scope.launch {
                                    // Reset DB
                                    viewModel.repository.nukeTable()
                                    viewModel.repository.insertInitialData()
                                    // Reset preferences
                                    sharedPref.edit().clear().apply()
                                    viewModel.securityRepository.clearAllExceptLanguageAndOnboarding()
                                    // Go back
                                    showResetConfirm = false
                                    navController.popBackStack()
                                    // Restarts activity to apply cleanly
                                    (context as? android.app.Activity)?.recreate()
                                }
                            }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                                Text(stringResource(R.string.yes))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showResetConfirm = false }) { Text(stringResource(R.string.cancel)) }
                        }
                    )
                }
            } else {
                when(currentCategory) {
                    1 -> {
                        val isSmsTtsEnabled by viewModel.securityRepository.isSmsTtsEnabled.collectAsState(initial = false)
                        Text(text = stringResource(id = R.string.language_option), style = MaterialTheme.typography.titleLarge)
                        Button(onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")) }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) { Text("English", fontSize = 24.sp) }
                        Button(onClick = { AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr")) }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) { Text("Türkçe", fontSize = 24.sp) }
                        
                        Divider()
                        val isAutoBatteryWarningEnabled by viewModel.securityRepository.isAutoBatteryWarningEnabled.collectAsState(initial = true)
                        SettingsToggleRow(
                            text = stringResource(R.string.auto_battery_warning),
                            isChecked = isAutoBatteryWarningEnabled,
                            onCheckedChange = { scope.launch { viewModel.securityRepository.setAutoBatteryWarningEnabled(it) } }
                        )

                        SettingsToggleRow(
                            text = stringResource(R.string.sms_read_incoming),
                            isChecked = isSmsTtsEnabled,
                            onCheckedChange = { scope.launch { viewModel.securityRepository.setSmsTtsEnabled(it) } }
                        )
                        Divider()
                        Text(text = stringResource(R.string.clock_tap_action), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        var expanded by remember { mutableStateOf(false) }
                        val options = listOf(
                            0 to stringResource(R.string.clock_action_read),
                            1 to stringResource(R.string.clock_action_calendar),
                            2 to stringResource(R.string.clock_action_both)
                        )
                        val selectedOptionText = options.firstOrNull { it.first == clockTapAction }?.second ?: ""
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp)
                                    .padding(vertical = 4.dp)
                                    .background(Color.White, shape = RoundedCornerShape(12.dp))
                                    .clickable { expanded = true }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = selectedOptionText,
                                    fontSize = 20.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(text = "▼", fontSize = 16.sp, color = Color.Black)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .background(Color.White)
                            ) {
                                options.forEach { (value, label) ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                text = label, 
                                                fontSize = 20.sp, 
                                                fontWeight = FontWeight.Bold, 
                                                color = Color.Black
                                            ) 
                                        },
                                        onClick = {
                                            scope.launch { viewModel.securityRepository.setClockTapAction(value) }
                                            expanded = false
                                        },
                                        modifier = Modifier.background(Color.White)
                                    )
                                }
                            }
                        }
                    }
                    2 -> {
                        SettingsToggleRow(
                            text = stringResource(R.string.protection_mode),
                            isChecked = isProtectionEnabled,
                            onCheckedChange = { checked ->
                                if (checked && savedPin == null) showSetPinDialog = true
                                else scope.launch { viewModel.securityRepository.setProtectionEnabled(checked) }
                            }
                        )
                        Button(onClick = { showSetPinDialog = true }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) {
                            Text(stringResource(R.string.change_pin), fontSize = 24.sp)
                        }
                        SettingsToggleRow(
                            text = stringResource(R.string.home_fav_lock),
                            isChecked = isHomeFavLockEnabled,
                            onCheckedChange = { scope.launch { viewModel.securityRepository.setHomeFavLockEnabled(it) } }
                        )
                    }
                    3 -> {
                        Text(text = stringResource(R.string.sos_settings), style = MaterialTheme.typography.titleLarge)
                        var isLowBatterySosEnabled by remember { mutableStateOf(sharedPref.getBoolean("low_battery_sos_enabled", false)) }
                        SettingsToggleRow(
                            text = stringResource(R.string.low_battery_sos),
                            isChecked = isLowBatterySosEnabled,
                            onCheckedChange = { 
                                isLowBatterySosEnabled = it
                                sharedPref.edit().putBoolean("low_battery_sos_enabled", it).apply() 
                            }
                        )
                        var sendLocationSosEnabled by remember { mutableStateOf(sharedPref.getBoolean("sos_send_location", false)) }
                        SettingsToggleRow(
                            text = stringResource(R.string.sos_send_location),
                            isChecked = sendLocationSosEnabled,
                            onCheckedChange = { 
                                sendLocationSosEnabled = it
                                sharedPref.edit().putBoolean("sos_send_location", it).apply() 
                            }
                        )
                        
                        val contactPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
                            uri?.let {
                                val cursor = context.contentResolver.query(it, null, null, null, null)
                                cursor?.use { c ->
                                    if (c.moveToFirst()) {
                                        val id = c.getString(c.getColumnIndexOrThrow(android.provider.ContactsContract.Contacts._ID))
                                        val phones = context.contentResolver.query(
                                            android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                            null,
                                            android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                            arrayOf(id),
                                            null
                                        )
                                        phones?.use { p ->
                                            if (p.moveToFirst()) {
                                                val num = p.getString(p.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))
                                                sosNumber = num
                                                sharedPref.edit().putString("sos_number", num).apply()
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = sosNumber,
                                onValueChange = { sosNumber = it; sharedPref.edit().putString("sos_number", it).apply() },
                                label = { Text(stringResource(R.string.sos_number_label)) },
                                modifier = Modifier.weight(1f),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 22.sp),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
                            )
                            IconButton(onClick = { contactPickerLauncher.launch(null) }) {
                                Icon(Icons.Default.Person, contentDescription = stringResource(R.string.sos_pick_contact), modifier = Modifier.size(48.dp))
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
                        Button(onClick = { navController.navigate("manage_pages") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
                            Text(stringResource(R.string.manage_pages), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(onClick = { 
                            if (isDefault) {
                                try {
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_HOME_SETTINGS)
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    // Fallback if the standard intent isn't supported on specific OEMs
                                    val intent = android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
                                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                }
                            } else {
                                val intent = LauncherUtils.getRoleRequestIntent(context)
                                if (intent != null) launcher.launch(intent)
                                else LauncherUtils.requestSetDefaultLauncher(context)
                            }
                        }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(16.dp)) {
                            Text(if (isDefault) "${stringResource(R.string.set_default)} (${stringResource(R.string.active_status)})" else stringResource(R.string.set_default), fontSize = 20.sp)
                        }
                        
                        Divider()
                        
                        var showOtherTools by remember { mutableStateOf(sharedPref.getBoolean("show_other_tools", true)) }
                        SettingsToggleRow(
                            text = stringResource(R.string.show_other_tools),
                            isChecked = showOtherTools,
                            onCheckedChange = { 
                                showOtherTools = it
                                sharedPref.edit().putBoolean("show_other_tools", it).apply()
                            }
                        )
                        
                        Divider()

                        val isHideSettingsEnabled by viewModel.securityRepository.isHideSettingsEnabled.collectAsState(initial = false)
                        var showHideSettingsWarning by remember { mutableStateOf(false) }

                        SettingsToggleRow(
                            text = stringResource(R.string.hide_settings_btn),
                            isChecked = isHideSettingsEnabled,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showHideSettingsWarning = true
                                } else {
                                    scope.launch { viewModel.securityRepository.setHideSettingsEnabled(false) }
                                }
                            }
                        )

                        if (showHideSettingsWarning) {
                            AlertDialog(
                                onDismissRequest = { showHideSettingsWarning = false },
                                title = { Text(stringResource(R.string.dialog_warning)) },
                                text = { Text(stringResource(R.string.hide_settings_btn_warning)) },
                                confirmButton = {
                                    Button(onClick = {
                                        scope.launch {
                                            viewModel.securityRepository.setHideSettingsEnabled(true)
                                            showHideSettingsWarning = false
                                        }
                                    }) {
                                        Text(stringResource(R.string.ok))
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showHideSettingsWarning = false }) {
                                        Text(stringResource(R.string.close_btn))
                                    }
                                }
                            )
                        }
                    }
                    5 -> {
                        val medName by viewModel.securityRepository.medName.collectAsState(initial = "")
                        val medSurname by viewModel.securityRepository.medSurname.collectAsState(initial = "")
                        val medAge by viewModel.securityRepository.medAge.collectAsState(initial = "")
                        val medAddress by viewModel.securityRepository.medAddress.collectAsState(initial = "")
                        val medBlood by viewModel.securityRepository.medBlood.collectAsState(initial = "")
                        val medChronic by viewModel.securityRepository.medChronic.collectAsState(initial = "")
                        val medContactName by viewModel.securityRepository.medContactName.collectAsState(initial = "")
                        val medContactNumber by viewModel.securityRepository.medContactNumber.collectAsState(initial = "")
                        val medContactRelation by viewModel.securityRepository.medContactRelation.collectAsState(initial = "")

                        var localName by remember(medName) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(medName)) }
                        var localSurname by remember(medSurname) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(medSurname)) }
                        var localAge by remember(medAge) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(medAge)) }
                        var localAddressTextField by remember(medAddress) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(medAddress)) }
                        var localChronic by remember(medChronic) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(medChronic)) }
                        val firstSpaceIndex = medBlood.indexOf(" ")
                        val initialBloodType = if (firstSpaceIndex != -1) medBlood.substring(0, firstSpaceIndex) else "A"
                        val initialBloodRh = if (firstSpaceIndex != -1) medBlood.substring(firstSpaceIndex + 1) else "${stringResource(R.string.positive)} (+)"
                        var localBloodType by remember(medBlood) { mutableStateOf(initialBloodType) }
                        var localBloodRh by remember(medBlood) { mutableStateOf(initialBloodRh) }
                        var localContactName by remember(medContactName) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(medContactName)) }
                        var localContactNumber by remember(medContactNumber) { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue(medContactNumber)) }
                        var localContactRelation by remember(medContactRelation) { mutableStateOf(if(medContactRelation.isEmpty()) context.getString(R.string.other) else medContactRelation) }

                        Text(text = stringResource(R.string.medical_id_short), style = MaterialTheme.typography.titleLarge)
                        TextField(value = localName, onValueChange = { localName = it }, label = { Text(stringResource(R.string.medical_firstname)) }, modifier = Modifier.fillMaxWidth())
                        TextField(value = localSurname, onValueChange = { localSurname = it }, label = { Text(stringResource(R.string.medical_lastname)) }, modifier = Modifier.fillMaxWidth())
                        TextField(value = localAge, onValueChange = { localAge = it }, label = { Text(stringResource(R.string.medical_age)) }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            var expandedType by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }, modifier = Modifier.weight(1f)) {
                                TextField(value = localBloodType, onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.medical_blood)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) }, modifier = Modifier.menuAnchor())
                                ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
                                    listOf("A", "B", "AB", "0").forEach { type ->
                                        DropdownMenuItem(text = { Text(type) }, onClick = { localBloodType = type; expandedType = false })
                                    }
                                }
                            }
                            var expandedRh by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(expanded = expandedRh, onExpandedChange = { expandedRh = !expandedRh }, modifier = Modifier.weight(1f)) {
                                TextField(value = localBloodRh, onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.medical_rh)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRh) }, modifier = Modifier.menuAnchor())
                                ExposedDropdownMenu(expanded = expandedRh, onDismissRequest = { expandedRh = false }) {
                                    listOf("${stringResource(R.string.positive)} (+)", "${stringResource(R.string.negative)} (-)").forEach { rh ->
                                        DropdownMenuItem(text = { Text(rh) }, onClick = { localBloodRh = rh; expandedRh = false })
                                    }
                                }
                            }
                        }
                        TextField(value = localChronic, onValueChange = { localChronic = it }, label = { Text(stringResource(R.string.medical_chronic)) }, modifier = Modifier.fillMaxWidth())
                        TextField(value = localAddressTextField, onValueChange = { localAddressTextField = it }, label = { Text(stringResource(R.string.medical_address)) }, modifier = Modifier.fillMaxWidth())
                        
                        val locationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                            if (isGranted) {
                                try {
                                    val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        if (location != null) {
                                            try {
                                                val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                                if (addresses != null && addresses.isNotEmpty()) {
                                                    val address = addresses[0]
                                                    localAddressTextField = androidx.compose.ui.text.input.TextFieldValue(address.getAddressLine(0) ?: "")
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        
                        Button(
                            onClick = {
                                if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                                    try {
                                        val fusedLocationClient = com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(context)
                                        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                            if (location != null) {
                                                try {
                                                    val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
                                                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                                    if (addresses != null && addresses.isNotEmpty()) {
                                                        val address = addresses[0]
                                                        localAddressTextField = androidx.compose.ui.text.input.TextFieldValue(address.getAddressLine(0) ?: "")
                                                    }
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.width(16.dp))
                            Text(stringResource(R.string.get_current_location), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = stringResource(R.string.medical_contact_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        TextField(value = localContactName, onValueChange = { localContactName = it }, label = { Text(stringResource(R.string.medical_contact_name)) }, modifier = Modifier.fillMaxWidth())
                        TextField(value = localContactNumber, onValueChange = { localContactNumber = it }, label = { Text(stringResource(R.string.medical_contact_number)) }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Phone), modifier = Modifier.fillMaxWidth())
                        var expandedRelation by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(expanded = expandedRelation, onExpandedChange = { expandedRelation = !expandedRelation }, modifier = Modifier.fillMaxWidth()) {
                            TextField(value = localContactRelation, onValueChange = {}, readOnly = true, label = { Text(stringResource(R.string.medical_contact_relation)) }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRelation) }, modifier = Modifier.menuAnchor().fillMaxWidth())
                            ExposedDropdownMenu(expanded = expandedRelation, onDismissRequest = { expandedRelation = false }) {
                                val relations = listOf(stringResource(R.string.son), stringResource(R.string.daughter), stringResource(R.string.spouse), stringResource(R.string.mother), stringResource(R.string.father), stringResource(R.string.grandchild), stringResource(R.string.other))
                                relations.forEach { relation ->
                                    DropdownMenuItem(text = { Text(relation) }, onClick = { localContactRelation = relation; expandedRelation = false })
                                }
                            }
                        }
                        Button(onClick = {
                            scope.launch { viewModel.securityRepository.setMedicalInfo(localName.text, localSurname.text, localAge.text, localAddressTextField.text, "$localBloodType $localBloodRh", localChronic.text, localContactName.text, localContactNumber.text, localContactRelation) }
                            currentCategory = 0
                        }, modifier = Modifier.fillMaxWidth().height(80.dp).padding(top = 16.dp), shape = RoundedCornerShape(16.dp)) {
                            Text(stringResource(R.string.medical_save), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
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
    Card(
        modifier = Modifier.fillMaxWidth().height(100.dp).clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(contentAlignment = androidx.compose.ui.Alignment.Center, modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
            Text(text, fontSize = 26.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, lineHeight = 30.sp, color = Color.Black)
        }
    }
}

@Composable
fun SettingsToggleRow(text: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White, contentColor = Color.Black),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
            Text(text, fontSize = 22.sp, modifier = Modifier.weight(1f), color = Color.Black, fontWeight = FontWeight.SemiBold)
            Switch(checked = isChecked, onCheckedChange = onCheckedChange)
        }
    }
}
