package carwash.baselogic;

import carwash.baselogic.models.Wash;
import carwash.baselogic.models.WashCard;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class DTO {
    private ArrayList<Wash> totalWash;
    private HashMap<String, WashCard> activeWashCards;
    private JSONParser jsonParser = new JSONParser();

    DTO(ArrayList<Wash> totalWash, HashMap<String, WashCard> activeWashCards){
        this.totalWash= totalWash;
        this.activeWashCards = activeWashCards;

    }
    public void writeData() {
        writeCards();
        writeWashes();
    }

    public void retrieveData() {
        readTotal();
        readCards();

    }

    private void readTotal() {
        try {
            JSONArray washList  = (JSONArray) jsonParser.parse(new FileReader("wash.json"));
            washList.forEach(wash -> {
                Wash w = parseWash((JSONObject) wash);
                totalWash.add(w);

            });
        } catch (IOException | ParseException e) {
            System.out.println("File not found" + e.getMessage());
        }

    }
    private void readCards() {
        try {
            JSONArray cardsList = (JSONArray) jsonParser.parse(new FileReader("cards.json"));
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

    private void writeCards() {
        JSONArray a = new JSONArray();
        activeWashCards.forEach((k, v) -> writeSingleCard(v,a));

    }
    private JSONObject getJsonObject(Wash wash) {
        JSONObject jsonWash = new JSONObject();
        jsonWash.put("type", wash.getType());
        jsonWash.put("price", wash.getPrice());
        return jsonWash;
    }
    private void writeWashes(){
        try {
        JSONArray JSONtotal = new JSONArray();
        totalWash.forEach(wash -> JSONtotal.add(getJsonObject(wash)));
        FileWriter writer = new FileWriter("wash.json");
        writer.write(JSONtotal.toJSONString());
        writer.flush();
    } catch (IOException e) {
        e.printStackTrace();
    }
    }

    private Wash parseWash(JSONObject wash) {
        String type = (String) wash.get("type");
        Long price = (Long) wash.get("price");
        return new Wash(type, price.intValue());
    }


}
