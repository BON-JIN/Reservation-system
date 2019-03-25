// -------------------------------
// adapated from Kevin T. Manley
// CSE 593
//
package server.ResImpl;

import java.net.*; 
import java.io.*;

import server.ResInterface.*;
import util.Command;

import java.util.*;

import static util.Command.*;
import static util.Util.*;

public class ResourceManagerImpl {// implements ResourceManager {

    public static void main(String args[]) {
        Vector <ServerSocket> servers;
        String address = "localhost";
        // if (args.length != 4) {
        //     System.err.println("Wrong usage");
        //     System.out.println("Usage: java ResImpl.ResourceManagerImpl location [port1] [port2] [port3]");
        //     System.exit(1);
        // }       
        try{
            Socket socket = new Socket(address, Integer.parseInt(args[0]));       
            
            String request = "";
            request = args[1] + ", " + args[2] + ", " + args[3] + ", " + args[4];
            System.out.println(request);

            System.out.println("Updating Loadbalancer in master server.");
            String valid_message = (String) send_receive(socket, request);
            socket.close();
            System.out.println(valid_message);

            for(int i = 2; i < args.length; i++){
                ServerSocket server = new ServerSocket(Integer.parseInt(args[i]));
                Thread t = new ClientHandler(Integer.parseInt(args[i]));
                System.out.println("Server started: " + t.getName());
                t.start();
            }
        }
        catch (IOException e){
            System.out.println(e);
            error(e, "A TCP error occurred.");
        }
    }
}


class ClientHandler extends Thread{

    protected RMHashtable m_itemHT = new RMHashtable();

    // Initialize the variables
    private Vector data;
    private String message;
    private int port;

    public ClientHandler(int p){
        this.port = p;
    } 

    @Override
    public void run(){
        // Establish server

            while (true) {
                try {
                    ServerSocket server = new ServerSocket(port);
                    System.out.println("Waiting on: " + port); 
                    Socket socket = server.accept();
                    System.out.println("Client accepted: " + port);

                    Command command = null;
                    data = (Vector) receive(socket); // Read data from input stream.
                    System.out.println("Received a request from the client (middleware). :" + this.port);

                    command = findCommand((String)data.elementAt(0));

                    // Execute the command and returns result as String message
                    message = executeRequest(command, data);
                    
                    // Only message will be sent from server to middleware.
                    send(socket, message);
                    System.out.println("Message has been sent back");  
                    socket.close();
                    server.close();
            }
            catch (IOException e) {

            }   
            // close connection
            //System.out.println("Closing connection"); 
        }

    }



    public int getInt(Object temp) throws Exception {
        try {
            return (new Integer((String)temp)).intValue();
        }
        catch(Exception e) {
            throw e;
        }
    }

    public boolean getBoolean(Object temp) throws Exception {
        try {
            return (new Boolean((String)temp)).booleanValue();
        }
        catch(Exception e) {
            throw e;
        }
    }

    public String getString(Object temp) throws Exception {
        try {
            return (String)temp;
        }
        catch (Exception e) {
            throw e;
        }
    }

