package stanic.stsell.utils

import stanic.stsell.Main

class SellUtils {

    private val sett = Main.settings

    fun sellItem(id: Int, data: Int): Sell? {
        var sell: Sell? = null
        val item = sett.getConfigurationSection("Sell").getKeys(false).filter { sett.getInt("Sell.$it.id") == id && sett.getInt("Sell.$it.data") == data }.getOrNull(0)

        if (item != null) {
            val price = sett.getDouble("Sell.$item.price")
            sell = Sell(price)

            sett.getConfigurationSection("Sell.$item.booster").getKeys(false).forEach { booster ->
                sell.booster["stsell.booster.$booster"] = sett.getDouble("Sell.$item.booster.$booster")
            }
        }

        return sell
    }

    fun sellBlock(id: Int, data: Int): Sell? {
        var sell: Sell? = null
        val selected = sett.getConfigurationSection("Drops.mining.items").getKeys(false).filter { sett.getInt("Drops.mining.items.$it.id") == id && sett.getInt("Drops.mining.items.$it.data") == data }.getOrNull(0)

        if (selected != null) {
            val block = "Drops.mining.items.$selected"

            val price = sett.getDouble("$block.price")
            sell = Sell(price)

            sett.getConfigurationSection("$block.booster").getKeys(false).forEach { booster ->
                sell.booster["stsell.booster.$booster"] = sett.getDouble("$block.booster.$booster")
            }
        }

        return sell
    }

    fun sellMob(type: String): Sell? {
        var sell: Sell? = null
        val selected = sett.getConfigurationSection("Drops.mobs.items").getKeys(false).filter { sett.getString("Drops.mobs.items.$it.type").toLowerCase() == type.toLowerCase() }.getOrNull(0)

        if (selected != null) {
            val mob = "Drops.mobs.items.$selected"

            val price = sett.getDouble("$mob.price")
            sell = Sell(price)

            sett.getConfigurationSection("$mob.booster").getKeys(false).forEach { booster ->
                sell.booster["stsell.booster.$booster"] = sett.getDouble("$mob.booster.$booster")
            }
        }

        return sell
    }

    fun verifyItem(id: Int, data: Int): String? {
        var type: String? = null
        val selected = sett.getConfigurationSection("Drops.items").getKeys(false).filter { sett.getInt("Drops.items.$it.id") == id && sett.getInt("Drops.items.$it.data") == data }.getOrNull(0)

        if (selected != null) {
            val item = "Drops.items.$selected"
            type = "Drops.${sett.getString("$item.type")}.items.${sett.getString("$item.from")}"
        }

        return type
    }

    inner class Sell(var money: Double) {
        val booster = HashMap<String, Double>()
    }

}