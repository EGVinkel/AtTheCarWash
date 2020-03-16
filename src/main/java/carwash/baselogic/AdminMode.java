package carwash.baselogic;

import carwash.baselogic.models.Wash;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

class AdminMode {
    private boolean adminModeRunning;
    private ArrayList<Wash> currentWashSelections;
    private ArrayList<Wash> totalWashes;
    private HashMap<String, Wash> washCounter;
    private BufferedReader reader;

    AdminMode(BufferedReader reader, ArrayList<Wash> totalWashes) {
        initializeWashSelection();
        washCounter = new HashMap<>();
        this.totalWashes = totalWashes;
        this.reader = reader;
    }

    public ArrayList<Wash> getCurrentWashSelections() {
        return currentWashSelections;
    }

    void begin() {
        adminModeRunning = true;
        while (adminModeRunning) {
            System.out.println("Enter 1 for wash statistics");
            System.out.println("Enter 2 to add or remove wash");
            System.out.println("Enter 3 to quit admin mode");
            try {
                String input = reader.readLine();
                switch (input) {
                    case "1":
                        getCount();
                        getStats();
                        decrementCount();
                        break;
                    case "2":
                        washAdminstration();
                        break;
                    case "3":
                        quit();
                        break;
                    default:
                        System.err.println("Invalid option");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private void addWash(String type, int price) {
        Wash wash = new Wash(type, price);
        currentWashSelections.add(wash);
    }

    private void removeWash(String type) {
        currentWashSelections.removeIf(wash -> wash.getType().equalsIgnoreCase(type));
    }

    private void getStats() {
        final int[] totalEarnings = {0};
        washCounter.forEach((name, wash) -> {
            System.out.println(name + " Amount " + wash.getCount() +
                    " Earnings for this wash type " + wash.getCount() * wash.getPrice());
            totalEarnings[0] = totalEarnings[0] + wash.getCount() * wash.getPrice();
        });
        System.out.println(totalEarnings[0]);
    }

    private void washAdminstration() throws IOException {
        System.out.println("Current Wash selection: ");
        System.out.println(currentWashSelections);
        System.out.println("To add a wash press 1");
        System.out.println("To remove a wash press 2");
        String input = reader.readLine();
        if (input.equals("1")) {
            System.out.println("Enter a new was in the following format name,price");
            String[] secondInput = reader.readLine().split(",");
            try {
                addWash(secondInput[0], Integer.parseInt(secondInput[1]));
            } catch (Exception e) {
                System.out.println("The entered values where invalid");
            }
        } else if (input.equals("2")) {
            System.out.println("Entered the exact name of the wash you want to remove");
            input = reader.readLine();
            try {
                for (Wash w : currentWashSelections) {
                    if (input.equals(w.getType())) {
                        removeWash(input);
                        System.out.println("Wash removed");
                        return;
                    } else {
                        System.out.println("No wash by that name found");
                        return;
                    }

                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }


    }

    private void initializeWashSelection() {
        currentWashSelections = new ArrayList<Wash>() {{
            add(new Wash("Discount Wash", 50));
            add(new Wash("Standard Wash", 80));
            add(new Wash("Deluxe Wash", 120));
        }};
    }

    private void quit() {
        adminModeRunning = false;
    }

    private void getCount() {
        totalWashes.forEach(wash -> {
            Wash prev = washCounter.put(wash.getType(), wash);
            if (prev != null) {
                prev.incrementCount();
                washCounter.put(wash.getType(), prev);
            }
        });
    }
    private void decrementCount(){
        washCounter.forEach((name,wash) -> wash.resetCount() );
    }

}
