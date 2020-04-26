package stanic.stsell.bukkit.events

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.DefaultFlag
import me.MnMaxon.AutoPickup.API.DropToInventoryEvent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import stanic.stsell.Main
import stanic.stsell.factory.SellFactory
import stanic.stsell.factory.model.Drops
import stanic.stsell.factory.model.Player
import stanic.stsell.factory.verifyIfIsNegative
import stanic.stsell.hooks.Vault
import stanic.stsell.utils.*
import stanic.stutils.bukkit.event.event
import stanic.stutils.bukkit.message.send

@Suppress("DEPRECATION")
class PlayerMiningListener {

    private val sett = Main.settings

    fun onMining(main: Main) = main.event<BlockBreakEvent>(EventPriority.MONITOR) { event ->
        val player = event.player
        val block = event.block

        if (!main.drops.containsKey(player.name)) {
            main.drops[player.name] =
                Drops(0.0, 0.0, 0.0, 0.0, HashMap(), HashMap())
        }

        if (!main.player.containsKey(player.name)) main.player[player.name] = Player(
            autoSell = false,
            shiftSell = false,
            enableDrops = true
        )

        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            val worldGuard =
                if (WorldGuardPlugin.inst().getRegionManager(block.location.world)
                        .getApplicableRegions(block.location) != null
                ) WorldGuardPlugin.inst().regionContainer.createQuery().testBuild(
                    block.location,
                    player,
                    DefaultFlag.BLOCK_BREAK
                ) else true

            if (!worldGuard) {
                if (!player.hasPermission("worldguard.region.bypass.*")) {
                    event.isCancelled = true
                    return@event
                }
            }
        }
        if (!main.player[player.name]!!.enableDrops || !Main.settings.getBoolean("Drops.mining.enable")) return@event

        for (world in sett.getStringList("Drops.mining.blockedWorlds")) if (block.world.name == world) return@event

        val sell = SellUtils().sellBlock(block.typeId, block.state.data.toItemStack().durability.toInt())
        if (sell != null && sett.getBoolean("Drops.mining.enable")) {
            sell.booster.keys.forEach {
                if (sell.booster[it]!! > sell.money && player.hasPermission(it)) sell.money = sell.booster[it]!!
            }

            val drops = main.drops[player.name]!!

            val itemsFilter =
                event.block.drops.toHashSet()
                    .filter { SellUtils().verifyItem(it.typeId, it.durability.toInt()) != null }
            val droppedItems = itemsFilter
                .associateWith { item ->
                    itemsFilter.filter { item.typeId == it.typeId && item.durability == it.durability }
                        .sumBy { it.amount }
                } as HashMap<ItemStack, Int>

            val fortune = EnchantmentsUtils.getFortune(player)
            var amountOfDrops = 0

            droppedItems.keys.forEach {
                amountOfDrops += if (amountOfDrops == 0) (droppedItems.getValue(it) * fortune).apply {
                    droppedItems[it] = droppedItems.getValue(it) * fortune
                }
                else droppedItems.getValue(it)
                droppedItems[it] = droppedItems.getValue(it) + it.amount
                amountOfDrops += it.amount
                if (it.amount > 1) it.amount = 1
            }

            if (main.player[player.name]!!.autoSell && Main.settings.getBoolean("Drops.mining.enableAutoSell")) {
                Vault.eco.depositPlayer(player, sell.money * amountOfDrops)

                Messages().get("dropsSoldActionbar")
                    .replace("{amount}", "$amountOfDrops").replace(
                        "{money}",
                        (sell.money * amountOfDrops).format()
                    ).sendInActionbar(player)

                block.type = Material.AIR
                block.drops.clear()
                block.breakNaturally()
                event.isCancelled = true
                return@event
            }

            if ((drops.priceMine * amountOfDrops + drops.sellMine).verifyIfIsNegative()) {
                Vault.eco.depositPlayer(player, sell.money * amountOfDrops)

                Messages().get("dropsLimitOutmoded")
                    .replace("{amount}", "$amountOfDrops").replace(
                        "{money}",
                        (sell.money * amountOfDrops).format()
                    ).sendInActionbar(player)
            } else {
                Bukkit.getServer().scheduler.runTaskAsynchronously(Main.instance) {
                    SellFactory().addDrops(
                        drops,
                        amountOfDrops.toDouble(),
                        sell.money * amountOfDrops.toDouble(),
                        "mining"
                    )
                    SellFactory().addItems(drops, droppedItems, "mining")
                }

                if (Main.settings.getBoolean("Drops.mining.enableNewDropsChat"))
                    player.send(
                        Messages().get("newDrops").replace(
                            "{amount}",
                            "$amountOfDrops"
                        ).replace("{money}", (sell.money * amountOfDrops).format())
                    )

                if (Main.settings.getBoolean("Drops.mining.enableNewDropsActionbar"))
                    Messages().get("newDropsActionbarMining").replace(
                        "{amount}",
                        "$amountOfDrops"
                    ).replace("{money}", (sell.money * amountOfDrops).format())
                        .sendInActionbar(player)
            }
            block.type = Material.AIR
            block.drops.clear()
            block.breakNaturally()
            event.isCancelled = true
        }
    }

    fun onDropItemListener(main: Main) = main.event<DropToInventoryEvent> { e ->
        if (e.items.isNotEmpty()) {
            val sell = SellUtils().verifyItem(e.items[0].typeId, e.items[0].durability.toInt())
            if (sell != null) {
                e.isCancelled = true
                e.items.clear()
            }
        }
    }

}