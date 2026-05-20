package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CookingViewModel
import com.example.ui.viewmodel.CookingViewModelFactory
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize components from the Application singleton
                val app = application as CookingApplication
                val viewModel: CookingViewModel = viewModel(
                    factory = CookingViewModelFactory(app.repository)
                )

                val activeSession by viewModel.activeSession.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                // Reactive listener for notifications from repository actions
                LaunchedEffect(Unit) {
                    viewModel.uiMessage.collectLatest { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    BoxWithTransitions(
                        modifier = Modifier.padding(innerPadding),
                        activeSession = activeSession,
                        viewModel = viewModel
                    )
                }
            }
        }
    }
}

@Composable
fun BoxWithTransitions(
    modifier: Modifier = Modifier,
    activeSession: Any?,
    viewModel: CookingViewModel
) {
    BoxWithConstraints(modifier = modifier) {
        if (activeSession == null) {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {}
            )
        } else {
            DashboardScreen(
                viewModel = viewModel,
                onLogout = {}
            )
        }
    }
}

// Retain legacy Composable to maintain backward compatibility with default unit and screenshot tests
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
