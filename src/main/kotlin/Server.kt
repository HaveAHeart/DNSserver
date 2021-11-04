import models.*
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
            val clazz = dnsMsg.question.qclass
            val ttl = 60

            val resources = getResource(qName, type, clazz, ttl)

            if (resources.isNotEmpty() && checkHeader(dnsMsg)) {
                dnsMsg.header.ancount = countResources(resources, type).toShort()
                dnsMsg.header.arcount = (resources.size - dnsMsg.header.ancount).toShort()
            }
            else errorFunc(dnsMsg, 3)

            dnsMsg.resList = resources
            val sentData = dnsMsg.toByteArray()
            val response = DatagramPacket(sentData, sentData.size, retAddress, retPort)
            socket.send(response)

            println("msg sent!")
        }
    }

    private fun countResources(res: List<Resource>, type: RecordType): Int {
        var count = 0
        for (entry in res) {
            if (entry.type == type)
                count++
        }
        return count
    }

    private fun getResource(name: String, rType: RecordType, rClass: RecordClass, ttl: Int): List<Resource>  {
        val filePath = when (rType) {
            is RecordType.A -> "src/main/NameLists/A.txt"
            is RecordType.MX -> "src/main/NameLists/MX.txt"
            is RecordType.TXT -> "src/main/NameLists/TXT.txt"
            is RecordType.AAAA -> "src/main/NameLists/AAAA.txt"
            is RecordType.NotImpl -> ""
        }
        //types: 1 15 16 28
        //resource: name type rClass=1 ttl rdlength rdata
        val file = File(filePath)
        val res = LinkedHashMap<String, RecordType>()
        val result = mutableListOf<Resource>()
        for (line in file.readLines()) {
            val address = line.split(SPACE_CHARACTER.toRegex(), 2)
            if (address[0] == name) {
                res[address[1]] = rType
            }
        }
        for (entry in res) {
            val rdLength: Int = when(rType) {
                is RecordType.A -> RecordType.A().size()
                is RecordType.MX -> (entry.key.split(COLON_CHARACTER).last()).length + 4
                is RecordType.AAAA -> RecordType.AAAA().size()
                is RecordType.TXT -> entry.key.length
                is RecordType.NotImpl -> throw exceptions.NotImplTypeException(NOT_IMPL_MSG)
            }
            result.add(Resource(name, rType, rClass, ttl, rdLength.toShort(), entry.key))
        }
        for (localRes in result) println(localRes.toString())
        //result : aaa.a xxx.xxx.xxx.xxx
        val additionalRes = mutableListOf<Resource>()
        for (resource in result) {
            if (resource.type == RecordType.of(15)) {
                val resName = resource.rdata.split(COLON_CHARACTER).last()
                println("looking for A for $resName")
                val resA = getResource(resName, RecordType.of(1), rClass, ttl)
                println("looking for AAAA for $resName")
                val resAAAA = getResource(resName, RecordType.of(28), rClass, ttl)
                additionalRes.addAll(resA)
                additionalRes.addAll(resAAAA)
            }
        }
        result.addAll(additionalRes)

        for (re in result) println(re.toString())

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