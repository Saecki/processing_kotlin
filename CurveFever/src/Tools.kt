import kotlin.math.atan2

var DEBUG = false

fun floorMod(a: Float, b: Float): Float {
    var result = a

    while (result < 0) {
        result += b
    }

    return result % b
}

fun angle(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val xDist = x2 - x1
    val yDist = y2 - y1

    return atan2(yDist.toDouble(), xDist.toDouble()).toFloat()
}

fun printD(msg: Any) {
    if (DEBUG)
        print(msg)
}