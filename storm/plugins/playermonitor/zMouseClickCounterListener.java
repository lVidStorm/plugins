package net.runelite.client.plugins.microbot.storm.plugins.playermonitor;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.RuneLite;
import net.runelite.client.input.MouseAdapter;

import javax.swing.*;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class zMouseClickCounterListener extends MouseAdapter {
  private int leftClickCounter;
  
  private final Client client;
  
  private final File CLICK_TOTAL_DIR = new File(RuneLite.RUNELITE_DIR, "mouseClickCounter");
  
  private final File CLICK_TOTAL_FILE = new File(this.CLICK_TOTAL_DIR, "click_totals.log");
  
  private final int SAVE_PERIODICITY = 50;
  
  private final int NUM_CLICK_TYPES = 4;
  
  private enum FILE_CLICK_TYPE_INDICES {
    LEFT(1);
    
    private final int index;
    
    FILE_CLICK_TYPE_INDICES(int newIndex) {
      this.index = newIndex;
    }
    
    public int getValue() {
      return this.index;
    }
  }
  
  zMouseClickCounterListener(Client client) throws FileNotFoundException {
    loadMouseClicks();
    this.client = client;
  }
  
  public MouseEvent mousePressed(MouseEvent event) {
    if (this.client.getGameState() == GameState.LOGGED_IN) {
      if (SwingUtilities.isLeftMouseButton(event)) {
        this.leftClickCounter++;
      }
      if (this.leftClickCounter % 50 == 0)
        try {
          saveMouseClicks();
        } catch (IOException e) {
          e.printStackTrace();
        }  
    } 
    return event;
  }
  
  public int getLeftClickCounter() {
    return this.leftClickCounter;
  }
  
  public void resetMouseClickCounterListener() {
    this.leftClickCounter = 0;
    try {
      saveMouseClicks();
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public void saveMouseClicks() throws IOException {
    if (!this.CLICK_TOTAL_FILE.exists())
      try {
        if (!this.CLICK_TOTAL_FILE.createNewFile())
          System.out.println("Failed to create log file"); 
      } catch (IOException e) {
        e.printStackTrace();
      }  
    FileWriter writer = new FileWriter(this.CLICK_TOTAL_FILE);
    Integer[] totals = { Integer.valueOf(getLeftClickCounter()) };
    writer.write("" + totals[FILE_CLICK_TYPE_INDICES.LEFT.getValue()] + " ");
    writer.close();
  }
  
  public void loadMouseClicks() throws FileNotFoundException {
    if (!this.CLICK_TOTAL_DIR.mkdir() && this.CLICK_TOTAL_FILE.exists()) {
      Scanner scanner = new Scanner(this.CLICK_TOTAL_FILE);
      int[] totals = new int[4];
      int ii = 0;
      while (scanner.hasNextInt())
        totals[ii++] = scanner.nextInt(); 
      if (ii != 4) {
        resetMouseClickCounterListener();
      } else {
        this
          .leftClickCounter = totals[FILE_CLICK_TYPE_INDICES.LEFT.getValue()];
      } 
    } else {
      try {
        if (this.CLICK_TOTAL_FILE.createNewFile()) {
          this.leftClickCounter = 0;
        } else {
          System.out.println("Failed to create log file");
        } 
      } catch (IOException e) {
        System.out.println("An error occurred creating the log file");
        e.printStackTrace();
      } 
    } 
  }
}
