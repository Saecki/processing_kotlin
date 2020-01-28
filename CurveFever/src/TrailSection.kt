abstract class TrailSection(val x1: Float, val y1: Float, val direction: Player.Direction, val gap: Boolean, val thickness: Float) {

    abstract val lastPosX: Float
    abstract val lastPosY: Float

    abstract fun intersectsWith(x: Float, y: Float, thickness: Float): Boolean
}