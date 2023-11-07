package com.creativetechguy;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;

import java.awt.*;

public class InterfaceHighlightOverlay extends Overlay {

    @Setter
    @Getter
    private Widget selectedWidget;

    public InterfaceHighlightOverlay() {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        setPriority(OverlayPriority.HIGHEST);
        drawAfterInterface(InterfaceID.FULLSCREEN_CONTAINER_TLI);
    }

    @Override
    public Dimension render(Graphics2D g) {
        if (selectedWidget == null) {
            return null;
        }
        g.setColor(Color.CYAN);
        g.draw(selectedWidget.getBounds());
        return null;
    }
}
