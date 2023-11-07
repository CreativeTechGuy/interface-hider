package com.creativetechguy;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@PluginDescriptor(
        name = "Interface Tweaker",
        description = "Enables tweaking interface elements by hiding or making transparent",
        configName = InterfaceTweakerConfig.GROUP_NAME
)
public class InterfaceTweakerPlugin extends Plugin {
    @Inject
    private Client client;

    @Inject
    private ClientThread clientThread;

    @Inject
    private InterfaceTweakerConfig config;

    @Inject
    private OverlayManager overlayManager;

    @Inject
    private InterfaceHighlightOverlay interfaceHighlightOverlay;

    @Inject
    private InterfaceOverrideConfigManager interfaceOverrideConfigManager;

    @Inject
    private ClientToolbar clientToolbar;

    private final List<Widget> visibleWidgets = new ArrayList<>();
    private final Map<Integer, DefaultWidgetSettings> previousWidgetSettings = new HashMap<>();
    private NavigationButton navButton;
    private InterfaceTweakerPanel panel;

    @Provides
    InterfaceTweakerConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(InterfaceTweakerConfig.class);
    }

    @Override
    protected void startUp() throws Exception {
        createNavButton();
        if (!config.hideSidePanel()) {
            clientToolbar.addNavigation(navButton);
            overlayManager.add(interfaceHighlightOverlay);
        }
        clientThread.invokeLater(this::tweakWidgets);
    }

    @Override
    protected void shutDown() throws Exception {
        clientThread.invokeLater(() -> {
            restoreWidgets();
            visibleWidgets.clear();
            previousWidgetSettings.clear();
        });
        clientToolbar.removeNavigation(navButton);
        overlayManager.remove(interfaceHighlightOverlay);
    }

    @Subscribe
    public void onConfigChanged(ConfigChanged event) {
        if (!event.getGroup().equals(InterfaceTweakerConfig.GROUP_NAME)) {
            return;
        }
        if (event.getKey().equals(InterfaceTweakerConfig.OVERRIDES_KEY) && event.getNewValue().isEmpty()) {
            interfaceOverrideConfigManager.clearOverrides();
        }
        if (event.getKey().equals(InterfaceTweakerConfig.LABELS_KEY) && event.getNewValue().isEmpty()) {
            interfaceOverrideConfigManager.clearLabels();
        }
        clientThread.invokeLater(this::tweakWidgets);
        if (event.getKey().equals(InterfaceTweakerConfig.INTERFACES_SHOWN) || event.getKey()
                .equals(InterfaceTweakerConfig.DISABLE_PLUGIN_INTERFACE_NAMES)) {
            panel.updateList();
        }
        if (event.getKey().equals(InterfaceTweakerConfig.HIDE_SIDE_PANEL)) {
            if (config.hideSidePanel()) {
                clientToolbar.removeNavigation(navButton);
                overlayManager.remove(interfaceHighlightOverlay);
            } else {
                clientToolbar.addNavigation(navButton);
                overlayManager.add(interfaceHighlightOverlay);
            }
        }
        if (event.getKey().equals(InterfaceTweakerConfig.SIDE_PANEL_POSITION)) {
            createNavButton();
        }
    }

    @Subscribe
    protected void onGameStateChanged(GameStateChanged event) {
        if (event.getGameState() != GameState.LOGGED_IN) {
            visibleWidgets.clear();
            panel.updateList();
        }
    }

    @Subscribe
    protected void onGameTick(GameTick event) {
        if (panel.isActive()) {
            List<Widget> previousVisibleWidgets = new ArrayList<>(visibleWidgets);
            visibleWidgets.clear();
            for (Widget root : client.getWidgetRoots()) {
                visibleWidgets.addAll(findLogicalWidgets(root));
            }
            if (!previousVisibleWidgets.equals(visibleWidgets)) {
                panel.updateList();
            }
        }
    }


    @Subscribe
    protected void onWidgetLoaded(WidgetLoaded event) {
        tweakWidgets();
    }

    private List<Widget> findLogicalWidgets(Widget root) {
        List<Widget> widgets = new ArrayList<>();
        // Don't continue looking down the tree when a banned widget is found
        if (BannedInterfaces.isBanned(root.getId())) {
            return widgets;
        }
        for (Widget child : getWidgetChildren(root)) {
            widgets.addAll(findLogicalWidgets(child));
        }
        if (isLogicalWidget(root)) {
            widgets.add(root);
        }
        return widgets;
    }

    private boolean isLogicalWidget(Widget widget) {
        if (widget.getParentId() == widget.getId()) {
            return false;
        }
        if (config.interfacesShown() == InterfaceCategories.ALL) {
            return true;
        }
        if ((interfaceOverrideConfigManager.hasLabel(widget.getId()) || NamedInterfaces.hasName(widget.getId()))) {
            return true;
        }
        if (widget.getType() != WidgetType.LAYER) {
            return false;
        }
        if (interfaceOverrideConfigManager.hasOverride(widget.getId()) || previousWidgetSettings.containsKey(widget.getId())) {
            return true;
        }
        if (widget.isHidden() || widget.getWidth() == 0 || widget.getHeight() == 0) {
            return false;
        }
        Collection<Widget> children = getWidgetChildren(widget);
        boolean areChildrenHidden = children.stream().allMatch((c) -> {
            if (c.getWidth() == 0 || c.getHeight() == 0 || c.getType() == WidgetType.LAYER || c.isHidden()) {
                return true;
            }
            return false;
        });
        if (areChildrenHidden) {
            return false;
        }
        return true;
    }

    private Collection<Widget> getWidgetChildren(Widget widget) {
        try {
            Set<Widget> children = new HashSet<>();
            if (widget.getChildren() != null) {
                children.addAll(Arrays.asList(widget.getChildren()));
            }
            if (widget.getDynamicChildren() != null) {
                children.addAll(Arrays.asList(widget.getDynamicChildren()));
            }
            if (widget.getNestedChildren() != null) {
                children.addAll(Arrays.asList(widget.getNestedChildren()));
            }
            if (widget.getStaticChildren() != null) {
                children.addAll(Arrays.asList(widget.getStaticChildren()));
            }
            return children.stream().filter((c) -> !c.isHidden()).collect(Collectors.toList());
        } catch (Exception ignored) {
        }
        return new HashSet<>();
    }

    private void restoreWidgets() {
        List<Integer> previousSettingsToRemove = new ArrayList<>();
        for (Map.Entry<Integer, DefaultWidgetSettings> previousOverride : previousWidgetSettings.entrySet()) {
            Widget widget = client.getWidget(previousOverride.getKey());
            if (widget != null) {
                if (interfaceOverrideConfigManager.hasOverride(previousOverride.getKey())) {
                    DefaultWidgetSettings previousSettings = previousOverride.getValue();
                    InterfaceOverride newSettings = interfaceOverrideConfigManager.getOverride(previousOverride.getKey());
                    if (previousSettings.getOpacity() == newSettings.getOpacity()) {
                        continue;
                    }
                }
                for (Widget child : getWidgetChildren(widget)) {
                    if (child.getId() == widget.getId()) {
                        child.setHidden(previousOverride.getValue().isHidden());
                        child.setOpacity(previousOverride.getValue().getOpacity());
                    }
                }
                widget.setHidden(previousOverride.getValue().isHidden());
                widget.setOpacity(previousOverride.getValue().getOpacity());
                previousSettingsToRemove.add(previousOverride.getKey());
            }
        }
        for (Integer id : previousSettingsToRemove) {
            previousWidgetSettings.remove(id);
        }
    }

    private void tweakWidgets() {
        restoreWidgets();
        for (InterfaceOverride override : interfaceOverrideConfigManager.getAllOverrides()) {
            Widget widget = client.getWidget(override.getId());
            if (widget != null) {
                if (!previousWidgetSettings.containsKey(widget.getId())) {
                    previousWidgetSettings.put(widget.getId(),
                            new DefaultWidgetSettings(widget.getOpacity()));
                }
                if (override.isHidden()) {
                    widget.setHidden(true);
                } else {
                    setWidgetOpacity(widget, override.getOpacity());
                }
            }
        }
    }

    private void setWidgetOpacity(Widget widget, int opacity) {
        if (!previousWidgetSettings.containsKey(widget.getId())) {
            previousWidgetSettings.put(widget.getId(),
                    new DefaultWidgetSettings(widget.getOpacity()));
        }
        widget.setOpacity(opacity);
        for (Widget child : getWidgetChildren(widget)) {
            setWidgetOpacity(child, opacity);
        }
    }

    private void createNavButton() {
        clientToolbar.removeNavigation(navButton);
        panel = new InterfaceTweakerPanel(interfaceOverrideConfigManager,
                visibleWidgets,
                interfaceHighlightOverlay,
                config);
        navButton = NavigationButton.builder()
                .tooltip(getName())
                .priority(config.panelPosition())
                .icon(ImageUtil.loadImageResource(getClass(), "icon.png"))
                .panel(panel)
                .build();
        clientToolbar.addNavigation(navButton);
    }
}
