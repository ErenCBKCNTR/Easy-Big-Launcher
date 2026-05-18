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

@Composable
fun OnboardingScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var showSosDisclosure by remember { mutableStateOf(false) }
    var showContactsDisclosure by remember { mutableStateOf(false) }
    
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

    val sosPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    val contactsPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

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
                        Manifest.permission.CALL_PHONE
                    )
                )
            },
            onDecline = { showContactsDisclosure = false }
        )
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.onboarding_welcome),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LanguageButton(
                    text = "TÜRKÇE",
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("tr"))
                        scope.launch { viewModel.securityRepository.setLanguage("tr") }
                    }
                )
                LanguageButton(
                    text = "ENGLISH",
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                        scope.launch { viewModel.securityRepository.setLanguage("en") }
                    }
                )

                Button(
                    onClick = { showSosDisclosure = true },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("SOS İZİNLERİ / SOS PERMS", fontSize = 18.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }

                Button(
                    onClick = { showContactsDisclosure = true },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                ) {
                    Text("REHBER İZİNLERİ / CONTACT PERMS", fontSize = 18.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }

                Button(
                    onClick = { 
                        BatteryOptimizationManager.requestIgnoreBatteryOptimizations(context)
                    },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isIgnoringBattery) Color(0xFF4CAF50) else Color(0xFFB71C1C)
                    )
                ) {
                    Text("PİL MUAFİYETİ / BATTERY EXEMPT", fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }

                Button(
                    onClick = { 
                        try {
                            val intent = android.content.Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(70.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (hasDndPermission) Color(0xFF4CAF50) else Color.DarkGray
                    )
                ) {
                    Text("SES KONTROL / DND PERM", fontSize = 18.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    scope.launch {
                        viewModel.securityRepository.setOnboardingCompleted(true)
                        navController.navigate("home") { popUpTo("onboarding") { inclusive = true } }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.onboarding_start), fontSize = 28.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun LanguageButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}
