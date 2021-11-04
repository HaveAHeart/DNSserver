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
        val receiveBuf = ByteArray(MAX_PACKET_SIZE)
        val packet = DatagramPacket(receiveBuf, receiveBuf.size)
        val socket: DatagramSocket
        try {
            socket = DatagramSocket(PORT)
        } catch (e: SocketException) {
            println("Can not open the socket at chosen port ($PORT)")
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

            val resMap: LinkedHashMap<String, RecordType> = when (type) {
                is RecordType.A -> getInfo(qName, "src/main/NameLists/A.txt", RecordType.of(1))
                is RecordType.MX -> getInfo(qName, "src/main/NameLists/MX.txt", RecordType.of(15))
                is RecordType.TXT -> getInfo(qName, "src/main/NameLists/TXT.txt", RecordType.of(16))
                is RecordType.AAAA -> getInfo(qName, "src/main/NameLists/AAAA.txt", RecordType.of(28))
                is RecordType.NotImpl -> { LinkedHashMap() }
            }
            for (mutableEntry in resMap) {
                println("${mutableEntry.key}-->${mutableEntry.value}")
            }
            val resources = mutableListOf<Resource>()
            if (resMap.isNotEmpty() && checkHeader(dnsMsg)) {
                dnsMsg.header.ancount = countResources(resMap, type).toShort()
                dnsMsg.header.arcount = (resMap.size - dnsMsg.header.ancount).toShort()
                val clazz = dnsMsg.question.qclass
                val ttl = 60
                resMap.forEach { resource ->
                    val name = dnsMsg.question.qname
                    val rType = resource.value
                    val rdLength: Int = when(rType) {
                        is RecordType.A -> RecordType.A().size()
                        is RecordType.MX -> (resource.key.split(COLON_CHARACTER).last()).length + 4
                        is RecordType.AAAA -> RecordType.AAAA().size()
                        is RecordType.TXT -> resource.key.length
                        is RecordType.NotImpl -> throw exceptions.NotImplTypeException(NOT_IMPL_MSG)
                    }
                    resources.add(Resource(name, rType, clazz, ttl, rdLength.toShort(), resource.key))
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

    private fun countResources(resMap: LinkedHashMap<String, RecordType>, type: RecordType): Int {
        var count = 0
        for (mutableEntry in resMap) {
            if (mutableEntry.value == type)
                count++
        }
        return count
    }

    private fun getInfo(field: String, filePath: String, rType: RecordType): LinkedHashMap<String, RecordType>  {
        val file = File(filePath)
        val res = LinkedHashMap<String, RecordType>()
        val result = LinkedHashMap<String, RecordType>()
        for (line in file.readLines()) {
            val address = line.split(SPACE_CHARACTER.toRegex(), 2)
            if (address[0] == field) {
                res[address[1]] = rType
            }
        }
        result.putAll(res)
        if (rType == RecordType.of(15)) {
            for (resources in res) {
                val parsedRes = resources.key.split(COLON_CHARACTER).last()
                val aResources = getInfo(parsedRes, "src/main/NameLists/A.txt", RecordType.of(1))
                val aaaaResources = getInfo(parsedRes, "src/main/NameLists/AAAA.txt", RecordType.of(28))
                result.putAll(aResources)
                result.putAll(aaaaResources)
            }
        }
        return result
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