package map

import kotlin.collections.Map

class MapDisplay(private val map: Map) : Application() {
    override fun start(primaryStage: Stage) {
        val canvas = Canvas(map.width * 10.0, map.height * 10.0) // Размер ячейки 10 пикселей
        val graphicsContext = canvas.graphicsContext2D

        for (y in 0..<map.height) {
            for (x in 0..<map.width) {
                when (map.getCell(x, y)) {
                    0 -> graphicsContext.fill = Color.WHITE // Свободное пространство
                    1 -> graphicsContext.fill = Color.BLACK // Препятствие
                    // ... другие типы клеток
                }
                graphicsContext.fillRect(x * 10.0, y * 10.0, 10.0, 10.0)
            }
        }

        val root = StackPane(canvas)
        primaryStage.scene = Scene(root, map.width * 10.0, map.height * 10.0)
        primaryStage.show()
    }
}