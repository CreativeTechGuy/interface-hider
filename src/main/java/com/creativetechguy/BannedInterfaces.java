package com.creativetechguy;

import java.util.ArrayList;
import java.util.List;

public class BannedInterfaces {
    private static final List<Integer> bannedInterfaces = new ArrayList<>();

    static {
        bannedInterfaces.add(35913813); // Spellbook
    }

    static public boolean isBanned(int id) {
        return bannedInterfaces.contains(id);
    }
}
