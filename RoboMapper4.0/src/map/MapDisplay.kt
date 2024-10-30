package map

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.layout.StackPane
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.abs
import kotlin.math.sign


class MapDisplay(private val map: Map) : Application() {
    private val canvasSizeD = 10.0 // Размер ячейки в пикселях (Double)
    private val canvasSizeI = 10 // Размер ячейки в пикселях (Int)

    override fun start(primaryStage: Stage) {

        val canvas = Canvas(map.width * canvasSizeD, map.height * canvasSizeD) // Создаем Canvas нужного размера
        val graphicsContext = canvas.graphicsContext2D // Получаем GraphicsContext для рисования

        var startX = 0.0 // Начальная координата X для рисования линии
        var startY = 0.0 // Начальная координата Y для рисования линии

        // Инициализация карты
        drawMap(graphicsContext, canvas) // Рисуем карту

        // Создаем сцену и отображаем окно
        val root = StackPane(canvas)
        primaryStage.scene = Scene(root, map.width * canvasSizeD, map.height * canvasSizeD)
        primaryStage.show()

        // Обработчик нажатия мыши
        canvas.setOnMousePressed { event ->
            startX = event.x // Запоминаем координаты начала линии
            startY = event.y
            map.updateMap(event.x.toInt() / canvasSizeI, event.y.toInt() / canvasSizeI) // Обновляем значение ячейки карты
            drawMap(graphicsContext, canvas) // Перерисовываем карту
        }

        // Обработчик перетаскивания мыши
        canvas.setOnMouseDragged { event ->
            graphicsContext.strokeLine(startX, startY, event.x, event.y) // Рисуем линию
            // Обновляем карту вдоль линии
            updateMapAlongLine(startX.toInt() / canvasSizeI,
                startY.toInt() / canvasSizeI,
                event.x.toInt() / canvasSizeI,
                event.y.toInt() / canvasSizeI
            )
            startX = event.x // Обновляем координаты начала линии
            startY = event.y
            drawMap(graphicsContext, canvas) // Перерисовываем карту
        }
    }

    // Функция для рисования карты
    private fun drawMap(graphicsContext: GraphicsContext, canvas: Canvas) {
        // Перерисовываем всю карту
        graphicsContext.clearRect(0.0, 0.0, canvas.width, canvas.height) // Очищаем Canvas
        // Проходим по каждой ячейке карты
        for (y in 0..<map.height) {
            for (x in 0..<map.width) {
                // Устанавливаем цвет заливки в зависимости от значения ячейки (препятствие или свободное пространство)
                graphicsContext.fill = when (map.getCell(x, y)) {
                    false -> Color.WHITE // Свободное пространство
                    true -> Color.BLACK // Препятствие
                }
                // Рисуем прямоугольник, соответствующий ячейке карты
                graphicsContext.fillRect(x * canvasSizeD, y * canvasSizeD, canvasSizeD, canvasSizeD)
            }
        }
    }

    // Функция для обновления значений ячеек карты вдоль линии (алгоритм Брезенхема)
    private fun updateMapAlongLine(x1: Int, y1: Int, x2: Int, y2: Int) {
        var x = x1
        var y = y1
        // Вычисляются разности по осям X и Y (dx и dy)
        val dx = abs(x2 - x1)
        val dy = -abs(y2 - y1)
        // Определяются знаки инкрементов по осям (sx и sy).
        val sx = (x2 - x1).sign // val sx = if (x1 < x2) 1 else -1
        val sy = (y2 - y1).sign // val sy = if (y1 < y2) 1 else -1
        // Эта переменная показывает, насколько текущая точка отклоняется от идеальной линии.
        var err = dx + dy

        while (true) {
            //Помечается текущая точка как принадлежащая линии.
            map.updateMap(x, y, true) // Устанавливаем значение true (препятствие) для текущей ячейки
            if (x == x2 && y == y2) break // Если достигли конца линии, выходим из цикла
            val e2 = 2 * err
            //Если ошибка больше половины dy, то следующая точка будет выше
            if (e2 >= dy) {
                err += dy
                x += sx
            }
            //Если ошибка меньше половины dx, то следующая точка будет правее
            if (e2 <= dx) {
                err += dx
                y += sy
            }
        }
    }
}