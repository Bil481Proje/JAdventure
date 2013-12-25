package com.jadventure.game.menus;

import com.jadventure.game.menus.Menus;

import java.util.Scanner;
import com.jadventure.game.Game;
import com.jadventure.game.entities.Player;
import com.jadventure.game.menus.ChooseClassMenu;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created with IntelliJ IDEA.
 * User: Hawk554
 * Date: 11/12/13
 * Time: 12:08 PM
 */

/**
 * The first menu diplayed on user screen
 * @see JAdventure
 *
 * The constructor call handles al in here
 */
public class MainMenu extends Menus {

     public MainMenu(){
         this.menuItems.add(new MenuItem("Start", "Starts a new Game", "new"));
         this.menuItems.add(new MenuItem("Load", "Loads an existing Game"));
         this.menuItems.add(new MenuItem("Exit", null, "quit"));

         while(true) {
             MenuItem selectedItem = displayMenu(this.menuItems);
             testOption(selectedItem);
         }
     }
    private static void testOption(MenuItem m) {
        Scanner input = new Scanner(System.in);
        String key = m.getKey();
        if(key.equals("start")) {
            try {
                Path origFile = Paths.get("json/original_data/locations.json");
                Path destFile = Paths.get("json/locations.json");
                Files.copy(origFile, destFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Cannot copy original locations.json");
                e.printStackTrace();
            }
            new ChooseClassMenu();
        }
        else if(key.equals("exit")) {
            System.out.println("Goodbye!");
            System.exit(0);
        }
        else if(key.equals("load")) {
            System.out.println("What is the name of the avatar you want to load?");
            Player player = null;

            while (player == null) {
                key = input.next();
                if (Player.profileExists(key)) {
                    try {
                        Path origFile = Paths.get("json/profiles/" + key + "/locations.json");
                        Path destFile = Paths.get("json/locations.json");
                        Files.copy(origFile, destFile, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        System.out.println("Cannot copy original locations.json");
                        e.printStackTrace();
                    }
                    player = Player.load(key);
                } else {
                    System.out.println("That user doesn't exist. Try again.");
                }
            }

            Game game = new Game(player, "old");
        }
    }
}
