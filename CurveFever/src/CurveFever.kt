import processing.core.PApplet
import processing.core.PConstants

class CurveFever() : PApplet() {

    companion object {

        fun run() {
            val cf = CurveFever()
            cf.runSketch()
        }
    }

    private var world = World()

    private val menu: Boolean
        get() = world.state == World.State.STOPPED || world.state == World.State.PAUSED

    private val selectionFields = 3

    private var playerMenu = false
    private var selectedPlayerIndex = 0
    private var selectedPlayerField = 0
    private var selectionActive = false

    override fun settings() {
        size(1280, 720)
    }

    override fun setup() {
        surface.setResizable(true)

        frameRate(240f)

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

        if (!playerMenu)
            drawHUD()
    }

    override fun keyPressed() {

        if (playerMenu) {
            when (key) {
                ESC -> playerMenu = false
                ENTER -> toggleSelection()
                '+' -> if (!selectionActive) world.addPlayer()
                '-' -> if (!selectionActive) {
                    world.removePlayer(selectedPlayerIndex)
                    if (selectedPlayerIndex > world.players.size - 1)
                        selectedPlayerIndex = world.players.size - 1
                }
            }

            when (keyCode) {
                LEFT -> selectionLeft()
                RIGHT -> selectionRight()
                UP -> selectionUp()
                DOWN -> selectionDown()
            }

            if (selectionActive) {
                when (selectedPlayerField) {
                    0 -> if (key == BACKSPACE)
                        world.players[selectedPlayerIndex].name = world.players[selectedPlayerIndex].name.dropLast(1)
                    else if (keyCode != SHIFT && key != ENTER && key != ESC)
                        world.players[selectedPlayerIndex].name += key
                    1 -> if (key != ENTER)
                        world.players[selectedPlayerIndex].setLeftKey(key, keyCode)
                    2 -> if (key != ENTER)
                        world.players[selectedPlayerIndex].setRightKey(key, keyCode)
                }
            }
        } else {
            when (key) {
                ' ' -> world.restart()
                ESC -> menu()
                in "Pp" -> if (world.state == World.State.STOPPED) playerMenu = true
            }

            world.players.forEach {
                when (keyCode) {
                    it.leftKeyCode -> it.leftPressed = true
                    it.rightKeyCode -> it.rightPressed = true
                }

                it.turn()
            }
        }

        key = ' '
        keyCode = 0
    }

    override fun keyReleased() {
        world.players.forEach {
            when (keyCode) {
                it.leftKeyCode -> it.leftPressed = false
                it.rightKeyCode -> it.rightPressed = false
            }

            it.turn()
        }
    }

    private fun update() {
        Time.update(world.state == World.State.PAUSED)
        Specs.width = width
        Specs.height = height
    }

    private fun drawMenu() {
        noStroke()
        fill(0, 100f)
        rect(0f, 0f, width.toFloat(), height.toFloat())

        if (playerMenu) {
            val rectWidth = width / 6f
            val rectHeight = height / 9f

            world.players.forEachIndexed { index, player ->

                //name
                setFill(player.color)
                textAlign(CENTER, CENTER)
                textSize(rectHeight / 2f)
                text(player.name, width / 2f - rectWidth * 1f, rectHeight * (index + 1f))

                //left key
                fill(200)
                text(player.leftKey, width / 2f + rectWidth * 0.5f, rectHeight * (index + 1f))

                //right key
                text(player.rightKey, width / 2f + rectWidth * 1.5f, rectHeight * (index + 1f))
            }

            //selection
            noFill()
            if (selectionActive)
                stroke(200)
            else
                stroke(100)
            strokeWeight(4f)
            val w = if (selectedPlayerField == 0) rectWidth * 2 else rectWidth
            val x = if (selectedPlayerField == 0) -2f else selectedPlayerField - 1f
            rect(width / 2 + rectWidth * x, rectHeight * (selectedPlayerIndex + 0.5f), w, rectHeight)

        } else if (world.state == World.State.STOPPED) {
            val text = """
                SPACE : restart
                P : manage players
                """.trimIndent()
            fill(200)
            textAlign(CENTER, CENTER)
            textSize(20f)
            text(text, width / 2f, height / 2f)
        }
    }

    private fun drawHUD() {
        textAlign(LEFT, TOP)
        textSize(14f)

        world.players.forEachIndexed { index, p ->
            setFill(p.color)
            text("${p.name} : ${p.score}", 10f, 10 + index * 20f)
        }

        if (world.state == World.State.STARTING) {
            fill(230)
            textAlign(CENTER, CENTER)
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

    private fun toggleSelection() {
        selectionActive = !selectionActive
    }

    private fun selectionLeft() {
        if (!selectionActive) {
            selectedPlayerField = floorMod(--selectedPlayerField, selectionFields)
        } else if (selectedPlayerField == 0) {
            world.players[selectedPlayerIndex].color = world.prevColor(selectedPlayerIndex)
        }
    }

    private fun selectionRight() {
        if (!selectionActive) {
            selectedPlayerField = (selectedPlayerField + 1) % selectionFields
        } else if (selectedPlayerField == 0) {
            world.players[selectedPlayerIndex].color = world.nextColor(selectedPlayerIndex)
        }
    }

    private fun selectionUp() {
        if (!selectionActive) {
            selectedPlayerIndex = floorMod(--selectedPlayerIndex, world.players.size)
        } else if (selectedPlayerField == 0) {
            world.players[selectedPlayerIndex].color = world.prevColor(selectedPlayerIndex)
        }
    }

    private fun selectionDown() {
        if (!selectionActive) {
            selectedPlayerIndex = (selectedPlayerIndex + 1) % world.players.size
        } else if (selectedPlayerField == 0) {
            world.players[selectedPlayerIndex].color = world.nextColor(selectedPlayerIndex)
        }
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
