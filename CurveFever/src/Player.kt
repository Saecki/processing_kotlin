import processing.core.PApplet.*
import kotlin.math.absoluteValue

data class Player(var x: Float, var y: Float, var angle: Float, val color: Color, val world: World, val name: String) {

    enum class Direction(val factor: Int) {
        STRAIGHT(0),
        COUNTER_CLOCKWISE(-1),
        CLOCKWISE(1)
    }

    companion object {
        private const val BASE_SPEED = 150f
        private const val MIN_SPEED = 50f
        private const val BASE_TURNING_RADIUS = 50f
        private const val MIN_TURNING_RADIUS = 25f
        private const val BASE_THICKNESS = 4f
        private const val MIN_THICKNESS = 1f
        private const val GAP_RATE = 0.4

        private const val WALL_CRASH_MESSAGE = "crashed into the wall"
        private const val SELF_CRASH_MESSAGE = "crashed into himself"
        private const val PLAYER_CRASH_MESSAGE = "crashed into "

        fun intersectsWithTrail(x: Float, y: Float, thickness: Float, trail: List<TrailSection>): Boolean {
            for (ts in trail) {
                if (ts.gap)
                    continue

                if (ts.intersectsWith(x, y, thickness))
                    return true
            }

            return false
        }
    }

    var leftKey: Int = 0
    var rightKey: Int = 0

    var leftPressed = false
    var rightPressed = false

    var effects = mutableListOf<Effect>()
    var trail = mutableListOf<TrailSection>()
    var direction = Direction.STRAIGHT
    var crashed = false
    var score = 0

    val speed: Float
        get() {
            val s = BASE_SPEED + effects.sumByDouble { it.type.speed.toDouble() }.toFloat()
            return if (s < MIN_SPEED) MIN_SPEED else s
        }

    val turningRadius: Float
        get() {
            val t = BASE_TURNING_RADIUS + effects.sumByDouble { it.type.turningRadius.toDouble() }.toFloat()
            return if (t < MIN_TURNING_RADIUS) MIN_TURNING_RADIUS else t
        }

    val thickness: Float
        get() {
            val t = BASE_THICKNESS + effects.sumByDouble { it.type.thickness.toDouble() }.toFloat()
            return if (t < MIN_THICKNESS) MIN_THICKNESS else t
        }

    val gap: Boolean
        get() = effects.any { effect -> effect.type == Effect.Type.GAP }

    fun reset(x: Float, y: Float) {
        this.x = x
        this.y = y
        angle = (Math.random() * TAU).toFloat()
        effects = mutableListOf()
        trail = mutableListOf()
        direction = Direction.STRAIGHT
        crashed = false

        leftPressed = false
        rightPressed = false
    }

    fun update() {
        if (!crashed) {
            updateEffects()
            move()
        }
    }

    fun postUpdate() {
        if (!crashed) {
            checkForCrash()
            collectItems()
        }
    }

    private fun updateEffects() {
        effects.removeAll { effect -> effect.start + effect.duration < Time.now }

        if (!gap && Math.random() < Time.deltaTime * GAP_RATE)
            effects.add(Effect.ofType(Effect.Type.GAP))
    }

    private fun move() {

        if (trail.isEmpty()) {
            addTrailSection()
        } else {
            updateTrailSection()
        }

        x = trail.last().lastPosX
        y = trail.last().lastPosY

        if (trail.last().direction != Direction.STRAIGHT)
            angle = (trail.last() as ArcTrailSection).end

        if (trail.last().direction != direction)
            addTrailSection()

        if (trail.last().gap != gap)
            addTrailSection()

        if (trail.last().thickness != thickness)
            addTrailSection()

        if (trail.last().direction != Direction.STRAIGHT)
            if ((trail.last() as ArcTrailSection).radius != turningRadius)
                addTrailSection()
    }

    private fun updateTrailSection() {
        when (trail.last().direction) {
            Direction.STRAIGHT -> {
                (trail.last() as LinearTrailSection).apply {
                    x2 += Time.deltaTime * speed * cos(angle)
                    y2 += Time.deltaTime * speed * sin(angle)
                }
            }
            else -> {
                (trail.last() as ArcTrailSection).apply {
                    end += Time.deltaTime * speed / radius * trail.last().direction.factor
                }
            }
        }
    }

    private fun addTrailSection() {
        trail.add(
            when (direction) {
                Direction.STRAIGHT -> {
                    LinearTrailSection(x, y, gap, thickness, x, y)
                }
                else -> {
                    ArcTrailSection(x, y, direction, gap, thickness, turningRadius, angle, angle)
                }
            }
        )
    }

    private fun checkForCrash() {
        if (x < thickness / 2 || x > Specs.width - thickness / 2
            || y < thickness / 2 || y > Specs.height - thickness / 2
        )
            world.crashed(this, WALL_CRASH_MESSAGE)

        if (intersectsWithOwnTrail()) {
            world.crashed(this, SELF_CRASH_MESSAGE)
        }

        val others = world.players.filter { it != this }

        for (p in others) {
            if (p.intersectsWith(this.x, this.y, this.thickness)) {
                world.crashed(this, PLAYER_CRASH_MESSAGE + p.name)
                break
            }
        }
    }

    fun intersectsWith(x: Float, y: Float, radius: Float): Boolean {
        if (intersectsWithTrail(x, y, thickness, this.trail))
            return true

        if (dist(this.x, this.y, x, y) < this.thickness / 2 + radius)
            return true

        return false
    }

    private fun intersectsWithOwnTrail(): Boolean {
        val trailToCheck = trail.filter {
            val minDist = thickness / 2 + it.thickness / 2
            val endDist = dist(x, y, it.lastPosX, it.lastPosY)

            if (endDist < minDist) {
                if (it.direction != Direction.STRAIGHT) {
                    with(it as ArcTrailSection) {
                        val angleDiff = (start - end).absoluteValue
                        if (angleDiff > PI) {
                            val startDist = dist(x1, y1, x, y)
                            if (startDist < minDist) {
                                return true
                            }
                        }
                    }
                }
            }

            endDist > minDist
        }

        return intersectsWithTrail(this.x, this.y, this.thickness, trailToCheck)
    }

    private fun collectItems() {
        val collectedItems = world.items.filter { item -> dist(item.x, item.y, x, y) < Item.RADIUS + thickness / 2 }

        world.items.removeAll(collectedItems)

        effects.addAll(collectedItems.map { item -> Effect.ofType(item.type) })

        if (collectedItems.any { item -> item.type == Effect.Type.CLEAR }) {
            world.clearPlayerTrails()
        }
    }

    fun clearTrail() {
        trail.clear()
    }

    fun turn() {
        this.direction = when {
            leftPressed == rightPressed -> Direction.STRAIGHT
            leftPressed -> Direction.COUNTER_CLOCKWISE
            else -> Direction.CLOCKWISE
        }
    }
}

