package com.example.cheesechase

import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.absoluteValue
import kotlin.random.Random

class GameActivity(private val context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("game_prefs", Context.MODE_PRIVATE)

    private fun saveHighScore(score: Int) {
        val editor = sharedPreferences.edit()
        editor.putInt("high_score", score)
        editor.apply()
    }

    fun getHighScore(): Int {
        return sharedPreferences.getInt("high_score", 0)
    }

    private fun updateHighScore() {
        val currentScore = score.value
        val highScore = getHighScore()
        if (currentScore > highScore) {
            saveHighScore(currentScore)
        }
    }

    private fun endGame() {
        updateHighScore()
        gameover.value = true
    }

    private var mp1: MediaPlayer? = null
    private var mp2: MediaPlayer? = null
    private var mp3: MediaPlayer? = null
    private var mp4: MediaPlayer? = null
    private var mp5: MediaPlayer? = null

    init {
        mp1 = MediaPlayer.create(context, R.raw.buzz)
        mp2 = MediaPlayer.create(context, R.raw.over)
        mp3 = MediaPlayer.create(context, R.raw.game)
        mp4 = MediaPlayer.create(context, R.raw.coin)
        mp5 = MediaPlayer.create(context, R.raw.shoot)
    }

    val jerryX = mutableStateOf(1)
    val jerryY = mutableStateOf(0)
    val tomX = mutableStateOf(1)
    val tomY = mutableStateOf(0)
    val score = mutableStateOf(1)
    val distance = mutableStateOf(0)
    val gameover = mutableStateOf(false)
    val obstacles = mutableStateListOf<Obstacle>()
    val cheeses = mutableStateListOf<Cheese>()
    val coins = mutableStateListOf<Coin>()
    val bullets = mutableStateListOf<Bullet>()
    val random = Random
    val coincount = mutableStateOf(0)
    val bulletcount = mutableStateOf(0)
    val hascollided = mutableStateOf(false)

    private val lanes = 3
    private val collisionlimit = 10
    private val obstacleinterval = 1000L
    private val tomdistanceback = 100
    private val cheeseinterval = 5000L
    private val coininterval = 2000L
    private val lanewidth = getScreenWidth(context) / 3

    var tomfollowingJob: Job? = null
    var obstaclegenerationJob: Job? = null
    var coingenerationJob: Job? = null
    var cheesegenerationJob: Job? = null

    var retrofit = ApiClient.getInstance()?.create(APIInterface::class.java)
    var live = mutableStateOf(1)
    var initialLive = live.value

    init {
        fetchObstacleLimit()
    }

    private fun fetchObstacleLimit() {
        retrofit?.getObstacleLimit()?.enqueue(object : Callback<obstacleLimit> {
            override fun onResponse(call: Call<obstacleLimit>, response: Response<obstacleLimit>) {
                if (response.isSuccessful) {
                    val obstacleLimit = response.body()?.obstacleLimit ?: 0
                    live.value = obstacleLimit
                    initialLive = obstacleLimit
                    setupGame()
                }
            }

            override fun onFailure(call: Call<obstacleLimit>, t: Throwable) {

            }
        })
    }

    private fun setupGame() {
        val screenHeight = getScreenHeight(context)
        jerryY.value = screenHeight / 2
        tomY.value = jerryY.value + lanewidth * 2
        startTomFollowing()
        startObstacleGeneration()
        startCheeseGeneration()
        startCoinGeneration()
    }

    fun moveJerry(left: Boolean) {
        if (!gameover.value) {
            if (left) {
                if (jerryX.value > 0) jerryX.value--
            } else {
                if (jerryX.value < lanes - 1) jerryX.value++
            }
        }
    }

    fun update() {
        if (!gameover.value) {
            for (i in obstacles.indices.reversed()) {
                val obstacle = obstacles[i]
                obstacle.y += 5

                if (obstacle.x == jerryX.value &&
                    obstacle.y >= jerryY.value - collisionlimit &&
                    obstacle.y <= jerryY.value + collisionlimit) {
                    if (!hascollided.value) {
                        live.value -= 1
                        obstacles.removeAt(i)
                        hascollided.value = true
                        playBuzzSound()

                        if (live.value == 0) {
                            playOverSound()
                            endGame()
                        } else {
                            tomY.value = jerryY.value - tomdistanceback
                        }
                    }
                }

                if (obstacle.x == tomX.value &&
                    obstacle.y >= tomY.value - collisionlimit &&
                    obstacle.y <= tomY.value + collisionlimit) {
                    obstacles.removeAt(i)
                    playBuzzSound()
                }

                if (obstacle.y > jerryY.value) {
                    if (hascollided.value) {
                        hascollided.value = false
                    }
                }

                if (obstacle.y > getScreenHeight(context)) {
                    obstacles.removeAt(i)
                }
            }

            for (i in cheeses.indices.reversed()) {
                val cheese = cheeses[i]
                cheese.y += 5

                if (cheese.x == jerryX.value &&
                    cheese.y >= jerryY.value - collisionlimit &&
                    cheese.y <= jerryY.value + collisionlimit) {
                    playCheeseSound()
                    bulletcount.value++
                    cheeses.removeAt(i)
                    if (live.value < initialLive) {
                        live.value++
                    }
                }

                if (cheese.y > getScreenHeight(context)) {
                    cheeses.removeAt(i)
                }
            }

            for (i in coins.indices.reversed()) {
                val coin = coins[i]
                coin.y += 5

                if (coin.x == jerryX.value &&
                    coin.y >= jerryY.value - collisionlimit &&
                    coin.y <= jerryY.value + collisionlimit) {
                    playCheeseSound()
                    coins.removeAt(i)
                    score.value += 1000
                    coincount.value++
                }

                if (coin.y > getScreenHeight(context)) {
                    coins.removeAt(i)
                }
            }

            for (i in bullets.indices.reversed()) {
                val bullet = bullets[i]
                bullet.y -= 10

                for (j in obstacles.indices.reversed()) {
                    val obstacle = obstacles[j]
                    if (bullet.x == obstacle.x &&
                        bullet.y >= obstacle.y - collisionlimit &&
                        bullet.y <= obstacle.y + collisionlimit) {
                        obstacles.removeAt(j)
                        bullets.removeAt(i)
                        break
                    }
                }
            }

            if (live.value <= 0) {
                endGame()
            }

            score.value++
            distance.value++
        }
    }

    private fun playBuzzSound() {
        mp1?.start()
    }

    private fun playOverSound() {
        mp2?.start()
    }

    private fun playCheeseSound() {
        if (mp4?.isPlaying == false) {
            mp4?.start()
        }
    }

    private fun playShootSound() {
        mp5?.start()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startObstacleGeneration() {
        obstaclegenerationJob?.cancel()
        obstaclegenerationJob = GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(obstacleinterval)
                if (!gameover.value) {
                    withContext(Dispatchers.Main) {
                        addObstacle()
                    }
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startTomFollowing() {
        tomfollowingJob = GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(100)
                if (!gameover.value) {
                    withContext(Dispatchers.Main) {
                        tomX.value = jerryX.value
                        tomY.value = when {
                            (live.value >= 2) -> jerryY.value + lanewidth * 2
                            (live.value < 2) -> jerryY.value + lanewidth
                            else -> jerryY.value + lanewidth * 2
                        }
                    }
                }
            }
        }
    }

    fun resetGame() {
        tomfollowingJob?.cancel()
        obstaclegenerationJob?.cancel()
        cheesegenerationJob?.cancel()

        jerryX.value = 1
        jerryY.value = getScreenHeight(context) / 2
        tomX.value = 1
        tomY.value = jerryY.value + lanewidth * 2
        score.value = 0
        distance.value = 0
        gameover.value = false
        obstacles.clear()
        cheeses.clear()
        live.value = initialLive
        hascollided.value = false
        bullets.clear()

        startTomFollowing()
        startObstacleGeneration()
        startCheeseGeneration()
        startCoinGeneration()
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startCheeseGeneration() {
        cheesegenerationJob?.cancel()
        cheesegenerationJob = GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(cheeseinterval)
                if (!gameover.value && live.value < initialLive.absoluteValue) {
                    withContext(Dispatchers.Main) {
                        addCheese()
                    }
                }
            }
        }
    }

    fun shootBullet() {
        if (!gameover.value && bulletcount.value > 0) {
            bullets.add(Bullet(jerryX.value, jerryY.value))
            bulletcount.value--
            playShootSound()
        }
    }

    fun addObstacle() {
        if (!gameover.value) {
            val lane = random.nextInt(lanes)
            val newY = -lanewidth

            if (!isPositionOccupied(lane, newY)) {
                obstacles.add(Obstacle(lane, newY))
            }
        }
    }

    fun addCheese() {
        if (!gameover.value && live.value < initialLive.absoluteValue) {
            val lane = random.nextInt(lanes)
            val newY = -lanewidth

            if (!isPositionOccupied(lane, newY)) {
                cheeses.add(Cheese(lane, newY))
            }
        }
    }

    fun addCoin() {
        if (!gameover.value) {
            val lane = random.nextInt(lanes)
            val newY = -lanewidth

            if (!isPositionOccupied(lane, newY)) {
                coins.add(Coin(lane, newY))
            }
        }
    }

    private fun isPositionOccupied(lane: Int, y: Int): Boolean {
        for (obstacle in obstacles) {
            if (obstacle.x == lane && obstacle.y == y) return true
        }
        for (cheese in cheeses) {
            if (cheese.x == lane && cheese.y == y) return true
        }
        for (coin in coins) {
            if (coin.x == lane && coin.y == y) return true
        }
        return false
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun startCoinGeneration() {
        coingenerationJob?.cancel()
        coingenerationJob = GlobalScope.launch(Dispatchers.Default) {
            while (isActive) {
                delay(coininterval)
                if (!gameover.value) {
                    withContext(Dispatchers.Main) {
                        addCoin()
                    }
                }
            }
        }
    }

    data class Obstacle(var x: Int, var y: Int)
    data class Cheese(var x: Int, var y: Int)
    data class Coin(var x: Int, var y: Int)
    data class Bullet(var x: Int, var y: Int)

    fun getScreenHeight(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        return displayMetrics.heightPixels
    }

    fun getScreenWidth(context: Context): Int {
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetric)
        return displayMetric.widthPixels
    }
}
