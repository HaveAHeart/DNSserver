package models

import DOT_CHARACTER
import SPACE_CHARACTER
import models.Header.Companion.getHeaderFromByteArray
import models.Question.Companion.getQuestionFromByteArray
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.nio.ByteBuffer

class DNSMessage(val header: Header, val question: Question, val resList: MutableList<Resource>) {

    companion object {
        fun parseByteArray(inData: ByteArray) : DNSMessage {

            val header = getHeaderFromByteArray(inData.copyOfRange(0, 12))
            val question = getQuestionFromByteArray(inData)
            //TODO(parseResources)

            return DNSMessage(header, question, mutableListOf())
        }
    }

    fun toByteArray(): ByteArray {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeShort(header.id.toInt())

        //flags stuff - omg I have lack of straight bit access in java\kotlin >:C
        val flags = header.flags.toShort()
        dos.writeShort(flags.toInt())

        dos.writeShort(header.qdcount.toInt())
        dos.writeShort(header.ancount.toInt())
        dos.writeShort(header.nscount.toInt())
        dos.writeShort(header.arcount.toInt())
        println("-------------------")
        dos.write(Question.qNameToBytes(question.qname))
        println(question.qname)
        dos.writeShort(question.qtype.toInt())
        println(question.qtype.toInt())
        dos.writeShort(question.qclass.toInt())
        println(question.qclass.toInt())
        println("-------------------")
        for (i in resList) {
            //TODO
        }
        println(header.id.toString(radix = 2))
        println(header.qdcount.toString(radix = 2))
        println(header.ancount.toString(radix = 2))
        println(header.nscount.toString(radix = 2))
        println(header.arcount.toString(radix = 2))

        for (i in baos.toByteArray()) println(i.toString(radix = 2))
        return baos.toByteArray()
    }

    override fun toString(): String {
        return "DNSMessage(header=$header\nquestion=$question\nresList=$resList)"
    }

}