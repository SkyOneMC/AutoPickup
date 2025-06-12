package us.thezircon.play.autopickup.utils;

import lombok.Getter;

@Getter
public enum Lang {

    PREFIX("msgPrefix"),
    AUTO_PICKUP_ENABLE("msgAutoPickupEnable"),
    AUTO_PICKUP_DISABLE("msgAutoPickupDisable"),
    AUTO_ENABLED("msgAutoEnable"),
    AUTO_REENABLED("msgEnabledJoinMSG"),
    RELOAD("msgReload"),
    FULL_INVENTORY("msgFullInv"),
    NO_PERMS("msgNoperms"),
    AUTO_DROPS_ENABLE("msgAutoMobDropsEnable"),
    AUTO_DROPS_DISABLE("msgAutoMobDropsDisable"),
    AUTO_SMELT_ENABLE("msgAutoSmeltEnable"),
    AUTO_SMELT_DISABLE("msgAutoSmeltDisable"),
    TITLE_LINE_1("titlebar.line1"),
    TITLE_LINE_2("titlebar.line2");

    private final String path;

    Lang(String path) {
        this.path = path;
    }
}
