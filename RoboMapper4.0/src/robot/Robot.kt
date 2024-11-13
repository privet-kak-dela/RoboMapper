package robot

import map.Map

class Robot(private val map: Map)
{
    //Позиция робота(по умолчанию 0,0)
    var PosX = 0
    var PosY = 0

    public fun moveRight()
    {
        if (PosX + 1 < map.width && !map.getCell(PosX + 1, PosY)) {
            PosX++
        }
    }
    public fun moveLeft()
    {
        if (PosX - 1 >= 0 && !map.getCell(PosX - 1, PosY)) {
            PosX--
        }
    }
    public fun moveUp()
    {
        if (PosY - 1 >= 0 && !map.getCell(PosX, PosY - 1)) {
            PosY--
        }
    }
    public fun moveDown()
    {
        if (PosY + 1 < map.height && !map.getCell(PosX, PosY + 1)) {
            PosY++
        }
    }

}