package stanic.stsell.bukkit.events

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import stanic.stsell.Main
import stanic.stsell.factory.SellFactory
import stanic.stsell.hooks.Vault
import stanic.stsell.utils.*
import stanic.stutils.bukkit.event.event
import stanic.stutils.bukkit.message.replaceColor
import stanic.stutils.bukkit.message.send
import java.lang.Exception

class PlayerInventoryListener {

    private val sett = Main.settings

    companion object {
        var inventoryType = ""
    }

    fun onInteractInDrops(main: Main) = main.event<InventoryClickEvent> { event ->
        val player = event.whoClicked as Player
        val inventory = event.inventory

        if (inventory.title == sett.getString("Menus.dropsMenu.name").replaceColor()) {
            event.isCancelled = true

            if (event.slot == sett.getInt("Menus.dropsMenu.items.miningDrops.slot") || event.slot == sett.getInt("Menus.dropsMenu.items.mobsDrops.slot")) {
                val type = when (event.slot) {
                    sett.getInt("Menus.dropsMenu.items.miningDrops.slot") -> "mining"
                    sett.getInt("Menus.dropsMenu.items.mobsDrops.slot") -> "mob"
                    else -> "null"
                }

                when (event.click) {
                    ClickType.LEFT -> {
                        val money = SellFactory().getPrice(Main.instance.drops[player.name]!!, type)

                        Vault.eco.depositPlayer(player, money)

                        player.send(
                            Messages().get("dropsSold").replace(
                                "{amount}", when (type) {
                                    "mining" -> main.drops[player.name]!!.mineItems.size.formatDrops()
                                    "mob" -> main.drops[player.name]!!.mobItems.size.formatDrops()
                                    else -> TODO()
                                }
                            ).replace("{money}", money.format())
                        ).apply { SellFactory().clearDrops(Main.instance.drops[player.name]!!, type) }

                        player.closeInventory()
                        Menus.openDropsMenu(player)
                    }
                    ClickType.RIGHT -> {
                        inventoryType = type
                        Menus.openItemsMenu(player, Main.instance.drops[player.name]!!, type)
                    }
                    else -> TODO()
                }
            }
        }
    }

    fun onInteractInMain(main: Main) = main.event<InventoryClickEvent> { event ->
        val player = event.whoClicked as Player

        if (event.inventory.title == sett.getString("Menus.sellMenu.name").replaceColor()) {
            event.isCancelled = true

            when (event.slot) {
                sett.getInt("Menus.sellMenu.items.configItem.slot") -> Menus.openConfigMenu(player)
                sett.getInt("Menus.sellMenu.items.dropsItem.slot") -> Menus.openDropsMenu(player)
            }
        }
    }

    fun onInteractInConfig(main: Main) = main.event<InventoryClickEvent> { event ->
        val player = event.whoClicked as Player

        if (event.inventory.title == sett.getString("Menus.configMenu.name").replaceColor()) {
            event.isCancelled = true

            when (event.slot) {
                sett.getInt("Menus.configMenu.items.dropsItem.slot") -> {
                    if (player.hasPermission("stsell.enableDrops")) {
                        main.player[player.name]!!.enableDrops = !main.player[player.name]!!.enableDrops
                        player.closeInventory()
                        Menus.openConfigMenu(player)
                    } else player.send(Messages().get("noPerm")).apply { player.closeInventory() }
                }
                sett.getInt("Menus.configMenu.items.autoSellItem.slot") -> {
                    if (player.hasPermission("stsell.autosell")) {
                        main.player[player.name]!!.autoSell = !main.player[player.name]!!.autoSell
                        player.closeInventory()
                        Menus.openConfigMenu(player)
                    } else player.send(Messages().get("noPerm")).apply { player.closeInventory() }
                }
                sett.getInt("Menus.configMenu.items.shiftSellItem.slot") -> {
                    if (player.hasPermission("stsell.shiftsell")) {
                        main.player[player.name]!!.shiftSell = !main.player[player.name]!!.shiftSell
                        player.closeInventory()
                        Menus.openConfigMenu(player)
                    } else player.send(Messages().get("noPerm")).apply { player.closeInventory() }
                }
            }
        }
    }

    fun onInteractInItems(main: Main) = main.event<InventoryClickEvent>(EventPriority.HIGHEST) { event ->
        val player = event.whoClicked as Player
        val inventory = event.inventory

        if (inventory.title == sett.getString("Menus.itemsMenu.name").replaceColor()) {
            event.isCancelled = true

            if (event.clickedInventory != null && event.clickedInventory.name == sett.getString("Menus.itemsMenu.name").replaceColor()) {
                if (event.currentItem != null && event.currentItem.type != Material.AIR) {
                    when (inventoryType) {
                        "mining" -> {
                            val item =
                                SellUtils().verifyItem(
                                    event.currentItem.typeId,
                                    event.currentItem.durability.toInt()
                                )
                                    ?: return@event

                            var price = sett.getDouble("$item.price")
                            sett.getConfigurationSection("$item.booster").getKeys(false)
                                .forEach { booster ->
                                    if (player.hasPermission("stsell.booster.$booster")) price =
                                        sett.getDouble("$item.booster.$booster")
                                }

                            SellFactory().removeItem(
                                Main.instance.drops[player.name]!!,
                                event.currentItem,
                                price,
                                "mining"
                            ).apply {
                                player.inventory.addItem(event.currentItem)
                                inventory.removeItem(event.currentItem)
                                if (main.drops[player.name]!!.mineItems.isEmpty()) SellFactory().clearDrops(
                                    main.drops[player.name]!!,
                                    inventoryType
                                )
                            }
                        }
                        "mob" -> {
                            val item =
                                SellUtils().verifyItem(
                                    event.currentItem.typeId,
                                    event.currentItem.durability.toInt()
                                )
                                    ?: return@event

                            var price = sett.getDouble("$item.price")
                            sett.getConfigurationSection("$item.booster").getKeys(false)
                                .forEach { booster ->
                                    if (player.hasPermission("stsell.booster.$booster")) price =
                                        sett.getDouble("$item.booster.$booster")
                                }

                            SellFactory().removeItem(
                                Main.instance.drops[player.name]!!,
                                event.currentItem,
                                price,
                                "mob"
                            ).apply {
                                player.inventory.addItem(event.currentItem)
                                inventory.removeItem(event.currentItem)
                                if (main.drops[player.name]!!.mobItems.isEmpty()) SellFactory().clearDrops(
                                    main.drops[player.name]!!,
                                    inventoryType
                                )
                            }
                        }
                    }

                    player.closeInventory()
                    Menus.openItemsMenu(player, main.drops[player.name]!!, inventoryType)
                }
            }
        }
    }

}