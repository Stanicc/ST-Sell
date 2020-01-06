package stanic.stsell.factory

import org.bukkit.inventory.ItemStack
import stanic.stsell.factory.model.Drops

class SellFactory {

    fun getPrice(drops: Drops, type: String): Double {
        return when (type) {
            "mining" -> drops.priceMine
            "mob" -> drops.priceMob
            else -> 0.0
        }
    }

    fun addItems(drops: Drops, items: List<ItemStack>, type: String) = items.forEach {
        when (type) {
            "mining" -> drops.mineItems.add(it)
            "mob" -> drops.mobItems.add(it)
        }
    }

    fun removeItem(drops: Drops, item: ItemStack, money: Double, type: String) {
        when (type) {
            "mining" -> {
                drops.sellMine -= item.amount
                drops.priceMine -= money * item.amount

                for (i in 0..item.amount) {
                    val amount = item.amount
                    item.amount = 1
                    drops.mineItems.remove(item).apply { item.amount = amount }
                }
            }
            "mob" -> {
                drops.sellMob -= item.amount
                drops.priceMob -= money * item.amount

                for (i in 0..item.amount) {
                    val amount = item.amount
                    item.amount = 1
                    drops.mobItems.remove(item).apply { item.amount = amount }
                }
            }
        }

    }

    fun addDrops(drops: Drops, amount: Double, money: Double, type: String) {
        when (type) {
            "mining" -> {
                drops.sellMine += amount
                drops.priceMine += money
            }
            "mob" -> {
                drops.sellMob += amount
                drops.priceMob += money
            }
        }
    }

    fun clearDrops(drops: Drops, type: String) {
        when (type) {
            "mining" -> {
                drops.priceMine = 0.0
                drops.sellMine = 0.0
                drops.mineItems.clear()
            }
            "mob" -> {
                drops.priceMob = 0.0
                drops.sellMob = 0.0
                drops.mobItems.clear()
            }
        }
    }

}

fun Double.verifyIfIsNegative() = this < 0