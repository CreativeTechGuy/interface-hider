package com.creativetechguy;

import lombok.Getter;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.PluginPanel;

import javax.annotation.Nullable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

public class InterfaceHiderPanel extends PluginPanel {
    private InterfaceHiderConfig config;

    @Getter
    private boolean isActive = false;

    private InterfaceOverrideConfigManager interfaceOverrideConfigManager;
    private final JPanel widgetList;

    private final Collection<Widget> visibleWidgets;
    private final InterfaceHighlightOverlay interfaceHighlightOverlay;

    private int updateId = 0;

    public InterfaceHiderPanel(InterfaceOverrideConfigManager interfaceOverrideConfigManager, Collection<Widget> visibleWidgets, InterfaceHighlightOverlay interfaceHighlightOverlay, InterfaceHiderConfig config) {
        this.interfaceOverrideConfigManager = interfaceOverrideConfigManager;
        this.visibleWidgets = visibleWidgets;
        this.interfaceHighlightOverlay = interfaceHighlightOverlay;
        this.config = config;

        setLayout(new BorderLayout());

        JPanel header = new JPanel();
        header.setLayout(new BorderLayout());
        header.setBorder(new MatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel title = new JLabel("Interface Hider");
        title.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(title, BorderLayout.NORTH);

        JLabel description = new JLabel("Right click an item below for options");
        description.setHorizontalAlignment(SwingConstants.CENTER);
        header.add(description, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        widgetList = new JPanel();
        widgetList.setLayout(new BoxLayout(widgetList, BoxLayout.Y_AXIS));
        widgetList.setBorder(new EmptyBorder(10, 5, 5, 5));

        add(widgetList, BorderLayout.CENTER);
    }

    @Override
    public void onActivate() {
        isActive = true;
        updateList();
    }

    @Override
    public void onDeactivate() {
        isActive = false;
    }

    public void updateList() {
        updateId++;
        int currentUpdateId = updateId;
        ArrayList<Widget> visibleWidgetsCopy = new ArrayList<>(visibleWidgets);
        visibleWidgetsCopy.sort(Comparator.comparing(Widget::getId));
        ArrayList<InterfaceOverride> overridesCopy = new ArrayList<>(interfaceOverrideConfigManager.getAllOverrides());
        overridesCopy.sort(Comparator.comparing(InterfaceOverride::getId));
        SwingUtilities.invokeLater(() -> {
            if (updateId != currentUpdateId) {
                return;
            }
            widgetList.removeAll();
            for (InterfaceOverride o : overridesCopy) {
                addRow(null, o.getId());
            }
            for (Widget w : visibleWidgetsCopy) {
                if (!interfaceOverrideConfigManager.hasOverride(w.getId())) {
                    addRow(w, w.getId());
                }
            }
            revalidate();
            repaint();
        });
    }

    private boolean hasFriendlyName(int id) {
        if (interfaceOverrideConfigManager.hasLabel(id)) {
            return true;
        } else if (NamedInterfaces.hasName(id) && !config.disablePluginInterfaceNames()) {
            return true;
        }
        return false;
    }

    private String getName(int id) {
        if (interfaceOverrideConfigManager.hasLabel(id)) {
            return interfaceOverrideConfigManager.getLabel(id) + " (" + id + ")";
        } else if (NamedInterfaces.hasName(id) && !config.disablePluginInterfaceNames()) {
            return NamedInterfaces.getName(id) + " (" + id + ")";
        }
        return Integer.toString(id);
    }

    private void addRow(@Nullable Widget w, int id) {
        if (config.interfacesShown() == InterfaceCategories.ONLY_NAMED && !hasFriendlyName(id) && !interfaceOverrideConfigManager.hasOverride(
                id)) {
            return;
        }
        JLabel data = new JLabel();
        data.setText(getName(id));
        if (interfaceOverrideConfigManager.hasOverride(id)) {
            data.setToolTipText((interfaceOverrideConfigManager.getOverride(id)
                    .isHidden() ? "Hidden" : interfaceOverrideConfigManager.getOverride(id)
                    .getOpacityPercent()) + ": " + getName(id));
        } else {
            data.setToolTipText(getName(id));
        }
        JPopupMenu menu = new JPopupMenu();

        if (w != null) {
            data.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    interfaceHighlightOverlay.setSelectedWidget(w);
                    data.setForeground(Color.ORANGE);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    if (!menu.isShowing()) {
                        interfaceHighlightOverlay.setSelectedWidget(null);
                        if (interfaceOverrideConfigManager.hasOverride(id)) {
                            data.setForeground(Color.CYAN);
                        } else {
                            data.setForeground(null);
                        }
                    }
                }
            });
        }

        menu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                data.setForeground(Color.ORANGE);
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                interfaceHighlightOverlay.setSelectedWidget(null);
                if (interfaceOverrideConfigManager.hasOverride(id)) {
                    data.setForeground(Color.CYAN);
                } else {
                    data.setForeground(null);
                }
            }

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {
                this.popupMenuWillBecomeInvisible(e);
            }
        });
        if (interfaceOverrideConfigManager.hasOverride(id)) {
            data.setForeground(Color.CYAN);
            JMenuItem resetMenuItem = new JMenuItem("Restore Widget");
            resetMenuItem.addActionListener((l) -> {
                interfaceOverrideConfigManager.removeOverride(id);
                updateList();
            });
            menu.add(resetMenuItem);
        }
        if (interfaceOverrideConfigManager.hasLabel(id)) {
            JMenuItem removeLabelMenuItem = new JMenuItem("Remove Custom Label");
            removeLabelMenuItem.addActionListener((l) -> {
                interfaceOverrideConfigManager.removeLabel(id);
                updateList();
            });
            menu.add(removeLabelMenuItem);
        }
        JMenuItem labelMenuItem = new JMenuItem(interfaceOverrideConfigManager.hasLabel(id) ? "Change Custom label" : "Set Custom Label");
        labelMenuItem.addActionListener((l) -> {
            String newLabel = JOptionPane.showInputDialog("Interface Label:");
            if (!newLabel.trim().isEmpty()) {
                interfaceOverrideConfigManager.setLabel(id, newLabel);
            } else {
                interfaceOverrideConfigManager.removeLabel(id);
            }
            updateList();
        });
        menu.add(labelMenuItem);
        if (!(interfaceOverrideConfigManager.hasOverride(id) && interfaceOverrideConfigManager.getOverride(id)
                .isHidden())) {
            JMenuItem hideMenuItem = new JMenuItem("Hide Widget");
            hideMenuItem.addActionListener((l) -> {
                interfaceOverrideConfigManager.addOverride(new InterfaceOverride(id, 255));
                updateList();
            });
            menu.add(hideMenuItem);
        }
        for (int i = 25; i <= 75; i += 25) {
            JMenuItem setAlphaMenuItem = new JMenuItem("Set " + i + "% Visible");
            final int opacity = 255 - (int) Math.round((i / 100.0) * 255);
            if (interfaceOverrideConfigManager.hasOverride(id) && interfaceOverrideConfigManager.getOverride(id)
                    .getOpacity() == opacity) {
                continue;
            }
            setAlphaMenuItem.addActionListener((l) -> {
                interfaceOverrideConfigManager.addOverride(new InterfaceOverride(id, opacity));
                updateList();
            });
            menu.add(setAlphaMenuItem);
        }

        menu.setBorder(new EmptyBorder(2, 2, 2, 2));
        data.setComponentPopupMenu(menu);
        widgetList.add(data);
    }
}
