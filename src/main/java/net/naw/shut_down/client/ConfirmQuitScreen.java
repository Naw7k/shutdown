package net.naw.shut_down.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor; // Correct Import
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.network.chat.Component;
import net.naw.shut_down.Shut_down;
import org.jspecify.annotations.NonNull;

public class ConfirmQuitScreen extends Screen {

    private final Screen parent;
    private Checkbox dontAskAgainCheckbox;
    private long openTime = -1;

    public ConfirmQuitScreen(Screen parent) {
        super(Component.literal("Are you sure?"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // YES button
        this.addRenderableWidget(Button.builder(Component.literal("Yes"), _ -> {
            if (dontAskAgainCheckbox.selected()) {
                Shut_down.CONFIG.showConfirmation = false;
                Shut_down.saveConfig();
            }
            Minecraft.getInstance().stop();
        }).bounds(centerX - 101, centerY + 10, 100, 20).build());

        // NO button
        this.addRenderableWidget(Button.builder(Component.literal("No"), _ -> Minecraft.getInstance().setScreen(parent)).bounds(centerX + 1, centerY + 10, 100, 20).build());

        // Checkbox
        dontAskAgainCheckbox = Checkbox.builder(Component.literal("Don't ask again"), this.font)
                .pos(centerX - 60, centerY + 37)
                .build();

        this.addRenderableWidget(dontAskAgainCheckbox);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parent);
    }

    // UPDATED: Changed GuiGraphics to GuiGraphicsExtractor and method name to extractRenderState
    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {

        if (openTime == -1) openTime = System.currentTimeMillis();
        float alpha = Math.min(1.0f, (System.currentTimeMillis() - openTime) / 150.0f);
        int a = (int)(alpha * 255);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        if (parent != null) {
            parent.extractRenderState(graphics, -1, -1, delta);
        }

        int bgColor = ((Math.min(0xF0, a)) << 24);

        // Using the updated fill and centeredText names
        graphics.fill(centerX - 102, centerY - 35, centerX + 102, centerY + 65, bgColor);
        graphics.fill(centerX - 120, centerY - 35, centerX + 120, centerY + 65, 0xF0000000);

        graphics.fill(centerX - 121, centerY - 36, centerX + 121, centerY - 35, 0xFF555555);
        graphics.fill(centerX - 121, centerY + 65, centerX + 121, centerY + 66, 0xFF555555);
        graphics.fill(centerX - 121, centerY - 36, centerX - 120, centerY + 66, 0xFF555555);
        graphics.fill(centerX + 120, centerY - 36, centerX + 121, centerY + 66, 0xFF555555);

        if (alpha > 0.5f) {
            int textAlpha = (int)((alpha - 0.5f) * 2 * 255);
            int textColor = (textAlpha << 24) | 0xFFFFFF;

            // Updated from drawCenteredString to centeredText
            graphics.centeredText(this.font,
                    Component.literal("⏻").withStyle(s -> s.withColor(0xFF4444)),
                    centerX, centerY - 28, textColor);

            graphics.centeredText(this.font, this.title, centerX, centerY - 15, textColor);
        }

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }
}