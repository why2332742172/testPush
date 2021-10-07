package event

import TopTransfer
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import utils.getFromYML
import java.io.File

object PlayerJoinEvent {
    @SubscribeEvent
    fun join(event : PlayerJoinEvent){
        val player = event.player
        val folders : File = File("plugins\\TopTransfer\\data\\${player.uniqueId}")
        if(!folders.exists()){
            folders.mkdir()
        }
        TopTransfer.storgeMap[player.uniqueId] = mutableMapOf()

        //读取该文件夹下的全部文件 对于每一个文件都getFromYML得到itemStack 然后存储到Map中
        val allItemFile: FileTreeWalk = folders.walk()
        allItemFile.maxDepth(1)
            .filter { it.isFile }
            .forEach {
                val item = getFromYML.get(it.name, player.uniqueId.toString(), player)
                if (item != null) {
                    TopTransfer.storgeMap[player.uniqueId]?.set(it.name, item)
                    info("已加载玩家${player.name}信息: ${it.name} , $item")
                }
            }
    }
}