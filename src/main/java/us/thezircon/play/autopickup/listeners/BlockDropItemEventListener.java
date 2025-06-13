package us.thezircon.play.autopickup.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.inventory.ItemStack;
import us.thezircon.play.autopickup.AutoPickup;
import us.thezircon.play.autopickup.utils.AutoSmeltUtils;
import us.thezircon.play.autopickup.utils.Lang;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;

public class BlockDropItemEventListener implements Listener {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);
    private static final long NOTIFY_COOLDOWN_MS = 15_000;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onDrop(BlockDropItemEvent event) {
        if (event.isCancelled()) {
//            Bukkit.getLogger().info("BlockDropItemEvent - Cancelled");
            return;
        }
        Player player = event.getPlayer();
//        Bukkit.getLogger().info("BlockDropItemEvent 1");
        if (!PLUGIN.autopickup_list.contains(player)) return;

//        Bukkit.getLogger().info("BlockDropItemEvent 2");

        Block block = event.getBlock();
        Location location = block.getLocation();
        if (PLUGIN.getConfigManager().isWorldBlacklisted(location)) return;

        boolean doSmelt = PLUGIN.auto_smelt_blocks.contains(player);
        boolean doFullInvMsg = PLUGIN.getConfigManager().isDoFullInvMsg();
        boolean voidOnFullInv = PLUGIN.getConfigManager().isVoidOnFullInv();
        boolean useBlacklist = PLUGIN.getConfigManager().isDoBlacklisted();

        for (Item itemEntity : event.getItems()) {

//            Bukkit.getLogger().info("BlockDropItemEvent Loop");
            ItemStack drop = itemEntity.getItemStack();

            if (useBlacklist && PLUGIN.getConfigManager().getBlacklistedItems().contains(drop.getType().toString())) {
                continue;
            }

            if (doSmelt) {
                drop = AutoSmeltUtils.smelt(drop, player);
            }

            HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(drop);

            if (!overflow.isEmpty()) {
                handleFullInventory(player, location, overflow, doFullInvMsg, voidOnFullInv);
            }

            itemEntity.remove(); // Always remove dropped item if picked up or overflow handled
        }
    }

    private void handleFullInventory(Player player, Location dropLocation, HashMap<Integer, ItemStack> overflow,
                                     boolean doFullInvMsg, boolean voidOnFullInv) {

        if (isInventoryFull(player)) {
            sendInventoryFullTitle(player);
            if (doFullInvMsg) sendInventoryFullMessage(player);
        }

        if (voidOnFullInv) return;

        for (ItemStack leftover : overflow.values()) {
            player.getWorld().dropItemNaturally(dropLocation, leftover);
        }
    }

    private boolean isInventoryFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }

    private void sendInventoryFullTitle(Player player) {
        if (!PLUGIN.getConfig().getBoolean("titlebar.doTitleBar", false)) return;

        Component title1 = PLUGIN.getMsg().get(Lang.TITLE_LINE_1);
        Component title2 = PLUGIN.getMsg().get(Lang.TITLE_LINE_2);

        Duration fadeIn = Ticks.duration(1);
        Duration stay = Ticks.duration(20);
        Duration fadeOut = Ticks.duration(1);

        Title.Times times = Title.Times.times(fadeIn, stay, fadeOut);

        Title title = Title.title(title1, title2, times);

        player.showTitle(title);
    }

    private void sendInventoryFullMessage(Player player) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        long lastSent = AutoPickup.lastInvFullNotification.getOrDefault(uuid, 0L);
        if ((now - lastSent) >= NOTIFY_COOLDOWN_MS) {
            PLUGIN.getMsg().send(player, Lang.FULL_INVENTORY);
            AutoPickup.lastInvFullNotification.put(uuid, now);
        }
    }
}
