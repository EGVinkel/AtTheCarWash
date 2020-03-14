package carwash.baselogic;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            new WashSystem().startSystem();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
