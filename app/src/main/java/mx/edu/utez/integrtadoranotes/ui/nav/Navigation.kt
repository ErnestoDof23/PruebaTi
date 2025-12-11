package mx.edu.utez.integrtadoranotes.ui.nav


import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notasapp.ui.login.LoginScreen
import com.example.notasapp.ui.notes.NoteDetailScreen
import com.example.notasapp.ui.notes.NoteListScreen
import mx.edu.utez.integrtadoranotes.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = if (authViewModel.isLoggedIn.value) "notes" else "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("notes") }
            )
        }

        composable("notes") {
            NoteListScreen(navController)
        }

        composable("note/detail/{noteId}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteDetailScreen(navController, noteId)
        }

        composable("note/edit") {
            NoteDetailScreen(navController, null)
        }
    }
}