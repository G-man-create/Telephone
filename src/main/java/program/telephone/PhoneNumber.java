package program.telephone;
import java.io.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 * Класс, представляющий номер телефона в телефонной книге.
 * Содержит информацию о номере телефона и его типе (мобильный, домашний и т.д.).
 * Реализует интерфейс Serializable для возможности сериализации объектов.
 * @see Contact
 * @see Serializable
 */
class PhoneNumber implements Serializable {
    private static final Logger logger = LogManager.getLogger(PhoneNumber.class);
    /** Номер телефона в строковом формате
     */
    private String number;
    /** Тип номера телефона (мобильный, домашний и т.д.)
     */
    private String type;
    /**
     * Создает новый экземпляр номера телефона.
     * @see #getNumber()
     * @see #getType()
     */
    public PhoneNumber(String number, String type) {
        this.number = number;
        this.type = type;
        logger.debug("Создан новый номер телефона: {} (тип: {})", number, type);
    }
    /**
     * Возвращает номер телефона.
     * @return строковое представление номера телефона
     * @see #PhoneNumber(String, String)
     * @see #getType()
     */
    public String getNumber() {
        return number;
    }
    /**
     * Возвращает тип номера телефона.
     * @return тип номера (например, "Мобильный", "Домашний" и т.д.)
     * @see #PhoneNumber(String, String)
     * @see #getNumber()
     */
    public String getType() {
        return type;
    }
    /**
     * Возвращает строковое представление номера телефона в формате "Тип: Номер".
     * @return отформатированная строка с информацией о номере
     */
    @Override
    public String toString() {
        String phone= type + ": " + number;
        logger.trace("Преобразование PhoneNumber в строку: {}", phone);
        return phone;
    }
}