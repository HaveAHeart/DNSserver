package models

import java.nio.ByteBuffer

data class Header(var id: Short = 0,
                  var flags: Flags = Flags(),
                  var qdcount: Short = 1,
                  var ancount: Short = 0,
                  var nscount: Short = 0,
                  var arcount: Short = 0) {

    companion object {
        fun getHeaderFromByteArray(headerBytes: ByteArray) : Header {
            val id = byteSubsequence(headerBytes, 0, 2).short
            val flags = Flags.shortToFlags(byteSubsequence(headerBytes, 2, 4).short)
            val qdCount = byteSubsequence(headerBytes, 4, 6).short
            val anCount = byteSubsequence(headerBytes, 6, 8).short
            val nsCount = byteSubsequence(headerBytes, 8, 10).short
            val arCount = byteSubsequence(headerBytes, 10, 12).short
            return Header(id, flags, qdCount, anCount, nsCount, arCount)
        }

        private fun byteSubsequence(array: ByteArray, start: Int, end : Int) =
            ByteBuffer.wrap(array.copyOfRange(start, end))
    }

    override fun toString(): String {
        return "Header(id=$id, flags=$flags,  qdcount=$qdcount, ancount=$ancount, nscount=$nscount, arcount=$arcount)"
    }
}