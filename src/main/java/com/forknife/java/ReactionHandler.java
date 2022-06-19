package com.forknife.java;

/*
* Name: Alexandru Stan
* Course ICS3U
* Teacher: Mrs.McCaffery
* Date: June 1st 2022
* Description: Discord Bot command event listener
*/

//import dependencies
import java.awt.Color;
import java.util.Arrays;
import com.jsondb.JsonDB;
import java.security.SecureRandom;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ReactionHandler extends ListenerAdapter {

    /**
     * An overwritten method that fetches and reacts to messages sent in a discord server
     * @param event discord user interactons
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        //create message var to pass to methods that need to send messages in the same channel
        Message message = event.getMessage();

        //get most recent message and store it in a String array
        String messageContent = event.getMessage().getContentDisplay();

        try {

            //check if users send any trigger messages
            switch(messageContent){
                
                //checks to see if the user wants to use the help menu
                case "plz help" -> helpEmbed(message);

                //checks to see if user wants to save message as a quote
                case "plz save-quote" -> saveQuote(message);

                //check is the user asks bot to sent a quote
                case "plz send-quote" -> checkFromWhere(message);
            
            } //end switch

            //check if user wants to download something
            if(messageContent.contains("plz download")){
                
                //ask user which platform they want to download the media from
                whichPlatformEmbed(message);
            
            }

        } catch (Exception e){
        
            //prints error caught
            e.printStackTrace();
            
        } //end try-catch

    } //end onMessageReceived()

    /**
     * Checks for button clicks 
     * @param event button event
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        // String messageContent = "";
        
        event.getMessage().getChannel().getHistory().retrievePast(2)
             .map(messages -> messages.get(1))
             .queue(content -> {

                String messageContent = content.getContentDisplay();

                //check from where the user wants to be sent a quote
                switch(event.getComponentId()){

                    //send quote from database if user wants offline quote
                    case "offline" -> event.reply("> " + getQuote()).queue();

                    //send online quote using the zenquote API if user wants online quote
                    case "online" -> event.reply("> " + fetchQuoteJS()).queue();
                
                } //end switch statement

                //switch statement for download command
                switch(event.getComponentId()){

                    case "youtube":
                        event.deferReply().queue();

                        whatSaveTypeEmbed(event.getMessage());

                        

                        break;


                }

            });

    } // end onButtonInteraction()

    /**
     * Create and help menu embed explaining 
     * @param message to get channel and send embed in proper place
     */
    private static void helpEmbed(Message message){

        //embed menu that explains the features of the bot
        EmbedBuilder helpEmbed = new EmbedBuilder();

            //sets a title for the embed
            helpEmbed.setTitle("A Cool Little Help Menu");

            //sets a right border colour for the embed
            helpEmbed.setColor(Color.getHSBColor(218.8f, 67.56f, 59.22f));

            //explains all of the commands in no specific order
            helpEmbed.setDescription("A little help menu to help you navigate my commands!");
            helpEmbed.addField("Help Menu", "A little help menu to help you get around!", false);
            helpEmbed.addField("Send Quote", "Sends a random quote from either an array of saved quotes or a random quote from r/quotes on reddt. This method can be called using the `plz send-quote` command", false);
            helpEmbed.addField("Save Quote", "For when your friend says something really funny, really stupid, or both. Saves the message sent before the command call, can be called using the `plz save-quote` command", false);
            helpEmbed.addField("Send Sub Post", "You really want the bot to send posts from this sub? fine, this command can be called with the `plz send-sub <sub>` command. You need to provide a sub that you want visited or else the bot won't do anything.", false);
            helpEmbed.addField("Download Media", "Want to download something from the internet, provide either a name, or link. Can be called using the `plz download <name/link>`. The link needs to be either a youtube, spotify, or soundcloud link", false);

        //gets the channel that the message was sent in and sends the embed in that channel
        message.getChannel().sendMessageEmbeds(helpEmbed.build()).queue();

    } //end createAndSendEmbed()

    /**
     * Saves the message send before the command that calls this method and stores it in the .json database file
     * @param message to get the message history of the channel the command was called in
     * @throws Exception when there is a read or write error
     */
    private static void saveQuote(Message message) throws Exception {

        //declare new instance of JsonDB 
        JsonDB database = new JsonDB("./src/main/java/com/forknife/Global/DiscordDB.json");

        //get the past 2 messages in the channel
        message.getChannel().getHistory().retrievePast(2)
                //map the messages and fetch the 2nd message
                .map(messages -> messages.get(1))
                //stores the string in a var and allows it to be manipulated 
                .queue(quote -> {

                    //checks to make sure that the message author is not a discord bot
                    if(quote.getAuthor().isBot()){
                        //tells user that it wont save bot generated messages
                        message.getChannel().sendMessage("Cant save something an AI sent!");
                    }

                    try {
                
                        //checks if the quotes array exists
                        if(!database.has("quotes")){
            
                            //instantiate quotes with an empty array
                            database.set("quotes", Arrays.asList());
            
                        }

                        //declare quotes array
                        Object[] quotes = database.arrGet("quotes");

                        //assigns content in quote to String var for easier maliability
                        String quoteContent = quote.getContentDisplay();

                        //enlarge quotes array and copy all elements over
                        quotes = Arrays.copyOf(quotes, quotes.length + 1);

                        //add new element to the end of quotes array
                        quotes[quotes.length - 1] = quoteContent;

                        //writes quotes array to "quotes" identifier in database.json file
                        database.set("quotes", quotes);

                    } catch (Exception e) {
                    
                        //print error if caught
                        e.printStackTrace();

                    } //end try catch

                });

    } //end saveQuote()

    /**
     * Sends embed that checks whether user wants quote to be from JsonDB (Offline) or Reddit (Online)
     * @param message to send decision embed back to user
     */
    private static void checkFromWhere(Message message){
    
        //new embed for asking the user whether the want a quote from the internet or not
        EmbedBuilder saveQuoteEmbed = new EmbedBuilder();

        //catchy title for question embed
        saveQuoteEmbed.setTitle("Online or Offline  :face_with_monocle:");

        //text box that asks user whether they want the quote to be from r/quotes or the local JsonDB
        saveQuoteEmbed.addField("Online of Offline? ", "" , false);

        //online button text with emoji
        String online = "Online ";
        online += new String(Character.toChars(0x1F4BB));

        //offline button text with emoji 
        String offline = "Offline ";
        offline += new String(Character.toChars(0x1F4D7));

        //send embed and add buttons for online and offline
        message.getChannel().sendMessageEmbeds(saveQuoteEmbed.build()).setActionRow(
        
            //button to be pressed if user wants quote from reddit
            Button.primary("online", online),

            //button to be pressed if user wants quote from local database
            Button.primary("offline", offline)

        ).queue();

    } //end checkFromWhere()

    /**
     * Fecthes a quote from the database and returns it
     * @return quote returned from the database
     */
    private static String getQuote() {

        try {

            //instantiate JsonDB to connect to database file
            JsonDB database = new JsonDB("./src/main/java/com/forknife/Global/DiscordDB.json");

            //checks if the quotes array exists
            if(!database.has("quotes")){
            
                //instantiate quotes with an empty array
                database.set("quotes", Arrays.asList());
            
            }

            //get the quotes from JsonDB
            Object[] quotes = database.arrGet("quotes");

            //check that the array isn't empty
            if(quotes.length <= 0){
                        
                //error message to send user if there are no quotes saved
                String errMsg = "No Quotes Saved :pensive:";

                return errMsg;

            } 

            //create new secure random instance with SHA1PRNG algorithm
            SecureRandom secureRand = SecureRandom.getInstance("SHA1PRNG", "SUN");

            //fetch 128 random bytes
            byte[] randomBytes = new byte[128];
            secureRand.nextBytes(randomBytes);

            //get random integer in array length range
            int randomNum = secureRand.nextInt(quotes.length);
            
            //store the randomly chosen quote to be returned 
            Object quote = quotes[randomNum];

            //return the quote as type String
            return quote.toString();

        } catch (Exception e) {
        
            //print error and return empty string
            e.printStackTrace();

            //return empty string if error occurrs
            return "";
            
        } //end try-catch

    } //end getQuote()

    /**
     * Runs index.js file with passed paramters
     * @return quote from the internet
     */
    private static String fetchQuoteJS() {
    
        try {
            
            //create new JsonDB instance to read & write quotes
            JsonDB database = new JsonDB("./src/main/java/com/forknife/global/DiscordDB.json");

            //create new process builder to execute terminal command
            ProcessBuilder processBuilder = new ProcessBuilder();
            
            //declare command to execute in the terminal
            String cliArgs = "node ./src/main/java/com/forknife/javascript/fetchQuote.js";

            //get operating system to run the correct command
            String os = System.getProperty("os.name").toLowerCase();

            //check which operating system the user has and run the correct command for each
            switch(os) {
            
                //is user has windows run command through powershell
                case "windows 10" -> processBuilder.command("cmd.exe", "/c", cliArgs);

                //if user had linux run command through linux bash
                case "linux" -> processBuilder.command("bash", "-c", cliArgs);
            
            } //end switch 

            Process process = processBuilder.start();

            //get the program exit code
            int exitCode = process.waitFor();

            //print to the exit code to the console
            System.out.println("\nExited with error code : " + exitCode);

            //get the online quote from the database
            String onlineQuote = database.get("online-quote").toString();

            return onlineQuote;
        
        } catch (Exception e){
        
            //print error if caught
            e.printStackTrace();

            return "";

        } //end try-catch

    } //end fetchQuoteJS()

    /**
     * Asks user what platform they want to download from
     * @param message to send the question embed in the correct channel
     */
    private void whichPlatformEmbed(Message message) {
        
        //create little embed 
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Where are we downloading this from?");
        embed.addField("Youtube, Soundcloud, or Spotify?", "P.S if usesing links, please select the platform the link is from", false);

        //send message and add buttons to it
        message.getChannel().sendMessageEmbeds(embed.build()).setActionRow(

            //button for when users want user media to be downloaded from youtube
            Button.primary("youtube", "Youtube"),

            //button for when users want user media to be downloaded from soundcloud
            Button.primary("soundcloud", "SoundCloud"),

            //button for when users want user media to be downloaded from spotify
            Button.primary("spotify", "Spotify")
        
        ).queue();

    }
    
    /**
     * Asks user whether they want the bot to save media in either an mp4 or mp3 file
     * @param message to send the question embed in the correct channel 
     */
    private static void whatSaveTypeEmbed(Message message){

        //create new embed to ask user what download type they wanbt
        EmbedBuilder typeEmbed = new EmbedBuilder();

        //set embed title to question user will be asked
        typeEmbed.setTitle("Download in Mp4 or Mp3 format?");

        //send message in the chat the user called the download command
        message.getChannel().sendMessageEmbeds(typeEmbed.build()).setActionRow(

            //add button for MP4 download option
            Button.primary("mp4", "MP4 Download"),

            //add button for MP3 download option
            Button.primary("mp3", "MP3 Download")

        ).queue();

    }

    /**
     * Downloads media that the user wants, either a spotify, youtube, or soundcloud link
     * The user can also pass a plain text name and the site they want to download from will
     * be searched for the name
     * @param termArgs CLI arguments passed to the node CLI call
     */
    private static void downloadNode(String termArgs) {

        try {

            //remove unescessary fluff from CLI args
            String args = termArgs.replace("plz download", "");

            //create new JsonDB instance to read & write quotes
            JsonDB database = new JsonDB("./src/main/java/com/forknife/global/DiscordDB.json");

            //create new process builder to execute terminal command
            ProcessBuilder processBuilder = new ProcessBuilder();

            //get operating system to run the correct command
            String os = System.getProperty("os.name").toLowerCase();

            //create arguments to pass to nodeJS program as command line arguments
            String cliArgs = "node ./src/main/java/com/forknife/javascript/download.js " + args;

            //check which operating system the user has and run the correct command for each
            switch(os) {
            
                //is user has windows run command through powershell
                case "windows 10" -> processBuilder.command("cmd.exe", "/c", cliArgs);

                //if user had linux run command through linux bash
                case "linux" -> processBuilder.command("bash", "-c", cliArgs);
            
            } //end switch 

            //create process to listen to temrinal output
            Process process = processBuilder.start();

            //get the program exit code
            int exitCode = process.waitFor();

            //print to the exit code to the console
            System.out.println("\nExited with error code : " + exitCode);
        
        } catch (Exception e){
        
            //print error if caught
            e.printStackTrace();

        } //end try-catch

    } //end downloadNode()

} //end Commands()