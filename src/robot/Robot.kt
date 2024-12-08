package robot

import map.Map
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType

class Robot(private val map: Map)
{
    private val apparentDistance: Int = 10 //уровень сигнала(между роботами + расстояние видимости между роботом и стенкой)
    var position = Position(null,null); //Позиция робота(по умолчанию 0,0)
    var isMain: Boolean = false;
    var isLast: Boolean = false;

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

    public fun moveRight()
    {
        if (position.getX() != null) {
//            for (rob in robots) {
//                if (position.getX()!! + 1 == rob.position.getX()!! && position.getY()!! == rob.position.getY()) {
//                    warningMessage("Осторожно! Столкновение!")
//                }
//            }
            if (position.getX()!! + 1 < map.width && map.getCell(position.getX()!! + 1, position.getY()!!) == 0) {
                position.setX(position.getX()!! + 1)
            } else {
                warningMessage("Осторожно! Столкновение!")
            }
        }
    }
    public fun moveLeft()
    {
        if (position.getX() != null) {
//            for (rob in robots) {
//                if (position.getX()!! - 1 == rob.position.getX()!! && position.getY()!! == rob.position.getY()) {
//                    warningMessage("Осторожно! Столкновение!")
//                }
//            }

            if (position.getX()!! - 1 >= 0 && map.getCell(position.getX()!! - 1, position.getY()!!) == 0) {
                position.setX(position.getX()!! - 1)
            } else {
                warningMessage("Осторожно! Столкновение!")
            }
        }
    }
    public fun moveUp()
    {
        if (position.getY() != null) {
//            for (rob in robots) {
//                if (position.getX()!! == rob.position.getX()!! && position.getY()!! - 1 == rob.position.getY()) {
//                    warningMessage("Осторожно! Столкновение!")
//                }
//            }
            if (position.getY()!! - 1 >= 0 && map.getCell(position.getX()!!, position.getY()!! - 1) == 0) {
                position.setY(position.getY()!! - 1)
            } else {
                warningMessage("Осторожно! Столкновение!")
            }
        }
    }
    public fun moveDown()
    {
        if (position.getY() != null) {
//            for (rob in robots) {
//                if (position.getX()!! == rob.position.getX()!! && position.getY()!! + 1 == rob.position.getY()) {
//                    warningMessage("Осторожно! Столкновение!")
//                }
//            }

            if (position.getY()!! + 1 < map.height && map.getCell(position.getX()!!, position.getY()!! + 1) == 0) {
                position.setY(position.getY()!! + 1)
            } else {
                warningMessage("Осторожно! Столкновение!")
            }
        }
    }

    public fun radar()
    {
        var flagLeft: Boolean = true;
        var flagRight: Boolean = true;
        var flagUp: Boolean = true;
        var flagDown: Boolean = true;

        for (i in 1 .. apparentDistance )
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

}