package command

import TopTransfer.config
import TopTransfer.plugin
import `class`.TransferConfig
import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.PermissionDefault
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.info
import taboolib.common.platform.function.onlinePlayers
import taboolib.library.xseries.XMaterial
import taboolib.module.ui.ClickEvent
import taboolib.module.ui.openMenu
import taboolib.module.ui.type.Linked
import taboolib.platform.util.buildItem
import taboolib.platform.util.giveItem
import taboolib.platform.util.isNotAir
import utils.getFromYML
import utils.saveToYML
import java.io.File
import java.util.*

@CommandHeader(name = "toptransfer", permissionDefault = PermissionDefault.TRUE)
object CommandHub {
    val guiList = listOf<Int>(
        0, 1, 2, 3, 4, 5, 6, 7, 8,
        9, 10, 11, 12, 13, 14, 15, 16, 17,
        18, 19, 20, 21, 22, 23, 24, 25, 26,
        27, 28, 29, 30, 31, 32, 33, 34, 35,
        36, 37, 38, 39, 40, 41, 42, 43, 44,
    )

    //reload重载
    @CommandBody(permissionDefault = PermissionDefault.OP)
    val reload = subCommand {
        execute<Player> { sender, context, argument ->
            config.reload()
            TopTransfer.loadAll()
        }
    }

    //实行转换指令
    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val work = subCommand {
        execute<Player> { sender, context, argument ->
            //开始转换
            if(config.getBoolean("TopTransfer.enable")){
                doWrok(sender)
            }
        }
    }

    //测试存储指令
    // /toptransfer save
//    @CommandBody(permissionDefault = PermissionDefault.OP)
//    val save = subCommand {
//        execute<Player> { sender, context, argument ->
//            //根据玩家的uuid去找到其文件夹 然后打开GUI 同时在GUI里面加入物品
//            val uuid = sender.uniqueId
//            val item = sender.itemInHand
//            if (item.isNotAir()) {
//                //物品ID获得方式: 物品Type名+当前系统毫秒数
//                //存储在TopTransfer.storgeMap中
//                val itemID = "${item.type}${System.currentTimeMillis()}"
//                saveToYML.save(itemID, uuid.toString(), item, sender)
//                TopTransfer.storgeMap[uuid]?.forEach {
//                    info("已加载玩家${sender.name}信息: ${it.key} , ${it.value}")
//                }
//            }
//        }
//    }

    //打开存储GUI
    // /toptransfer open 玩家id
    //打开指定玩家的存储GUI
    @CommandBody(permissionDefault = PermissionDefault.OP)
    val open = subCommand {
        dynamic {
            suggestion<Player>(uncheck = true) { _, _ ->
                onlinePlayers().map { it.name }
            }
            execute<Player> { sender, context, argument ->
                //根据玩家的uuid去找到其文件夹 然后打开GUI 同时在GUI里面加入物品
                val targetPlayer = Bukkit.getOfflinePlayer(argument)
                val uuid = targetPlayer.uniqueId
                openGUI(sender, uuid)
            }
        }
    }


    //打开存储GUI
    // /toptransfer me
    //打开自己的存储GUI
    @CommandBody(permissionDefault = PermissionDefault.TRUE)
    val me = subCommand {
        execute<Player> { sender, context, argument ->
            //根据玩家的uuid去找到其文件夹 然后打开GUI 同时在GUI里面加入物品
            val uuid = sender.uniqueId
            openGUI(sender, uuid)
        }
    }

