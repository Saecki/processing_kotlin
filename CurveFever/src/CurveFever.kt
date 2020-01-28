import processing.core.PApplet

class CurveFever() : PApplet() {

    companion object Factory {

        fun run() {
            val cf = CurveFever()
            cf.runSketch()
        }
    }

    var world = World()

    var p1CW = false
    var p1CCW = false
    var p2CW = false
    var p2CCW = false

    val menu: Boolean
        get() = world.state == World.State.STOPPED || world.state == World.State.PAUSED

    override fun settings() {
        size(1280, 720)
    }

    override fun setup() {
        surface.setResizable(true)

        frameRate(144f)

        update()
        world.init()
        background(50)
    }

    override fun draw() {
        update()
        world.update()

        background(50)

        world.items.forEach { drawItem(it) }
        world.players.forEach { drawPlayer(it) }

        if (menu)
            drawMenu()

        drawHUD()
    }

    override fun keyPressed() {

        when (key) {
            ' ' -> world.restart()
            ESC -> menu()
        }

        world.players.forEach {
            when (keyCode) {
                it.leftKey -> it.leftPressed = true
                it.rightKey -> it.rightPressed = true
            }

            it.turn()
        }

        key = ' '
        keyCode = 0
    }

    override fun keyReleased() {
        world.players.forEach {
            when (keyCode) {
                it.leftKey -> it.leftPressed = false
                it.rightKey -> it.rightPressed = false
            }

            it.turn()
        }
    }

    private fun update() {
        Time.update()
        Specs.width = width
        Specs.height = height
    }

    private fun drawMenu() {
        noStroke()
        fill(0, 100f)
        rect(0f, 0f, width.toFloat(), height.toFloat())
    }

    private fun drawHUD() {
        textSize(14f)

        world.players.forEachIndexed { index, p ->
            setFill(p.color)
            textAlign(LEFT, TOP)
            text("${p.name} : ${p.score}", 10f, 10 + index * 20f)
        }

        if (world.state == World.State.STARTING) {
            textAlign(CENTER, CENTER)
            fill(230)
            textSize(30f)
            text(((world.startTime - Time.now) / 1000 + 1).toInt(), width / 2f, height / 2f)
        }
    }

    private fun drawPlayer(player: Player) {
        setStroke(player.color)
        noFill()
        for (ts: TrailSection in player.trail) {
            drawPlayerTrailSection(ts)
        }

        if (player.gap || player.trail.isEmpty()) {
            noStroke()
            setFill(player.color)
            circle(player.x, player.y, player.thickness)
        }

        if (world.state == World.State.STARTING)
            drawPlayerDirectionArrow(player)
    }

    private fun drawPlayerTrailSection(ts: TrailSection) {
        if (ts.gap) return

        strokeWeight(ts.thickness)

        when (ts.direction) {
            Player.Direction.STRAIGHT -> with(ts as LinearTrailSection) {
                line(x1, y1, x2, y2)
            }
            else -> with(ts as ArcTrailSection) {
                val arcStartAngle = if (startAngle > endAngle) endAngle else startAngle
                val arcEndAngle = if (startAngle > endAngle) startAngle else endAngle

                arc(arcCenterX, arcCenterY, radius * 2, radius * 2, arcStartAngle, arcEndAngle)
            }
        }
    }

    private fun drawPlayerDirectionArrow(player: Player) {
        strokeWeight(player.thickness / 3)
        stroke(230)

        val startDistance = 10f
        val endDistance = 30f
        val arrowDistance = 5f
        val firstArrowAngle = player.angle - PI / 4
        val secondArrowAngle = player.angle + PI / 4

        val startX = player.x + cos(player.angle) * startDistance
        val startY = player.y + sin(player.angle) * startDistance
        val endX = player.x + cos(player.angle) * endDistance
        val endY = player.y + sin(player.angle) * endDistance

        val firstArrowX = endX - cos(firstArrowAngle) * arrowDistance
        val firstArrowY = endY - sin(firstArrowAngle) * arrowDistance
        val secondArrowX = endX - cos(secondArrowAngle) * arrowDistance
        val secondArrowY = endY - sin(secondArrowAngle) * arrowDistance


        line(startX, startY, endX, endY)
        line(endX, endY, firstArrowX, firstArrowY)
        line(endX, endY, secondArrowX, secondArrowY)
    }

    private fun drawItem(item: Item) {
        noStroke()
        setFill(item.type.color)
        ellipse(item.x, item.y, Item.RADIUS * 2, Item.RADIUS * 2)
    }

    private fun menu() {
        if (menu)
            hideMenu()
        else
            showMenu()
    }

    private fun showMenu() {
        world.pause()
    }

    private fun hideMenu() {
        world.resume()
    }

    private fun setFill(color: Color) = fill(color.r, color.g, color.b)

    private fun setStroke(color: Color) = stroke(color.r, color.g, color.b)
}

fun main(args: Array<String>) {

    if (args.isNotEmpty())
        if (args[0] == "d") {
            DEBUG = true
            println("DEBUG")
        }

    CurveFever.run()
}
