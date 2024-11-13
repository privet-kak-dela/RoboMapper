package map

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.Stage
import kotlin.math.abs
import kotlin.math.sign
import javafx.scene.control.ChoiceDialog
import javafx.scene.control.ScrollPane
import javafx.scene.input.MouseEvent
import javafx.scene.input.KeyCode
import javafx.scene.layout.HBox
import javafx.stage.FileChooser
import robot.Robot
import java.io.File

class MapDisplay @JvmOverloads constructor(
    private val map: Map = Map(20, 20) // Если нужно, можно создать карту по умолчанию 20x20
) : Application() {
    private val robot: Robot = Robot(map)
    private val canvasSizeD = 10.0 // Размер ячейки в пикселях
    private val canvasSizeI = 10 // Размер ячейки в пикселях (Int)
    private var isSettingRobot = false // Флаг для режима установки робота

    override fun start(primaryStage: Stage) {
        val canvas = Canvas(map.width * canvasSizeD, map.height * canvasSizeD)
        val graphicsContext = canvas.graphicsContext2D
        var startX = 0.0
        var startY = 0.0

        var startCoordinates = doubleArrayOf(0.0, 0.0)

        // Отображение карты
        drawMap(graphicsContext, canvas)

        // Кнопка для установки робота в любое место по клику
        val setRobotButton = Button("Установить Робота")
        setRobotButton.setOnAction {
            isSettingRobot = true // Включаем режим установки робота
        }

        // Создаем кнопку "Сохранить"
        val saveButton = createSaveButton(primaryStage, canvas)
        // Создаем кнопку "Загрузить"
        val loadButton = createLoadButton(primaryStage, canvas)

        val menuBar = HBox(10.0, saveButton, loadButton, setRobotButton)
        val scrollPane = ScrollPane()
        scrollPane.content = canvas
        //scrollPane.isPannable = true // Включаем возможность перемещаться по карте

        val root = VBox(menuBar, scrollPane)
        root.spacing = 10.0
        scrollPane.prefWidthProperty().bind(root.widthProperty())
        scrollPane.prefHeightProperty().bind(root.heightProperty())

        // Устанавливаем сцену и отображаем окно
        primaryStage.scene = Scene(root, map.width * canvasSizeD, map.height * canvasSizeD + 40)
        primaryStage.title = "Map Display"
        primaryStage.show()

        primaryStage.scene.setOnKeyPressed { event ->
            when (event.text) {
//                KeyCode.UP -> robot.moveUp()
//                KeyCode.DOWN -> robot.moveDown()
//                KeyCode.LEFT -> robot.moveLeft()
//                KeyCode.RIGHT -> robot.moveRight()
                "w" -> robot.moveUp()
                "s" -> robot.moveDown()
                "a" -> robot.moveLeft()
                "d" -> robot.moveRight()
                else -> {}
            }
            drawMap(graphicsContext, canvas)
        }


        // Отслеживаем изменение размеров ScrollPane и увеличиваем карту
        scrollPane.widthProperty().addListener { _, _, newWidth ->
            adjustMapWidth(graphicsContext, canvas, newWidth.toDouble())
        }
        scrollPane.heightProperty().addListener { _, _, newHeight ->
            adjustMapHeight(graphicsContext, canvas, newHeight.toDouble())
        }

        canvas.setOnMousePressed { event -> handleMousePressed(event, graphicsContext, canvas, startCoordinates) }
        canvas.setOnMouseDragged { event -> handleMouseDragged(event, graphicsContext, canvas, startCoordinates) }
    }


    private fun handleMousePressed(event: MouseEvent, graphicsContext: GraphicsContext, canvas: Canvas, startCoordinates: DoubleArray) {
        startCoordinates[0] = event.x
        startCoordinates[1] = event.y

        if (isSettingRobot) {
            // Установка робота
            setRobotPosition(event)
        }
        else if (event.button == javafx.scene.input.MouseButton.PRIMARY) {
            map.updateMap(event.x.toInt() / canvasSizeI, event.y.toInt() / canvasSizeI, true)
        } else if (event.button == javafx.scene.input.MouseButton.SECONDARY) {
            map.updateMap(event.x.toInt() / canvasSizeI, event.y.toInt() / canvasSizeI, false)
        }

        drawMap(graphicsContext, canvas)
    }

    private fun handleMouseDragged(event: MouseEvent, graphicsContext: GraphicsContext, canvas: Canvas, startCoordinates: DoubleArray) {
        val flag = event.button == javafx.scene.input.MouseButton.PRIMARY

        // Обновляем карту по линии между предыдущей и текущей точками
        updateMapAlongLine(
            startCoordinates[0].toInt() / canvasSizeI,
            startCoordinates[1].toInt() / canvasSizeI,
            event.x.toInt() / canvasSizeI,
            event.y.toInt() / canvasSizeI,
            flag
        )

        // Обновляем стартовые координаты для следующего вызова handleMouseDragged
        startCoordinates[0] = event.x
        startCoordinates[1] = event.y

        drawMap(graphicsContext, canvas)
    }


    private fun setRobotPosition(event: MouseEvent) {
        val x = (event.x / canvasSizeD).toInt()
        val y = (event.y / canvasSizeD).toInt()
        if (map.getCell(x, y)) return
        if (x in 0 until map.width && y in 0 until map.height) {
            robot.PosX = x
            robot.PosY = y
            isSettingRobot = false
        }
    }


    private fun adjustMapWidth(graphicsContext: GraphicsContext, canvas: Canvas, newWidth: Double) {
        val newCols = (newWidth / canvasSizeD).toInt()
        if (newCols > map.width) {
            map.expandWidth(newCols)
            canvas.width = newCols * canvasSizeD
            drawMap(graphicsContext, canvas)
        }
    }

    private fun adjustMapHeight(graphicsContext: GraphicsContext, canvas: Canvas, newHeight: Double) {
        val newRows = (newHeight / canvasSizeD).toInt()
        if (newRows > map.height) {
            map.expandHeight(newRows)
            canvas.height = newRows * canvasSizeD
            drawMap(graphicsContext, canvas)
        }
    }


    private fun createSaveButton(primaryStage: Stage, canvas: Canvas): Button
    {
        val saveButton = Button("Сохранить")
        saveButton.setOnAction {
            // Диалог выбора формата файла
            val formats = listOf("PNG", "CSV")
            val choiceDialog = ChoiceDialog("PNG", formats)
            choiceDialog.title = "Выбор формата"
            choiceDialog.headerText = "Выберите формат для сохранения карты"
            val chosenFormat = choiceDialog.showAndWait()

            chosenFormat.ifPresent { format ->
                // Настройка диалога выбора файла
                val fileChooser = FileChooser()
                fileChooser.title = "Сохранить карту как $format"
                fileChooser.extensionFilters.add(
                    FileChooser.ExtensionFilter("$format Files", "*.${format.lowercase()}")
                )

                val file: File? = fileChooser.showSaveDialog(primaryStage)
                if (file != null) {
                    when (format) {
                        "PNG" -> map.saveMapAsPng(canvas, file.path)
                        "CSV" -> map.saveMapAsCsv(file.path)
                    }
                }
            }
        }
        return saveButton
    }

    private fun createLoadButton(primaryStage: Stage, canvas: Canvas): Button
    {
        val loadButton = Button("Загрузить")
        loadButton.setOnAction {
            // Диалог выбора формата файла
            val formats = listOf("PNG", "CSV")
            val choiceDialog = ChoiceDialog("PNG", formats)
            choiceDialog.title = "Выбор формата"
            choiceDialog.headerText = "Выберите формат для загрузки карты"
            val chosenFormat = choiceDialog.showAndWait()

            chosenFormat.ifPresent { format ->
                // Настройка диалога выбора файла
                val fileChooser = FileChooser()
                fileChooser.title = "Загрузить карту в формате $format"
                fileChooser.extensionFilters.add(
                    FileChooser.ExtensionFilter("$format Files", "*.${format.lowercase()}")
                )

                val file: File? = fileChooser.showOpenDialog(primaryStage)
                if (file != null) {
                    when (format) {
                        "PNG" -> map.loadMapFromPng(file.path)  // Загружаем карту в формате PNG
                        "CSV" -> {
                            //надо сделать функцию для загрузки карты в формате csv
                        }
                    }
                }
            }
        }
        return loadButton
    }


    private fun drawMap(graphicsContext: GraphicsContext, canvas: Canvas) {
        graphicsContext.clearRect(0.0, 0.0, canvas.width, canvas.height)
        for (y in 0..<map.height) {
            for (x in 0..<map.width) {
                graphicsContext.fill = if (map.getCell(x, y)) Color.BLACK else Color.WHITE
                graphicsContext.fillRect(x * canvasSizeD, y * canvasSizeD, canvasSizeD, canvasSizeD)
            }
        }
        if (!isSettingRobot) {
            graphicsContext.fill = Color.RED
            graphicsContext.fillRect(robot.PosX * canvasSizeD, robot.PosY * canvasSizeD, canvasSizeD, canvasSizeD)
        }
    }

    private fun updateMapAlongLine(x1: Int, y1: Int, x2: Int, y2: Int, flag: Boolean) {
        var x = x1
        var y = y1
        val dx = abs(x2 - x1)
        val dy = -abs(y2 - y1)
        val sx = (x2 - x1).sign
        val sy = (y2 - y1).sign
        var err = dx + dy

        while (true) {

            map.updateMap(x, y, flag)
            if (x == x2 && y == y2) break
            val e2 = 2 * err
            if (e2 >= dy) {
                err += dy
                x += sx
            }
            if (e2 <= dx) {
                err += dx
                y += sy
            }
        }
    }
}
