import processing.core.PApplet.*

class LinearTrailSection(x1: Float, y1: Float, gap: Boolean, thickness: Float, var x2: Float, var y2: Float) :
    TrailSection(x1, y1, Player.Direction.STRAIGHT, gap, thickness) {

    override val lastPosX: Float
        get() = x2

    override val lastPosY: Float
        get() = y2

    override fun intersectsWith(x: Float, y: Float, thickness: Float): Boolean {
        val p1Dist = dist(x1, y1, x, y)
        val p2Dist = dist(x2, y2, x, y)

        if (p1Dist < this.thickness / 2 + thickness / 2) {
            printD("linear\n")
            return true
        }

        if (p2Dist < this.thickness / 2 + thickness / 2) {
            printD("linear\n")
            return true
        }

        val centerLineAngle = floorMod(angle(x1, y1, x2, y2), TAU)
        val inverseCenterLineAngle = floorMod(centerLineAngle + PI, TAU)

        val xL1 = x1 + cos(centerLineAngle - HALF_PI) * this.thickness / 2
        val yL1 = y1 + sin(centerLineAngle - HALF_PI) * this.thickness / 2
        val xL2 = x1 - cos(centerLineAngle - HALF_PI) * this.thickness / 2
        val yL2 = y1 - sin(centerLineAngle - HALF_PI) * this.thickness / 2

        val maxDist = dist(x2, y2, xL1, yL1)

        if (p1Dist > maxDist || p2Dist > maxDist)
            return false

        val angleL1 = floorMod(angle(xL1, yL1, x, y), TAU)
        val angleL2 = floorMod(angle(xL2, yL2, x, y), TAU)

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