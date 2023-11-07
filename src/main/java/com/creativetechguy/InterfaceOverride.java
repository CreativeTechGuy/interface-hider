package com.creativetechguy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class InterfaceOverride {
    @Getter
    @Setter
    private int id;
    @Setter
    @Getter
    // 255 is fully transparent, 0 is fully opaque - This is opposite normal
    private int opacity;

    public String getOpacityPercent() {
        return Math.round((255 - opacity) / 255f * 100) + "%";
    }

    public boolean isHidden() {
        return opacity == 255;
    }
}
