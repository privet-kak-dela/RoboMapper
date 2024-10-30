import javafx.application.Application
import javafx.stage.Stage
import map.Map
import map.MapDisplay


class Main : Application() {

    override fun start(primaryStage: Stage) {
        val map = Map(20, 20) // Создаем экземпляр класса Map


        val mapDisplay = MapDisplay(map)
        mapDisplay.start(primaryStage)
    }
}
fun main() {
    Application.launch(Main::class.java)
}