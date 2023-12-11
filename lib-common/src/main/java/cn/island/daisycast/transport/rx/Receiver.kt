package cn.island.daisycast.transport.rx

interface Receiver {
    fun open()
    fun close()
    fun setOnFrameCallback(callback: (ByteArray) -> Unit)
}