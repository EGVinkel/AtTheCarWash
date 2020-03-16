package carwash.baselogic.models;

import java.util.ArrayList;

public class WashCard {
    private String id;
    private String name;
    private int balance = 1000;
    private ArrayList<Wash> washTransactions;

    public WashCard(String id, String name, ArrayList<Wash> washTransactions, int balance) {
        this.id = id;
        this.name = name;
        this.washTransactions = washTransactions;
        this.balance = balance;
    }

    public ArrayList<Wash> getWashTransactions() {
        return washTransactions;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBalance() {
        return balance;
    }

    public void withDraw(Wash wash, boolean discount) {
        int currentPrice = wash.getPrice();
        if(discount&&!wash.getType().equalsIgnoreCase("Deluxe Wash")){
            currentPrice = (int) (currentPrice*0.8);
        }
        System.out.println("balance " + this.balance + " price " + currentPrice);
        int newBalance = this.balance - currentPrice;

        if (newBalance < 0) {
            System.out.println("Insufficient funds, please recharge card");
            return;
        }
        this.balance = newBalance;
        System.out.println(this.balance);
        washTransactions.add(wash);
    }

    public boolean recharge(int amount) {
        amount = Math.abs(amount);
        int newBalance = this.balance + amount;
        if (newBalance > 1000) {
            System.out.println("Exceeding maximum amount of 1000 please select " + (1000-this.balance) + " or less");
            return false;
        }
        this.balance = newBalance;
        return true;
    }
}
