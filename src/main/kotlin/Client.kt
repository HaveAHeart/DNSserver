import java.net.*
import kotlin.random.Random
import models.DNSMessage
import models.Header
import models.Question
import java.lang.NumberFormatException
import java.util.*
import kotlin.system.exitProcess

class Client {
    private val scanner = Scanner(System.`in`)

    fun run() {
        println("Enter the type (A, AAAA, MX, TXT)")
        val type: Short = when (scanner.nextLine()) {
            "A" -> 1
            "AAAA" -> 28
            "MX" -> 15
            "TXT" -> 16
            else -> {
                println("idiot")
                exitProcess(1)
            }
        }

        println("Enter the domain name you wanna find info about")
        val domainName = scanner.nextLine()

        println("Enter the ip address of the dns server you wanna connect to")
        val ipText = scanner.nextLine()

        println("Enter the port of dns server or leave empty for default (53)")
        val portText = scanner.nextLine()

        val port = if (portText.isBlank()) 53
        else {
            try { portText.toInt() }
            catch (ex: NumberFormatException) {
                println("idiot")
                exitProcess(1)
            }
        }

        val addr = InetSocketAddress(ipText, port) //TODO try catch for ip exceptions
        val reqId = Random.nextInt(Short.MAX_VALUE + 1).toShort()

        val header = Header(
            id = reqId,
            qr = false, opcode = 0, aa = false, tc = false, rd = false, ra = false, z = 0, rcode = 0,
            qdcount = 1, ancount = 0, nscount = 0, arcount = 0
        )

        //my.domain.at.com -> my domain at com -> 2 M Y 6 D O M A I N 2 A T 3 C O M 0
        val parsedDomain = domainName.split(".")
        println(">>>>>>>>>>>>>>>>>>>${parsedDomain.size}")
        for (i in parsedDomain) println("----------$i")
        val qName = ByteArray(domainName.length + 2) // dot -> number + 1 and zero in the end
        var byteIter = 0
        for (subDomain in parsedDomain) {
            qName[byteIter] = subDomain.length.toByte()
            byteIter++
            for (letter in subDomain) {
                qName[byteIter] = letter.toByte() //TODO encodings?
                byteIter++
            }
        }
        qName[byteIter] = 0

        val question = Question(
            qname = qName, qtype = type, qclass = 1 //IN
        )

        val dnsMsg = DNSMessage(header, question, mutableListOf())
        val sendData = dnsMsg.toByteArray()

        val socket = DatagramSocket()
        socket.connect(addr.address, addr.port)



        val packet = DatagramPacket(sendData, sendData.size, addr.address, addr.port)
        socket.send(packet)


        //val response = DatagramPacket(receiveBuf, receiveBuf.size)
        //socket.receive(response)

        //val data = response.data
        //for (i in data) { println(i) }
    }
}