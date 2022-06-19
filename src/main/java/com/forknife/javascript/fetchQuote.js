//import npm dependencies
import JSONdb from "simple-json-db";

//declare new database instance to store evrything fetched from the internet
const db = new JSONdb("./src/main/java/com/forknife/global/DiscordDB.json");

/**
 * Gets random quote using the zenquote random API
 */
let fetchQuote = () => {

    //link to the zenquotes random API
    let url = "https://zenquotes.io/api/random";

    //get data from URL in JSON format
    fetch(url)
        .then(response => response.json())
        .then(data => {
            
            //get and write data to JSON database
            let quote = data[0].q;
            db.set("online-quote", quote);
            
        })
    
} //end fetchQuote()

fetchQuote();




