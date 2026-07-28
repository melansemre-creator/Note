package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Habit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    habits: List<Habit>,
    onAddHabit: (title: String, category: String) -> Unit,
    onEditHabit: (Habit) -> Unit,
    onToggleCheckIn: (Habit) -> Unit,
    onDeleteHabit: (Habit) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var habitToEdit by remember { mutableStateOf<Habit?>(null) }
    var selectedCategory by remember { mutableStateOf("Tümü") }

    val categories = listOf("Tümü", "Sağlık", "Zihin", "Verimlilik", "Spor", "Genel")
    val filteredHabits = remember(habits, selectedCategory) {
        habits.filter { habit ->
            if (selectedCategory == "Tümü") true else habit.category.equals(selectedCategory, ignoreCase = true)
        }
    }

    val completedTodayCount = remember(habits) { habits.count { it.completedToday } }
    val maxStreak = remember(habits) { habits.maxOfOrNull { it.streakCount } ?: 0 }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("add_habit_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Alışkanlık Ekle")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("habits_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🔥 $maxStreak gün",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "En İyi Seri",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Divider(
                            modifier = Modifier
                                .height(32.dp)
                                .width(1.dp)
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$completedTodayCount/${habits.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Bugün Tamamlanan",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Category Filter
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Günlük Alışkanlık Takibi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredHabits.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = null,
                                modifier = Modifier.size(56.dp),
                                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                            )
                            Text(
                                text = "Henüz alışkanlık oluşturulmadı",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Düzenli alışkanlıklar ekleyerek verimliliğinizi artırın.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredHabits, key = { "habit_${it.id}" }) { habit ->
                    HabitCard(
                        habit = habit,
                        onToggle = { onToggleCheckIn(habit) },
                        onEdit = { habitToEdit = habit },
                        onDelete = { onDeleteHabit(habit) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        HabitFormDialog(
            titleText = "Yeni Alışkanlık Ekle",
            confirmText = "Alışkanlığı Kaydet",
            onDismiss = { showAddDialog = false },
            onConfirm = { title, category ->
                onAddHabit(title, category)
                showAddDialog = false
            }
        )
    }

    habitToEdit?.let { habit ->
        HabitFormDialog(
            initialTitle = habit.title,
            initialCategory = habit.category,
            titleText = "Alışkanlığı Düzenle",
            confirmText = "Güncelle",
            onDismiss = { habitToEdit = null },
            onConfirm = { title, category ->
                onEditHabit(habit.copy(title = title, category = category))
                habitToEdit = null
            }
        )
    }
}

@Composable
fun HabitCard(
    habit: Habit,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (habit.completedToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onToggle,
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            if (habit.completedToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            CircleShape
                        )
                        .testTag("habit_toggle_${habit.id}")
                ) {
                    Icon(
                        imageVector = if (habit.completedToday) Icons.Default.Check else Icons.Default.LocalFireDepartment,
                        contentDescription = "Alışkanlık işaretle",
                        tint = if (habit.completedToday) Color.White else MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        text = habit.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
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
                                text = habit.category,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "🔥 ${habit.streakCount} günlük seri",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 7-Day Visual Week Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val weekDays = listOf("P", "S", "Ç", "P", "C", "C", "P")
                        val completedDaysCount = minOf(habit.streakCount, 7)
                        weekDays.forEachIndexed { index, day ->
                            val isDone = index >= (7 - completedDaysCount) || (index == 6 && habit.completedToday)
                            Surface(
                                shape = CircleShape,
                                color = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = day,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (isDone) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_habit_${habit.id}")) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Alışkanlığı düzenle",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_habit_${habit.id}")) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Alışkanlığı sil",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun HabitFormDialog(
    initialTitle: String = "",
    initialCategory: String = "Sağlık",
    titleText: String = "Yeni Alışkanlık Ekle",
    confirmText: String = "Alışkanlığı Kaydet",
    onDismiss: () -> Unit,
    onConfirm: (title: String, category: String) -> Unit
) {
    var habitTitle by remember { mutableStateOf(initialTitle) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    val categories = listOf("Sağlık", "Zihin", "Verimlilik", "Spor", "Genel")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titleText) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = habitTitle,
                    onValueChange = { habitTitle = it },
                    label = { Text("Alışkanlık Adı") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_habit_title_input")
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (habitTitle.isNotBlank()) {
                        onConfirm(habitTitle, selectedCategory)
                    }
                },
                modifier = Modifier.testTag("save_habit_button")
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
