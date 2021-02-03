package oblig1;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

class AddWebThread extends Thread{

    public AddWebThread(Socket forbindelse, int i) throws IOException {
        run(forbindelse, i);
    }

    private void run(Socket forbindelse, int i) throws IOException {
        InputStreamReader leseforbindelse = new InputStreamReader(forbindelse.getInputStream());
        BufferedReader leseren = new BufferedReader(leseforbindelse);
        PrintWriter skriveren = new PrintWriter(forbindelse.getOutputStream(), true);

        skriveren.println("HTTP/1.0 200 OK");
        skriveren.println("Content-Type: text/html; charset=utf-8 (linjeskift)");
        String alt = "<ul>";
        String inputLine;
        while (!(inputLine = leseren.readLine()).equals("")) {
            alt += "<li>" + inputLine + "</li>";
        }
        alt += "</ul>";
        skriveren.println("\r\n");
        skriveren.println("<HTML><BODY>\n" +
                "<H1> Hilsen. Du har koblet deg opp til min enkle web-tjener </h1>\n" +
                alt +
                "</BODY></HTML>");
        skriveren.println("Du er koblet opp som bruker " + i);
        skriveren.flush();
        forbindelse.close();
    }

}

public class AddWebTjener {
    public static void main(String[] args) throws IOException{
        final int PORTNR = 80;
        int MAX = 10; //hvor mange tråder kan koble til samtidig

        ServerSocket tjener = new ServerSocket(PORTNR);
        System.out.println("Venter pa klient, hold tight");

        for (int i = 1; i < MAX + 1; i++) {
            try {
                Socket forbindelse = tjener.accept();
                System.out.println("Trad " + i + " venter");
                AddWebThread awt = new AddWebThread(forbindelse, i);
                awt.start();
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }
}
