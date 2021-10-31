package models

import Type

data class Resource(var name: String = "",
                    var type: Short = 0,
                    var rclass: Short = 0, //keyword class is reserved
                    var ttl: Int = 0,
                    var rdlength: Short = 0,
                    var rdata: String = "") { //size unknown
    override fun toString(): String {
        return "Resource(name='$name', type=$type, rclass=$rclass, ttl=$ttl, rdlength=$rdlength, rdata='$rdata')"
    }

    fun rDataToByteArray(type: Short, rdata: String, rLength: Short): ByteArray {
        var res = ByteArray(rLength.toInt())
        when (type.toInt()) {
            Type.A.code -> {
                val splitStr = rdata.split("\\.".toRegex())
                for ((i, byteStr) in splitStr.withIndex()) {
                    res[i] = byteStr.toUByte().toByte() //for binary representation
                }
            }
            Type.AAAA.code -> {
                val splitStr = rdata.split(":".toRegex())
                for ((i, byteStr) in splitStr.withIndex()) {
                    res[i] = byteStr.toUByte(radix = 16).toByte() //for binary representation
                }
            }
            Type.MX.code -> {
                val splitStr = rdata.split(":".toRegex())
                val preference = splitStr.first().toShort()
                val exchange = DNSMessage.nameToBytes(splitStr.last())

                val prefBytes = DNSMessage.shortToByteArray(preference)
                res[0] = prefBytes[0]
                res[1] = prefBytes[1]
                var iter = 2
                for (byte in exchange) {
                    res[iter] = byte
                    iter++
                }
                //TODO - see TODO in DNSMessage - parsename
            }
            Type.TXT.code -> {
                res = rdata.toByteArray(Charsets.US_ASCII) //TODO encodings?
            }
        }
        return res
    }
}