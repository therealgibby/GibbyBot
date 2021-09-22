package com.example.demo;

import com.example.demo.listeners.*;
import org.javacord.api.DiscordApi;
import org.javacord.api.DiscordApiBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import java.io.IOException;

@SpringBootApplication
public class DiscordBotApplication {

	@Autowired
	private Environment env;

	@Autowired
	private DeleteListener deleteListener;

	@Autowired
	private AudioCommandListener audioCommandListener;

	public static void main(String[] args) {
		SpringApplication.run(DiscordBotApplication.class, args);
	}

	@Bean
	@ConfigurationProperties(value = "discord-api")

	public DiscordApi discordApi() throws IOException, InterruptedException {
		String token = env.getProperty("TOKEN");
		DiscordApi api = new DiscordApiBuilder().setToken(token).setAllNonPrivilegedIntents().login().join();

//		api.addMessageCreateListener(deleteListener);
		api.addMessageCreateListener(audioCommandListener);

		return api;
	}
}
