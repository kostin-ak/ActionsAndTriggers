package kostin.ak.actionstriggers.api.parser;

/**
 * Исключение, выбрасываемое при критических ошибках в конфигурации (Fail-Fast).
 */
public class AATParseException extends RuntimeException {
    public AATParseException(String message) {
        super(message);
    }

    public AATParseException(String message, Throwable cause) {
        super(message, cause);
    }
}