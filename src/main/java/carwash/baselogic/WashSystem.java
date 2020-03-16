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
    private AdminMode adminMode;
    private BufferedReader reader;
    private JSONParser jsonParser = new JSONParser();

    WashSystem() {
        initializeReaders();
        this.adminMode = new AdminMode(reader);
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
                System.out.println(messsage + " Current balance is " + currentWashCard.getBalance());
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
                        if (input.equals("r")) {
                            endTransaction();
                            return;
                        }
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
                        writeCards();
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
        writeWash(wash);
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
            Object cards = jsonParser.parse(new FileReader("cards.json"));
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
        String type = (String) wash.get("type");
        Long price = (Long) wash.get("price");
        return new Wash(type, price.intValue());
    }

    private void writeSingleCard(WashCard washCard, JSONArray jsonArray)  {
        try{

            JSONObject card = new JSONObject();
            card.put("id", washCard.getId());
            card.put("name", washCard.getName());
            card.put("balance", washCard.getBalance());
            JSONArray transactions = new JSONArray();
            washCard.getWashTransactions().forEach(wash ->
                    {
                        JSONObject jsonWash = getJsonObject(wash);
                        transactions.add(jsonWash);
                    }
            );
            card.put("washTransactions", transactions);
            jsonArray.add(card);
            FileWriter writer = new FileWriter("cards.json");
            writer.write(jsonArray.toJSONString());
            writer.flush();
        }catch (IOException e){
            System.err.println(e.getMessage());
        }

    }

    private JSONObject getJsonObject(Wash wash) {
        JSONObject jsonWash = new JSONObject();
        jsonWash.put("type", wash.getType());
        jsonWash.put("price", wash.getPrice());
        return jsonWash;
    }

    private void writeCards() throws IOException, ParseException {
        JSONArray a = new JSONArray();
        activeWashCards.forEach((k, v) -> writeSingleCard(v,a));

    }

    private void writeWash(Wash wash) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONArray totalWashes = (JSONArray) jsonParser.parse(new FileReader("wash.json"));
            totalWashes.add(getJsonObject(wash));
            System.out.println(totalWashes.toJSONString());
            FileWriter writer = new FileWriter("wash.json");
            writer.write(totalWashes.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }



    }

    private boolean checkValidity(String in) {
        return in.equals("c") | in.equals("r") | in.equals("l");
    }

}
