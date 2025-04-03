package program.telephone;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Класс, представляющий контакт в телефонной книге.
 * <p>
 * Содержит информацию о имени контакта и списке его телефонных номеров.
 * Реализует интерфейс Serializable для возможности сериализации.
 * @see PhoneNumber
 * @see Serializable
 * </p>
 */

public class Contact implements Serializable {
    private static final Logger logger = LogManager.getLogger(Contact.class);
    /**
     * Полное имя контакта.
     */
    private String name;
    /**
     * Список телефонных номеров, связанных с контактом.
     */
    private List<PhoneNumber> phoneNumbers;
    /**
     * Создает новый контакт.
     * @param fullName полное имя контакта
     */
    public Contact(String fullName) {
        this.name = fullName;
        this.phoneNumbers = new ArrayList<>();
        logger.debug("Создан новый контакт: {}", fullName);
    }
    /**
     * Возвращает полное имя контакта.
     *
     * @return имя контакта
     */
    public String getName() {
        return name;
    }/**
     * Устанавливает новое имя для контакта.
     * @param name новое имя контакта
     */

    public void setName(String name) {
        this.name = name;
    }
    /**
     * Возвращает список телефонных номеров контакта.
     */
    public List<PhoneNumber> getPhoneNumbers() {
        return phoneNumbers;
    }
    /**
     * Добавляет новый телефонный номер к контакту.
     * @param phoneNumber номер телефона для добавления
     */
    public void addPhoneNumber(PhoneNumber phoneNumber) {
        phoneNumbers.add(phoneNumber);
        logger.debug("Добавлен номер телефона: {} (тип: {})", phoneNumber.getNumber(), phoneNumber.getType());
    }
    /**
     * Возвращает строковое представление контакта.
     */
    @Override
    public String toString() {
        String contact = name;
        logger.trace("Преобразование Contact в строку: {}", contact);
        return contact;
    }
}


