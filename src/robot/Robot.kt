package robot

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
//import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import map.Map
import map.MapDisplay

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.shape.Polygon

import java.lang.Thread.sleep
import kotlin.math.abs

class Robot(private val map: Map): Machine
{
    constructor(map: Map, position: Position): this(map) {
        this.position = position
    }
    constructor(map: Map, x: Int?, y: Int?, robotHeightNew : Int): this(map) {
        position.setX(x)
        position.setY(y)
        robotHeight = robotHeightNew
    }
    constructor(map: Map, x: Int?, y: Int?, color: Color, robotHeightNew : Int): this(map) {
        position.setX(x)
        position.setY(y)
        col = color
        robotHeight = robotHeightNew
    }

    var apparentDistance: Int = 10 //уровень сигнала(между роботами + расстояние видимости между роботом и стенкой)
    var position = Position(null,null); //Позиция робота(по умолчанию 0,0)
    var col: Color? = null
    var lastPressedButton : Char = 'w'
    var robotHeight: Int = 10

//    var robotCount: Int;
//    // при запуске нового
//    if (!robotCount) {
//        warningMessage("Роботы кончились!")
//    }
//    else {
//        robotCount--
//    }

    //var path: Array<>

    ///////временные поля!!!!!!!!! (удалить после создания станции)

    private fun warningMessage(warn: String) {
        var alert = Alert(AlertType.INFORMATION)
        alert.title = "Предупреждение"
        alert.headerText = null
        alert.contentText = warn
        alert.showAndWait()
    }



    var prevRobot: Machine? = null
    var nextRobot: Robot? = null
    var isLast: Boolean = false
    var isLead: Boolean = false

    var path: MutableList<Position> = mutableListOf()


    public fun moveRight()
    {
        if (position.getX() != null && position.getX()!! + 1 < map.width && map.getCell(position.getX()!! + 1, position.getY()!!) == 0) {
            position.setX(position.getX()!! + 1);
            path.add(Position(position.getX()!!, position.getY()!!));
            lastPressedButton = 'D'
        }
        else {
            warningMessage("Осторожно! Столкновение!")
        }
    }
    public fun moveLeft()
    {

        if (position.getX() != null && position.getX()!! - 1 >= 0 && map.getCell(position.getX()!! - 1, position.getY()!!) == 0) {
            position.setX(position.getX()!! - 1)
            path.add(Position(position.getX()!!, position.getY()!!));
            lastPressedButton = 'A'
        }
        else {
            warningMessage("Осторожно! Столкновение!")
        }
    }
    public fun moveUp()
    {

        if (position.getY() != null && position.getY()!! - 1 >= 0 && map.getCell(position.getX()!!, position.getY()!! - 1) == 0) {
            position.setY(position.getY()!! - 1)
            path.add(Position(position.getX()!!, position.getY()!!));
            lastPressedButton = 'W'
        } else {
                warningMessage("Осторожно! Столкновение!")
            }

    }

    public fun moveDown()
    {

        if (position.getY() != null && position.getY()!! + 1 < map.height && map.getCell(position.getX()!!, position.getY()!! + 1) == 0) {
            position.setY(position.getY()!! + 1)
            path.add(Position(position.getX()!!, position.getY()!!));
            lastPressedButton = 'S'
        }
        else
        {
            warningMessage("Осторожно! Столкновение!")
        }

    }


    public fun radar()
    {
        var flagLeft: Boolean = true;
        var flagRight: Boolean = true;
        var flagUp: Boolean = true;
        var flagDown: Boolean = true;

        for (i in 1 ..apparentDistance!!)
        {
            if(flagLeft && map.getCell(position.getX()!!.minus(i), position.getY()!!) != 0)
            {
                map.updateMap(position.getX()!!.minus(i), position.getY()!!, 2)
                flagLeft = false
            }
            if(flagRight && map.getCell(position.getX()!!.plus(i), position.getY()!!) != 0)
            {
                map.updateMap(position.getX()!!.plus(i), position.getY()!!, 2)
                flagRight = false
            }
            if(flagUp && map.getCell(position.getX()!!, position.getY()!!.plus(i)) != 0)
            {
                map.updateMap(position.getX()!!, position.getY()!!.plus(i), 2)
                flagUp = false
            }
            if(flagDown && map.getCell(position.getX()!!, position.getY()!!.minus(i)) != 0)
            {
                map.updateMap(position.getX()!!, position.getY()!!.minus(i), 2)
                flagDown = false
            }
        }

    }

