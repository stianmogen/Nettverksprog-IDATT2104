package oblig1;

import java.io.*;
import java.net.*;

class AddThread extends Thread{

    public AddThread(Socket forbindelse) throws IOException {
        run(forbindelse);
    }

    public void run(Socket forbindelse) throws IOException {
        InputStreamReader leseforbindelse = new InputStreamReader(forbindelse.getInputStream());
        BufferedReader leseren = new BufferedReader(leseforbindelse);
        PrintWriter skriveren = new PrintWriter(forbindelse.getOutputStream(), true);

        //gir tilbakemeldinger til klienten
        skriveren.println("Du har kontakt");
        skriveren.println("Skriv inn et regnestykke, for eksempel '6 + 4' eller 7 - 2");

        String input = leseren.readLine();
        while (!input.equals("nei")) { //så lenge vi ikke får beskjed om å slutte
            System.out.println("En klient skrev " + input);
            String svar = kalk(input);
            skriveren.println(svar);
            skriveren.println("Et regnestykke til? Hvis du vil avslutte, skriv 'nei'");
            input = leseren.readLine();
        }

        /* Lukker forbindelsen */
        leseren.close();
        skriveren.close();
        forbindelse.close();
    }

    /**
     * Tar inn en streng med et regnestykket, og returnerer utregningen som streng
     * @param input
     * @return
     */

    static String kalk(String input){
        int svar;
        String[] regnestykket = input.split(" ");
        int tall1 = Integer.parseInt(regnestykket[0]);
        int tall2 = Integer.parseInt(regnestykket[2]);
        String operasjon = regnestykket[1];
        if (operasjon.equals("+")) svar = tall1 + tall2;
        else if (operasjon.equals("-")) svar = tall1 - tall2;
        else {
            return "Feil operasjon";
        }
        return String.valueOf(svar);
    }
}

class AddTjener {

    public static void main(String[] args) throws IOException{
        final int PORTNR = 1250;

        ServerSocket tjener = new ServerSocket(PORTNR);
        System.out.println("Venter pa klient, hold tight");
        int MAX = 10; //hvor mange tråder kan koble til samtidig

        for (int i = 1; i < MAX + 1; i++) {
            try {
                Socket forbindelse = tjener.accept();
                System.out.println("Trad " + i + " venter");
                AddThread at = new AddThread(forbindelse);
                at.start();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
