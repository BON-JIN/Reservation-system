package util;

public enum Command {
    HELP,
    NEWFLIGHT,
    NEWCAR,
    NEWROOM,
    NEWCUSTOMER,
    DELETEFLIGHT,
    DELETECAR,
    DELETEROOM,
    DELETECUSTOMER,
    QUERYFLIGHT,
    QUERYCAR,
    QUERYROOM,
    QUERYCUSTOMER,
    QUERYFLIGHTPRICE,
    QUERYCARPRICE,
    QUERYROOMPRICE,
    RESERVEFLIGHT,
    RESERVECAR,
    RESERVEROOM,
    ITINERARY,
    QUIT,
    NEWCUSTOMERID,
    INVALID;


    public static Command findCommand(String argument) {
        if (argument.compareToIgnoreCase("help")==0)
            return Command.HELP;
        else if(argument.compareToIgnoreCase("newflight")==0)
            return Command.NEWFLIGHT;
        else if(argument.compareToIgnoreCase("newcar")==0)
            return Command.NEWCAR;
        else if(argument.compareToIgnoreCase("newroom")==0)
            return Command.NEWROOM;
        else if(argument.compareToIgnoreCase("newcustomer")==0)
            return Command.NEWCUSTOMER;
        else if(argument.compareToIgnoreCase("deleteflight")==0)
            return Command.DELETEFLIGHT;
        else if(argument.compareToIgnoreCase("deletecar")==0)
            return Command.DELETECAR;
        else if(argument.compareToIgnoreCase("deleteroom")==0)
            return Command.DELETEROOM;
        else if(argument.compareToIgnoreCase("deletecustomer")==0)
            return Command.DELETECUSTOMER;
        else if(argument.compareToIgnoreCase("queryflight")==0)
            return Command.QUERYFLIGHT;
        else if(argument.compareToIgnoreCase("querycar")==0)
            return Command.QUERYCAR;
        else if(argument.compareToIgnoreCase("queryroom")==0)
            return Command.QUERYROOM;
        else if(argument.compareToIgnoreCase("querycustomer")==0)
            return Command.QUERYCUSTOMER;
        else if(argument.compareToIgnoreCase("queryflightprice")==0)
            return Command.QUERYFLIGHTPRICE;
        else if(argument.compareToIgnoreCase("querycarprice")==0)
            return Command.QUERYCARPRICE;
        else if(argument.compareToIgnoreCase("queryroomprice")==0)
            return Command.QUERYROOMPRICE;
        else if(argument.compareToIgnoreCase("reserveflight")==0)
            return Command.RESERVEFLIGHT;
        else if(argument.compareToIgnoreCase("reservecar")==0)
            return Command.RESERVECAR;
        else if(argument.compareToIgnoreCase("reserveroom")==0)
            return Command.RESERVEROOM;
        else if(argument.compareToIgnoreCase("itinerary")==0)
            return Command.ITINERARY;
        else if (argument.compareToIgnoreCase("quit")==0)
            return Command.QUIT;
        else if (argument.compareToIgnoreCase("newcustomerid")==0)
            return Command.NEWCUSTOMERID;
        else
            return Command.INVALID;
    }


