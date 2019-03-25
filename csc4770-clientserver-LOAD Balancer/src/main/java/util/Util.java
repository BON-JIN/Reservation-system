package util;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class Util {
    public static void error(Exception e, String message) {
        System.err.println(message);
        e.printStackTrace();
    }

    // Sends message to given server and returns any reply
    public static Object send_receive(Socket server, Object message) {
        send(server, message);
        return receive(server);
    }

    public static void send(Socket server, Object message) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(server.getOutputStream());
            out.writeObject(message);
        } catch (IOException e) {
            error(e, "A TCP error occurred opening output stream from " + server.toString());
        }
    }

    public static Object receive(Socket server) {
        Object reply = null;
        try {
            ObjectInputStream in = new ObjectInputStream(server.getInputStream());
            reply = in.readObject();
        } catch (IOException e) {
            error(e, "A TCP error occurred opening input stream from " + server.toString());
        } catch (ClassNotFoundException e) {
            error(e, "Object could not be read from " + server.toString());
        }
        return reply;
    }

    public static Vector parse(String command) {
        Vector data = new Vector();
        StringTokenizer tokenizer = new StringTokenizer(command,",");
        String argument;

        while (tokenizer.hasMoreTokens()){
            argument = tokenizer.nextToken();
            argument = argument.trim();
            data.add(argument);
        }

        return data;
    }
}
