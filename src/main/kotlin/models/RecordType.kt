package models

import exceptions.NotImplTypeException

sealed class RecordType(val code: Short) {
    class A : RecordType(1)
    class AAAA : RecordType(28)
    class NotImpl(type: Short) : RecordType(type)
    data class MX(val size: Int): RecordType(15)
    data class TXT(val size: Int): RecordType(16)

    companion object {
        fun of(code: Short): RecordType = when (code.toInt()) {
                1 -> A()
                28 -> AAAA()
                15 -> MX(0)
                16 -> TXT(0)
                else -> NotImpl(code)
        }
    }

    fun size(): Int = when(this) {
            is A -> 4
            is AAAA -> 16
            is MX -> this.size
            is TXT -> this.size
            is NotImpl -> throw NotImplTypeException("This record type is not implemented")
    }
}

