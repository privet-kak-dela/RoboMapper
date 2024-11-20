package map
import javafx.embed.swing.SwingFXUtils
import java.io.File
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javax.imageio.ImageIO
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.paint.Color
import javax.swing.plaf.synth.ColorType


class Map(var height: Int, var width: Int) {

    private var grid: Array<IntArray> =
        Array(height) { IntArray(width) { 0 } }

    fun countOnes(): Int {
        var count = 0
        for (i in grid.indices) { // Проходим по строкам
            for (j in grid[i].indices) { // Проходим по столбцам
                if (grid[i][j] == 1) { // Проверяем, является ли элемент единицей
                    count++
                }
            }
        }
        return count
    }

    fun countTwos(): Int {
        var count = 0
        for (i in grid.indices) { // Проходим по строкам
            for (j in grid[i].indices) { // Проходим по столбцам
                if (grid[i][j] == 2) { // Проверяем, является ли элемент двойкой
                    count++
                }
            }
        }
        return count
    }
    fun expandWidth(newWidth: Int) {
        if (newWidth > width) {
            for (y in grid.indices) {
                grid[y] = grid[y].copyOf(newWidth)
            }
            width = newWidth
        }
    }

    fun expandHeight(newHeight: Int) {
        if (newHeight > height) {
            grid += Array(newHeight - height) { IntArray(width) { 0 } }
            height = newHeight
        }
    }

    //fun updateMap(x: Int, y: Int) {
    //    if (x in 0..<width && y in 0..<height)
    //        grid[y][x] = !grid[y][x]
    //}

    fun updateMap(x: Int, y: Int, value: Int) {
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
                csvData.append(if (getCell(x, y) != 0)  "1" else "0") // Преобразуем ячейку в "1" или "0"
                if (x < width - 1) csvData.append(",")
            }
            csvData.append("\n")
        }
        File(filePath).writeText(csvData.toString())
    }

    // Загрузки карты из PNG
    fun loadMapFromPng(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            // Загружаем изображение
            val image = Image(file.toURI().toString())
            val pixelReader: PixelReader = image.pixelReader
            for (y in 0 until height) {
                for (x in 0 until width) {
                    val color: Color = pixelReader.getColor(x*10, y*10)
                    println(color)
                    if(color.red == 1.0)
                        continue
                    updateMap(x, y, 1)
                }
            }
            println(grid)
        } else {
            println("Файл не найден: $filePath")
        }
    }

    fun loadMapFromCSV(filename: String) {
        val file = File(filename)
        var y = 0;
        if(file.exists()){
           file.forEachLine {line ->
               val values = line.split(",")
               for(i in values.indices){
                   updateMap(i, y, values[i].toInt())
               }
               y++
           }

        }
        else{
            println("Файл не найден: $filename")
        }
    }

    fun getCell(x: Int, y: Int): Int =
        if (x in 0..<width && y in 0..<height) {
            grid[y][x]
        } else {
            -1 // Или бросить исключение, если выход за границы недопустим
        }
}