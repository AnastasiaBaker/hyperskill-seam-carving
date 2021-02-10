package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.pow
import kotlin.math.sqrt


fun main(args: Array<String>) {
    val pic = SeamCarving(ImageIO.read(File(args[args.indexOf("-in") + 1])))

    repeat(args[args.indexOf("-width") + 1].toInt()) { pic.removeSeam() }

    pic.transpose()

    repeat(args[args.indexOf("-height") + 1].toInt()) { pic.removeSeam() }

    pic.transpose()

    val clipped = BufferedImage(pic.width, pic.height, pic.img.type)

    clipped.setRGB(0, 0, pic.width, pic.height, pic.pixels.toRgbArray(), 0, pic.width)

    ImageIO.write(clipped, "png", File(args[args.indexOf("-out") + 1]))
}

class Pixel(var x: Int, var y: Int, val rgb: Int, var nrg: Double, var dst: Double)

class SeamCarving(var img: BufferedImage) {
    private val recountNRG = mutableListOf<Pixel>()
    val pixels = mutableListOf<MutableList<Pixel>>()
    var width = img.width
    var height = img.height

    init {
        for (y in 0 until height) {
            pixels.add(y, mutableListOf())

            for (x in 0 until width) { pixels[y].add(x, Pixel(x, y, img.getRGB(x, y), pixelEnergy(x, y, true), -1.0)) }
        }
    }

    fun removeSeam() {
        val seam = mutableListOf<Pixel>()

        recountNRG.forEach { it.nrg = pixelEnergy(it.x, it.y, false) }.also { recountNRG.clear() }

        pixels.getDist()

        seam.add(pixels[height - 1].minByOrNull { it.dst }!!)

        for (i in height - 1 downTo 1) {
            seam.add(pixels.getNeighbors(seam.last().x, seam.last().y - 1).minByOrNull { it.dst }!!)
        }

        seam.forEach {
            if (it.x != 0) { recountNRG.add(pixels[it.y][it.x - 1]) }
            if (it.x != width - 1) recountNRG.add(pixels[it.y][it.x + 1])
            if (it.x == 2) recountNRG.add(pixels[it.y][0])
            if (it.x == width - 3) recountNRG.add(pixels[it.y][width - 1])

            for (x in it.x until width - 1) {
                pixels[it.y][x] = pixels[it.y][x + 1]
                pixels[it.y][x].x --
            }

            pixels[it.y].removeLast()
        }

        width --
    }

    private fun MutableList<MutableList<Pixel>>.getDist() {
        val queue = mutableListOf<MutableSet<Pixel>>()

        pixels.forEachIndexed { i, l ->
            queue.add(i, mutableSetOf())

            l.forEach { it.dst = if (i == 0) it.nrg else -1.0 }
        }

        queue[0].addAll(pixels[0])

        for (y in 0 until height - 1) {
            val temp = queue[y].sortedBy { it.dst }

            temp.forEach { p -> this.getNeighbors(p.x, p.y + 1)
                .forEach {
                    queue[y + 1].add(it)

                    (p.dst + it.nrg).also { d -> if (it.dst == -1.0 || d < it.dst) it.dst = d }
                }
            }
        }
    }

    private fun pixelEnergy(x: Int, y: Int, fromIMG: Boolean): Double {
        val x1 = if (x == 0) 1 else if (x == width - 1) x - 1 else x
        val y1 = if (y == 0) 1 else if (y == height - 1) y - 1 else y
        val a = Color(if (fromIMG) img.getRGB(x1 - 1, y) else pixels[y][x1 - 1].rgb)
        val b = Color(if (fromIMG) img.getRGB(x1 + 1, y) else pixels[y][x1 + 1].rgb)
        val c = Color(if (fromIMG) img.getRGB(x, y1 - 1) else pixels[y1 - 1][x].rgb)
        val d = Color(if (fromIMG) img.getRGB(x, y1 + 1) else pixels[y1 + 1][x].rgb)

        return sqrt(sqGradient(a, b) + sqGradient(c, d))
    }

    private fun sqGradient(rgb1: Color, rgb2: Color): Double {
        return (rgb1.red - rgb2.red).toDouble().pow(2.0) +
                (rgb1.green - rgb2.green).toDouble().pow(2.0) +
                (rgb1.blue - rgb2.blue).toDouble().pow(2.0)
    }

    private fun MutableList<MutableList<Pixel>>.getNeighbors(x: Int, y: Int): List<Pixel> {
        val neighbors = mutableListOf(this[y][x])

        if (x != 0) neighbors.add(this[y][x - 1])
        if (x != width - 1) neighbors.add(this[y][x + 1])

        return neighbors.toList()
    }

    fun transpose() {
        val result = mutableListOf<MutableList<Pixel>>()

        for (m in pixels[0].indices) {
            result.add(m, mutableListOf())

            for (n in pixels.indices) {
                val pixel = pixels[n][m]

                pixel.x = n
                pixel.y = m

                result[m].add(n, pixels[n][m])
            }
        }

        pixels.clear()

        result.forEachIndexed { i, l -> pixels.add(i, l) }

        width = pixels[0].lastIndex + 1
        height = pixels.lastIndex + 1
    }
}

fun MutableList<MutableList<Pixel>>.toRgbArray(): IntArray {
    val list = mutableListOf<Int>()

    this.forEach { list.addAll(it.map { p -> p.rgb }) }

    return list.toIntArray()
}
