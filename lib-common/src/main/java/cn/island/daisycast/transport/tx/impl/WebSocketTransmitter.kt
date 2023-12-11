package cn.island.daisycast.transport.tx.impl

import cn.island.daisycast.transport.tx.Transmitter
import org.java_websocket.WebSocket
import org.java_websocket.enums.ReadyState
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress
import java.util.concurrent.Executors

class WebSocketTransmitter(port: Int): WebSocketServer(InetSocketAddress(port)), Transmitter {

//    private val mConnections = mapOf<String, WebSocket>()
    private val mConnections = mutableListOf<WebSocket>()
    private val mExecutor = Executors.newSingleThreadExecutor()

    override fun open() {
        mExecutor.submit {
            start()
        }
    }

    override fun write(byteArray: ByteArray) {
        mExecutor.submit {
            mConnections.forEach {
                if (it.readyState == ReadyState.OPEN) {
                    it.send(byteArray)
                }
            }
        }
    }

    override fun close() {
        mExecutor.submit {
            mConnections.forEach {
                it.close()
            }
        }
    }

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        conn?.let {
            mConnections.add(it)
        }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        mConnections.remove(conn)
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        /*ignored*/
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        System.err.println("WebsocketTransmitter error: $ex")
    }

    override fun onStart() {
        println("WebsocketTransmitter server started")
    }
}