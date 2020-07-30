import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector
import peasy.PeasyCam


data class Tile(val color: Color) {

    enum class Color(val index: Int, val r: Int, val g: Int, val b: Int) {
        WHITE(0, 230, 230, 230),
        ORANGE(1, 240, 150, 0),
        BLUE(2, 50, 50, 230),
        YELLOW(3, 240, 240, 0),
        RED(4, 230, 0, 20),
        GREEN(5, 100, 220, 0)
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
        var newFaces = this.faces.toMutableList()

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

    private val baseFaceVectors = listOf(
        listOf(PVector(0f, 0f, 0f), PVector(1f, 0f, 0f), PVector(0f, 0f, 1f)),
        listOf(PVector(0f, 0f, 0f), PVector(0f, 0f, 1f), PVector(0f, 1f, 0f)),
        listOf(PVector(0f, 0f, 0f), PVector(0f, 1f, 0f), PVector(1f, 0f, 0f)),
        listOf(PVector(1f, 1f, 1f), PVector(-1f, 0f, 0f), PVector(0f, 0f, -1f)),
        listOf(PVector(1f, 1f, 1f), PVector(0f, 0f, -1f), PVector(0f, -1f, 0f)),
        listOf(PVector(1f, 1f, 1f), PVector(0f, -1f, 0f), PVector(-1f, 0f, 0f))
    )
    private val rotationSpeed = 0.04

    private var cube = Cube.ofSize(size)
    private var faceLength = 0f

    private var up = false
    private var down = false
    private var right = false
    private var left = false

    private var clockwise = true

    private lateinit var cam: PeasyCam
    private lateinit var faceVectors: List<List<PVector>>
    private lateinit var screenFaceCoords: List<List<PVector>>

    override fun settings() {
        size(800, 600, PConstants.P3D)
    }

    override fun setup() {
        surface.setResizable(true)
        stroke(50)

        faceLength = (height + width).toFloat() / 8f

        cam = PeasyCam(this, faceLength * 2.0)
        cam.wheelHandler = null
        cam.setRightDragHandler(null)
        cam.setCenterDragHandler(null)
    }

    override fun draw() {

        faceLength = (height + width).toFloat() / 8f
        faceVectors = baseFaceVectors.map {
            it.map { vector ->
                vector.copy().mult(faceLength)
            }
        }

        cam.distance = faceLength * 2.0

        if (down) cam.rotateX(rotationSpeed)
        if (up) cam.rotateX(-rotationSpeed)
        if (left) cam.rotateY(rotationSpeed)
        if (right) cam.rotateY(-rotationSpeed)

        strokeWeight(faceLength / 40)
        translate(-faceLength / 2f, -faceLength / 2f, -faceLength / 2f)

        background(50)
        drawCube(cube)

        screenFaceCoords = faceVectors.map {
            listOf(
                screenVector(it[0]),
                screenVector(
                    it[0].copy()
                        .add(it[1])
                ),
                screenVector(
                    it[0].copy()
                        .add(it[1])
                        .add(it[2])
                ),
                screenVector(
                    it[0].copy()
                        .add(it[2])
                )
            )
        }
    }

    override fun keyPressed() {

        when (keyCode) {
            PConstants.SHIFT -> clockwise = false
            PConstants.UP -> up = true
            PConstants.DOWN -> down = true
            PConstants.RIGHT -> right = true
            PConstants.LEFT -> left = true
            'U'.toInt() -> cube = cube.rotatedY(clockwise, 0)
            'D'.toInt() -> cube = cube.rotatedY(!clockwise, size - 1)
            'R'.toInt() -> cube = cube.rotatedX(!clockwise, size - 1)
            'L'.toInt() -> cube = cube.rotatedX(clockwise, 0)
            'F'.toInt() -> cube = cube.rotatedZ(!clockwise, size - 1)
            'B'.toInt() -> cube = cube.rotatedZ(clockwise, 0)
            'N'.toInt() -> cube = Cube.ofSize(size)
            'O'.toInt() -> ortho()
            'P'.toInt() -> perspective()
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

    override fun mousePressed() {
        val mouseVector = PVector(mouseX.toFloat(), mouseY.toFloat())
        val insideFaces = screenFaceCoords.map { insidePolygon(mouseVector, it) }
        for (i in insideFaces.indices) {
            if (insideFaces[i])
                println(Tile.Color.values()[i])
        }
    }

    private fun insidePolygon(point: PVector, polygon: List<PVector>): Boolean {
        if (polygon.size < 3) return false

        for (i in 0 until polygon.size - 3) {
            if (insideTriangle(point, polygon[i], polygon[i + 1], polygon[i + 2])) {
                return true
            }
        }

        return false
    }

    private fun insideTriangle(p0: PVector, p1: PVector, p2: PVector, p3: PVector): Boolean {
        val p1P2 = dist(p1, p2)
        val p2P3 = dist(p2, p3)
        val p3P1 = dist(p3, p1)

        val p0P1 = dist(p0, p1)
        val p0P2 = dist(p0, p2)
        val p0P3 = dist(p0, p3)

        val areaCombined = areaOfTriangle(p1P2, p2P3, p3P1)
        val area1 = areaOfTriangle(p0P1, p1P2, p0P2)
        val area2 = areaOfTriangle(p0P2, p2P3, p0P3)
        val area3 = areaOfTriangle(p0P3, p3P1, p0P1)

        return (areaCombined >= area1 + area2 + area3)
    }

    private fun areaOfTriangle(a: Float, b: Float, c: Float): Float {
        val s = (a + b + c) / 2
        return s * (s - a) * (s - b) * (s - c)
    }

    private fun dist(v1: PVector, v2: PVector) = dist(v1.x, v1.y, v2.x, v2.y)

    private fun screenX(vector: PVector) = screenX(vector.x, vector.y, vector.z)

    private fun screenY(vector: PVector) = screenY(vector.x, vector.y, vector.z)

    private fun screenVector(vector: PVector) = PVector(screenX(vector), screenY(vector))

    private fun drawCube(cube: Cube) {
        for (i in cube.faces.indices) {
            drawFace(cube.faces[i], faceVectors[i][0], faceVectors[i][1], faceVectors[i][2])
        }
    }

    private fun drawFace(face: Face, start: PVector, edge1: PVector, edge2: PVector) {

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

    private fun drawTile(tile: Tile, p1: PVector, p2: PVector, p3: PVector, p4: PVector) {
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
