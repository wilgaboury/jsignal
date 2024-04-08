package com.github.wilgaboury.sigwig;

public class BlobException extends Exception {
    public BlobException(String message) {
        super(message);
    }

    public BlobException(String message, Throwable cause) {
        super(message, cause);
    }
}
