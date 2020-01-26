import processing.core.PApplet.*
import kotlin.math.absoluteValue

private const val BASE_SPEED = 150f
private const val MIN_SPEED = 50f
private const val BASE_TURNING_RADIUS = 50f
private const val MIN_TURNING_RADIUS = 25f
private const val BASE_THICKNESS = 4f
private const val MIN_THICKNESS = 1f
private const val GAP_RATE = 0.4

data class Player(var x: Float, var y: Float, var angle: Float, val color: Color, val world: World) {

    abstract class TrailSection(val x1: Float, val y1: Float, val direction: Direction, val gap: Boolean, val thickness: Float) {
        abstract fun lastPosition(): Pair<Float, Float>
        abstract fun intersectsWith(x: Float, y: Float, thickness: Float): Boolean
    }

    class LinearTrailSection(x1: Float, y1: Float, gap: Boolean, thickness: Float, var x2: Float, var y2: Float) :
        TrailSection(x1, y1, Direction.STRAIGHT, gap, thickness) {

        override fun lastPosition() = Pair(x2, y2)

        override fun intersectsWith(x: Float, y: Float, thickness: Float): Boolean {
            printD("linear")
            val p1Dist = dist(x1, y1, x, y)
            val p2Dist = dist(x2, y2, x, y)

            if (p1Dist < this.thickness / 2 + thickness / 2)
                return true

            if (p2Dist < this.thickness / 2 + thickness / 2)
                return true

            val lineAngle = angle(x1, y1, x2, y2)
            val oppositeLineAngle = lineAngle + PI

            val xL1 = x1 + cos(lineAngle - HALF_PI) * this.thickness / 2
            val yL1 = y1 + sin(lineAngle - HALF_PI) * this.thickness / 2
            val xL2 = x1 - cos(lineAngle - HALF_PI) * this.thickness / 2
            val yL2 = y1 - sin(lineAngle - HALF_PI) * this.thickness / 2

            val maxDist = dist(x2, y2, xL1, yL1)

            if (p1Dist < maxDist && p2Dist < maxDist) {
                val angleL1 = angle(xL1, yL1, x, y)
                val angleL2 = angle(xL2, yL2, x, y)

                if (angleL1 in lineAngle..oppositeLineAngle != angleL2 in lineAngle..oppositeLineAngle)
                    return true
            }

            return false
        }
    }

    class ArcTrailSection(x1: Float, y1: Float, direction: Direction, gap: Boolean, thickness: Float, val radius: Float, val start: Float, var end: Float) :
        TrailSection(x1, y1, direction, gap, thickness) {

        val startAngle: Float
            get() = start - HALF_PI * direction.factor

        val endAngle: Float
            get() = end - HALF_PI * direction.factor

        override fun lastPosition(): Pair<Float, Float> {
            val x = x1 + (cos(endAngle) - cos(startAngle)) * radius
            val y = y1 + (sin(endAngle) - sin(startAngle)) * radius
            return Pair(x, y)
        }

        override fun intersectsWith(x: Float, y: Float, thickness: Float): Boolean {
            printD("arc")
            val minDist = this.radius - this.thickness / 2 - thickness / 2
            val maxDist = this.radius + this.thickness / 2 + thickness / 2

            val arcCenterX = x1 - cos(startAngle) * radius
            val arcCenterY = y1 - sin(startAngle) * radius

            val arcCenterDist = dist(arcCenterX, arcCenterY, x, y).absoluteValue

            if (arcCenterDist > minDist && arcCenterDist < maxDist) {
                val arcStartAngle = floorMod(if (direction == Direction.CLOCKWISE) startAngle else endAngle, TAU)
                val arcEndAngle = floorMod(if (direction == Direction.CLOCKWISE) endAngle else startAngle, TAU)
                val arcAngle = floorMod(angle(arcCenterX, arcCenterY, x, y), TAU)

                return if (arcStartAngle < arcEndAngle) {
                    arcAngle > arcStartAngle && arcAngle < arcEndAngle
                } else {
                    arcAngle > arcStartAngle || arcAngle < arcEndAngle
                }
            }

            return false
        }
    }

    enum class Direction(val factor: Int) {
        STRAIGHT(0),
        COUNTER_CLOCKWISE(-1),
        CLOCKWISE(1)
    }

    var effects = mutableListOf<Effect>()
    var trail = mutableListOf<TrailSection>()
    var direction = Direction.STRAIGHT
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
    }

    fun update() {
        updateEffects()
        move()
    }

    fun postUpdate() {
        checkForCrash()
        collectItems()
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

        val pos = trail.last().lastPosition()
        x = pos.first
        y = pos.second

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
        if (x < thickness / 2 || x > Specs.width - thickness / 2 ||
            y < thickness / 2 || y > Specs.height - thickness / 2
        )
            world.crashed(this)

        val other = if (world.player1 == this) world.player2 else world.player1

        if (intersectsWithPlayer(other))
            world.crashed(this)
    }

    private fun intersectsWithPlayer(other: Player): Boolean {
        if (intersectsWithTrail(other.trail))
            return true

        if (dist(other.x, other.y, x, y) < other.thickness / 2 + thickness / 2)
            return true

        return false
    }

    private fun intersectsWithTrail(trail: List<TrailSection>): Boolean {
        for (ts in trail) {
            if (ts.gap)
                continue

            printD("<")
            if (ts.intersectsWith(this.x, this.y, this.thickness)) {
                printD(">: ")
                return true
            } else {
                printD(">\n")
            }
        }

        return false
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

    fun turn(direction: Direction) {
        this.direction = direction
    }
}

