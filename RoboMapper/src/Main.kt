import javafx.application.Application
import javafx.stage.Stage
import map.*


// Отображение карты
class Main : Application() {

    override fun start(primaryStage: Stage) {
        val map = Map() // Создаем экземпляр класса Map
        map.initMap(20, 20) // Инициализируем карту размером 20x20

        val mapDisplay = MapDisplay(map)
        mapDisplay.start(primaryStage)
    }
}

fun main() {
    /*val map = Map()
    map.initMap(10, 10)

    // Обновление карты
    map.updateMap(5, 5, 1) // Пометить ячейку с координатами (5, 5) как препятствие

    // Сохранение карты
    map.saveMap("my_map.txt")*/

    // Загрузка карты
    /*val loadedMap = Map()
    loadedMap.loadMap("my_map.txt")*/

    // Отображение карты
    Application.launch(Main::class.java)
}