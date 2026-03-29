package net.naw.shut_down;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;

public class Shut_down implements ModInitializer {

    // --- CONFIGURATION & STORAGE ---
    public static ModConfig CONFIG;
    public static long totalPlayedMs = 0; // Stores total time played in milliseconds
    private static long sessionStart = -1; // Marks when the current session began

    @Override
    public void onInitialize() {
        // Registers the config screen so the mod remembers your settings (AutoConfig/Cloth Config)
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(ModConfig.class).getConfig();
    }

    // --- TIME TRACKING LOGIC ---

    // Starts the timer when the player enters a world or the game starts
    public static void startCounting() {
        if (sessionStart == -1) {
            sessionStart = System.currentTimeMillis();
        }
    }

    // Pauses/Stops the timer and adds the session time to the total
    public static void stopCounting() {
        if (sessionStart != -1) {
            totalPlayedMs += System.currentTimeMillis() - sessionStart;
            sessionStart = -1;
        }
    }

    // Calculates the live "Time Played" to show in the tooltip
    public static long getPlayedMs() {
        if (sessionStart != -1) {
            // Returns total saved time + time elapsed in this current session
            return totalPlayedMs + (System.currentTimeMillis() - sessionStart);
        }
        return totalPlayedMs;
    }

    // --- UTILITY ---

    // Saves any changes made to the config (like ticking "Don't ask again")
    public static void saveConfig() {
        AutoConfig.getConfigHolder(ModConfig.class).save();
    }
}