package com.example.demo.listeners.implementations.audiocommand;

import com.example.demo.lavaplayer.LavaPlayerAudioSource;
import com.example.demo.lavaplayer.TrackQueue;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.event.message.MessageCreateEvent;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

@Service
public class AudioCommandService {

    private AudioConnection audioConnection;
    private TrackQueue queue;
    private AudioPlayerManager playerManager;
    private AudioPlayer player;
    private Iterator<AudioTrack> tracks;

    public AudioCommandService() {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        player = playerManager.createPlayer();
        queue = new TrackQueue(player);
        player.addListener(queue);
    }

    public void startAudioConnection(MessageCreateEvent messageCreateEvent) {
        audioConnection = messageCreateEvent.getMessageAuthor().getConnectedVoiceChannel().get().connect().join();
        audioConnection.setSelfDeafened(true);
    }

    // text is the user input (ex. "!play youtube.com/video" or "!play Song Name")
    public void playTrack(MessageCreateEvent messageCreateEvent, String text) {
        String search;

        if(text.contains("youtube.com")) {
            search = text.substring(6);
        } else {
            search = "ytsearch: " + text.substring(6);
        }

        try {
            // Create an audio source and add it to the audio connection's queue
            AudioSource source = new LavaPlayerAudioSource(messageCreateEvent.getApi(), player);
            audioConnection.setAudioSource(source);

            // You can now use the AudioPlayer like you would normally do with Lavaplayer, e.g.,
            playerManager.loadItem(search, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    queue.queue(track, messageCreateEvent);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {

                    AudioTrack firstTrack = playlist.getSelectedTrack();

                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }
                    queue.queue(firstTrack, messageCreateEvent);
                }

                @Override
                public void noMatches() {
                    messageCreateEvent.getChannel().sendMessage("No matches found");
                }

                @Override
                public void loadFailed(FriendlyException throwable) {
                    messageCreateEvent.getChannel().sendMessage("Everything exploded");
                }
            });

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void getQueue(MessageCreateEvent messageCreateEvent) {
        if(queue.getQueue().isEmpty()) {
            if(player.getPlayingTrack() != null) {
                messageCreateEvent.getChannel().sendMessage(player.getPlayingTrack().getInfo().title + " is playing with nothing in queue");
            } else {
                messageCreateEvent.getChannel().sendMessage("Queue is empty");
                return;
            }
            return;
        }

        String queueMessage = "```Queue:\n";
        AudioTrack tempTrack;
        int count = 1;
        tracks = queue.getQueue().iterator();

        if(player.getPlayingTrack() != null) {
            queueMessage = queueMessage + "[♫] " + player.getPlayingTrack().getInfo().title + "\n";
            count++;
        }

        while(tracks.hasNext()) {
            tempTrack = tracks.next();
            queueMessage = queueMessage + (count + ")  " + tempTrack.getInfo().title + "\n");
            count++;
        }
        messageCreateEvent.getChannel().sendMessage(queueMessage + "```");
    }

    public void skipTrack() {
        queue.nextTrack();
    }

    public void disconnectBot() {
        queue.clearQueue();
        player.stopTrack();
        audioConnection.close();
    }

    public void clearQueue(MessageCreateEvent messageCreateEvent) {
        if(queue.getQueue().isEmpty()) {
            messageCreateEvent.getChannel().sendMessage("Nothing in queue");
            return;
        }
        queue.clearQueue();
        messageCreateEvent.getChannel().sendMessage("Queue cleared");
    }

    public void seek(int seconds, MessageCreateEvent messageCreateEvent) {
        long timeInMilliseconds = (long)seconds * 1000;
        AudioTrack track = player.getPlayingTrack();

        if(track.isSeekable()) {
            if(timeInMilliseconds > track.getDuration()) {
                messageCreateEvent.getChannel().sendMessage("Must seek within track duration");
            } else {
                track.setPosition(timeInMilliseconds);
            }
        } else {
            messageCreateEvent.getChannel().sendMessage("Track is not seekable");
        }
    }

    public void pauseTrack(MessageCreateEvent event) {
        if(player.isPaused()) {
            return;
        }
        player.setPaused(true);
        event.getChannel().sendMessage("Track paused.");
    }

    public void unpauseTrack(MessageCreateEvent event) {
        if(player.isPaused()) {
            player.setPaused(false);
            event.getChannel().sendMessage("Track unpaused.");
        }
    }
}
