package program.telephone;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Этот класс основным который занимается созданием, удалением редактированием классов, также сохранением их в бинарный файл
 */
public class PhoneBook {
    /** Логгер для класса PhoneBook. */
    private static final Logger logger = LogManager.getLogger(PhoneBook.class);
    /** Контроллер меню приложения. */
    private Menu menuController;
    /** searchField для поиска. */
    @FXML
    private TextField searchField;
    /** ListView для отображения списка контактов. */
    @FXML
    private ListView<Contact> contactData;
    /** ListView для отображения списка номеров. */
    @FXML
    private ListView<PhoneNumber> numberData;
    /** Наблюдаемый список контактов. */
    private ObservableList<Contact> contacts;
    /** Имя файла для хранения данных телефонной книги. */
    private static final String DATA_BIN  = "phonebook.bin";
    /**
     * Инициализирует файл данных, если он не существует.
     * Создает новый файл и записывает в него пустой список контактов.
     *
     * @throws IOException если произошла ошибка ввода-вывода при создании файла или записи данных
     * @see File
     * @see ObjectOutputStream
     * @see FileOutputStream
     */
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
     * Инициализирует данные приложения, загружая контакты из файла и настраивая отображение данных.
     *
     * <p>Метод выполняет следующие действия:
     * <ol>
     *   <li>Инициализирует файл с данными о контактах (если он не существует)</li>
     *   <li>Загружает список контактов из файла</li>
     *   <li>Устанавливает загруженные контакты в таблицу контактов</li>
     *   <li>Настраивает слушатель выбора контакта для отображения связанных телефонных номеров</li>
     * </ol>
     *
     * <p>В случае успешного выполнения логируется информационное сообщение. При возникновении ошибок
     * информация об исключении записывается в лог.
     *
     * @see PhoneBook#initDataFile()
     * @see #loadContacts()
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
     * Сохраняет список контактов в бинарный файл.
     *
     * <p>Метод выполняет следующие действия:
     * <ol>
     *   <li>Фильтрует null-значения из списка контактов</li>
     *   <li>Сохраняет отфильтрованный список в файл, путь к которому указан в константе {@code DATA_BIN}</li>
     *   <li>Использует {@link ObjectOutputStream} для сериализации данных</li>
     * </ol>
     *
     * <p>В процессе работы метод логирует:
     * <ul>
     *   <li>Начало выполнения операции</li>
     *   <li>Факт сохранения контактов в файл</li>
     *   <li>Успешное завершение операции или ошибку</li>
     * </ul>
     *
     * @throws RuntimeException если произошла ошибка ввода-вывода при сохранении файла
     * @see ObjectOutputStream
     * @see FileOutputStream
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
     * Возвращает список контактов в виде ObservableList.
     * <p>
     * Возвращаемый список автоматически обновляет UI при изменениях.
     *
     * @return ObservableList объектов {@link Contact}, содержащая все контакты книги
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
    /**
     * Перечисление типов диалоговых окон.
     */
    private enum DialogType {
        /** Диалог для работы с контактами. */
        CONTACT_DIALOG,

