package com.github.wilgaboury.sigwig

import com.google.common.net.MediaType
import java.io.IOException
import java.lang.AssertionError
import java.util.logging.Level
import java.util.logging.Logger

class Blob(val data: ByteArray, val mime: MediaType) {
    companion object {
        private val logger = Logger.getLogger(Blob::class.java.getName())

        fun fromResource(name: String?, type: MediaType): Blob {
            try {
                Blob::class.java.getResourceAsStream(name).use { resource ->
                    if (resource == null) {
                        val msg = String.format("missing resource: %s", name)
                        logger.log(Level.SEVERE, msg)
                        throw AssertionError(msg)
                    }
                    return Blob(resource.readAllBytes(), type)
                }
            } catch (e: IOException) {
                val msg = String.format("failed to read resource: %s", name)
                logger.log(Level.SEVERE, msg, e)
                throw AssertionError(msg)
            }
        }
    }
}
