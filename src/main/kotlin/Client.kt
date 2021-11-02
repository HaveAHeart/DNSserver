import exceptions.NotImplTypeException
import java.net.*
import kotlin.random.Random
import models.DNSMessage
import models.Header
import models.Question
import models.RecordType
import java.lang.NumberFormatException
import java.nio.charset.Charset
import java.util.*
import kotlin.system.exitProcess

class Client {
    private val scanner = Scanner(System.`in`)

    fun run() {
        println("Enter the type (A, AAAA, MX, TXT)")
        val type: RecordType = when (scanner.nextLine().toUpperCase()) {
            "A" -> RecordType.of(1)
            "AAAA" -> RecordType.of(28)
            "MX" -> RecordType.of(15)
            "TXT" -> RecordType.of(16)
            else -> {
                println("This record type is not implemented")
                exitProcess(1)
            }
        }

        println("Enter the domain name you wanna find info about")
        val domainName = scanner.nextLine()
        //TODO(Validate domainName)

        println("Enter the ip address of the dns server you wanna connect to")
        val ipText = scanner.nextLine()
        //TODO(Validate dnsServer ip address)

        println("Enter the port of dns server or leave empty for default (53)")
        val portText = scanner.nextLine()
        if (ipText.isBlank() || portText.isBlank() || domainName.isBlank()) {
            println("One of the field was empty.")
            exitProcess(1)
        }

        val port = try { portText.toInt() }
        catch (ex: NumberFormatException) {
            println("idiot")
            exitProcess(1)
        }
        send(ipText, port, domainName, type)
    }

    private fun send(ipText: String, port: Int, domainName: String, type: RecordType) {
        val addr = InetSocketAddress(ipText, port) //TODO try catch for ip exceptions
        val reqId = Random.nextInt(Short.MAX_VALUE + 1).toShort()

        val header = Header(id = reqId)
        val question = Question(qname = domainName, qtype = type, qclass = 1)
        val dnsMsg = DNSMessage(header, question, listOf())

        val sendData = dnsMsg.toByteArray()
        val socket = DatagramSocket()
        socket.use { datagramSocket ->
            datagramSocket.connect(addr.address, addr.port)
            val packet = DatagramPacket(sendData, sendData.size, addr.address, addr.port)
            datagramSocket.send(packet)

            val receiveBuf = ByteArray(512)
            val response = DatagramPacket(receiveBuf, receiveBuf.size)
            datagramSocket.receive(response)

            val data = response.data
            val retDNSMessage = DNSMessage.parseByteArray(data)
            println(retDNSMessage.toString())
        }
    }
}