        NUMBER_DIALOG
    }
    /**
     * Отображает диалоговое окно с заданными параметрами.
     *
     * @param <T> тип данных, возвращаемых диалогом (должен соответствовать типу initialData)
     * @param title заголовок диалогового окна
     * @param type тип диалога (определяет его вид и поведение)
     * @param initialData начальные данные для отображения в диалоге (может быть null)
     * @return {@code Optional<T>}, содержащий результат если пользователь подтвердил действие,
     *         или {@code Optional.empty()} если диалог был отменён
     * @throws IllegalArgumentException если type равен null
     * @see DialogType
     */
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
     * Добавляет новый контакт в список контактов после взаимодействия с пользователем.
     *
     * <p>Метод выполняет следующие действия:
     * <ol>
     *   <li>Отображает диалоговое окно для ввода имени нового контакта</li>
     *   <li>Проверяет, что введенное имя не пустое</li>
     *   <li>Проверяет, что контакт с таким именем еще не существует (без учета регистра)</li>
     *   <li>Если проверки пройдены, создает новый контакт и добавляет его в список</li>
     *   <li>Сохраняет обновленный список контактов</li>
     * </ol>
     * @see #showAlert(String, String, String) - для отображения сообщений об ошибках
     * @see #saveContacts() - для сохранения списка контактов
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
     * Сохраняет контакты в хранилище.
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
     * Добавляет новый номер телефона к выбранному контакту.
     *
     * Метод выполняет следующие действия:
     * 1. Получает выбранный контакт из списка контактов
     * 2. Если контакт не выбран, завершает выполнение
     * 3. Отображает диалоговое окно для ввода нового номера телефона
     * 4. Проверяет корректность введенного номера
     * 5. Проверяет, не существует ли уже такой номер у контакта
     * 6. Если проверки пройдены, добавляет номер к контакту и сохраняет изменения
     * @see #showAlert(String, String, String)
     * @see #saveContacts()
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
    /**
     * Проверяет валидность номера телефона и его уникальность среди контактов.
     *
     * <p>Метод выполняет следующие проверки:
     * <ol>
     *   <li>Проверяет, что номер не null и не пустой</li>
     *   <li>Очищает номер от всех нецифровых символов</li>
     *   <li>Проверяет соответствие формату номера в зависимости от типа телефона</li>
     *   <li>Проверяет уникальность номера среди всех контактов (игнорируя текущий контакт)</li>
     * </ol>
     *
     * @param number проверяемый номер телефона (может содержать нецифровые символы)
     * @param type тип телефона ("Мобильный", "Домашний", "Рабочий")
     * @param currentContact текущий контакт, который исключается из проверки на уникальность
     * @return true если номер валиден и уникален, false в противном случае
     *
     * @see Contact
     * @see PhoneNumber
     */
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
     * Удаляет выбранный номер телефона у выбранного контакта.
     * Если контакт и номер телефона выбраны, метод удаляет номер из списка номеров контакта
     * и обновляет отображаемые данные. После успешного удаления сохраняет изменения.
     * В случае возникновения ошибки логирует её и пробрасывает исключение дальше.
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
     * Сортирует список контактов по имени в алфавитном порядке (А-Я) или обратном порядке (Я-А).
     * <p>
     * Метод автоматически определяет текущий порядок сортировки и меняет его на противоположный.
     * Если контакты не отсортированы или отсортированы в прямом порядке (А-Я),
     * то будет применена сортировка по убыванию (Я-А), и наоборот.
     * </p>
     * <p>
     * Результат сортировки сохраняется в {@code contactData} после очистки предыдущего списка.
     * </p>
     *
     * @see Contact#getName()
     * @see Comparator#comparing(java.util.function.Function)
     * @see FXCollections#observableArrayList(java.util.Collection)
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
     * Выполняет поиск контактов по имени или номеру телефона на основе введенного запроса.
     * <p>
     * Метод фильтрует список контактов, оставляя только те, у которых имя содержит поисковый запрос
     *  или хотя бы один из номеров телефона содержит поисковый запрос.
     * Если поисковый запрос пустой, отображаются все контакты.
     * </p>
     * <p>
     * Процесс поиска логируется на уровне INFO, а детали совпадений для каждого контакта
     * логируются на уровне TRACE. В случае возникновения ошибок они логируются на уровне ERROR.
     * </p>
     * @see Contact
     * @see Contact#getName()
     * @see Contact#getPhoneNumbers()
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
     * Редактирует выбранный номер телефона выбранного контакта.
     *
     * <p>Метод выполняет следующие действия:
     * <ol>
     *   <li>Получает выбранный контакт и выбранный номер телефона из таблиц контактов и номеров</li>
     *   <li>Если контакт или номер не выбраны, метод завершает выполнение</li>
     *   <li>Отображает диалоговое окно для редактирования номера телефона</li>
     *   <li>Проверяет новый номер на корректность с помощью {@link #numberVerification}</li>
     *   <li>При успешной проверке заменяет старый номер новым в списке номеров контакта</li>
     *   <li>Обновляет отображаемый список номеров и сохраняет изменения</li>
     * </ol>
     *
     * <p>В случае если номер не проходит проверку, отображается предупреждающее сообщение.
     *
     * <p>Метод логирует свои действия с помощью {@link Logger}.
     *
     * @see #showAlert(String, String, String)
     * @see #saveContacts()
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
     * Редактирует выбранный контакт в списке контактов.
     *
     * <p>Метод выполняет следующие действия:
     * <ol>
     *   <li>Получает выбранный контакт из модели выбора</li>
     *   <li>Если контакт не выбран, завершает выполнение</li>
     *   <li>Отображает диалоговое окно для изменения имени контакта</li>
     *   <li>Если пользователь подтверждает изменение, обновляет имя контакта</li>
     *   <li>Обновляет отображение списка контактов</li>
     *   <li>Сохраняет изменения в хранилище</li>
     * </ol>
     *
     * <p>Логирует процесс редактирования контакта и его успешное завершение.
     *
     * @see Contact#setName(String)
     * @see #saveContacts()
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
    /**
     * Отображает предупреждающее диалоговое окно с заданными параметрами.
     *
     * @param title заголовок диалогового окна
     * @param header текст заголовка сообщения (может быть null)
     * @param content основное содержание сообщения
     */
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
     * Устанавливает контроллер меню для текущего класса.
     *
     * @param menuController экземпляр контроллера меню, который будет использоваться
     *                      для взаимодействия с элементами меню приложения.
     *                      Не должен быть null.
     * @throws IllegalArgumentException если переданный menuController равен null
     */
    public void setMenu(Menu menuController) {
        logger.debug("Установка контроллера Menu: {}", menuController);
        this.menuController = menuController;
        if (logger.isTraceEnabled()) {
            logger.trace("Контроллер меню установлен успешно: {}", this.menuController);
        }
    }
    /**
     * Обрабатывает возврат в главное меню приложения.
     *
     * <p>Метод выполняет следующие действия:
     * <ol>
     *   <li>Логирует попытку перехода в меню</li>
     *   <li>Проверяет наличие инициализированного контроллера меню</li>
     *   <li>При успешной проверке переключает сцену на главное меню</li>
     *   <li>Логирует результат операции</li>
     * </ol>
     * <p>В случае ошибки метод:
     * @see Menu#switchScene(String)
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
