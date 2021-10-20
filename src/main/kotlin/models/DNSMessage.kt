package models

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.lang.reflect.Field
import java.nio.ByteBuffer
import java.nio.ByteOrder

class DNSMessage(val header: Header, val question: Question, val resList: MutableList<Resource>) {
    fun parseByteArray(inData: ByteArray) {
        //TODO
    }

    fun toByteArray(): ByteArray {
        val result = ByteArray(512)
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        dos.writeShort(header.id.toInt())

        //flags stuff - omg I have lack of straight bit access in java\kotlin >:C
        val flagsStr =
                    getBitFromBool(header.qr) +
                    getBitsFromShort(header.opcode) +
                    getBitFromBool(header.aa) +
                    getBitFromBool(header.tc) +
                    getBitFromBool(header.rd) +
                    getBitFromBool(header.ra) +
                    "000" + //z here - always 0
                    getBitsFromShort(header.rcode)
        val flags = flagsStr.toShort(radix = 2)
        dos.writeShort(flags.toInt())

        dos.writeShort(header.qdcount.toInt())
        dos.writeShort(header.ancount.toInt())
        dos.writeShort(header.nscount.toInt())
        dos.writeShort(header.arcount.toInt())
        println("-------------------")
        dos.write(question.qname)
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
        println(flagsStr)
        println(header.qdcount.toString(radix = 2))
        println(header.ancount.toString(radix = 2))
        println(header.nscount.toString(radix = 2))
        println(header.arcount.toString(radix = 2))

        for (i in baos.toByteArray()) println(i.toString(radix = 2))
        return baos.toByteArray()
    }

    private fun getBitFromBool(inBool: Boolean): Char = if (inBool) '1' else '0'
    private fun getBitsFromShort(inShort: Short): String {
        return when (inShort.toInt()) {
            0 -> "0000"
            1 -> "0001"
            2 -> "0010"
            3 -> "0011"
            4 -> "0100"
            5 -> "0101"
            else -> "1111" //both rcode and opcode can not be more than 5
        }
    }
}