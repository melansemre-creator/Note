package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Task

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    onAddTask: (title: String, category: String, priority: String) -> Unit,
    onEditTask: (Task) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onClearCompleted: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Tümü") }
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val categories = listOf("Tümü", "Kişisel", "İş", "Öğrenme", "Sağlık", "Genel")
    val filteredTasks = remember(tasks, searchQuery, selectedFilter, selectedCategory) {
        tasks.filter { task ->
            val matchesSearch = searchQuery.isBlank() || task.title.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                "Bekleyenler" -> !task.isCompleted
                "Tamamlananlar" -> task.isCompleted
                else -> true
            }
            val matchesCategory = if (selectedCategory == "Tümü") true else task.category.equals(selectedCategory, ignoreCase = true)
            matchesSearch && matchesFilter && matchesCategory
        }
    }

    val completedCount = remember(tasks) { tasks.count { it.isCompleted } }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_task_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Görev Ekle")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("tasks_screen")
        ) {
            // Search field
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("tasks_search_input"),
                placeholder = { Text("Görevlerde ara...") },
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

            // Filter Tabs & Clear Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Tümü", "Bekleyenler", "Tamamlananlar").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            modifier = Modifier.testTag("filter_chip_$filter")
                        )
                    }
                }

                if (completedCount > 0) {
                    TextButton(onClick = onClearCompleted) {
                        Text("Tamamlananları Sil", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Category Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        leadingIcon = {
                            if (selectedCategory == category) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }
            }

            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Task,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = "Görev bulunamadı",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Yeni bir görev eklemek için + butonuna dokunun.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredTasks, key = { "task_${it.id}" }) { task ->
                        TaskCard(
                            task = task,
                            onToggle = { onToggleTask(task) },
                            onEdit = { taskToEdit = task },
                            onDelete = { onDeleteTask(task) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        TaskFormDialog(
            title = "Yeni Görev Oluştur",
            confirmText = "Görev Ekle",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category, priority ->
                onAddTask(title, category, priority)
                showAddDialog = false
            }
        )
    }

    taskToEdit?.let { task ->
        TaskFormDialog(
            initialTitle = task.title,
            initialCategory = task.category,
            initialPriority = task.priority,
            title = "Görevi Düzenle",
            confirmText = "Kaydet",
            onDismiss = { taskToEdit = null },
            onConfirm = { title, category, priority ->
                onEditTask(task.copy(title = title, category = category, priority = priority))
                taskToEdit = null
            }
        )
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (task.priority.lowercase()) {
        "high", "yüksek" -> Color(0xFFFF5252)
        "medium", "orta" -> Color(0xFFFFAB40)
        else -> Color(0xFF69F0AE)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("task_card_checkbox_${task.id}")
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = task.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "${task.priority} Öncelik",
                            style = MaterialTheme.typography.labelSmall,
                            color = priorityColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_task_${task.id}")) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Görevi düzenle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_task_${task.id}")) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Görevi sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun TaskFormDialog(
    initialTitle: String = "",
    initialCategory: String = "Kişisel",
    initialPriority: String = "Orta",
    title: String = "Yeni Görev Oluştur",
    confirmText: String = "Görev Ekle",
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String, priority: String) -> Unit
) {
    var taskTitle by remember { mutableStateOf(initialTitle) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedPriority by remember { mutableStateOf(initialPriority) }

    val categories = listOf("Kişisel", "İş", "Öğrenme", "Sağlık", "Genel")
    val priorities = listOf("Düşük", "Orta", "Yüksek")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Görev Başlığı") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_task_title_input")
                )

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

                Text("Öncelik", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    priorities.forEach { priority ->
                        FilterChip(
                            selected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            label = { Text(priority) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (taskTitle.isNotBlank()) {
                        onConfirm(taskTitle, selectedCategory, selectedPriority)
                    }
                },
                modifier = Modifier.testTag("save_task_button")
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
