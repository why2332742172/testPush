package `class`

class ConflictConfig constructor(id : String , conflictList : MutableList<String>){
    var id : String = ""
    var conflictList : MutableList<String> = mutableListOf()

    init {
        this.id = id
        this.conflictList.addAll(conflictList)
    }

    override fun toString(): String {
        return "(id='$id', conflictList=$conflictList)"
    }


}