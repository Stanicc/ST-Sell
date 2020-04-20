package stanic.stsell.bukkit.events

import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.inventory.ItemStack
import stanic.stsell.Main
import stanic.stsell.factory.SellFactory
import stanic.stsell.factory.model.Drops
import stanic.stsell.factory.verifyIfIsNegative
import stanic.stsell.hooks.Vault
import stanic.stsell.utils.*
import stanic.stutils.bukkit.event.event
import stanic.stutils.bukkit.message.send

class PlayerKillListener {

    fun onKill(main: Main) = main.event<EntityDeathEvent>(EventPriority.HIGHEST) { event ->
        if (event.entity.killer is Player) {
            val player = event.entity.killer
            val entity = event.entity

            if (!main.drops.containsKey(player.name)) {
                main.drops[player.name] =
                    Drops(0.0, 0.0, 0.0, 0.0, ArrayList(), ArrayList())
            }

            if (!main.player.containsKey(player.name)) main.player[player.name] =
                stanic.stsell.factory.model.Player(
                    autoSell = false,
                    shiftSell = false,
                    enableDrops = true
                )

            if (!main.player[player.name]!!.enableDrops || !Main.settings.getBoolean("Drops.mobs.enable")) return@event

            for (world in Main.settings.getStringList("Drops.mobs.blockedWorlds")) if (event.entity.world.name == world) return@event

            val sell = SellUtils().sellMob(entity.type.name)
            if (sell != null) {
                sell.booster.keys.forEach {
                    if (sell.booster[it]!! > sell.money && player.hasPermission(it)) sell.money = sell.booster[it]!!
                }

                val drops = main.drops[player.name]!!

                val droppedItems = ArrayList<ItemStack>()
                event.drops.forEach { droppedItems.add(it) }

                if (droppedItems.isEmpty()) return@event

                if (EnchantmentsUtils.getLooting(player) > 1) {
                    var looting = EnchantmentsUtils.getLooting(player)
                    while (looting > 0) {
                        droppedItems.add(droppedItems[0])
                        looting -= 1
                    }
                }

                if (Main.instance.jhmobs && player.isSneaking) {
                    if (entity.hasMetadata("JH_StackMobs")) {
                        val amount =
                            droppedItems.size * JH_StackMobs.API.getStackAmount(entity)

                        while (droppedItems.size < amount) {
                            event.drops.forEach { droppedItems.add(it) }
                        }
                    }
                }

                val mobDrops = droppedItems.toList()

                event.drops.clear()
                event.entity.remove()

                if (main.player[player.name]!!.autoSell && Main.settings.getBoolean("Drops.mobs.enableAutoSell")) {
                    Vault.eco.depositPlayer(player, sell.money * mobDrops.size)

                    Messages().get("dropsSoldActionbar")
                        .replace("{amount}", "${mobDrops.size}").replace(
                            "{money}",
                            (sell.money * mobDrops.size).format()
                        ).sendInActionbar(player)

                    return@event
                }

                if ((drops.priceMob * mobDrops.size + drops.sellMob).verifyIfIsNegative()) {
                    Vault.eco.depositPlayer(player, sell.money * mobDrops.size)

                    Messages().get("dropsLimitOutmoded")
                        .replace("{amount}", "${mobDrops.size}").replace(
                            "{money}",
                            (sell.money * mobDrops.size).format()
                        ).sendInActionbar(player)
                } else {
                    SellFactory().addDrops(
                        drops,
                        mobDrops.size.toDouble(),
                        sell.money * (mobDrops.size),
                        "mob"
                    )
                    SellFactory().addItems(drops, mobDrops, "mob")

                    if (Main.settings.getBoolean("Drops.mobs.enableNewDropsChat"))
                    player.send(
                        Messages().get("newDrops").replace(
                            "{amount}",
                            "${mobDrops.size}"
                        ).replace(
                            "{money}",
                            (sell.money * mobDrops.size).format()
                        )
                    )

                    if (Main.settings.getBoolean("Drops.mobs.enableNewDropsActionbar"))
                    Messages().get("newDropsActionbarKill").replace(
                        "{amount}",
                        "${mobDrops.size}"
                    ).replace("{money}", (sell.money * mobDrops.size).format())
                        .sendInActionbar(player)
                }
            }
        }
    }

}