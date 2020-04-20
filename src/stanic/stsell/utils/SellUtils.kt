package stanic.stsell.utils

import stanic.stsell.Main

class SellUtils {

    private val sett = Main.settings

    fun sellItem(id: Int, data: Int): Sell? {
        var sell: Sell? = null

        val section = sett.getConfigurationSection("Sell")
        for (it in section.getKeys(false)) {
            val item = "Sell.$it"
            val price = sett.getDouble("$item.price")
            if (sett.getInt("$item.id") == id && sett.getInt("$item.data") == data) {
                sell = Sell(price)

                sett.getConfigurationSection("$item.booster").getKeys(false).forEach { booster ->
                    sell.booster["stsell.booster.$booster"] = sett.getDouble("$item.booster.$booster")
                }

                break
            }
        }

        return sell
    }

    fun sellBlock(id: Int, data: Int): Sell? {
        var sell: Sell? = null

        val section = sett.getConfigurationSection("Drops.mining.items")
        for (it in section.getKeys(false)) {
            val block = "Drops.mining.items.$it"
            val price = sett.getDouble("$block.price")

            if (sett.getInt("$block.id") == id && sett.getInt("$block.data") == data) {
                sell = Sell(price)

                sett.getConfigurationSection("$block.booster").getKeys(false).forEach { booster ->
                    sell.booster["stsell.booster.$booster"] = sett.getDouble("$block.booster.$booster")
                }

                break
            }
        }

        return sell
    }

    fun sellMob(type: String): Sell? {
        var sell: Sell? = null

        val section = sett.getConfigurationSection("Drops.mobs.items")
        for (it in section.getKeys(false)) {
            val mob = "Drops.mobs.items.$it"
            val price = sett.getDouble("$mob.price")

            if (sett.getString("$mob.type").toLowerCase() == type.toLowerCase()) {
                sell = Sell(price)

                sett.getConfigurationSection("$mob.booster").getKeys(false).forEach { booster ->
                    sell.booster["stsell.booster.$booster"] = sett.getDouble("$mob.booster.$booster")
                }

                break
            }
        }

        return sell
    }

    fun verifyItem(id: Int, data: Int): String? {
        var type: String? = null

        val section = sett.getConfigurationSection("Drops.items")
        for (it in section.getKeys(false)) {
            val item = "Drops.items.$it"
            if (sett.getInt("$item.id") == id && sett.getInt("$item.data") == data) {
                type = "Drops.${sett.getString("$item.type")}.items.${sett.getString("$item.from")}"
                break
            }
        }

        return type
    }

    inner class Sell(var money: Double) {
        val booster = HashMap<String, Double>()
    }

}