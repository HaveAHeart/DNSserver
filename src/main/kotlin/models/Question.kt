package models

data class Question(var qname: ByteArray,
                    var qtype: Short,
                    var qclass: Short)