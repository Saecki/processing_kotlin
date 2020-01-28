object Time {
    private var lastMillis = now

    fun update() {
        val currentMillis = System.currentTimeMillis()

        deltaTime = (currentMillis - lastMillis) / 1000f
        lastMillis = currentMillis
    }

    var deltaTime = 0f
        private set

    val now
        get() = System.currentTimeMillis()
}