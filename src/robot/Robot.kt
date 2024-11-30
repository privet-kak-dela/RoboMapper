package robot

import map.Map

class Robot(private val map: Map)
{
    private val apparentDistance: Int = 10 //уровень сигнала(между роботами + расстояние видимости между роботом и стенкой)
    var position = Position(null,null); //Позиция робота(по умолчанию 0,0)
    var isMain: Boolean = false;
    var isLast: Boolean = false;
    //var path: Array<>

    ///////временные поля!!!!!!!!! (удалить после создания станции)




    public fun moveRight()
    {
        if (position.getX() != null && position.getX()!! + 1 < map.width && map.getCell(position.getX()!! + 1, position.getY()!!) == 0) {
            position.setX(position.getX()!! + 1);
        }
    }
    public fun moveLeft()
    {
        if (position.getX() != null && position.getX()!! - 1 >= 0 && map.getCell(position.getX()!! - 1, position.getY()!!) == 0) {
            position.setX(position.getX()!! - 1)
        }
    }
    public fun moveUp()
    {
        if (position.getY() != null && position.getY()!! - 1 >= 0 && map.getCell(position.getX()!!, position.getY()!! - 1) == 0) {
            position.setY(position.getY()!! - 1)
        }
    }
    public fun moveDown()
    {
        if (position.getY() != null && position.getY()!! + 1 < map.height && map.getCell(position.getX()!!, position.getY()!! + 1) == 0) {
            position.setY(position.getY()!! + 1)
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