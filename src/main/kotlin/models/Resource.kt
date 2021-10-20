package models

data class Resource(var name: String,
                    var type: Short,
                    var rclass: Short, //keyword class is reserved
                    var ttl: Int,
                    var rdlength: Short,
                    var rdata: String //size unknown
)