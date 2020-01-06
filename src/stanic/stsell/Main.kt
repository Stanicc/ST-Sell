package stanic.stsell

import org.bukkit.Bukkit
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.*
import org.bukkit.plugin.java.JavaPlugin
import stanic.stsell.bukkit.commands.*
import stanic.stsell.bukkit.events.*
import stanic.stsell.factory.model.*
import stanic.stsell.hooks.Vault
import stanic.stutils.bukkit.message.send
import java.io.File

class Main: JavaPlugin() {

    val drops = HashMap<String, Drops>()
    val player = HashMap<String, Player>()

    override fun onEnable() {
        instance = this
        Vault.setupEconomy()

        loadSettings()
        loadCommands()
        loadEvents()

        Bukkit.getConsoleSender().send("§e[ST-Sell] §fAtivado!")
    }

    private fun loadCommands() {
        SellCommand().run(this)
        DropsCommand().run(this)
    }

    private fun loadEvents() {
        PlayerMiningListener().onMining(this)
        PlayerInventoryListener().onInteractInDrops(this)
        PlayerInventoryListener().onInteractInMain(this)
        PlayerInventoryListener().onInteractInConfig(this)
        PlayerInventoryListener().onInteractInItems(this)
        PlayerConnectListener().onJoin(this)
        PlayerKillListener().onKill(this)
        PlayerAutoSellListeners().onClickInShift(this)
        PlayerAutoSellListeners().onPickup(this)
    }

    fun loadSettings() {
        sett = File(dataFolder, "settings.yml")
        if (!sett.exists()) {
            sett.parentFile.mkdirs()
            saveResource("settings.yml", false)
        }
        settings = YamlConfiguration()
        try {
            settings.load(sett)
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
        }
    }

    companion object {
        lateinit var instance: Main
            private set

        lateinit var settings: FileConfiguration
            internal set
        lateinit var sett: File
    }

}