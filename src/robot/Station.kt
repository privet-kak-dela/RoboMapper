package robot

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color


import map.Map
import kotlin.math.sqrt

class Station(
    private val map: Map,
    var x: Int,
    var y: Int,
    val signalRange: Int = 10,
    val maxRobots: Int = 10
) {
    val robots = mutableListOf<Robot>()

    init {
        // Создаем начального робота
        if (maxRobots > 0) {
            robots.add(Robot(map, x, y))
        }
    }





    // Теперь управление идет от станции, думаю логично
    //Так что и в MapDisplay мы именно станцией управляем

    // ДАЛЬШЕ ОТ СЕБЯ КОД НАБРОСАЛ, МОЖЕТ КОМУ ПРИГОДИТСЯ








    // Метод для запуска нового робота

    fun launchRobot() {
        if (robots.size < maxRobots) {
            val lastRobot = robots.last()

            val newRobot = Robot(map, lastRobot.position.getX()!!, lastRobot.position.getY()!!)
            robots.add(newRobot)
        }
    }

    fun drawStation(gc: GraphicsContext) {
        gc.fill = Color.BLUE
        gc.fillRect(x * 10.0, y * 10.0, 10.0, 10.0)
    }
    fun moveRobots(direction: Direction) {
        if (robots.isEmpty()) return

        val leadRobot = robots[0]
        // Перемещаем ведущего робота
        when (direction) {
            Direction.UP -> leadRobot.moveUp()
            Direction.DOWN -> leadRobot.moveDown()
            Direction.LEFT -> leadRobot.moveLeft()
            Direction.RIGHT -> leadRobot.moveRight()
        }
        // Перемещаем остальных роботов, сохраняя дистанцию
        for (i in 1 until robots.size) {
            val currentRobot = robots[i]
            val previousRobot = robots[i - 1]
            currentRobot.follow(previousRobot)
            // Запускаем радар для каждого робота
            currentRobot.radar()
        }

        // Запускаем радар для ведущего робота после перемещения остальных
        leadRobot.radar()

        // Добавляем роботов, если расстояние позволяет
        if (robots.size < maxRobots) {
            if (robots.size > 1) {
                val lastRobot = robots.last()
                val prevRobot = robots[robots.size - 2]
                // используем position.getX() и position.getY() для расчета дистанции
                val distanceToPrevious = distance(lastRobot, prevRobot)
                if (distanceToPrevious > signalRange) {
                    launchRobot()
                }
            } else if (robots.size == 1) {
                // Случай, когда есть только один робот - сравниваем с начальной позицией станции
                val distanceToStation = distance(robots[0], Robot(map, x,y))
                if (distanceToStation > signalRange) {
                    launchRobot()
                }
            }
        }
    }



    // Функция для расчета расстояния между роботами
    private fun distance(robot1: Robot, robot2: Robot): Double {
        val dx = robot1.position.getX()!! - robot2.position.getX()!!
        val dy = robot1.position.getY()!! - robot2.position.getY()!!
        return sqrt((dx * dx + dy * dy).toDouble())
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}