package robot

class Position (private var x: Int?, private var y: Int?){
    private var posX: Int? = x;
    private var posY: Int? = y;

    fun getX(): Int? {
        return posX;
    }

    fun getY(): Int? {
        return posY;
    }

    fun setX(x: Int?) {
        posX = x;
    }

    fun setY(y: Int?){
        posY = y;
    }

}