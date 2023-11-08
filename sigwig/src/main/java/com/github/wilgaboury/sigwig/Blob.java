package com.github.wilgaboury.sigwig;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Blob {
    private static final Logger logger = Logger.getLogger(Blob.class.getName());

    private final byte[] data;
    private final MediaType mime;

    public Blob(byte[] data, MediaType mime) {
        this.data = data;
        this.mime = mime;
    }

    public byte[] getData() {
        return data;
    }

    public MediaType getMime() {
        return mime;
    }

    public static Blob fromResource(String name, MediaType type) {
        try (var resource = Blob.class.getResourceAsStream(name)) {
            if (resource == null) {
                logger.log(Level.SEVERE, String.format("missing resource: %s", name));
                return null;
            }

            return new Blob(resource.readAllBytes(), type);
        } catch (IOException e) {
            logger.log(Level.SEVERE, String.format("failed to read resource: %s", name), e);
            return null;
        }
    }
}
