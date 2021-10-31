import java.net.*
import kotlin.random.Random
import models.DNSMessage
import models.Header
import models.Question
import java.lang.NumberFormatException
import java.nio.charset.Charset
import java.util.*
import kotlin.system.exitProcess

class Client {
    private val scanner = Scanner(System.`in`)

    fun run() {
        println("Enter the type (A, AAAA, MX, TXT)")
        val type: Short = when (scanner.nextLine().toUpperCase()) {
            "A" -> Type.A.code.toShort()
            "AAAA" -> Type.AAAA.code.toShort()
            "MX" -> Type.MX.code.toShort()
            "TXT" -> Type.TXT.code.toShort()
            else -> {
                println("Such type not found")
                exitProcess(1)
            }
        }

        println("Enter the domain name you wanna find info about")
        val domainName = scanner.nextLine()

        println("Enter the ip address of the dns server you wanna connect to")
        val ipText = scanner.nextLine()

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

        val addr = InetSocketAddress(ipText, port) //TODO try catch for ip exceptions
        val reqId = Random.nextInt(Short.MAX_VALUE + 1).toShort()

        val question = Question(
            qname = domainName, qtype = type, qclass = 1 //IN
        )

        val header = Header(id = reqId)

        val dnsMsg = DNSMessage(header, question, mutableListOf())
        val sendData = dnsMsg.toByteArray()

        val socket = DatagramSocket()
        socket.connect(addr.address, addr.port)

        val packet = DatagramPacket(sendData, sendData.size, addr.address, addr.port)
        socket.send(packet)

        val receiveBuf = ByteArray(512)
        val response = DatagramPacket(receiveBuf, receiveBuf.size)
        socket.receive(response)

        val data = response.data
        val retDNSMessage = DNSMessage.parseByteArray(data)
        println(retDNSMessage.toString())
    }
}