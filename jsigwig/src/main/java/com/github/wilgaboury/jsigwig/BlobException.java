package com.github.wilgaboury.jsigwig;

public class BlobException extends Exception {
    public BlobException(String message) {
        super(message);
    }

    public BlobException(String message, Throwable cause) {
        super(message, cause);
    }
}
