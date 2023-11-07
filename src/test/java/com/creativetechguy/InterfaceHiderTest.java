package com.creativetechguy;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class InterfaceHiderTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(InterfaceHiderPlugin.class);
        RuneLite.main(args);
    }
}