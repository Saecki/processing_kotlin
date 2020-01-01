import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

data class Tile(val color: Color) {

    enum class Color(val index: Int, val r: Int, val g: Int, val b: Int) {
        WHITE(0, 255, 255, 255),
        ORANGE(1, 255, 150, 0),
        BLUE(2, 0, 0, 255),
        YELLOW(3, 255, 255, 0),
        RED(4, 255, 0, 0),
        GREEN(5, 100, 255, 0)
    }
}

data class Face(val tiles: List<List<Tile>>) {

    val size: Int
        get() {
            return tiles.size
        }

    constructor(size: Int, color: Tile.Color) : this(List(size) { List(size) { Tile(color) } })

    fun rotated(clockwise: Boolean): Face {
        return Face(
            tiles.mapIndexed { i, list ->
                list.mapIndexed { j, _ ->
                    val k = (i * size + j)
                    if (clockwise)
                        tiles[k % size][size - k / size - 1]
                    else
                        tiles[size - k % size - 1][k / size]
                }
            })
    }
}

data class Cube(val faces: List<Face>) {

    val size: Int
        get() {
            return faces[0].size
        }

    companion object {
        fun ofSize(size: Int): Cube {
            return Cube(Tile.Color.values().map { color -> Face(size, color) })
        }

    }

    fun rotatedY(clockwise: Boolean, layer: Int): Cube {
        val newFaces = this.faces.toMutableList()

        when (layer) {
            0 -> newFaces[0] = faces[0].rotated(clockwise)
            size - 1 -> newFaces[3] = faces[3].rotated(clockwise)
        }

        val f1: MutableList<MutableList<Tile>> = faces[1].tiles.map { it.toMutableList() }.toMutableList()
        val f2: MutableList<MutableList<Tile>> = faces[2].tiles.map { it.toMutableList() }.toMutableList()
        val f4: MutableList<MutableList<Tile>> = faces[4].tiles.map { it.toMutableList() }.toMutableList()
        val f5: MutableList<MutableList<Tile>> = faces[5].tiles.map { it.toMutableList() }.toMutableList()

        if (clockwise) {
            for (i in 0 until size) {
                val t = f5[size - layer - 1][size - i - 1]
                f5[size - layer - 1][size - i - 1] = f4[i][size - layer - 1]
                f4[i][size - layer - 1] = f2[layer][size - i - 1]
                f2[layer][size - i - 1] = f1[i][layer]
                f1[i][layer] = t
            }
        } else {
            for (i in 0 until size) {
                val t = f1[i][layer]
                f1[i][layer] = f2[layer][size - i - 1]
                f2[layer][size - i - 1] = f4[i][size - layer - 1]
                f4[i][size - layer - 1] = f5[size - layer - 1][size - i - 1]
                f5[size - layer - 1][size - i - 1] = t
            }
        }

        newFaces[1] = Face(f1)
        newFaces[2] = Face(f2)
        newFaces[4] = Face(f4)
        newFaces[5] = Face(f5)

        return Cube(newFaces)
    }

    fun rotatedX(clockwise: Boolean, layer: Int): Cube {
        val newFaces = this.faces.toMutableList()

        when (layer) {
            0 -> newFaces[1] = faces[1].rotated(clockwise)
            size - 1 -> newFaces[4] = faces[4].rotated(clockwise)
        }

        val f0: MutableList<MutableList<Tile>> = faces[0].tiles.map { it.toMutableList() }.toMutableList()
        val f2: MutableList<MutableList<Tile>> = faces[2].tiles.map { it.toMutableList() }.toMutableList()
        val f3: MutableList<MutableList<Tile>> = faces[3].tiles.map { it.toMutableList() }.toMutableList()
        val f5: MutableList<MutableList<Tile>> = faces[5].tiles.map { it.toMutableList() }.toMutableList()

        if (clockwise) {
            for (i in 0 until size) {
                val t = f0[layer][i]
                f0[layer][i] = f2[size - i - 1][layer]
                f2[size - i - 1][layer] = f3[size - layer - 1][i]
                f3[size - layer - 1][i] = f5[size - i - 1][size - layer - 1]
                f5[size - i - 1][size - layer - 1] = t
            }
        } else {
            for (i in 0 until size) {
                val t = f5[size - i - 1][size - layer - 1]
                f5[size - i - 1][size - layer - 1] = f3[size - layer - 1][i]
                f3[size - layer - 1][i] = f2[size - i - 1][layer]
                f2[size - i - 1][layer] = f0[layer][i]
                f0[layer][i] = t
            }
        }

        newFaces[0] = Face(f0)
        newFaces[2] = Face(f2)
        newFaces[3] = Face(f3)
        newFaces[5] = Face(f5)

        return Cube(newFaces)
    }

