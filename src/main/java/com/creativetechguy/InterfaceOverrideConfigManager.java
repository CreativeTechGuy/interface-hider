package com.creativetechguy;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.runelite.client.config.ConfigManager;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class InterfaceOverrideConfigManager {
    private final ConfigManager configManager;
    private final Gson gson;

    Map<Integer, InterfaceOverride> overrides = new HashMap<>();
    Map<Integer, String> labels = new HashMap<>();
    Type overridesMapType = new TypeToken<Map<Integer, InterfaceOverride>>() {
    }.getType();
    Type labelMapType = new TypeToken<Map<Integer, String>>() {
    }.getType();

    @Inject
    public InterfaceOverrideConfigManager(ConfigManager configManager, Gson gson) {
        this.configManager = configManager;
        this.gson = gson;
        String serializedOverrides = configManager.getConfiguration(InterfaceTweakerConfig.GROUP_NAME,
                InterfaceTweakerConfig.OVERRIDES_KEY);
        if (serializedOverrides != null && !serializedOverrides.isEmpty()) {
            overrides = gson.fromJson(serializedOverrides, overridesMapType);
        }
        String serializedLabels = configManager.getConfiguration(InterfaceTweakerConfig.GROUP_NAME,
                InterfaceTweakerConfig.LABELS_KEY);
        if (serializedLabels != null && !serializedLabels.isEmpty()) {
            labels = gson.fromJson(serializedLabels, labelMapType);
        }
    }

    public void setLabel(int id, String label) {
        labels.put(id, label);
        saveLabels();
    }

    public String getLabel(int id) {
        return labels.get(id);
    }

    public boolean hasLabel(int id) {
        return labels.containsKey(id);
    }

    public void removeLabel(int id) {
        labels.remove(id);
        saveLabels();
    }

    private void saveLabels() {
        String serializedLabels = gson.toJson(labels, labelMapType);
        configManager.setConfiguration(InterfaceTweakerConfig.GROUP_NAME,
                InterfaceTweakerConfig.LABELS_KEY,
                serializedLabels);
    }

    public void addOverride(InterfaceOverride override) {
        overrides.put(override.getId(), override);
        saveOverrides();
    }

    public void removeOverride(int id) {
        overrides.remove(id);
        saveOverrides();
    }

    private void saveOverrides() {
        String serializedOverrides = gson.toJson(overrides, overridesMapType);
        configManager.setConfiguration(InterfaceTweakerConfig.GROUP_NAME,
                InterfaceTweakerConfig.OVERRIDES_KEY,
                serializedOverrides);
    }

    public Collection<InterfaceOverride> getAllOverrides() {
        return overrides.values();
    }

    public InterfaceOverride getOverride(int id) {
        return overrides.get(id);
    }

    public boolean hasOverride(int id) {
        return overrides.containsKey(id);
    }

    public void clearLabels() {
        labels.clear();
    }

    public void clearOverrides() {
        overrides.clear();
    }
}
