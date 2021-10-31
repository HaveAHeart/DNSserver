package models

import DOT_CHARACTER
import models.Header.Companion.getHeaderFromByteArray
import models.Question.Companion.getQuestionFromByteArray
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

class DNSMessage(var header: Header, var question: Question, var resList: MutableList<Resource>) {

    companion object {
        fun parseByteArray(inData: ByteArray) : DNSMessage {

            val header = getHeaderFromByteArray(inData)
            val question = getQuestionFromByteArray(inData)
            //TODO(parseResources)
            val resList = mutableListOf<Resource>()
            return DNSMessage(header, question, resList)
        }

        fun nameToBytes(inName: String): ByteArray {
            //my.domain.at.com -> my domain at com -> 2 M Y 6 D O M A I N 2 A T 3 C O M 0
            val parsedDomain = inName.split(DOT_CHARACTER)
            val qName = ByteArray(inName.length + 2)
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
            return qName
        }

        fun parseName(initPointer: Int) {
            //TODO - parse name with  pointers
        }

        fun shortToByteArray(inShort: Short): ByteArray {
            val buffer = ByteBuffer.allocate(2)
            buffer.putShort(inShort)
            return buffer.array()
        }
    }

    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeShort(header.id.toInt())

        val flags = header.flags.toUShort()
        dos.writeShort(flags.toInt())

        dos.writeShort(header.qdcount.toInt())
        dos.writeShort(header.ancount.toInt())
        dos.writeShort(header.nscount.toInt())
        dos.writeShort(header.arcount.toInt())
        dos.write(nameToBytes(question.qname))
        dos.writeShort(question.qtype.toInt())
        dos.writeShort(question.qclass.toInt())
        for (resource in resList) {
            dos.write(nameToBytes(resource.name))
            dos.writeShort(resource.type.toInt())
            dos.writeShort(resource.rclass.toInt())
            dos.write(resource.ttl)
            dos.writeShort(resource.rdlength.toInt())
            dos.write(Resource().rDataToByteArray(resource.type, resource.rdata, resource.rdlength))
        }

        return baos.toByteArray()
    }

    override fun toString(): String {
        return "DNSMessage(header=$header\nquestion=$question\nresList=$resList)"
    }



}