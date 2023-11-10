package com.github.wilgaboury.sigwig.examples

import com.github.wilgaboury.jsignal.Effect
import com.github.wilgaboury.jsignal.ReactiveUtil.*
import com.github.wilgaboury.jsignal.interfaces.SignalLike
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * Reactive port, server automatically restarts whenever port is changed
 */
class Server(port: Int) {
    private val port: SignalLike<Int>
    private val serverStartEffect: Effect

    init {
        this.port = createAtomicSignal(port)
        serverStartEffect = createServerStartEffect()
    }

    fun setPort(port: Int) {
        this.port.accept(port)
    }

    private fun createServerStartEffect(): Effect {
        return createAsyncEffect(deferProvideAsyncExecutor {
            val socket = createServerSocket()
            onCleanup {
                logger.log(Level.INFO, "running socket cleanup")
                closeServerSocket(socket)
            }
            val thread = Thread { serverLoop(socket) }
            thread.start()
        })
    }

    private fun serverLoop(serverSocket: ServerSocket) {
        try {
            while (true) {
                handleConnection(serverSocket.accept())
            }
        } catch (e: SocketException) {
            logger.log(Level.INFO, "socket closed")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun createServerSocket(): ServerSocket {
        return try {
            ServerSocket(port.get())
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun closeServerSocket(socket: ServerSocket) {
        try {
            socket.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    private fun handleConnection(socket: Socket) {
        println("connection accepted")
    }

    companion object {
        private val logger = Logger.getLogger(Server::class.java.getName())
    }
}
