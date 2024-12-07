package map
import javafx.embed.swing.SwingFXUtils
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import java.io.File
import javafx.scene.image.WritableImage
import javax.imageio.ImageIO
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import javafx.scene.paint.Color


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
    fun saveMapAsPng(filePath: String) {
        val writableImage = WritableImage(grid[0].size, grid.size)
        for(y in grid.indices){
            for(x in grid[0].indices) {
                val color = if (grid[y][x] == 1) Color.BLACK else Color.WHITE
                writableImage.pixelWriter.setColor(x, y, color)
            }
        }
        val bufferedImage = SwingFXUtils.fromFXImage(writableImage, null)
        ImageIO.write(bufferedImage, "png", File(filePath))
    }

    // Метод для сохранения карты в CSV
    fun saveMapAsCsv(filePath: String) {
        val csvData = StringBuilder()
        for (y in grid.indices) {
            for (x in grid[0].indices) {
                csvData.append(if (getCell(x, y) != 0)  "1" else "0") // Преобразуем ячейку в "1" или "0"
                if (x < width - 1) csvData.append(",")
            }
            csvData.append("\n")
        }
        File(filePath).writeText(csvData.toString())
    }

    // Загрузки карты из PNG
    fun loadMapFromPng(filePath: String) {
        //clearMap()
        val file = File(filePath)
        if (file.exists()) {    
            // Загружаем изображение
            val image = Image(file.toURI().toString())
            if(!setMapSize(image.width.toInt(), image.height.toInt())){
                val alert = Alert(Alert.AlertType.NONE,"Карта больше допустимого размера", ButtonType.OK)
                alert.showAndWait()
                return
            }
            val pixelReader: PixelReader = image.pixelReader
            for (y in 0 until image.height.toInt()) {
                for (x in 0 until image.width.toInt()) {
                    val color: Color = pixelReader.getColor(x, y)
                    if(color == Color.WHITE)
                        continue
                    updateMap(x, y, 1)
                }
            }
        } else {
            println("Файл не найден: $filePath")
        }
    }

    fun loadMapFromCSV(filename: String) {
        //clearMap()
        val file = File(filename)
        var y = 0
        if(file.exists()){
            val lines = file.readLines()
            if(!setMapSize(lines[0].length - (lines[0].count { it == ',' }), lines.size)){
                val alert = Alert(Alert.AlertType.NONE,"Карта больше допустимого размера", ButtonType.OK)
                alert.showAndWait()
                return
            }
            for (line in lines) {
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

    // Очистка всей карты
    fun clearAll() {
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                grid[y][x] = 0
            }
        }
    }
    fun clearRobotPaths() {
        for (y in grid.indices) {
            for (x in grid[y].indices) {
                if (grid[y][x] == 2) {
                    grid[y][x] = 1
                }
            }

        }
    }

    private fun setMapSize(newWidth: Int, newHeight: Int): Boolean{
        if(newWidth * newHeight > 550*295 || newWidth > 818 || newHeight > 818) {
           return false
        }
        expandWidth(newWidth)
        expandHeight(newHeight)
        MapDisplay.canvas.width = width * 10.0
        MapDisplay.canvas.height = height * 10.0
        MapDisplay.canvas2.width = width * 10.0
        MapDisplay.canvas2.height = height * 10.0
        return true

    }
}