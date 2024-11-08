import javafx.application.Application
import javafx.stage.Stage
import map.Map
import map.MapDisplay


class Main : Application() {

    override fun start(primaryStage: Stage) {

        val map = Map(50, 100) // Создаем экземпляр класса Map

        //primaryStage.isResizable = false
        val mapDisplay = MapDisplay(map)
        mapDisplay.start(primaryStage)
    }
}
fun main() {
    Application.launch(Main::class.java)
}