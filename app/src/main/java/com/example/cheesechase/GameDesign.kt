package com.example.cheesechase

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.times

@Composable
fun GameDesign(gameState: GameActivity) {
    val context = LocalContext.current
    val apiService = remember {
        Retrofit.Builder()
            .baseUrl("https://chasedeux.vercel.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(APIInterface::class.java)
    }

    var gameImages by remember { mutableStateOf<GameImages?>(null) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            gameImages = fetchImages(apiService)
        }
    }

    gameImages?.let { images ->
        val configuration = LocalConfiguration.current
        val screenWidthPx = configuration.screenWidthDp.dp
        val screenHeightPx = configuration.screenHeightDp.dp
        val laneWidth = screenWidthPx / 3
        val initialdelay = 16L
        val fastdelay = 8L
        val fasterdelay = 4L
        val extremelyfastdelay = 2L
        val deadlydelay = 1L

        LaunchedEffect(Unit) {
            var currentdelay = initialdelay
            while (!gameState.gameover.value) {
                gameState.update()
                currentdelay = when {
                    gameState.score.value >= 11500 -> deadlydelay
                    gameState.score.value >= 8000 -> extremelyfastdelay
                    gameState.score.value >= 6500 -> fasterdelay
                    gameState.score.value >= 4000 -> fastdelay
                    else -> initialdelay
                }
                delay(currentdelay)
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xff00B2B3))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset ->
                            if (offset.x < size.width / 2) {
                                gameState.moveJerry(left = true)
                            } else {
                                gameState.moveJerry(left = false)
                            }
                        }
                    )
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.gamer),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )



            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(
                    modifier = Modifier
                        .width(10.dp)
                        .background(Color.Black)
                        .fillMaxHeight()
                ) {}

                Column(
                    modifier = Modifier
                        .width(10.dp)
                        .background(Color.Black)
                        .fillMaxHeight()
                ) {}
            }


            Canvas(modifier = Modifier.fillMaxSize()) {
                val laneWidthPx = laneWidth.toPx()

                gameState.cheeses.forEach { cheese ->
                    drawCircle(
                        color = Color.Yellow,
                        center = Offset(
                            (cheese.x + 0.5f) * laneWidthPx,
                            cheese.y.toFloat()
                        ),
                        radius = laneWidthPx / 4
                    )
                }

                gameState.coins.forEach { coin ->
                    drawCircle(
                        color = Color.Blue,
                        center = Offset(
                            (coin.x + 0.5f) * laneWidthPx,
                            coin.y.toFloat()
                        ),
                        radius = laneWidthPx / 4
                    )
                }

                gameState.bullets.forEach { bullet ->
                    drawCircle(
                        color = Color.Yellow,
                        center = Offset(
                            (bullet.x + 0.5f) * laneWidthPx,
                            bullet.y.toFloat()
                        ),
                        radius = laneWidthPx / 8
                    )
                }

                gameState.obstacles.forEach { obstacle ->
                    drawImage(
                        image = images.obstacleImage,
                        topLeft = Offset(
                            ((obstacle.x * laneWidthPx)-50),
                            obstacle.y.toFloat()
                        ),




                        )
                }
            }


            val jerryY = screenHeightPx / 2f

            val tomY = jerryY.value.dp + laneWidth*( (gameState.live.value + 1) /2)
            Image(

                bitmap = images.jerryImage,
                contentDescription = "Jerry",
                modifier = Modifier
                    .size(laneWidth, laneWidth)
                    .offset(
                        x = laneWidth * gameState.jerryX.value,
                        y = jerryY.value.dp
                    )
            )

            Image(
                bitmap = images.tomImage,
                contentDescription = "Tom",
                modifier = Modifier
                    .size(laneWidth*2, laneWidth*2)
                    .offset(
                        x = (laneWidth * gameState.tomX.value)-laneWidth/2,
                        y = tomY
                    )
            )

            Button(
                onClick = { gameState.shootBullet() },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(Color.Blue)
            ) {
                Text(text = "Shoot", fontSize = 20.sp)
            }






            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Score: ${gameState.score.value}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                )
            }
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Lives: ${gameState.live.value}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(50.dp)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Coins: ${gameState.coincount.value}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 84.dp, end = 50.dp)
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "Cheese: ${gameState.bulletcount.value} ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 118.dp, end = 50.dp)
                )
            }
        }
    } ?: run {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Loading ...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    color = Color.White,
                    strokeWidth = 4.dp
                )

            }
        }
}}

private suspend fun fetchImages(apiService: APIInterface): GameImages {
    return withContext(Dispatchers.IO) {
        val jerryResponse = apiService.getImage("jerry").execute()
        val tomResponse = apiService.getImage("tom").execute()
        val obstacleResponse = apiService.getImage("obstacle").execute()

        val jerryImage = jerryResponse.body()?.byteStream()?.use { BitmapFactory.decodeStream(it).asImageBitmap() }
        val tomImage = tomResponse.body()?.byteStream()?.use { BitmapFactory.decodeStream(it).asImageBitmap() }
        val obstacleImage = obstacleResponse.body()?.byteStream()?.use { BitmapFactory.decodeStream(it).asImageBitmap() }

        GameImages(
            jerryImage = jerryImage ?: throw Exception("Failed to load Jerry image"),
            tomImage = tomImage ?: throw Exception("Failed to load Tom image"),
            obstacleImage = obstacleImage ?: throw Exception("Failed to load Obstacle image")
        )
    }
}

@Preview
@Composable
fun PreviewGameDesign() {
    val context = LocalContext.current
    GameDesign(gameState = GameActivity(context))
}