    fun isLostConnection(other: Robot): Boolean {
        val dx = abs(other.position.getX()!! - position.getX()!!)
        val dy = abs(other.position.getY()!! - position.getY()!!)
        return !(dx <= apparentDistance && dy == 0 || dy <= apparentDistance && dx == 0)
    }

    // Метод для следования робота
    override fun follow(other: Machine, gc: GraphicsContext) {
        var robot = other as Robot
        var pos = robot.path.indexOfLast { p -> p.getX() == position.getX() && p.getY() == position.getY() }
        gc.fill = Color.WHITE
        gc.fillRect(position.getX()!! * 10.0, position.getY()!! * 10.0, 10.0, 10.0)
        position.setX(robot.path[pos + 1].getX())
        position.setY(robot.path[pos + 1].getY())
        path.add(Position(position.getX()!!, position.getY()!!))

        drawRobot(gc)

        if(isLostConnection(prevRobot!!)) {
            prevRobot?.follow(this, gc)
        }
        if(isLostConnection(robot))
            follow(robot, gc)
    }

    fun back(gc: GraphicsContext)
    {
        gc.fill = Color.WHITE
        gc.fillRect(position.getX()!! * 10.0, position.getY()!! * 10.0, 10.0, 10.0)
        if(path.size > 1) {
            position.setX(path[path.size - 2].getX())
            position.setY(path[path.size - 2].getY())
            path.removeAt(path.size - 1)

            if(path.size != 1)
                drawRobot(gc)
            return
        }
        path.removeAt(path.size - 1)

    }



    override fun isLostConnection(other: Machine): Boolean {
        var dx: Int
        var dy: Int
        if(other is Robot) {
            var o = other as Robot
            dx = abs(o.position.getX()!! - position.getX()!!)
            dy = abs(o.position.getY()!! - position.getY()!!)
        }
        else {
            var o = other as Station
            dx = abs(o.position.getX()!! - position.getX()!!)
            dy = abs(o.position.getY()!! - position.getY()!!)
        }
        return !(dx <= apparentDistance && dy == 0 || dy <= apparentDistance && dx == 0)
    }

    fun drawRobot(gc: GraphicsContext) {
        val x = position.getX()
        val y = position.getY()
        if (x != null && y != null) { // Проверка на null
            gc.fill = col
            //gc.fillRect(x * 10.0, y * 10.0, 10.0, 10.0)

            //Массивы точек треугольника
            val xArray = DoubleArray(3)
            val yArray = DoubleArray(3)

            if (robotHeight % 2 != 0)
            {
                robotHeight += 1
            }
            val heightHalf : Double = robotHeight / 2.0

            if (lastPressedButton == 'D')
            {
                xArray[0] = x * 10 + heightHalf
                xArray[1] = x * 10 - heightHalf
                xArray[2] = x * 10 - heightHalf

                yArray[0] = y * 10 + 0.0
                yArray[1] = y * 10 + heightHalf
                yArray[2] = y * 10 - heightHalf
            }
            else if (lastPressedButton == 'W')
            {
                xArray[0] = x * 10 + 0.0
                xArray[1] = x * 10 + heightHalf
                xArray[2] = x * 10 - heightHalf

                yArray[0] = y * 10 - heightHalf
                yArray[1] = y * 10 + heightHalf
                yArray[2] = y * 10 + heightHalf
            }
            else if (lastPressedButton == 'A')
            {
                xArray[0] = x * 10 - heightHalf
                xArray[1] = x * 10 + heightHalf
                xArray[2] = x * 10 + heightHalf

                yArray[0] = y * 10 + 0.0
                yArray[1] = y * 10 + heightHalf
                yArray[2] = y * 10 - heightHalf
            }
            else if (lastPressedButton == 'S')
            {
                xArray[0] = x * 10 + 0.0
                xArray[1] = x * 10 + heightHalf
                xArray[2] = x * 10 - heightHalf

                yArray[0] = y * 10 + heightHalf
                yArray[1] = y * 10 - heightHalf
                yArray[2] = y * 10 - heightHalf
            }
            gc.fillPolygon(xArray, yArray, 3)

        }
        else {
            println("Robot position is not initialized. Cannot draw.")
        }
    }
}