import java.nio.ByteBuffer

const val SPACE_CHARACTER = " "
const val COLON_CHARACTER = ":"
const val DOT_CHARACTER = "."
const val HEADER_SIZE = 12
const val ZERO = "0"

fun getBoolFromBit(char: Char): Boolean = char == '1'

fun getBitsFromShort(inShort: Short): String {
    val strBytes = shortToString(inShort)
    return strBytes.substring(strBytes.length - 4, strBytes.length)
}

fun shortToString(inShort: Short): String {
    return String.format("%" + 16 + "s", inShort.toString(radix = 2)).replace(SPACE_CHARACTER.toRegex(), ZERO)
}

fun byteToString(inByte: Byte): String {
    return String.format("%" + 8 + "s", inByte.toUByte().toString(radix = 2)).replace(SPACE_CHARACTER.toRegex(), ZERO)
}

fun byteToInt(inByte: Byte): Int = inByte.toInt() and 0xff

fun byteToHex(inByte: Byte): String {
    val intByteStr = (inByte.toInt() and 0xff).toString(radix = 16)
    return String.format("%" + 2 + "s", intByteStr).replace(SPACE_CHARACTER.toRegex(), ZERO)
}


fun getBitFromBool(inBool: Boolean): Char = if (inBool) '1' else '0'

fun byteSubsequence(array: ByteArray, start: Int, end : Int): ByteBuffer =
    ByteBuffer.wrap(array.copyOfRange(start, end))

fun nameToBytes(inName: String): ByteArray {
    //my.domain.at.com -> my domain at com -> 2 M Y 6 D O M models.A I N 2 models.A T 3 C O M 0
    val parsedDomain = inName.split(DOT_CHARACTER)
    val qName = ByteArray(inName.length + 2)
    var byteIter = 0
    parsedDomain.forEach { subDomain ->
        qName[byteIter] = subDomain.length.toByte()
        byteIter++
        subDomain.forEach { letter ->
            qName[byteIter] = letter.toByte()
            byteIter++
        }
    }
    qName[byteIter] = 0
    return qName
}

//2 M Y 6 D O M A I N 2 A T 3 C O M 0 -> my.domain.at.com
//2 M Y 6 D O M A I N 2 A T 3 C O M 0 ...... 2 M X 11000000 00001100 (pointer to 12) -> mx.my.domain.at.com
fun bytesToName(initPointer: Int, inData: ByteArray): Pair<String, Int> {
    var name = ""
    var i = initPointer
    var end = 0
    var readAmount = -1
    var pointerFound = false
    while (readAmount != 0) {
        val currByte = inData[i]
        if (!pointerFound) {
            val currByteStr = byteToString(currByte)
            val isPointer = currByteStr[0] == '1' && currByteStr[1] == '1'
            if (isPointer) {
                end = i + 2
                i = ("00" + currByteStr.substring(2, 7) + inData[i + 1].toString(radix = 2)).toInt(radix = 2)
                pointerFound = true
                continue
            }
        }
        readAmount = currByte.toInt()
        i++
        for (j in 0 until readAmount) {
            name += inData[i].toChar()
            i++
        }
        if (inData[i].toInt() != 0) name += DOT_CHARACTER
    }
    if (!pointerFound) end = i
    return Pair(name, end)
}

fun shortToByteArray(inShort: Short): ByteArray {
    val buffer = ByteBuffer.allocate(2)
    buffer.putShort(inShort)
    return buffer.array()
}