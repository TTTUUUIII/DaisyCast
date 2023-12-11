package cn.island.daisycast.transport.rx.impl

import cn.island.daisycast.transport.rx.Receiver
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import java.nio.ByteBuffer

class WebsocketReceiver(val wsUrl: String): WebSocketClient(URI(wsUrl)), Receiver {

    private var mCallback: ((ByteArray) -> Unit)? = null

    override fun onOpen(handshakedata: ServerHandshake?) {
        println("WebsocketReceiver open")
    }

    override fun onMessage(message: String?) {
        /*Ignored*/
    }

    override fun onMessage(bytes: ByteBuffer?) {
        super.onMessage(bytes)
        bytes?.let {buffer ->
            mCallback?.let {
                it(buffer.array())
            }
        }
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        println("WebsocketReceiver close")
    }

    override fun onError(ex: Exception?) {
        System.err.println("WebsocketReceiver error: $ex")
    }

    override fun open() {
        println("WebsocketReceiver open")
        connect()
    }

    override fun close() {
        super.close()
    }

    override fun setOnFrameCallback(callback: (ByteArray) -> Unit) {
        mCallback = callback
    }
}