    //函数 打开GUI
    fun openGUI(player: Player, uuid: UUID): Boolean {
        val folders: File = File("plugins\\TopTransfer\\data\\${uuid}")
        if (!folders.exists()) {
            player.sendMessage("当前玩家不存在存储GUI!")
            return false
        }
        //读取该文件夹下的全部文件 对于每一个文件都getFromYML得到itemStack 然后存储到List中
        val itemList: MutableList<ItemStack> = mutableListOf()
        val allItemFile: FileTreeWalk = folders.walk()
        allItemFile.maxDepth(1)
            .filter { it.isFile }
            .forEach {
                val item = getFromYML.get(it.name, uuid.toString(), player)
                if (item != null) {
                    itemList.add(item)
                }
            }

        //打开GUI 是一个Link的GUI 能翻页
        val names = ChatColor.translateAlternateColorCodes('&', TopTransfer.config.getString("name"))
        player.openMenu<Linked<ItemStack>>(names) {
            rows(6)
            handLocked(true)
            slots(guiList)

            elements {
                itemList
            }
            onGenerate { _, element, _, _ ->
                element
            }

            //点击的时候取出该物品 然后关闭当前GUI 再打开当前GUI
            onClick { event: ClickEvent, element ->
                event.isCancelled = true
                if (!event.currentItem!!.type.equals(Material.STRUCTURE_VOID)) {
                    if (event.clickEvent().isLeftClick) {
                        //给予物品
                        player.giveItem(element, 1)
                        //删除文件
                        //遍历该玩家的整个存储图 如果其物品与点击的相等 则找到该id
                        lateinit var itemID : String
                        run findID@{
                            TopTransfer.storgeMap[uuid]?.entries?.forEach {
                                if(it.value == element){
                                    info(it.key)
                                    itemID = it.key
                                    return@findID
                                }
                            }
                        }

                        val itemFile: File = File("plugins\\TopTransfer\\data\\${uuid}\\$itemID")
                        if (itemFile.exists()) {
                            itemFile.delete()
                        }
                        TopTransfer.storgeMap[uuid]?.remove(itemID)
                        itemList.clear()
                        player.closeInventory()
                        openGUI(player, uuid)

                    }
                }
            }

            setNextPage(53) { _, hasNextPage ->
                if (hasNextPage) {
                    buildItem(XMaterial.STRUCTURE_VOID) { name = "§7下一页" }
                } else {
                    buildItem(XMaterial.STRUCTURE_VOID) { name = "§8下一页" }
                }
            }
            setPreviousPage(45) { _, hasPreviousPage ->
                if (hasPreviousPage) {
                    buildItem(XMaterial.STRUCTURE_VOID) { name = "§7上一页" }
                } else {
                    buildItem(XMaterial.STRUCTURE_VOID) { name = "§8上一页" }
                }
            }

        }
        return true
    }

    //函数 执行转换
    fun doWrok(player : Player) : Boolean{
        //根据玩家手上的物品 然后遍历全部的transferMap 找到对应lore后执行指令
        val handItem = player.itemInHand
        if(handItem.isNotAir()){
            if(handItem.hasItemMeta()){
                val meta = handItem.itemMeta
                if(meta!!.hasLore()){
                    TopTransfer.transferMap.forEach{
                        if(handItem.type == XMaterial.matchXMaterial(it.value.type).get().parseMaterial()){
                            //如果材质匹配上了
                            //开始判断lore
                            val cfg = it.value
                            var loreFlag = false
                            run checkLore@{
                                meta.lore!!.forEach {
                                    val temp = it.replace("§","&")
                                    if(temp == cfg.lore){
                                        //匹配上了
                                        loreFlag = true
                                        return@checkLore
                                    }
                                }
                            }
                            if(loreFlag){
                                //type lore 全匹配了
                                //首先清除手上的物品 然后将其上面的附魔全部分成单本书加入玩家的存储gui 然后执行指令

                                //清除物品
                                player.itemInHand.amount -= 1

                                //将上面的附魔分成单本书
                                if(meta.hasEnchants()){
                                    meta.enchants.forEach {
                                        val enchantBook = buildItem(XMaterial.ENCHANTED_BOOK){
                                            enchants[it.key] = it.value
                                        }
                                        //player.giveItem(enchantBook)
                                        val itemID = "${enchantBook.type}${System.currentTimeMillis()}"
                                        saveToYML.save(itemID,player.uniqueId.toString(),enchantBook,player)
                                    }
                                }

                                //执行指令
                                val cmd = cfg.commands
                                cmd.forEach {
                                    val cmd1 = PlaceholderAPI.setPlaceholders(player,it)
                                    Bukkit.dispatchCommand(plugin.server.consoleSender,cmd1)
                                }
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }
}