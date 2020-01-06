package stanic.stsell.bukkit.events

import org.bukkit.Material
import org.bukkit.event.player.PlayerPickupItemEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import stanic.stsell.Main
import stanic.stsell.hooks.Vault
import stanic.stsell.utils.Messages
import stanic.stsell.utils.SellUtils
import stanic.stsell.utils.format
import stanic.stutils.bukkit.event.event
import stanic.stutils.bukkit.message.send

class PlayerAutoSellListeners {

    fun onPickup(main: Main) = main.event<PlayerPickupItemEvent> { event ->
        val player = event.player
        val item = event.item.itemStack

        if (main.player[player.name]!!.autoSell) {
            var items = 0
            var total = 0.0
            if (item != null && item.type != Material.AIR) {
                val sell = SellUtils().sellItem(item.typeId, item.durability.toInt())
                if (sell != null) {
                    items += item.amount
                    var amount = sell.money
                    sell.booster.keys.forEach {
                        if (sell.booster[it]!! > amount && player.hasPermission(it)) amount = sell.booster[it]!!
                    }
                    amount *= items
                    total += amount
                    event.item.remove()
                    event.isCancelled = true
                }
            }

            if (total > 0) {
                Vault.eco.depositPlayer(player, total)
                player.send(
                    Messages().get("itemsSoldAuto").replace("{amount}", "$items").replace(
                        "{money}",
                        total.format()
                    )
                )
            }
        }
    }

    fun onClickInShift(main: Main) = main.event<PlayerToggleSneakEvent> { event ->
        val player = event.player

        if (player.isSneaking && main.player[player.name]!!.shiftSell) {
            var items = 0
            var total = 0.0

            for (item in player.inventory.contents) {
                if (item != null) {
                    val sell = SellUtils().sellItem(item.typeId, item.durability.toInt())
                    if (sell != null) {
                        items += item.amount
                        var amount = sell.money
                        sell.booster.keys.forEach {
                            if (sell.booster[it]!! > amount && player.hasPermission(it)) amount = sell.booster[it]!!
                        }
                        amount *= items
                        total += amount
                        player.inventory.remove(item)
                    }
                }
            }

            if (total > 0) {
                Vault.eco.depositPlayer(player, total)
                player.send(
                    Messages().get("itemsSoldShift").replace("{amount}", "$items").replace(
                        "{money}",
                        total.format()
                    )
                )
            }
        }
    }

}