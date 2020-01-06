package stanic.stsell.utils

import com.connorlinfoot.actionbarapi.ActionBarAPI
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import stanic.stsell.Main
import stanic.stsell.factory.SellFactory
import stanic.stsell.factory.model.Drops
import stanic.stutils.bukkit.message.replaceColor
import stanic.stutils.bukkit.message.send
import stanic.stutils.server.utils.sendActionBar
import java.text.DecimalFormat

class Messages {

    fun onlyInGame(sender: CommandSender) = sender.send(get("onlyInGame"))

    fun get(msg: String) = Main.settings.getString("Messages.$msg").replaceColor().replace("@n", "\n")!!

}

fun String.sendInActionbar(player: Player) {
    if (Main.settings.getBoolean("ActionBar.enable")) {
        if (Bukkit.getPluginManager().getPlugin("ActionBarAPI") != null) ActionBarAPI.sendActionBar(player, this)
        else sendActionBar(player, this)
    }
}

fun Double.format(): String {
    val chars = ArrayList<String>()
    for (i in Main.settings.getStringList("MoneyFormat")) chars.add(i)

    var value = this
    var index = 0
    while (value / 1000.0 >= 1.0) {
        value /= 1000.0
        ++index
    }

    val decimalFormat = DecimalFormat("#.####")
    return String.format("%s %s", decimalFormat.format(value), chars[index])
}

fun Int.formatDrops(): String {
    val formatter = DecimalFormat("###,###,###.##")
    return formatter.format(this)
}

fun String.replaceInfo(drops: Drops) = this.run {
    replace("{miningAmount}", drops.mineItems.size.formatDrops())
        .replace("{miningPrice}", SellFactory().getPrice(drops, "mining").format())
        .replace("{mobAmount}", drops.mobItems.size.formatDrops())
        .replace("{mobPrice}", SellFactory().getPrice(drops, "mob").format())
        .replaceColor()
}!!