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

        update()
        world.init()
        background(50)
    }

    override fun draw() {
        update()
        world.update()

        background(50)

        world.items.forEach {
            drawItem(it)
        }
        drawPlayer(world.player1)
        drawPlayer(world.player2)

        if (menu)
            drawMenu()

        drawHUD()
    }

    override fun keyPressed() {

        when (keyCode) {
            LEFT -> p1CW = true
            RIGHT -> p1CCW = true
        }

        when (key) {
            in "Aa" -> p2CW = true
            in "Dd" -> p2CCW = true
            in "Rr" -> world.restart()
            ESC -> menu()
        }

        world.player1.turn(direction(p1CW, p1CCW))
        world.player2.turn(direction(p2CW, p2CCW))

        key = ' '
        keyCode = 0
    }

    override fun keyReleased() {
        when (keyCode) {
            LEFT -> p1CW = false
            RIGHT -> p1CCW = false
        }

        when (key) {
            in "Aa" -> p2CW = false
            in "Dd" -> p2CCW = false
        }

        world.player1.turn(direction(p1CW, p1CCW))
        world.player2.turn(direction(p2CW, p2CCW))
    }

    private fun update() {
        Time.update()
        Specs.width = width
        Specs.height = height
    }

    private fun direction(cw: Boolean, ccw: Boolean) = when {
        cw == ccw -> Player.Direction.STRAIGHT
        cw -> Player.Direction.COUNTER_CLOCKWISE
        else -> Player.Direction.CLOCKWISE
    }

    private fun drawMenu() {
        noStroke()
        fill(0, 100f)
        rect(0f, 0f, width.toFloat(), height.toFloat())
    }

    private fun drawHUD() {
        textSize(14f)
        setFill(world.player2.color)
        textAlign(LEFT, TOP)
        text("Player 2: ${world.player2.score}", 10f, 10f)

        setFill(world.player1.color)
        textAlign(RIGHT, TOP)
        text("Player 1: ${world.player1.score}", width - 10f, 10f)

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
        for (ts: Player.TrailSection in player.trail) {
            drawPlayerTrailSection(ts)
        }

        noStroke()
        setFill(player.color)
        circle(player.x, player.y, player.thickness)

        if (world.state == World.State.STARTING)
            drawPlayerDirectionArrow(player)
    }

    private fun drawPlayerTrailSection(ts: Player.TrailSection) {
        if (ts.gap) return

        strokeWeight(ts.thickness)

        when (ts.direction) {
            Player.Direction.STRAIGHT -> with(ts as Player.LinearTrailSection) {
                line(x1, y1, x2, y2)
            }
            else -> with(ts as Player.ArcTrailSection) {
                val arcCenterX = x1 - cos(startAngle) * radius
                val arcCenterY = y1 - sin(startAngle) * radius

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
        if (args[0] == "d"){
            DEBUG = true
            println("DEBUG")
        }

    CurveFever.run()
}
