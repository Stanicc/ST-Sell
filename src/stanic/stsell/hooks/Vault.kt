package stanic.stsell.hooks

import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import stanic.stsell.Main
import stanic.stutils.bukkit.message.send

object Vault {

    lateinit var eco: Economy

    fun setupEconomy(): Boolean {
        if (Main.instance.server.pluginManager.getPlugin("Vault") == null) {
            Bukkit.getConsoleSender().send("§cVault is not enabled! The plugin will be disabled").apply {
                Main.instance.server.pluginManager.disablePlugin(Main.instance)
            }
            return false
        }
        val economy =
            Main.instance.server.servicesManager.getRegistration(Economy::class.java as Class<*>)
        eco = economy.provider as Economy
        return true
    }

}