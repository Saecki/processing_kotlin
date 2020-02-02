data class Effect(val start: Long, val duration: Long, var type: Type) {

    companion object {

        val itemEffects = listOf(
            Type.SPEEDUP,
            Type.SLOWDOWN,
            Type.EXPAND,
            Type.SHRINK,
            Type.FAST_TURNING,
            Type.SLOW_TURNING,
            Type.CLEAR
        )

        fun ofType(type: Type): Effect {
            val duration = type.defaultDuration + (type.deviation * Math.random()).toLong()
            return Effect(Time.now, duration, type)
        }
    }

    enum class Type(val speed: Float, val turningRadius: Float, val thickness: Float, val defaultDuration: Long, val deviation: Long, val color: Color) {
        GAP(0f, 0f, 0f, 150L, 100L, Color(0f, 0f, 0f)),
        SPEEDUP(50f, 0f, 0f, 5000L, 1000L, Color(50f, 60f, 200f)),
        SLOWDOWN(-50f, 0f, 0f, 5000L, 1000L, Color(200f, 60f, 50f)),
        FAST_TURNING(0f, -20f, 0f, 5000L, 1000L, Color(30f, 240f, 220f)),
        SLOW_TURNING(0f, 20f, 0f, 5000L, 1000L, Color(150f, 40f, 240f)),
        EXPAND(0f, 0f, 4f, 5000L, 1000L, Color(30f, 230f, 0f)),
        SHRINK(0f, 0f, -2f, 5000L, 1000L, Color(200f, 200f, 40f)),
        CLEAR(0f, 0f, 0f, 0L, 0L, Color(230f, 40f, 220f))
    }
}