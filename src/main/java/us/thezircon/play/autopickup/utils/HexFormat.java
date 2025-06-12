package us.thezircon.play.autopickup.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class HexFormat {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    public static Component format(String msg) {
        if (msg == null || msg.isEmpty()) {
            return Component.empty();
        }
        return MINI.deserialize(msg);
    }

}
