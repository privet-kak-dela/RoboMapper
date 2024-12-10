package robot

import javafx.scene.canvas.GraphicsContext

interface Machine {

    fun follow(other: Machine, gc: GraphicsContext)
    fun isLostConnection(other: Machine): Boolean
}