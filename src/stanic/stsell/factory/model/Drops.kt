package stanic.stsell.factory.model

import org.bukkit.inventory.ItemStack

class Drops(
    var sellMine: Double,
    var sellMob: Double,
    var priceMine: Double,
    var priceMob: Double,
    var mineItems: HashMap<ItemStack, Long>,
    var mobItems: HashMap<ItemStack, Long>
) {

    fun getDropsSize(type: String): Int {
        return when (type) {
            "mining" -> {
                var amount = 0
                mineItems.forEach {
                    amount += it.value.toInt()
                }

                amount
            }
            "mob" -> {
                var amount = 0
                mobItems.forEach {
                    amount += it.value.toInt()
                }

                amount
            }
            else -> 0
        }
    }

    fun getDrops(type: String): List<ItemStack> {
        val items = ArrayList<ItemStack>()

        when (type) {
            "mining" -> {
                mineItems.forEach {
                    val item = it.key
                    repeat(it.value.toInt()) {
                        if (items.size < 3510) items.add(item)
                        else return items
                    }
                }
            }
            "mob" -> {
                mobItems.forEach {
                    val item = it.key
                    repeat(it.value.toInt()) {
                        if (items.size < 3510) items.add(item)
                        else return items
                    }
                }
            }
        }

        return items
    }

}