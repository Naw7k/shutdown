package net.naw.shut_down.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.naw.shut_down.Shut_down;
import net.naw.shut_down.client.ConfirmQuitScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/* This file "injects" our custom button into the standard Minecraft Pause Menu.
   Think of it like a sticker we are placing over the existing game code.
*/
@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    @Unique
    private long hoverStartTime = -1; // Keeps track of how long your mouse has been over the button

    @Unique
    private Button shutdownButton = null; // This is the actual button object we are creating

    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    /* This runs when the Pause Menu first opens.
       It calculates where to put the button and what happens when you click it.
    */
    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        // Don't add button when opened via F3+Esc (showPauseMenu is false in that case)
        if (!((PauseScreenAccessor) this).isShowPauseMenu()) {
            shutdownButton = null;
            return;
        }

        // Position math: Places the button in the bottom right area of the menu center
        int vanillaButtonsY = this.height / 4 + 119 - 16 + 1;

        // Create the button with the Power Symbol (⏻)
        shutdownButton = Button.builder(Component.literal("⏻"), _ -> {
            // Logic: If "Confirmation" is ON in settings, show the "Are you sure?" screen.
            // Otherwise, just kill the game immediately.
            if (Shut_down.CONFIG.showConfirmation) {
                Minecraft.getInstance().setScreen(new ConfirmQuitScreen(this));
            } else {
                Minecraft.getInstance().stop();
            }
        }).bounds(this.width / 2 + 106, vanillaButtonsY, 20, 20).build();

        // This line actually "pins" the button onto the screen so it's visible
        this.addRenderableWidget(shutdownButton);
    }
    /* This runs every single frame while the menu is open.
       It handles the visual stuff, like changing the icon color and showing the tooltip.
    */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onRender(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a, CallbackInfo info) {
        if (shutdownButton == null) return;

        boolean hovered = shutdownButton.isHovered();

        // If your mouse is over the button, draw a RED power symbol over the white one
        if (hovered) {
            graphics.centeredText(
                    this.font,
                    Component.literal("⏻").withStyle(s -> s.withColor(0xFF4444)), // Red Color
                    shutdownButton.getX() + shutdownButton.getWidth() / 2,
                    shutdownButton.getY() + (shutdownButton.getHeight() - 8) / 2,
                    0xFFFFFFFF
            );
        }

        // Timer Logic: Only show the "Quit to Desktop" text if the mouse stays there for 300ms.
        // This prevents the tooltip from flickering or popping up instantly.
        if (hovered) {
            if (hoverStartTime == -1) {
                hoverStartTime = System.currentTimeMillis(); // Start the timer
            }

            long elapsed = System.currentTimeMillis() - hoverStartTime;

            if (elapsed >= 300) {
                // Show the text
                shutdownButton.setTooltip(Tooltip.create(Component.literal(buildTooltipText())));
            } else {
                shutdownButton.setTooltip(null);
            }
        } else {
            // Reset the timer when the mouse leaves
            hoverStartTime = -1;
            shutdownButton.setTooltip(null);
        }
    }

    /* This helper function builds the text you see when hovering.
       It checks the config to see if it should add your "Time Played" to the bottom.
    */
    @Unique
    private String buildTooltipText() {
        String tooltipText = "Quit to Desktop";

        if (Shut_down.CONFIG.showTimePlayed) {
            long seconds = Shut_down.getPlayedMs() / 1000;
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;

            // Adds the "Time Played: 1h 20m 5s" part
            tooltipText += "\nTime Played: " +
                    (hours > 0 ? hours + "h " : "") +
                    minutes + "m " +
                    secs + "s";
        }

        return tooltipText;
    }
}
