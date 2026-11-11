package dev.opencover.desk

import android.net.LocalServerSocket
import android.net.LocalSocket
import android.util.Log
import java.io.DataInputStream

/** Cursor drawing only. ADB's shell is already authorized to inject input; ordinary apps cannot use this socket. */
class DeskRemoteCursor(private val displayId: Int, private val width: Int, private val height: Int,
                       private val cursor: IDeskCursor) {
    @Volatile private var running = true
    @Volatile private var server: LocalServerSocket? = null
    @Volatile private var client: LocalSocket? = null

    fun start() {
        Thread({
            try {
                val listener = LocalServerSocket("opencover_cursor_$displayId")
                server = listener
                if (!running) { listener.close(); return@Thread }
                while (running) {
                    val socket = listener.accept()
                    client = socket
                    try {
                        val uid = socket.peerCredentials.uid
                        Log.i("OpenCoverCursor", "cursor peer uid=$uid display=$displayId")
                        if (uid == 2000) {
                            val input = DataInputStream(socket.inputStream)
                            while (running) {
                                val id = input.readInt()
                                val x = input.readFloat(); val y = input.readFloat()
                                val down = input.readInt() != 0
                                if (id == displayId && x.isFinite() && y.isFinite()) {
                                    cursor.moved(x.coerceIn(0f, width - 1f), y.coerceIn(0f, height - 1f), down)
                                }
                            }
                        }
                    } catch (e: Exception) { Log.i("OpenCoverCursor", "cursor disconnected: ${e.javaClass.simpleName}") }
                    finally { runCatching { socket.close() }; client = null }
                }
            } catch (e: Exception) { Log.i("OpenCoverCursor", "cursor listener stopped: ${e.javaClass.simpleName}") }
            finally { runCatching { server?.close() }; server = null }
        }, "desk-remote-cursor").start()
    }

    fun stop() {
        running = false
        runCatching { client?.close() }
        runCatching { server?.close() }
    }
}
