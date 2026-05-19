package com.prusoft.easybiglauncher.ui.screens

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.prusoft.easybiglauncher.R
import com.prusoft.easybiglauncher.utils.FavoritesUtils
import com.prusoft.easybiglauncher.utils.FavoriteContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ContactInfo(
    val id: String,
    val name: String,
    val number: String,
    val photoUri: String?
)

@Composable
fun BigContactsScreen() {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<ContactInfo>>(emptyList()) }
    var searchQuery by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    var favoriteContactToAdd by remember { mutableStateOf<ContactInfo?>(null) }

    LaunchedEffect(Unit) {
        contacts = withContext(Dispatchers.IO) {
            fetchContacts(context.contentResolver)
        }
        isLoading = false
    }

    val filteredContacts = remember(contacts, searchQuery) {
        if (searchQuery.isEmpty()) contacts
        else contacts.filter { it.name.contains(searchQuery, ignoreCase = true) || it.number.contains(searchQuery) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text(stringResource(R.string.search_contacts_hint), color = Color.LightGray, fontSize = 20.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.White) },
            singleLine = true,
            textStyle = androidx.compose.ui.text.TextStyle(color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.DarkGray,
                unfocusedContainerColor = Color.DarkGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredContacts) { contact ->
                    ContactRow(contact = contact, onClick = {
                        val intent = Intent(Intent.ACTION_CALL).apply {
                            data = Uri.parse("tel:${contact.number}")
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${contact.number}")
                            }
                            context.startActivity(dialIntent)
                        }
                    }, onLongClick = {
                        favoriteContactToAdd = contact
                    })
                }
            }
        }
    }

    if (favoriteContactToAdd != null) {
        com.prusoft.easybiglauncher.ui.components.BigConfirmDialog(
            title = stringResource(R.string.add_to_favorites),
            message = stringResource(R.string.add_to_favorites_desc, favoriteContactToAdd!!.name),
            onConfirm = {
                favoriteContactToAdd?.let {
                    FavoritesUtils.addFavorite(context, it.name, it.number)
                }
                favoriteContactToAdd = null
            },
            onCancel = { favoriteContactToAdd = null }
        )
    }
}

@Composable
fun ContactRow(contact: ContactInfo, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongClick() },
                    onTap = { onClick() }
                )
            },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Photo or Initial
            if (contact.photoUri != null) {
                AsyncImage(
                    model = contact.photoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = contact.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = contact.name,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = contact.number,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun fetchContacts(contentResolver: ContentResolver): List<ContactInfo> {
    val contactList = mutableListOf<ContactInfo>()
    val cursor = contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val photoIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_THUMBNAIL_URI)
        val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)

        while (it.moveToNext()) {
            val name = it.getString(nameIndex)
            val number = it.getString(numberIndex)
            val photoUri = it.getString(photoIndex)
            val id = it.getString(idIndex)
            contactList.add(ContactInfo(id, name, number, photoUri))
        }
    }
    return contactList.distinctBy { it.number }
}
