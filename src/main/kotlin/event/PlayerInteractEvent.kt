package event

import `class`.ConflictConfig
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.event.SubscribeEvent
import taboolib.library.xseries.XMaterial
import taboolib.module.chat.colored
import taboolib.platform.util.buildItem
import taboolib.platform.util.giveItem
import taboolib.platform.util.isAir
import utils.saveToYML

object PlayerInteractEvent {
    //玩家在交互时 检查其手上和装备栏里的物品 是否有超过等级的附魔、冲突的附魔、保护附魔、附魔数量
    @SubscribeEvent
    fun interact(event: PlayerInteractEvent) {

        if(TopTransfer.config.getBoolean("TopEcoDown.enable")){
            val player = event.player
            val handItem = player.itemInHand
            val helmet = player.inventory.helmet
            val chest = player.inventory.chestplate
            val leggings = player.inventory.leggings
            val boots = player.inventory.boots
            val offHand = player.inventory.itemInOffHand

            //超等级附魔?
            checkLevel(handItem,player,0)
            helmet?.let { checkLevel(it,player,1) }
            chest?.let { checkLevel(it,player,2) }
            leggings?.let { checkLevel(it,player,3) }
            boots?.let { checkLevel(it,player,4) }
            checkLevel(offHand,player,5)

            //冲突附魔?
//            handItem.enchantments.forEach {
//                player.sendMessage(it.key.name)
//            }
            checkConflict(handItem,player,0)
            helmet?.let { checkConflict(it,player,1) }
            chest?.let { checkConflict(it,player,2) }
            leggings?.let { checkConflict(it,player,3) }
            boots?.let { checkConflict(it,player,4) }
            checkConflict(offHand,player,5)

            //是否有保护附魔?
            checkProtect(handItem,player,0)
            helmet?.let { checkProtect(it,player,1) }
            chest?.let { checkProtect(it,player,2) }
            leggings?.let { checkProtect(it,player,3) }
            boots?.let { checkProtect(it,player,4) }
            checkProtect(offHand,player,5)

            //附魔数量是否超过限制?
            cheackNum(handItem,player,0)
            helmet?.let { cheackNum(it,player,1) }
            chest?.let { cheackNum(it,player,2) }
            leggings?.let { cheackNum(it,player,3) }
            boots?.let { cheackNum(it,player,4) }
            cheackNum(offHand,player,5)

        }



    }

    //函数 检查物品附魔数量是否超过限制
    // slot代表装备槽位 0主手 1头盔 2胸甲 3裤子 4鞋子 5副手
    fun cheackNum(itemStack: ItemStack, player: Player, slot: Int){

        if(!TopTransfer.config.getBoolean("TopEcoDown.enLimit.enable")){
            return
        }

        if(!TopTransfer.config.getStringList("TopEcoDown.typeList").contains(itemStack.type.toString())){
            return
        }

        if(itemStack.isAir()){
            return
        }

        if(itemStack.enchantments.keys.size > TopTransfer.config.getInt("TopEcoDown.enLimit.num")){
            player.sendMessage(TopTransfer.config.getString("TopEcoDown.msg3").colored())
            itemStack.enchantments.forEach{
                val book = buildItem(XMaterial.ENCHANTED_BOOK){
                    enchants[it.key] = it.value
                }
                itemStack.removeEnchantment(it.key)
                val itemID = "${book.type}${System.currentTimeMillis()}"
                saveToYML.save(itemID,player.uniqueId.toString(),book,player)
            }
            when(slot){
                0 -> {
                    player.setItemInHand(itemStack)
                    return
                }
                1 -> {
                    player.inventory.helmet = itemStack
                    return
                }
                2 -> {
                    player.inventory.chestplate = itemStack
                    return
                }
                3 -> {
                    player.inventory.leggings = itemStack
                    return
                }
                4 -> {
                    player.inventory.boots = itemStack
                    return
                }
                5 -> {
                    player.inventory.setItemInOffHand(itemStack)
                    return
                }
            }
        }


    }

