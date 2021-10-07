package `class`

class TransferConfig constructor(id : String , type : String , lore : String , commands : MutableList<String>){
    var id : String = ""
    var type : String = ""
    var lore : String = ""
    var commands : MutableList<String> = mutableListOf()

    init {
        this.id = id
        this.type = type
        this.lore = lore
        this.commands.addAll(commands)
    }

    override fun toString(): String {
        return "(id='$id', type='$type', lore='$lore', commands=$commands)"
    }


}