    fun rotatedZ(clockwise: Boolean, layer: Int): Cube {
        val newFaces = this.faces.toMutableList()

        when (layer) {
            0 -> newFaces[2] = faces[2].rotated(clockwise)
            size - 1 -> newFaces[5] = faces[5].rotated(clockwise)
        }

        val f0: MutableList<MutableList<Tile>> = faces[0].tiles.map { it.toMutableList() }.toMutableList()
        val f1: MutableList<MutableList<Tile>> = faces[1].tiles.map { it.toMutableList() }.toMutableList()
        val f3: MutableList<MutableList<Tile>> = faces[3].tiles.map { it.toMutableList() }.toMutableList()
        val f4: MutableList<MutableList<Tile>> = faces[4].tiles.map { it.toMutableList() }.toMutableList()

        if (clockwise) {
            for (i in 0 until size) {
                val t = f4[size - layer - 1][size - i - 1]
                f4[size - layer - 1][size - i - 1] = f3[i][size - layer - 1]
                f3[i][size - layer - 1] = f1[layer][size - i - 1]
                f1[layer][size - i - 1] = f0[i][layer]
                f0[i][layer] = t
            }
        } else {
            for (i in 0 until size) {
                val t = f0[i][layer]
                f0[i][layer] = f1[layer][size - i - 1]
                f1[layer][size - i - 1] = f3[i][size - layer - 1]
                f3[i][size - layer - 1] = f4[size - layer - 1][size - i - 1]
                f4[size - layer - 1][size - i - 1] = t
            }
        }

        newFaces[0] = Face(f0)
        newFaces[1] = Face(f1)
        newFaces[3] = Face(f3)
        newFaces[4] = Face(f4)

        return Cube(newFaces)
    }
}

class RubikCube(val size: Int) : PApplet() {

    companion object Factory {

        fun run(size: Int) {
            val rc = RubikCube(size)
            rc.runSketch()
        }
    }

    val rotationSpeed = 0.04f

    var cube = Cube.ofSize(size)//Cube(Tile.Color.values().map { color -> Face(List(size) { List(size) { Tile(Tile.Color.values().random()) } }) })
    var faceLength = 0f

    var up = false
    var down = false
    var right = false
    var left = false

    var rotationY = 0f
    var rotationX = -PI / 8f
    var clockwise = true

    override fun settings() {
        size(800, 600, PConstants.P3D)
        smooth(4)
    }

    override fun setup() {
        surface.setResizable(true)
        stroke(50)
        strokeWeight(3f)
    }

    override fun draw() {
        faceLength = (height + width).toFloat() / 8f
        if (up)
            rotationX += rotationSpeed
        if (down)
            rotationX -= rotationSpeed
        if (right)
            rotationY += rotationSpeed
        if (left)
            rotationY -= rotationSpeed

        translate(width / 2f, height / 2f)
        rotateX(rotationX)
        rotateY(rotationY)
        pushMatrix()
        translate(-faceLength / 2f, -faceLength / 2f, -faceLength / 2f)

        background(50)
        drawCube(cube)

        popMatrix()
    }

