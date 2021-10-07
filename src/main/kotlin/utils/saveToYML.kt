package utils

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.serializeToByteArray
import java.io.File

object saveToYML {
    fun save(ItemID : String, folder : String, itemStack: ItemStack, sender : Player) : Boolean{

        val folders : File = File("plugins\\TopTransfer\\data\\$folder")
        if(!folders.exists()){
            folders.mkdir()
        }
        val itemYML : File = File(folders, ItemID)
        if(!itemYML.exists()){
            itemYML.createNewFile()
        }

        val itemArray = itemStack.serializeToByteArray(true)
        itemYML.writeBytes(itemArray)
        TopTransfer.storgeMap[sender.uniqueId]?.set(ItemID, itemStack)

        return true;
    }
}