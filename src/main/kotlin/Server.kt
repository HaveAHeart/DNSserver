import models.DNSMessage
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

            val type = dnsMsg.question.qtype
            val qName = dnsMsg.question.qname

            var isCorrect: Boolean
            isCorrect = checkHeader(dnsMsg)

            var res = listOf<String>()
            when (type.toInt()) {
                Type.A.code -> {res = getInfo(qName, "src/main/NameLists/A.txt")}
                Type.MX.code -> {res = getInfo(qName, "src/main/NameLists/MX.txt")}
                Type.TXT.code -> {res = getInfo(qName, "src/main/NameLists/TXT.txt")}
                Type.AAAA.code -> {res = getInfo(qName, "src/main/NameLists/AAAA.txt")}
                else -> {/*package substitution*/}
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
                for (resource in res) {
                    var rData: String //TODO(data(type: Type))
                    var rdLength: Int
                    when {
                        rType.toInt() == Type.A.code -> {
                            rdLength = Type.A.size
                            //rData = TODO(data(type = A))
                        }
                        rType.toInt() == Type.MX.code -> {
                            rdLength = resource.length + 2
                            //rData = TODO(data(type = MX))
                        }
                        rType.toInt() == Type.AAAA.code -> {
                            rdLength = Type.AAAA.size
                            //rData = TODO(data(type = AAAA))
                        }
                        else -> {
                            rdLength = resource.length
                            //rData = TODO(data(type = TXT))
                        }
                    }
                    resources.add(Resource(name, rType, clazz, ttl, rdLength.toShort(), rData))
                }
            }

            val text = "HELLO THERE".toByteArray()
            val response = DatagramPacket(text, text.size, retAddress, retPort)
            socket.send(response)

        }
    }

    private fun getInfo(field: String, filePath: String) : List<String> {
        val file = File(filePath)
        val res = mutableListOf<String>()
        for (line in file.readLines()) {
            val address = line.split(SPACE_CHARACTER.toRegex(), 1)
            if (address[0] == field) {
                res.add(address[1])
            }
        }
        return res
    }

    private fun checkHeader(dnsMsg: DNSMessage): Boolean {
        val header = dnsMsg.header
        val flags = header.flags
        if (!flags.qr || flags.aa || flags.tc || flags.z.toInt() != 0
            || flags.rcode.toInt() != 0 || header.qdcount.toInt() == 0) {
            errorFunc(dnsMsg, 1)
            return false
        }
        else if (flags.opcode.toInt() != 0 || flags.rd || header.qdcount.toInt() > 1) {
            errorFunc(dnsMsg, 4)
            return false
        }
        return true
    }

    private fun errorFunc(dnsMsg: DNSMessage, rCode: Int) {
        dnsMsg.header.flags.qr = true
        dnsMsg.header.flags.rcode = rCode.toShort()
    }
}