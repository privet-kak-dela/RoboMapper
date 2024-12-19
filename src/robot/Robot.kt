package robot

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
//import jdk.nashorn.internal.runtime.regexp.joni.Config.log
import map.Map

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

import java.lang.Thread.sleep
import kotlin.math.abs

class Robot(private val map: Map): Machine {
    var robotSize: Int = 7 // Размер робота по умолчанию (нечетное число)
    var apparentDistance: Int = 10 // Дальность сигнала и дальность видимости
    var position = Position(null, null)
    var col: Color? = null
    var prevRobot: Machine? = null
    var nextRobot: Robot? = null
    var isLast: Boolean = false
    var isLead: Boolean = false
    var path: MutableList<Position> = mutableListOf()
    var direction: Direction = Direction.UP // Начальное направление


    // Constructors (adapted for robotSize)
    constructor(map: Map, position: Position, robotSize: Int = 7) : this(map) {
        this.position = position
        this.robotSize = robotSize
    }

    constructor(map: Map, x: Int?, y: Int?, robotSize: Int = 7) : this(map) {
        position.setX(x)
        position.setY(y)
        this.robotSize = robotSize
    }

    constructor(map: Map, x: Int?, y: Int?, color: Color, robotSize: Int = 7) : this(map) {
        position.setX(x)
        position.setY(y)
        col = color
        this.robotSize = robotSize
    }




    fun moveRight()
    {
        if (position.getX() != null && position.getX()!! + 1 < map.width && map.getCell(position.getX()!! + 1, position.getY()!!) == 0) {
            position.setX(position.getX()!! + 1);
            path.add(Position(position.getX()!!, position.getY()!!));
            direction = Direction.RIGHT
        }
        else {
            warningMessage("Осторожно! Столкновение!")
        }
    }
    fun moveLeft()
    {

        if (position.getX() != null && position.getX()!! - 1 >= 0 && map.getCell(position.getX()!! - 1, position.getY()!!) == 0) {
            position.setX(position.getX()!! - 1)
            path.add(Position(position.getX()!!, position.getY()!!));
            direction = Direction.LEFT
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
            direction = Direction.UP
        } else {
                warningMessage("Осторожно! Столкновение!")
            }

    }

    public fun moveDown()
    {

        if (position.getY() != null && position.getY()!! + 1 < map.height && map.getCell(position.getX()!!, position.getY()!! + 1) == 0) {
            position.setY(position.getY()!! + 1)
            path.add(Position(position.getX()!!, position.getY()!!));
            direction = Direction.DOWN
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
        if (x != null && y != null && col != null) {
            gc.fill = col
            val halfSize = (robotSize - 1) / 2

            when (direction) {
                Direction.UP -> drawTriangle(gc, x, y, halfSize, pointingUp = false)
                Direction.DOWN -> drawTriangle(gc, x, y, halfSize, pointingUp = true)
                Direction.LEFT -> drawTriangle(gc, x, y, halfSize, pointingUp = false, isRotated = true)
                Direction.RIGHT -> drawTriangle(gc, x, y, halfSize, pointingUp = true, isRotated = true)
            }
        } else {
            println("Robot position or color is not initialized. Cannot draw.")
        }
    }

    private fun drawTriangle(gc: GraphicsContext, x: Int, y: Int, halfSize: Int, pointingUp: Boolean, isRotated: Boolean = false) {
        for (i in 0..halfSize) {
            val width = 2 * i + 1
            for (j in 0 until width) {
                val dx = halfSize - i + j
                val dy = if (pointingUp) -i else i


                val currentX = if (isRotated) x + dy else x + dx
                val currentY = if (isRotated) y + dx else y + dy


                if (currentX in 0 until map.width && currentY in 0 until map.height) {
                    gc.fillRect(currentX * 10.0, currentY * 10.0, 10.0, 10.0)
                }
            }
        }
    }

    private fun warningMessage(warn: String) {
        val alert = Alert(AlertType.INFORMATION)
        alert.title = "Warning"
        alert.headerText = null
        alert.contentText = warn
        alert.showAndWait()
    }


}