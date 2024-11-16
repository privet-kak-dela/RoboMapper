package robot

import map.Map

class Robot(private val map: Map)
{
    //Позиция робота(по умолчанию 0,0)
    var PosX: Int? = null
    var PosY: Int? = null

    public fun moveRight()
    {
        if (PosX != null && PosX!! + 1 < map.width && !map.getCell(PosX!! + 1, PosY!!)) {
            PosX = PosX!! + 1
        }
    }
    public fun moveLeft()
    {
        if (PosX != null && PosX!! - 1 >= 0 && !map.getCell(PosX!! - 1, PosY!!)) {
            PosX = PosX!! - 1
        }
    }
    public fun moveUp()
    {
        if (PosY != null && PosY!! - 1 >= 0 && !map.getCell(PosX!!, PosY!! - 1)) {
            PosY = PosY!! - 1
        }
    }
    public fun moveDown()
    {
        if (PosY != null && PosY!! + 1 < map.height && !map.getCell(PosX!!, PosY!! + 1)) {
            PosY = PosY!!+ 1
        }
    }

}