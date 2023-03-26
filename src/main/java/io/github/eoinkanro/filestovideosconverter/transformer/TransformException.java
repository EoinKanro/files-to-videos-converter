package io.github.eoinkanro.filestovideosconverter.transformer;

public class TransformException extends RuntimeException {

    public TransformException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransformException(String message) {
        super(message);
    }

}