    //函数 检查物品是否有保护附魔
    // slot代表装备槽位 0主手 1头盔 2胸甲 3裤子 4鞋子 5副手
    fun checkProtect(itemStack: ItemStack, player: Player, slot: Int){

        if(!TopTransfer.config.getStringList("TopEcoDown.typeList").contains(itemStack.type.toString())){
            return
        }

        if(itemStack.isAir()){
            return
        }

        if(itemStack.enchantments.containsKey(Enchantment.PROTECTION_ENVIRONMENTAL)){

            val book = buildItem(XMaterial.ENCHANTED_BOOK){
                enchants[Enchantment.PROTECTION_ENVIRONMENTAL] = itemStack.enchantments[Enchantment.PROTECTION_ENVIRONMENTAL]!!
            }
            itemStack.removeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL)
            val itemID = "${book.type}${System.currentTimeMillis()}"
            saveToYML.save(itemID,player.uniqueId.toString(),book,player)
            val msg4 = TopTransfer.config.getString("TopEcoDown.msg4")
            player.sendMessage(msg4.colored())
            when(slot){
                0 -> {
                    player.setItemInHand(itemStack)
                    return
                }
                1 -> {
                    player.inventory.helmet = itemStack
                    return
                }
                2 -> {
                    player.inventory.chestplate = itemStack
                    return
                }
                3 -> {
                    player.inventory.leggings = itemStack
                    return
                }
                4 -> {
                    player.inventory.boots = itemStack
                    return
                }
                5 -> {
                    player.inventory.setItemInOffHand(itemStack)
                    return
                }
            }
        }

    }

    //函数 检查物品附魔是否有冲突
    // slot代表装备槽位 0主手 1头盔 2胸甲 3裤子 4鞋子 5副手
    fun checkConflict(itemStack: ItemStack, player: Player, slot: Int) {

        if(!TopTransfer.config.getStringList("TopEcoDown.typeList").contains(itemStack.type.toString())){
            return
        }

        if(itemStack.isAir()){
            return
        }
        lateinit var conflictCfg : ConflictConfig
        var flag = false
        //遍历物品的全部附魔
        run a@{
            itemStack.enchantments.forEach {
                val enchant = it.key

                //遍历物品的全部的冲突附魔表 对每个冲突配置看他是否包含当前物品的该附魔
                run findConflitCfg@{
                    TopTransfer.conflictMap.forEach {
                        if(it.value.conflictList.contains(enchant.name)){
                            conflictCfg = ConflictConfig(it.value.id,it.value.conflictList)
                            flag = true
                            return@findConflitCfg
                        }
                    }
                }

                if(flag){
                    return@a
                }
            }
        }
        //如果包括则命中冲突表 然后以此冲突表为基础 再次遍历整个物品的附魔 然后把冲突的附魔给去掉放入GUI 去掉原来的附魔
        if(flag){
            itemStack.enchantments.forEach {
                if(conflictCfg.conflictList.contains(it.key.name)){
                    val book = buildItem(XMaterial.ENCHANTED_BOOK){
                        enchants[it.key] = it.value
                    }
                    itemStack.removeEnchantment(it.key)
                    val itemID = "${book.type}${System.currentTimeMillis()}"
                    saveToYML.save(itemID,player.uniqueId.toString(),book,player)
                }
            }

            //遍历完后 全部的冲突附魔已经消除 则根据slot来替换物品
            var msg2 = TopTransfer.config.getString("TopEcoDown.msg2")
            msg2 = msg2.replace("<Conflict>",conflictCfg.conflictList.toString())
            player.sendMessage(msg2.colored())
            when(slot){
                0 -> {
                    player.setItemInHand(itemStack)
                    return
                }
                1 -> {
                    player.inventory.helmet = itemStack
                    return
                }
                2 -> {
                    player.inventory.chestplate = itemStack
                    return
                }
                3 -> {
                    player.inventory.leggings = itemStack
                    return
                }
                4 -> {
                    player.inventory.boots = itemStack
                    return
                }
                5 -> {
                    player.inventory.setItemInOffHand(itemStack)
                    return
                }
            }
            return
        }
    }

    //函数 检查物品是否有超等级的附魔
    // slot代表装备槽位 0主手 1头盔 2胸甲 3裤子 4鞋子 5副手
    fun checkLevel(itemStack: ItemStack, player: Player, slot: Int) {

        if(!TopTransfer.config.getStringList("TopEcoDown.typeList").contains(itemStack.type.toString())){
            return
        }

        if(itemStack.isAir()){
            return
        }

        var flag1 = false //锋利是否超等级
        var flag2 = false //亡灵杀手是否超等级
        var flag3 = false //节肢杀手是否超等级

        //判断锋利
        if (itemStack.enchantments.containsKey(Enchantment.DAMAGE_ALL)) {
            if (itemStack.enchantments[Enchantment.DAMAGE_ALL]!! > 5) {
                flag1 = true
            }
        }

        //判断亡灵杀手
        if (itemStack.enchantments.containsKey(Enchantment.DAMAGE_UNDEAD)) {
            if (itemStack.enchantments[Enchantment.DAMAGE_UNDEAD]!! > 5) {
                flag2 = true
            }
        }

        //判断节肢杀手
        if (itemStack.enchantments.containsKey(Enchantment.DAMAGE_ARTHROPODS)) {
            if (itemStack.enchantments[Enchantment.DAMAGE_ARTHROPODS]!! > 5) {
                flag3 = true
            }
        }

        if (flag1) {
            val book = buildItem(XMaterial.ENCHANTED_BOOK){
                enchants[Enchantment.DAMAGE_ALL] = itemStack.enchantments[Enchantment.DAMAGE_ALL]!!
            }
            val itemID = "${book.type}${System.currentTimeMillis()}"
            saveToYML.save(itemID,player.uniqueId.toString(),book,player)
            itemStack.removeEnchantment(Enchantment.DAMAGE_ALL)
        }
        if (flag2) {
            val book = buildItem(XMaterial.ENCHANTED_BOOK){
                enchants[Enchantment.DAMAGE_UNDEAD] = itemStack.enchantments[Enchantment.DAMAGE_UNDEAD]!!
            }
            val itemID = "${book.type}${System.currentTimeMillis()}"
            saveToYML.save(itemID,player.uniqueId.toString(),book,player)
            itemStack.removeEnchantment(Enchantment.DAMAGE_UNDEAD)
        }
        if (flag3) {
            val book = buildItem(XMaterial.ENCHANTED_BOOK){
                enchants[Enchantment.DAMAGE_ARTHROPODS] = itemStack.enchantments[Enchantment.DAMAGE_ARTHROPODS]!!
            }
            val itemID = "${book.type}${System.currentTimeMillis()}"
            saveToYML.save(itemID,player.uniqueId.toString(),book,player)
            itemStack.removeEnchantment(Enchantment.DAMAGE_ARTHROPODS)
        }

        if(flag1 || flag2 || flag3){
            player.sendMessage(TopTransfer.config.getString("TopEcoDown.msg1").colored())
            when(slot){
                0 -> {
                    player.setItemInHand(itemStack)
                    return
                }
                1 -> {
                    player.inventory.helmet = itemStack
                    return
                }
                2 -> {
                    player.inventory.chestplate = itemStack
                    return
                }
                3 -> {
                    player.inventory.leggings = itemStack
                    return
                }
                4 -> {
                    player.inventory.boots = itemStack
                    return
                }
                5 -> {
                    player.inventory.setItemInOffHand(itemStack)
                    return
                }
            }
        }






    }
}