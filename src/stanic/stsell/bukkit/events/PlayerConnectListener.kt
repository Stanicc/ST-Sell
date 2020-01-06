package stanic.stsell.bukkit.events

import org.bukkit.event.player.PlayerJoinEvent
import stanic.stsell.Main
import stanic.stsell.factory.model.Drops
import stanic.stsell.factory.model.Player
import stanic.stutils.bukkit.event.event

class PlayerConnectListener {

    fun onJoin(main: Main) = main.event<PlayerJoinEvent> { event ->
        val player = event.player

        if (!main.drops.containsKey(player.name)) main.drops[player.name] =
            Drops(0.0, 0.0, 0.0, 0.0, ArrayList(), ArrayList())
        if (!main.player.containsKey(player.name)) main.player[player.name] = Player(
            autoSell = false,
            shiftSell = false,
            enableDrops = true
        )
    }

}