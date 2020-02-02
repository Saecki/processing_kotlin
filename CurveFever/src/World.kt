import processing.core.PApplet.*
import java.util.*

class World {

    enum class State() {
        STARTING,
        RUNNING,
        PAUSED,
        STOPPED,
    }

    companion object {
        private const val DEFAULT_START_DELAY = 2000L
        private const val DEFAULT_ITEM_LIMIT = 5
        private const val DEFAULT_ITEM_SPAWN_RATE = 0.002
        private const val MIN_ITEM_DIST = 200f
        private const val MIN_WALL_DIST = 150f
    }

    private val itemLimit: Int
        get() = DEFAULT_ITEM_LIMIT

    private val itemSpawnRate: Double
        get() = DEFAULT_ITEM_SPAWN_RATE

    var state = State.STOPPED
        private set

    var startTime = 0L
        private set

    var items = mutableListOf<Item>()

    var players = mutableListOf<Player>()

    fun init() {
        printD("##############################\n")
        printD("-------------init-------------\n")
        printD("##############################\n")
        startTime = Time.now + DEFAULT_START_DELAY

        val p1 = randomPlayer("Player1").apply {
            setLeftKey(' ', LEFT)
            setRightKey(' ', RIGHT)
        }
        val p2 = randomPlayer("Player2", p1).apply {
            setLeftKey('a', 65)
            setRightKey('d', 68)
        }

        players.add(p1)
        players.add(p2)
    }

    fun update() {
        when (state) {
            State.STARTING -> {
                if (Time.now > startTime)
                    state = State.RUNNING
            }
            State.RUNNING -> {
                spawnItems(itemSpawnRate)
                players.forEach { it.update() }
                players.forEach { it.postUpdate() }
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
            printD("############\n")
            printD("restart\n")
            items = mutableListOf()
            state = State.STARTING
            startTime = Time.now + DEFAULT_START_DELAY

            val newPlayers = mutableListOf<Player>()

            for (p in players) {
                val coords = randomCoordinates(*newPlayers.toTypedArray())

                p.reset(coords.first, coords.second)
                newPlayers.add(p)
            }
        }
    }

    fun crashed(player: Player, msg: String) {
        player.crashed = true
        printD("${player.name} $msg\n")

        if (players.sumBy { if (it.crashed) 0 else 1 } < 2) {
            players.forEach { if (!it.crashed) it.score++ }
            stop()
        }
    }

    fun clearPlayerTrails() {
        players.forEach { it.clearTrail() }
    }

    fun addPlayer() {
        if (players.size < Color.PLAYER_COLORS.size) {
            val player = randomPlayer("Player${players.size + 1}", *players.toTypedArray())
            players.add(player)
        }
    }

    fun removePlayer(index: Int) {
        if (players.size > 2)
            players.removeAt(index)
    }

    private fun spawnItems(spawnRate: Double) {
        if (items.size >= itemLimit) return

        if (Math.random() <= spawnRate) {
            items.add(randomItem())
        }
    }

    private fun randomPlayer(name: String, vararg others: Player): Player {
        val coordinates = randomCoordinates(*others)
        val x = coordinates.first
        val y = coordinates.second
        val angle = Math.random().toFloat() * TAU
        val remainingColors = Color.PLAYER_COLORS.toMutableList()

        remainingColors.removeAll(others.map { it.color })

        return Player(x, y, angle, remainingColors.random(), name, this)
    }

    private fun randomItem(): Item {
        val coordinates = randomCoordinates(*players.toTypedArray())
        val x = coordinates.first
        val y = coordinates.second
        val type = Effect.itemEffects.random()

        return Item(x, y, type)
    }

    private fun randomCoordinates(vararg others: Player): Pair<Float, Float> {
        val r = Random()
        var x = 0f
        var y = 0f

        for (i in 0..50) {
            x = r.nextInt(Specs.width - (MIN_WALL_DIST * 2).toInt()).toFloat() + MIN_WALL_DIST
            y = r.nextInt(Specs.height - (MIN_WALL_DIST * 2).toInt()).toFloat() + MIN_WALL_DIST
            var tooClose = false

            for (i in items) {
                if (dist(x, y, i.x, i.y) < MIN_ITEM_DIST) {
                    tooClose = true
                    break
                }
            }

            for (p in others) {
                if (p.intersectsWith(x, y, MIN_ITEM_DIST)) {
                    tooClose = true
                    break
                }
            }

            if (!tooClose) break
        }

        return Pair(x, y)
    }

    fun nextColor(playerIndex: Int): Color {
        val player = players[playerIndex]
        val availableColors = Color.PLAYER_COLORS.filter { it !in players.map { it.color } || it == player.color }
        if (availableColors.size > 1) {
            val colorIndex = (availableColors.indexOf(player.color) + 1) % availableColors.size
            return availableColors[colorIndex]
        }

        return player.color
    }

    fun prevColor(playerIndex: Int): Color {
        val player = players[playerIndex]
        val availableColors = Color.PLAYER_COLORS.filter { it !in players.map { it.color } || it == player.color }
        if (availableColors.size > 1) {
            val colorIndex = floorMod(availableColors.indexOf(player.color) - 1, availableColors.size)
            return availableColors[colorIndex]
        }

        return player.color
    }
}