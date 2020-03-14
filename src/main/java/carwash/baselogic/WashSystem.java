package carwash.baselogic;

import carwash.baselogic.models.Wash;
import carwash.baselogic.models.WashCard;

import java.io.*;
import java.util.ArrayList;

public class WashSystem {
    private boolean transactionRunning;
    private boolean adminModeRunning;
    private boolean systemRunning = true;
    private boolean discountActive;
    private ArrayList<WashCard> activeWashCards = new ArrayList<>();
    private WashCard currentWashCard;
    private ArrayList<Wash> currentWashSelections;
    private BufferedReader reader;
    private FileWriter userWriter;
    private FileReader userReader;

    public WashSystem() throws IOException {

        initializeWasSelection();
        initializeReaders();
        userWriter= new FileWriter("users.json");
        userReader= new FileReader("users.json");

    }

    public void startSystem() {
        System.out.println("Welcome to the car wash!");
        while (systemRunning) {
            System.out.println("Enter customerID to begin transaction");
            System.out.println("Enter AdminCode  to enter adminMode");
            System.out.println("Enter q to quit");
            try {
                String input = reader.readLine();
                switch (input){
                    case "admin": adminMode();
                    break;
                    case "q": quit();
                    break;
                    default: beginTransaction(input);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }

    private void adminMode() {
        while (adminModeRunning) {

        }
    }

    private void beginTransaction(String id) {

    }


    private void initializeReaders() {
        InputStreamReader stream = new InputStreamReader(System.in);
        reader = new BufferedReader(stream);

    }

    private void initializeWasSelection() {
        currentWashSelections = new ArrayList<Wash>() {{
            add(new Wash("Discount Wash", 50));
            add(new Wash("Standard Wash", 80));
            add(new Wash("Deluxe Wash", 120));
        }};
    }

    private void addWash(String type, int price) {
        Wash wash = new Wash(type, price);
        currentWashSelections.add(wash);
    }

    private void removeWash(String type) {
        currentWashSelections.removeIf(wash -> wash.getType().equalsIgnoreCase(type));
    }

    private void quit() {
        systemRunning = false;
    }

    private void endTransaction() {
        transactionRunning = false;
    }


}
