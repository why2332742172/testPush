package utils

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.platform.util.deserializeToItemStack
import java.io.File

object getFromYML {
    fun get(ItemID : String, folder : String, sender : Player) : ItemStack? {

        val folders : File = File("plugins\\TopTransfer\\data\\$folder")
        if(!folders.exists()){
            sender.sendMessage("不存在 $folder 文件夹!")
            return null;
        }
        val itemYML : File = File(folders, ItemID)
        if(!itemYML.exists()){
            sender.sendMessage("不存在 $ItemID 此物品!")
            return null;
        }

        return itemYML.readBytes().deserializeToItemStack(true)


    }
}