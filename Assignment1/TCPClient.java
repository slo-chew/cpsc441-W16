/*
 * A simple TCP client that sends messages to a server and display the message
   from the server. 
 * For use in CPSC 441 lectures
 * Instructor: Prof. Mea Wang
 * Modified by: Group 33 - W2016
 */


import java.io.*; 
import java.nio.file.*;
import java.net.*; 

class TCPClient { 

    public static void main(String args[]) throws Exception 
    { 
        if (args.length != 2)
        {
            System.out.println("Usage: TCPClient <Server IP> <Server Port>");
            System.exit(1);
        }

        // Initialize a client socket connection to the server
        Socket clientSocket = new Socket(args[0], Integer.parseInt(args[1])); 

        // Initialize input and an output stream for the connection(s)
		PrintWriter outBuffer =
			new PrintWriter(clientSocket.getOutputStream(), true);
		
        BufferedReader inBuffer = 
          new BufferedReader(new
          InputStreamReader(clientSocket.getInputStream())); 

        // Initialize user input stream
        String line; 
        BufferedReader inFromUser = 
        new BufferedReader(new InputStreamReader(System.in)); 

        // Get user input and send to the server
        // Display the echo meesage from the server
        System.out.print("Please enter a message to be sent to the server ('logout' to terminate): ");
        line = inFromUser.readLine(); 
        
        
        while (!line.equals("logout"))
        {
        	outBuffer.println(line); 
        	String[] splitCmd = line.split(" ", 2); //Split user input into 2 parts: command and filename
        	
			if (line.equals("list")) {
				while (true) {
					
					String temp = inBuffer.readLine();

					if (!temp.equals("eof")) {
						System.out.println(temp);
					} else {
						break;
					}
				}
			}

			else if (splitCmd[0].equals("get")) {

				String initialResponse = inBuffer.readLine();
				
				if (initialResponse.equals("Invalid use of get")) {
					System.out.println(initialResponse);
				}
				else {
					
					String fileName = splitCmd[1]; //set fileName equal to the file name
					int portNum = clientSocket.getLocalPort();
					String pNum = String.valueOf(portNum);
					
					int numBytes = 0;
					
					String fileNameNew = fileName + "-" + pNum;
					
					// TODO create file here
					PrintWriter writerToFile = new PrintWriter(fileNameNew, "UTF-8");
					
					// TODO copy contents into file
					while (true) {
						String temp = inBuffer.readLine();
						if (!temp.equals("eof")) {
							writerToFile.println(temp);
							
							final byte[] utf8Bytes = temp.getBytes("UTF-8");
							numBytes += utf8Bytes.length;
							
						}
						else{
							writerToFile.close();
							System.out.println("File saved in " + fileNameNew + "(" + numBytes + " bytes)");
							break;
						}
					}
					

				}

			}

			// Send to the server

			else {

				// Getting response from the server
				line = inBuffer.readLine();
				System.out.println("Server: " + line);
			}
            System.out.print("Please enter a message to be sent to the server ('logout' to terminate): ");
            line = inFromUser.readLine(); 
            
        	
        	
        }

        // Close the socket
        clientSocket.close();           
    } 
} 
