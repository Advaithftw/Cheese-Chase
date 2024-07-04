package com.example.cheesechase

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.delay

@Composable
fun GameDesign(gameState: GameActivity) {
    val configuration = LocalConfiguration.current
    val screenWidthPx = configuration.screenWidthDp.dp
    val screenHeightPx = configuration.screenHeightDp.dp
    val backgroundPainter: Painter = painterResource(id = R.drawable.gamescreen)

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
            when {
                gameState.score.value >= 11500 -> currentdelay = deadlydelay
                gameState.score.value >= 8000 -> currentdelay = extremelyfastdelay
                gameState.score.value >= 6500 -> currentdelay = fasterdelay
                gameState.score.value >= 4000 -> currentdelay = fastdelay
                else -> currentdelay = initialdelay
            }

            delay(currentdelay)
        }

    }


    Box(modifier = Modifier
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
            painter = backgroundPainter,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
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
        Canvas(modifier = Modifier.fillMaxSize()) {
            val jerryDiameter = laneWidth / 2f
            val jerryY = screenHeightPx / 2f
            val tomDiameter = laneWidth
            val tomY = gameState.tomY.value

            val jerryCenter = laneWidth * gameState.jerryX.value + laneWidth / 2
            drawCircle(
                color = Color(0xff5C4033),
                center = androidx.compose.ui.geometry.Offset(jerryCenter.toPx(), jerryY.toPx()),
                radius = jerryDiameter.toPx() / 2
            )
            val tomCenter = laneWidth * gameState.tomX.value + laneWidth / 2
            drawCircle(
                color = Color(0xff808990),
                center = androidx.compose.ui.geometry.Offset(tomCenter.toPx(), tomY.toFloat()),
                radius = tomDiameter.toPx() / 2
            )
            gameState.obstacles.forEach { obstacle ->
                val obstacleSize = (laneWidth / 6f).toPx()
                drawRect(
                    color = Color.Red,
                    topLeft = androidx.compose.ui.geometry.Offset(
                        (obstacle.x * laneWidth).toPx(),
                        obstacle.y.toFloat()
                    ),
                    size = androidx.compose.ui.geometry.Size(laneWidth.toPx(), obstacleSize)
                )

            }
            gameState.cheeses.forEach { cheese ->
                drawCircle(
                    color = Color.Yellow,
                    center = androidx.compose.ui.geometry.Offset(
                        (cheese.x + 0.5f) * laneWidth.toPx(),
                        cheese.y.toFloat()
                    ),
                    radius = laneWidth.toPx() / 4

                )

            }
            gameState.coins.forEach { coin ->
                drawCircle(
                    color = Color.Blue,
                    center = androidx.compose.ui.geometry.Offset(
                        (coin.x + 0.5f) * laneWidth.toPx(),
                        coin.y.toFloat()
                    ),
                    radius = laneWidth.toPx() / 4

                )

            }
            gameState.bullets.forEach { bullet ->
                drawCircle(
                    color = Color.Yellow,
                    center = androidx.compose.ui.geometry.Offset(
                        (bullet.x + 0.5f) * laneWidth.toPx(),
                        bullet.y.toFloat()
                    ),
                    radius = laneWidth.toPx() / 8)}
        }
    }
}

@Preview
@Composable
fun PreviewGameDesign() {
    val context = LocalContext.current
    GameDesign(gameState = GameActivity(context))
}
