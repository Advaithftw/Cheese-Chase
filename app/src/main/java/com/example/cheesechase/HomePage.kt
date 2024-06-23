package com.example.cheesechase

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.cheesechase.ui.theme.CheeseChaseTheme
import kotlinx.coroutines.delay
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class HomePage() : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CheeseChaseTheme {

                val navController = rememberNavController()


                HomePageContent(navController = navController)
            }
        }
    }
}


@Composable
fun GameScreen(navController: NavController) {
    val context = LocalContext.current
    val gameState = remember { GameActivity(context) }


    val systemUiController = rememberSystemUiController()
    val useDarkIcons = false

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = useDarkIcons
        )
    }


    GameDesign(gameState)

    LaunchedEffect(key1 = gameState) {
        while (!gameState.gameover.value) {
            gameState.update()
            delay(16L)
        }
    }

    if (gameState.gameover.value) {
        AlertDialog(
            onDismissRequest = { /* No-op */ },
            title = { Text(text = "Game Over") },
            text = { Text(text = "Your score is: ${gameState.score.value}") },
            confirmButton = {
                Button(onClick = {
                    gameState.resetGame()
                    navController.navigate("game") {
                        popUpTo("game") { inclusive = true }
                    }
                }) {
                    Text("Restart")
                }
                Button(onClick = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = true }
                    }
                }) {
                    Text("Home")}

            }
        )
    }
}




