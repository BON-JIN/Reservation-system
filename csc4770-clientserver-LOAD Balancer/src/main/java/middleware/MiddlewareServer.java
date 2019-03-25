package middleware;

import server.ResImpl.Trace;
import util.Command;

import java.net.*;
import java.io.*; // (Including IOException)
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static util.Command.*;
import static util.Util.*;


class LoadBalancer 
{
    private Hashtable<String, Vector<Integer> > routerTable ;
    private int port;
    private ServerSocket socket_loadbalancer = null;
 
    LoadBalancer(int p){
        routerTable = new Hashtable<String, Vector<Integer> >();
        port = p;
    }

    public void updateTable(String loc, Vector<Integer> ports){
        routerTable.put(loc, ports);
        return;
    }
    public int getPort(){
        return  port;
    }
    public Vector<Integer> getPorts(String key){
        return  routerTable.get(key);
    }

    public boolean findKey(String key){
        return routerTable.containsKey(key);
    }
}


public class MiddlewareServer 
{
    public static void main(String[] args) 
    {
        String address = "localhost";
        Vector data = null;

        // if (args.length != 4) {
        //     usage();
        // }
        //try {

            LoadBalancer loadbalancer = new LoadBalancer(Integer.parseInt(args[0]));
            
            Thread t_loadbalancer = new ServerHandler(loadbalancer);
            t_loadbalancer.start();
               
            try{
                t_loadbalancer.sleep(500);
            }
            catch(InterruptedException i){

            }

            // If a client sends a request then creates a thread
            while(true){
                try{
                     ServerSocket socket_clients = new ServerSocket(Integer.parseInt(args[1]));
                    System.out.println("Waiting for a new client at port:" + args[1]);

                    Socket socket = socket_clients.accept();
                    socket_clients.close();

                    Thread thread = new ClientHandler(loadbalancer, socket);

                    thread.start();                
                }
                catch(IOException e){

                }
            }
            
                
            // try{
            //     Socket server_flight = new Socket(address, 7000);
            //     Socket server_car = new Socket(address, 7001);
            //     Socket server_room = new Socket(address, 7002);                
            // } 
            // catch(IOException i){

            // }
            
            // while(true){
            //     Socket socket = null;
            //     try {
            //         ServerSocket socket_clients = new ServerSocket(Integer.parseInt(args[1]));
            //         System.out.println("Waiting for a new client at port:" + args[1]);
            //         socket = socket_clients.accept();
            //         System.out.println("Connected to a new client.");

            //         String request = (String) receive(socket);
            //         System.out.println(request);
            //         request = request.trim();
            //         data = parse(request);

            //         String key = (String) data.elementAt(0);
            //         data.remove(0);
            //         key = key.toLowerCase();
            //         Vector <Integer> ports = loadbalancer.getPorts(key);



            //         Socket server_flight = new Socket(address, ports.get(0));
            //         Socket server_car = new Socket(address, ports.get(1));
            //         Socket server_room = new Socket(address, ports.get(2));

            //         Thread thread = new ClientHandler(socket, server_flight, server_car, server_room, data);
            //         System.out.println("New thread created with CID: " + thread.getId());

            //         send(socket, "YEAH MAN");


            //         thread.start();

            //         System.out.println("Thread has been stopped. Socket is closed now.");
            //         //socket.close();
            //         //socket_clients.close();
            //     }        
            //     catch(IOException e) {
            //         System.out.println(e);
            //     }
            //     // catch(IOException e){
            //     //     socket.close();
            //     //     e.printStackTrace(); 
            //     //     error(e, "A TCP error occurred");
            //     //     break;
            //     // } 
            // }

    }

    private static void usage() {
        System.out.println ("Usage: java MiddlewareServer <middleware server port> <flight server port> <car server port> <room server port>");
        System.exit(1);
    }

}


class ClientHandler extends Thread  
{ 
    // Flight, car, and room locks should prevent simultaneous reservations, additions, or deletions,
    // especially while querying one of them
    private final ReentrantReadWriteLock flightLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock carLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock roomLock = new ReentrantReadWriteLock();
    // Customer lock should prevent addition or deletion of customer while querying or reserving anything
    private final ReentrantReadWriteLock customerLock = new ReentrantReadWriteLock();
 
    
    private Socket socket;


    private Vector data;
    private Vector <Integer> ports;
    private String request;
    private String message = "Failed.";
    private long CID;
    private LoadBalancer loadbalancer;
    private int port;
    private String address = "localhost";

    public int getPort(int id){
        return ports.get(id);
    }

    // Constructor 
    public ClientHandler(LoadBalancer l, Socket s) { 
        // this.socket = s; 
        
        // this.server_flight = flight;
        // this.server_car = car;
        // this.server_room = room;
        // this.data = dt;
        this.loadbalancer = l;
        this.socket = s;
        this.CID = this.getId();
    } 

