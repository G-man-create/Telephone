package program.telephone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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

    public static void initDataFile() throws IOException {
        File dataFile = new File(DATA_BIN);
        if (!dataFile.exists()) {
            dataFile.createNewFile();
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                oos.writeObject(new ArrayList<Contact>());
            }
        }
    }
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
            PhoneBook.initDataFile();
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
    private ObservableList<Contact> loadContacts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_BIN))) {
            return FXCollections.observableArrayList((List<Contact>) ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Ошибка загрузки", e);
            return FXCollections.observableArrayList();
        }
    }
    private enum DialogType {
        CONTACT_DIALOG,
        NUMBER_DIALOG
    }

    private <T> Optional<T> showDialog(String title, DialogType type, T initialData) {
        try {
            String fxmlFile;
            switch (type) {
                case CONTACT_DIALOG:
                    fxmlFile = "contact-dialog.fxml";
                    break;
                case NUMBER_DIALOG:
                    fxmlFile = "number-dialog.fxml";
                    break;
                default:
                    return Optional.empty();
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            DialogPane dialogPane = loader.load();
            Dialog<T> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.setTitle(title);

            Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("telephone.png")));
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
            okButton.setDefaultButton(true);
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK)  {
                    switch (type) {
                        case CONTACT_DIALOG:
                            TextField nameField = (TextField) dialogPane.lookup("#nameField");
                            return (T) nameField.getText();
                        case NUMBER_DIALOG:
                            TextField numberField = (TextField) dialogPane.lookup("#numberField");
                            ComboBox<String> typeComboBox = (ComboBox<String>) dialogPane.lookup("#typeComboBox");
                            return (T) new PhoneNumber(numberField.getText(), typeComboBox.getValue());
                    }
                }
                return null;
            });
            switch (type) {
                case CONTACT_DIALOG:
                    TextField nameField = (TextField) dialogPane.lookup("#nameField");
                    nameField.setText((String) initialData);
                    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
                        if (!newValue.matches("[а-яА-Яa-zA-Z ]*")) {
                            nameField.setText(oldValue);
                        }
                    });
                    break;
                case NUMBER_DIALOG:
                    PhoneNumber phoneNumber = (PhoneNumber) initialData;
                    TextField numberField = (TextField) dialogPane.lookup("#numberField");
                    ComboBox<String> typeComboBox = (ComboBox<String>) dialogPane.lookup("#typeComboBox");

                    numberField.setText(phoneNumber.getNumber());
                    typeComboBox.setValue(phoneNumber.getType());
                    allowOnlyNumbers(numberField);
                    break;
            }

            return dialog.showAndWait();
        } catch (IOException e) {
            logger.error("Ошибка при загрузке диалога", e);
            return Optional.empty();
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
        logger.info("Добавление контакта с помощью метода addContact");
        showDialog("Добавить контакт", DialogType.CONTACT_DIALOG, "")
                .ifPresent(name -> {
                    if (name.toString().isEmpty()) return;

                    boolean exists = contacts.stream()
                            .anyMatch(c -> c.getName().equalsIgnoreCase(name.toString()));

                    if (exists) {
                        logger.warn("Попытка добавить уже существующий контакт: {}", name);
                        showAlert("Ошибка", "Контакт уже существует", "Контакт с таким именем уже есть в справочнике");
                    } else {
                        Contact contact = new Contact(name.toString());
                        contacts.add(contact);
                        saveContacts();
                        logger.info("Контакт {} успешно добавлен и сохранен", name);
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
            contactData.getSelectionModel().clearSelection();
            saveContacts();
            logger.info("Контакт успешно удален");
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
        if (choosecontact == null) return;

        showDialog("Добавить номер", DialogType.NUMBER_DIALOG, new PhoneNumber("", "Мобильный"))
                .ifPresent(phoneNumber -> {
                    if (!numberVerification(phoneNumber.getNumber(), phoneNumber.getType(), null)) {
                        showAlert("Ошибка", "Некорректный номер",
                                "Номер не соответствует формату или слишком похож на существующий");
                        return;
                    }

                    boolean exists = choosecontact.getPhoneNumbers().stream()
                            .anyMatch(n -> n.getNumber().equals(phoneNumber.getNumber()));

                    if (exists) {
                        logger.warn("Попытка добавить уже существующий номер: {}", phoneNumber.getNumber());
                        showAlert("Ошибка", "Номер уже существует", "Этот номер уже есть у контакта");
                    } else {
                        choosecontact.addPhoneNumber(phoneNumber);
                        numberData.getItems().add(phoneNumber);
                        saveContacts();
                    }
                });
    }

    private boolean numberVerification(String number, String type, Contact currentContact) {
        if (number == null || number.isEmpty()) {
            return false;
        }

        String cleanNumber = number.replaceAll("[^0-9]", "");

        boolean formatValid;
        switch(type) {
            case "Мобильный":
                formatValid = cleanNumber.matches("^[78]\\d{10}$");
                break;
            case "Домашний":
                formatValid = cleanNumber.matches("^\\d{6,7}$");
                break;
            case "Рабочий":
                formatValid = cleanNumber.matches("^\\d{6,11}$");
                break;
            default:
                formatValid = cleanNumber.matches("^\\d{6,11}$");
        }

        if (!formatValid) {
            return false;
        }

        String numberWithoutFirstDigit = cleanNumber.substring(1);
        for (Contact contact : contacts) {
            if (contact == currentContact) {
                continue;
            }

            for (PhoneNumber existingNumber : contact.getPhoneNumbers()) {
                String existingCleanNumber = existingNumber.getNumber().replaceAll("[^0-9]", "");
                if (existingCleanNumber.length() == cleanNumber.length() &&
                        existingCleanNumber.substring(1).equals(numberWithoutFirstDigit)) {
                    return false; // Найден похожий номер
                }
            }
        }

        return true;
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

            // Очищаем и добавляем отсортированные элементы
            contactData.getItems().setAll(sorted);
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
            contactData.setItems(contacts);
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
    private void editnumber() {
        logger.info("Редактирование номера телефона с помощью метода editnumber");
        Contact choosecontact = contactData.getSelectionModel().getSelectedItem();
        PhoneNumber choosenumber = numberData.getSelectionModel().getSelectedItem();
        if (choosecontact == null || choosenumber == null) return;

        showDialog("Редактировать номер", DialogType.NUMBER_DIALOG, choosenumber)
                .ifPresent(newPhoneNumber -> {
                    if (!numberVerification(newPhoneNumber.getNumber(),
                            newPhoneNumber.getType(),
                            choosecontact)) {
                        showAlert("Ошибка", "Некорректный номер",
                                "Номер не соответствует формату или слишком похож на существующий");
                        return;
                    }

                    choosecontact.getPhoneNumbers().remove(choosenumber);
                    choosecontact.getPhoneNumbers().add(newPhoneNumber);
                    numberData.setItems(FXCollections.observableArrayList(choosecontact.getPhoneNumbers()));
                    saveContacts();
                    logger.info("Номер успешно изменен и сохранен");
                });
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
    private void editcontact() {
        logger.info("Редактирование контакта с помощью метода editcontact");
        Contact choosecontact = contactData.getSelectionModel().getSelectedItem();
        if (choosecontact == null) return;

        showDialog("Изменить контакт", DialogType.CONTACT_DIALOG, choosecontact.getName())
                .ifPresent(newName -> {
                    choosecontact.setName(newName.toString());
                    contactData.refresh();
                    saveContacts();
                    logger.info("Контакт успешно обновлен");
                });
    }
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    /**
     * Ограничивает ввод в текстовое поле только цифрами.
     * @param textField текстовое поле для валидации
     */
    private void allowOnlyNumbers(TextField textField) {
        textField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("^[+]?\\d*")) {
                logger.debug("Неправильный ввод в текстовом поле. Введены недопустимые символы: '{}'",
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
