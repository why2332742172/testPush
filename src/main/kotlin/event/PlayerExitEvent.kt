package event

import org.bukkit.event.player.PlayerQuitEvent
import taboolib.common.platform.event.SubscribeEvent

object PlayerExitEvent {
    @SubscribeEvent
    fun playerExit(event : PlayerQuitEvent){
        val player = event.player
        if(TopTransfer.storgeMap.containsKey(player.uniqueId)){
            TopTransfer.storgeMap.remove(player.uniqueId)
        }
    }
}