package com.creativetechguy;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup(InterfaceHiderConfig.GROUP_NAME)
public interface InterfaceHiderConfig extends Config {
    String GROUP_NAME = "interface-hider";

    String SIDE_PANEL_POSITION = "panelPosition";

    @ConfigItem(
            keyName = SIDE_PANEL_POSITION,
            name = "Side Panel Position",
            description = "The location of the plugin's panel in the sidebar",
            position = 0
    )
    @Range(min = 0)
    default int panelPosition() {
        return 7;
    }

    String HIDE_SIDE_PANEL = "hideSidePanel";

    @ConfigItem(
            keyName = HIDE_SIDE_PANEL,
            name = "Hide Side Panel",
            description = "If you don't need to configure the plugin often, you can remove the entry from the side panel",
            position = 1
    )
    default boolean hideSidePanel() {
        return false;
    }

    String INTERFACES_SHOWN = "interfacesShown";

    @ConfigItem(
            keyName = INTERFACES_SHOWN,
            name = "Interfaces Shown",
            description = "The amount of filtering when listing interfaces in the side panel.",
            position = 2
    )
    default InterfaceCategories interfacesShown() {
        return InterfaceCategories.ONLY_NAMED;
    }

    String DISABLE_PLUGIN_INTERFACE_NAMES = "disablePluginInterfaceNames";

    @ConfigItem(
            keyName = DISABLE_PLUGIN_INTERFACE_NAMES,
            name = "Disable Plugin-provided Interface Names",
            description = "This only exists in the event an update makes all of the built-in names incorrect. Enable this to disable all default names.",
            position = 3
    )
    default boolean disablePluginInterfaceNames() {
        return false;
    }

    String OVERRIDES_KEY = "overrides";

    @ConfigItem(
            keyName = OVERRIDES_KEY,
            name = "Interface Overrides",
            description = "",
            hidden = true
    )
    default String overrides() {
        return "";
    }

    String LABELS_KEY = "labels";

    @ConfigItem(
            keyName = LABELS_KEY,
            name = "Interface Custom Labels",
            description = "",
            hidden = true
    )
    default String labels() {
        return "";
    }
}
