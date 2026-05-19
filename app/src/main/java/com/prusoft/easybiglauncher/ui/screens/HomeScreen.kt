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
    val isTtsEnabled by viewModel.securityRepository.isTtsEnabled.collectAsState(initial = false)
    val isHomeFavLockEnabled by viewModel.securityRepository.isHomeFavLockEnabled.collectAsState(initial = false)
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
            title = { Text("Eylem Seçin", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            text = { Text("Bu kişi için kısayol eylemi ne olsun?", fontSize = 20.sp) },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val (name, number, slot) = contactToConfigure!!
                    Button(onClick = { viewModel.assignContactToItem(slot, name, number); contactToConfigure = null }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text("Telefon ile Arama", fontSize = 20.sp) }
                    Button(onClick = { viewModel.assignContactToItem(slot, name, "whatsapp_audio:$number"); contactToConfigure = null }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366))) { Text("WhatsApp Sesli", fontSize = 20.sp) }
                    Button(onClick = { viewModel.assignContactToItem(slot, name, "whatsapp_video:$number"); contactToConfigure = null }, modifier = Modifier.fillMaxWidth().height(60.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF128C7E))) { Text("WhatsApp Görüntülü", fontSize = 20.sp) }
                }
            },
            dismissButton = {
                TextButton(onClick = { contactToConfigure = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showToolDialog && slotToAssign != null) {
        AlertDialog(
            onDismissRequest = { showToolDialog = false },
            title = { Text("Araç Seç", fontSize = 24.sp, fontWeight = FontWeight.Bold) },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val magnifierName = stringResource(R.string.magnifier)
                    val sirenName = stringResource(R.string.panic_siren)
                    val aiName = stringResource(R.string.ai_assistant)
                    Button(onClick = { viewModel.assignContactToItem(slotToAssign!!, magnifierName, "tool_magnifier"); showToolDialog = false; slotToAssign = null }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text(stringResource(R.string.magnifier), fontSize = 20.sp) }
                    Button(onClick = { viewModel.assignContactToItem(slotToAssign!!, sirenName, "tool_siren"); showToolDialog = false; slotToAssign = null }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text(stringResource(R.string.panic_siren), fontSize = 20.sp) }
                    Button(onClick = { viewModel.assignContactToItem(slotToAssign!!, aiName, "tool_ai"); showToolDialog = false; slotToAssign = null }, modifier = Modifier.fillMaxWidth().height(60.dp)) { Text(stringResource(R.string.ai_assistant), fontSize = 20.sp) }
                }
            },
            dismissButton = {
                TextButton(onClick = { showToolDialog = false }) { Text(stringResource(R.string.cancel)) }
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
                        Text("Araç Seç", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
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
        StatusBarWidget(isTtsEnabled = isTtsEnabled)
        
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
                                    isTtsEnabled = isTtsEnabled,
                                badgeCount = when {
                                    item.packageName == "com.android.dialer" || item.packageName == "com.google.android.dialer" -> missedCalls
                                    item.packageName == "com.android.messaging" || item.packageName == "com.google.android.apps.messaging" -> unreadSms
                                    else -> 0
                                },
                                onClick = {
                                    if (isProtectionEnabled && showPinDialogForNav) return@GridItem
                                    if (item.itemType == ItemType.EMPTY) {
                                         if (!isProtectionEnabled) {
                                             slotToAssign = item
                                             showAddSlotDialog = true
                                         }
                                    } else if (item.itemType == ItemType.CONTACT) {
                                        val pkg = item.packageName ?: ""
                                        if (pkg.startsWith("whatsapp_audio:") || pkg.startsWith("whatsapp_video:")) {
                                            try {
                                                val intent = Intent(Intent.ACTION_VIEW)
                                                intent.type = if (pkg.startsWith("whatsapp_video")) "vnd.android.cursor.item/vnd.com.whatsapp.video.call" else "vnd.android.cursor.item/vnd.com.whatsapp.voip.call"
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                val dialIntent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:" + pkg.substringAfter(":")))
                                                context.startActivity(dialIntent)
                                            }
                                        } else if (pkg.startsWith("tool_")) {
                                            when (pkg) {
                                                "tool_magnifier" -> navController.navigate("magnifier")
                                                "tool_siren" -> navController.navigate("siren")
                                                "tool_ai" -> {
                                                    try {
                                                        val intent = Intent(Intent.ACTION_VOICE_COMMAND).apply {
                                                            setPackage("com.google.android.apps.bard")
                                                            putExtra("android.intent.extra.START_VOICE_SESSION", true)
                                                            putExtra("android.intent.extra.ASSIST_INPUT_DEVICE_ID", 0)
                                                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                                                        }
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        val playStoreIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("market://details?id=com.google.android.apps.bard"))
                                                        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                        context.startActivity(playStoreIntent)
                                                    }
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
                            isTtsEnabled = isTtsEnabled,
                            onClick = {
                                val sosContact = sharedPref.getString("sos_number", "")
                                if (sosContact.isNullOrEmpty()) {
                                    navController.navigate("settings") // Should point to Emergency category, handled later
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
                            isTtsEnabled = isTtsEnabled,
                            onClick = {
                                navController.navigate("medical_id")
                            },
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
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
                            navController.navigate("tools")
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GridItem(item: LauncherItem, isTtsEnabled: Boolean, badgeCount: Int = 0, onClick: () -> Unit, onLongClick: () -> Unit) {
    val isPhone = item.packageName == "com.android.dialer" || item.packageName == "com.google.android.dialer"
    val isSms = item.packageName == "com.android.messaging" || item.packageName == "com.google.android.apps.messaging"
    val isTool = item.packageName?.startsWith("tool_") == true
    val isWhatsApp = item.packageName?.startsWith("whatsapp_") == true
    
    val toolIcon = when(item.packageName) {
        "tool_magnifier" -> Icons.Default.Search
        "tool_siren" -> Icons.Default.Warning
        "tool_ai" -> Icons.Default.Mic
        else -> Icons.Default.Build
    }
    
    val waColor = if (item.packageName?.contains("video") == true) Color(0xFF128C7E) else Color(0xFF25D366)

    BigButton(
        text = when {
            item.itemType == ItemType.EMPTY -> stringResource(R.string.add_btn)
            isPhone -> item.customLabel ?: stringResource(R.string.btn_phone)
            isSms -> item.customLabel ?: stringResource(R.string.btn_messages)
            isTool -> item.customLabel ?: item.label ?: ""
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
            item.itemType == ItemType.EMPTY -> MaterialTheme.colorScheme.surfaceVariant
            isPhone -> Color.Green
            isSms -> Color.Blue
            isTool -> Color.DarkGray
            isWhatsApp -> waColor
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (item.itemType == ItemType.EMPTY) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
        badgeCount = badgeCount,
        customColor = item.customColor,
        customImageUri = item.customImageUri,
        isTtsEnabled = isTtsEnabled,
        appIconPackageName = if (item.itemType == ItemType.APP && !isPhone && !isSms && !isTool) item.packageName else null,
        isContact = item.itemType == ItemType.CONTACT && !isTool && !isWhatsApp,
        onClick = onClick,
        onLongClick = onLongClick
    )
}
