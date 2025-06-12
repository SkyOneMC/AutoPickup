package us.thezircon.play.autopickup.utils;

import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import us.thezircon.play.autopickup.AutoPickup;

import java.util.HashMap;

public class InventoryUtils {
    private static final long COOLDOWN_MILLIS = 15_000L; // 15 seconds
    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    public static void handleItemOverflow(Location loc, Player player, boolean doFullInvMSG, HashMap<Integer, ItemStack> leftOver, AutoPickup plugin) {
        // Drop leftover items at location
        leftOver.values().forEach(item -> player.getWorld().dropItemNaturally(loc, item));

        if (!doFullInvMSG) return;

        long lastNotification = AutoPickup.lastInvFullNotification.getOrDefault(player.getUniqueId(), 0L);
        long timeSinceLast = System.currentTimeMillis() - lastNotification;

        if (timeSinceLast >= COOLDOWN_MILLIS) {
            // Using Adventure Component here could be an improvement if plugin.getMsg() supports it.
            PLUGIN.getMsg().send(player, Lang.FULL_INVENTORY);
            AutoPickup.lastInvFullNotification.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    private static boolean isMendable(ItemStack item) {
        if (item == null) return false;
        if (!item.containsEnchantment(Enchantment.MENDING)) return false;
        ItemMeta meta = item.getItemMeta();
        return meta instanceof Damageable;
    }

    public static int mend(ItemStack item, int xp) {
        if (!isMendable(item)) return xp;

        ItemMeta meta = item.getItemMeta();
        Damageable damage = (Damageable) meta;
        int damageAmount = damage.getDamage();

        int repairAmount = Math.min(xp, damageAmount);
        int newDamage = damageAmount - repairAmount;

        if (newDamage <= 0 && damageAmount > 0) {
            fix(item);
        } else {
            damage.setDamage(newDamage);
            item.setItemMeta(meta);
        }

        xp -= repairAmount;
        return xp;
    }

    private static void fix(ItemStack item) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ItemMeta meta = item.getItemMeta();
                if (meta instanceof Damageable damage) {
                    damage.setDamage(0);
                    item.setItemMeta(meta);
                }
            }
        }.runTaskLater(PLUGIN, 1);
    }

    public static void applyMending(Player player, int xp) {
        player.giveExp(xp); // Give player XP first

        mend(player.getInventory().getItemInMainHand(), xp);
        mend(player.getInventory().getItemInOffHand(), xp);

        for (ItemStack armorPiece : player.getInventory().getArmorContents()) {
            mend(armorPiece, xp);
        }
    }
}
