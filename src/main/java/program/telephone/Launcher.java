package program.telephone;
import javafx.application.Application;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Класс-лаунчер для запуска приложения.
 */
public class Launcher {
    private static final Logger logger = LogManager.getLogger(Launcher.class);
    /**
     * Точка входа для запуска JavaFX приложения.
     * @param args аргументы командной строки
     */
    public static void main(String[] args) {
        logger.info("Запуск приложения через Launcher");
        Application.launch(Main.class,args);
    }
}
