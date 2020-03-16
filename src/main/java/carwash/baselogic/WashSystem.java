package carwash.baselogic;

import carwash.baselogic.models.Wash;
import carwash.baselogic.models.WashCard;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

class WashSystem {
    private boolean transactionRunning;
    private boolean systemRunning = true;
    private HashMap<String, WashCard> activeWashCards = new HashMap();
    private WashCard currentWashCard;
    private FileWriter totalWashStatsWriter;
    private AdminMode adminMode;
    private BufferedReader reader;
    private FileWriter cardWriter;
    private FileReader cardReader;
    private JSONParser jsonParser = new JSONParser();

    WashSystem() throws IOException {
        initializeReaders();
        this.adminMode = new AdminMode(reader);
        totalWashStatsWriter = new FileWriter("wash.json");
        cardWriter = new FileWriter("cards.json");
        cardReader = new FileReader("cards.json");
        readCards();

    }

    public void startSystem() {
        System.out.println("Welcome to the car wash!");
        while (systemRunning) {
            System.out.println("Enter customerID to begin transaction");
            System.out.println("Enter admin to enter adminMode");
            System.out.println("Enter q to quit");
            try {
                String input = reader.readLine();
                switch (input) {
                    case "admin":
                        adminMode.begin();
                        break;
                    case "q":
                        quit();
                        break;
                    default:
                        beginTransaction(input);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }


    private void beginTransaction(String id) {
        transactionRunning = true;
        while (transactionRunning) {
            if (activeWashCards.containsKey(id)) {
                currentWashCard = activeWashCards.get(id);
                String messsage = "Welcome " + currentWashCard.getName();
                if (currentWashCard.getWashTransactions().size() > 0) {
                    messsage = "Welcome back " + currentWashCard.getName();
                }
                System.out.println(messsage + " Current balance is " +currentWashCard.getBalance());
                System.out.println("Current selection of washes: ");
                int counter = 1;
                for (Wash w : adminMode.getCurrentWashSelections()) {
                    System.out.println("-" + counter + "- " + w.getType() + " Price " + w.getPrice());
                    counter++;
                }
                if (discountActive()) {
                    System.out.println("We currently have a discount on Standard and Discount washes");
                }
                System.out.println("Enter the number of the wash you would like to buy!");
                System.out.println("Type l to view a list of previous washes");
                System.out.println("Type c to charge you card");
                System.out.println("Press r to return");
                try {
                    String input = reader.readLine();
                    if (checkValidity(input)) {
                        if(input.equals("r")) endTransaction();
                        transactionsOptions(input);
                        beginTransaction(id);
                    } else {
                        int selection = Integer.parseInt(input);
                        if (!(selection > adminMode.getCurrentWashSelections().size())) {
                            Wash w = adminMode.getCurrentWashSelections().get(selection - 1);
                            if (w != null)
                                makePayment(w);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Something went wrong " + e.getMessage());
                } catch (NumberFormatException e) {
                    System.out.println("Entered character is invalid!");
                }

            } else {
                System.out.println("That id does currently not belong to a WashCard, " +
                        "would you like to create one? Enter y/n");
                try {
                    String input = reader.readLine();
                    if (input.equals("y")) {
                        System.out.println("Enter the following, (with the same formatting), id,name");
                        String[] multiInput = reader.readLine().split(",");
                        String newId = multiInput[0];
                        String name = multiInput[1];
                        WashCard washCard = new WashCard(newId, name, new ArrayList<>(), 1000);
                        activeWashCards.put(newId, washCard);
                        System.out.println("Hello " + name + " a wash card with id " + newId + " has been created ");
                        writeSingleCard(washCard);
                        beginTransaction(newId);
                    }
                    if (input.equals("n")) {
                        System.out.println("Ok, returning to previous menu");
                        endTransaction();
                    }
                } catch (IOException | ParseException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    private void makePayment(Wash wash) {
        currentWashCard.withDraw(wash, discountActive());
    }

    private void transactionsOptions(String option) throws IOException {
        switch (option) {
            case "c":
                while (true) {
                    System.out.println("Enter amount to recharge with");
                    int input = Integer.parseInt(reader.readLine());
                    if (currentWashCard.recharge(input)) break;
                }
                break;
            case "l":
                System.out.println(currentWashCard.getWashTransactions().toString());
                break;
        }
    }

    private void initializeReaders() {
        InputStreamReader stream = new InputStreamReader(System.in);
        reader = new BufferedReader(stream);

    }

    private void quit() throws IOException, ParseException {
        systemRunning = false;
        writeCards();
    }

    private void endTransaction() {
        transactionRunning = false;
    }

    private boolean discountActive() {
        LocalDateTime date = LocalDateTime.now();
        return date.getHour() < 14 && date.getDayOfWeek().getValue() < 6;
    }

    private void readCards() {
        try {
            Object cards = jsonParser.parse(cardReader);
            JSONArray cardsList = (JSONArray) cards;
            cardsList.forEach(card -> {
                parseCard((JSONObject) card);
            });
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

    }

    private void parseCard(JSONObject card) {
        String id = (String) card.get("id");
        String name = (String) card.get("name");
        int balance = Integer.parseInt(card.get("balance").toString());
        JSONArray washList = (JSONArray) card.get("washTransactions");
        ArrayList<Wash> washTransactions = new ArrayList<>();
        washList.forEach(wash -> washTransactions.add(parseWash((JSONObject) wash)));
        WashCard wc = new WashCard(id, name, washTransactions, balance);
        activeWashCards.put(id, wc);
    }

    private Wash parseWash(JSONObject wash) {
        JSONObject type = (JSONObject) wash.get("type");
        JSONObject price = (JSONObject) wash.get("price");
        return new Wash(type.toString(), Integer.parseInt(price.toString()));
    }

    private void writeSingleCard(WashCard washCard) throws IOException, ParseException {
        JSONArray a = (JSONArray) jsonParser.parse(cardReader);
        JSONObject card = new JSONObject();
        card.put("id", washCard.getId());
        card.put("name", washCard.getName());
        card.put("balance", washCard.getBalance());
        card.put("washTransactions", washCard.getWashTransactions());
        a.add(card);
        cardWriter.write(a.toJSONString());
        cardWriter.flush();
    }

    private void writeCards() throws IOException, ParseException {
        JSONArray array = new JSONArray();
        activeWashCards.forEach((k, v) -> array.add(v));
        cardWriter.write(array.toJSONString());
    }

    private void writeWash() {

    }

    private boolean checkValidity(String in) {
        return in.equals("c") | in.equals("r") | in.equals("l");
    }

}
