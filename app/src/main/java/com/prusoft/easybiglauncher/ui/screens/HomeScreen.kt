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
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MedicalInformation
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Person
import com.prusoft.easybiglauncher.utils.EmergencyManager
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

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.prusoft.easybiglauncher.utils.ToolManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: LauncherViewModel = viewModel()) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("sos_prefs", android.content.Context.MODE_PRIVATE) }
    var showContactsDisclosure by remember { mutableStateOf(false) }

    // Drag and Drop state
    var draggedItem by remember { mutableStateOf<LauncherItem?>(null) }
    var currentDragPosition by remember { mutableStateOf(Offset.Zero) }
    val itemPositions = remember { mutableMapOf<Int, androidx.compose.ui.layout.LayoutCoordinates>() }
    
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
                contactsLauncher.launch(arrayOf(
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.CALL_PHONE,
                    Manifest.permission.READ_CALL_LOG
                ))
            },
            onDecline = { showContactsDisclosure = false }
        )
    }

    val pages by viewModel.pages.collectAsState(initial = emptyList())
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val missedCalls by NotificationTracker.missedCalls.collectAsState()
    val unreadSms by NotificationTracker.unreadSmsCount.collectAsState()
    val isProtectionEnabled by viewModel.securityRepository.isProtectionEnabled.collectAsState(initial = false)
    val isHomeFavLockEnabled by viewModel.securityRepository.isHomeFavLockEnabled.collectAsState(initial = false)
    val isHideSettingsEnabled by viewModel.securityRepository.isHideSettingsEnabled.collectAsState(initial = false)
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

    var contactToConfigure by remember { mutableStateOf<Triple<String, String, LauncherItem>?>(null) }
    var showToolDialog by remember { mutableStateOf(false) }
    var showSosWarningDialog by remember { mutableStateOf(false) }

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
                    contactToConfigure = Triple(name, number, slotToAssign!!)
                    slotToAssign = null
                }
            }
        }
    }

    if (contactToConfigure != null) {
        AlertDialog(
            onDismissRequest = { contactToConfigure = null },
            title = { Text(stringResource(R.string.action_choose), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.action_choose_desc), fontSize = 20.sp) },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val (name, number, slot) = contactToConfigure!!
                    Button(onClick = { viewModel.assignContactToItem(slot, name, number); contactToConfigure = null }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text(stringResource(R.string.action_phone_call), fontSize = 20.sp) }
                    Button(onClick = { viewModel.assignContactToItem(slot, name, "whatsapp_audio:$number"); contactToConfigure = null }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text(stringResource(R.string.action_wa_audio), fontSize = 20.sp) }
                    Button(onClick = { viewModel.assignContactToItem(slot, name, "whatsapp_video:$number"); contactToConfigure = null }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))) { Text(stringResource(R.string.action_wa_video), fontSize = 20.sp) }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(onClick = { contactToConfigure = null }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.cancel), fontSize = 18.sp, color = MaterialTheme.colorScheme.error) }
                }
            },
            dismissButton = {}
        )
    }

    if (showSosWarningDialog) {
        AlertDialog(
            onDismissRequest = { showSosWarningDialog = false },
            title = { Text(stringResource(R.string.dialog_warning), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text(stringResource(R.string.sos_not_configured_warning), fontSize = 24.sp) },
            confirmButton = {
                Button(
                    onClick = { showSosWarningDialog = false },
                    modifier = Modifier.fillMaxWidth().height(80.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.close_btn), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showToolDialog && slotToAssign != null) {
        AlertDialog(
            onDismissRequest = { showToolDialog = false },
            title = { Text(stringResource(R.string.action_choose_tool), fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = {
                val magnifierName = stringResource(R.string.magnifier)
                val sirenName = stringResource(R.string.panic_siren)
                val flashlightName = stringResource(R.string.flashlight_on)
                val wifiName = stringResource(R.string.wifi_settings)
                val btName = stringResource(R.string.bluetooth_settings)
                val remindersName = stringResource(R.string.reminders_title)
                
                val tools = listOf(
                    Triple(magnifierName, "tool_magnifier", Icons.Default.Search),
                    Triple(sirenName, "tool_siren", Icons.Default.Warning),
                    Triple(flashlightName, "tool_flashlight", Icons.Default.FlashlightOn),
                    Triple(wifiName, "tool_wifi", Icons.Default.Wifi),
                    Triple(btName, "tool_bluetooth", Icons.Default.Bluetooth),
                    Triple(remindersName, "tool_reminders", Icons.Default.DateRange)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tools.size) { index ->
                        val tool = tools[index]
                        Card(
                            onClick = { viewModel.assignContactToItem(slotToAssign!!, tool.first, tool.second); showToolDialog = false; slotToAssign = null },
                            modifier = Modifier.aspectRatio(1f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(tool.third, contentDescription = null, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(tool.first, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showToolDialog = false }) { Text(stringResource(R.string.cancel), fontSize = 20.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold) }
            }
        )
    }

    if (showAddSlotDialog && slotToAssign != null) {
        AlertDialog(
            onDismissRequest = { showAddSlotDialog = false },
            title = { Text(stringResource(R.string.add_btn), fontSize = 28.sp, fontWeight = FontWeight.Bold) },
            text = { Text(stringResource(R.string.add_item_type_desc), fontSize = 20.sp) }, 
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
                    Button(
                        onClick = {
                            showAddSlotDialog = false
                            showToolDialog = true
                        },
                        modifier = Modifier.fillMaxWidth().height(80.dp),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
                    ) {
                        Text(stringResource(R.string.action_choose_tool), color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
        val isSystemDefault = itemToEdit!!.packageName == "com.android.dialer" || itemToEdit!!.packageName == "com.google.android.dialer" || itemToEdit!!.packageName == "com.android.messaging" || itemToEdit!!.packageName == "com.google.android.apps.messaging"
        ButtonEditorSheet(
            item = itemToEdit!!,
            canDelete = !isSystemDefault,
            onSave = { label, color, image ->
                scope.launch {
                    viewModel.repository.updateItem(itemToEdit!!.copy(customLabel = label, customColor = color, customImageUri = image))
                }
                showEditSheet = false
            },
            onDelete = {
                scope.launch {
                    viewModel.clearSlot(itemToEdit!!)
                }
                showEditSheet = false
            },
            onDismiss = { showEditSheet = false }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        StatusBarWidget(
            onBatteryTenClicks = {
                if (isProtectionEnabled) {
                    navDestination = "settings"
                    showPinDialogForNav = true
                } else {
                    navController.navigate("settings")
                }
            }
        )
        
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
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items.size) { index ->
                            val item = items[index]
                            Box(
                                modifier = Modifier
                            ) {
                                GridItem(item, 
                                    isHomeFavLockEnabled = isHomeFavLockEnabled,
                                badgeCount = when {
                                    item.packageName == "com.android.dialer" || item.packageName == "com.google.android.dialer" -> missedCalls
                                    item.packageName == "com.android.messaging" || item.packageName == "com.google.android.apps.messaging" -> unreadSms
                                    else -> 0
                                },
                                onClick = {
                                    if (isProtectionEnabled && showPinDialogForNav) return@GridItem
                                    if (item.itemType == ItemType.EMPTY) {
                                         if (!isProtectionEnabled && !isHomeFavLockEnabled) {
                                             slotToAssign = item
                                             showAddSlotDialog = true
                                         }
                                    } else if (item.itemType == ItemType.CONTACT) {
                                        val pkg = item.packageName ?: ""
                                        if (pkg.startsWith("whatsapp_audio:") || pkg.startsWith("whatsapp_video:")) {
                                            val number = pkg.substringAfter(":")
                                            val isVideo = pkg.startsWith("whatsapp_video")
                                            val mimeString = if (isVideo) "vnd.android.cursor.item/vnd.com.whatsapp.video.call" else "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
                                            
                                            // Make sure the number is clean for wa.me fallback
                                            val cleanNumber = number.replace(Regex("[^0-9+]"), "")
                                            
                                            try {
                                                var callUri: android.net.Uri? = null
                                                val resolver = context.contentResolver
                                                val cursor = resolver.query(
                                                    android.provider.ContactsContract.Data.CONTENT_URI,
                                                    arrayOf(android.provider.ContactsContract.Data._ID),
                                                    "${android.provider.ContactsContract.Data.MIMETYPE} = ? AND ${android.provider.ContactsContract.Data.DATA1} LIKE ?",
                                                    arrayOf(mimeString, "%${cleanNumber.takeLast(10)}%"),
                                                    null
                                                )
                                                if (cursor != null && cursor.moveToFirst()) {
                                                    val id = cursor.getLong(0)
                                                    callUri = android.content.ContentUris.withAppendedId(android.provider.ContactsContract.Data.CONTENT_URI, id)
                                                    cursor.close()
                                                }
                                                
                                                if (callUri != null) {
                                                    val intent = Intent(Intent.ACTION_VIEW)
                                                    intent.setDataAndType(callUri, mimeString)
                                                    intent.setPackage("com.whatsapp")
                                                    context.startActivity(intent)
                                                } else {
                                                    // Fallback to chat if we can't directly call
                                                    val chatIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber"))
                                                    context.startActivity(chatIntent)
                                                }
                                            } catch (e: Exception) {
                                                // Ultimate fallback if WhatsApp is not installed or error
                                                try {
                                                    val chatIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://api.whatsapp.com/send?phone=$cleanNumber"))
                                                    context.startActivity(chatIntent)
                                                } catch (e2: Exception) {
                                                    // Do nothing
                                                }
                                            }
                                        } else if (pkg.startsWith("tool_")) {
                                            when (pkg) {
                                                "tool_magnifier" -> navController.navigate("magnifier")
                                                "tool_siren" -> navController.navigate("siren")
                                                "tool_reminders" -> navController.navigate("reminders")
                                                "tool_flashlight" -> ToolManager.toggleFlashlight(context)
                                                "tool_wifi" -> {
                                                    val intent = Intent(android.provider.Settings.ACTION_WIFI_SETTINGS)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    context.startActivity(intent)
                                                }
                                                "tool_bluetooth" -> {
                                                    val intent = Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    context.startActivity(intent)
                                                }
                                            }
                                        } else {
                                            try {
                                                val intent = Intent(Intent.ACTION_CALL).apply { data = android.net.Uri.parse("tel:$pkg") }
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val intent = Intent(Intent.ACTION_DIAL).apply { data = android.net.Uri.parse("tel:$pkg") }
                                                context.startActivity(intent)
                                            }
                                        }
                                    } else {
                                        item.packageName?.let { pkg ->
                                            when {
                                                pkg == "com.android.dialer" || pkg == "com.google.android.dialer" -> {
                                                    NotificationTracker.resetMissedCalls(context)
                                                    navController.navigate("dialer")
                                                }
                                                pkg == "com.android.messaging" || pkg == "com.google.android.apps.messaging" -> {
                                                    NotificationTracker.resetUnreadSms(context)
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
                                    if (isHomeFavLockEnabled) return@GridItem // Lock Home Screen and Favorites
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
                Box(modifier = Modifier.weight(1.5f)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                        BigFooterButton(
                            text = "SOS",
                            icon = Icons.Default.Call,
                            containerColor = Color.Red,
                            contentColor = Color.White,
                            isTtsEnabled = false,
                            onClick = {
                                val sosContact = sharedPref.getString("sos_number", "")
                                if (sosContact.isNullOrEmpty()) {
                                    showSosWarningDialog = true
                                } else {
                                    EmergencyManager.triggerSos(context)
                                }
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                        BigFooterButton(
                            text = stringResource(R.string.medical_id_short),
                            icon = Icons.Default.MedicalInformation,
                            containerColor = Color.White,
                            contentColor = Color.Red,
                            isTtsEnabled = false,
                            onClick = {
                                navController.navigate("medical_id")
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
                if (!isHideSettingsEnabled) {
                    Box(modifier = Modifier.weight(1f)) {
                        BigFooterButton(
                            text = stringResource(R.string.btn_settings),
                            icon = Icons.Default.Settings,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            isTtsEnabled = false,
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
                }
                Box(modifier = Modifier.weight(1f)) {
                    BigFooterButton(
                        text = stringResource(R.string.tools_title),
                        icon = Icons.Default.Build,
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White,
                        isTtsEnabled = false,
                        onClick = {
                            navController.navigate("tools")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GridItem(item: LauncherItem, isHomeFavLockEnabled: Boolean, badgeCount: Int = 0, onClick: () -> Unit, onLongClick: () -> Unit) {
    val isPhone = item.packageName == "com.android.dialer" || item.packageName == "com.google.android.dialer"
    val isSms = item.packageName == "com.android.messaging" || item.packageName == "com.google.android.apps.messaging"
    val isTool = item.packageName?.startsWith("tool_") == true
    val isWhatsApp = item.packageName?.startsWith("whatsapp_") == true
    
    val toolIcon = when(item.packageName) {
        "tool_magnifier" -> Icons.Default.Search
        "tool_siren" -> Icons.Default.Warning
        "tool_reminders" -> Icons.Default.DateRange
        "tool_flashlight" -> Icons.Default.FlashlightOn
        "tool_wifi" -> Icons.Default.Wifi
        "tool_bluetooth" -> Icons.Default.Bluetooth
        else -> Icons.Default.Build
    }
    
    val waColor = if (item.packageName?.contains("video") == true) Color(0xFF128C7E) else Color(0xFF25D366)

    val toolName = when(item.packageName) {
        "tool_magnifier" -> stringResource(R.string.magnifier)
        "tool_siren" -> stringResource(R.string.panic_siren)
        "tool_reminders" -> stringResource(R.string.reminders_title)
        "tool_flashlight" -> stringResource(R.string.flashlight_on)
        "tool_wifi" -> stringResource(R.string.wifi_settings)
        "tool_bluetooth" -> stringResource(R.string.bluetooth_settings)
        else -> item.label ?: ""
    }

    BigButton(
        text = when {
            item.itemType == ItemType.EMPTY && isHomeFavLockEnabled -> ""
            item.itemType == ItemType.EMPTY -> stringResource(R.string.add_btn)
            isPhone -> item.customLabel ?: stringResource(R.string.btn_phone)
            isSms -> item.customLabel ?: stringResource(R.string.btn_messages)
            isTool -> item.customLabel ?: toolName
            else -> item.customLabel ?: item.label ?: stringResource(R.string.app_placeholder)
        },
        icon = when {
            item.itemType == ItemType.EMPTY -> Icons.Default.Add
            isPhone -> Icons.Default.Phone
            isSms -> Icons.Default.Mail
            isTool -> toolIcon
            isWhatsApp -> Icons.Default.Person
            else -> Icons.Default.Apps
        },
        backgroundColor = when {
            item.itemType == ItemType.EMPTY && isHomeFavLockEnabled -> Color.Transparent
            item.itemType == ItemType.EMPTY -> MaterialTheme.colorScheme.surfaceVariant
            isPhone -> Color(0xFF388E3C)
            isSms -> Color.Blue
            isTool -> Color.DarkGray
            isWhatsApp -> waColor
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (item.itemType == ItemType.EMPTY && isHomeFavLockEnabled) Color.Transparent else if (item.itemType == ItemType.EMPTY) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
        badgeCount = badgeCount,
        customColor = item.customColor,
        customImageUri = item.customImageUri,
        isTtsEnabled = false,
        appIconPackageName = if (item.itemType == ItemType.APP && !isPhone && !isSms && !isTool) item.packageName else null,
        isContact = item.itemType == ItemType.CONTACT && !isTool && !isWhatsApp,
        onClick = onClick,
        onLongClick = onLongClick
    )
}
