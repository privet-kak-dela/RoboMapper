package robot

import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.paint.Color
import map.Map
import java.lang.Thread.sleep
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class Station(private val map: Map): Machine {

    var signalRange: Int = 10
    var maxRobots: Int = 10
    var lastRobot: Robot? = null
    var leadRobot: Robot? = null
    var position = Position(null,null);
    val robots = mutableListOf<Robot>()

    constructor(map: Map, x: Int?, y: Int?, maxRobots: Int?, signalRange: Int?): this(map) {
        position.setX(x)
        position.setY(y)
        this.maxRobots = maxRobots!!
        this.signalRange = signalRange!!
        for (i in 0 until this.maxRobots) {
            var robot = Robot(map, position.getX(), position.getY(), getRandomColor())
            robot.path.add(Position(position.getX(), position.getY()))
            robots.add(robot)
        }
    }

    var dir: Direction? = null

    // Метод для запуска нового робота
    fun launchRobot(direction: Direction, gc: GraphicsContext) {
        if (robots.size > 0) {
            if (robots.size == maxRobots) {
                leadRobot = robots.last()
                leadRobot?.isLead = true
                lastRobot?.nextRobot = null
                dir = direction
            }
            if(lastRobot != null) {
                robots.last().nextRobot = lastRobot
                lastRobot?.prevRobot = robots.last()
            }

            lastRobot = robots.last()
            lastRobot?.prevRobot = this
            when (direction) {
                Direction.UP -> lastRobot?.moveUp()
                Direction.DOWN -> lastRobot?.moveDown()
                Direction.LEFT -> lastRobot?.moveLeft()
                Direction.RIGHT -> lastRobot?.moveRight()
            }
            lastRobot!!.apparentDistance = signalRange
            lastRobot!!.drawRobot(gc)
            robots.removeLast()
        }

    }

    fun drawStation(gc: GraphicsContext) {
        gc.fill = Color.BLUE
        gc.fillRect(position.getX()!! * 10.0, position.getY()!! * 10.0, 10.0, 10.0)
    }

    fun drawRobots(gc: GraphicsContext) {
        var cur: Machine? = leadRobot
        while(cur is Robot){
           if(cur.position.getX() != position.getX() || cur.position.getY() != position.getY())
                cur.drawRobot(gc)
            cur = cur.prevRobot
        }
    }

    private fun warningMessage(warn: String) {
        var alert = Alert(AlertType.INFORMATION)
        alert.title = "Предупреждение"
        alert.headerText = null
        alert.contentText = warn
        alert.showAndWait()
    }

    fun moveRobots(direction: Direction, gc: GraphicsContext) {
        if (leadRobot == null) {
            // Обработка случая, когда ведущего робота ещё нет
            // Можно выйти из функции, показать сообщение или запустить робота.
            warningMessage("Роботы ещё не запущены.") // Или запустить робота
            return
        }

        var x = leadRobot!!.position.getX()!!
        var y = leadRobot!!.position.getY()!!
        // Перемещаем ведущего робота
        if(isEncounter(direction)){
            warningMessage("Осторожно! Столкновение!")
            return
        }

        when (direction) {
            Direction.UP -> leadRobot?.moveUp()
            Direction.DOWN -> leadRobot?.moveDown()
            Direction.LEFT -> leadRobot?.moveLeft()
            Direction.RIGHT -> leadRobot?.moveRight()
        }
        leadRobot?.radar()
        leadRobot?.drawRobot(gc)
        gc.fill = Color.WHITE
        gc.fillRect(x * 10.0, y * 10.0, 10.0, 10.0)

        if(leadRobot!!.isLostConnection(leadRobot?.prevRobot!!) ) {
            leadRobot?.prevRobot?.follow(leadRobot!!, gc)
        }
    }

    override fun follow(other: Machine, gc: GraphicsContext) {
        var last = lastRobot
        if (last != null) {
            if(robots.size == 0 && last.isLostConnection(this))
            {
                val alert2 = Alert(Alert.AlertType.CONFIRMATION)
                alert2.title = "Возврат роботов"
                alert2.headerText = "Связь потеряна. Роботы будут возвращены на станцию"
                alert2.showAndWait()
                robotBack(gc)
                return
            }
        }
        launchRobot(dir!!, gc)
        if(last!!.isLostConnection(lastRobot!!))
            lastRobot?.follow(last, gc)
    }

    override fun isLostConnection(other: Machine): Boolean {
        val dx = abs(position.getX()!! - (other as Robot).position.getX()!!)
        val dy = abs(position.getY()!! - (other as Robot).position.getY()!!)
        return !(dx < signalRange && dy == 0 || dy < signalRange && dx == 0)
    }

    fun isEncounter(direction: Direction): Boolean
    {
        var cur = leadRobot?.prevRobot

        while(cur is Robot) {
            var x = leadRobot?.position?.getX()!!
            var y = leadRobot?.position?.getY()!!
            if(direction == Direction.DOWN)
                y += 1
            else if(direction == Direction.UP)
                y -= 1
            else if(direction == Direction.LEFT)
                x -= 1
            else if(direction == Direction.RIGHT)
                x += 1

             if(cur.position.getX()!! == x && cur.position.getY()!! == y)
                 return true
            cur = cur.prevRobot
        }
        return false
    }

    fun robotBack(gc: GraphicsContext)
    {
        while(robots.size != maxRobots)
        {
            var cur: Robot? = lastRobot
            while(cur != null)
            {
                cur.back(gc)
                cur = cur.nextRobot
            }
            if(lastRobot?.path?.size == 1)
            {
                var last = lastRobot
                robots.add(lastRobot!!)
                //gc.fill = Color.WHITE
                //gc.fillRect(lastRobot!!.position.getX()!! * 10.0, lastRobot!!.position.getY()!! * 10.0, 10.0, 10.0)
                lastRobot = lastRobot?.nextRobot
                last?.prevRobot = null
                last?.nextRobot = null

            }
            //sleep(100)
        }
    }
    private var lastColor: Color? = null  // Хранит последний сгенерированный цвет

    private fun isBlackOrBlue(color: Color): Boolean {
        // Проверяем, является ли цвет черным или синим
        return color == Color.BLACK || (color.red == 0.0 && color.green == 0.0 && color.blue > 0.5)
    }

    private fun getRandomColor(): Color {
        var newColor: Color
        do {
            val red = Random.nextDouble(0.1, 0.9)
            val green = Random.nextDouble(0.1, 0.9)
            val blue = Random.nextDouble(0.0, 0.5)
            newColor = Color.color(red, green, blue)
        } while (newColor == lastColor || isBlackOrBlue(newColor))

        lastColor = newColor
        return newColor
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}