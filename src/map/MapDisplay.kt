package map

import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import robot.Direction
import robot.Robot
import robot.Station
import java.io.File
import kotlin.math.abs
import kotlin.math.sign


class MapDisplay @JvmOverloads constructor(
    private val map: Map = Map(20, 20) // Если нужно, можно создать карту по умолчанию 20x20
) : Application() {
    private val robot: Robot = Robot(map)
    private val canvasSizeD = 10.0 // Размер ячейки в пикселях
    private val canvasSizeI = 10 // Размер ячейки в пикселях (Int)
    private var isSettingRobot = false // Флаг для режима установки робота
    private var isEditing = false // Флаг для режима редактирования карты
    private val dynamicText = SimpleStringProperty("0")
    companion object {
        var canvas = Canvas()
        var canvas2 = Canvas()
    }
    private var station: Station? = null
    override fun start(primaryStage: Stage) {
        val drawingPart = Canvas(map.width * canvasSizeD, map.height * canvasSizeD)
        val scanningPart= Canvas(map.width * canvasSizeD, map.height * canvasSizeD)
        val graphicsContext = drawingPart.graphicsContext2D
        val graphicsContext2 = scanningPart.graphicsContext2D

        var startCoordinates = doubleArrayOf(0.0, 0.0)

        // Отображение карты

        drawMap(graphicsContext, canvas)
        hideMap(graphicsContext2, canvas2)



        // Кнопка для установки робота в любое место по клику
        val setRobotButton = Button("Установить Робота")
        setRobotButton.setOnAction {
            isSettingRobot = true
                //hideMap(graphicsContext, canvas)

        }

        // Создаем кнопку "Сохранить"
        val saveButton = createSaveButton(primaryStage, drawingPart)
        // Создаем кнопку "Загрузить"
        val loadButton = createLoadButton(primaryStage, drawingPart)
        val loadButton = createLoadButton(primaryStage, canvas)
        // Кнопка "Редактировать"
        val editButton = ToggleButton("Редактировать")
        editButton.setOnAction {
            isEditing = editButton.isSelected
            editButton.text = if (isEditing) "Режим редактирования включен" else "Редактировать"
            canvas.isDisable = !isEditing // Отключаем/включаем взаимодействие с картой
        }
        // Кнопка "Подтвердить"
        val confirmButton = Button("Подтвердить")
        confirmButton.setOnAction {
            isEditing = false
            editButton.isSelected = false
            editButton.text = "Редактировать"
            canvas.isDisable = true // Полностью отключаем редактирование карты
        }

        var exploreText = createExploreText(primaryStage, drawingPart)

        val menuBar = HBox(10.0, saveButton, loadButton, setRobotButton,editButton, confirmButton, exploreText)

        val scrollPane = ScrollPane()
        scrollPane.content = drawingPart

        val scrollPane2 = ScrollPane()
        scrollPane2.content = scanningPart

        val splitPane = SplitPane()
        splitPane.items.addAll(scrollPane, scrollPane2)
        splitPane.setDividerPositions(0.5)
        splitPane.dividers[0].positionProperty().addListener { observable, _, _ ->
            splitPane.setDividerPositions(0.5)
        }

        val root = VBox(menuBar, splitPane)
        root.spacing = 10.0
        scrollPane.prefWidthProperty().bind(root.widthProperty())
        scrollPane.prefHeightProperty().bind(root.heightProperty())

        // Устанавливаем сцену и отображаем окно
        primaryStage.scene = Scene(root, map.width * canvasSizeD, map.height * canvasSizeD + 40)
        primaryStage.title = "Map Display"
        primaryStage.show()

        primaryStage.scene.setOnKeyPressed { event ->

            when (event.code) {
                KeyCode.W -> station?.moveRobots(Direction.UP)
                KeyCode.S -> station?.moveRobots(Direction.DOWN)
                KeyCode.A -> station?.moveRobots(Direction.LEFT)
                KeyCode.D -> station?.moveRobots(Direction.RIGHT)
                else -> {}
            }
            robot.radar()
            drawMap(graphicsContext, drawingPart)
            hideMap(graphicsContext2, scanningPart)
            dynamicText.set(countPercentOfExploredCells())
        }



        // Отслеживаем изменение размеров ScrollPane и увеличиваем карту
        scrollPane.widthProperty().addListener { _, _, newWidth ->
            adjustMapWidth(graphicsContext, graphicsContext2, drawingPart, scanningPart, newWidth.toDouble())
        }
        scrollPane.heightProperty().addListener { _, _, newHeight ->
            adjustMapHeight(graphicsContext, drawingPart, newHeight.toDouble())
        }

        drawingPart.setOnMousePressed { event -> handleMousePressed(event, graphicsContext, drawingPart, startCoordinates) }
        scanningPart.setOnMousePressed { event -> handleMousePressedSetRobotPosition(event, graphicsContext2, scanningPart, startCoordinates) }
        drawingPart.setOnMouseDragged { event -> handleMouseDragged(event, graphicsContext, graphicsContext2, drawingPart, scanningPart, startCoordinates) }
    }

    //
    private fun createEditButton(primaryStage: Stage, canvas: Canvas)
    {

    }

    private fun createClearButton(primaryStage: Stage, canvas: Canvas)
    {

    }


    private fun createSubmitButton(primaryStage: Stage, canvas: Canvas)
    {

    }


    private fun handleMousePressed(event: MouseEvent, graphicsContext: GraphicsContext, drawingPart: Canvas, startCoordinates: DoubleArray) {
        startCoordinates[0] = event.x
        startCoordinates[1] = event.y

        if(isEditing) {
            if (!isSettingRobot && event.button == javafx.scene.input.MouseButton.PRIMARY)
                map.updateMap(event.x.toInt() / canvasSizeI, event.y.toInt() / canvasSizeI, 1)
            else if (!isSettingRobot && event.button == javafx.scene.input.MouseButton.SECONDARY)
                map.updateMap(event.x.toInt() / canvasSizeI, event.y.toInt() / canvasSizeI, 0)
        }
        drawMap(graphicsContext, drawingPart)


    }
    private fun handleMousePressedSetRobotPosition(event: MouseEvent, graphicsContext: GraphicsContext, drawingPart: Canvas, startCoordinates: DoubleArray) {
        startCoordinates[0] = event.x
        startCoordinates[1] = event.y

        if (isSettingRobot) {
            val x = (event.x / canvasSizeD).toInt()
            val y = (event.y / canvasSizeD).toInt()

            if (x in 0 until map.width && y in 0 until map.height && map.getCell(x,y) == 0) {
                station = Station(map, x, y) // Создаем станцию
                isSettingRobot = false
                hideMap(graphicsContext, canvas) // Рисуем станцию и роботов
            }
        }
        //старый код
        /*if (isSettingRobot) {
            // Установка робота
            setRobotPosition(event)
            robot.radar()
            hideMap(graphicsContext, drawingPart)
            dynamicText.set(countPercentOfExploredCells())
        }*/

    }


    private fun handleMouseDragged(event: MouseEvent, graphicsContext: GraphicsContext,graphicsContext2: GraphicsContext, drawingPart: Canvas,scanningPart: Canvas, startCoordinates: DoubleArray) {
        val flag = if (event.button == javafx.scene.input.MouseButton.PRIMARY) 1 else 0
        if(isEditing) {// Обновляем карту по линии между предыдущей и текущей точками
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

        drawMap(graphicsContext, drawingPart)
        hideMap(graphicsContext2, scanningPart)
    }


    private fun setRobotPosition(event: MouseEvent) {
        val x = (event.x / canvasSizeD).toInt()
        val y = (event.y / canvasSizeD).toInt()
        if (map.getCell(x, y) != 0 ) return
        if (x in 0 until map.width && y in 0 until map.height) {
            robot.position.setX(x)
            robot.position.setY(y)
            isSettingRobot = false
        }
    }


    private fun adjustMapWidth(graphicsContext: GraphicsContext,graphicsContext2: GraphicsContext, canvas: Canvas, scanningPart: Canvas, newWidth: Double) {
        val newCols = (newWidth / canvasSizeD).toInt()
        if (newCols > map.width) {
            map.expandWidth(newCols)
            canvas.width = newCols * canvasSizeD
            drawMap(graphicsContext, canvas)
            hideMap(graphicsContext2, scanningPart)
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
                        "PNG" -> map.saveMapAsPng(file.path)
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
                        "PNG" ->{
                            map.loadMapFromPng(file.path)
                            drawMap(canvas.graphicsContext2D, canvas)
                        }  // Загружаем карту в формате PNG
                        "CSV" -> {
                            map.loadMapFromCSV(file.path)
                            drawMap(canvas.graphicsContext2D, canvas)
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
                graphicsContext.fill = if (map.getCell(x, y) != 0) Color.BLACK else Color.WHITE
                graphicsContext.fillRect(x * canvasSizeD, y * canvasSizeD, canvasSizeD, canvasSizeD)
            }
        }
    }
    private fun hideMap(graphicsContext: GraphicsContext, canvas: Canvas)
    {
        graphicsContext.clearRect(0.0, 0.0, canvas.width, canvas.height)
        for (y in 0..<map.height) {
            for (x in 0..<map.width) {
                graphicsContext.fill =  if (map.getCell(x, y) != 2) Color.WHITE else Color.BLACK
                graphicsContext.fillRect(x * canvasSizeD, y * canvasSizeD, canvasSizeD, canvasSizeD)
            }
        }
        if (!isSettingRobot && robot.position.getX() != null && robot.position.getY() != null) {
            graphicsContext.fill = Color.RED
            graphicsContext.fillRect(robot.position.getX()!! * canvasSizeD, robot.position.getY()!! * canvasSizeD, canvasSizeD, canvasSizeD)
        }
        // Рисуем станцию, если она установлена
        station?.drawStation(graphicsContext)

        //Для отрисовки роботовв дальнейшем
        /*// Рисуем роботов, если станция установлена
        station?.robots?.forEach { if (it.PosX != null && it.PosY != null) {
                it.drawRobot(graphicsContext)
            }
        }*/
    }

    private fun updateMapAlongLine(x1: Int, y1: Int, x2: Int, y2: Int, flag: Int) {
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

    private fun createExploreText(primaryStage: Stage, canvas: Canvas) : Label{
        var exploreText = Label()
        exploreText.textProperty().bind(dynamicText)
        return exploreText
    }

    private fun countPercentOfExploredCells() : String{
        return String.format("%.2f",map.countTwos() / (map.countOnes() + map.countTwos()).toDouble() * 100)
    }
}