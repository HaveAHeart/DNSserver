package models

import models.Header.Companion.getHeaderFromByteArray
import models.Question.Companion.getQuestionFromByteArray
import nameToBytes
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream

class DNSMessage(var header: Header, var question: Question, var resList: List<Resource>) {

    companion object {
        fun parseByteArray(inData: ByteArray) : DNSMessage {
            val header = getHeaderFromByteArray(inData)
            val question = getQuestionFromByteArray(inData)
            val resources = getResourcesFromByteArray(inData)


            return DNSMessage(header, question, resources)
        }

        private fun getResourcesFromByteArray(inData: ByteArray): List<Resource> {
            TODO("Not yet implemented")
        }
    }

    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeShort(header.id.toInt())

        val flags = header.flags.toUShort()
        dos.writeShort(flags.toInt())

        dos.write(header.qdcount.toInt())
        dos.writeShort(header.ancount.toInt())
        dos.writeShort(header.nscount.toInt())
        dos.writeShort(header.arcount.toInt())
        dos.write(nameToBytes(question.qname))
        dos.writeShort(question.qtype.code.toInt())
        dos.writeShort(question.qclass.toInt())
        for (resource in resList) {
            dos.write(nameToBytes(resource.name))
            dos.writeShort(resource.type.code.toInt())
            dos.writeShort(resource.rclass.toInt())
            dos.write(resource.ttl)
            dos.writeShort(resource.rdlength.toInt())
            dos.write(Resource().rDataToByteArray(resource.type, resource.rdata, resource.rdlength))
        }
        return baos.toByteArray()
    }
}