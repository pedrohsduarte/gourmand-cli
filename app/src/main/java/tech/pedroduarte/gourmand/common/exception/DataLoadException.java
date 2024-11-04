package tech.pedroduarte.gourmand.common.exception;

/**
 * Exception thrown when there are errors loading or parsing data files.
 */
public class DataLoadException extends RuntimeException {

    public DataLoadException(String message) {
        super(message);
    }

    public DataLoadException(String message, Throwable cause) {
        super(message, cause);
    }

}
