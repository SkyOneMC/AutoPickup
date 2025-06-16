package us.thezircon.play.autopickup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import us.thezircon.play.autopickup.AutoPickup;

import java.time.Duration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class InventoryUtils {
    private static final long COOLDOWN_MILLIS = 15_000L; // 15 seconds
    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);

    public static void handleDropsGive(Player player, Location location, List<ItemStack> drops, boolean isSmelt) {
        Iterator<ItemStack> iterator = drops.iterator();
        while (iterator.hasNext()) {
            ItemStack drop = iterator.next();

            if (!handleDropGive(player, location, drop, isSmelt)) continue;

            iterator.remove();
        }
    }

    public static boolean handleDropGive(Player player, Location location, ItemStack drop, boolean isSmelt) {
        if (PLUGIN.getConfigManager().isDoBlacklisted()
                && PLUGIN.getConfigManager().getBlacklistedItems().contains(drop.getType().toString())) {
            return false;
        }

        if (isSmelt && PLUGIN.auto_smelt_blocks.contains(player)) {
            drop = AutoSmeltUtils.smelt(drop, player);
        }

        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(drop);

        if (!leftover.isEmpty()) {
            handleItemOverflow(player, location, leftover);
        }
        return true;
    }

    public static void handleItemOverflow(Player player, Location loc, HashMap<Integer, ItemStack> leftOver) {
        sendInventoryFullMessage(player);
        sendInventoryFullTitle(player);

        if (PLUGIN.getConfigManager().isVoidOnFullInv()) return;

        leftOver.values().forEach(item -> player.getWorld().dropItemNaturally(loc, item));
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

    private static void sendInventoryFullTitle(Player player) {
        if (!PLUGIN.getConfigManager().isTitleBar()) return;

        Component title1 = PLUGIN.getMsg().get(Lang.TITLE_LINE_1);
        Component title2 = PLUGIN.getMsg().get(Lang.TITLE_LINE_2);

        Duration fadeIn = Ticks.duration(1);
        Duration stay = Ticks.duration(20);
        Duration fadeOut = Ticks.duration(1);

        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);

        Title title = Title.title(title1, title2, times);

        player.showTitle(title);
    }

    private static void sendInventoryFullMessage(Player player) {
        if (!PLUGIN.getConfigManager().isDoFullInvMsg()) return;

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        long lastSent = AutoPickup.lastInvFullNotification.getOrDefault(uuid, 0L);
        if ((now - lastSent) >= COOLDOWN_MILLIS) {
            PLUGIN.getMsg().send(player, Lang.FULL_INVENTORY);
            AutoPickup.lastInvFullNotification.put(uuid, now);
        }
    }
}
