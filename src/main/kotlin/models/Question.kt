package models

import DOT_CHARACTER
import HEADER_SIZE
import byteSubsequence

data class Question(var qname: String,
                    var qtype: RecordType,
                    var qclass: Short) {
    companion object {
        fun getQuestionFromByteArray(question: ByteArray) : Question {
            if (question.size - HEADER_SIZE < 6) throw TODO()
            var qName = String()
            var i = HEADER_SIZE
            var readAmount = question[i].toInt()
            i++
            while (readAmount != 0) {
                for (j in 0 until readAmount) {
                    qName += question[i].toChar()
                    i++
                }
                readAmount = question[i].toInt()
                i++
                if (readAmount != 0) qName += DOT_CHARACTER
            }
            val qType = byteSubsequence(question, i, i + 2).short
            val qClass = byteSubsequence(question, i + 2, i + 4).short
            return Question(qName, RecordType.of(qType), qClass)
        }
    }
}

