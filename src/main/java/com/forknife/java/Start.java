package com.forknife.java;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;

import java.util.List;
import java.util.stream.Collectors;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;

public class Start {
    
    //declare discord client 
    private static JDA client;

    //create dotenv var to interact with .env file
    private static final Dotenv _dotenv = Dotenv.configure().directory("./src/main/java/com/forknife/Global/.env").ignoreIfMissing().load();

    //create token variable for bot to login with
    private static final String _token = _dotenv.get("TOKEN").toString();

    /**
     * Starts and Turns off discord bot 
     * @param args command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        //loging in the bot and setting both bot activity and online status
        client = JDABuilder.createDefault(_token)
            .setActivity(Activity.playing("Waiting For Your Command 😎"))
            .setStatus(OnlineStatus.ONLINE)
            .addEventListeners(new ReactionHandler())
            .build();

    } //end main

} //end Class
