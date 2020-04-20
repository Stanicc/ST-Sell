package stanic.stsell.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import stanic.stsell.Main
import stanic.stsell.factory.SellFactory
import stanic.stsell.utils.format
import stanic.stsell.utils.formatDrops

class PlaceholderAPI: PlaceholderExpansion() {

    override fun getVersion(): String {
        return "1.0"
    }

    override fun getAuthor(): String {
        return "Stanic"
    }

    override fun getIdentifier(): String {
        return "sell"
    }

    override fun onPlaceholderRequest(p: Player?, params: String?): String {
        if (p == null) return ""

        val drops = Main.instance.drops[p.name]!!
        return when (params) {
            "miningAmount" -> drops.mineItems.size.formatDrops()
            "miningPrice" -> SellFactory().getPrice(drops, "mining").format()
            "mobAmount" -> drops.mobItems.size.formatDrops()
            "mobPrice" -> SellFactory().getPrice(drops, "mob").format()
            "totalAmount" -> (drops.mineItems.size + drops.mobItems.size).formatDrops()
            "totalPrice" -> (SellFactory().getPrice(drops, "mining") + SellFactory().getPrice(drops, "mob")).format()
            else -> ""
        }
    }

}