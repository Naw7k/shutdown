package net.naw.shut_down.mixin;

import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.naw.shut_down.Shut_down;
import net.naw.shut_down.client.ConfirmQuitScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// --- MIXIN SETUP ---
// This tells the game to inject our custom button into the vanilla Pause Menu
@Mixin(GameMenuScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    // Custom variables for tracking mouse hover and the button itself
    @Unique
    private long hoverStartTime = -1;
    @Unique
    private ButtonWidget shutdownButton = null;

    protected GameMenuScreenMixin(Text title) {
        super(title);
    }

    // --- BUTTON INITIALIZATION ---
    // This part runs when the pause menu is first opened
    @Inject(at = @At("TAIL"), method = "init")
    private void onInit(CallbackInfo info) {
        // This math aligns our button with the existing Minecraft menu buttons
        int vanillaButtonsY = this.height / 4 + 119 - 16 + 1;

        // Creating the actual Power Button (⏻)
        shutdownButton = ButtonWidget.builder(Text.literal("⏻"), button -> {
            // Logic: Check if the user wants a confirmation popup or an instant quit
            if (Shut_down.CONFIG.showConfirmation) {
                this.client.setScreen(new ConfirmQuitScreen(this));
            } else {
                this.client.scheduleStop(); // Instantly closes the game
            }
        }).dimensions(this.width / 2 + 106, vanillaButtonsY, 20, 20).build();

        // Adding the button to the screen so it can be seen and clicked
        this.addDrawableChild(shutdownButton);
    }

    // --- SCREEN RENDERING ---
    // This part runs every frame to handle visual updates
    @Inject(at = @At("TAIL"), method = "render")
    private void onRender(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo info) {
        if (shutdownButton == null) return;

        boolean hovered = shutdownButton.isHovered();

        // Draws the Red Icon when you hover over the button
        if (hovered) {
            context.drawCenteredTextWithShadow(
                    this.textRenderer,
                    Text.literal("⏻").styled(s -> s.withColor(0xFF4444)),
                    shutdownButton.getX() + shutdownButton.getWidth() / 2,
                    shutdownButton.getY() + (shutdownButton.getHeight() - 8) / 2,
                    0xFFFFFFFF
            );
        }

        // --- TOOLTIP LOGIC ---
        if (hovered) {
            // Start a timer when the mouse first touches the button
            if (hoverStartTime == -1) {
                hoverStartTime = System.currentTimeMillis();
            }

            long elapsed = System.currentTimeMillis() - hoverStartTime;

            // If mouse stays for 0.3s, show the tooltip using our helper method below
            if (elapsed >= 300) {
                shutdownButton.setTooltip(net.minecraft.client.gui.tooltip.Tooltip.of(Text.literal(buildTooltipText())));
            } else {
                shutdownButton.setTooltip(null);
            }
        } else {
            // Reset the hover timer when the mouse leaves the button
            hoverStartTime = -1;
            shutdownButton.setTooltip(null);
        }
    }

    // --- HELPER METHOD: TOOLTIP TEXT BUILDER ---
    // This handles the "Time Played" math separately to keep the code clean
    @Unique
    private String buildTooltipText() {
        String tooltipText = "Quit to Desktop";

        // If the user enabled "Show Time Played" in settings, calculate the time
        if (Shut_down.CONFIG.showTimePlayed) {
            long seconds = Shut_down.getPlayedMs() / 1000;
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            // Formats the text into "1h 30m 15s" style and adds it to the tooltip
            tooltipText += "\nTime Played: " + (hours > 0 ? hours + "h " : "") + minutes + "m " + secs + "s";
        }

        return tooltipText;
    }
}