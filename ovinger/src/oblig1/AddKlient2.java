package oblig1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class AddKlient2 {
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        /* Bruker en scanner til å lese fra kommandovinduet */
        Scanner leserFraKommandovindu = new Scanner(System.in);
        System.out.print("Oppgi navnet pa maskinen til tjenerprogrammet: ");
        String tjenermaskin = leserFraKommandovindu.nextLine();

        /* Setter opp forbindelsen til tjenerprogrammet */
        Socket forbindelse = new Socket(tjenermaskin, PORTNR);
        System.out.println("Forbindelsen opprettet.");
        /* åpner en forbindelse for kommunikasjon med tjenerprogrammet */
        InputStreamReader leseforbindelse
                = new InputStreamReader(forbindelse.getInputStream());
        BufferedReader leseren = new BufferedReader(leseforbindelse);
        PrintWriter skriveren = new PrintWriter(forbindelse.getOutputStream(), true);

        String beskjed1 = leseren.readLine();
        String beskjed2 = leseren.readLine();
        System.out.println(beskjed1 + "\n" + beskjed2);
        String regnestykke = leserFraKommandovindu.nextLine();
        skriveren.println(regnestykke);
        while (!regnestykke.equals("nei")) {
            String svar = leseren.readLine();
            System.out.println("Ditt svar: " + svar);
            System.out.println(leseren.readLine());
            regnestykke = leserFraKommandovindu.nextLine();  // mottar en linje med tekst
            skriveren.println(regnestykke);
        }

        leseren.close();
        skriveren.close();
        forbindelse.close();

    }
}
