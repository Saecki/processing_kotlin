import processing.core.PApplet
import processing.core.PConstants
import processing.core.PVector

data class Tile(val color: Color) {

    enum class Color(val index: Int, val r: Int, val g: Int, val b: Int) {
        WHITE(0, 255, 255, 255),
        RED(1, 255, 0, 0),
        BLUE(2, 0, 0, 255),
        YELLOW(3, 255, 255, 0),
        ORANGE(4, 255, 150, 0),
        GREEN(5, 100, 255, 0)
    }
}

data class Face(val tiles: List<List<Tile>>) {

    val size: Int
        get() {
            return tiles.size
        }

    constructor(size: Int, color: Tile.Color) : this(List(size) {
        List(size) {
            Tile(Tile.Color.values().random())
        }
    })

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
            return faces.size
        }

    constructor(size: Int) : this(Tile.Color.values().map { color -> Face(size, color) })
}

class RubikCube(val size: Int) : PApplet() {

    companion object Factory {

        fun run(size: Int) {
            val rc = RubikCube(size)
            rc.runSketch()
        }
    }

    var cube = Cube(size)
    var faceLength = 0f
    var rotationZ = 0f
    var rotationX = PI / 4
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

        translate(width / 2f, height / 2f)
        rotateX(rotationX)
        rotateZ(rotationZ)
        pushMatrix()
        translate(-faceLength / 2f, -faceLength / 2f)

        background(200)
        drawCube(cube)

        popMatrix()

        rotationZ += 0.002f
    }

    override fun keyPressed() {
        if (keyCode == PConstants.SHIFT) {
            clockwise = false
        }

        if (key in "Uu") {
            cube = Cube(cube.faces.map { face -> face.rotated(clockwise) })
        }
    }

    override fun keyReleased() {
        if (keyCode == PConstants.SHIFT) {
            clockwise = true
        }
    }

    fun drawCube(cube: Cube) {
        drawFace(cube.faces[0], PVector(0f, 0f, 0f), PVector(faceLength, 0f, 0f), PVector(0f, faceLength, 0f))
        drawFace(cube.faces[1], PVector(0f, 0f, 0f), PVector(0f, faceLength, 0f), PVector(0f, 0f, faceLength))
        drawFace(cube.faces[2], PVector(0f, 0f, 0f), PVector(0f, 0f, faceLength), PVector(faceLength, 0f, 0f))
        drawFace(cube.faces[3], PVector(faceLength, faceLength, faceLength), PVector(-faceLength, 0f, 0f), PVector(0f, -faceLength, 0f))
        drawFace(cube.faces[4], PVector(faceLength, faceLength, faceLength), PVector(0f, -faceLength, 0f), PVector(0f, 0f, -faceLength))
        drawFace(cube.faces[5], PVector(faceLength, faceLength, faceLength), PVector(0f, 0f, -faceLength), PVector(-faceLength, 0f, 0f))
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
        vertex(p1.x, p1.y, p1.z)
        endShape()
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