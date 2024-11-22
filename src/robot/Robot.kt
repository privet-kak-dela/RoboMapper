package robot

import map.Map

class Robot(private val map: Map)
{
    //Позиция робота(по умолчанию 0,0)
    var PosX: Int? = null
    var PosY: Int? = null
    val apparentDistance: Int = 10

    public fun moveRight()
    {
        if (PosX != null && PosX!! + 1 < map.width && map.getCell(PosX!! + 1, PosY!!) == 0) {
            PosX = PosX!! + 1
        }
    }
    public fun moveLeft()
    {
        if (PosX != null && PosX!! - 1 >= 0 && map.getCell(PosX!! - 1, PosY!!) == 0) {
            PosX = PosX!! - 1
        }
    }
    public fun moveUp()
    {
        if (PosY != null && PosY!! - 1 >= 0 && map.getCell(PosX!!, PosY!! - 1) == 0) {
            PosY = PosY!! - 1
        }
    }
    public fun moveDown()
    {
        if (PosY != null && PosY!! + 1 < map.height && map.getCell(PosX!!, PosY!! + 1) == 0) {
            PosY = PosY!!+ 1
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
            if(flagLeft && map.getCell(PosX!!.minus(i), PosY!!) != 0)
            {
                map.updateMap(PosX!!.minus(i), PosY!!, 2)
                flagLeft = false
            }
            if(flagRight && map.getCell(PosX!!.plus(i), PosY!!) != 0)
            {
                map.updateMap(PosX!!.plus(i), PosY!!, 2)
                flagRight = false
            }
            if(flagUp && map.getCell(PosX!!, PosY!!.plus(i)) != 0)
            {
                map.updateMap(PosX!!, PosY!!.plus(i), 2)
                flagUp = false
            }
            if(flagDown && map.getCell(PosX!!, PosY!!.minus(i)) != 0)
            {
                map.updateMap(PosX!!, PosY!!.minus(i), 2)
                flagDown = false
            }
        }

    }

}