object Time {
    private var lastMillis = now

    fun update() {
        val currentMillis = System.currentTimeMillis()

        deltaTime = (currentMillis - lastMillis) / 1000f
        lastMillis = currentMillis
    }

    var deltaTime = 0f
        get() = if (DEBUG) 0.016f else field
        private set

    val now
        get() = System.currentTimeMillis()
}