package oblig2;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Scanner;

/**
 * https://docs.oracle.com/javase/tutorial/networking/datagrams/examples/QuoteClient.java
 */

public class UDPClient {

    public static void main(String[] args) throws IOException {

        final int PORTNR = 4445;

        Scanner readCommand = new Scanner(System.in);
        System.out.println("Whats the address?");
        String host = readCommand.nextLine();

        byte[] sendBuf = new byte[256];
        DatagramSocket socket = new DatagramSocket();
        InetAddress serverAddress = InetAddress.getByName(host);
        DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, serverAddress, PORTNR);
        socket.send(packet);

        packet = new DatagramPacket(sendBuf, sendBuf.length);
        socket.receive(packet);
        System.out.println(new String(packet.getData(), 0, packet.getLength()));
        byte[] input = readCommand.nextLine().getBytes();
        packet = new DatagramPacket(input, input.length, serverAddress, PORTNR);
        socket.send(packet);
        while (!new String(input, 0, input.length).equals("no")){

            packet = new DatagramPacket(sendBuf, sendBuf.length);
            socket.receive(packet);
            String ans = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Answer: " + ans);

            input = readCommand.nextLine().getBytes();

            packet = new DatagramPacket(input, input.length, serverAddress, PORTNR);
            socket.send(packet);

        }

        socket.close();

    }
}
