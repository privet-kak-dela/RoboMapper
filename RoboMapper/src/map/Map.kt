package map

class Map {
    private val map: MutableList<MutableList<Int>> = mutableListOf()
    var height = 0
        private set
    var width = 0
        private set



    fun initMap(width: Int, height: Int) {
        this.height = height
        this.width = width
        repeat(height) {
            map.add(MutableList(width) { 0 }) // Инициализация ячеек со значением 0 (свободное пространство)
        }
    }

    fun updateMap(x: Int, y: Int, value: Int) {
        // Обновление значения ячейки карты
        map[y][x] = value
    }

    fun saveMap(filename: String) {
        // Сохранение карты в файл (например, в формате CSV)
    }

    fun loadMap(filename: String) {
        // Загрузка карты из файла
    }

    fun getCell(x: Int, y: Int): Int {
        return map[y][x]
    }
}