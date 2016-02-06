/*
 * A simple TCP select server that accepts multiple connections and echo message back to the clients
 * For use in CPSC 441 lectures
 * Instructor: Prof. Mea Wang
 */

import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;

public class SelectServer {
    public static int BUFFERSIZE = 32;
    public static void main(String args[]) throws Exception 
    {
    	
    	DataOutputStream outBuffer = null;
        if (args.length != 1)
        {
            System.out.println("Usage: UDPServer <Listening Port>");
            System.exit(1);
        }

        // Initialize buffers and coders for channel receive and send
        String line = "";
        Charset charset = Charset.forName( "us-ascii" );  
        CharsetDecoder decoder = charset.newDecoder();  
        CharsetEncoder encoder = charset.newEncoder();
        ByteBuffer inBuffer = null;
        CharBuffer cBuffer = null;
        int bytesSent, bytesRecv;     // number of bytes sent or received
        
        // Initialize the selector
        Selector selector = Selector.open();

        // Create a server channel and make it non-blocking
        ServerSocketChannel tcpserver = ServerSocketChannel.open();
        tcpserver.configureBlocking(false);
       
        // Get the port number and bind the socket
        InetSocketAddress isa = new InetSocketAddress(Integer.parseInt(args[0]));
        tcpserver.socket().bind(isa);

        // Register that the server selector is interested in connection requests
        tcpserver.register(selector, SelectionKey.OP_ACCEPT);

        DatagramChannel udpserver = DatagramChannel.open();
        udpserver.socket().bind(isa);
        udpserver.configureBlocking(false);
        udpserver.register(selector, SelectionKey.OP_READ);

        // Wait for something happen among all registered sockets
        // for(;;) {
        try {
            boolean terminated = false;
            while (!terminated) 
            {
                if (selector.select(500) < 0)
                {
                    System.out.println("select() failed");
                    System.exit(1);
                }
                
                // Get set of ready sockets
                Set readyKeys = selector.selectedKeys();
                Iterator readyItor = readyKeys.iterator();

                // Walk through the ready set
                while (readyItor.hasNext()) 
                {
                    // Get key from set
                    SelectionKey key = (SelectionKey)readyItor.next();

                    // Remove current entry
                    readyItor.remove();

                    // Get the channel associated with the key
                    Channel currChan = (Channel) key.channel();

                    // Accept new connections, if any
                    if (key.isAcceptable() && currChan == tcpserver)
                    {
                        
                        SocketChannel clientChannel = ((ServerSocketChannel)key.channel()).accept();
                        clientChannel.configureBlocking(false);
                        System.out.println("Accept connection from " + clientChannel.socket().toString());
                        
                        // Register the new connection for read operation
                        clientChannel.register(selector, SelectionKey.OP_READ);
                    } 
                    else 
                    {
                        if (key.isReadable()) {
                            inBuffer = ByteBuffer.allocateDirect(BUFFERSIZE);
                            cBuffer = CharBuffer.allocate(BUFFERSIZE);
                            if (currChan == udpserver) {
                                DatagramChannel udpClient = (DatagramChannel)key.channel();
                                // Read from socket
                                SocketAddress clientAddress = udpClient.receive(inBuffer);
                                if (clientAddress == null)
                                {
                                    System.out.println("read() error, or connection closed");
                                    key.cancel();  // deregister the socket
                                    continue;
                                }
                                inBuffer.flip();      // make buffer available  
                                decoder.decode(inBuffer, cBuffer, false);
                                cBuffer.flip();
                                line = cBuffer.toString();
                                System.out.print("UDPClient: " + line + "\n");
                       
                                // Echo the message back
                                inBuffer.flip();
                                bytesSent = udpClient.send(inBuffer, clientAddress); 
                           
                                
                                if (line.equals("terminate"))
                                    terminated = true;
                                  continue;

                            }
                            else {

                                SocketChannel clientChannel = (SocketChannel)key.channel();
                                Socket socket = clientChannel.socket();
                                
                               
//                                outBuffer = new DataOutputStream(socket.getOutputStream());
                                // Read from socket
                                bytesRecv = clientChannel.read(inBuffer);
                                if (bytesRecv <= 0)
                                {
                                    System.out.println("read() error, or connection closed");
                                    key.cancel();  // deregister the socket
                                    break;
                                }
                                inBuffer.flip();      // make buffer available  
                                decoder.decode(inBuffer, cBuffer, false);
                                cBuffer.flip();
                                line = cBuffer.toString();
                                System.out.print("TCPClient: " + line);
                                String[] splitCmd = line.split(" ", 2);
                       
                                
                                ByteBuffer buff = null;
                                if(line.equals("list\n")){
                                	
                                    String filePath = System.getProperty("user.dir");
                                    String outputString = "";
                                    File folder = new File(filePath);
                                    File[] listOfFiles = folder.listFiles();

                                	

                                    for (int i = 0; i < listOfFiles.length; i++) {
                                        if (listOfFiles[i].isFile()) {
                                        	
                                        	outputString = listOfFiles[i].getName() + "\n";
                                        	
                                        	buff = ByteBuffer.wrap(outputString.getBytes());
                                        	
                                        	clientChannel.write(buff); 
                                        	
                                        
                                        
                                        }  
                                     }
                                    
                                   
                                    String eof = "eof\n";
                                    buff = ByteBuffer.wrap(eof.getBytes());
                                    clientChannel.write(buff);
                                	    
                                }
                                
                                else if(splitCmd[0].contains("get")){
                                	//Is there a filename after?
                                	if(splitCmd.length == 1){
                                		
                                		buff = ByteBuffer.wrap("Invalid use of get\n".getBytes());
                                        clientChannel.write(buff);
                                	}
                                	else{
                                		buff = ByteBuffer.wrap("Valid use of get\n".getBytes()); //send a dummy message to ignore
                                        clientChannel.write(buff);
                                        
                                		String filePath = System.getProperty("user.dir");
                                		String fileName = splitCmd[1];
                                		
                                		File folder = new File(filePath);
                                        File[] listOfFiles = folder.listFiles();
                                        
                                        boolean fileFound = false; 
                                        System.out.print("Open file: " + fileName);
                                        
                                        for (int i = 0; i < listOfFiles.length; i++) {
                                            if (listOfFiles[i].isFile()) {
                                            	
                                            	//if the file is in the directory 
                                            	if((listOfFiles[i].getName() + "\n").equals(fileName)){
                                                    fileFound = true;
                                            		BufferedReader br = new BufferedReader(new FileReader(listOfFiles[i].getName()));
                                                    try {
                                                        StringBuilder sb = new StringBuilder();
                                                        String templine = br.readLine();
                                                        
                                                        
                                                        while (templine != null) {
                                                            
                                                        	templine = templine + "\n";
                                                        	
                                                        	buff = ByteBuffer.wrap(templine.getBytes());
                                                            clientChannel.write(buff); //write the contents to the client
                                                            
                                                            templine = br.readLine();
                                                        }
                                                        buff = ByteBuffer.wrap("eof\n".getBytes());
                                                        clientChannel.write(buff); //end the file
                                                    } finally {
                                                        br.close();
                                                    }
                                            		
                                            		
                                            		
                                            	}
                                            }  
                                         }
                                        
                                        //if the file doesn't exist, print an error message
                                        if(fileFound == false)
                                        {
                                        	buff = ByteBuffer.wrap(("Error in opening file: " + fileName).getBytes());
                                            clientChannel.write(buff); //write error to client
                                            
                                            buff = ByteBuffer.wrap("eof\n".getBytes());
                                            clientChannel.write(buff); //end the output
                                            System.out.println("open() failed");
                                            
                                        }
                                		
                                	}
                                	
                                }
                                
                                else if(line.equals("terminate\n")){
                                	terminated = true;
                                	break;
                                }
                                else{
                                	String unknownCmd = "Unknown Command: " + line;
                                	buff = ByteBuffer.wrap(unknownCmd.getBytes());
                                	bytesSent = clientChannel.write(buff); 
                                }
                                
                                
                                
//                                if (line.equals("terminate\n"))
//                                    terminated = true;
                                
                            }
                            
                         }
                        //outBuffer.close();
                    }
                    
                } // end of while (readyItor.hasNext()) 
                
            } // end of while (!terminated)
        }
        catch (IOException e) {
            System.out.println(e);
        }

 
        // close all connections
        Set keys = selector.keys();
        Iterator itr = keys.iterator();
        while (itr.hasNext()) 
        {
            SelectionKey key = (SelectionKey)itr.next();
            //itr.remove();
            if (key.channel() instanceof ServerSocketChannel)
                ((ServerSocketChannel)key.channel()).socket().close();
            else if (key.channel() instanceof DatagramChannel)
                ((DatagramChannel)key.channel()).socket().close();
            else
                ((SocketChannel)key.channel()).socket().close();
        }
      }
}
