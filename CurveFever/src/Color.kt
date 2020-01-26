import java.util.*

data class Color(val r: Float, val g: Float, var b: Float) {

    companion object {

        val PLAYER_COLORS = listOf(
            Color(230f, 100f, 20f),
            Color(50f, 230f, 20f),
            Color(130f, 100f, 200f),
            Color(30f, 200f, 200f),
            Color(230f, 40f, 200f)
        )

        fun random(): Color {
            val random = Random()
            val r = random.nextInt(255)
            val g = random.nextInt(255)
            val b = random.nextInt(255)
            return Color(r.toFloat(), g.toFloat(), b.toFloat())
        }
    }
}