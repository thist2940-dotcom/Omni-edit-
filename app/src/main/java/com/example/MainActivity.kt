package com.example

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.AspectRatioType
import com.example.ui.components.NewProjectDialog
import com.example.ui.components.ProjectsDashboard
import com.example.ui.screens.EditorScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.StudioDarkBg
import com.example.viewmodel.EditorViewModel

enum class CurrentScreen {
    DASHBOARD,
    EDITOR
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val app = applicationContext as Application
                val viewModel: EditorViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return EditorViewModel(app) as T
                        }
                    }
                )

                var currentScreen by remember { mutableStateOf(CurrentScreen.EDITOR) }
                var showNewProjectDialog by remember { mutableStateOf(false) }
                val savedProjects by viewModel.savedProjects.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = StudioDarkBg
                ) {
                    when (currentScreen) {
                        CurrentScreen.DASHBOARD -> {
                            ProjectsDashboard(
                                savedProjects = savedProjects,
                                onOpenProject = { projectId ->
                                    viewModel.openProject(projectId)
                                    currentScreen = CurrentScreen.EDITOR
                                },
                                onOpenTemplate = { template ->
                                    viewModel.loadTemplate(template)
                                    currentScreen = CurrentScreen.EDITOR
                                },
                                onDeleteProject = { projectId ->
                                    viewModel.deleteProject(projectId)
                                },
                                onCreateNewClick = {
                                    showNewProjectDialog = true
                                },
                                onBackToEditor = {
                                    currentScreen = CurrentScreen.EDITOR
                                }
                            )
                        }
                        CurrentScreen.EDITOR -> {
                            EditorScreen(
                                viewModel = viewModel,
                                onBackToProjects = {
                                    currentScreen = CurrentScreen.DASHBOARD
                                }
                            )
                        }
                    }

                    if (showNewProjectDialog) {
                        NewProjectDialog(
                            onDismiss = { showNewProjectDialog = false },
                            onCreateProject = { name, aspect, fps, durationMs ->
                                viewModel.createNewProject(name, aspect, fps, durationMs)
                                currentScreen = CurrentScreen.EDITOR
                            }
                        )
                    }
                }
            }
        }
    }
}
