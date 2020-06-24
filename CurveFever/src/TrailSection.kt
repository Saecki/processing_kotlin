import processing.core.PApplet
import processing.core.PConstants
import kotlin.math.absoluteValue

abstract class TrailSection(val x1: Float, val y1: Float, val direction: Player.Direction, val gap: Boolean, val thickness: Float) {
    abstract val lastPosX: Float
    abstract val lastPosY: Float

    abstract fun intersectsWith(x: Float, y: Float, dist: Float): Boolean
}

class LinearTrailSection(x1: Float, y1: Float, gap: Boolean, thickness: Float, var x2: Float, var y2: Float) :
    TrailSection(x1, y1, Player.Direction.STRAIGHT, gap, thickness) {

    override val lastPosX: Float
        get() = x2

    override val lastPosY: Float
        get() = y2

    override fun intersectsWith(x: Float, y: Float, dist: Float): Boolean {
        val p1Dist = PApplet.dist(x1, y1, x, y)
        val p2Dist = PApplet.dist(x2, y2, x, y)

        if (p1Dist < this.thickness / 2 + dist) {
            printD("linear\n")
            return true
        }

        if (p2Dist < this.thickness / 2 + dist) {
            printD("linear\n")
            return true
        }

        val centerLineAngle = floorMod(angle(x1, y1, x2, y2), PApplet.TAU)
        val inverseCenterLineAngle = floorMod(centerLineAngle + PApplet.PI, PApplet.TAU)

        val xL1 = x1 + PApplet.cos(centerLineAngle - PApplet.HALF_PI) * this.thickness / 2
        val yL1 = y1 + PApplet.sin(centerLineAngle - PApplet.HALF_PI) * this.thickness / 2
        val xL2 = x1 - PApplet.cos(centerLineAngle - PApplet.HALF_PI) * this.thickness / 2
        val yL2 = y1 - PApplet.sin(centerLineAngle - PApplet.HALF_PI) * this.thickness / 2

        val maxDist = PApplet.dist(x2, y2, xL1, yL1)

        if (p1Dist > maxDist || p2Dist > maxDist)
            return false

        val angleL1 = floorMod(angle(xL1, yL1, x, y), PApplet.TAU)
        val angleL2 = floorMod(angle(xL2, yL2, x, y), PApplet.TAU)

        if (centerLineAngle < inverseCenterLineAngle) {
            if ((angleL1 > centerLineAngle && angleL1 < inverseCenterLineAngle)
                != (angleL2 > centerLineAngle && angleL2 < inverseCenterLineAngle)
            ) {
                printD("linear\n")
                return true
            }
        } else {
            if ((angleL1 > centerLineAngle || angleL1 < inverseCenterLineAngle)
                != (angleL2 > centerLineAngle || angleL2 < inverseCenterLineAngle)
            ) {
                printD("linear\n")
                return true
            }
        }

        return false
    }
}

class ArcTrailSection(x1: Float, y1: Float, direction: Player.Direction, gap: Boolean, thickness: Float, val radius: Float, val start: Float, var end: Float) :
    TrailSection(x1, y1, direction, gap, thickness) {

    val startAngle: Float
        get() = start - PConstants.HALF_PI * direction.factor

    val endAngle: Float
        get() = end - PConstants.HALF_PI * direction.factor

    val arcCenterX: Float
        get() = x1 - PApplet.cos(startAngle) * radius

    val arcCenterY: Float
        get() = y1 - PApplet.sin(startAngle) * radius

    override val lastPosX: Float
        get() = x1 + (PApplet.cos(endAngle) - PApplet.cos(startAngle)) * radius

    override val lastPosY: Float
        get() = y1 + (PApplet.sin(endAngle) - PApplet.sin(startAngle)) * radius

    override fun intersectsWith(x: Float, y: Float, dist: Float): Boolean {
        val p1Dist = PApplet.dist(x1, y1, x, y)
        val p2Dist = PApplet.dist(lastPosX, lastPosY, x, y)
        val maxEndDist = this.thickness / 2 + dist

        if (p1Dist < maxEndDist || p2Dist < maxEndDist) {
            printD("arc\n")
            return true
        }

        val minDist = this.radius - this.thickness / 2 - dist
        val maxDist = this.radius + this.thickness / 2 + dist

        val arcCenterDist = PApplet.dist(arcCenterX, arcCenterY, x, y).absoluteValue

        if (arcCenterDist < minDist || arcCenterDist > maxDist)
            return false

        val arcStartAngle = floorMod(if (direction == Player.Direction.CLOCKWISE) startAngle else endAngle, PConstants.TAU)
        val arcEndAngle = floorMod(if (direction == Player.Direction.CLOCKWISE) endAngle else startAngle, PConstants.TAU)
        val arcAngle = floorMod(angle(arcCenterX, arcCenterY, x, y), PConstants.TAU)

        if (arcStartAngle <= arcEndAngle) {
            if (arcAngle > arcStartAngle && arcAngle < arcEndAngle) {
                printD("arc\n")
                return true
            }
        } else {
            if (arcAngle > arcStartAngle || arcAngle < arcEndAngle) {
                printD("arc\n")
                return true
            }
        }

        return false
    }
}
