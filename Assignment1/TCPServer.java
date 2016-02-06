/*
 * A simple TCP server that echos messages back to the client.
 * For use in CPSC 441 lectures
 * Instructor: Prof. Mea Wang
 */

import java.io.*;
import java.net.*;
import java.nio.file.Paths;

public class TCPServer {
    public static void main(String args[]) {
        // Initialize a server socket and a client socket for the server
        ServerSocket echoServer = null;
        Socket clientSocket = null; //TODO use this to find port number
        
        
        // Initialize an input and an output stream
        String line = "";
        BufferedReader inBuffer;
        DataOutputStream outBuffer;

        if (args.length != 1)
        {
            System.out.println("Usage: TCPServer <Listening Port>");
            System.exit(1);
        }
        
        // Try to open a server socket on the given port
        // Note that we can't choose a port less than 1023 if we are not
        // privileged users (root)
        try {
            echoServer = new ServerSocket(Integer.parseInt(args[0])); //PORT number
        }
        catch (IOException e) {
            System.out.println(e);
        }
   
        try {
            // Create a socket object from the ServerSocket to listen and accept 
            // connections.
            clientSocket = echoServer.accept();
            System.out.println("Accept connection from " + clientSocket.toString());

            // Open input and output streams
            inBuffer = new BufferedReader(new
                 InputStreamReader(clientSocket.getInputStream()));
            outBuffer = new DataOutputStream(clientSocket.getOutputStream());

            // As long as we receive data, echo that data back to the client.
            while (!line.equals("terminate")) {
                line = inBuffer.readLine();
                System.out.println("Client: " + line);
                String[] s = line.split(" ", 2);
                if(line.equals("list")){
                	
                    String filePath = System.getProperty("user.dir");
                    
                    String outputString = "";
                    File folder = new File(filePath);
                    File[] listOfFiles = folder.listFiles();

                    for (int i = 0; i < listOfFiles.length; i++) {
                        if (listOfFiles[i].isFile()) {
                        	outputString = listOfFiles[i].getName() + "\n";
                        	outBuffer.writeBytes(outputString);
                        }  
                     }
                    
                     outBuffer.writeBytes("eof" + "\n"); // end of file      
                }
                else if(s[0].contains("get")){
                	System.out.println(s.length);
                	//Is there a filename after?
                	if(s.length == 1){
                		outBuffer.writeBytes("Invalid use of get");
                	}
                	else{
                		String filePath = System.getProperty("user.dir");
                		String fileName = s[1];
                		File folder = new File(filePath);
                        File[] listOfFiles = folder.listFiles();

                        for (int i = 0; i < listOfFiles.length; i++) {
                            if (listOfFiles[i].isFile()) {
                            	
                            	//if the file is in the directory 
                            	if(listOfFiles[i].getName().equals(fileName)){
                            		
                            		
                            		String fileNameNew = null;
                            		int portNum = clientSocket.getPort();
                            		System.out.println("Client port: " + portNum);
                            		
                            		outBuffer.writeBytes(fileNameNew); //send the new filename to the Client
                            		
                            		BufferedReader br = new BufferedReader(new FileReader(fileName));
                                    try {
                                        StringBuilder sb = new StringBuilder();
                                        String templine = br.readLine();

                                        while (templine != null) {
                                            
                                        	outBuffer.writeBytes(templine + "\n"); //write the contents to the client
                                            templine = br.readLine();
                                        }
                                       outBuffer.writeBytes("eof"); 
                                    } finally {
                                        br.close();
                                    }
                            		
                            		
                            		
                            	}
                            }  
                         }
                		
                	}
                	
                }
                
                else{
                	outBuffer.writeBytes("Unknown command: " + line + "\n");
                }
            }
            
            // Close the connections
            inBuffer.close();
            outBuffer.close();
            clientSocket.close();
            echoServer.close();
        }   
        catch (IOException e) {
            System.out.println(e);
        }
    }
    
    /**
     * From: http://stackoverflow.com/questions/16027229/reading-from-a-text-file-and-storing-in-a-string
     * @param fileName
     * @return
     * @throws IOException
     */
    
    String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        try {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append("\n");
                line = br.readLine();
            }
            return sb.toString();
        } finally {
            br.close();
        }
    }
    
}
