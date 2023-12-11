package cn.island.daisycast.transport.tx

interface Transmitter {
    fun open()
    fun write(byteArray: ByteArray)
    fun close()
}