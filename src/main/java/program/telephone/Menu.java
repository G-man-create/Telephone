package program.telephone;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Класс контроллера для главного меню приложения.
 * Отвечает за навигацию между основными разделами приложения:
 * Открытие телефонной книги
 * Выход из приложения
 * Анимированное переключение между сценами
 */
public class Menu {
    /**
     * Логгер для работы с меню приложения.
     * <p>
     * Используется для записи событий взаимодействия с пользовательским меню.
     */
    private static final Logger logger = LogManager.getLogger(Menu.class);
    /**
     * Главное окно (сцена) приложения.
     * <p>
     * Используется для отображения пользовательского интерфейса меню.
     */
    private Stage stage;
    /**
     * Устанавливает основную сцену (Stage) для данного класса.
     *
     * @param stage объект Stage (сцена JavaFX), который будет использоваться
     *              для отображения диалоговых окон и других UI-элементов
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    /**
     * Обработчик события для открытия телефонной книги.
     */
    @FXML
    private void openPhoneBook() {
        logger.info("Открытие телефонной книги");
        switchScene("telephone.fxml");
    }
    /**
     * Обработчик события для выхода из приложения.
     */
    @FXML
    private void Exit() {
        logger.info("Завершение работы приложения");
        System.exit(0);
    }
    /**
     * Переключает текущую сцену на новую, загружаемую из указанного FXML-файла.
     *
     * <p>Метод выполняет загрузку FXML-файла, создает новую сцену и устанавливает ее
     * в качестве текущей для основного окна приложения (Stage).</p>
     *
     * @param fxml путь к FXML-файлу с описанием интерфейса (например, "/view/main.fxml").
     *             Должен быть валидным путем к существующему FXML-файлу.
     * @throws RuntimeException если произошла ошибка при загрузке FXML-файла
     */
    public void switchScene(String fxml)  {
        if (stage == null){
            logger.warn("Попытка переключения сцены ");
            return;
        }

        try {
            logger.debug("Загрузка FXML: {}", fxml);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent newRoot = loader.load();

            if (fxml.equals("telephone.fxml")) {
                PhoneBook controller = loader.getController();
                controller.setMenu(this);
                logger.debug("Установлен контроллер для телефонной книги");
            } else if (fxml.equals("menu.fxml")) {
                Menu controller = loader.getController();
                controller.setStage(stage);
                logger.debug("Установлен контроллер для меню");
            }

            Scene scene = stage.getScene();
            Parent oldRoot = scene.getRoot();
            logger.debug("Запуск анимации перехода между сценами");
            AnimationTimer fadeOut = new AnimationTimer() {
                private long start = -1;

                @Override
                public void handle(long now) {
                    if (start == -1) start = now;
                    double elapsed = (now - start) / 1e9;
                    double opacity = Math.max(1 - elapsed * 2, 0);
                    oldRoot.setOpacity(opacity);

                    if (opacity <= 0) {
                        stop();
                        scene.setRoot(newRoot);
                        newRoot.setOpacity(0);

                        AnimationTimer fadeIn = new AnimationTimer() {
                            private long startIn = -1;

                            @Override
                            public void handle(long now) {
                                if (startIn == -1) startIn = now;
                                double elapsedIn = (now - startIn) / 1e9;
                                double opacityIn = Math.min(elapsedIn * 2, 1);
                                newRoot.setOpacity(opacityIn);

                                if (opacityIn >= 1) {
                                    stop();
                                    logger.debug("Анимация перехода завершена");
                                }
                            }
                        };
                        fadeIn.start();
                    }
                }
            };
            fadeOut.start();

        } catch (IOException e) {
            logger.error("Ошибка при переключении сцены", e);
            e.printStackTrace();
        }
    }
}

