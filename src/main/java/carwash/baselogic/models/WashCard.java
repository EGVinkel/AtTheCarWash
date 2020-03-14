package carwash.baselogic.models;

import java.util.ArrayList;

public class WashCard {
    private String id;
    private String name;
    private int balance = 1000;
    private ArrayList<Wash> washTransactions;

    public WashCard(String id, String name) {
        this.id = id;
        this.name = name;
        washTransactions = new ArrayList<>();
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

    public void withDraw(Wash wash) {
        int newBalance = this.balance - wash.getPrice();

        if (newBalance < 0) {
            System.out.println("Insufficient funds, please recharge card");
            return;
        }
        this.balance = newBalance;
        washTransactions.add(wash);
    }

    public void recharge(int amount) {
        int newBalance = this.balance + amount;
        if (newBalance > 1000) {
            System.out.println("Exceeding maximum amount of 1000 please select " + (newBalance - 1000) + " or less");
            return;
        }
        this.balance = newBalance;
    }
}
