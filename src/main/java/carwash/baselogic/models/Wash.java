package carwash.baselogic.models;

public class Wash {
    private String type;
    private int price;

    public int getCount() {
        return count;
    }

    public void incrementCount() {
        this.count++;
    }

    private int count;

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }



    public Wash(String type, int price) {
        this.type = type;
        this.price = price;
    }

    public String getType() {
        return type;
    }



    @Override
    public String toString() {
        return  type;
    }
}
