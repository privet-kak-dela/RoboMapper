import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.stage.Stage
import map.Map
import map.MapDisplay


class Main : Application() {

    override fun start(primaryStage: Stage) {

        val map = Map(76, 150) // Создаем экземпляр класса Map
        primaryStage.isMaximized = true;
        //primaryStage.isResizable = false
        val mapDisplay = MapDisplay(map)
        mapDisplay.start(primaryStage)


        //Проверка


    }
}
fun main() {
    Application.launch(Main::class.java)

}