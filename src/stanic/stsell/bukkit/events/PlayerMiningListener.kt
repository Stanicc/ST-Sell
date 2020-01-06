package stanic.stsell.bukkit.events

import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.DefaultFlag
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.util.CraftMagicNumbers
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import stanic.stsell.Main
import stanic.stsell.factory.*
import stanic.stsell.factory.model.*
import stanic.stsell.hooks.Vault
import stanic.stsell.utils.*
import stanic.stutils.bukkit.event.event
import stanic.stutils.bukkit.message.replaceColor
import stanic.stutils.bukkit.message.send
import kotlin.random.Random

@Suppress("DEPRECATION")
class PlayerMiningListener {

    private val sett = Main.settings

    fun onMining(main: Main) = main.event<BlockBreakEvent>(EventPriority.MONITOR, cancelEvent = true) { event ->
        val player = event.player
        val block = event.block

        if (!main.drops.containsKey(player.name)) {
            main.drops[player.name] =
                Drops(0.0, 0.0, 0.0, 0.0, ArrayList(), ArrayList())

            if (!main.player.containsKey(player.name)) main.player[player.name] = Player(
                autoSell = false,
                shiftSell = false,
                enableDrops = true
            )
        }
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null) {
            val worldGuard = if (WorldGuardPlugin.inst().getRegionManager(block.location.world).getApplicableRegions(block.location) != null) WorldGuardPlugin.inst().regionContainer.createQuery().testState(block.location, player, DefaultFlag.BLOCK_BREAK) else true

            if (!worldGuard && !player.hasPermission("worldguard.region.bypass.*")) {
                event.isCancelled = true
                return@event
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

            val droppedItems = ArrayList<ItemStack>()
            event.block.drops.forEach { droppedItems.add(it) }

            if (EnchantmentsUtils.getFortune(player) > 1) {
                var fortune = EnchantmentsUtils.getFortune(player)
                while (fortune > 0) {
                    droppedItems.add(droppedItems[0])
                    fortune -= 1
                }
            }

            val blockDrops = droppedItems.toList()

            if (main.player[player.name]!!.autoSell && Main.settings.getBoolean("Drops.mining.enableAutoSell")) {
                Vault.eco.depositPlayer(player, sell.money * blockDrops.size)

                Messages().get("dropsSoldActionbar")
                    .replace("{amount}", "${blockDrops.size}").replace(
                        "{money}",
                        (sell.money * blockDrops.size).format()
                    ).sendInActionbar(player)

                return@event
            }

            if ((drops.priceMine * blockDrops.size + drops.sellMine).verifyIfIsNegative()) {
                Vault.eco.depositPlayer(player, sell.money * blockDrops.size)

                Messages().get("dropsLimitOutmoded")
                    .replace("{amount}", "${blockDrops.size}").replace(
                        "{money}",
                        (sell.money * blockDrops.size).format()
                    ).sendInActionbar(player)

            } else {
                SellFactory().addDrops(
                    drops,
                    blockDrops.size.toDouble(),
                    sell.money * blockDrops.size.toDouble(),
                    "mining"
                )

                SellFactory().addItems(drops, blockDrops, "mining")

                if (Main.settings.getBoolean("Drops.mining.enableNewDropsChat"))
                player.send(
                    Messages().get("newDrops").replace(
                        "{amount}",
                        "${blockDrops.size}"
                    ).replace("{money}", (sell.money * blockDrops.size).format())
                )

                if (Main.settings.getBoolean("Drops.mining.enableNewDropsActionbar"))
                Messages().get("newDropsActionbarMining").replace(
                    "{amount}",
                    "${blockDrops.size}"
                ).replace("{money}", (sell.money * blockDrops.size).format())
                    .sendInActionbar(player)
            }
            block.type = Material.AIR
            block.drops.clear()
            block.breakNaturally()
            event.isCancelled = true
        }
    }

}