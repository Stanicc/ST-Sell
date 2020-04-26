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
                    Drops(0.0, 0.0, 0.0, 0.0, HashMap(), HashMap())
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

                val itemsFilter =
                    event.drops.toHashSet()
                        .filter { SellUtils().verifyItem(it.typeId, it.durability.toInt()) != null }
                val droppedItems = itemsFilter
                    .associateWith { item ->
                        itemsFilter.filter { item.typeId == it.typeId && item.durability == it.durability }
                            .sumBy { it.amount }
                    } as HashMap<ItemStack, Int>

                val looting = EnchantmentsUtils.getLooting(player)
                var multiply = 1
                var amountOfDrops = 0

                if (Main.instance.jhmobs && player.isSneaking) {
                    if (entity.hasMetadata("JH_StackMobs")) multiply = JH_StackMobs.API.getStackAmount(entity)
                }

                droppedItems.keys.forEach {
                    amountOfDrops += if (amountOfDrops == 0) (droppedItems.getValue(it) * looting).apply {
                        droppedItems[it] = droppedItems.getValue(it) * looting
                    }
                    else droppedItems.getValue(it)
                    droppedItems[it] = droppedItems.getValue(it) * multiply + it.amount
                    if (it.amount > 1) {
                        amountOfDrops += it.amount
                        it.amount = 1
                    }
                }
                amountOfDrops *= multiply

                event.drops.clear()
                event.entity.remove()

                if (main.player[player.name]!!.autoSell && Main.settings.getBoolean("Drops.mobs.enableAutoSell")) {
                    Vault.eco.depositPlayer(player, sell.money * amountOfDrops)

                    Messages().get("dropsSoldActionbar")
                        .replace("{amount}", "$amountOfDrops").replace(
                            "{money}",
                            (sell.money * amountOfDrops).format()
                        ).sendInActionbar(player)

                    return@event
                }

                if ((drops.priceMob * (droppedItems.keys.size * (looting * multiply)) + drops.sellMob).verifyIfIsNegative()) {
                    Vault.eco.depositPlayer(player, sell.money * amountOfDrops)

                    Messages().get("dropsLimitOutmoded")
                        .replace("{amount}", "$amountOfDrops").replace(
                            "{money}",
                            (sell.money * amountOfDrops).format()
                        ).sendInActionbar(player)
                } else {
                    SellFactory().addDrops(
                        drops,
                        amountOfDrops.toDouble(),
                        sell.money * amountOfDrops,
                        "mob"
                    )
                    SellFactory().addItems(drops, droppedItems, "mob")

                    if (Main.settings.getBoolean("Drops.mobs.enableNewDropsChat"))
                        player.send(
                            Messages().get("newDrops").replace(
                                "{amount}",
                                "$amountOfDrops"
                            ).replace(
                                "{money}",
                                (sell.money * amountOfDrops).format()
                            )
                        )

                    if (Main.settings.getBoolean("Drops.mobs.enableNewDropsActionbar"))
                        Messages().get("newDropsActionbarKill").replace(
                            "{amount}",
                            "$amountOfDrops"
                        ).replace("{money}", (sell.money * amountOfDrops).format())
                            .sendInActionbar(player)
                }
            }
        }
    }

}