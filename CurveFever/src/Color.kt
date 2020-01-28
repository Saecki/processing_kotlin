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
    }
}