package program.telephone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Этот класс основным который занимается созданием, удалением редактированием классов, также сохранением их в бинарный файл
 */
public class PhoneBook {
    private static final Logger logger = LogManager.getLogger(PhoneBook.class);

    private Menu menuController;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private TextField searchField;
    @FXML
    private ListView<Contact> contactData;
    @FXML
    private ListView<PhoneNumber> numberData;

    private ObservableList<Contact> contacts;

    private static final String DATA_BIN  = "phonebook.bin";
    /**
     * Инициализирует данные телефонной книги.
     * Метод выполняет следующие действия:Загружает список контактов из файла
     * Создает  список (ObservableList) для хранения контактов
     * Устанавливает загруженные контакты в ListView для отображения
     * Обновляет отображение номеров телефона
     * Логирует успешное завершение инициализации или ошибки при их возникновении
     * Обрабатываемые исключения
     */
    public void initialize()  {
        try {
            logger.info("Инициализация данных о контактах");
            contacts = FXCollections.observableArrayList(loadContacts());
            contactData.setItems(contacts);
            contactData.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    numberData.setItems(FXCollections.observableArrayList(newValue.getPhoneNumbers()));
                } else {
                    numberData.setItems(null);
                }
            });

            logger.info("Инициализация успешно завершена");
        } catch (Exception e) {
            logger.error("Ошибка при инициализации: ", e);
            throw e;
        }
    }
    /**
     * Сохраняет все контакты телефонной книги в бинарный файл
     * Функции:
     * Создает или перезаписывает файл
     * Фильтрует список контактов, удаляя null-значения
     * Записывает отфильтрованный список контактов в файл с помощью ObjectOutputStream
     * Закрывает поток записи автоматически
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void saveContacts() {
        logger.info("Сохранение контактов с помощью метода saveContacts");
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_BIN))) {
            List<Contact> toSave = contacts.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("Сохранение контактов в файл");
            oos.writeObject(toSave);
            logger.info("Контакты успешно сохранены в файл");
        } catch (IOException e) {
            logger.error("Не удалось сохранить контакты в файл: {}", e.getMessage(), e);
        }
    }
    /**
     * Загружает список контактов из бинарного файла.
     * Функции:
     * Открывает файл  для чтения в бинарном режиме
     * Преобразует список в {@code ObservableList} для использования в JavaFX
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private  ObservableList<Contact> loadContacts() {
        logger.info("Загрузка данных с помощью метода loadContacts");
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_BIN))) {
            List<Contact> loaded = (ArrayList<Contact>) ois.readObject();
            logger.info("Успешная загрузка");

            return FXCollections.observableArrayList(
                    loaded.stream()
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
        } catch (IOException e) {
            logger.error("Ошибка при загрузке контактов из файла: {}", e.getMessage(), e);
            return FXCollections.observableArrayList();
        } catch (ClassNotFoundException e) {
            logger.error("Класс не найден при загрузке контактов: {}", e.getMessage(), e);
            return FXCollections.observableArrayList();
        }
    }
    /**
     * Добавляет новый контакт в телефонную книгу.
     * Функции:
     * Выводит окно для добавления контакта
     * Проверяет ввод символов
     * Сохраняет  в файл с помощью метода saveContacts()
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void addContact() {
        logger.info("Добавление контакта с помощьюе метода addContact");
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Добавить контакт");
        dialog.setHeaderText("Введите ФИО контакта:");
        try {
            dialog.getDialogPane().getStylesheets().add(getClass().getResource("style1.css").toExternalForm());
            dialog.getDialogPane().getStyleClass().add("custom-dialog");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
            logger.debug("Вызов окна добавления контакта ");
        } catch (Exception e) {
            logger.error("Ошибка вызова окна", e);
        }
        dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("[а-яА-Яa-zA-Z ]*")) {
                dialog.getEditor().setText(oldValue);
            }

        });
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            boolean exists = contacts.stream()
                    .anyMatch(c -> c.getName().equalsIgnoreCase(name));

            if (exists) {
                logger.warn("Попытка добавить уже существующий контакт: {}", name);
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Контакт уже существует");
                alert.setContentText("Контакт с таким именем уже есть в справочнике");
                alert.showAndWait();
            } else {
                Contact contact = new Contact(name);
                contacts.add(contact);
                try {
                    saveContacts();
                    logger.info("Контакт {} успешно добавлен и сохранен", name);
                } catch (Exception e) {
                    logger.error("Ошибка при сохранении контакта {}", name, e);
                }

            }
        });
    }
    /**
     * Удаляет выбранный контакт из телефонной книги.
     * Функции:
     * Получает выбранный контакт из списка контактов
     * Если контакт выбран, удаляет его из списка контактов
     * Сохраняет изменения в файл с помощью метода saveContacts()
     *
     */
    @FXML
    private void removeContact() {
        logger.info("Удаление контакта с помощью метода removeContact");
        Contact choosecontact = contactData.getSelectionModel().getSelectedItem();
        if (choosecontact != null) {
            contacts.remove(choosecontact);
            saveContacts();
            logger.debug("Контакт успешно удален");
        }
    }
    /**
     * Добавляет номер телефона к выбранному контакту
     * Функции:
     * Проверяет, выбран ли контакт в списке контактов
     * Создает диалоговое окно для ввода данных номера
     * Проверяет ввод символов
     * Предоставляет выбор типа номера из предопределенного списка
     * Проверяет, не существует ли уже такой номер у контакта
     * Добавляет номер к контакту и сохраняет изменения
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void addNumber() {
        logger.info("Добавление номера с помощью метода addNumber");
        Contact choosecontact = contactData.getSelectionModel().getSelectedItem();
        if (choosecontact == null) {
            return;
        }
        try {
            Dialog<PhoneNumber> dialog = new Dialog<>();
            dialog.setTitle("Добавить номер");

            Label numberLabel = new Label("Номер:");
            TextField numberField = new TextField();
            Label typeLabel = new Label("Тип:");
            ComboBox<String> typeComboBox = new ComboBox<>(FXCollections.observableArrayList("Мобильный", "Рабочий", "Домашний", "Факс"));
            typeComboBox.getSelectionModel().selectFirst();
            allowOnlyNumbers(numberField);
            typeComboBox.getStyleClass().add("combobox");
            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
            numberField.getStyleClass().add("textfield");
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(numberLabel, 0, 0);
            grid.add(numberField, 1, 0);
            grid.add(typeLabel, 0, 1);
            grid.add(typeComboBox, 1, 1);
            dialog.getDialogPane().setContent(grid);
            grid.getStyleClass().add("grid");
            grid.getStylesheets().add(getClass().getResource("style2.css").toExternalForm());
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    return new PhoneNumber(numberField.getText(), typeComboBox.getValue());
                }
                logger.debug("Отмена добавление контакта");
                return null;

            });
            Optional<PhoneNumber> result = dialog.showAndWait();
            result.ifPresent(phoneNumber -> {
                logger.debug("Обработка результата диалога");
                boolean exists = choosecontact.getPhoneNumbers().stream()
                        .anyMatch(n -> n.getNumber().equals(phoneNumber.getNumber()));
                if (exists) {
                    logger.warn("Попытка добавить уже существующий номер: {}", phoneNumber.getNumber());
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Номер уже существует");
                    alert.setContentText("Этот номер уже есть у контакта");
                    alert.showAndWait();
                } else {
                    choosecontact.addPhoneNumber(phoneNumber);
                    numberData.getItems().add(phoneNumber);
                    saveContacts();
                }
            });
        } catch (Exception e) {
            logger.error("Ошибка при добавлении номера", e);
        }
    }
    /**
     * Удаляет выбранный номер телефона у текущего контакта
     * Функции:
     * Получает выбранный контакт из списка контактов
     * Получает выбранный номер телефона из списка номеров
     * Если и контакт, и номер существую:
     * Удаляет номер из списка номеров контакта
     * Обновляет отображение списка номеров
     * Сохраняет изменения в файл
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void removeNumber() {
        logger.info("Удаление номера с помощью метода removeNumber");
        try {
            Contact choosecontact = contactData.getSelectionModel().getSelectedItem();
            PhoneNumber choosenumber = numberData.getSelectionModel().getSelectedItem();

            if (choosecontact != null && choosenumber != null) {
                choosecontact.getPhoneNumbers().remove(choosenumber);
                numberData.getItems().remove(choosenumber);
                logger.debug("Номер телефона успешно удален");
                saveContacts();
                logger.info("Сохранение изменения");
            }
        } catch (Exception e) {
            logger.error("Возникла ошибка при удалении телефонного номера", e);
            throw e;
        }
    }
    /**
     * Сортирует контакты в телефонной книге по имени в алфавитном порядке
     * Функции:
     * Сортирует контакты по А-Я
     * Сортирует контакты по Я-А
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void sort() {
        logger.info("Запуск сортировки контактов");

        try {
            boolean reverse = contactData.getItems().equals(FXCollections.observableArrayList(
                    contacts.stream()
                            .sorted(Comparator.comparing(Contact::getName))
                            .collect(Collectors.toList())
            ));

            List<Contact> sorted;
            if (reverse) {
                sorted = contacts.stream()
                        .sorted(Comparator.comparing(Contact::getName).reversed())
                        .collect(Collectors.toList());
                logger.info("Применена сортировка по убыванию (Я-А)");
            } else {
                sorted = contacts.stream()
                        .sorted(Comparator.comparing(Contact::getName))
                        .collect(Collectors.toList());
                logger.info("Применена сортировка по возрастанию (А-Я)");
            }

            contactData.setItems(FXCollections.observableArrayList(sorted));
        } catch (Exception e) {
            logger.error("Ошибка при сортировке контактов", e);
            throw e;
        }
    }
    /**
     * Выполняет поиск контактов по введенной строке в поисковом поле
     * Поиск осуществляется как по имени контакта, так и по номерам телефона
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void search() {
        logger.info("Поиск котактов и номеров с помощью метода Search");
        String request = searchField.getText().toLowerCase().trim();
        if (request.isEmpty()) {
            contactData.setItems(FXCollections.observableArrayList(contacts));
            return;
        }

        try {
            List<Contact> filtered = contacts.stream()
                    .filter(contact -> {
                        boolean nameMatch = contact.getName().toLowerCase().contains(request);
                        boolean numberMatch = contact.getPhoneNumbers().stream()
                                .anyMatch(number -> number.getNumber().contains(request));

                        logger.trace("Contact '{}' - name match: {}, number match: {}",
                                contact.getName(), nameMatch, numberMatch);

                        return nameMatch || numberMatch;
                    })
                .collect(Collectors.toList());
            contactData.setItems(FXCollections.observableArrayList(filtered));
        } catch (Exception e) {
            logger.error("Ошибка при поиске ", e);
        }

    }
    /**
     * Редактирует имя выбранного контакта в телефонной книге.
     * Функции:
     * Проверяет, выбран ли контакт для редактирования
     * Создает диалоговое окно для ввода нового имени контакта
     * Проверяет вводимые символы
     * Если новое имя контакта уже существует в телефонной книге, будет показано предупреждение и изменения не сохранятся.
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void editcontact(ActionEvent event) {
        logger.info("Редактирование контакта с помощью метода editcontact ");
        try {
            Contact choosecontact = contactData.getSelectionModel().getSelectedItem();
            if (choosecontact == null) {
                return;
            }

            TextInputDialog dialog = new TextInputDialog(choosecontact.getName());
            dialog.setTitle("Изменить контакт");
            dialog.setHeaderText("Введите новое ФИО контакта:");

            try {
                dialog.getDialogPane().getStylesheets().add(getClass().getResource("style1.css").toExternalForm());
                dialog.getDialogPane().getStyleClass().add("dialog");
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
                logger.debug("Диалог редактирования контакта настроен");
            } catch (Exception e) {
                logger.error("Ошибка настройки диалогового окна", e);
            }

            dialog.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.matches("[а-яА-Яa-zA-Z ]*")) {
                    dialog.getEditor().setText(oldValue);
                }
            });

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(newName -> {
                boolean nameExists = contacts.stream()
                        .filter(contact -> !contact.equals(choosecontact))
                        .anyMatch(contact -> contact.getName().equalsIgnoreCase(newName.trim()));

                if (nameExists) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Контакт уже существует");
                    alert.setContentText("Контакт с таким именем уже есть в справочнике");

                    try {
                        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
                        alert.showAndWait();
                    } catch (Exception e) {
                        logger.error("Ошибка при отображении предупреждения", e);
                    }
                } else {
                    choosecontact.setName(newName.trim());
                    contactData.refresh();
                    saveContacts();
                }
            });

        } catch (Exception e) {
            logger.error("Ошибка при редактировании контакта", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось изменить контакт");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
    /**
     * Редактирует выбранный номер телефона.
     * Функции:
     * Проверяет, выбран ли контакт для редактирования
     * Создает диалоговое окно с текущими значениями номера и типа
     * Проверяет вводимые символы
     * Проверяет, что новый номер не дублирует существующие
     * Обновляет данные номера и сохраняет изменения
     * В случае ошибки метод логирует исключение с помощью logger.error()
     */
    @FXML
    private void editnumber(ActionEvent event) {
        logger.info("Редактирование номера телефона с помощью метода editnumber ");
        Contact choosecontact = contactData.getSelectionModel().getSelectedItem();
        PhoneNumber choosenumber = numberData.getSelectionModel().getSelectedItem();

        if (choosecontact == null || choosenumber == null) {
            logger.warn("Попытка редактирования без выбранного контакта или номера");
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не выбраны контакт или номер");
            alert.setContentText("Пожалуйста, выберите контакт и номер для редактирования");
            try {
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
                alert.showAndWait();
            } catch (Exception e) {
                logger.error("Ошибка при отображении предупреждения", e);
            }
            return;
        }

        try {
            Dialog<PhoneNumber> dialog = new Dialog<>();
            dialog.setTitle("Редактировать номер");
            TextField numberField = new TextField(choosenumber.getNumber());
            ComboBox<String> typeComboBox = new ComboBox<>(
                    FXCollections.observableArrayList("Мобильный", "Рабочий", "Домашний", "Факс"));
            typeComboBox.setValue(choosenumber.getType());
            allowOnlyNumbers(numberField);
            try {
                typeComboBox.getStyleClass().add("combobox");
                numberField.getStyleClass().add("textfield");
                GridPane grid = new GridPane();
                grid.setHgap(10);
                grid.setVgap(10);
                grid.add(new Label("Номер:"), 0, 0);
                grid.add(numberField, 1, 0);
                grid.add(new Label("Тип:"), 0, 1);
                grid.add(typeComboBox, 1, 1);
                grid.getStyleClass().add("grid");
                grid.getStylesheets().add(getClass().getResource("style2.css").toExternalForm());
                dialog.getDialogPane().setContent(grid);
                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
            } catch (Exception e) {
                logger.error("Ошибка настройки диалога", e);
            }

            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.setResultConverter(button -> {
                if (button == ButtonType.OK) {
                    return new PhoneNumber(numberField.getText().trim(), typeComboBox.getValue());
                }
                return null;
            });

            Optional<PhoneNumber> result = dialog.showAndWait();
            result.ifPresent(newPhoneNumber -> {
                boolean numberExists = choosecontact.getPhoneNumbers().stream()
                        .filter(n -> !n.equals(choosenumber))
                        .anyMatch(n -> n.getNumber().equals(newPhoneNumber.getNumber()));

                if (numberExists) {
                    logger.warn("Попытка изменить на существующий номер: {}", newPhoneNumber.getNumber());
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Ошибка");
                    alert.setHeaderText("Номер уже существует");
                    alert.setContentText("Этот номер уже есть у контакта");
                    try {
                        Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                        alertStage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
                        alert.showAndWait();
                    } catch (Exception e) {
                        logger.error("Ошибка при отображении предупреждения", e);
                    }
                } else {
                    choosecontact.getPhoneNumbers().remove(choosenumber);
                    choosecontact.getPhoneNumbers().add(newPhoneNumber);
                    numberData.setItems(FXCollections.observableArrayList(choosecontact.getPhoneNumbers()));
                    saveContacts();
                }
            });

        } catch (Exception e) {
            logger.error("Ошибка при редактировании номера", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось изменить номер");
            alert.setContentText(e.getMessage());
            try {
                Stage alertStage = (Stage) alert.getDialogPane().getScene().getWindow();
                alertStage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
                alert.showAndWait();
            } catch (Exception ex) {
                logger.error("Ошибка при отображении ошибки", ex);
            }
        }
    }
    /**
     * Ограничивает ввод в текстовое поле только цифрами.
     * @param textField текстовое поле для валидации
     */
    private void allowOnlyNumbers(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                logger.debug("Неправильный ввод в текстовом поле.Введены недопустимые символы: '{}'",
                        newValue);
                textField.setText(oldValue);
            }
        });

    }
    /**
     *  Этот метод обеспечивает связь между телефонной книгой и главным меню приложения
     */
    public void setMenu(Menu menuController) {
        logger.debug("Установка контроллера Menu: {}", menuController);
        this.menuController = menuController;
        if (logger.isTraceEnabled()) {
            logger.trace("Контроллер меню установлен успешно: {}", this.menuController);
        }
    }
    /**
     *  Обрабатывает возврат пользователя в главное меню из телефонной книги
     */
    @FXML
    private void onBackToMenu() {
        try {
            logger.debug("Возращение в меню");

            if (menuController != null) {
                menuController.switchScene("menu.fxml");
                logger.info("Успешное переключение");
            } else {
                logger.warn("невозможно переключиться на меню");
            }
        } catch (Exception e) {
            logger.error("Ошибка при переходе в меню: " + e.getMessage(), e);
            throw e; //
        }
    }
}
