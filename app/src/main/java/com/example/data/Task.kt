package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "General",
    val priority: String = "Medium",
    val isCompleted: Boolean = false,
    val dueDate: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis()
)
