package com.example.data

import kotlinx.coroutines.flow.Flow

class HubRepository(
    private val taskDao: TaskDao,
    private val habitDao: HabitDao,
    private val noteDao: NoteDao
) {
    val tasks: Flow<List<Task>> = taskDao.getAllTasks()
    val habits: Flow<List<Habit>> = habitDao.getAllHabits()
    val notes: Flow<List<Note>> = noteDao.getAllNotes()

    suspend fun insertTask(task: Task) = taskDao.insertTask(task)
    suspend fun updateTask(task: Task) = taskDao.updateTask(task)
    suspend fun deleteTask(task: Task) = taskDao.deleteTask(task)

    suspend fun insertHabit(habit: Habit) = habitDao.insertHabit(habit)
    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)
    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun insertNote(note: Note) = noteDao.insertNote(note)
    suspend fun updateNote(note: Note) = noteDao.updateNote(note)
    suspend fun deleteNote(note: Note) = noteDao.deleteNote(note)
}
