package stanic.stsell.factory.model

import org.bukkit.inventory.ItemStack

class Drops(
    var sellMine: Double,
    var sellMob: Double,
    var priceMine: Double,
    var priceMob: Double,
    var mineItems: ArrayList<ItemStack>,
    var mobItems: ArrayList<ItemStack>
)