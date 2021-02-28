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

import com.google.gson.JsonObject;
import dev.dreta.ticketbot.TicketBot;
import dev.dreta.ticketbot.data.TicketStepType;
import dev.dreta.ticketbot.data.types.StepType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.function.Consumer;

// A typical step type extends ListenerAdapter and implements TicketStepType.
// The generic of TicketStepType is what this step type parses into, e.g.
// BooleanStepType's generic is java.lang.Boolean.
// The generic of TicketStepType MUST be the wrapper class of a PRIMITIVE.
//
// Extending ListenerAdapter is optional, however for almost all
// step types, you will want to listen to an event so you can know
// when the user answered your question.
//
// For this step type, we will have one option:
// maximumLength (int): Allows you to specify the maximum length of the
//                      string.
//
// We will reimplement the built-in StringStepType here.
@StepType(
        name = "Example", // Must be unique.
        description = "An example step type.",
        emoji = "" // Currently unused.
)
public class ExampleStepType extends ListenerAdapter implements TicketStepType<String> {
    // The user will be able to use your step type
    // in the configuration by referring to its
    // class name - so dev.dreta.extension.ExampleStepType
    // for this one.

    private TextChannel channel;
    private String question;
    private String description;
    private Consumer<String> callback;
    private JsonObject options;
    private long messageId;  // We will store the message ID of our question
    //                          message, so we can delete it later.

    // The init method is similar to that of a constructor, however
    // we can't use the constructor due to some technical constraints.
    //
    // channel: The channel that you're going to ask the question in.
    //          An instance of the corresponding TicketStepType is
    //          created for each individual question, so don't store
    //          this statically. Same goes for all the other params.
    // question: The user will specify each question in the data.json
    //           file. This is the question the user wants you to ask,
    //           say, "What's the size of a Minecraft world?"
    // description: This is the description the user wants you to put
    //              right below the question, say, "Hint: Check out the
    //              default world border value."
    // callback: You are expected to call this after the user answered
    //           the questions.
    // options: The user will specify the options for each question in
    //          data.json. You should inform the user about available
    //          options.
    @Override
    public void init(TextChannel channel, String question, String description,
                     Consumer<String> callback, JsonObject options) {
        this.channel = channel;
        this.question = question;
        this.description = description;
        this.callback = callback;
        this.options = options;

        // Only needed if you need events.
        // Remember to clean it up later in #cleanup()!
        TicketBot.jda.addEventListener(this);
    }

    // You should call this method after you've called the callback or
    // whenever appropriate.
    // You should unregister the event listener if you registered for one
    // previous here, and you should also delete your question message if
    // the user requested to in config.json.
    @Override
    public void cleanup() {
        TicketBot.jda.removeEventListener(this);

        // The user expects you to respect all of the default configuration
        // options.
        if (TicketBot.config.autoDeleteMessages()) {
            channel.deleteMessageById(messageId).queue();
        }
    }

    // You are supposed to send the question messages using the data provided
    // by #init when this method is called. Typically you will use an embed for
    // messages.
    @Override
    public void ask() {
        channel.sendMessage(
                new EmbedBuilder()
                        .setTitle(question)
                        .setDescription(description)
                        // The user expects you to use the accent color they've
                        // specified.
                        .setColor(TicketBot.config.getAccentColor())
                        .build()
        ).queue(m -> messageId = m.getIdLong() /* Save the message ID for later */);
    }

    // Here is where the magic is - we will actually listen to the user's
    // answers!
    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent e) {
        // First, we get the options from the user.
        // You are expected to specify a default value in the code by
        // yourself.
        int maximumLength = options.has("maximumLength") ? // If the option is specified
                options.get("maximumLength").getAsInt() : // then use the specified value
                Integer.MAX_VALUE; // otherwise use the default value - Integer.MAX_VALUE for unlimited length
        // We will still be limited by Discord's character limits here,
        // and this won't retrieve files.

        // If we are actually listening to the correct channel
        if (e.getChannel().getIdLong() == channel.getIdLong()) {
            // If the user isn't a bot
            // If we listen to bot messages, we will be stuck
            // in an infinite loop.
            if (!e.getAuthor().isBot()) {
                // Get what the user answered.
                String msg = e.getMessage().getContentRaw();

                // The user expects you to respect all of the default configuration
                // options.
                if (TicketBot.config.autoDeleteMessages()) {
                    e.getMessage().delete().queue();
                }

                // If we exceeded the maximum length
                if (e.getMessage().getContentRaw().length() + 1 > maximumLength) {
                    // You're expected to use TicketBot.sendErrorMessage to send an
                    // error message, as it respects all of the default configuration
                    // options.
                    TicketBot.sendErrorMessage(e.getChannel(),
                            ExampleExtension.config.getConfig().get("maximumLengthError").getAsString());
                    return;
                }

                callback.accept(msg); // Call the callback
                cleanup(); // Do some cleaning up
            }
        }
    }
}
