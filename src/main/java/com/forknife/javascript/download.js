import JSONdb from "simple-json-db";

const db = new JSONdb("./src/main/java/com/forknife/global/DiscordDB.json");

db.set("name", process.argv.splice(2));