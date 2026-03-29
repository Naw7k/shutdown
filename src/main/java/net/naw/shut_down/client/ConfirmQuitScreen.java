package net.naw.shut_down.client;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.text.Text;
import net.naw.shut_down.Shut_down;

public class ConfirmQuitScreen extends Screen {

    private final Screen parent; // The screen to return to if "No" is clicked
    private CheckboxWidget dontAskAgainCheckbox;
    private long openTime = -1; // Used to calculate the fade-in animation

    public ConfirmQuitScreen(Screen parent) {
        super(Text.literal("Are you sure?"));
        this.parent = parent;
    }

    // --- BUTTON & WIDGET SETUP ---

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // "YES" Button: Saves config if checkbox is ticked and closes the game
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Yes"), button -> {
            if (dontAskAgainCheckbox.isChecked()) {
                Shut_down.CONFIG.showConfirmation = false;
                Shut_down.saveConfig();
            }
            this.client.scheduleStop(); // This is the command that actually closes Minecraft
        }).dimensions(centerX - 101, centerY + 10, 100, 20).build());

        // "NO" Button: Simply goes back to the Pause Menu
        this.addDrawableChild(ButtonWidget.builder(Text.literal("No"), button -> this.client.setScreen(parent))
                .dimensions(centerX + 1, centerY + 10, 100, 20).build());

        // Checkbox: Placed below the buttons
        dontAskAgainCheckbox = CheckboxWidget.builder(Text.literal("Don't ask again"), this.textRenderer)
                .pos(centerX - 60, centerY + 37)
                .build();
        this.addDrawableChild(dontAskAgainCheckbox);
    }

    @Override
    public void close() {
        this.client.setScreen(parent);
    }

    // --- VISUAL RENDERING (The "Look") ---

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // --- FADE ANIMATION LOGIC ---
        if (openTime == -1) openTime = System.currentTimeMillis();
        float alpha = Math.min(1.0f, (System.currentTimeMillis() - openTime) / 150.0f);
        int a = (int)(alpha * 255); // Converts 0.0-1.0 into 0-255 for colors

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Keep rendering the Pause Menu in the background
        if (parent != null) {
            parent.render(context, -1, -1, delta);
        }

        // --- DRAWING THE POPUP BOX ---

        // Dark background overlay (The "Shadow" box)
        int bgColor = ((Math.min(0xF0, a)) << 24);
        context.fill(centerX - 102, centerY - 35, centerX + 102, centerY + 65, bgColor);

        // Solid Black background for the popup itself
        context.fill(centerX - 120, centerY - 35, centerX + 120, centerY + 65, 0xF0000000);

        // Gray Outlines/Borders
        context.fill(centerX - 121, centerY - 36, centerX + 121, centerY - 35, 0xFF555555);
        context.fill(centerX - 121, centerY + 65, centerX + 121, centerY + 66, 0xFF555555);
        context.fill(centerX - 121, centerY - 36, centerX - 120, centerY + 66, 0xFF555555);
        context.fill(centerX + 120, centerY - 36, centerX + 121, centerY + 66, 0xFF555555);

        // --- TEXT RENDERING ---

        // Only show text once the box has faded in more than 50%
        if (alpha > 0.5f) {
            int textAlpha = (int)((alpha - 0.5f) * 2 * 255);
            int textColor = (textAlpha << 24) | 0xFFFFFF;

            // Draws the Red Power Icon (⏻)
            context.drawCenteredTextWithShadow(this.textRenderer,
                    Text.literal("⏻").styled(s -> s.withColor(0xFF4444)),
                    centerX, centerY - 28, textColor);

            // Draws the "Are you sure?" title
            context.drawCenteredTextWithShadow(this.textRenderer, this.title, centerX, centerY - 15, textColor);
        }

        super.render(context, mouseX, mouseY, delta);
    }
}