package org.jsignal.std;

import com.google.common.net.MediaType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public record Blob(byte[] data, MediaType mime) {
  public static Blob fromResource(String name, MediaType type) throws BlobException {
    try (var resource = Blob.class.getResourceAsStream(name)) {
      if (resource == null) {
        throw new BlobException(String.format("missing resource: %s", name));
      }
      return new Blob(resource.readAllBytes(), type);
    } catch (IOException e) {
      throw new BlobException(String.format("failed to read resource: %s", name), e);
    }
  }

  public static Blob fromPath(Path path) throws BlobException {
    String probedContentType = null;
    try {
      probedContentType = Files.probeContentType(path);
    } catch (IOException e) {
      throw new BlobException(String.format("failed to probe content type of path: %s", path), e);
    }

    try {
      return fromPath(path, MediaType.parse(probedContentType));
    } catch (IllegalArgumentException e) {
      throw new BlobException(String.format("failed to parse media type from probed content: %s", probedContentType), e);
    }
  }

  public static Blob fromPath(Path path, MediaType type) throws BlobException {
    try {
      return new Blob(Files.readAllBytes(path), type);
    } catch (IOException e) {
      throw new BlobException("failed to read path", e);
    }
  }
}