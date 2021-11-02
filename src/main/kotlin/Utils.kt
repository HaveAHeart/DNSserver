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
            qName[byteIter] = letter.toByte() //TODO encodings?
            byteIter++
        }
    }
    qName[byteIter] = 0
    return qName
}

fun shortToByteArray(inShort: Short): ByteArray {
    val buffer = ByteBuffer.allocate(2)
    buffer.putShort(inShort)
    return buffer.array()
}