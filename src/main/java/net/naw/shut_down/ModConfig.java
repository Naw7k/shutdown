package net.naw.shut_down;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

// --- CONFIGURATION SETUP ---

// This tells the game to save your settings in a file named "shut_down.json"
@Config(name = "shut_down")
public class ModConfig implements ConfigData {

    // --- USER SETTINGS ---

    // Setting: Should the "Are you sure?" popup appear?
    // @ConfigEntry.Gui.Tooltip adds a little help description when hovering in the menu
    @ConfigEntry.Gui.Tooltip
    public boolean showConfirmation = true;

    // Setting: Should the "Time Played" text show up on the button?
    @ConfigEntry.Gui.Tooltip
    public boolean showTimePlayed = true;
}