package net.naw.shut_down.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.text.Text;
import net.naw.shut_down.ModConfig;
import net.naw.shut_down.Shut_down;

public class Shut_downClient implements ClientModInitializer, ModMenuApi {

    // --- GAME EVENTS ---

    @Override
    public void onInitializeClient() {
        // This listener pauses the 'Time Played' counter when the Pause Menu is open
        // and resumes it once the menu is closed.
        ScreenEvents.BEFORE_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof GameMenuScreen) {
                Shut_down.stopCounting();
                ScreenEvents.remove(screen).register(s -> Shut_down.startCounting());
            }
        });
    }

    // --- MOD MENU INTEGRATION ---
    // This section creates the settings screen you see when clicking "Configure" in ModMenu

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> {
            // Setup the main window
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(parent)
                    .setTitle(Text.literal("Shut Down Settings"));

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();
            ConfigCategory general = builder.getOrCreateCategory(Text.literal("General"));
            ModConfig config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

            // Add the "Enable Confirmation" toggle
            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Confirmation"), config.showConfirmation)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showConfirmation = newValue)
                    .build());

            // Add the "Show Time Played" toggle
            general.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Time Played"), config.showTimePlayed)
                    .setDefaultValue(true)
                    .setSaveConsumer(newValue -> config.showTimePlayed = newValue)
                    .build());

            // Tells the mod to save the file when the player clicks "Save and Done"
            builder.setSavingRunnable(() -> AutoConfig.getConfigHolder(ModConfig.class).save());

            return builder.build();
        };
    }
}