package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.*
import com.example.data.AppDatabase
import com.example.data.HubRepository
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.*
import com.example.ui.theme.MyHubTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        val db = AppDatabase.getDatabase(applicationContext)
        val repository = HubRepository(db.taskDao(), db.habitDao(), db.noteDao())
        MainViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyHubTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Overview", Icons.Default.Dashboard)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.TaskAlt)
    object Habits : Screen("habits", "Habits", Icons.Default.LocalFireDepartment)
    object Notes : Screen("notes", "Notes", Icons.Default.StickyNote2)
    object Analytics : Screen("analytics", "Analytics", Icons.Default.Insights)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    val screens = listOf(
        Screen.Home,
        Screen.Tasks,
        Screen.Habits,
        Screen.Notes,
        Screen.Analytics
    )

    val currentScreen = screens.find { it.route == currentRoute } ?: Screen.Home

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentScreen.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                screens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            if (currentRoute != screen.route) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = {
                            Icon(imageVector = screen.icon, contentDescription = screen.title)
                        },
                        label = {
                            Text(text = screen.title, style = MaterialTheme.typography.labelSmall)
                        },
                        modifier = Modifier.testTag("nav_item_${screen.route}")
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    tasks = tasks,
                    habits = habits,
                    onToggleTask = { viewModel.toggleTaskCompleted(it) },
                    onToggleHabit = { viewModel.toggleHabitCheckIn(it) },
                    onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                    onNavigateToHabits = { navController.navigate(Screen.Habits.route) },
                    onNavigateToNotes = { navController.navigate(Screen.Notes.route) }
                )
            }
            composable(Screen.Tasks.route) {
                TasksScreen(
                    tasks = tasks,
                    onAddTask = { title, category, priority -> viewModel.addTask(title, category, priority) },
                    onToggleTask = { viewModel.toggleTaskCompleted(it) },
                    onDeleteTask = { viewModel.deleteTask(it) }
                )
            }
            composable(Screen.Habits.route) {
                HabitsScreen(
                    habits = habits,
                    onAddHabit = { title, category -> viewModel.addHabit(title, category) },
                    onToggleCheckIn = { viewModel.toggleHabitCheckIn(it) },
                    onDeleteHabit = { viewModel.deleteHabit(it) }
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    notes = notes,
                    onAddNote = { title, content, category, isPinned -> viewModel.addNote(title, content, category, isPinned) },
                    onTogglePin = { viewModel.toggleNotePinned(it) },
                    onDeleteNote = { viewModel.deleteNote(it) }
                )
            }
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    tasks = tasks,
                    habits = habits,
                    notes = notes
                )
            }
        }
    }
}
