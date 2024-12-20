package map
import javafx.embed.swing.SwingFXUtils
import java.io.File
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import javax.imageio.ImageIO
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.paint.Color



class Map(var height: Int, var width: Int) {

    private var grid: Array<BooleanArray> =
        Array(height) { BooleanArray(width) { false } }


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
            grid += Array(newHeight - height) { BooleanArray(width) { false } }
            height = newHeight
        }
    }

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

    // Загрузки карты из PNG
    fun loadMapFromPng(filePath: String) {
        /*val file = File(filePath)
        if (file.exists()) {
            // Загружаем изображение
            val image = Image(file.toURI().toString())
            val pixelReader: PixelReader = image.pixelReader


            for (y in 0 until height) {
                for (x in 0 until width) {
                    val color: Color = pixelReader.getColor(x, y)
                    // Считаем, что черный цвет означает заполненную ячейку, а белый — пустую
                    updateMap(x, y, color == Color.BLACK)
                }
            }
        } else {
            println("Файл не найден: $filePath")
        }
        */
    }

    fun loadMapFromCSV(filename: String) {
        val file = File(filename)
        var y = 0;
        if(file.exists()){
           file.forEachLine {line ->
               val values = line.split(",")
               for(i in values.indices){
                   updateMap(i, y, values[i].toInt() == 1)
               }
               y++
           }

        }
        else{
            println("Файл не найден: $filename")
        }
    }

    fun getCell(x: Int, y: Int): Boolean =
        if (x in 0..<width && y in 0..<height) {
            grid[y][x]
        } else {
            false // Или бросить исключение, если выход за границы недопустим
        }
}