    public String executeRequest(Command command, Vector data) {
        String message = "Request failed.";
        try {
            int id = 0;
            int flightNum, flightSeats, flightPrice, numCars, numRooms, price, customer;
            if (data.size() > 1) {
                id = this.getInt(data.elementAt(1));
            }
            String location;
            switch (command) {
                case NEWFLIGHT:
                    flightNum = this.getInt(data.elementAt(2));
                    flightSeats = this.getInt(data.elementAt(3));
                    flightPrice = this.getInt(data.elementAt(4));

                    if (this.addFlight(id, flightNum, flightSeats, flightPrice)) {
                        System.out.println("Flight added");

                        message = "Added a new Flight using id: " + id + "\n"
                                + "Flight number: " + flightNum + "\n"
                                + "Add Flight Seats: " + flightSeats + "\n"
                                + "Set Flight Price: " + flightPrice + "\n";
                    } else
                        message = "Flight could not be added";
                    break;

                case DELETEFLIGHT: //delete Flight
                    flightNum = this.getInt(data.elementAt(2));

                    if (this.deleteFlight(id, flightNum)) {
                        System.out.println("Flight Deleted");

                        message = "Deleted a flight using id: " + id + "\n"
                                + "Flight Number: " + flightNum;

                    } else
                        message = "Flight could not be deleted";
                    break;

                case QUERYFLIGHT: //querying a flight
                    flightNum = this.getInt(data.elementAt(2));

                    int seats = this.queryFlight(id, flightNum);
                    message = "Querying a flight using id: " + id + "\n"
                            + "Flight number: " + flightNum + "\n"
                            + "Number of seats available:" + seats;
                    break;

                case QUERYFLIGHTPRICE: //querying a flight Price
                    flightNum = this.getInt(data.elementAt(2));
                    flightPrice = this.queryFlightPrice(id, flightNum);

                    message = "Querying a flight Price using id: " + id + "\n"
                            + "Flight number: " + flightNum + "\n"
                            + "Price of a seat:" + flightPrice;
                    break;

                case RESERVEFLIGHT:  //reserve a flight
                    customer = this.getInt(data.elementAt(2));
                    flightNum = this.getInt(data.elementAt(3));

                    if (this.reserveFlight(id, customer, flightNum))
                        message = "Reserving a seat on a flight using id: " + id + "\n"
                                + "Customer id: " + customer + "\n"
                                + "Flight number: " + flightNum + "\n"
                                + "Flight Reserved";
                    else
                        message = "Flight could not be reserved.";
                    break;

                case NEWCAR:  //new Car
                    location = this.getString(data.elementAt(2));
                    numCars = this.getInt(data.elementAt(3));
                    price = this.getInt(data.elementAt(4));

                    if (this.addCars(id, location, numCars, price)) {
                        message = "Adding a new Car using id: " + id + "\n"
                                + "Car Location: " + location + "\n"
                                + "Add Number of Cars: " + numCars + "\n"
                                + "Set Price: " + price + "\n"
                                + "Cars added";
                    } else
                        message = "Cars could not be added";
                    break;

                case DELETECAR: //delete Car
                    location = this.getString(data.elementAt(2));

                    if (this.deleteCars(id, location)) {
                        message = "Deleting the cars from a particular location  using id: " + id + "\n"
                                + "Car Location: " + location + "\n"
                                + "Cars Deleted";
                    } else
                        message = "Cars could not be deleted";
                    break;


                case QUERYCAR: //querying a Car Location
                    location = this.getString(data.elementAt(2));
                    numCars = this.queryCars(id, location);

                    message = "Querying a car location using id: " + id + "\n"
                            + "Car location: " + location + "\n"
                            + "number of Cars at this location:" + numCars + "\n";
                    break;


                case QUERYCARPRICE: //querying a Car Price
                    location = this.getString(data.elementAt(2));
                    price = this.queryCarsPrice(id, location);

                    message = "Querying a car price using id: " + id + "\n"
                            + "Car location: " + location + "\n"
                            + "Price of a car at this location:" + price;
                    break;

                case RESERVECAR:  //reserve a car
                    customer = this.getInt(data.elementAt(2));
                    location = this.getString(data.elementAt(3));

                    if (this.reserveCar(id, customer, location)) {
                        message = "Reserving a car at a location using id: " + id + "\n"
                                + "Customer id: " + customer + "\n"
                                + "Location: " + location + "\n"
                                + "Car Reserved";
                    } else
                        message = "Car could not be reserved.";
                    break;


                case NEWROOM:  //new Room
                    location = this.getString(data.elementAt(2));
                    numRooms = this.getInt(data.elementAt(3));
                    price = this.getInt(data.elementAt(4));

                    if (this.addRooms(id, location, numRooms, price)) {
                        message = "Adding a new Room using id: " + id + "\n"
                                + "Room Location: " + location + "\n"
                                + "Add Number of Rooms: " + numRooms + "\n"
                                + "Set Price: " + price + "\n"
                                + "Rooms added";
                    } else
                        message = "Rooms could not be added";
                    break;

                case DELETEROOM: //delete Room;
                    location = this.getString(data.elementAt(2));
                    if (this.deleteRooms(id, location)) {
                        message = "Deleting all rooms from a particular location  using id: " + id + "\n"
                                + "Room Location: " + location + "\n"
                                + "Rooms Deleted";
                    } else
                        message = "Rooms could not be deleted";

                    break;


                case QUERYROOM: //querying a Room location
                    location = this.getString(data.elementAt(2));
                    numRooms = this.queryRooms(id, location);

                    message = "Querying a room location using id: " + id + "\n"
                            + "Room location: " + location + "\n"
                            + "number of Rooms at this location:" + numRooms;
                    break;


                case QUERYROOMPRICE: //querying a Room price
                    location = this.getString(data.elementAt(2));
                    price = this.queryRoomsPrice(id, location);

                    message = "Querying a room price using id: " + id + "\n"
                            + "Room Location: " + location + "\n"
                            + "Price of Rooms at this location:" + price;


                    System.out.println();
                    break;

                case RESERVEROOM:  //reserve a room
                    customer = this.getInt(data.elementAt(2));
                    location = this.getString(data.elementAt(3));

                    if (this.reserveRoom(id, customer, location)) {
                        message = "Reserving a room at a location using id: " + id + "\n"
                                + "Customer id: " + customer + "\n"
                                + "Location: " + location + "\n"
                                + "Room Reserved";
                    } else
                        message = "Room could not be reserved.";
                    break;


                // Purpose: Get the system to provide a new customer id (same as adding a new customer)
                case NEWCUSTOMER:  //new Customer
                    customer = this.newCustomer(id);

                    message = "Adding a new Customer using id:" + id + "\n"
                            + "new customer id:" + customer;
                    break;

                // Purpose: creates a new customer with id provided as input
                case NEWCUSTOMERID:  //new Customer given id
                    int Cid = this.getInt(data.elementAt(2));
                    boolean result = this.newCustomer(id, Cid);
                    message = "Adding a new Customer using id:" + id + " and cid " + Cid + "\n"
                            + "new customer id:" + Cid;
                    break;

                case QUERYCUSTOMER: //querying Customer Information
                    customer = this.getInt(data.elementAt(2));
                    String bill = this.queryCustomerInfo(id, customer);
                    message = "Querying Customer information using id: " + id + "\n"
                            + "Customer id:" + customer + "\n"
                            + "Customer info:" + bill;
                    break;

                case DELETECUSTOMER: //delete Customer
                    customer = this.getInt(data.elementAt(2));

                    if (this.deleteCustomer(id, customer))
                        message = "Deleting a customer from the database using id: " + data.elementAt(1) + "\n"
                                + "Customer id: " + data.elementAt(2) + "\n"
                                + "Customer Deleted";
                    else
                        message = "Customer could not be deleted";
                    break;

                case QUIT:  //reserve a room
                    message = "Thanks for using our service.";
                    break;
            }
        } catch (Exception e) {
            error(e, e.getMessage());
        }
        return message;
    }

