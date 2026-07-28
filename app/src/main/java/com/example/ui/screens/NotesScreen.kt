package com.example.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Note
import java.text.SimpleDateFormat
import java.util.*

enum class NoteSortOption(val label: String) {
    UPDATED("Son Güncelleme"),
    TITLE("Başlık (A-Z)"),
    CATEGORY("Kategori")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(
    notes: List<Note>,
    onAddNote: (title: String, content: String, category: String, isPinned: Boolean) -> Unit,
    onEditNote: (Note) -> Unit,
    onTogglePin: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var noteToEdit by remember { mutableStateOf<Note?>(null) }
    var noteForDetail by remember { mutableStateOf<Note?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Tümü") }
    var isGridView by remember { mutableStateOf(false) }
    var currentSort by remember { mutableStateOf(NoteSortOption.UPDATED) }
    var showSortMenu by remember { mutableStateOf(false) }

    val categories = listOf("Tümü", "Fikirler", "Projeler", "Günlük", "Kişisel", "Genel")
    val context = LocalContext.current

    // Filtering & Sorting logic
    val filteredNotes = remember(notes, searchQuery, selectedCategory, currentSort) {
        notes.filter { note ->
            val matchesSearch = searchQuery.isEmpty() ||
                    note.title.contains(searchQuery, ignoreCase = true) ||
                    note.content.contains(searchQuery, ignoreCase = true) ||
                    note.category.contains(searchQuery, ignoreCase = true)
            val matchesCategory = if (selectedCategory == "Tümü") true else note.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesCategory
        }.sortedWith { a, b ->
            when (currentSort) {
                NoteSortOption.UPDATED -> b.updatedAt.compareTo(a.updatedAt)
                NoteSortOption.TITLE -> a.title.lowercase().compareTo(b.title.lowercase())
                NoteSortOption.CATEGORY -> a.category.compareTo(b.category)
            }
        }
    }

    val pinnedNotes = remember(filteredNotes) { filteredNotes.filter { it.isPinned } }
    val otherNotes = remember(filteredNotes) { filteredNotes.filter { !it.isPinned } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.testTag("add_note_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Not Ekle")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("notes_screen")
        ) {
            // Stats Header
            NotesHeaderStats(
                totalCount = notes.size,
                pinnedCount = notes.count { it.isPinned },
                filteredCount = filteredNotes.size
            )

            // Search Bar & View Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("notes_search_input"),
                    placeholder = { Text("Notlarda ara...") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Temizle")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Sort Button & Menu
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier.testTag("sort_notes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sort,
                            contentDescription = "Sırala",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        NoteSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = option.label,
                                        fontWeight = if (currentSort == option) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    currentSort = option
                                    showSortMenu = false
                                },
                                leadingIcon = if (currentSort == option) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null
                            )
                        }
                    }
                }

