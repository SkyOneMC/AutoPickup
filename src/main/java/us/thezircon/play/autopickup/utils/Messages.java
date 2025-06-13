package us.thezircon.play.autopickup.utils;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import us.thezircon.play.autopickup.AutoPickup;

import java.util.EnumMap;
import java.util.Map;

@Getter
public class Messages {

    private static final AutoPickup PLUGIN = AutoPickup.getPlugin(AutoPickup.class);
    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private final Map<Lang, Component> messages = new EnumMap<>(Lang.class);

    private Component updateNoticeMessage;
    private Component updateClickHereMessage;
    private final Component noConsoleMessage = Component.text("This command can only be executed by a player!", NamedTextColor.RED);

    public Messages() {
        reloadMessages();
        createCachedJoinMessages();
    }

    public void reloadMessages() {
        for (Lang key : Lang.values()) {
            messages.put(key, parseOrEmpty(key.getPath()));
        }
        checkConfigVersion();
    }

    private Component parseOrEmpty(String path) {
        String raw = PLUGIN.getConfig().getString(path);
        return (raw == null || raw.isEmpty()) ? Component.empty() : MINI.deserialize(raw);
    }

    private void checkConfigVersion() {
        double ver = PLUGIN.getConfig().getDouble("ConfigVersion", 1.0);
        if (ver < 1.2) {
            PLUGIN.getLogger().warning("----------------------------------");
            PLUGIN.getLogger().warning("Outdated Config! Your version: " + ver + " Latest: 1.2");
            PLUGIN.getLogger().warning("Please update your config from the default on Spigot.");
            PLUGIN.getLogger().warning("----------------------------------");
        }
    }

    public void createCachedJoinMessages() {
        String currentVersion = PLUGIN.getDescription().getVersion();

        updateNoticeMessage = Component.text(" Version: ", NamedTextColor.YELLOW)
                .append(Component.text(currentVersion, NamedTextColor.RED))
                .append(Component.text(" is not up to date. Please check your console on next startup or reload.", NamedTextColor.YELLOW));

        updateClickHereMessage = Component.text("➤ Click HERE to view the latest version.", NamedTextColor.GOLD)
                .clickEvent(ClickEvent.openUrl("https://www.spigotmc.org/resources/autopickup-1-16-support.70157/"))
                .hoverEvent(HoverEvent.showText(Component.text("Click to open on Spigot!", NamedTextColor.YELLOW)));
    }

    // === Core Access ===

    public Component get(Lang lang) {
        return messages.getOrDefault(lang, Component.empty());
    }

    // === Messaging API ===

    public void send(CommandSender sender, Lang lang) {
        send(sender, get(lang), true);
    }

    public void send(CommandSender sender, Component component) {
        send(sender, component, true);
    }

    public void send(CommandSender sender, Component component, boolean prefix) {
        if (sender instanceof Player player) {
            send((Audience) player, component, prefix);
        } else {
            sendToConsole(component, prefix);
        }
    }

    public void send(Audience audience, Component component) {
        send(audience, component, true);
    }

    public void send(Audience audience, Component component, boolean prefix) {
        Component finalMessage = prefix ? get(Lang.PREFIX).append(Component.space()).append(component) : component;
        audience.sendMessage(finalMessage);
    }

    /**
     * Sends a message to the server console with optional prefix.
     */
    public void sendToConsole(Component component, boolean prefix) {
        CommandSender console = Bukkit.getServer().getConsoleSender();
        Component fullMessage = prefix ? get(Lang.PREFIX).append(Component.space()).append(component) : component;

        String legacyMessage = LegacyComponentSerializer.legacySection().serialize(fullMessage);
        console.sendMessage(legacyMessage);
    }

    /**
     * Convenience method that sends to console with prefix.
     */
    public void sendToConsole(Component component) {
        sendToConsole(component, true);
    }
}