    public static String getHelpDescription(String command) {
        String string = "\nHelp on: ";

        switch(findCommand(command)) {
            case HELP:
                string = string + "Help\n"
                        + "Typing help on the prompt gives a list of all the commands available.\n"
                        + "Typing help, <commandname> gives details on how to use the particular command.";
                break;
            case NEWFLIGHT:  //new flight
                string = string + "Adding a new Flight.\n"
                        + "Purpose:\n"
                        + "\tAdd information about a new flight.\n"
                        + "\nUsage:\n"
                        + "\tnewflight,<id>,<flightnumber>,<flightSeats>,<flightprice>";
                break;
            case NEWCAR:  //new Car
                string = string + "Adding a new Car.\n"
                        + "Purpose:\n"
                        + "\tAdd information about a new car location.\n"
                        + "\nUsage:\n"
                        + "\tnewcar,<id>,<location>,<numberofcars>,<pricepercar>";
                break;
            case NEWROOM:  //new Room
                string = string + "Adding a new Room.\n"
                        + "Purpose:\n"
                        + "\tAdd information about a new room location.\n"
                        + "\nUsage:\n"
                        + "\tnewroom,<id>,<location>,<numberofrooms>,<priceperroom>\n";
                break;
            case DELETEFLIGHT:  //new Customer
                string = string + "Adding a new Customer.\n"
                        + "Purpose:\n"
                        + "\tGet the system to provide a new customer id. (same as adding a new customer)\n"
                        + "\nUsage:\n"
                        + "\tnewcustomer,<id>\n";
                break;
            case NEWCUSTOMER: //delete Flight
                string = string + "Deleting a flight\n"
                        + "Purpose:\n"
                        + "\tDelete a flight's information.\n"
                        + "\nUsage:\n"
                        + "\tdeleteflight,<id>,<flightnumber>\n";
                break;
            case DELETECAR: //delete Car
                string = string + "Deleting a Car\n"
                        + "Purpose:\n"
                        + "\tDelete all cars from a location.\n"
                        + "\nUsage:\n"
                        + "\tdeletecar,<id>,<location>,<numCars>\n";
                break;
            case DELETEROOM: //delete Room
                string = string + "Deleting a Room\n"
                        + "\nPurpose:\n"
                        + "\tDelete all rooms from a location.\n"
                        + "Usage:"
                        + "\tdeleteroom,<id>,<location>,<numRooms>\n";
                break;
            case DELETECUSTOMER: //delete Customer
                string = string + "Deleting a Customer\n"
                        + "Purpose:\n"
                        + "\tRemove a customer from the database.\n"
                        + "\nUsage:\n"
                        + "\tdeletecustomer,<id>,<customerid>\n";
                break;
            case QUERYFLIGHT: //querying a flight
                string = string + "Querying flight.\n"
                        + "Purpose:\n"
                        + "\tObtain Seat information about a certain flight.\n"
                        + "\nUsage:\n"
                        + "\tqueryflight,<id>,<flightnumber>\n";
                break;
            case QUERYCAR: //querying a Car Location
                string = string + "Querying a Car location.\n"
                        + "Purpose:\n"
                        + "\tObtain number of cars at a certain car location.\n"
                        + "\nUsage:\n"
                        + "\tquerycar,<id>,<location>\n";
                break;
            case QUERYROOM: //querying a Room location
                string = string + "Querying a Room Location.\n"
                        + "Purpose:\n"
                        + "\tObtain number of rooms at a certain room location.\n"
                        + "\nUsage:\n"
                        + "\tqueryroom,<id>,<location>\n";
                break;
            case QUERYCUSTOMER: //querying Customer Information
                string = string + "Querying Customer Information.\n"
                        + "Purpose:\n"
                        + "\tObtain information about a customer.\n"
                        + "\nUsage:\n"
                        + "\tquerycustomer,<id>,<customerid>\n";
                break;
            case QUERYFLIGHTPRICE: //querying a flight for price
                string = string + "Querying flight.\n"
                        + "Purpose:\n"
                        + "\tObtain price information about a certain flight.\n"
                        + "\nUsage:\n"
                        + "\tqueryflightprice,<id>,<flightnumber>\n";
                break;
            case QUERYCARPRICE: //querying a Car Location for price
                string = string + "Querying a Car location.\n"
                        + "Purpose:\n"
                        + "\tObtain price information about a certain car location.\n"
                        + "\nUsage:\n"
                        + "\tquerycarprice,<id>,<location>\n";
                break;
            case QUERYROOMPRICE: //querying a Room location for price
                string = string + "Querying a Room Location.\n"
                        + "Purpose:\n"
                        + "\tObtain price information about a certain room location.\n"
                        + "\nUsage:\n"
                        + "\tqueryroomprice,<id>,<location>\n";
                break;
            case RESERVEFLIGHT:  //reserve a flight
                string = string + "Reserving a flight.\n"
                        + "Purpose:\n"
                        + "\tReserve a flight for a customer.\n"
                        + "\nUsage:\n"
                        + "\treserveflight,<id>,<customerid>,<flightnumber>\n";
                break;
            case RESERVECAR:  //reserve a car
                string = string + "Reserving a Car.\n"
                        + "Purpose:\n"
                        + "\tReserve a given number of cars for a customer at a particular location.\n"
                        + "\nUsage:\n"
                        + "\treservecar,<id>,<customerid>,<location>,<nummberofCars>\n";
                break;
            case RESERVEROOM:  //reserve a room
                string = string + "Reserving a Room.\n"
                        + "Purpose:\n"
                        + "\tReserve a given number of rooms for a customer at a particular location.\n"
                        + "\nUsage:\n"
                        + "\treserveroom,<id>,<customerid>,<location>,<nummberofRooms>\n";
                break;
            case ITINERARY:  //reserve an Itinerary
                string = string + "Reserving an Itinerary.\n"
                        + "Purpose:\n"
                        + "\tBook one or more flights.Also book zero or more cars/rooms at a location.\n"
                        + "\nUsage:\n"
                        + "\titinerary,<id>,<customerid>,<flightnumber1>....<flightnumberN>,<LocationToBookCarsOrRooms>,<NumberOfCars>,<NumberOfRoom>\n";
                break;
            case QUIT:  //quit the Client
                string = string + "Quitting Client.\n"
                        + "Purpose:\n"
                        + "\tExit the Client application.\n"
                        + "\nUsage:\n"
                        + "\tquit\n";
                break;
            case NEWCUSTOMERID:  //new customer with id
                string = string + "Create new customer providing an id\n"
                        + "Purpose:\n"
                        + "\tCreates a new customer with the id provided\n"
                        + "\nUsage:\n"
                        + "\tnewcustomerid, <id>, <customerid>\n";
                break;
            case INVALID:
                string = "The interface does not support this command.\n";
                break;
        }

        return string;
    }
}
