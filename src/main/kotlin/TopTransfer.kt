import `class`.ConflictConfig
import `class`.TransferConfig
import org.bukkit.inventory.ItemStack
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.info
import taboolib.module.configuration.Config
import taboolib.module.configuration.SecuredFile
import taboolib.platform.BukkitPlugin
import java.io.File
import java.util.*

object TopTransfer : Plugin(){

    //存储玩家当前库存里面的物品对照图
    val storgeMap : MutableMap<UUID , MutableMap<String , ItemStack>> = mutableMapOf()

    //存储全部的TransferConfig
    val transferMap : MutableMap<String , TransferConfig> = mutableMapOf()

    //存储全部的ConflictConfig
    val conflictMap : MutableMap<String , ConflictConfig> = mutableMapOf()

    val plugin by lazy {
        BukkitPlugin.getInstance()
    }

    @Config(autoReload = false)
    lateinit var config : SecuredFile
        private set

    override fun onEnable() {
        info("已加载--TopTransfer")
        info("作者--StrawberryYu")
        info("版本--v1.0")
        val storgeDir : File = File("plugins\\TopTransfer\\data")
        if(!storgeDir.exists()){
            storgeDir.mkdir()
        }
    }

    override fun onDisable() {
        info("已卸载--TopTransfer")
    }

    @Awake(LifeCycle.ENABLE)
    fun loadAll(){
        transferMap.clear()
        conflictMap.clear()

        //从config.yml里面读取全部的transferConfig 录入到map中
        val transferConfigCs = config.getConfigurationSection("TopTransfer.transferConfig")
        transferConfigCs.getKeys(false).forEach {
            val id = it
            val type = config.getString("TopTransfer.transferConfig.${it}.type")
            val lore = config.getString("TopTransfer.transferConfig.${it}.lore")
            val commands = config.getStringList("TopTransfer.transferConfig.${it}.commands")

            val transferCfg = TransferConfig(id,type,lore,commands)
            info("已加载转换配置: $transferCfg")
            transferMap[id] = transferCfg
        }

        //从config.yml里面读取全部的conflictConfig 录入到map中
        val conflictConfigCs = config.getConfigurationSection("TopEcoDown.conflictList")
        conflictConfigCs.getKeys(false).forEach {
            val id = it
            val conflictLists = config.getStringList("TopEcoDown.conflictList.$it")

            val conflictCfg = ConflictConfig(id,conflictLists)
            info("已加载附魔冲突配置: $conflictCfg")
            conflictMap[id] = conflictCfg
        }
    }
}