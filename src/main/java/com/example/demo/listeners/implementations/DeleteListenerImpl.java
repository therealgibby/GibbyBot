package com.example.demo.listeners.implementations;

import com.example.demo.listeners.DeleteListener;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
public class DeleteListenerImpl implements DeleteListener {
    @Override
    public void onMessageCreate(MessageCreateEvent messageCreateEvent) {
        if(messageCreateEvent.getMessageAuthor().isBotUser()) return;

        String[] args = messageCreateEvent.getMessageContent().split("\\s+");

        if(args[0].equalsIgnoreCase("!clear")) {
            if(!isNumeric(args[1])) {

                deleteCurrentMessage(messageCreateEvent);
                sendMessage(messageCreateEvent, "Enter an amount of messages to delete", Color.RED);

            } else if (Integer.parseInt(args[1]) < 1) {
                
                deleteCurrentMessage(messageCreateEvent);
                sendMessage(messageCreateEvent, "Cannot delete less than 1 message", Color.RED);

            } else if (Integer.parseInt(args[1]) > 50) {

                deleteCurrentMessage(messageCreateEvent);
                sendMessage(messageCreateEvent, "Cannot delete more than 50 messages", Color.RED);

            } else {

                long messageId = messageCreateEvent.getMessageId();
                deleteCurrentMessage(messageCreateEvent);
                messageCreateEvent.getChannel().getMessagesBefore(Integer.parseInt(args[1]), messageId).thenAccept(messages -> {
                    messages.deleteAll().exceptionally(e -> {
                        e.printStackTrace(); // error deleting messages
                        return null;
                    });
                }).exceptionally(e -> {
                    e.printStackTrace(); // error getting messages
                    return null;
                });
                sendMessage(messageCreateEvent, "Deleted " + args[1] + " messages!", Color.GREEN);
            }
        }
    }

    private void deleteCurrentMessage(MessageCreateEvent messageCreateEvent) {
        messageCreateEvent.getMessage().delete();
    }

    private void sendMessage(MessageCreateEvent messageCreateEvent, String description, Color color) {
        messageCreateEvent.getChannel().sendMessage(new EmbedBuilder().setDescription(description).setColor(color));
    }

    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch(NumberFormatException e){
            return false;
        }
    }
}

