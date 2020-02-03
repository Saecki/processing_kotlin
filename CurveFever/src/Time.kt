object Time {

    private var lastMillis = 0L

    fun update(paused: Boolean) {
        val currentMillis = System.currentTimeMillis()

        if (paused) {
            deltaMillis = 0L
            deltaTime = 0f
        } else {
            deltaMillis = currentMillis - lastMillis
            deltaTime = deltaMillis / 1000f
            now += deltaMillis
        }
        lastMillis = currentMillis
    }

    var deltaMillis = 0L
        private set

    var deltaTime = 0f
        private set

    var now = 0L
        private set
}