import processing.core.PApplet.dist
import processing.core.PConstants.HALF_PI
import processing.core.PConstants.TAU
import kotlin.math.absoluteValue
import kotlin.math.cos
import kotlin.math.sin

class ArcTrailSection(x1: Float, y1: Float, direction: Player.Direction, gap: Boolean, thickness: Float, val radius: Float, val start: Float, var end: Float) :
    TrailSection(x1, y1, direction, gap, thickness) {

    val startAngle: Float
        get() = start - HALF_PI * direction.factor

    val endAngle: Float
        get() = end - HALF_PI * direction.factor

    val arcCenterX: Float
        get() = x1 - cos(startAngle) * radius

    val arcCenterY: Float
        get() = y1 - sin(startAngle) * radius

    override val lastPosX: Float
        get() = x1 + (cos(endAngle) - cos(startAngle)) * radius

    override val lastPosY: Float
        get() = y1 + (sin(endAngle) - sin(startAngle)) * radius

    override fun intersectsWith(x: Float, y: Float, thickness: Float): Boolean {
        val p1Dist = dist(x1, y1, x, y)
        val p2Dist = dist(lastPosX, lastPosY, x, y)
        val maxEndDist = this.thickness / 2 + thickness / 2

        if (p1Dist < maxEndDist) {
            printD("arc\n")
            return true
        }

        if (p2Dist < maxEndDist) {
            printD("arc\n")
            return true
        }

        val minDist = radius - this.thickness / 2 - thickness / 2
        val maxDist = radius + this.thickness / 2 + thickness / 2

        val arcCenterDist = dist(arcCenterX, arcCenterY, x, y).absoluteValue

        if (arcCenterDist < minDist || arcCenterDist > maxDist)
            return false

        val arcStartAngle = floorMod(if (direction == Player.Direction.CLOCKWISE) startAngle else endAngle, TAU)
        val arcEndAngle = floorMod(if (direction == Player.Direction.CLOCKWISE) endAngle else startAngle, TAU)
        val arcAngle = floorMod(angle(arcCenterX, arcCenterY, x, y), TAU)

        if (arcStartAngle < arcEndAngle) {
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