    public boolean itinerary(int id, int customer, Vector flightNumbers, String location, boolean Car, boolean Room) {
        return true;
    }

    // Reads a data item
    private RMItem readData( int id, String key )
    {
        synchronized(m_itemHT){
            return (RMItem) m_itemHT.get(key);
        }
    }

    // Writes a data item
    private void writeData( int id, String key, RMItem value )
    {
        synchronized(m_itemHT){
            m_itemHT.put(key, value);
        }
    }

    // Remove the item out of storage
    protected RMItem removeData(int id, String key){
        synchronized(m_itemHT){
            return (RMItem)m_itemHT.remove(key);
        }
    }


    // deletes the entire item
    protected boolean deleteItem(int id, String key)
    {
        Trace.info("RM::deleteItem(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key );
        // Check if there is such an item in the storage
        if( curObj == null ) {
            Trace.warn("RM::deleteItem(" + id + ", " + key + ") failed--item doesn't exist" );
            return false;
        } else {
            if(curObj.getReserved()==0){
                removeData(id, curObj.getKey());
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item deleted" );
                return true;
            }
            else{
                Trace.info("RM::deleteItem(" + id + ", " + key + ") item can't be deleted because some customers reserved it" );
                return false;
            }
        } // if
    }


    // query the number of available seats/rooms/cars
    protected int queryNum(int id, String key) {
        Trace.info("RM::queryNum(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0;
        if( curObj != null ) {
            value = curObj.getCount();
        } // else
        Trace.info("RM::queryNum(" + id + ", " + key + ") returns count=" + value);
        return value;
    }

    // query the price of an item
    protected int queryPrice(int id, String key){
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") called" );
        ReservableItem curObj = (ReservableItem) readData( id, key);
        int value = 0;
        if( curObj != null ) {
            value = curObj.getPrice();
        } // else
        Trace.info("RM::queryCarsPrice(" + id + ", " + key + ") returns cost=$" + value );
        return value;
    }

    // reserve an item
    protected boolean reserveItem(int id, int customerID, String key, String location){
        Trace.info("RM::reserveItem( " + id + ", customer=" + customerID + ", " +key+ ", "+location+" ) called" );
        // Read customer object if it exists (and read lock it)
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            Trace.warn("RM::reserveCar( " + id + ", " + customerID + ", " + key + ", "+location+")  failed--customer doesn't exist" );
            return false;
        }

        // check if the item is available
        ReservableItem item = (ReservableItem)readData(id, key);
        if(item==null){
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " +location+") failed--item doesn't exist" );
            return false;
        }else if(item.getCount()==0){
            Trace.warn("RM::reserveItem( " + id + ", " + customerID + ", " + key+", " + location+") failed--No more items" );
            return false;
        }else{
            cust.reserve( key, location, item.getPrice());
            writeData( id, cust.getKey(), cust );

            // decrease the number of available items in the storage
            item.setCount(item.getCount() - 1);
            item.setReserved(item.getReserved()+1);

            Trace.info("RM::reserveItem( " + id + ", " + customerID + ", " + key + ", " +location+") succeeded" );
            return true;
        }
    }

    // Create a new flight, or add seats to existing flight
    //  NOTE: if flightPrice <= 0 and the flight already exists, it maintains its current price
    public boolean addFlight(int id, int flightNum, int flightSeats, int flightPrice)
    {
        Trace.info("RM::addFlight(" + id + ", " + flightNum + ", $" + flightPrice + ", " + flightSeats + ") called" );
        Flight curObj = (Flight) readData( id, Flight.getKey(flightNum) );
        if( curObj == null ) {
            // doesn't exist...add it
            Flight newObj = new Flight( flightNum, flightSeats, flightPrice );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addFlight(" + id + ") created new flight " + flightNum + ", seats=" +
                    flightSeats + ", price=$" + flightPrice );
        } else {
            // add seats to existing flight and update the price...
            curObj.setCount( curObj.getCount() + flightSeats );
            if( flightPrice > 0 ) {
                curObj.setPrice( flightPrice );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addFlight(" + id + ") modified existing flight " + flightNum + ", seats=" + curObj.getCount() + ", price=$" + flightPrice );
        } // else
        return(true);
    }

    public boolean deleteFlight(int id, int flightNum)
    {
        return deleteItem(id, Flight.getKey(flightNum));
    }

    // Create a new room location or add rooms to an existing location
    //  NOTE: if price <= 0 and the room location already exists, it maintains its current price
    public boolean addRooms(int id, String location, int count, int price)
    {
        Trace.info("RM::addRooms(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Hotel curObj = (Hotel) readData( id, Hotel.getKey(location) );
        if( curObj == null ) {
            // doesn't exist...add it
            Hotel newObj = new Hotel( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addRooms(" + id + ") created new room location " + location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing object and update price...
            curObj.setCount( curObj.getCount() + count );
            if( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addRooms(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return(true);
    }


    // Delete rooms from a location
    public boolean deleteRooms(int id, String location)
    {
        return deleteItem(id, Hotel.getKey(location));
    }


    // Create a new car location or add cars to an existing location
    //  NOTE: if price <= 0 and the location already exists, it maintains its current price
    public boolean addCars(int id, String location, int count, int price)
    {
        Trace.info("RM::addCars(" + id + ", " + location + ", " + count + ", $" + price + ") called" );
        Car curObj = (Car) readData( id, Car.getKey(location) );
        if( curObj == null ) {
            // car location doesn't exist...add it
            Car newObj = new Car( location, count, price );
            writeData( id, newObj.getKey(), newObj );
            Trace.info("RM::addCars(" + id + ") created new location " + location + ", count=" + count + ", price=$" + price );
        } else {
            // add count to existing car location and update price...
            curObj.setCount( curObj.getCount() + count );
            if( price > 0 ) {
                curObj.setPrice( price );
            } // if
            writeData( id, curObj.getKey(), curObj );
            Trace.info("RM::addCars(" + id + ") modified existing location " + location + ", count=" + curObj.getCount() + ", price=$" + price );
        } // else
        return(true);
    }


    // Delete cars from a location
    public boolean deleteCars(int id, String location)
    {
        return deleteItem(id, Car.getKey(location));
    }


    // Returns the number of empty seats on this flight
    public int queryFlight(int id, int flightNum)
    {
        return queryNum(id, Flight.getKey(flightNum));
    }

    // Returns price of this flight
    public int queryFlightPrice(int id, int flightNum )
    {
        return queryPrice(id, Flight.getKey(flightNum));
    }


    // Returns the number of rooms available at a location
    public int queryRooms(int id, String location)
    {
        return queryNum(id, Hotel.getKey(location));
    }


    // Returns room price at this location
    public int queryRoomsPrice(int id, String location)
    {
        return queryPrice(id, Hotel.getKey(location));
    }


    // Returns the number of cars available at a location
    public int queryCars(int id, String location)
    {
        return queryNum(id, Car.getKey(location));
    }


    // Returns price of cars at this location
    public int queryCarsPrice(int id, String location)
    {
        return queryPrice(id, Car.getKey(location));
    }

    // Returns data structure containing customer reservation info. Returns null if the
    //  customer doesn't exist. Returns empty RMHashtable if customer exists but has no
    //  reservations.
    public RMHashtable getCustomerReservations(int id, int customerID)
    {
        Trace.info("RM::getCustomerReservations(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            Trace.warn("RM::getCustomerReservations failed(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return null;
        } else {
            return cust.getReservations();
        } // if
    }


    // return a bill
    public String queryCustomerInfo(int id, int customerID)
    {
        Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            Trace.warn("RM::queryCustomerInfo(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return "";   // NOTE: don't change this--WC counts on this value indicating a customer does not exist...
        } else {
            String s = cust.printBill();
            Trace.info("RM::queryCustomerInfo(" + id + ", " + customerID + "), bill follows..." );
            System.out.println( s );
            return s;
        } // if
    }


    // customer functions
    // new customer just returns a unique customer identifier
    public int newCustomer(int id)
    {
        Trace.info("INFO: RM::newCustomer(" + id + ") called" );
        // Generate a globally unique ID for the new customer
        int cid = Integer.parseInt( String.valueOf(id) +
                String.valueOf(Calendar.getInstance().get(Calendar.MILLISECOND)) +
                String.valueOf( Math.round( Math.random() * 100 + 1 )));
        Customer cust = new Customer( cid );
        writeData( id, cust.getKey(), cust );
        Trace.info("RM::newCustomer(" + id + ") returns ID=" + cid );
        return cid;
    }


    // I opted to pass in customerID instead. This makes testing easier
    public boolean newCustomer(int id, int customerID )
    {
        Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            cust = new Customer(customerID);
            writeData( id, cust.getKey(), cust );
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") created a new customer" );
            return true;
        } else {
            Trace.info("INFO: RM::newCustomer(" + id + ", " + customerID + ") failed--customer already exists");
            return false;
        } // else
    }


    // Deletes customer from the database.
    public boolean deleteCustomer(int id, int customerID)
    {
        Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") called" );
        Customer cust = (Customer) readData( id, Customer.getKey(customerID) );
        if( cust == null ) {
            Trace.warn("RM::deleteCustomer(" + id + ", " + customerID + ") failed--customer doesn't exist" );
            return false;
        } else {
            // Increase the reserved numbers of all reservable items which the customer reserved.
            RMHashtable reservationHT = cust.getReservations();
            for(Enumeration e = reservationHT.keys(); e.hasMoreElements();){
                String reservedkey = (String) (e.nextElement());
                ReservedItem reserveditem = cust.getReservedItem(reservedkey);
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + " " +  reserveditem.getCount() +  " times"  );
                ReservableItem item  = (ReservableItem) readData(id, reserveditem.getKey());
                Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") has reserved " + reserveditem.getKey() + "which is reserved" +  item.getReserved() +  " times and is still available " + item.getCount() + " times"  );
                item.setReserved(item.getReserved()-reserveditem.getCount());
                item.setCount(item.getCount()+reserveditem.getCount());
            }

            // remove the customer from the storage
            removeData(id, cust.getKey());

            Trace.info("RM::deleteCustomer(" + id + ", " + customerID + ") succeeded" );
            return true;
        } // if
    }

    // Adds car reservation to this customer.
    public boolean reserveCar(int id, int customerID, String location)
    {
        return reserveItem(id, customerID, Car.getKey(location), location);
    }


    // Adds room reservation to this customer.
    public boolean reserveRoom(int id, int customerID, String location)
    {
        return reserveItem(id, customerID, Hotel.getKey(location), location);
    }


    // Adds flight reservation to this customer.
    public boolean reserveFlight(int id, int customerID, int flightNum)
    {
        return reserveItem(id, customerID, Flight.getKey(flightNum), String.valueOf(flightNum));
    }
}




