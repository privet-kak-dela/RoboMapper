package map

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

    fun saveMap(filename: String) {
        // Сохранение карты в файл (например, в формате CSV)
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