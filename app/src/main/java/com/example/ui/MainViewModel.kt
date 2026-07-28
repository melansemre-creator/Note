package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: HubRepository) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.tasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habits: StateFlow<List<Habit>> = repository.habits
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notes: StateFlow<List<Note>> = repository.notes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            tasks.first().let { currentTasks ->
                if (currentTasks.isEmpty()) {
                    repository.insertTask(Task(title = "Welcome to My Hub!", category = "Personal", priority = "High"))
                    repository.insertTask(Task(title = "Organize workspace", category = "Work", priority = "Medium"))
                    repository.insertTask(Task(title = "Read 15 minutes", category = "Learning", priority = "Low"))
                }
            }
            habits.first().let { currentHabits ->
                if (currentHabits.isEmpty()) {
                    repository.insertHabit(Habit(title = "Morning Stretch & Mindfulness", category = "Health", streakCount = 3, completedToday = false))
                    repository.insertHabit(Habit(title = "Drink 2L Water", category = "Health", streakCount = 5, completedToday = true))
                    repository.insertHabit(Habit(title = "Write Daily Note", category = "Mindset", streakCount = 2, completedToday = false))
                }
            }
            notes.first().let { currentNotes ->
                if (currentNotes.isEmpty()) {
                    repository.insertNote(Note(title = "🚀 Welcome Note", content = "My Hub is your personal space to stay organized, track habits, and capture ideas in one sleek app.", category = "Ideas", isPinned = true))
                    repository.insertNote(Note(title = "💡 App Ideas", content = "1. Clean UI layout\n2. Reactive Room storage\n3. Habit streak visualizer", category = "Projects", isPinned = false))
                }
            }
        }
    }

    fun addTask(title: String, category: String, priority: String) {
        viewModelScope.launch {
            repository.insertTask(Task(title = title, category = category, priority = priority))
        }
    }

    fun toggleTaskCompleted(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task.copy(isCompleted = !task.isCompleted))
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addHabit(title: String, category: String) {
        viewModelScope.launch {
            repository.insertHabit(Habit(title = title, category = category))
        }
    }

    fun toggleHabitCheckIn(habit: Habit) {
        viewModelScope.launch {
            val isNowCompleted = !habit.completedToday
            val newStreak = if (isNowCompleted) habit.streakCount + 1 else maxOf(0, habit.streakCount - 1)
            repository.updateHabit(
                habit.copy(
                    completedToday = isNowCompleted,
                    streakCount = newStreak,
                    lastCompletedDate = if (isNowCompleted) System.currentTimeMillis() else habit.lastCompletedDate
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun addNote(title: String, content: String, category: String, isPinned: Boolean) {
        viewModelScope.launch {
            repository.insertNote(Note(title = title, content = content, category = category, isPinned = isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun toggleNotePinned(note: Note) {
        viewModelScope.launch {
            repository.updateNote(note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis()))
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            repository.deleteNote(note)
        }
    }
}

class MainViewModelFactory(private val repository: HubRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
