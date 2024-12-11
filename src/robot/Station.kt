package robot

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import map.Map
import kotlin.math.abs
import kotlin.math.sqrt

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
            robots.add(Robot(map, position.getX(), position.getY()))
        }
    }

    // Метод для запуска нового робота
    fun launchRobot(direction: Direction, gc: GraphicsContext) {
        if (robots.size > 0) {
            if (robots.size == maxRobots) {
                leadRobot = robots.last()
                leadRobot?.isLead = true

            }
            if(lastRobot != null) {
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
            cur.drawRobot(gc)
            cur = cur.prevRobot
        }
    }

    fun moveRobots(direction: Direction, gc: GraphicsContext) {

        var x = leadRobot!!.position.getX()!!
        var y = leadRobot!!.position.getY()!!
        // Перемещаем ведущего робота
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
        launchRobot(Direction.RIGHT, gc)
        if(last!!.isLostConnection(lastRobot!!))
            lastRobot?.follow(last, gc)
    }

    override fun isLostConnection(other: Machine): Boolean {
        val dx = abs(position.getX()!! - (other as Robot).position.getX()!!)
        val dy = abs(position.getY()!! - (other as Robot).position.getY()!!)
        return !(dx < signalRange && dy == 0 || dy < signalRange && dx == 0)
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}