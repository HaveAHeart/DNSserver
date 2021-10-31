package models

data class Resource(var name: String = "",
                    var type: Short = 0,
                    var rclass: Short = 0, //keyword class is reserved
                    var ttl: Int = 0,
                    var rdlength: Short = 0,
                    var rdata: String = "") { //size unknown
    override fun toString(): String {
        return "Resource(name='$name', type=$type, rclass=$rclass, ttl=$ttl, rdlength=$rdlength, rdata='$rdata')"
    }
}