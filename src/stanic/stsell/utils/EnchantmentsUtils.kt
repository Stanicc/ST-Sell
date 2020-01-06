package stanic.stsell.utils

import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import java.util.*

object EnchantmentsUtils {

    fun getFortune(p: Player): Int {
        if (p.itemInHand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)) {
            val level = p.itemInHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)
            val random = Random()
            var nivel = random.nextInt(4) + 1
            if (level >= 30) {
                val max = level / 6
                val least = level / 7
                nivel = random.nextInt(max) + least
            }
            return nivel
        }
        return 1
    }

    fun getLooting(p: Player): Int {
        if (p.itemInHand.containsEnchantment(Enchantment.LOOT_BONUS_MOBS)) {
            val level = p.itemInHand.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS)
            val random = Random()
            var nivel = random.nextInt(4) + 1
            if (level >= 30) {
                val max = level / 6
                val least = level / 7
                nivel = random.nextInt(max) + least
            }
            return nivel
        }
        return 1
    }

}