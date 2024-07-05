package com.example.cheesechase

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun HomePageContent(navController: NavController) {
    val context = LocalContext.current
    val gameActivity = remember { GameActivity(context) }
    val highScore = gameActivity.getHighScore() + 1

    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Black,
            darkIcons = false
        )
    }
    val backgroundPainter: Painter = painterResource(id = R.drawable.home)
    val gradientColors = listOf(Color(0xFFFFD700), Color(0xFFFFA500))
    val showDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Image(
            painter = backgroundPainter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.TopEnd
            ) {
                Button(
                    onClick = { showDialog.value = true },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(Color.Red),
                ) {
                    Text(text = "Rules", color = Color.White)
                }
            }

            Text(
                text = "Cheese Chase",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    brush = Brush.linearGradient(gradientColors),
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(2f, 2f),
                        blurRadius = 3f
                    )
                ),
                modifier = Modifier
                    .padding(top = 30.dp, bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)

            )
            Text(
                text = "High Score: $highScore",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = Color(0xFFffffff),
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Spacer(modifier = Modifier.height(150.dp))

            Button(
                onClick = { navController.navigate("game") },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    Color(0xFF3A9FF1),
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .height(50.dp)
                    .width(160.dp)
                    .padding(8.dp)
                    .shadow(8.dp, RoundedCornerShape(50))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF3A9FF1), Color(0xFF3369E8)),
                            start = Offset(0f, 0f),
                            end = Offset(0f, 100f)
                        )
                    ),
                contentPadding = PaddingValues(0.dp) // Remove default padding
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3A9FF1), Color(0xFF3369E8)),
                                start = Offset(0f, 0f),
                                end = Offset(0f, 100f)
                            ),
                            shape = RoundedCornerShape(50)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp) // Add custom padding
                ) {
                    Text(
                        text = "Start Game",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.fillMaxSize(),
                        textAlign = TextAlign.Center
                    )
                }
            }

        }
    }


    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            confirmButton = {
                TextButton(onClick = { showDialog.value = false }) {
                    Text("Got it!")
                }
            },
            title = {
                Text(text = "Game Rules")
            },
            text = {
                Text("1. Avoid obstacles as they reduce your life by 1.\n2.Number of lives you have per match is random and will vary from 2 to 5.\n3. Tom gets close to you if your life goes below 3.\n4. Collecting cheese gives you cheese bullets to shoot down obstacles and heals you till you reach the inital number lives you had.\n5.Collecting coins increases your score by 1000")
            }
        )
    }
}
