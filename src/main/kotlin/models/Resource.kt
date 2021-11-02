package models

import COLON_CHARACTER
import DOT_CHARACTER
import nameToBytes
import shortToByteArray

data class Resource(var name: String = "", var type: RecordType = RecordType.of(1), var rclass: Short = 0,
                    var ttl: Int = 0, var rdlength: Short = 0, var rdata: String = "") {

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
                    res[i] = byteStr.toUByte(radix = 16).toByte()
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
                //TODO - see TODO in DNSMessage - parsename
            }
            is RecordType.TXT -> {
                res = rdata.toByteArray(Charsets.US_ASCII) //TODO encodings?
            }
            is RecordType.NotImpl -> {TODO()}
        }
        return res
    }
}