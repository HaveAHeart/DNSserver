package models

data class Header(var id: Short,
                  var qr: Boolean, var opcode: Short, var aa: Boolean, var tc: Boolean, var rd:Boolean,
                  var ra: Boolean, var z: Short, var rcode: Short,
                  var qdcount: Short,
                  var ancount: Short,
                  var nscount: Short,
                  var arcount: Short)