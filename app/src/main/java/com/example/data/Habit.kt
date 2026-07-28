package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String = "Health",
    val streakCount: Int = 0,
    val completedToday: Boolean = false,
    val targetDaysPerWeek: Int = 7,
    val lastCompletedDate: Long = 0L
)
