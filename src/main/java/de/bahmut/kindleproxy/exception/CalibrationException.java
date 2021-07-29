package de.bahmut.kindleproxy.exception;

public class CalibrationException extends Exception {

    public CalibrationException(String message) {
        super(message);
    }

    public CalibrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public CalibrationException(Throwable cause) {
        super(cause);
    }

}
