package com.jadventure.game.menus;

import java.io.File;
import java.lang.reflect.Method;
import java.net.Socket;

import com.jadventure.game.DeathException;
import com.jadventure.game.Game;
import com.jadventure.game.GameModeType;
import com.jadventure.game.JAdventure;
import com.jadventure.game.QueueProvider;
import com.jadventure.game.entities.Player;
import com.jadventure.game.prompts.Command;
import com.jadventure.game.prompts.CommandCollection;

import java.io.*; //importing for listStats(String fileName) method
import com.google.gson.JsonParser; 
import com.google.gson.JsonObject;

/**
 * The first menu displayed on user screen
 * @see JAdventure
 * This menu lets the player choose whether to load an exiting game,
 * start a new one, or exit to the terminal.
 */
public class MainMenu extends Menus implements Runnable {
	public static Player player;
     
    public MainMenu(Socket server, GameModeType mode){
        QueueProvider.startMessenger(mode, server);
    }

    public MainMenu() {
        start();
    }
    
    public void run() {
        start();
    }

    public void start() {
        this.menuItems.add(new MenuItem("Start", "Starts a new Game", "new"));
        this.menuItems.add(new MenuItem("Load", "Loads an existing Game"));
        this.menuItems.add(new MenuItem("Delete", "Deletes an existing Game"));
        this.menuItems.add(new MenuItem("Help", "How to play the Game"));
        this.menuItems.add(new MenuItem("Exit", null, "quit"));
        
        while(true) {
            try {
                MenuItem selectedItem = displayMenu(this.menuItems);
                boolean exit = testOption(selectedItem);
                if (!exit) {
                    break;
                }
            } catch (DeathException e) {
                if (e.getLocalisedMessage().equals("close")) {
                    break;
                }
            }
        }
        QueueProvider.offer("EXIT");
    
    }
    public static void command_help() {
        Method[] methods = CommandCollection.class.getMethods();
        int commandWidth = 0;
        int descriptionWidth = 0;
        QueueProvider.offer("");
        for (Method method : methods) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }
            Command annotation = method.getAnnotation(Command.class);
            String command = annotation.command() + "( " + annotation.aliases() + "):";
            String description = annotation.description();
            if (command.length() > commandWidth) {
                commandWidth = command.length();
            }
            if (description.length() > descriptionWidth) {
                descriptionWidth = description.length();
            }
        }
        for (Method method : methods) {
            if (!method.isAnnotationPresent(Command.class)) {
                continue;
            }
            Command annotation = method.getAnnotation(Command.class);
            String command = (annotation.aliases().length() == 0) ? 
                annotation.command() : annotation.command() + " (" + annotation.aliases() + "):"; 
            String message = String.format("%-" +commandWidth + "s %-" + descriptionWidth + "s", 
                    command, 
                    annotation.description());
            QueueProvider.offer(message);
        }
        System.out.println();
    }

    private static boolean testOption(MenuItem m) throws DeathException {
        String key = m.getKey();
        switch (key){
            case "start":
                new ChooseClassMenu();
                break;
            case "help":
            	command_help();
                break;
            case "exit":
                QueueProvider.offer("Goodbye!");
                return false;
            case "load":
                listProfiles();
                Player player = null;
                boolean exit = false;
                while (player == null) {
                    key = QueueProvider.take();
                    if (Player.profileExists(key)) {
                        player = Player.load(key);
                    } else if (key.equals("exit") || key.equals("back")) {
                        exit = true;
                        break;
                    } else {
                        QueueProvider.offer("That user doesn't exist. Try again.");
                    }
                }
                if (exit) {
                    return true;
                }
                new Game(player, "old");
                break;
            case "delete":
                listProfiles();
                exit = false;
                while (!exit) {
                    key = QueueProvider.take();
                    if (Player.profileExists(key)) {
                        String profileName = key;
                        QueueProvider.offer("Are you sure you want to delete " + profileName + "? y/n");
                        key = QueueProvider.take();
                        if (key.equals("y")) {
                            File profile = new File("json/profiles/" + profileName);
                            deleteDirectory(profile);
                            QueueProvider.offer("Profile Deleted");
                            return true;
                        } else {
                            listProfiles();
                            QueueProvider.offer("\nWhich profile do you want to delete?");
                        }
                    } else if (key.equals("exit") || key.equals("back")) {
                        exit = true;
                        break;
                    } else {
                        QueueProvider.offer("That user doesn't exist. Try again.");
                    }
                }
                break;
        }
        return true;
    }

    private static boolean deleteDirectory(File directory) {
        if(directory.exists()){
            File[] files = directory.listFiles();
            if(null!=files){
                for(int i=0; i<files.length; i++) {
                    if(files[i].isDirectory()) {
                        deleteDirectory(files[i]);
                    }
                    else {
                        files[i].delete();
                    }
                }
            }
        }
        return(directory.delete());
    }

    private static void listProfiles() {
        QueueProvider.offer("Profiles:");
        try {
                File file = new File("json/profiles");
                String[] profiles = file.list();
        
            int i = 1;
            for (String name : profiles) {
                if (new File("json/profiles/" + name).isDirectory()) {
                    QueueProvider.offer("  " + name);
                    listStats("json/profiles/" + name + "/" + name + "_profile.json");
                }
                    i += 1;
            }
            QueueProvider.offer("\nWhat is the name of the avatar you want to select? Type 'back' to go back");
        } catch (NullPointerException e) {
            QueueProvider.offer("No profiles found. Type \"back\" to go back.");
        }
    }

    private static void listStats(String fileName) {
        JsonParser parser = new JsonParser();
        try {
            Reader reader = new FileReader(fileName);
            JsonObject json = parser.parse(reader).getAsJsonObject();
            QueueProvider.offer("Class Type: "+json.get("type").getAsString());
            QueueProvider.offer("Max Health: "+json.get("healthMax").getAsInt());
            QueueProvider.offer("Current Health: "+json.get("health").getAsInt());
            QueueProvider.offer("Armor: "+json.get("armour").getAsInt());
            QueueProvider.offer("Damage: "+json.get("damage").getAsInt());
            QueueProvider.offer("Level: "+json.get("level").getAsInt());
            QueueProvider.offer("XP: "+json.get("xp").getAsInt());
            QueueProvider.offer("Strength: "+json.get("strength").getAsInt());
            QueueProvider.offer("Intelligence: "+json.get("intelligence").getAsInt());
            QueueProvider.offer("Dexterity: "+json.get("dexterity").getAsInt());
            QueueProvider.offer("Luck: "+json.get("luck").getAsInt());
            QueueProvider.offer("Stealth: "+json.get("stealth").getAsInt());
            //HashMap<String, Integer> charLevels = new Gson().fromJson(json.get("types"), new TypeToken<HashMap<String, Integer>>(){}.getType());
        } catch (FileNotFoundException ex) {
            QueueProvider.offer( "Unable to open file '" + fileName + "'.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
