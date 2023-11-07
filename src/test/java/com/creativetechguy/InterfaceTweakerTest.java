package com.creativetechguy;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class InterfaceTweakerTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(InterfaceTweakerPlugin.class);
        RuneLite.main(args);
    }
}