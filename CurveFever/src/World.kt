import processing.core.PApplet.*
import java.util.*

private const val DEFAULT_START_DELAY = 2000L
private const val DEFAULT_ITEM_LIMIT = 5
private const val DEFAULT_ITEM_SPAWN_RATE = 0.005
private const val MIN_ITEM_DIST = 50f
private const val MIN_WALL_DIST = 150f

class World {

    enum class State() {
        STARTING,
        RUNNING,
        PAUSED,
        STOPPED,
    }

    private val itemLimit: Int
        get() = DEFAULT_ITEM_LIMIT

    private val itemSpawnRate: Double
        get() = DEFAULT_ITEM_SPAWN_RATE

    var state = State.STARTING
        private set

    var startTime = 0L
        private set

    var items = mutableListOf<Item>()
    lateinit var player1: Player
    lateinit var player2: Player

    fun init() {
        startTime = Time.now + DEFAULT_START_DELAY
        player1 = randomPlayer()
        player2 = randomPlayer(player1)
    }

    fun update() {
        when (state) {
            State.STARTING -> {
                if (Time.now > startTime)
                    state = State.RUNNING
            }
            State.RUNNING -> {
                spawnItems(itemSpawnRate)
                player1.update()
                player2.update()
                player1.postUpdate()
                player2.postUpdate()
            }
        }
    }

    fun pause() {
        if (state == State.RUNNING)
            state = State.PAUSED
    }

    fun resume() {
        if (state == State.PAUSED)
            state = State.RUNNING
    }

    fun stop() {
        state = State.STOPPED
    }

    fun restart() {
        if (state == State.PAUSED || state == State.STOPPED) {
            startTime = Time.now + DEFAULT_START_DELAY
            items = mutableListOf()
            state = State.STARTING

            val p1Coords = randomCoordinates()
            val p2Coords = randomCoordinates()
            player1.reset(p1Coords.first, p1Coords.second)
            player2.reset(p2Coords.first, p2Coords.second)
        }
    }

    fun clearPlayerTrails() {
        player1.clearTrail()
        player2.clearTrail()
    }

    fun crashed(player: Player) {
        stop()
        if (player == player1) {
            printD("player1 crashed\n")
            player2.score++
        } else {
            printD("player2 crashed\n")
            player1.score++
        }
    }

    private fun spawnItems(spawnRate: Double) {
        if (items.size >= itemLimit) return

        if (Math.random() <= spawnRate) {
            items.add(randomItem())
        }
    }

    private fun randomPlayer(vararg other: Player): Player {
        val coordinates = randomCoordinates(*other.map { Pair(it.x, it.y) }.toTypedArray())
        val x = coordinates.first
        val y = coordinates.second
        val angle = Math.random().toFloat() * TAU
        val remainingColors = Color.PLAYER_COLORS.toMutableList()

        remainingColors.removeAll(other.map { it.color })

        return Player(x, y, angle, remainingColors.random(), this)
    }

    private fun randomItem(vararg other: Player): Item {
        val coordinates = randomCoordinates()
        val x = coordinates.first
        val y = coordinates.second
        val type = Effect.itemEffects.random()

        return Item(x, y, type)
    }

    private fun randomCoordinates(vararg other: Pair<Float, Float>): Pair<Float, Float> {
        val r = Random()
        var x = 0f
        var y = 0f

        for (i in 0..20) {
            x = r.nextInt(Specs.width - (MIN_WALL_DIST * 2).toInt()).toFloat() + MIN_WALL_DIST
            y = r.nextInt(Specs.height - (MIN_WALL_DIST * 2).toInt()).toFloat() + MIN_WALL_DIST
            var tooClose = false

            for (i in items) {
                if (dist(x, y, i.x, i.y) < MIN_ITEM_DIST) {
                    tooClose = true
                    break
                }
            }

            for (c in other) {
                if (dist(x, y, c.first, c.second) < MIN_ITEM_DIST) {
                    tooClose = true
                    break
                }
            }

            if (!tooClose) break
        }

        return Pair(x, y)
    }
}