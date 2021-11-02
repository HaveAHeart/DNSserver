import models.DNSMessage
import models.ResponseCode
import models.RecordType
import models.Resource
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket

class Server {
    fun run() {
        val socket = DatagramSocket(5000)
        val receiveBuf = ByteArray(512)
        val packet = DatagramPacket(receiveBuf, receiveBuf.size)

        while (true) {
            println("waiting")
            socket.receive(packet)
            val retAddress = packet.address
            val retPort = packet.port
            val dnsMsg = DNSMessage.parseByteArray(packet.data)
            dnsMsg.header.flags.qr = true //doing an answer
            val type = dnsMsg.question.qtype
            val qName = dnsMsg.question.qname

            var isCorrect = checkHeader(dnsMsg)

            val res: List<String> = when (type) {
                is RecordType.A -> getInfo(qName, "src/main/NameLists/models.A.txt")
                is RecordType.MX -> getInfo(qName, "src/main/NameLists/models.MX.txt")
                is RecordType.TXT -> getInfo(qName, "src/main/NameLists/models.TXT.txt")
                is RecordType.AAAA -> getInfo(qName, "src/main/NameLists/models.AAAA.txt")
                is RecordType.NotImpl -> { listOf()}
            }

            if (res.isEmpty()) {
                errorFunc(dnsMsg, 3)
                isCorrect = false
            }
            val resources = mutableListOf<Resource>()
            if (isCorrect) {
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
                        is RecordType.NotImpl -> throw exceptions.NotImplTypeException("This record type is not implemented")
                    }
                    resources.add(Resource(name, rType, clazz, ttl, rdLength.toShort(), resource))
                }
            }
            dnsMsg.resList = resources
            val sentData = dnsMsg.toByteArray()
            val response = DatagramPacket(sentData, sentData.size, retAddress, retPort)
            socket.send(response)
        }
    }

    private fun getInfo(field: String, filePath: String): List<String> {
        return File(filePath).readLines()
            .map { it.split(SPACE_CHARACTER.toRegex(), 2) }
            .filter { it[0] == field }
            .map { it[1] }
    }

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