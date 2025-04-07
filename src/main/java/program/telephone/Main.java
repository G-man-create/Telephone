package program.telephone;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Главный класс приложения, запускающий JavaFX приложение.
 */
public class Main extends Application {
    /**
     * Логгер для работы с меню приложения.
     * <p>
     * Используется для записи событий взаимодействия с пользовательским меню.
     */
    private static final Logger logger = LogManager.getLogger(Main.class);
    @Override
    /**
     * Устанавливает основную сцену (Stage) для данного класса.
     *
     * @param stage объект Stage (сцена JavaFX), который будет использоваться
     *              для отображения диалоговых окон и других UI-элементов
     */
    public void start(Stage stage) throws Exception {
        logger.info("Запуск приложения Телефонный справочник");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));
            Scene scene = new Scene(loader.load());
            Menu menuController = loader.getController();
            menuController.setStage(stage);
            stage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
            stage.setScene(scene);
            stage.setTitle("Телефонный справочник");
            stage.show();
        }catch (Exception e) {
            logger.error("Ошибка при запуске приложения", e);
            throw e;
        }

    }
    /**
     * Основной метод запуска приложения
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        launch(args);
    }
}
