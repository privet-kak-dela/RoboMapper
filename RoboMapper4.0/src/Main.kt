import javafx.application.Application
import javafx.stage.Stage
import map.Map
import map.MapDisplay


class Main : Application() {

    override fun start(primaryStage: Stage) {
        val map = Map() // Создаем экземпляр класса Map
        map.initMap(20, 20) // Инициализируем карту размером 20x20

        val mapDisplay = MapDisplay(map)
        mapDisplay.start(primaryStage)
    }
}
fun main() {
    val name = "Kotlin"
    //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
    // to see how IntelliJ IDEA suggests fixing it.
    println("Hello, " + name + "!")

    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }
}