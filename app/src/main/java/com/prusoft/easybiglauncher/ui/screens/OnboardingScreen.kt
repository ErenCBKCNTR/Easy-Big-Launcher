package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import kotlinx.coroutines.launch
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.core.os.LocaleListCompat
import androidx.appcompat.app.AppCompatDelegate

import androidx.compose.ui.res.stringResource
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.ui.components.PermissionDisclosureDialog
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.prusoft.easybiglauncher.utils.BatteryOptimizationManager
import androidx.compose.ui.platform.LocalContext

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    val pagerState = rememberPagerState(pageCount = { 5 })

    var isIgnoringBattery by remember { mutableStateOf(BatteryOptimizationManager.isIgnoringBatteryOptimizations(context)) }
    var hasDndPermission by remember { 
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mutableStateOf(notificationManager.isNotificationPolicyAccessGranted) 
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isIgnoringBattery = BatteryOptimizationManager.isIgnoringBatteryOptimizations(context)
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                hasDndPermission = notificationManager.isNotificationPolicyAccessGranted
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var showSosDisclosure by remember { mutableStateOf(false) }
    val sosPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> scope.launch { pagerState.animateScrollToPage(2) } }

    var showContactsDisclosure by remember { mutableStateOf(false) }
    val contactsPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> scope.launch { pagerState.animateScrollToPage(3) } }
    
    var showSmsDisclosure by remember { mutableStateOf(false) }
    val smsPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> scope.launch { pagerState.animateScrollToPage(4) } }

    var skipDialogVisible by remember { mutableStateOf(false) }
    if (skipDialogVisible) {
        AlertDialog(
            onDismissRequest = { skipDialogVisible = false },
            title = { Text(stringResource(R.string.skip_setup)) },
            text = { Text(stringResource(R.string.skip_setup_desc)) },
            confirmButton = {
                TextButton(onClick = {
                    skipDialogVisible = false
                    scope.launch {
                        viewModel.securityRepository.setOnboardingCompleted(true)
                        navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                    }
                }) { Text(stringResource(R.string.yes)) }
            },
            dismissButton = {
                TextButton(onClick = { skipDialogVisible = false }) { Text(stringResource(R.string.no)) }
            }
        )
    }

    if (showSosDisclosure) {
        PermissionDisclosureDialog(
            description = stringResource(R.string.disclosure_sos_desc),
            onAccept = {
                showSosDisclosure = false
                sosPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.SEND_SMS,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            },
            onDecline = { showSosDisclosure = false }
        )
    }

    if (showContactsDisclosure) {
        PermissionDisclosureDialog(
            description = stringResource(R.string.disclosure_contacts_desc),
            onAccept = {
                showContactsDisclosure = false
                contactsPermissionsLauncher.launch(
                    arrayOf(
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.CALL_PHONE,
                        Manifest.permission.READ_CALL_LOG
                    )
                )
            },
            onDecline = { showContactsDisclosure = false }
        )
    }

    if (showSmsDisclosure) {
        PermissionDisclosureDialog(
            description = stringResource(R.string.permission_sms_desc),
            onAccept = {
                showSmsDisclosure = false
                smsPermissionsLauncher.launch(arrayOf(Manifest.permission.READ_SMS))
            },
            onDecline = { showSmsDisclosure = false }
        )
    }

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (page) {
                        0 -> {
                            Text(
                                text = stringResource(R.string.choose_language),
                                style = MaterialTheme.typography.displaySmall.copy(color = Color.White, textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = stringResource(R.string.onboarding_welcome_text),
                                style = MaterialTheme.typography.bodyLarge.copy(color = Color.LightGray, textAlign = TextAlign.Center)
                            )
                            Spacer(modifier = Modifier.height(40.dp))
                            LanguageButton(text = "🇹🇷 TÜRKÇE") {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"))
                                scope.launch { viewModel.securityRepository.setLanguage("tr") }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            LanguageButton(text = "🇬🇧 ENGLISH") {
                                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                scope.launch { viewModel.securityRepository.setLanguage("en") }
                            }
                            Spacer(modifier = Modifier.height(30.dp))
                            Button(onClick = { scope.launch { pagerState.animateScrollToPage(1) } }) {
                                Text(stringResource(R.string.next), fontSize = 24.sp)
                            }
                        }
                        1 -> {
                            Text("SOS / Acil Durum İzni", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(stringResource(R.string.disclosure_sos_desc), fontSize = 18.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(40.dp))
                            Button(onClick = { showSosDisclosure = true }, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                                Text(stringResource(R.string.grant_permission), fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { scope.launch { pagerState.animateScrollToPage(2) } }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                Text(stringResource(R.string.next), fontSize = 18.sp)
                            }
                        }
                        2 -> {
                            Text("Rehber & Arama İzni", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(stringResource(R.string.disclosure_contacts_desc), fontSize = 18.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(40.dp))
                            Button(onClick = { showContactsDisclosure = true }, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                                Text(stringResource(R.string.grant_permission), fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { scope.launch { pagerState.animateScrollToPage(3) } }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                Text(stringResource(R.string.next), fontSize = 18.sp)
                            }
                        }
                        3 -> {
                            Text(stringResource(R.string.permission_sms), fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(stringResource(R.string.permission_sms_desc), fontSize = 18.sp, color = Color.LightGray, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(40.dp))
                            Button(onClick = { showSmsDisclosure = true }, modifier = Modifier.fillMaxWidth().height(60.dp)) {
                                Text(stringResource(R.string.grant_permission), fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(onClick = { scope.launch { pagerState.animateScrollToPage(4) } }, colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)) {
                                Text(stringResource(R.string.next), fontSize = 18.sp)
                            }
                        }
                        4 -> {
                            Text(stringResource(R.string.setup_complete), fontSize = 32.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { BatteryOptimizationManager.requestIgnoreBatteryOptimizations(context) },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (isIgnoringBattery) Color(0xFF4CAF50) else Color.DarkGray)
                            ) { Text(stringResource(R.string.battery_optimization_title), fontSize = 16.sp) }
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { 
                                    try {
                                        val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                        context.startActivity(intent)
                                    } catch (e: Exception) { e.printStackTrace() }
                                },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (hasDndPermission) Color(0xFF4CAF50) else Color.DarkGray)
                            ) { Text("SES KONTROL / DND PERM", fontSize = 16.sp) }
                            Spacer(modifier = Modifier.height(40.dp))
                            Button(
                                onClick = {
                                    scope.launch {
                                        viewModel.securityRepository.setOnboardingCompleted(true)
                                        navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(80.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(stringResource(R.string.onboarding_start), fontSize = 28.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            
            if (pagerState.currentPage < 4) {
                TextButton(
                    onClick = { skipDialogVisible = true },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(stringResource(R.string.skip_setup), color = Color.Gray, fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
fun LanguageButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Text(text = text, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
    }
}
