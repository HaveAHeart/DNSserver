package models

import COLON_CHARACTER
import DOT_CHARACTER
import byteSubsequence
import byteToHex
import byteToInt
import bytesToName
import exceptions.NotImplTypeException
import nameToBytes
import shortToByteArray

data class Resource(var name: String = "", var type: RecordType = RecordType.of(1), var rclass: Short = 0,
                    var ttl: Int = 0, var rdlength: Short = 0, var rdata: String = "") {

    companion object {
        fun getResourceFromByteArray(inData: ByteArray, initIndex: Int): Pair<Resource, Int> {
            for ((i, byte) in inData.withIndex()) { println("$i ::: $byte") }
            var i = initIndex
            val nPair = bytesToName(i, inData)
            val rName = nPair.first
            i = nPair.second
            val rType =  RecordType.of(byteSubsequence(inData, i, i + 2).short)
            val rClass =  byteSubsequence(inData, i + 2, i + 4).short
            val rTtl = byteSubsequence(inData, i + 4, i + 8).int
            val rdLength = byteSubsequence(inData, i + 8, i + 10).short.toUShort().toInt()
            i += 10
            var rData = ""
            when (rType) {
                is RecordType.A -> {

                    rData += byteToInt(inData[i])
                    for (j in 1 until rdLength) {
                        rData += ".${byteToInt(inData[i + j])}"
                    }
                }
                is RecordType.AAAA -> {
                    rData += "${byteToHex(inData[i])}${byteToHex(inData[i + 1])}"
                    for (j in 2 until rdLength step 2) {
                        rData += ":${byteToHex(inData[i + j])}${byteToHex(inData[i + j + 1])}"
                    }
                }
                is RecordType.TXT -> {
                    for (j in 0 until rdLength) {
                        rData += inData[i + j].toChar()
                    }
                }
                is RecordType.MX -> {
                    val rPair = bytesToName(i + 2, inData)
                    rData += rPair.first
                }
                is RecordType.NotImpl -> {throw NotImplementedError()}
            }
            val resource = Resource(rName, rType, rClass, rTtl, rdLength.toShort(), rData)
            return Pair(resource, i + rdLength)
        }
    }


    fun rDataToByteArray(type: RecordType, rdata: String, rLength: Short): ByteArray {
        var res = ByteArray(rLength.toInt())
        when (type) {
            is RecordType.A -> {
                val splitStr = rdata.split(DOT_CHARACTER)
                splitStr.forEachIndexed{i, byteStr ->
                    res[i] = byteStr.toUByte().toByte()
                }
            }
            is RecordType.AAAA -> {
                val splitStr = rdata.split(COLON_CHARACTER)
                splitStr.forEachIndexed{i, byteStr ->
                    res[i * 2] = byteStr.substring(0, 2).toUByte(radix = 16).toByte()
                    res[i * 2 + 1] = byteStr.substring(2, 4).toUByte(radix = 16).toByte()
                }
            }
            is RecordType.MX -> {
                val splitStr = rdata.split(COLON_CHARACTER)
                val preference = splitStr.first().toShort()
                val exchange = nameToBytes(splitStr.last())

                val prefBytes = shortToByteArray(preference)
                res[0] = prefBytes[0]
                res[1] = prefBytes[1]
                exchange.forEachIndexed { index, byte ->
                    res[index + 2] = byte //May cause problems :)
                }
            }
            is RecordType.TXT -> {
                res = rdata.toByteArray(Charsets.US_ASCII)
            }
            is RecordType.NotImpl -> {throw NotImplTypeException("This record type is not supported.")}
        }
        return res
    }
}