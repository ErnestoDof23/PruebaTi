package mx.edu.utez.integrtadoranotes.ui.nav

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.edu.utez.integrtadoranotes.ui.screens.LoginScreen
import mx.edu.utez.integrtadoranotes.ui.screens.NoteDetailScreen
import mx.edu.utez.integrtadoranotes.ui.screens.NoteListScreen
import mx.edu.utez.integrtadoranotes.viewmodel.AuthViewModel
import mx.edu.utez.integrtadoranotes.viewmodel.NoteViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val noteViewModel: NoteViewModel = viewModel()
    
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val token by authViewModel.token.collectAsState()

    // Pasar el token al NoteViewModel cuando el usuario inicia sesión
    LaunchedEffect(token) {
        token?.let { 
            println("🔑 Token recibido en Navigation: ${it.take(20)}...")
            noteViewModel.setToken(it)
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "notes" else "login"
    ) {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = { 
                    navController.navigate("notes") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("notes") {
            NoteListScreen(
                navController = navController,
                noteViewModel = noteViewModel,
                onLogout = {
                    authViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("notes") { inclusive = true }
                    }
                }
            )
        }

        composable("note/detail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteDetailScreen(
                navController = navController,
                noteId = noteId,
                noteViewModel = noteViewModel
            )
        }

        composable("note/edit") {
            NoteDetailScreen(
                navController = navController,
                noteId = null,
                noteViewModel = noteViewModel
            )
        }
    }
}