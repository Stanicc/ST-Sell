package stanic.stsell.bukkit.commands

import org.bukkit.entity.Player
import stanic.stsell.Main
import stanic.stsell.utils.Menus
import stanic.stsell.utils.Messages
import stanic.stutils.bukkit.command.command

class DropsCommand {

    fun run(main: Main) = main.command("drops") { sender, _ ->
        if (sender !is Player) Messages().onlyInGame(sender)
        else {
            Menus.openDropsMenu(sender)
        }
    }

}