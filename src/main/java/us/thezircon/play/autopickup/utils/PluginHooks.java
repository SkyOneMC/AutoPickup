package us.thezircon.play.autopickup.utils;

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI;
import lombok.Getter;
import org.bukkit.Bukkit;

@Getter
public class PluginHooks {

    private final boolean usingUpgradableHoppers;
    private final boolean usingLocketteProByBrunyman;
    private final boolean usingBentoBox;
    private final boolean usingQuickShop;
    private final boolean usingEpicFurnaces;
    private final boolean usingWildChests;
    private final boolean usingMythicMobs;
    private final boolean usingSSB2OneBlock;
    private final boolean usingPlaceholderAPI;

    public PluginHooks() {
        this.usingUpgradableHoppers = isPluginEnabled("UpgradeableHoppers");
        this.usingLocketteProByBrunyman = isPluginEnabled("LockettePro");
        this.usingBentoBox = isPluginEnabled("BentoBox");
        this.usingQuickShop = isPluginEnabled("QuickShop");
        this.usingEpicFurnaces = isPluginEnabled("EpicFurnaces");
        this.usingWildChests = isPluginEnabled("WildChests");
        this.usingMythicMobs = isPluginEnabled("MythicMobs");
        this.usingPlaceholderAPI = isPluginEnabled("PlaceholderAPI");

        if (isPluginEnabled("SuperiorSkyblock2")) {
            this.usingSSB2OneBlock = SuperiorSkyblockAPI.getModules().getModule("OneBlock") != null;
        } else {
            this.usingSSB2OneBlock = false;
        }
    }

    private boolean isPluginEnabled(String pluginName) {
        return Bukkit.getPluginManager().getPlugin(pluginName) != null;
    }
}
