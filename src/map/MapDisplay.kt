package map

import javafx.application.Application
import javafx.beans.property.SimpleStringProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.Window
import robot.Direction
import robot.Robot
import robot.Station
import java.io.File
import kotlin.math.abs
import kotlin.math.sign


class MapDisplay @JvmOverloads constructor(
    private val map: Map = Map(20, 20)) : Application() {


    //private val robot: Robot = Robot(map, null, null)
    private val canvasSizeD = 10.0 // Размер ячейки в пикселях
    private val canvasSizeI = 10 // Размер ячейки в пикселях (Int)
    private var isSettingRobot = false // Флаг для режима установки робота
    private var isStationExist = false
    private var isEditing = false // Флаг для режима редактирования карты
    private var maxRobots = 10
    private var signalRange = 10
    private var robotSize = 7

    companion object {
        var canvas = Canvas()
        var canvas2 = Canvas()
    }
    private var station: Station? = null

    override fun start(primaryStage: Stage) {


        displayRobotSizeInputWindow(primaryStage)


        canvas = Canvas(map.width * canvasSizeD, map.height * canvasSizeD)
        canvas2 = Canvas(map.width * canvasSizeD, map.height * canvasSizeD)
        val gc = canvas.graphicsContext2D
        val gc2 = canvas2.graphicsContext2D

        val startCoordinates = doubleArrayOf(0.0, 0.0)

        drawMap(gc, canvas)
        hideMap(gc2, canvas2)

        val setRobotButton = Button("Установить станцию")
        setRobotButton.setOnAction {
            if (!isEditing) {
                if (!isStationExist) {
                    isSettingRobot = true
                    isStationExist = true
                    displayInputWindow(primaryStage)  // Передаем primaryStage в displayInputWindow
                } else {
                    val alert = Alert(Alert.AlertType.CONFIRMATION)
                    alert.title = "Установка станции"
                    alert.headerText = "Вы уверены, что хотите переустановить станцию?"

                    val result = alert.showAndWait()
                    if (result.isPresent && result.get() == ButtonType.OK) {
                        station?.robotBack(gc2)
                        isSettingRobot = true
                        hideMap(gc2, canvas2)
                    }
                }
            } else {
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.title = "Ошибка установки станции"
                alert.headerText = "Подтвердите карту"
                alert.showAndWait()
            }
        }


        val returnRobots = Button("Вернуть роботов")
        returnRobots.setOnAction {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Возврат роботов"
            alert.headerText = "Роботы будут возвращены на станцию"
            alert.showAndWait()
            station?.robotBack(gc2)

        }

        val saveButton = createSaveButton(primaryStage, canvas)
        val loadButton = createLoadButton(primaryStage, canvas)

        val editButton = ToggleButton("Нарисовать карту")
        editButton.setOnAction {
            if (isStationExist) {
                val alert = Alert(Alert.AlertType.CONFIRMATION)
                alert.title = "Редактирование карты"
                alert.headerText = "Весь прогресс будет утерян. Вы уверены?"

                val result = alert.showAndWait()
                if (result.isPresent && result.get() == ButtonType.OK) {
                    map.clearRobotPaths()
                    station = null
                    isStationExist = false
                    drawMap(gc, canvas)
                    hideMap(gc2, canvas2)
                }
            }
            isEditing = editButton.isSelected
            editButton.text = if (isEditing) "Подтвердить" else "Нарисовать карту"
            canvas.isDisable = !isEditing
        }



        val clearAllButton = createClearAllButton(gc, canvas, gc2, canvas2)


        val clearRobotPathsButton = Button("Очистить исследованную территорию").apply {
            setOnAction { handleClearRobotPaths(map, canvas2, gc2) }

        }


        val menuBar = HBox(10.0, saveButton, loadButton, editButton, clearAllButton, clearRobotPathsButton, setRobotButton, returnRobots)

        val scrollPane = ScrollPane(canvas)
        val scrollPane2 = ScrollPane(canvas2)

        val splitPane = SplitPane(scrollPane, scrollPane2)
        splitPane.setDividerPositions(0.5)


        val root = VBox(menuBar, splitPane)
        root.spacing = 10.0
        root.padding = Insets(10.0)  // Add padding to the root


        scrollPane.prefWidthProperty().bind(root.widthProperty())
        scrollPane.prefHeightProperty().bind(root.heightProperty().subtract(menuBar.heightProperty()).subtract(root.spacing))
        scrollPane2.prefWidthProperty().bind(root.widthProperty())
        scrollPane2.prefHeightProperty().bind(root.heightProperty().subtract(menuBar.heightProperty()).subtract(root.spacing))


        primaryStage.scene = Scene(root)
        primaryStage.title = "Map Display"


        canvas.setOnMousePressed { event -> handleMousePressed(event, gc, canvas, startCoordinates) }
        canvas2.setOnMousePressed { event -> handleMousePressedSetRobotPosition(event, gc2, canvas2, startCoordinates) }
        canvas.setOnMouseDragged { event -> handleMouseDragged(event, gc, gc2, canvas, canvas2, startCoordinates) }


        primaryStage.scene.setOnKeyPressed { event ->
            if (station != null) {

                when (event.code) {
                    KeyCode.W -> station?.moveRobots(Direction.UP, gc2)
                    KeyCode.S -> station?.moveRobots(Direction.DOWN, gc2)
                    KeyCode.A -> station?.moveRobots(Direction.LEFT, gc2)
                    KeyCode.D -> station?.moveRobots(Direction.RIGHT, gc2)


                    else -> {}
                }
                drawMap(gc, canvas)
                hideMap(gc2, canvas2)
            }
        }
        primaryStage.show()

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
    private fun handleMousePressedSetRobotPosition(event: MouseEvent, graphicsContext: GraphicsContext, scanningPart: Canvas, startCoordinates: DoubleArray) {
        startCoordinates[0] = event.x
        startCoordinates[1] = event.y


        if (isSettingRobot) {
            val x = (event.x / canvasSizeD).toInt()
            val y = (event.y / canvasSizeD).toInt()

            if (x in 0 until map.width && y in 0 until map.height && map.getCell(x, y) == 0) {
                // Get robot size from user
                val dialog = TextInputDialog("7") // Default value
                dialog.title = "Robot Size"
                dialog.headerText = "Enter the robot size (odd number):"
                dialog.contentText = "Size:"

                val result = dialog.showAndWait()
                val robotSize = if (result.isPresent) {
                    val input = result.get().toIntOrNull()
                    if (input != null && input % 2 != 0 && input > 0) {
                        input // Use valid input
                    } else {
                        7 // Use default if input is invalid
                    }
                } else {
                    7 // Use default if dialog is cancelled
                }

                //Creating a station
                station = Station(map, x, y, maxRobots, signalRange)

                // Set the size for each robot in the station
                for (robot in station!!.robots) {
                    robot.robotSize = robotSize
                }

                isSettingRobot = false
                hideMap(graphicsContext, scanningPart)
            }
        }

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
        }
        // Обновляем стартовые координаты для следующего вызова handleMouseDragged
        startCoordinates[0] = event.x
        startCoordinates[1] = event.y

        drawMap(graphicsContext, drawingPart)
        hideMap(graphicsContext2, scanningPart)
    }


//    private fun setRobotPosition(event: MouseEvent) {
//        val x = (event.x / canvasSizeD).toInt()
//        val y = (event.y / canvasSizeD).toInt()
//        if (map.getCell(x, y) != 0 ) return
//        if (x in 0 until map.width && y in 0 until map.height) {
//            robot.position.setX(x)
//            robot.position.setY(y)
//            isSettingRobot = false
//            isStationExist = true
//        }
//    }


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
//        if (!isSettingRobot && robot.position.getX() != null && robot.position.getY() != null) {
//            graphicsContext.fill = Color.RED
//            graphicsContext.fillRect(robot.position.getX()!! * canvasSizeD, robot.position.getY()!! * canvasSizeD, canvasSizeD, canvasSizeD)
//        }
        // Рисуем станцию, если она установлена
        station?.drawStation(graphicsContext)
        station?.drawRobots(graphicsContext)

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


    private fun createClearAllButton(gc: GraphicsContext, canvas: Canvas, gc2: GraphicsContext, canvas2: Canvas): Button {
        val clearAllButton = Button("Очистить карту")
        clearAllButton.setOnAction {
            val alert = Alert(Alert.AlertType.CONFIRMATION)
            alert.title = "Подтверждение очистки"
            alert.headerText = "Весь прогресс будет утерян. Вы уверены?"

            val result = alert.showAndWait()
            if (result.isPresent && result.get() == ButtonType.OK) {
                map.clearAll()
                //station?.robotBack(gc2)
                station = null
                isStationExist = false

                drawMap(gc, canvas)
                hideMap(gc2, canvas2)
            }
        }
        return clearAllButton
    }

    private fun handleClearRobotPaths(map: Map, canvas: Canvas, gc: GraphicsContext) {
        val alert = Alert(Alert.AlertType.CONFIRMATION)
        alert.title = "Подтверждение очистки"
        alert.headerText = "Вы точно хотите очистить исследованную зону?"


        val result = alert.showAndWait()
        if (result.isPresent && result.get() == ButtonType.OK) {
            val alert2 = Alert(Alert.AlertType.CONFIRMATION)
            alert2.title = "Возврат роботов"
            alert2.headerText = "Роботы будут возвращены на станцию"
            alert2.showAndWait()
            station?.robotBack(gc)
            //station = null
            //isStationExist = false
            map.clearRobotPaths()
            hideMap(gc, canvas)
        }
    }



    //Выводит окно с сообщением, что переданные значения не удовлетворяют условиям
    private fun showWrongInputMessage(message: String)
    {
        val inputWindow = Stage();
        inputWindow.initModality(Modality.APPLICATION_MODAL);
        inputWindow.title = "Wrong Parameters"

        val msg = Label(message)

        val layout = VBox(10.0);
        layout.children.addAll(msg)
        layout.alignment = Pos.CENTER
        val inputScene = Scene(layout, 200.0, 100.0);
        inputWindow.scene = inputScene;
        inputWindow.showAndWait()
    }

    //--Проверка, что введеные значения - числа
    //--Если числа - записывает их в параметры
    //--Если нет - выводит сообщение о неправильности данных и возвращает false
    private fun infoValidation(message:String , message2:String, stage: Stage ) : Boolean
    {
        try {
            maxRobots = Integer.parseInt(message2) //Поменять на параметр
        }catch ( e : NumberFormatException)
        {
            showWrongInputMessage(message2 + " не является числом")
            return false
        }

        try {
            signalRange = Integer.parseInt(message)+1 //Поменять на параметр
            stage.close()
            return true
        }catch ( e : NumberFormatException)
        {
            showWrongInputMessage(message + "не является числом")
            //Вернуть первому параметру старое значение?
            return false
        }
    }


    //Отображает окно для ввода количества роботов и уровня сигнала
    private fun displayInputWindow(owner: Window)
    {
        val inputWindow = Stage()
        inputWindow.initModality(Modality.APPLICATION_MODAL)
        inputWindow.initOwner(owner)  // Set owner for modal behavior
        inputWindow.title = "Input Parameters"

        val grid = GridPane();
        grid.padding = Insets(10.0, 10.0, 10.0, 10.0);
        grid.vgap = 8.0;
        grid.hgap = 8.0;

        val robotCountLabel = Label("Введите уровень сигнала ")
        GridPane.setConstraints(robotCountLabel, 0, 0);

        val robotCountInput = TextField()
        GridPane.setConstraints(robotCountInput, 0, 1);

        val signalLevelLabel = Label("Введите количество роботов")
        GridPane.setConstraints(signalLevelLabel, 0, 2);


        val signalLevelInput = TextField()
        GridPane.setConstraints(signalLevelInput, 0, 3);

        val okButton = Button("Ok")
        okButton.setOnAction { e -> infoValidation(robotCountInput.text, signalLevelInput.text, inputWindow) }

        val layout = VBox(10.0);
        layout.children.addAll(okButton)
        layout.alignment = Pos.CENTER
        GridPane.setConstraints(layout, 0, 4);

        grid.children.addAll(robotCountLabel, robotCountInput, signalLevelLabel, signalLevelInput, layout)


        val inputScene = Scene(grid, 300.0, 200.0);
        inputWindow.scene = inputScene;
        inputWindow.showAndWait()
    }

    private fun displayRobotSizeInputWindow(owner: Window) {
        val inputWindow = Stage()
        inputWindow.initModality(Modality.APPLICATION_MODAL)
        inputWindow.initOwner(owner)
        inputWindow.title = "Robot Size"

        val label = Label("Enter the robot size (odd number):")
        val inputField = TextField("7") // Default value
        val okButton = Button("OK")

        okButton.setOnAction {
            val input = inputField.text.toIntOrNull()
            if (input != null && input % 2 != 0 && input > 0) {
                robotSize = input
                // Update existing robots size, if any
                station?.robots?.forEach { it.robotSize = robotSize }
                inputWindow.close()
            } else {
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Invalid Input"
                alert.headerText = null
                alert.contentText = "Please enter a valid odd number greater than 0."
                alert.showAndWait()
            }
        }

        val vbox = VBox(label, inputField, okButton)
        vbox.padding = Insets(10.0)
        vbox.spacing = 8.0

        inputWindow.scene = Scene(vbox)
        inputWindow.showAndWait()

    }

}
