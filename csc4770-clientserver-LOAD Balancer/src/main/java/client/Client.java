package client;

import java.net.*;
import java.io.*;

import static util.Util.*;


public class Client
{
    public static void main(String args[])
    {
        String address = "localhost";
        int port = Integer.parseInt(args[0]);

        // if (args.length == 2)
        //     address = args[0];
        // else if (args.length != 0 &&  args.length != 1)
        // {
        //     System.out.println ("Usage: java Client [port]");
        //     System.exit(1);
        // }

        try{
            //System.out.println("Connected to a server.");

            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in)); // Needed to get keyboard inputs

            String request_send = "";

            while(!request_send.equals("quit")){
                System.out.print("\n>");
                request_send = args[1] + ", ";
                String input = stdin.readLine();
                

                if(input.length() > 0){
                    // Establish the socket
                    request_send += input;
                    Socket socket = new Socket(address, port);
                    
                    String message_receive = (String) send_receive(socket, request_send);
                    System.out.println("Sent Message to server (middleware): " + request_send);
                    System.out.println("Received Message from the client (middleware): \n" + message_receive);
                    socket.close(); 
                }
                else{
                    continue;
                }



            }
              
        } 
        catch(IOException e){
            error(e, "A TCP error occurred.");
        }
        

    }
}
