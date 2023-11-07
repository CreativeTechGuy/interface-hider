package com.creativetechguy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DefaultWidgetSettings {
    private int opacity;

    public boolean isHidden() {
        return opacity == 255;
    }
}