    @Override
    public void run() {

        try{ 
            String request = (String) receive(socket);
            System.out.println(request);
            System.out.println("Received Request from the client with CID: " + CID + "\n" + request);

            request = request.trim();
            Vector data = parse(request);

            // Getting port numbers.
            String key = (String) data.elementAt(0);
            data.remove(0);
            key = key.toLowerCase();

            if(!loadbalancer.findKey(key)) {
                message = "Sorry....The server in " + key + "is not currently available.";
            }
            else{
                ports = loadbalancer.getPorts(key);
                Command command = null;

                if(!isCommandValidated(data)){
                    message = "Invalid request.  The number of data provided in this command are wrong "
                            + "or the interface does not support this command: " + request
                            + "\nType help, <commandname> to check usage of this command.";
                }
                else {
                    // Decide which of the commands this was and set the sending/receiving stream(socket) that the middleware requests to.
                    // Since the middleware sever cannot call any function implemented in Severs/Client, it should only know which server/client to senda request.
                    // Some are special cases that middleware does not have to communicate other else servers than client while processing.
                    System.out.println("Sending a request to a specific server.");
                    command = findCommand((String) data.elementAt(0));
                    message = executeRequest(command, data);
                }
            }


            // // Sending a result to the client.
            // System.out.println("Sent back a message to the Client.");
            send(socket, message);
            socket.close();
            // server_flight.close();
            // server_car.close();
            // server_room.close();
                    
        }

        catch(IOException e){ 
            e.printStackTrace(); 
        }

    }

