package stanic.stsell

import org.bukkit.Bukkit
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import stanic.stsell.bukkit.commands.DropsCommand
import stanic.stsell.bukkit.commands.SellCommand
import stanic.stsell.bukkit.events.*
import stanic.stsell.factory.SellFactory
import stanic.stsell.factory.model.Drops
import stanic.stsell.factory.model.Player
import stanic.stsell.hooks.PlaceholderAPI
import stanic.stsell.hooks.Vault
import stanic.stsell.utils.format
import stanic.stsell.utils.formatDrops
import stanic.stutils.bukkit.message.send
import java.io.File

class Main: JavaPlugin() {

    val drops = HashMap<String, Drops>()
    val player = HashMap<String, Player>()

    var jhmobs = false

    override fun onEnable() {
        instance = this
        Vault.setupEconomy()

        loadSettings()
        loadCommands()
        loadEvents()

        if (Bukkit.getPluginManager().getPlugin("JH_StackMobs") != null) jhmobs = true
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) PlaceholderAPI().register()
        if (Bukkit.getPluginManager().getPlugin("MVdWPlaceholderAPI") != null) {
            be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(this, "sell_miningAmount") {
                val player = it.player

                return@registerPlaceholder drops[player.name]!!.mineItems.size.formatDrops()
            }

            be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(this, "sell_miningPrice") {
                val player = it.player

                return@registerPlaceholder SellFactory().getPrice(drops[player.name]!!, "mining").format()
            }

            be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(this, "sell_mobAmount") {
                val player = it.player

                return@registerPlaceholder drops[player.name]!!.mobItems.size.formatDrops()
            }

            be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(this, "sell_mobPrice") {
                val player = it.player

                return@registerPlaceholder SellFactory().getPrice(drops[player.name]!!, "mob").format()
            }

            be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(this, "sell_totalAmount") {
                val player = it.player

                return@registerPlaceholder (drops[player.name]!!.mineItems.size + drops[player.name]!!.mobItems.size).formatDrops()
            }

            be.maximvdw.placeholderapi.PlaceholderAPI.registerPlaceholder(this, "sell_totalPrice") {
                val player = it.player

                return@registerPlaceholder (SellFactory().getPrice(
                    drops[player.name]!!,
                    "mining"
                ) + SellFactory().getPrice(drops[player.name]!!, "mob")).format()
            }
        }

        Bukkit.getConsoleSender().send("§e[ST-Sell] §fAtivado!")
    }

    private fun loadCommands() {
        SellCommand().run(this)
        DropsCommand().run(this)
    }

    private fun loadEvents() {
        if (server.pluginManager.getPlugin("AutoPickup") != null) PlayerMiningListener().onDropItemListener(this)
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