package stanic.stsell.utils

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import stanic.stsell.Main
import stanic.stsell.factory.model.Drops
import stanic.stutils.bukkit.message.replaceColor
import stanic.stutils.bukkit.utils.ItemBuilder
import stanic.stutils.bukkit.utils.SkullUtils

class Menus {

    companion object {
        private val sett = Main.settings

        fun openMainMenu(sender: Player) {
            val inv = Bukkit.createInventory(
                null,
                sett.getInt("Menus.sellMenu.rows") * 9,
                sett.getString("Menus.sellMenu.name").replaceColor()
            )
            val section = sett.getConfigurationSection("Menus.sellMenu.items")
            for (it in section.getKeys(false)) {
                val item = "Menus.sellMenu.items.$it"

                val lore = ArrayList<String>()
                for (line in Main.settings.getStringList("$item.lore")) lore.add(line.replaceColor())

                inv.setItem(
                    sett.getInt("$item.slot"),
                    if (!sett.getString("$item.skullOwner").startsWith("http"))
                        ItemBuilder(ItemStack(sett.getInt("$item.id"))).setDurability(sett.getInt("$item.data")).setSkullOwner(
                            sett.getString("$item.skullOwner")
                        ).setName(sett.getString("$item.name").replaceColor()).setLore(lore).build()
                    else ItemBuilder(SkullUtils().getSkull(sett.getString("$item.skullOwner"))).setName(sett.getString("$item.name").replaceColor()).setLore(
                        lore
                    ).build()
                )
            }

            sender.openInventory(inv)
        }

        fun openDropsMenu(sender: Player) {
            val inv = Bukkit.createInventory(
                null,
                sett.getInt("Menus.dropsMenu.rows") * 9,
                sett.getString("Menus.dropsMenu.name").replaceColor()
            )
            val section = sett.getConfigurationSection("Menus.dropsMenu.items")
            for (it in section.getKeys(false)) {
                val item = "Menus.dropsMenu.items.$it"

                val lore = ArrayList<String>()
                for (line in Main.settings.getStringList("$item.lore")) lore.add(line.replaceColor().replaceInfo(Main.instance.drops[sender.name]!!))

                inv.setItem(
                    sett.getInt("$item.slot"),
                    if (!sett.getString("$item.skullOwner").startsWith("http"))
                        ItemBuilder(ItemStack(sett.getInt("$item.id"))).setDurability(sett.getInt("$item.data")).setSkullOwner(
                            sett.getString("$item.skullOwner")
                        ).setName(sett.getString("$item.name").replaceInfo(Main.instance.drops[sender.name]!!).replaceColor()).setLore(
                            lore
                        ).build()
                    else ItemBuilder(SkullUtils().getSkull(sett.getString("$item.skullOwner"))).setName(
                        sett.getString("$item.name").replaceInfo(
                            Main.instance.drops[sender.name]!!
                        ).replaceColor()
                    ).setLore(
                        lore
                    ).build()
                )
            }

            sender.openInventory(inv)
        }

        fun openItemsMenu(sender: Player, drops: Drops, type: String) {
            val inv = Bukkit.createInventory(
                null,
                54,
                Main.settings.getString("Menus.itemsMenu.name").replaceColor()
            )

            when (type) {
                "mining" -> drops.getDrops("mining").forEach { inv.addItem(it) }
                "mob" -> drops.getDrops("mob").forEach { inv.addItem(it) }
            }

            sender.openInventory(inv)
        }

        fun openConfigMenu(sender: Player) {
            val inv = Bukkit.createInventory(
                null,
                Main.settings.getInt("Menus.configMenu.rows") * 9,
                Main.settings.getString("Menus.configMenu.name").replaceColor()
            )

            val section = Main.settings.getConfigurationSection("Menus.configMenu.items")
            for (it in section.getKeys(false)) {
                val item = "Menus.configMenu.items.$it"

                val lore = ArrayList<String>()
                for (line in Main.settings.getStringList("$item.lore")) lore.add(
                    line.replaceColor().replaceInfo(Main.instance.drops[sender.name]!!).replace(
                        "{status}", when (it) {
                            "dropsItem" -> if (Main.instance.player[sender.name]!!.enableDrops) Main.settings.getString(
                                "$item.statusOn"
                            ) else Main.settings.getString(
                                "$item.statusOff"
                            )
                            "autoSellItem" -> if (Main.instance.player[sender.name]!!.autoSell) Main.settings.getString(
                                "$item.statusOn"
                            ) else Main.settings.getString(
                                "$item.statusOff"
                            )
                            "shiftSellItem" -> if (Main.instance.player[sender.name]!!.shiftSell) Main.settings.getString(
                                "$item.statusOn"
                            ) else Main.settings.getString(
                                "$item.statusOff"
                            )
                            else -> ""
                        }
                    )
                )

                inv.setItem(
                    sett.getInt("$item.slot"),
                    if (!sett.getString("$item.skullOwner").startsWith("http"))
                        ItemBuilder(ItemStack(sett.getInt("$item.id"))).setDurability(sett.getInt("$item.data")).setSkullOwner(
                            sett.getString("$item.skullOwner")
                        ).setName(sett.getString("$item.name").replaceInfo(Main.instance.drops[sender.name]!!).replaceColor()).setLore(
                            lore
                        ).build()
                    else ItemBuilder(SkullUtils().getSkull(sett.getString("$item.skullOwner"))).setName(
                        sett.getString("$item.name").replaceInfo(
                            Main.instance.drops[sender.name]!!
                        ).replaceColor()
                    ).setLore(
                        lore
                    ).build()
                )
            }

            sender.openInventory(inv)
        }

    }

}