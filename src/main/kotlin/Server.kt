import models.DNSMessage
import models.RecordType
import models.Resource
import models.ResponseCode
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import kotlin.system.exitProcess

class Server {
    fun run() {
        val receiveBuf = ByteArray(512)
        val packet = DatagramPacket(receiveBuf, receiveBuf.size)
        val port = 53
        val socket: DatagramSocket
        try {
            socket = DatagramSocket(port)
        } catch (e: SocketException) {
            println("Can not open the socket at chosen port ($port)")
            exitProcess(1)
        }
        while (true) {
            println("waiting...")
            socket.receive(packet)
            println("msg received!")
            val retAddress = packet.address
            val retPort = packet.port
            val dnsMsg = DNSMessage.parseByteArray(packet.data)
            dnsMsg.header.flags.qr = true //sending an answer
            val type = dnsMsg.question.qtype
            val qName = dnsMsg.question.qname

            val res: List<String> = when (type) {
                is RecordType.A -> getInfo(qName, "src/main/NameLists/A.txt")
                is RecordType.MX -> getInfo(qName, "src/main/NameLists/MX.txt")
                is RecordType.TXT -> getInfo(qName, "src/main/NameLists/TXT.txt")
                is RecordType.AAAA -> getInfo(qName, "src/main/NameLists/AAAA.txt")
                is RecordType.NotImpl -> { listOf() }
            }

            val resources = mutableListOf<Resource>()
            if (res.isNotEmpty() && checkHeader(dnsMsg)) {
                dnsMsg.header.ancount = res.size.toShort() //TODO arcount
                val name = dnsMsg.question.qname
                val rType = dnsMsg.question.qtype
                val clazz = dnsMsg.question.qclass
                val ttl = 60
                res.forEach { resource ->
                    val rdLength: Int = when(type) {
                        is RecordType.A -> RecordType.A().size()
                        is RecordType.MX -> (resource.split(COLON_CHARACTER).last()).length + 2
                        is RecordType.AAAA -> RecordType.AAAA().size()
                        is RecordType.TXT -> resource.length
                        is RecordType.NotImpl -> throw exceptions.NotImplTypeException(NOT_IMPL_MSG)
                    }
                    resources.add(Resource(name, rType, clazz, ttl, rdLength.toShort(), resource))
                }
            }
            else errorFunc(dnsMsg, 3)

            dnsMsg.resList = resources
            val sentData = dnsMsg.toByteArray()
            val response = DatagramPacket(sentData, sentData.size, retAddress, retPort)
            socket.send(response)

            println("msg sent!")
        }
    }

    private fun getInfo(field: String, filePath: String): List<String> =
        File(filePath).readLines()
            .map { it.split(SPACE_CHARACTER.toRegex(), 2) }
            .filter { it[0] == field }
            .map { it[1] }


    private fun checkHeader(dnsMsg: DNSMessage): Boolean {
        val header = dnsMsg.header
        val flags = header.flags
        if (!flags.qr || flags.aa || flags.tc || flags.z.toInt() != 0
            || flags.rcode.code.toInt() != 0 || header.qdcount.toInt() == 0) {
            errorFunc(dnsMsg, 1)
            return false
        }
        else if (flags.opcode.code.toInt() != 0 || flags.rd || header.qdcount.toInt() > 1) {
            errorFunc(dnsMsg, 4)
            return false
        }
        return true
    }

    private fun errorFunc(dnsMsg: DNSMessage, rCode: Int) {
        dnsMsg.header.flags.rcode = ResponseCode.of(rCode.toShort())
    }
}