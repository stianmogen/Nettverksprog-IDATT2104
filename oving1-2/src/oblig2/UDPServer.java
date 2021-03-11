package oblig2;

import javax.xml.crypto.Data;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * https://docs.oracle.com/javase/tutorial/displayCode.html?code=https://docs.oracle.com/javase/tutorial/networking/datagrams/examples/QuoteServerThread.java
 */

class UDPServerThread extends Thread{

    DatagramSocket socket = null;
    final int PORTNR = 4445; //udp

    public UDPServerThread() throws IOException {
        this("QuoteServer");
    }

    public UDPServerThread(String name) throws IOException{
        super(name);
        socket = new DatagramSocket(PORTNR);
    }

    public void run(){
        try {
            byte[] buf = new byte[256];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            InetAddress address = packet.getAddress();
            String message = "Write an equation, on the format '6 + 4' or '9 - 14'";
            byte[] messageBytes = message.getBytes();
            int port = packet.getPort();
            packet = new DatagramPacket(messageBytes, messageBytes.length, address, port);
            socket.send(packet);
            packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            String equation = new String(packet.getData(), 0, packet.getLength());
            byte[] ans;
            while (!equation.equals("no")){
                System.out.println("A client wrote " + equation);
                ans = kalk(equation).getBytes();
                packet = new DatagramPacket(ans, ans.length, address, port);
                socket.send(packet);
                packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);
                equation = new String(packet.getData(), 0, packet.getLength());
            }

            socket.close();

        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    static String kalk(String input){
        int svar;
        String[] regnestykket = input.split(" ");
        int tall1 = Integer.parseInt(regnestykket[0]);
        int tall2 = Integer.parseInt(regnestykket[2]);
        String operasjon = regnestykket[1];
        if (operasjon.equals("+")) svar = tall1 + tall2;
        else if (operasjon.equals("-")) svar = tall1 - tall2;
        else {
            return "Illegal operation";
        }
        return String.valueOf(svar);
    }

}

public class UDPServer {

    public static void main(String[] args) throws IOException {
        //new UDPServerThread().start();
        UDPServerThread udpServerThread = new UDPServerThread();
        udpServerThread.start();
    }

}
