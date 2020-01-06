package stanic.stsell.bukkit.commands

import org.bukkit.entity.Player
import stanic.stsell.Main
import stanic.stsell.hooks.Vault
import stanic.stsell.utils.Menus
import stanic.stsell.utils.Messages
import stanic.stsell.utils.SellUtils
import stanic.stsell.utils.format
import stanic.stutils.bukkit.command.command
import stanic.stutils.bukkit.message.send

class SellCommand {

    fun run(main: Main) = main.command("sell") { sender, args ->
        if (sender !is Player) Messages().onlyInGame(sender)
        else {
            if (args.isEmpty()) {
                var items = 0
                var total = 0.0

                for (item in sender.inventory.contents) {
                    if (item != null) {
                        val sell = SellUtils().sellItem(item.typeId, item.durability.toInt())
                        if (sell != null) {
                            items += item.amount
                            var amount = sell.money
                            sell.booster.keys.forEach {
                                if (sell.booster[it]!! > amount && sender.hasPermission(it)) amount = sell.booster[it]!!
                            }
                            amount *= items
                            total += amount
                            sender.inventory.remove(item)
                        }
                    }
                }

                if (total > 0) {
                    Vault.eco.depositPlayer(sender, total)
                    sender.send(Messages().get("itemsSold").replace("{amount}", "$items").replace("{money}", total.format()))
                } else sender.send(Messages().get("notHasItems"))

                return@command
            }

            when (args[0].toLowerCase()) {
                "menu" -> Menus.openMainMenu(sender)
                "reload" -> {
                    if (sender.hasPermission("stsell.admin")) {
                        main.loadSettings().apply {
                            sender.send(Messages().get("configReloaded"))
                        }
                    } else {
                        sender.send(Messages().get("noPerm"))
                    }
                }
            }
        }
    }

}