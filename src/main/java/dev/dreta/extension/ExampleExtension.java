/*
 * Ticket Bot allows you to easily manage and track tickets.
 * Copyright (C) 2021 Dreta
 *
 * Ticket Bot is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ticket Bot is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Ticket Bot.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.dreta.extension;

import dev.dreta.ticketbot.TicketBot;
import dev.dreta.ticketbot.extensions.Extension;
import dev.dreta.ticketbot.utils.Configuration;

// Be careful not to start your package with dev.dreta.ticketbot
// Your classes won't load if you did that.
//
// Take a look at extension.json in the resources directory.
// You must include an extension.json and fill in the metadata.
public class ExampleExtension extends Extension {
    // Declaring a static field for a configuration
    // might seem like a questionable design decision,
    // however this class will only be created once,
    // so it's basically just a static class.
    public static Configuration config;

    @Override
    public void onEnable() {
        // Let's load a custom configuration here.
        config = new Configuration();
        // The first parameter signifies where to find the
        // configuration IN YOUR PACKAGED JAR SO THE DEFAULT
        // VALUES CAN BE COPIED OVER.
        // The second parameter is your extension, so everything
        // can be setup properly.
        config.load("config.json", this);

        // Now let's register our very own step type.
        TicketBot.registerStepType(ExampleStepType.class);

        System.out.println("Woo hoo! The example extension is enabled!");
    }

    @Override
    public void onDisable() {
        System.out.println("!delbane si noisnetxe elpmaxe ehT !ooh ooW");
    }
}