    private Vector parse(String command) {
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

    private String executeRequest(Command command, Vector data) {
        switch (command) {
            case HELP:
                return getMessage(command, data);
            case NEWFLIGHT:
                return addFlight(data);
            case NEWCAR:
                return addCars(data);
            case NEWROOM:
                return addRooms(data);
            case NEWCUSTOMER:
                return newCustomer(data);
            case DELETEFLIGHT:
                return deleteFlight(data);
            case DELETECAR:
                return deleteCars(data);
            case DELETEROOM:
                return deleteRooms(data);
            case DELETECUSTOMER:
                return deleteCustomer(data);
            case QUERYFLIGHT:
                return queryFlight(data);
            case QUERYCAR:
                return queryCars(data);
            case QUERYROOM:
                return queryRooms(data);
            case QUERYCUSTOMER:
                return queryCustomerInfo(data);
            case QUERYFLIGHTPRICE:
                return queryFlightPrice(data);
            case QUERYCARPRICE:
                return queryCarsPrice(data);
            case QUERYROOMPRICE:
                return queryRoomsPrice(data);
            case RESERVEFLIGHT:
                return reserveFlight(data);
            case RESERVECAR:
                return reserveCar(data);
            case RESERVEROOM:
                return reserveRoom(data);
            case ITINERARY:
                return itinerary(data);
            case QUIT:
                return quit(data); // TODO not needed
            case NEWCUSTOMERID:
                return newCustomer(data); // TODO update as needed
            case INVALID:
                return "";
        }
        return "";
    }

    private boolean isCommandValidated(Vector data) {
        String command = (String)data.elementAt(0);

        // Validation for help
        if (command.compareToIgnoreCase("help")==0)
            return true;

        // Validation for Flights
        else if(command.compareToIgnoreCase("newflight")==0 && data.size() == 5)
            return true;
        else if(command.compareToIgnoreCase("deleteflight")== 0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("queryflight")== 0  && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("queryflightprice")==0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("reserveflight")==0 && data.size() == 4)
            return true;

        // Validation for Cars
        else if(command.compareToIgnoreCase("newcar")==0 && data.size() == 5)
            return true;
        else if(command.compareToIgnoreCase("deletecar")==0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("querycar")==0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("querycarprice")== 0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("reservecar")==0 && data.size() == 4)
            return true;

        // Validation for Customers
        else if(command.compareToIgnoreCase("newcustomer")== 0 && data.size() == 2)
            return true;
        else if(command.compareToIgnoreCase("newcustomerid")== 0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("deletecustomer")== 0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("querycustomer")== 0 && data.size() == 3)
            return true;


        // Validation for Rooms
        else if(command.compareToIgnoreCase("newroom")==0 && data.size() == 5)
            return true;
        else if(command.compareToIgnoreCase("deleteroom")==0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("queryroom")==0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("queryroomprice")==0 && data.size() == 3)
            return true;
        else if(command.compareToIgnoreCase("reserveroom")==0 && data.size() == 4)
            return true;
        else if(command.compareToIgnoreCase("itinerary")==0 && !(data.size() < 7))
            return true;
        else return command.compareToIgnoreCase("quit") == 0;
    }


    private boolean getBoolean(Object temp) {
        return Boolean.valueOf((String) temp);
    }

    private String getMessage(Command command, Vector data) {
        String string;

        if(data.size()==1)   //command was "help"
            string = listCommands();
        else if (data.size()==2)  //command was "help <commandname>"
            string = getHelpDescription((String)data.elementAt(1));
        else  //wrong use of help command
            string = "Improper use of help command. Type help or help, <commandname>";

        return string;
    }

    private String listCommands() {
        String string = ("\nWelcome to the Client interface provided to test your project.\n"
                        + "Commands accepted by the interface are:\n"
                        + "help\n"
                        + "newflight\nnewcar\nnewroom\nnewcustomer\nnewcusomterid\ndeleteflight\ndeletecar\ndeleteroom\n"
                        + "deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer\n"
                        + "queryflightprice\nquerycarprice\nqueryroomprice\n"
                        + "reserveflight\nreservecar\nreserveroom\nitinerary\n"
                        + "quit\n"
                        + "\ntype help, <commandname> for detailed info(NOTE the use of comma).");
        return string;
    }
    
    private String addFlight(Vector data) {
        String result = "";

        try{
            Trace.info("MS::addFlight(" + data.toString() + ") called" );
            flightLock.writeLock().lock();
            Socket server_flight = new Socket(address, getPort(0));
            result = (String)send_receive(server_flight, data);
            server_flight.close();
            flightLock.writeLock().unlock();
            return result;
        }
        catch(IOException e){

        }
        return result;
    }

    private String addCars(Vector data) {
        String result = "";
        try{
            Trace.info("MS::addCars(" + data.toString() + ") called" );
            carLock.writeLock().lock();
            Socket server_car = new Socket(address, getPort(1));
            result = (String)send_receive(server_car, data);
            server_car.close();
            carLock.writeLock().unlock();
            return result; 
        }
        catch(IOException e){

        }
        return result; 
    }

    private String addRooms(Vector data) {
        String result = "";
        try{
        Trace.info("MS::addRooms(" + data.toString() + ") called" );
        roomLock.writeLock().lock();
        Socket server_room = new Socket(address, getPort(2));
        result = (String)send_receive(server_room, data);
        server_room.close();
        roomLock.writeLock().unlock();
        return result;
        }
        catch(IOException e){

        }
        return result;
    }

    // TODO need to parse data for specific customer id (probably separate method)
    private String newCustomer(Vector data) {
        Trace.info("MS::newCustomer(" + data.toString() + ") called" );
        return manageCustomer(data);
    }

    private String deleteFlight(Vector data) {
        String result = "";
        try{
        Trace.info("MS::deleteFlight(" + data.toString() + ") called");
        flightLock.writeLock().lock();
        Socket server_flight = new Socket(address, getPort(0));
        result = (String)send_receive(server_flight, data);
        server_flight.close();
        flightLock.writeLock().unlock();
        return result;
        }
        catch(IOException e){
            
        }  
        return result;     
    }

    private String deleteCars(Vector data) {
        String result = "";
        try{
        Trace.info("MS::deleteCars(" + data.toString() + ") called");
        carLock.writeLock().lock();
        Socket server_car = new Socket(address, getPort(1));
        result = (String)send_receive(server_car, data);
        server_car.close();
        carLock.writeLock().unlock();
        return result;
        }
        catch(IOException e){
            
        } 
        return result;
    }

    private String deleteRooms(Vector data) {
        String result = "";
        try{
        Trace.info("MS::deleteRooms(" + data.toString() + ") called");
        roomLock.writeLock().lock();
        Socket server_room = new Socket(address, getPort(2));
        result = (String)send_receive(server_room, data);
        server_room.close();
        roomLock.writeLock().unlock();
        return result;
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String deleteCustomer(Vector data) {
        Trace.info("MS::deleteCustomer(" + data.toString() + ") called" );
        return manageCustomer(data);
    }

    private String manageCustomer(Vector data) {
        String result = "";
        try{
        customerLock.writeLock().lock();
        Socket server_flight = new Socket(address, getPort(0));
        Socket server_car = new Socket(address, getPort(1));
        Socket server_room = new Socket(address, getPort(2));
        result = (String)send_receive(server_flight, data) +
                         send_receive(server_car, data) +
                         send_receive(server_room, data);
        server_flight.close();
        server_car.close();
        server_room.close();
        customerLock.writeLock().unlock();
        return result;
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String queryFlight(Vector data) {
        String result = "";
        try{
        Trace.info("MS::queryFlight(" + data.toString() + ") called");
        flightLock.readLock().lock();
        Socket server_flight = new Socket(address, getPort(0));
        result = (String)send_receive(server_flight, data);
        server_flight.close();
        flightLock.readLock().unlock();
        return result;
        }
        catch(IOException e){
            
        }
        return result;

    }

    private String queryCars(Vector data) {
        String result = "";
        try{
        Trace.info("MS::queryCars(" + data.toString() + ") called");
        carLock.readLock().lock();
        Socket server_car = new Socket(address, getPort(1));
        result = (String)send_receive(server_car, data);
        server_car.close();
        carLock.readLock().unlock();
        return result;
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String queryRooms(Vector data) {
        String result = "";
        try{
        Trace.info("MS::queryRooms(" + data.toString() + ") called");
        roomLock.readLock().lock();
        Socket server_room = new Socket(address, getPort(2));
        result = (String)send_receive(server_room, data);
        server_room.close();
        roomLock.readLock().unlock();
        return result;
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String queryCustomerInfo(Vector data) {

        String flightBill ="";
        String carBill = "";
        String roomBill = "";

        try{
            Trace.info("MS::queryCustomerInfo(" + data.toString() + ") called" );
            Socket server_flight = new Socket(address, getPort(0));
            Socket server_car = new Socket(address, getPort(1));
            Socket server_room = new Socket(address, getPort(2));
            flightBill = queryCustomerInfo(data, server_flight, flightLock);
            carBill = queryCustomerInfo(data, server_car, carLock);
            roomBill = queryCustomerInfo(data, server_room, roomLock);
            server_flight.close();
            server_car.close();
            server_room.close();        
        }
        catch(IOException e){
            
        }

        return flightBill + "\n" + carBill + "\n" + roomBill;
    }

    private String queryCustomerInfo(Vector data, Socket rm, ReentrantReadWriteLock lock) {
        lock.readLock().lock();
        String bill = (String)send_receive(rm, data);
        lock.readLock().unlock();
        return bill;
    }

    private String queryFlightPrice(Vector data) {
        String result = "";
        try{
        Trace.info("MS::queryFlightPrice(" + data.toString() + ") called");
        flightLock.readLock().lock();
        Socket server_flight = new Socket(address, getPort(0));
        result = (String)send_receive(server_flight, data);
        server_flight.close();
        flightLock.readLock().unlock();   
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String queryCarsPrice(Vector data) {
        String result = "";
        try{
        Trace.info("MS::queryCarsPrice(" + data.toString() + ") called");
        carLock.readLock().lock();
        Socket server_car = new Socket(address, getPort(1));
        result = (String)send_receive(server_car, data);
        server_car.close();
        carLock.readLock().unlock();
        
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String queryRoomsPrice(Vector data) {
        String result = "";
        try{
        Trace.info("MS::queryRoomsPrice(" + data.toString() + ") called");
        roomLock.readLock().lock();
        Socket server_room = new Socket(address, getPort(2));
        result = (String)send_receive(server_room, data);
        server_room.close();
        roomLock.readLock().unlock();
        
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String reserveFlight(Vector data) {
        String result = "";
        try{
        Trace.info("MS::reserveFlight(" + data.toString() + ") called");
        Socket server_flight = new Socket(address, getPort(0));
        result = reserveItem(data, flightLock, server_flight);
        server_flight.close();

        }
        catch(IOException e){
            
        }
        return result;
    }

    private String reserveCar(Vector data) {
        String result = "";

        try{
        Trace.info("MS::reserveCar(" + data.toString() + ") called");
        Socket server_car = new Socket(address, getPort(1));
        result = reserveItem(data, carLock, server_car);
        server_car.close();
        
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String reserveRoom(Vector data) {
        String result = "";
        try{
        Trace.info("MS::reserveRoom(" + data.toString() + ") called");
        Socket server_room = new Socket(address, getPort(2));
        result = reserveItem(data, roomLock, server_room);
        server_room.close();

        
        }
        catch(IOException e){
            
        }
        return result;
    }

    private String reserveItem(Vector data, ReentrantReadWriteLock lock, Socket server) {
        String result = "";
        lock.writeLock().lock();
        customerLock.writeLock().lock();
        result = (String)send_receive(server, data);
        customerLock.writeLock().unlock();
        lock.writeLock().unlock();
        return result;
    }

    private String itinerary(Vector data) {

        try{
        Trace.info("MS::itinerary(" + data.toString() + ") called");
        Vector packet;

        String command_str;
        String Id = ((String)data.elementAt(1));
        String customer = ((String)data.elementAt(2));
        String message;

        int offset = data.size()-6;
        String location = ((String)data.elementAt(3+offset));
        Object car = data.elementAt(4+offset);
        Object room = data.elementAt(5+offset);

        // Reserve for flights.
        command_str = "reserveflight";
        Vector flightNumbers = new Vector();

        for(int i=0;i<data.size()-6;i++){
            flightNumbers.addElement(data.elementAt(3+i));
        }

        Iterator it = flightNumbers.iterator();

        message = "\nFrom Flight server: \n";
        while(it.hasNext()){
            Object fn = it.next();
            String tmp = command_str + ", " + Id + ", " + customer + ", " + fn;
            System.out.println("Send a request: " + tmp);
            packet = parse(tmp);
            Socket server_flight = new Socket(address, getPort(0));
            message +=  send_receive(server_flight, packet) + "\n";
            server_flight.close();
        }

        // Reserve for Car
        command_str = "reservecar";
        if(this.getBoolean(car)){
            String tmp = command_str + ", " + Id + ", " + customer + ", " + location;
            System.out.println("Send a request: " + tmp);
            packet = parse(tmp);
            Socket server_car = new Socket(address, getPort(1));
            message += "\nFrom Car server: \n" + send_receive(server_car, packet) + "\n";
            server_car.close();
        }

        // Reserve for room
        command_str = "reserveroom";
        if(this.getBoolean(room)){
            String tmp = command_str + ", " + Id + ", " + customer + ", " + location;
            System.out.println("Send a request: " + tmp);
            packet = parse(tmp);
            Socket server_room = new Socket(address, getPort(2));
            message += "\nFrom Room server: \n" + send_receive(server_room, packet) + "\n";
            server_room.close();
        }

        message += "\n\nItinerary Reserved\n";
        }
        catch(IOException e){
            
        }
        return message;
    }

    private String quit(Vector data) {
        String result = "";
        try{
            Socket server_flight = new Socket(address, getPort(0));
            Socket server_car = new Socket(address, getPort(1));
            Socket server_room = new Socket(address, getPort(2));
            result = "From Flight server: \n" + send_receive(server_flight, data) + "\n" +
                         "From Car server: \n" + send_receive(server_car, data) + "\n" +
                         "From Room server: \n" + send_receive(server_room, data) + "\n";
            server_flight.close();
            server_car.close();
            server_room.close(); 
        }
        catch(IOException e){
            
        }
        return result;
    }
}

class ServerHandler extends Thread{
 
    private LoadBalancer loadBalancer;
    private String address = "localhost";

    ServerHandler(LoadBalancer load){
       this.loadBalancer = load;
    }

    @Override
    public void run(){

        try{
            while(true){
                ServerSocket socket_loadbalancer = new ServerSocket(loadBalancer.getPort());
                System.out.println("Waiting for a server on port: " + loadBalancer.getPort());
                Socket socket = socket_loadbalancer.accept();
                System.out.println("Accepted a new server."); 

                String request = (String) receive(socket);

                request = request.trim();
                Vector data = parse(request);

                Vector <Integer> ports = new Vector<Integer>();
                String location = (String) data.elementAt(0);
                location = location.toLowerCase();

                int port_flight = Integer.parseInt((String) data.elementAt(1));
                int port_car = Integer.parseInt((String) data.elementAt(2));
                int port_room = Integer.parseInt((String) data.elementAt(3));

                ports.add(port_flight);
                ports.add(port_car);
                ports.add(port_room);

                loadBalancer.updateTable(location, ports);
                System.out.println("Connection succeeded. Router table is updated.");
                send(socket, "Connection succeeded. Router table is updated.");

                System.out.println("Closing sockets.");
                socket.close();
                socket_loadbalancer.close();
            }
        }
        catch(IOException e){

        }
    }

}

// class ClientHandler extends Thread  
// { 
//     // Flight, car, and room locks should prevent simultaneous reservations, additions, or deletions,
//     // especially while querying one of them
//     private final ReentrantReadWriteLock flightLock = new ReentrantReadWriteLock();
//     private final ReentrantReadWriteLock carLock = new ReentrantReadWriteLock();
//     private final ReentrantReadWriteLock roomLock = new ReentrantReadWriteLock();
//     // Customer lock should prevent addition or deletion of customer while querying or reserving anything
//     private final ReentrantReadWriteLock customerLock = new ReentrantReadWriteLock();
 
    
//     final Socket socket;
//     final Socket server_flight;
//     final Socket server_car;
//     final Socket server_room;

//     private Vector data;
//     private String request;
//     private String message;
//     private long CID;

//     // Constructor 
//     public ClientHandler(Socket s, Socket flight, Socket car, Socket room, Vector dt) { 
//         this.socket = s; 
        
//         this.server_flight = flight;
//         this.server_car = car;
//         this.server_room = room;
//         this.data = dt;
//         this.CID = this.getId();
//     } 

//     @Override
//     public void run() {

//         while (!socket.isClosed())  {
//             try{ 
//                 //request = (String) receive(socket);
//                 System.out.println("Received Request from the client with CID: " + CID + "\n" + request);
//                 Command command = null;
                    
//                 // Packet/Convert the string request into data(Vector).
//                 //request = request.trim();
//                 //data = parse(request);

//                 if(!isCommandValidated(data)){
//                     message = "Invalid request.  The number of data provided in this command are wrong "
//                             + "or the interface does not support this command: " + request
//                             + "\nType help, <commandname> to check usage of this command.";
//                 }
//                 else {
//                     // Decide which of the commands this was and set the sending/receiving stream(socket) that the middleware requests to.
//                     // Since the middleware sever cannot call any function implemented in Severs/Client, it should only know which server/client to senda request.
//                     // Some are special cases that middleware does not have to communicate other else servers than client while processing.
//                     System.out.println("Sending a request to a specific server.");
//                     command = findCommand((String) data.elementAt(0));
//                     message = executeRequest(command, data);
//                 }

//                 // Sending a result to the client.
//                 System.out.println("Sent back a message to the Client.");
//                 send(socket, message);
//                 socket.close();

//                 // // If client sends quit command, it closes only the connection.
//                 // if(command == Command.QUIT) {  
//                 //     System.out.println("Client sends QUIT..."); 
//                 //     System.out.println("Closing this connection with the client with CID:" + CID); 
//                 //     socket.close(); 
//                 //     System.out.println("Connection is closed with the client."); 
//                 //     break; 
//                 // }
 
                
//             }
//             catch(IOException e){ 
//                 e.printStackTrace(); 
//             }

//         }
//     }

//     private Vector parse(String command) {
//         Vector data = new Vector();
//         StringTokenizer tokenizer = new StringTokenizer(command,",");
//         String argument;

//         while (tokenizer.hasMoreTokens()){
//             argument = tokenizer.nextToken();
//             argument = argument.trim();
//             data.add(argument);
//         }

//         return data;
//     }

//     private String executeRequest(Command command, Vector data) {
//         switch (command) {
//             case HELP:
//                 return getMessage(command, data);
//             case NEWFLIGHT:
//                 return addFlight(data);
//             case NEWCAR:
//                 return addCars(data);
//             case NEWROOM:
//                 return addRooms(data);
//             case NEWCUSTOMER:
//                 return newCustomer(data);
//             case DELETEFLIGHT:
//                 return deleteFlight(data);
//             case DELETECAR:
//                 return deleteCars(data);
//             case DELETEROOM:
//                 return deleteRooms(data);
//             case DELETECUSTOMER:
//                 return deleteCustomer(data);
//             case QUERYFLIGHT:
//                 return queryFlight(data);
//             case QUERYCAR:
//                 return queryCars(data);
//             case QUERYROOM:
//                 return queryRooms(data);
//             case QUERYCUSTOMER:
//                 return queryCustomerInfo(data);
//             case QUERYFLIGHTPRICE:
//                 return queryFlightPrice(data);
//             case QUERYCARPRICE:
//                 return queryCarsPrice(data);
//             case QUERYROOMPRICE:
//                 return queryRoomsPrice(data);
//             case RESERVEFLIGHT:
//                 return reserveFlight(data);
//             case RESERVECAR:
//                 return reserveCar(data);
//             case RESERVEROOM:
//                 return reserveRoom(data);
//             case ITINERARY:
//                 return itinerary(data);
//             case QUIT:
//                 return quit(data); // TODO not needed
//             case NEWCUSTOMERID:
//                 return newCustomer(data); // TODO update as needed
//             case INVALID:
//                 return "";
//         }
//         return "";
//     }

//     private boolean isCommandValidated(Vector data) {
//         String command = (String)data.elementAt(0);

//         // Validation for help
//         if (command.compareToIgnoreCase("help")==0)
//             return true;

//         // Validation for Flights
//         else if(command.compareToIgnoreCase("newflight")==0 && data.size() == 5)
//             return true;
//         else if(command.compareToIgnoreCase("deleteflight")== 0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("queryflight")== 0  && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("queryflightprice")==0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("reserveflight")==0 && data.size() == 4)
//             return true;

//         // Validation for Cars
//         else if(command.compareToIgnoreCase("newcar")==0 && data.size() == 5)
//             return true;
//         else if(command.compareToIgnoreCase("deletecar")==0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("querycar")==0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("querycarprice")== 0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("reservecar")==0 && data.size() == 4)
//             return true;

//         // Validation for Customers
//         else if(command.compareToIgnoreCase("newcustomer")== 0 && data.size() == 2)
//             return true;
//         else if(command.compareToIgnoreCase("newcustomerid")== 0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("deletecustomer")== 0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("querycustomer")== 0 && data.size() == 3)
//             return true;


//         // Validation for Rooms
//         else if(command.compareToIgnoreCase("newroom")==0 && data.size() == 5)
//             return true;
//         else if(command.compareToIgnoreCase("deleteroom")==0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("queryroom")==0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("queryroomprice")==0 && data.size() == 3)
//             return true;
//         else if(command.compareToIgnoreCase("reserveroom")==0 && data.size() == 4)
//             return true;
//         else if(command.compareToIgnoreCase("itinerary")==0 && !(data.size() < 7))
//             return true;
//         else return command.compareToIgnoreCase("quit") == 0;
//     }


//     private boolean getBoolean(Object temp) {
//         return Boolean.valueOf((String) temp);
//     }

//     private String getMessage(Command command, Vector data) {
//         String string;

//         if(data.size()==1)   //command was "help"
//             string = listCommands();
//         else if (data.size()==2)  //command was "help <commandname>"
//             string = getHelpDescription((String)data.elementAt(1));
//         else  //wrong use of help command
//             string = "Improper use of help command. Type help or help, <commandname>";

//         return string;
//     }

//     private String listCommands() {
//         String string = ("\nWelcome to the Client interface provided to test your project.\n"
//                         + "Commands accepted by the interface are:\n"
//                         + "help\n"
//                         + "newflight\nnewcar\nnewroom\nnewcustomer\nnewcusomterid\ndeleteflight\ndeletecar\ndeleteroom\n"
//                         + "deletecustomer\nqueryflight\nquerycar\nqueryroom\nquerycustomer\n"
//                         + "queryflightprice\nquerycarprice\nqueryroomprice\n"
//                         + "reserveflight\nreservecar\nreserveroom\nitinerary\n"
//                         + "quit\n"
//                         + "\ntype help, <commandname> for detailed info(NOTE the use of comma).");
//         return string;
//     }
    
//     private String addFlight(Vector data) {
//         Trace.info("MS::addFlight(" + data.toString() + ") called" );
//         System.out.println(server_flight.getPort());
//         flightLock.writeLock().lock();
//         String result = (String)send_receive(server_flight, data);
//         flightLock.writeLock().unlock();
//         return result;
//     }

//     private String addCars(Vector data) {
//         Trace.info("MS::addCars(" + data.toString() + ") called" );
//         System.out.println(server_car.getPort());
//         carLock.writeLock().lock();
//         String result = (String)send_receive(server_car, data);
//         carLock.writeLock().unlock();
//         return result;
//     }

//     private String addRooms(Vector data) {
//         Trace.info("MS::addRooms(" + data.toString() + ") called" );
//         roomLock.writeLock().lock();
//         String result = (String)send_receive(server_room, data);
//         roomLock.writeLock().unlock();
//         return result;
//     }

//     // TODO need to parse data for specific customer id (probably separate method)
//     private String newCustomer(Vector data) {
//         Trace.info("MS::newCustomer(" + data.toString() + ") called" );
//         return manageCustomer(data);
//     }

//     private String deleteFlight(Vector data) {
//         Trace.info("MS::deleteFlight(" + data.toString() + ") called");
//         flightLock.writeLock().lock();
//         String result = (String)send_receive(server_flight, data);
//         flightLock.writeLock().unlock();
//         return result;
//     }

//     private String deleteCars(Vector data) {
//         Trace.info("MS::deleteCars(" + data.toString() + ") called");
//         carLock.writeLock().lock();
//         String result = (String)send_receive(server_car, data);
//         carLock.writeLock().unlock();
//         return result;
//     }

//     private String deleteRooms(Vector data) {
//         Trace.info("MS::deleteRooms(" + data.toString() + ") called");
//         roomLock.writeLock().lock();
//         String result = (String)send_receive(server_room, data);
//         roomLock.writeLock().unlock();
//         return result;
//     }

//     private String deleteCustomer(Vector data) {
//         Trace.info("MS::deleteCustomer(" + data.toString() + ") called" );
//         return manageCustomer(data);
//     }

//     private String manageCustomer(Vector data) {
//         customerLock.writeLock().lock();
//         String result = (String)send_receive(server_flight, data) +
//                          send_receive(server_car, data) +
//                          send_receive(server_room, data);
//         customerLock.writeLock().unlock();
//         return result;
//     }

//     private String queryFlight(Vector data) {
//         Trace.info("MS::queryFlight(" + data.toString() + ") called");
//         flightLock.readLock().lock();
//         String result = (String)send_receive(server_flight, data);
//         flightLock.readLock().unlock();
//         return result;
//     }

//     private String queryCars(Vector data) {
//         Trace.info("MS::queryCars(" + data.toString() + ") called");
//         carLock.readLock().lock();
//         String result = (String)send_receive(server_car, data);
//         carLock.readLock().unlock();
//         return result;
//     }

//     private String queryRooms(Vector data) {
//         Trace.info("MS::queryRooms(" + data.toString() + ") called");
//         roomLock.readLock().lock();
//         String result = (String)send_receive(server_room, data);
//         roomLock.readLock().unlock();
//         return result;
//     }

//     private String queryCustomerInfo(Vector data) {
//         Trace.info("MS::queryCustomerInfo(" + data.toString() + ") called" );
//         String flightBill = queryCustomerInfo(data, server_flight, flightLock);
//         String carBill = queryCustomerInfo(data, server_car, carLock);
//         String roomBill = queryCustomerInfo(data, server_room, roomLock);
//         return flightBill + "\n" + carBill + "\n" + roomBill;
//     }

//     private String queryCustomerInfo(Vector data, Socket rm, ReentrantReadWriteLock lock) {
//         lock.readLock().lock();
//         String bill = (String)send_receive(rm, data);
//         lock.readLock().unlock();
//         return bill;
//     }

//     private String queryFlightPrice(Vector data) {
//         Trace.info("MS::queryFlightPrice(" + data.toString() + ") called");
//         flightLock.readLock().lock();
//         String result = (String)send_receive(server_flight, data);
//         flightLock.readLock().unlock();
//         return result;
//     }

//     private String queryCarsPrice(Vector data) {
//         Trace.info("MS::queryCarsPrice(" + data.toString() + ") called");
//         carLock.readLock().lock();
//         String result = (String)send_receive(server_car, data);
//         carLock.readLock().unlock();
//         return result;
//     }

//     private String queryRoomsPrice(Vector data) {
//         Trace.info("MS::queryRoomsPrice(" + data.toString() + ") called");
//         roomLock.readLock().lock();
//         String result = (String)send_receive(server_room, data);
//         roomLock.readLock().unlock();
//         return result;
//     }

//     private String reserveFlight(Vector data) {
//         Trace.info("MS::reserveFlight(" + data.toString() + ") called");
//         return reserveItem(data, flightLock, server_flight);
//     }

//     private String reserveCar(Vector data) {
//         Trace.info("MS::reserveCar(" + data.toString() + ") called");
//         return reserveItem(data, carLock, server_car);
//     }

//     private String reserveRoom(Vector data) {
//         Trace.info("MS::reserveRoom(" + data.toString() + ") called");
//         return reserveItem(data, roomLock, server_room);
//     }

//     private String reserveItem(Vector data, ReentrantReadWriteLock lock, Socket server) {
//         lock.writeLock().lock();
//         customerLock.writeLock().lock();
//         String result = (String)send_receive(server, data);
//         customerLock.writeLock().unlock();
//         lock.writeLock().unlock();
//         return result;
//     }

//     private String itinerary(Vector data) {
//         Trace.info("MS::itinerary(" + data.toString() + ") called");
//         Vector packet;

//         String command_str;
//         String Id = ((String)data.elementAt(1));
//         String customer = ((String)data.elementAt(2));
//         String message;

//         int offset = data.size()-6;
//         String location = ((String)data.elementAt(3+offset));
//         Object car = data.elementAt(4+offset);
//         Object room = data.elementAt(5+offset);

//         // Reserve for flights.
//         command_str = "reserveflight";
//         Vector flightNumbers = new Vector();

//         for(int i=0;i<data.size()-6;i++){
//             flightNumbers.addElement(data.elementAt(3+i));
//         }

//         Iterator it = flightNumbers.iterator();

//         message = "\nFrom Flight server: \n";
//         while(it.hasNext()){
//             Object fn = it.next();
//             String tmp = command_str + ", " + Id + ", " + customer + ", " + fn;
//             System.out.println("Send a request: " + tmp);
//             packet = parse(tmp);

//             message +=  send_receive(server_flight, packet) + "\n";
//         }

//         // Reserve for Car
//         command_str = "reservecar";
//         if(this.getBoolean(car)){
//             String tmp = command_str + ", " + Id + ", " + customer + ", " + location;
//             System.out.println("Send a request: " + tmp);
//             packet = parse(tmp);
//             message += "\nFrom Car server: \n" + send_receive(server_car, packet) + "\n";
//         }

//         // Reserve for room
//         command_str = "reserveroom";
//         if(this.getBoolean(room)){
//             String tmp = command_str + ", " + Id + ", " + customer + ", " + location;
//             System.out.println("Send a request: " + tmp);
//             packet = parse(tmp);
//             message += "\nFrom Room server: \n" + send_receive(server_room, packet) + "\n";
//         }

//         message += "\n\nItinerary Reserved\n";
//         return message;
//     }

//     private String quit(Vector data) {
//         return "From Flight server: \n" + send_receive(server_flight, data) + "\n" +
//                 "From Car server: \n" + send_receive(server_car, data) + "\n" +
//                 "From Room server: \n" + send_receive(server_room, data) + "\n";
//     }
// }
