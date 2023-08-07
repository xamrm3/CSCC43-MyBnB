package main;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MyBnB {
  
  private static final String className = "com.mysql.cj.jdbc.Driver";
  private static final String CONNECTION = "jdbc:mysql://127.0.0.1/MyBnB";
  private static final String USER = "root";
  private static final String PASS = "12345";
  static Scanner scan = new Scanner(System.in);
  static List<String> amenitiesList = new ArrayList<String>();  
  
  public static void main(String[] args) throws ClassNotFoundException{
    
    populateList();
    Class.forName(className);
    Connection conn = null;
    System.out.println("Connecting to database...");
    
    try {
      conn = DriverManager.getConnection(CONNECTION, USER, PASS);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    System.out.println("Successfully connected to MySQL!");
    
    
    System.out.println("Select what part to interact with: ");
    System.out.println("1. Operations");
    System.out.println("2. Queries");
    System.out.println("3. Reports");
    int part = userInput(Integer.class);
    
    if (part == 1) {
      operations(conn);
    }
    else if (part == 2) {
      showOpenListings(conn, true);
    }
    else if (part == 3) {
      reports(conn);
    }
    
    System.out.println("Bye");
  }
  
  // populate amenitiesList
  private static void populateList() {
    amenitiesList.add("bed");
    amenitiesList.add("tv");
    amenitiesList.add("bath");
    amenitiesList.add("coffee maker");
    amenitiesList.add("wifi");
    amenitiesList.add("pool");

  }
  
  private static void operations(Connection conn) {
    
    int Sin = -1;
    int choice = 1000;
    boolean host = false;
    System.out.println("Do you want to log in (l) or create an account (c)? ");
    String lc = scan.nextLine();
    
    if (lc.equals("c")) {
      Sin = createUser(conn);
      if (Sin < 0) return;
    }
    else {
      System.out.print("Please enter your Sin: ");
      Sin = userInput(Integer.class);
    }
    
    host = isHost(conn, Sin);
    
    // relevant functions for host
    if (host) {
      while (choice > 0) {
        System.out.println("Enter a number to do that option, 0 to end");
        System.out.println("1. Delete user");
        System.out.println("2. Show your owned listings");
        System.out.println("3. Create a listing");
        System.out.println("4. Add a listing to the calendar");
        System.out.println("5. Add amenities to a listing");
        System.out.println("6. Cancel a booking");
        System.out.println("7. Update a booking price");
        System.out.println("8. Remove a listing");
        System.out.println("9. Rate a user");
        choice = userInput(Integer.class);
        
        if (choice == 1) {
          deleteUser(conn, Sin);
          break;
        }
        else if (choice == 2) showOwnedListings(conn, Sin);
        else if (choice == 3) createListing(conn, Sin);
        else if (choice == 4) addCalendar(conn, -1, Sin, true);
        else if (choice == 5) addAmenities(conn, -1, Sin, true);
        else if (choice == 6) cancelBookingHost(conn, Sin);
        else if (choice == 7) updatePrice(conn, Sin);
        else if (choice == 8) removeListing(conn, Sin);
        else if (choice == 9) rateUser(conn, Sin, true);
      }
    }
    
    //relevant functions for renter
    else {
      while (choice > 0) {
        System.out.println("Enter a number to do that option, 0 to end");
        System.out.println("1. Delete user");
        System.out.println("2. Show your rented listings");
        System.out.println("3. Rent a listing");
        System.out.println("4. Cancel a booking");
        System.out.println("5. Rate a listing");
        System.out.println("6. Rate a user");
        choice = userInput(Integer.class);

        if (choice == 1) {
          deleteUser(conn, Sin);
          break;
        }
        else if (choice == 2) showRentedListings(conn, Sin);
        else if (choice == 3) bookListing(conn, Sin);
        else if (choice == 4) cancelBookingRenter(conn, Sin);
        else if (choice == 5) rateRentedListings(conn, Sin);
        else if (choice == 6) rateUser(conn, Sin, false);
      }
    }
    
  }
  
  private static void reports(Connection conn) {
    
    int choice = 1000;
    while (choice > 0) {
      System.out.println("Enter a number to run that report, 0 to end");
      System.out.println("1. Report bookings");
      System.out.println("2. Report listings");
      System.out.println("3. Report host rank");
      System.out.println("4. Report commercial hosts");
      System.out.println("5. Report renter rank");
      System.out.println("6. Report cancellations");
      System.out.println("7. Report nouns");
      choice = userInput(Integer.class);
      
      if (choice == 1) reportBookings(conn);
      else if (choice == 2) reportListings(conn);
      else if (choice == 3) reportHostRank(conn);
      else if (choice == 4) reportCommercialHost(conn);
      else if (choice == 5) reportRenterRank(conn);
      else if (choice == 6) reportCancellations(conn);
      else if (choice == 7) reportNouns(conn);
    }
  }
  
  private static <T> T userInput(Class<T> type) {
    while (true) {
      if (type == Integer.class) {
        if (scan.hasNextInt()) {
          int num = scan.nextInt();
          scan.nextLine();
          return type.cast(num);
        }
        else {
          scan.nextLine();
          System.out.print("Please enter a number: ");
        }
      }
      else {
        if (scan.hasNextDouble()) {
          double num = scan.nextDouble();
          scan.nextLine();
          return type.cast(num);
        }
        else {
          scan.nextLine();
          System.out.print("Please enter a number: ");
        }
      }
    }
  }
  
  // checks if the user is a host
  private static boolean isHost(Connection conn, int Sin) {
    
    try {
      PreparedStatement hosts = conn.prepareStatement("select Sin from host where Sin=?");
      hosts.setInt(1, Sin);
      ResultSet rs = hosts.executeQuery();
      return rs.next();
      
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
    
    return false;
  }
  
  // create a new user
  private static int createUser(Connection conn) {
    
    int Sin = -1;
    
    try {
      PreparedStatement newUser = conn.prepareStatement("insert into user (Sin, name, age, job, address) values (?, ?, ?, ?, ?)");
      
      System.out.print("Enter your social insurence number: ");
      Sin = userInput(Integer.class);
      newUser.setInt(1, Sin);
      
      System.out.print("Enter your name: ");
      newUser.setString(2,  scan.nextLine());
      
      System.out.print("Enter your age: ");
      int age = userInput(Integer.class);
      if (age < 18) {
        System.out.println("Sorry, you cannot make an account");
        return -1;
      }
      newUser.setInt(3, age);

      System.out.print("Enter your job: ");
      newUser.setString(4, scan.nextLine());
      
      System.out.print("Enter your address: ");
      newUser.setString(5,  scan.nextLine());
      
      newUser.executeUpdate();
      
      String RorH = "";
      while (!(RorH.equals("r") || RorH.equals("h"))) {
        System.out.print("Are you a renter (r) or a host (h)?: ");
        RorH = scan.nextLine();
      }
      
      if (RorH.equals("r")) {
        PreparedStatement newRenter = conn.prepareStatement("insert into renter values (?, ?)");
        newRenter.setInt(1,  Sin);
        
        System.out.print("Enter your credit card number: ");
        int credit = userInput(Integer.class);
        newRenter.setInt(2,  credit);
        newRenter.executeUpdate();
      }
      else {
        PreparedStatement newHost = conn.prepareStatement("insert into host values (?)");
        newHost.setInt(1,  Sin);
        newHost.executeUpdate();
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
    
    System.out.println("Welcome to MyBnB!");
    return Sin;
  }
  
  // delete an existing user
  private static void deleteUser(Connection conn, int Sin) {

    try {
      PreparedStatement getLids = conn.prepareStatement("select l.Lid from listing l join owns o on l.Lid=o.Lid where Sin=?");
      getLids.setInt(1, Sin);
      ResultSet rs = getLids.executeQuery();
      
      while(rs.next()) {
        int Lid = rs.getInt("Lid");
        PreparedStatement deleteCalendar = conn.prepareStatement("delete c from calendar c join availability a on a.Crid=c.Crid where Lid=?");
        deleteCalendar.setInt(1, Lid);
        deleteCalendar.executeUpdate();
      }
      
      PreparedStatement deleteListing = conn.prepareStatement("delete l from listing l join owns o on l.Lid=o.Lid where Sin=?");
      deleteListing.setInt(1, Sin);
      deleteListing.executeUpdate();
    
      PreparedStatement deletedUser = conn.prepareStatement("delete from user where Sin = ?");
      deletedUser.setInt(1, Sin);
      deletedUser.executeUpdate();
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
    System.out.println("Deleted");
  }
  
  // add amenities to a listing
  private static int addAmenities(Connection conn, int Lid, int hostId, boolean selectListing) {
    
    int count = 0;
    
    if (selectListing && showOwnedListings(conn, hostId)) {
      System.out.print("Please enter the listing code of the listing you wish to add amenities too: ");
      Lid = userInput(Integer.class);
    }
    
    String check = "";
    System.out.println("Please note that the following amenities can result in $5 more for each day compared to not having it!");
    System.out.println(amenitiesList);
    while (!check.equals("DONE")) {
      
      String amenity = "";
      String desc = "";
      
      System.out.print("Please enter the amenity to add: ");
      amenity = scan.nextLine();
      System.out.println("Please add a short description for "+amenity+": ");
      desc = scan.nextLine();
      
      try {
        PreparedStatement addAmen = conn.prepareStatement("insert into amenities (amenity, description) values (?, ?)");
        addAmen.setString(1, amenity);
        addAmen.setString(2, desc);
        addAmen.executeUpdate();
        
        PreparedStatement newAid = conn.prepareStatement("select Aid from amenities order by Aid desc limit 1");
        ResultSet rs = newAid.executeQuery();
        rs.next();
        int Aid = rs.getInt("Aid");
        
        PreparedStatement newHas = conn.prepareStatement("insert into has values (?, ?)");
        newHas.setInt(1, Aid);
        newHas.setInt(2, Lid);
        newHas.executeUpdate();
        
      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
      
      System.out.print("Type DONE if you do not want to add more dates: ");
      check = scan.nextLine();
      count++;
    }
    return count;
  }
  
  //create a new listing
  private static void createListing(Connection conn, int hostId) {
    
    try {
      PreparedStatement newListing = conn.prepareStatement("insert into listing (type, address, country, city, pc, lat, longi) values (?, ?, ?, ?, ?, ?, ?)");
      
      System.out.print("Enter the listing type (ex. house, condo, mansion etc): ");
      newListing.setString(1,  scan.nextLine());
      
      System.out.print("Enter the address: ");
      newListing.setString(2,  scan.nextLine());
      
      System.out.print("Enter the country: ");
      newListing.setString(3,  scan.nextLine());
      
      System.out.print("Enter the city: ");
      newListing.setString(4,  scan.nextLine());
      
      System.out.print("Enter the postal code: ");
      newListing.setString(5,  scan.nextLine());
      
      System.out.print("Enter the latitude: ");
      newListing.setDouble(6,  userInput(Double.class));
      
      System.out.print("Enter the longitude: ");
      newListing.setDouble(7,  userInput(Double.class));
      
      newListing.executeUpdate();
      PreparedStatement newLid = conn.prepareStatement("select Lid from listing order by Lid desc limit 1");
      ResultSet rs = newLid.executeQuery();
      rs.next();
      int Lid = rs.getInt("Lid");

      
      PreparedStatement newOwns = conn.prepareStatement("insert into owns values (?, ?)");
      newOwns.setInt(1, Lid);
      newOwns.setInt(2, hostId);
      
      newOwns.executeUpdate();
      
      System.out.print("Would you like to add amenities? (y/n): ");
      String amenities = scan.nextLine();
      if (amenities.equals("y")) {
        addAmenities(conn, Lid, hostId, false);
      }
      
      System.out.print("Would you like to add dates for rent? (y/n): ");
      String add = scan.nextLine();
      if (add.equals("y")) {
        addCalendar(conn, Lid, hostId, false); 
      }
      
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // returns a price option for a listing made by an algorithm
  private static int specialPricing(Connection conn, int Lid, int price) {
    
    int count = 0;
    try {
      PreparedStatement amenities = conn.prepareStatement("select amenity from amenities a join has h on a.Aid=h.Aid where h.Lid=?");
      amenities.setInt(1, Lid);
      ResultSet rs = amenities.executeQuery();
      
      while(rs.next()) {
        count++;
        String amenity = rs.getString("amenity");
        if (amenitiesList.contains(amenity)) count += 4;
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
    
    
    int specialPrice = (150+(count));
    System.out.print("Do you want to use your price $"+price+" or the suggested price of $"+specialPrice+"? (y/n): ");
    String yn = scan.nextLine();
    if (yn.equals("y")) return specialPrice;
    return price;
  }
  
  // add dates and price to the calendar for a listing
  private static void addCalendar(Connection conn, int Lid, int hostId, boolean selectLid) {
    
    String check = "";
    
    if (selectLid && showOwnedListings(conn, hostId)) {
      System.out.print("Enter the listing code of the location you want to add availability for: ");
      Lid = userInput(Integer.class);
    }
    
    while (!check.equals("DONE")) {
      try {
        PreparedStatement newCalendar = conn.prepareStatement("insert into calendar (start, end, price) values (?, ?, ?)");
        
        System.out.print("Enter the start date in the format YYMMDD: ");
        int start = userInput(Integer.class);
        newCalendar.setInt(1, start);
        
        System.out.print("Enter the end date in the format YYMMDD: ");
        int end = userInput(Integer.class);
        newCalendar.setInt(2, end);
        
        System.out.print("Enter the price per day: ");
        int price = userInput(Integer.class);
        price = specialPricing(conn, Lid, price);
        newCalendar.setInt(3, price);
        newCalendar.executeUpdate();

        PreparedStatement newCrid = conn.prepareStatement("select Crid from calendar order by Crid desc limit 1");
        ResultSet rs = newCrid.executeQuery();
        rs.next();
        
        PreparedStatement newAvail = conn.prepareStatement("insert into availability values (?, ?)");
        newAvail.setInt(1, Lid);
        newAvail.setInt(2, rs.getInt("Crid"));
        newAvail.executeUpdate();
        
        System.out.print("Type DONE if you do not want to add more dates: ");
        check = scan.nextLine();
      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
    }
  }
  
  // returns a string to be added to the query in showOpenListings to refine a search
  private static String searchParams() {
    
    int search = 1000;
    String where = "";
    String order = "";
    
    while (search > 0) {
      System.out.println("Enter a number for the specified search parameter, 0 to end search:");
      System.out.println("1. Latitude and Longitude");
      System.out.println("2. Price");
      System.out.println("3. Nearby Postal Code");
      System.out.println("4. Address");
      System.out.println("5. Temporal Search");
      
      search = userInput(Integer.class);
      
      if (search == 0) break;
      else if(search == 1) {
        System.out.print("Please enter your latitude: ");
        double lat = userInput(Double.class);
        
        System.out.print("Please enter your longitude: ");
        double longi = userInput(Double.class);
        
        System.out.print("Please enter your desired range. Enter 0 for the default: ");
        double range = userInput(Double.class);
        
        if(range <= 0) range = 10;
        
        where = where +  " and lat between "+(lat-range)+" and "+(lat+range)+" and longi between "+(longi-range)+" and "+(longi+range);
        if (!order.equals("")) order = order +"and";
        order = order + " (lat+longi)/2";
      }
      else if (search == 2) {
        String highLow = "";
        while(!(highLow.equals("h") || highLow.equals("l"))) {
          System.out.print("Please enter h to search for highest price or l to search for lowest price: ");
          highLow = scan.nextLine();
        }
        if (!order.equals("")) order = order +" and";
        if(highLow.equals("h")) {
          order = order + " price desc";
        }
        else {
          order = order + " price asc";
        }
      }
      else if (search == 3) {
        System.out.print("Please enter your postal code: ");
        String pc = scan.nextLine();
        
        where = where + " and pc like \""+pc.substring(0,3)+"%\"";
      }
      else if (search == 4) {
        System.out.print("Please enter the address: ");
        String addr = scan.nextLine();
        
        where = where + " and address = \""+addr+"\"";
      }
      else if (search == 5) {
        System.out.print("Enter the start date (YYMMDD) for an available listing: ");
        int start = userInput(Integer.class);
        System.out.print("Enter the end date (YYMMDD) for an available listing: ");
        int end = userInput(Integer.class);
        where = where + " and start <= "+start+" and end >= "+end;
      }
    }
    
    if(order.equals("")) return where;
    
    return where + " order by" + order;
  }
  
  // show the available listings
  private static boolean showOpenListings(Connection conn, boolean search) {
    
    String param = "";
    
    if(search) {
      param = searchParams();
    }
    
    try {
      PreparedStatement listings = conn.prepareStatement("select c.Crid, type, address, country, city, lat, longi, start, end, price from listing l join availability a on l.Lid = a.Lid join calendar c on a.Crid = c.Crid where c.booked = 0"+param);
      ResultSet rs = listings.executeQuery();
      
      System.out.println("Available listings:");
      while(rs.next()) {
        int Crid = rs.getInt("Crid");
        String type = rs.getString("type");
        String addr = rs.getString("address");
        String country = rs.getString("country");
        String city = rs.getString("city");
        double lat = rs.getDouble("lat");
        double longi = rs.getDouble("longi");
        int start = rs.getInt("start");
        int end = rs.getInt("end");
        int price = rs.getInt("price");
        
        System.out.println("Listing code: "+Crid);
        System.out.println("Type: "+type);
        System.out.println("Located at: "+addr+" "+country+", "+city+", "+lat+", "+longi);
        System.out.println("Available from: "+start+" - "+end+" for $"+price+" per day");
        System.out.println("_______________");
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  // book a listing
  private static void bookListing(Connection conn, int renterId) {
    
    if(showOpenListings(conn, false)) {
      System.out.print("Please enter the listing code of the listing you would like to book: ");
      int Crid = userInput(Integer.class);
      
      try {
        PreparedStatement updateCalendar = conn.prepareStatement("update calendar set booked=true where Crid=?");
        updateCalendar.setInt(1,  Crid);
        updateCalendar.executeUpdate();
        
        PreparedStatement getLid = conn.prepareStatement("select a.Lid from availability a join calendar c on a.Crid=c.Crid where a.Crid=?");
        getLid.setInt(1,  Crid);
        ResultSet rs = getLid.executeQuery();
        rs.next();
        int Lid = rs.getInt("Lid");
        
        PreparedStatement updateRented = conn.prepareStatement("insert into rented (Lid, Sin, Crid) values (?, ?, ?)");
        updateRented.setInt(1, Lid);
        updateRented.setInt(2, renterId);
        updateRented.setInt(3, Crid);
        updateRented.executeUpdate();

      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
    }  
  }
  
  // show the listings a renter has rented
  private static boolean showRentedListings(Connection conn, int renterId) {
    
    try {
      PreparedStatement listings = conn.prepareStatement("select distinct c.Crid, address, city, start, end, price from listing l join availability a on l.Lid = a.Lid join calendar c on a.Crid = c.Crid join rented rd on c.Crid = rd.Crid where rd.Sin=? and c.booked = 1;");
      listings.setInt(1, renterId);
      ResultSet rs = listings.executeQuery();
      
      System.out.println("Your current bookings:");
      while(rs.next()) {
        int Crid = rs.getInt("Crid");
        String addr = rs.getString("address");
        String city = rs.getString("city");
        int start = rs.getInt("start");
        int end = rs.getInt("end");
        int price = rs.getInt("price");
        
        System.out.println("Booking code: "+Crid);
        System.out.println("Located at: "+addr+", "+city);
        System.out.println("Available from: "+start+" - "+end+" for $"+price+" per day");
        System.out.println("_______________");
      }
      
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  // cancel a booking as a renter
  private static void cancelBookingRenter(Connection conn, int renterId) {
    
    if(showRentedListings(conn, renterId)) {
      System.out.print("Please enter the booking code of the booking you would like to cancel: ");
      int Crid = userInput(Integer.class);
      
      try {
        PreparedStatement updateCalendar = conn.prepareStatement("update calendar set booked=false where Crid=?");
        updateCalendar.setInt(1,  Crid);
        updateCalendar.executeUpdate();
        
        PreparedStatement updateRented = conn.prepareStatement("delete from rented where Crid=?");
        updateRented.setInt(1, Crid);
        updateRented.executeUpdate();
        
        PreparedStatement updateUser = conn.prepareStatement("update user set cancels=cancels+1 where Sin=?");
        updateUser.setInt(1, renterId);
        updateUser.executeUpdate();
      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
    }
  }
  
  // show the listings a host owns
  private static boolean showOwnedListings(Connection conn, int hostId) {
    
    try {
      PreparedStatement listings = conn.prepareStatement("select l.Lid, address, country, city from listing l join owns o on l.Lid = o.Lid where o.Sin=?");
      listings.setInt(1, hostId);
      ResultSet rs1 = listings.executeQuery();
      
      System.out.println("Your owned listings:");
      while(rs1.next()) {
        int Lid = rs1.getInt("Lid");
        String addr = rs1.getString("address");
        String city = rs1.getString("city");
        
        System.out.println("Listing code: "+Lid);
        System.out.println("Located at: "+addr+", "+city);
        
        System.out.println("With the following availability:");
        
        PreparedStatement availability = conn.prepareStatement("select c.Crid, start, end, price, booked from calendar c join availability a on a.Crid = c.Crid where a.Lid=?");
        availability.setInt(1, Lid);
        ResultSet rs2 = availability.executeQuery();
        while(rs2.next()) {
          int Crid = rs2.getInt("Crid");
          int start = rs2.getInt("start");
          int end = rs2.getInt("end");
          int price = rs2.getInt("price");
          boolean booked = rs2.getBoolean("booked");
          
          System.out.print(">Calendar code "+Crid+" is ");
          if(!booked) System.out.print("not ");
          System.out.println("booked from "+start+" - "+end+" for $"+price+" per day");
        }
        System.out.println("_______________");
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  // cancel a booking a host owns
  private static void cancelBookingHost(Connection conn, int hostId) {
   
    if(showOwnedListings(conn, hostId)) {
      System.out.print("Enter the calendar code of the booking you wish to cancel");
      int Crid = userInput(Integer.class);
      
      try {
        PreparedStatement updateCalendar = conn.prepareStatement("update calendar set booked=false where Crid=?");
        updateCalendar.setInt(1,  Crid);
        updateCalendar.executeUpdate();
        
        PreparedStatement updateRented = conn.prepareStatement("delete from rented where Crid=?");
        updateRented.setInt(1, Crid);
        updateRented.executeUpdate();
        
        PreparedStatement updateUser = conn.prepareStatement("update user set cancels=cancels+1 where Sin=?");
        updateUser.setInt(1, hostId);
        updateUser.executeUpdate();
      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
    }
  }
  
  // update the price of an owned listing on the calendar
  private static void updatePrice(Connection conn, int hostId) {
    
    if (showOwnedListings(conn, hostId)) {
      System.out.print("Enter the calendar code of the listing you wish to change the price of. Note you CANNOT change the price of a booked location: ");
      int Crid = userInput(Integer.class);
      
      try {
        PreparedStatement checkCrid = conn.prepareStatement("select booked from calendar where Crid=? and booked=0");
        checkCrid.setInt(1, Crid);
        ResultSet rs = checkCrid.executeQuery();
        
        if(!rs.next()) {
          System.out.println("Sorry, the price of a listing cannot be changed if it is already being rented");
          return;
        }
        
        System.out.print("Enter the new price: ");
        int price = userInput(Integer.class);
        
        PreparedStatement updatePrice = conn.prepareStatement("update calendar set price=? where Crid=?");
        updatePrice.setInt(1, price);
        updatePrice.setInt(2, Crid);
        updatePrice.executeUpdate();
        
      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
    }
  }
  
  // remove an owned listing as the host
  private static void removeListing(Connection conn, int hostId) {
    
    int count = 0;
    
    if(showOwnedListings(conn, hostId)) {
      System.out.print("Enter the listing code of the listing you wish to remove: ");
      int Lid = userInput(Integer.class);
      
      try {
        PreparedStatement getCrids = conn.prepareStatement("select c.Crid from calendar c join availability a on c.Crid=a.Crid where Lid=?");
        getCrids.setInt(1, Lid);
        ResultSet rs = getCrids.executeQuery();
        
        while(rs.next()) {
          int Crid = rs.getInt("Crid");
          PreparedStatement deleteCalendar = conn.prepareStatement("delete from calendar where Crid=?");
          deleteCalendar.setInt(1, Crid);
          deleteCalendar.executeUpdate();
          count++;
        }
        
        PreparedStatement deleteListing = conn.prepareStatement("delete from listing where Lid=?");
        deleteListing.setInt(1, Lid);
        deleteListing.executeUpdate();
        
        PreparedStatement updateUser = conn.prepareStatement("update user set cancels=cancels+? where Sin=?");
        updateUser.setInt(1, count);
        updateUser.setInt(2, hostId);
        updateUser.executeUpdate();

      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
    }
  }
  
  // rate a listing a renter has rented
  private static void rateRentedListings(Connection conn, int renterId) {
    
    if (showRentedListings(conn, renterId)) {
      System.out.print("Enter the booking code of the listing you wish to rate, 0 to cancel: ");
      int Crid = userInput(Integer.class);
      
      if (Crid==0) return;
      
      int stars = -1;
      while (stars < 1 || stars > 5) {
        System.out.print("Please enter a rating from 1 to 5: ");
        stars = userInput(Integer.class);
      }
      
      String comment = "";
      while (comment.length() > 250 || comment.length() < 1) {
        System.out.println("Please leave a comment (Max 250 characters): ");
        comment = scan.nextLine();
      }
      
      try {
        PreparedStatement rateRented = conn.prepareStatement("update rented set comment=?, stars=? where Crid=? and Sin=?");
        rateRented.setString(1, comment);
        rateRented.setInt(2, stars);
        rateRented.setInt(3, Crid);
        rateRented.setInt(4, renterId);
        rateRented.executeUpdate();
        
      } catch (SQLException e) {
        System.out.println("There was an error");
        e.printStackTrace();
      }
    }
  }
  
  // show the owners of each place a renter has rented
  private static boolean showOwnersOfRented(Connection conn, int renterId) {
    
    try {
      PreparedStatement hosts = conn.prepareStatement("select u.Sin, u.name, l.address from renter r join rented rd on r.Sin=rd.Sin join listing l on l.Lid=rd.Lid join owns o on l.Lid=o.Lid join user u on u.Sin=o.Sin where r.Sin=? order by u.Sin");
      hosts.setInt(1, renterId);
      ResultSet rs = hosts.executeQuery();
      
      System.out.println("These are the owners and addresses of places you've rented of theirs:");
      while(rs.next()) {
        int Sin = rs.getInt("Sin");
        String name = rs.getString("name");
        String addr = rs.getString("address");
        
        System.out.println("User ID: "+Sin+", Name: "+name+", address of listing: "+addr);
        showUserRatings(conn, Sin);
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  // show who has rented a listing a host owns
  private static boolean showRentersOfOwned(Connection conn, int hostId) {
    try {
      PreparedStatement renters = conn.prepareStatement("select u.Sin, u.name, l.address from host h join owns o on o.Sin=h.Sin join listing l on l.Lid=o.Lid join rented r on l.Lid=r.Lid join user u on u.Sin=r.Sin where h.Sin=? order by u.Sin");
      renters.setInt(1, hostId);
      ResultSet rs = renters.executeQuery();
      
      System.out.println("These are the renters and addresses of places they've rented from you:");
      while(rs.next()) {
        int Sin = rs.getInt("Sin");
        String name = rs.getString("name");
        String addr = rs.getString("address");
        
        System.out.println("User ID: "+Sin+", Name: "+name+", address of listing: "+addr);
        showUserRatings(conn, Sin);
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
      return false;
    }
    return true;
  }
  
  // rate a user
  private static void rateUser(Connection conn, int Sin, boolean isHost) {
    

    if(isHost) {
      if(!showRentersOfOwned(conn, Sin)) return;
    }
    else {
      if(!showOwnersOfRented(conn, Sin)) return;
    }
    
    System.out.print("Please enter the User ID of the user you wish to rate, 0 to cancel: ");

    int rateSin = userInput(Integer.class);
    
    if (rateSin==0) return;
    
    int stars = -1;
    while (stars < 1 || stars > 5) {
      System.out.print("Please enter a rating from 1 to 5: ");
      stars = userInput(Integer.class);
    }
    
    String comment = "";
    while (comment.length() > 250 || comment.length() < 1) {
      System.out.println("Please leave a comment (Max 250 characters): ");
      comment = scan.nextLine();
    }
    
    try {
      PreparedStatement updateRating = conn.prepareStatement("insert into rating (comment, rating) values (?, ?)");
      updateRating.setString(1, comment);
      updateRating.setInt(2, stars);
      updateRating.executeUpdate();
      
      PreparedStatement newRid = conn.prepareStatement("select Rid from rating order by Rid desc limit 1");
      ResultSet rs = newRid.executeQuery();
      rs.next();
      int Rid = rs.getInt("Rid");
      
      PreparedStatement updateRatings = conn.prepareStatement("insert into ratings values (?, ?)");
      updateRatings.setInt(1, rateSin);
      updateRatings.setInt(2, Rid);
      updateRatings.executeUpdate();
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // show the ratings of a user
  private static void showUserRatings(Connection conn, int Sin) {
    
    try {
      PreparedStatement ratings = conn.prepareStatement("select rating, comment from rating r join ratings rs on r.Rid=rs.Rid where Sin=?");
      ratings.setInt(1, Sin);
      ResultSet rs = ratings.executeQuery();
      
      while(rs.next()) {
        int rating = rs.getInt("rating");
        String comment = rs.getString("comment");
        System.out.println(rating+" stars");
        System.out.println(comment);
        System.out.println("_______________");
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    } 
  }
  
  // report the number of bookings in a date range in a city or city and postal code
  private static void reportBookings(Connection conn) {
    
    String report = "";
    String where = "";
    while(!(report.equals("c") || report.equals("p"))) {
      System.out.print("Do you want to report by city (c) or city and postal code (p)?: ");
      report = scan.nextLine();
    }
    
    if(report.equals("p")) {
      System.out.print("Please enter the postal code: ");
      report = scan.nextLine();
      where = " and pc = \"" + report+"\"";
    }
    
    System.out.print("Please enter the city name: ");
    report = scan.nextLine();
    where = " and city = \""+ report+"\"" + where ;
    
    System.out.print("Enter the start date (YYMMDD) for a booking: ");
    int start = userInput(Integer.class);
    System.out.print("Enter the end date (YYMMDD) for a booking: ");
    int end = userInput(Integer.class);
    where = where + " and start >= "+start+" and end <= "+end;
    
    try {
      PreparedStatement bookings = conn.prepareStatement("select count(*) from calendar c join availability a on a.Crid=c.Crid join listing l on l.Lid=a.Lid where c.booked=true"+where);
      ResultSet rs = bookings.executeQuery();
      rs.next();
      int count = rs.getInt("count(*)");
      System.out.println("There are "+count+" bookings");
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // report the number of listings per city, country and city or country city and postal code
  private static void reportListings(Connection conn) {
    
    String report = "";
    String select = "";
    String group = "";
    
    while(!(report.equals("c") || report.equals("cc") || report.equals("p"))) {
      System.out.print("Do you want to report by city (c), country and city (cc) or country, city and postal code (p)?: ");
      report = scan.nextLine();
    }
    
    if (report.equals("p")) {
      select = " pc, country,";
      group = " pc, country,";
    }
    else if (report.equals("cc")) {
      select = select + " country,";
      group = group + " country,";
    }
    select = select + " city";
    group = group + " city";
    
    try {
      PreparedStatement listings = conn.prepareStatement("select count(*), "+select+" from listing group by"+group);
      ResultSet rs = listings.executeQuery();
      
      while(rs.next()) {
        int count = rs.getInt("count(*)");
        
        System.out.print("There are "+count+" listings in ");
        if (report.equals("p")) {
          String pc = rs.getString("pc");
          String country = rs.getString("country");
          System.out.print(pc+", "+country+", ");
        }
        else if (report.equals("cc")) {
          String country = rs.getString("country");
          System.out.print(country+", ");
        }
        String city = rs.getString("city");
        System.out.println(city);
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // rank the hosts by how many listings they have in each country or city
  private static void reportHostRank(Connection conn) {
    
    String report = "";
    while(!(report.equals("co") || report.equals("ci"))) {
      System.out.print("Do you want to report by city (ci) or country (co)?: ");
      report = scan.nextLine();
    }
    
    String location = "";
    if (report.equals("co")) location = "country";
    else location = "city";
    
    try {
      PreparedStatement hostRank = conn.prepareStatement("select "+location+", name, count(*) from listing l join owns o on l.Lid=o.Lid join user u on o.Sin=u.Sin group by "+location+", name order by count(*) desc");
      ResultSet rs = hostRank.executeQuery();
      
      while(rs.next()) {
        String locationName = rs.getString(location);
        String name = rs.getString("name");
        int count = rs.getInt("count(*)");
        
        System.out.println(name+" has "+count+" locations in "+locationName);
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // report the hosts who own more than 10% of the listings in a city and country
  private static void reportCommercialHost(Connection conn) {
    
    String report = "";
    while(!(report.equals("co") || report.equals("ci"))) {
      System.out.print("Do you want to report by city (ci) or country (co)?: ");
      report = scan.nextLine();
    }
    
    String location = "";
    if (report.equals("co")) location = "country";
    else location = "city";
    
    try {
      PreparedStatement hostRank = conn.prepareStatement("select "+location+", name, count(*) from listing l1 join owns o on l1.Lid=o.Lid join user u on o.Sin=u.Sin group by "+location+", name having (count(*)/(select count(*) from listing l2 where l2."+location+" = l1."+location+")) > 0.1;");
      ResultSet rs = hostRank.executeQuery();
      
      while(rs.next()) {
        String locationName = rs.getString(location);
        String name = rs.getString("name");
        int count = rs.getInt("count(*)");
        
        System.out.println(name+" has "+count+" locations in "+locationName);
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // report the number of bookings made by a renter within a time period as well as time period per city (min 2)
  private static void reportRenterRank(Connection conn) {
    
    String where = "";
    String group = "";
    String select = "";
    
    System.out.print("Enter the start date (YYMMDD) for bookings: ");
    int start = userInput(Integer.class);
    System.out.print("Enter the end date (YYMMDD) for bookings: ");
    int end = userInput(Integer.class);
    where = " start >= "+start+" and end <= "+end;
    
    System.out.print("Do you want to know the most bookings per city? (y/n): ");
    String yn = scan.nextLine();
    if (yn.equals("y")) {
      group = " group by city, name having count(*) >= 2";
      select = ", city";
    }
    else {
      group = " group by name";
    }
    
    
    try {
      PreparedStatement rank = conn.prepareStatement("select count(*), name" +select+ " from listing l join availability a on l.Lid=a.Lid join calendar c on a.Crid=c.Crid join rented r on r.Crid=c.Crid join user u on r.Sin=u.Sin where"+where+group+" order by count(*)");
      ResultSet rs = rank.executeQuery();
      
      if (yn.equals("y")) {
        while(rs.next()) {
          String city = rs.getString("city");
          String name = rs.getString("name");
          int count = rs.getInt("count(*)");
          System.out.println(name+" has the most bookings in "+city+" with "+count);
        }
      }
      else {
        while(rs.next()) {
          String name = rs.getString("name");
          int count = rs.getInt("count(*)");
          System.out.println(name+" has "+count+" bookings");
        }
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // report the number of cancellations for each user (in a year???)
  private static void reportCancellations(Connection conn) {
    
    try {
      PreparedStatement mostRenter = conn.prepareStatement("select name, cancels from user u join renter r on u.Sin=r.Sin where u.cancels=(select max(cancels) from user u2 join renter r2 on u2.Sin=r2.Sin)");
      ResultSet rs1 = mostRenter.executeQuery();
      
      System.out.println("Renter:");
      while(rs1.next()) {
        String name = rs1.getString("name");
        int cancels = rs1.getInt("cancels");
        System.out.println(name+" cancelled "+cancels+" times");
      }
      
      PreparedStatement mostHost = conn.prepareStatement("select name, cancels from user u join host h on u.Sin=h.Sin where u.cancels=(select max(cancels) from user u2 join host h2 on u2.Sin=h2.Sin)");
      ResultSet rs2 = mostHost.executeQuery();
      
      System.out.println("Host:");
      while(rs2.next()) {
        String name = rs2.getString("name");
        int cancels = rs2.getInt("cancels");
        System.out.println(name+" cancelled "+cancels+" times");
      }
      
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
  
  // report the most common nouns found in the comments of each listing
  private static void reportNouns(Connection conn) {
    
    try {
      PreparedStatement getLids = conn.prepareStatement("select Lid, address from listing");
      ResultSet rs1 = getLids.executeQuery();
      
      while (rs1.next()) {
        int Lid = rs1.getInt("Lid");
        String addr = rs1.getString("address");
        System.out.println("For "+addr+", listing code: "+Lid);
        
        for (int i=0; i<amenitiesList.size(); i++) {
          String noun = amenitiesList.get(i);
          PreparedStatement nounCount = conn.prepareStatement("select count(*) from rented where Lid=? and comment like \"%"+noun+"%\"");
          nounCount.setInt(1, Lid);
          ResultSet rs2 = nounCount.executeQuery();
          while(rs2.next()) {
            int count = rs2.getInt("count(*)");
            System.out.println(">"+noun+" has "+count+" occurences");
          }
        }
      }
    } catch (SQLException e) {
      System.out.println("There was an error");
      e.printStackTrace();
    }
  }
}