                // Grid / List Toggle
                IconButton(
                    onClick = { isGridView = !isGridView },
                    modifier = Modifier.testTag("toggle_view_button")
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.GridView,
                        contentDescription = "Görünümü değiştir",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        modifier = Modifier.testTag("category_chip_$category")
                    )
                }
            }

            if (filteredNotes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.StickyNote2,
                            contentDescription = null,
                            modifier = Modifier.size(56.dp),
                            tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Not bulunamadı",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Fikirlerinizi kaydetmek için + butonuna dokunun.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredNotes, key = { "grid_note_${it.id}" }) { note ->
                            NoteCard(
                                note = note,
                                isCompact = true,
                                onClick = { noteForDetail = note },
                                onTogglePin = { onTogglePin(note) },
                                onDelete = { onDeleteNote(note) }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (pinnedNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = "📌 İğnelenenler (${pinnedNotes.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(pinnedNotes, key = { "note_${it.id}" }) { note ->
                                NoteCard(
                                    note = note,
                                    isCompact = false,
                                    onClick = { noteForDetail = note },
                                    onTogglePin = { onTogglePin(note) },
                                    onDelete = { onDeleteNote(note) }
                                )
                            }
                        }

                        if (otherNotes.isNotEmpty()) {
                            item {
                                Text(
                                    text = if (pinnedNotes.isNotEmpty()) "Diğer Notlar (${otherNotes.size})" else "Tüm Notlar (${otherNotes.size})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                            items(otherNotes, key = { "note_${it.id}" }) { note ->
                                NoteCard(
                                    note = note,
                                    isCompact = false,
                                    onClick = { noteForDetail = note },
                                    onTogglePin = { onTogglePin(note) },
                                    onDelete = { onDeleteNote(note) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        NoteFormDialog(
            titleText = "Yeni Not Oluştur",
            confirmText = "Notu Kaydet",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, content, category, isPinned ->
                onAddNote(title, content, category, isPinned)
                showAddDialog = false
            }
        )
    }

    noteForDetail?.let { note ->
        NoteDetailDialog(
            note = note,
            onDismiss = { noteForDetail = null },
            onEdit = {
                noteToEdit = note
                noteForDetail = null
            },
            onDelete = {
                onDeleteNote(note)
                noteForDetail = null
            },
            onShare = {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TITLE, note.title)
                    putExtra(Intent.EXTRA_TEXT, "${note.title}\n\n${note.content}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, "Notu Paylaş")
                context.startActivity(shareIntent)
            }
        )
    }

    noteToEdit?.let { note ->
        NoteFormDialog(
            initialTitle = note.title,
            initialContent = note.content,
            initialCategory = note.category,
            initialIsPinned = note.isPinned,
            titleText = "Notu Düzenle",
            confirmText = "Güncelle",
            onDismiss = { noteToEdit = null },
            onConfirm = { title, content, category, isPinned ->
                onEditNote(note.copy(title = title, content = content, category = category, isPinned = isPinned))
                noteToEdit = null
            }
        )
    }
}

@Composable
fun NotesHeaderStats(
    totalCount: Int,
    pinnedCount: Int,
    filteredCount: Int
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Not Defterim",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = "Toplam $totalCount not ($pinnedCount iğnelenmiş)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
            ) {
                Text(
                    text = "$filteredCount Gösteriliyor",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun NoteCard(
    note: Note,
    isCompact: Boolean = false,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d MMM, HH:mm", Locale("tr", "TR"))
    val formattedDate = dateFormat.format(Date(note.updatedAt))
    val categoryColor = getCategoryColor(note.category)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (note.isPinned) 2.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = categoryColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = note.category,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = categoryColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("pin_note_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "İğnele",
                            tint = if (note.isPinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("delete_note_${note.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = if (isCompact) 2 else 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = if (isCompact) 3 else 5,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                val wordCount = if (note.content.isBlank()) 0 else note.content.trim().split("\\s+".toRegex()).size
                Text(
                    text = "$wordCount kelime",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun NoteDetailDialog(
    note: Note,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit
) {
    val dateFormat = SimpleDateFormat("d MMMM yyyy, HH:mm", Locale("tr", "TR"))
    val formattedDate = dateFormat.format(Date(note.updatedAt))
    val categoryColor = getCategoryColor(note.category)
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val wordCount = if (note.content.isBlank()) 0 else note.content.trim().split("\\s+".toRegex()).size
    val charCount = note.content.length

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = note.category,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = categoryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    if (note.isPinned) {
                        Text("📌 İğnelenmiş", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tarih: $formattedDate",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$wordCount kelime, $charCount karakter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onShare) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Paylaş", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = {
                    val textToCopy = "${note.title}\n\n${note.content}"
                    clipboardManager.setText(AnnotatedString(textToCopy))
                    Toast.makeText(context, "Not kopyalandı", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Kopyala", tint = MaterialTheme.colorScheme.primary)
                }
                Button(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Düzenle")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        }
    )
}

@Composable
fun NoteFormDialog(
    initialTitle: String = "",
    initialContent: String = "",
    initialCategory: String = "Fikirler",
    initialIsPinned: Boolean = false,
    titleText: String = "Not Oluştur",
    confirmText: String = "Notu Kaydet",
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, category: String, isPinned: Boolean) -> Unit
) {
    var noteTitle by remember { mutableStateOf(initialTitle) }
    var noteContent by remember { mutableStateOf(initialContent) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var isPinned by remember { mutableStateOf(initialIsPinned) }

    val categories = listOf("Fikirler", "Projeler", "Günlük", "Kişisel", "Genel")

    val charCount = noteContent.length
    val wordCount = if (noteContent.isBlank()) 0 else noteContent.trim().split("\\s+".toRegex()).size

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Not Başlığı") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_note_title_input")
                )

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Not İçeriği") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_note_content_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "$wordCount kelime | $charCount karakter",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text("Kategori", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Üste İğnele", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = isPinned,
                        onCheckedChange = { isPinned = it },
                        modifier = Modifier.testTag("pin_note_switch")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                        onConfirm(noteTitle, noteContent, selectedCategory, isPinned)
                    }
                },
                modifier = Modifier.testTag("save_note_button")
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "fikirler", "ideas" -> Color(0xFF8E24AA) // Purple
        "projeler", "projects" -> Color(0xFF1976D2) // Blue
        "günlük", "journal" -> Color(0xFF00897B) // Teal
        "kişisel", "personal" -> Color(0xFF43A047) // Green
        else -> Color(0xFFE65100) // Dark Orange
    }
}