    override fun keyPressed() {
        when (keyCode) {
            PConstants.SHIFT -> clockwise = false
            PConstants.UP -> up = true
            PConstants.DOWN -> down = true
            PConstants.RIGHT -> right = true
            PConstants.LEFT -> left = true
        }

        when (key) {
            in "Uu" -> cube = cube.rotatedY(clockwise, 0)
            in "Dd" -> cube = cube.rotatedY(!clockwise, size - 1)
            in "Rr" -> cube = cube.rotatedX(!clockwise, size - 1)
            in "Ll" -> cube = cube.rotatedX(clockwise, 0)
            in "Ff" -> cube = cube.rotatedZ(!clockwise, size - 1)
            in "Bb" -> cube = cube.rotatedZ(clockwise, 0)
            in "Nn" -> cube = Cube.ofSize(size)
        }
    }

    override fun keyReleased() {
        when (keyCode) {
            PConstants.SHIFT -> clockwise = true
            PConstants.UP -> up = false
            PConstants.DOWN -> down = false
            PConstants.RIGHT -> right = false
            PConstants.LEFT -> left = false
        }
    }

    fun drawCube(cube: Cube) {
        drawFace(cube.faces[0], PVector(0f, 0f, 0f), PVector(faceLength, 0f, 0f), PVector(0f, 0f, faceLength))
        drawFace(cube.faces[1], PVector(0f, 0f, 0f), PVector(0f, 0f, faceLength), PVector(0f, faceLength, 0f))
        drawFace(cube.faces[2], PVector(0f, 0f, 0f), PVector(0f, faceLength, 0f), PVector(faceLength, 0f, 0f))
        drawFace(cube.faces[3], PVector(faceLength, faceLength, faceLength), PVector(-faceLength, 0f, 0f), PVector(0f, 0f, -faceLength))
        drawFace(cube.faces[4], PVector(faceLength, faceLength, faceLength), PVector(0f, 0f, -faceLength), PVector(0f, -faceLength, 0f))
        drawFace(cube.faces[5], PVector(faceLength, faceLength, faceLength), PVector(0f, -faceLength, 0f), PVector(-faceLength, 0f, 0f))
    }

    fun drawFace(face: Face, start: PVector, edge1: PVector, edge2: PVector) {
        for (i in 0 until face.size) {
            for (j in 0 until face.size) {
                val p1 = start.copy()
                    .add(edge1.copy().mult(i.toFloat() / face.size.toFloat()))
                    .add(edge2.copy().mult(j.toFloat() / face.size.toFloat()))

                val p2 = start.copy()
                    .add(edge1.copy().mult((i + 1).toFloat() / face.size.toFloat()))
                    .add(edge2.copy().mult(j.toFloat() / face.size.toFloat()))

                val p3 = start.copy()
                    .add(edge1.copy().mult((i + 1).toFloat() / face.size.toFloat()))
                    .add(edge2.copy().mult((j + 1).toFloat() / size.toFloat()))

                val p4 = start.copy()
                    .add(edge1.copy().mult(i.toFloat() / face.size.toFloat()))
                    .add(edge2.copy().mult((j + 1).toFloat() / face.size.toFloat()))

                drawTile(face.tiles[i][j], p1, p2, p3, p4)
            }
        }
    }

    fun drawTile(tile: Tile, p1: PVector, p2: PVector, p3: PVector, p4: PVector) {
        fill(tile.color.r.toFloat(), tile.color.g.toFloat(), tile.color.b.toFloat())
        beginShape()
        vertex(p1.x, p1.y, p1.z)
        vertex(p2.x, p2.y, p2.z)
        vertex(p3.x, p3.y, p3.z)
        vertex(p4.x, p4.y, p4.z)
        endShape(PConstants.CLOSE)
    }
}

fun main(args: Array<String>) {
    var size = 3
    try {
        size = args[0].toInt()
    } catch (e: NumberFormatException) {
        print("Couldn't cast the size argument")
        e.printStackTrace()
    }

    println("creating cube of size: $size")
    RubikCube.run(size)
}
