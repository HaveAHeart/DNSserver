import java.net.DatagramPacket
import java.net.DatagramSocket

class Server {
    fun run() {
        val socket = DatagramSocket(5000)
        val receiveBuf = ByteArray(512)
        val packet = DatagramPacket(receiveBuf, receiveBuf.size)

        println("Port opened!")

        while (true) {
            println("waiting")
            socket.receive(packet)
            println("gotcha")
            val retAddress = packet.address
            val retPort = packet.port
            val data = packet.data


            for (i in data) {
                println(i)
            }

            val text = "HELLO THERE".toByteArray()
            val response = DatagramPacket(text, text.size, retAddress, retPort)
            socket.send(response)

        }
    }
}