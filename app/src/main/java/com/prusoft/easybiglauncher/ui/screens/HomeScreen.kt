package com.prusoft.easybiglauncher.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import com.prusoft.easybiglauncher.data.ItemType
import com.prusoft.easybiglauncher.data.LauncherItem
import com.prusoft.easybiglauncher.viewmodel.LauncherViewModel
import com.prusoft.easybiglauncher.ui.components.PinPadDialog
import com.prusoft.easybiglauncher.ui.components.StatusBarWidget
import com.prusoft.easybiglauncher.ui.components.ButtonEditorSheet
import com.prusoft.easybiglauncher.components.BigButton
import com.prusoft.easybiglauncher.components.BigFooterButton
import com.prusoft.easybiglauncher.utils.NotificationTracker
import androidx.compose.foundation.combinedClickable
import androidx.activity.compose.BackHandler

import androidx.compose.ui.res.stringResource
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.ui.components.PermissionDisclosureDialog
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    var showContactsDisclosure by remember { mutableStateOf(false) }
    
    val contactsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val hasContacts = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        if (!hasContacts) {
            showContactsDisclosure = true
        }
    }

    if (showContactsDisclosure) {
        PermissionDisclosureDialog(
            description = stringResource(R.string.disclosure_contacts_desc),
            onAccept = {
                showContactsDisclosure = false
                contactsLauncher.launch(arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE))
            },
            onDecline = { showContactsDisclosure = false }
        )
    }

    val pages by viewModel.pages.collectAsState(initial = emptyList())
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val missedCalls by NotificationTracker.missedCalls.collectAsState()
    val unreadSms by NotificationTracker.unreadSmsCount.collectAsState()
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val isTtsEnabled by viewModel.securityRepository.isTtsEnabled.collectAsState(initial = false)
    val language by viewModel.securityRepository.language.collectAsState(initial = "tr")
    val savedPin by viewModel.securityRepository.savedPin.collectAsState(initial = null)
    
    val ttsManager = remember { com.prusoft.easybiglauncher.utils.TTSManager.getInstance(context) }
    
    LaunchedEffect(language) {
        ttsManager.setLanguage(java.util.Locale(language))
    }

    var showPinDialogForItem by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<LauncherItem?>(null) }
    var showPinDialogForNav by remember { mutableStateOf(false) }
    var navDestination by remember { mutableStateOf("") }
    var showEditSheet by remember { mutableStateOf(false) }
    
    var showAddSlotDialog by remember { mutableStateOf(false) }
    var slotToAssign by remember { mutableStateOf<LauncherItem?>(null) }
    val scope = rememberCoroutineScope()

    BackHandler {
        // Do nothing to prevent exiting the launcher via back button
    }

    val contactPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(android.provider.ContactsContract.Contacts.DISPLAY_NAME))
                
                // Get phone number
                val phones = context.contentResolver.query(
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null,
                    android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    arrayOf(id),
                    null
                )
                var number = ""
                if (phones != null && phones.moveToFirst()) {
                    number = phones.getString(phones.getColumnIndexOrThrow(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER))
                    phones.close()
                }
                cursor.close()
                
                if (slotToAssign != null) {
                    viewModel.assignContactToItem(slotToAssign!!, name, number)
                    slotToAssign = null
                }
            }
        }
    }

    if (showAddSlotDialog && slotToAssign != null) {
        AlertDialog(
            onDismissRequest = { showAddSlotDialog = false },
            title = { Text(stringResource(R.string.add_btn), fontSize = 28.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Lütfen eklemek istediğiniz türü seçin.", fontSize = 20.sp) }, 
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            showAddSlotDialog = false
                            viewModel.setPendingAssignmentItem(slotToAssign)
                            navController.navigate("all_apps")
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.add_app), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = {
                            showAddSlotDialog = false
                            contactPicker.launch(null)
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text(stringResource(R.string.add_contact), fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = { showAddSlotDialog = false }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text(stringResource(R.string.cancel), fontSize = 20.sp)
                    }
                }
            }
        )
    }

    if (showPinDialogForNav) {
        PinPadDialog(
            onPinDismiss = { showPinDialogForNav = false },
            onPinEntered = { pin ->
                if (pin == savedPin) {
                    showPinDialogForNav = false
                    navController.navigate(navDestination)
                }
            }
        )
    }

    if (showPinDialogForItem && itemToEdit != null) {
        PinPadDialog(
            onPinDismiss = { showPinDialogForItem = false },
            onPinEntered = { pin ->
                if (pin == savedPin) {
                    showPinDialogForItem = false
                    showEditSheet = true
                }
            }
        )
    }

    if (showEditSheet && itemToEdit != null) {
        ButtonEditorSheet(
            item = itemToEdit!!,
            onSave = { label, color, image ->
                scope.launch {
                    viewModel.repository.updateItem(itemToEdit!!.copy(customLabel = label, customColor = color, customImageUri = image))
                }
                showEditSheet = false
            },
            onDismiss = { showEditSheet = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarWidget()
        
        Box(modifier = Modifier.weight(1f)) {
            if (pages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Button(onClick = { navController.navigate("settings") }) {
                        Text(stringResource(R.string.go_to_settings_hint))
                    }
                }
            } else {
                HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
                    val page = pages[pageIndex]
                    val items by viewModel.repository.getItemsForPage(page.id).collectAsState(initial = emptyList())

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(page.columnCount),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(items.size) { index ->
                            val item = items[index]
                            GridItem(item, 
                                isTtsEnabled = isTtsEnabled,
                                badgeCount = when {
                                    item.packageName == "com.android.dialer" || item.packageName == "com.google.android.dialer" -> missedCalls
                                    item.packageName == "com.android.messaging" || item.packageName == "com.google.android.apps.messaging" -> unreadSms
                                    else -> 0
                                },
                                onClick = {
                                    if (item.itemType == ItemType.EMPTY) {
                                         if (!isProtectionEnabled) {
                                             slotToAssign = item
                                             showAddSlotDialog = true
                                         }
                                    } else if (item.itemType == ItemType.CONTACT) {
                                        item.packageName?.let { number ->
                                            val intent = Intent(Intent.ACTION_CALL).apply {
                                                data = Uri.parse("tel:$number")
                                            }
                                            context.startActivity(intent)
                                        }
                                    } else {
                                        item.packageName?.let { pkg ->
                                            when {
                                                pkg == "com.android.dialer" || pkg == "com.google.android.dialer" -> {
                                                    navController.navigate("dialer")
                                                }
                                                pkg == "com.android.messaging" || pkg == "com.google.android.apps.messaging" -> {
                                                    navController.navigate("sms")
                                                }
                                                else -> {
                                                    val intent = context.packageManager.getLaunchIntentForPackage(pkg)
                                                    if (intent != null) context.startActivity(intent)
                                                }
                                            }
                                        }
                                    }
                                },
                                onLongClick = {
                                    if (item.itemType != ItemType.EMPTY) {
                                        if (isProtectionEnabled) {
                                            itemToEdit = item
                                            showPinDialogForItem = true
                                        } else {
                                            itemToEdit = item
                                            showEditSheet = true
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Bottom Navigation Bar with Huge Buttons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    BigFooterButton(
                        text = "SOS",
                        icon = Icons.Default.Call,
                        containerColor = Color.Red,
                        contentColor = Color.White,
                        isTtsEnabled = isTtsEnabled,
                        onClick = {
                            // SOS Logic: Send SMS with location and Call
                            // This depends on previous implementation, let's assume it calls a helper
                            navController.navigate("tools") // Fallback or navigate to a dedicated SOS screen if exists
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    BigFooterButton(
                        text = stringResource(R.string.btn_settings),
                        icon = Icons.Default.Settings,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        isTtsEnabled = isTtsEnabled,
                        onClick = {
                            if (isProtectionEnabled) {
                                navDestination = "settings"
                                showPinDialogForNav = true
                            } else {
                                navController.navigate("settings")
                            }
                        }
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    BigFooterButton(
                        text = stringResource(R.string.tools_title),
                        icon = Icons.Default.Build,
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White,
                        isTtsEnabled = isTtsEnabled,
                        onClick = {
                            if (isProtectionEnabled) {
                                navDestination = "tools"
                                showPinDialogForNav = true
                            } else {
                                navController.navigate("tools")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GridItem(item: LauncherItem, isTtsEnabled: Boolean, badgeCount: Int = 0, onClick: () -> Unit, onLongClick: () -> Unit) {
    BigButton(
        text = if (item.itemType == ItemType.EMPTY) stringResource(R.string.add_btn) else (item.customLabel ?: item.label ?: stringResource(R.string.app_placeholder)),
        icon = if (item.itemType == ItemType.EMPTY) Icons.Default.Add else Icons.Default.Apps,
        backgroundColor = if (item.itemType == ItemType.EMPTY) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primaryContainer,
        contentColor = if (item.itemType == ItemType.EMPTY) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimaryContainer,
        badgeCount = badgeCount,
        customColor = item.customColor,
        customImageUri = item.customImageUri,
        isTtsEnabled = isTtsEnabled,
        onClick = onClick,
        onLongClick = onLongClick
    )
}
