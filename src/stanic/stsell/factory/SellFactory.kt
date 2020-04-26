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

    fun addItems(drops: Drops, items: HashMap<ItemStack, Int>, type: String) = items.forEach {
        when (type) {
            "mining" -> {
                if (drops.mineItems[it.key] == null) drops.mineItems[it.key] = 0L
                drops.mineItems[it.key] = drops.mineItems[it.key]!!.plus(it.value.toLong())
            }
            "mob" -> {
                if (drops.mobItems[it.key] == null) drops.mobItems[it.key] = 0L
                drops.mobItems[it.key] = drops.mobItems[it.key]!!.plus(it.value.toLong())
            }
        }
    }

    fun removeItem(drops: Drops, item: ItemStack, money: Double, type: String) {
        when (type) {
            "mining" -> {
                drops.sellMine -= item.amount
                drops.priceMine -= money * item.amount

                val amount = item.amount
                item.amount = 1
                if ((drops.mineItems[item]!! - amount) <= 0) drops.mineItems.remove(item)
                else drops.mineItems[item] = drops.mineItems[item]!! - amount

                item.amount = amount
            }
            "mob" -> {
                drops.sellMob -= item.amount
                drops.priceMob -= money * item.amount

                val amount = item.amount
                item.amount = 1
                if ((drops.mobItems[item]!! - amount) <= 0) drops.mobItems.remove(item)
                else drops.mobItems[item] = drops.mobItems[item]!! - amount

                item.amount = amount
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