package map
import javafx.embed.swing.SwingFXUtils
import java.io.File
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javax.imageio.ImageIO

class Map(val height: Int, val width: Int) {

    private val grid: Array<BooleanArray> =
        Array(height) { BooleanArray(width) { false } }

    fun updateMap(x: Int, y: Int) {
        if (x in 0..<width && y in 0..<height)
            grid[y][x] = !grid[y][x]
    }

    fun updateMap(x: Int, y: Int, value: Boolean) {
        if (x in 0..<width && y in 0..<height)
            grid[y][x] = value
    }

    // Метод для сохранения карты в PNG
    fun saveMapAsPng(canvas: Canvas, filePath: String) {
        val writableImage = WritableImage(canvas.width.toInt(), canvas.height.toInt())
        canvas.snapshot(null, writableImage)
        val bufferedImage = SwingFXUtils.fromFXImage(writableImage, null)
        ImageIO.write(bufferedImage, "png", File(filePath))
    }

    // Метод для сохранения карты в CSV
    fun saveMapAsCsv(filePath: String) {
        val csvData = StringBuilder()
        for (y in 0 until height) {
            for (x in 0 until width) {
                csvData.append(if (getCell(x, y)) "1" else "0") // Преобразуем ячейку в "1" или "0"
                if (x < width - 1) csvData.append(",")
            }
            csvData.append("\n")
        }
        File(filePath).writeText(csvData.toString())
    }

    fun loadMap(filename: String) {
        // Загрузка карты из файла
    }

    fun getCell(x: Int, y: Int): Boolean =
        if (x in 0..<width && y in 0..<height) {
            grid[y][x]
        } else {
            false // Или бросить исключение, если выход за границы недопустим
        }
}