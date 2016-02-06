/*
 * A simple UDP server that echos messages back to the client.
 * For use in CPSC 441 lectures
 * Instructor: Prof. Mea Wang
 */

import java.io.*;
import java.net.*;

public class UDPServer {
    public static int BUFFERSIZE = 32;
    public static void main(String args[]) {
       
        // Declare a UDP server socket and a datagram packet
        DatagramSocket echoServer = null;
        DatagramPacket myPacket = null;

        // Initialize an input and an output stream
        String line = "";
        byte[] inBuffer = null;

        if (args.length != 1)
        {
            System.out.println("Usage: UDPServer <Listening Port>");
            System.exit(1);
        }
        
        // Try to open a server socket on the given port
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)
        try {
            echoServer = new DatagramSocket(Integer.parseInt(args[0]));
        }
        catch (IOException e) {
            System.out.println(e);
        }
   
        try {
            // As long as we receive data, echo that data back to the client.
            while (!line.equals("terminate")) {
                inBuffer = new byte[BUFFERSIZE];

                // Initialize a datagram packet for the receive operation
                myPacket = new DatagramPacket(inBuffer, inBuffer.length);
                
                // Receive data into myPacket from the socket
                echoServer.receive(myPacket);
                
                // Convert the packet to a string
                line = new String(myPacket.getData());
                
                // Trim the buffer data and get the actual received data
                line = line.substring(0, myPacket.getLength());
                System.out.println("Client " + myPacket.getAddress() + ":" + myPacket.getPort() + ": " + line);
                
                 // Echo the packet back to the client
                echoServer.send(myPacket);
            }
            
            // Close the socket
            echoServer.close();
        }   
        catch (IOException e) {
            System.out.println(e);
        }
    }
}
