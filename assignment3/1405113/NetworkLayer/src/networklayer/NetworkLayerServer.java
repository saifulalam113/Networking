/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package networklayer;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author CSE_BUET
 */
public class NetworkLayerServer {
    static int clientCount = 1;
    static ArrayList<Router> routers = new ArrayList<>();
    static RouterStateChanger stateChanger = null;
    public static ArrayList<IPAddress> active_client_list = new ArrayList<>();

    /**
     * Each map entry represents number of client end devices connected to the interface
     */
    static Map<IPAddress,Integer> clientInterfaces = new HashMap<>();
    /**
     * @param args the command line arguments
     */

    static int numberOfUpdateHappened;
    public static void main(String[] args) {
        /**
         * Task: Maintain an active client list
         */
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(1234);
        } catch (IOException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }

        System.out.println("Server Ready: "+serverSocket.getInetAddress().getHostAddress());
        
        System.out.println("Creating router topology");
        
        readTopology();
        printRouters();
        
        /**
         * Initialize routing tables for all routers
         */
        initRoutingTables();


        System.out.println("\n\n\n\n");
        for(int i=0;i<routers.size();i++)
        {
            routers.get(i).printRoutingTable();
            System.out.println("\n");
        }
        /**
         * Update routing table using distance vector routing until convergence
         */
        DVR(2);

        //simpleDVR(2);

        System.out.println("\n\n\n\n----------------------------------------------------------------------------------------");
        System.out.println("First complete routing table : ");

        for(int i=0;i<routers.size();i++)
        {
            System.out.println("------------ Router :"+(i+1)+"------------");
            routers.get(i).printRoutingTable();
        }
        /**
         * Starts a new thread which turns on/off routers randomly depending on parameter Constants.LAMBDA
         */
        stateChanger = new RouterStateChanger();
        
        while(true){
            try {
                Socket clientSock = serverSocket.accept();
                System.out.println("Client attempted to connect");
                EndDevice endDevice = getClientDeviceSetup();
                ///////////////////////
                /*ObjectInputStream input;
                ObjectOutputStream output;
                input = new ObjectInputStream(clientSock.getInputStream());
                output = new ObjectOutputStream(clientSock.getOutputStream());
                */
                BufferedReader in = null;
                PrintWriter out = null;
                out = new PrintWriter(clientSock.getOutputStream(),true);
                in =  new BufferedReader(new InputStreamReader(clientSock.getInputStream()));
                out.println(endDevice.getIp().getString());
                out.println(endDevice.getGateway().getString());



                new ServerThread(clientSock);


            } catch (IOException ex) {
                Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
    }
    
    public static void initRoutingTables()
    {
        for(int i=0;i<routers.size();i++)
        {
            routers.get(i).initiateRoutingTable();
            System.out.println();
            System.out.println();
            routers.get(i).printRoutingTable();
        }
    }
    
    /**
     * Task: Implement Distance Vector Routing with Split Horizon and Forced Update
     */
    public static void DVR(int startingRouterId)
    {

        System.out.println("<------------------------------------------------------- DVR Start ------------------------------------------------------------->");

        /**
         * pseudocode
         */
        /*
        while(convergence)
        {
            //convergence means no change in any routingTable before and after executing the following for loop
            for each router r <starting from the router with routerId = startingRouterId, in any order>
            {
                1. T <- getRoutingTable of the router r
                2. N <- find routers which are the active neighbors of the current router r
                3. Update routingTable of each router t in N using the 
                   routing table of r [Hint: Use t.updateRoutingTable(r)]
            }
        }
        */
        int m=0;
        while(true){
            ArrayList<RoutingTableEntry> routingTable;
            ArrayList<Integer> activeRouters = new ArrayList<>();

            numberOfUpdateHappened = 0;
            for(int i = startingRouterId-1;i<routers.size();i++){

                System.out.println("---------------------------------------------------------------");

                if(routers.get(i).getState() == true){

                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : True]");
                    //print routing table of this router
                    System.out.println("Routing "+routers.get(i).getRouterId()+" : Routing table\n");
                    routers.get(i).printRoutingTable();



                    //print who is active neighbor of this router
                    System.out.print("Passing routing table to neighbour router (active): ");
                    ArrayList<Integer> neighborIds = routers.get(i).getNeighborRouterIds();

                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            System.out.print(neighborIds.get(j)+"   ");
                        }
                    }
                    System.out.println();


                    //pass routing table to neighbour
                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            routers.get((neighborIds.get(j))-1).updateRoutingTable(routers.get(i));
                        }
                    }





                    /*
                    //getRoutingTable of the router r
                    routingTable = routers.get(i).getRoutingTable();

                    //find routers which are the active neighbors of the current router r
                    for(int j=0;j<routers.size();j++){
                        if((routingTable.get(j).getDistance() == 1.0) && (routers.get(routingTable.get(j).getRouterId()-1).getState()==true)){
                            activeRouters.add(routingTable.get(j).getRouterId());
                            System.out.println(routingTable.get(j).getRouterId() +" is neighbor of "+(i+1));
                        }




                    }

                    //Update routingTable of each router t in N using the routing table of r
                    if(activeRouters.size() > 0){
                        for(int j=0;j<activeRouters.size();j++){
                            routers.get(activeRouters.get(j)-1).updateRoutingTable(routers.get(i));
                        }

                        activeRouters.clear();
                    }



                    */







                }
                else{
                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : False]");
                }



            }

            for(int i = 0;i<startingRouterId-1;i++){

                if(routers.get(i).getState() == true) {

                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : True]");
                    //print routing table of this router
                    System.out.println("Routing "+routers.get(i).getRouterId()+" : Routing table\n");
                    routers.get(i).printRoutingTable();




                    //print who is active neighbor of this router
                    System.out.print("Passing routing table to neighbour router : ");
                    ArrayList<Integer> neighborIds = routers.get(i).getNeighborRouterIds();

                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            System.out.print(neighborIds.get(j)+"   ");
                        }
                    }
                    System.out.println();

                    //pass routing table to neighbour
                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            routers.get((neighborIds.get(j))-1).updateRoutingTable(routers.get(i));
                        }
                    }



                    /*
                    //getRoutingTable of the router r
                    routingTable = routers.get(i).getRoutingTable();

                    //find routers which are the active neighbors of the current router r
                    for(int j=0;j<routers.size();j++){
                        if(routingTable.get(j).getDistance() == 1){
                            activeRouters.add(routingTable.get(j).getRouterId());
                        }
                    }


                    //Update routingTable of each router t in N using the routing table of r
                    if(activeRouters.size() > 0){
                        for(int j=0;j<activeRouters.size();j++){
                            routers.get(activeRouters.get(j)-1).updateRoutingTable(routers.get(i));
                        }

                        activeRouters.clear();
                    }
                    */




                }
                else{
                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : False]");
                }
            }



            if(numberOfUpdateHappened == 0) break;

            //m++;
            //if(m>10) break;
        }

        System.out.println("<------------------------------------------------------- DVR Complete ------------------------------------------------------------->");


    }
    
    /**
     * Task: Implement Distance Vector Routing without Split Horizon and Forced Update
     */
    public static void simpleDVR(int startingRouterId)
    {

        System.out.println("<-------------------------------------------------------Simple DVR Start ------------------------------------------------------------->");

        /**
         * pseudocode
         */
        /*
        while(convergence)
        {
            //convergence means no change in any routingTable before and after executing the following for loop
            for each router r <starting from the router with routerId = startingRouterId, in any order>
            {
                1. T <- getRoutingTable of the router r
                2. N <- find routers which are the active neighbors of the current router r
                3. Update routingTable of each router t in N using the
                   routing table of r [Hint: Use t.updateRoutingTable(r)]
            }
        }
        */
        int m=0;
        while(true){
            ArrayList<RoutingTableEntry> routingTable;
            ArrayList<Integer> activeRouters = new ArrayList<>();

            numberOfUpdateHappened = 0;
            for(int i = startingRouterId-1;i<routers.size();i++){

                System.out.println("---------------------------------------------------------------");

                if(routers.get(i).getState() == true){

                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : True]");
                    //print routing table of this router
                    System.out.println("Routing "+routers.get(i).getRouterId()+" : Routing table\n");
                    routers.get(i).printRoutingTable();



                    //print who is active neighbor of this router
                    System.out.print("Passing routing table to neighbour router (active): ");
                    ArrayList<Integer> neighborIds = routers.get(i).getNeighborRouterIds();

                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            System.out.print(neighborIds.get(j)+"   ");
                        }
                    }
                    System.out.println();


                    //pass routing table to neighbour
                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            routers.get((neighborIds.get(j))-1).updateRoutingTableSimple(routers.get(i));
                        }
                    }





                    /*
                    //getRoutingTable of the router r
                    routingTable = routers.get(i).getRoutingTable();

                    //find routers which are the active neighbors of the current router r
                    for(int j=0;j<routers.size();j++){
                        if((routingTable.get(j).getDistance() == 1.0) && (routers.get(routingTable.get(j).getRouterId()-1).getState()==true)){
                            activeRouters.add(routingTable.get(j).getRouterId());
                            System.out.println(routingTable.get(j).getRouterId() +" is neighbor of "+(i+1));
                        }




                    }

                    //Update routingTable of each router t in N using the routing table of r
                    if(activeRouters.size() > 0){
                        for(int j=0;j<activeRouters.size();j++){
                            routers.get(activeRouters.get(j)-1).updateRoutingTable(routers.get(i));
                        }

                        activeRouters.clear();
                    }



                    */







                }
                else{
                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : False]");
                }



            }

            for(int i = 0;i<startingRouterId-1;i++){

                if(routers.get(i).getState() == true) {

                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : True]");
                    //print routing table of this router
                    System.out.println("Routing "+routers.get(i).getRouterId()+" : Routing table\n");
                    routers.get(i).printRoutingTable();




                    //print who is active neighbor of this router
                    System.out.print("Passing routing table to neighbour router : ");
                    ArrayList<Integer> neighborIds = routers.get(i).getNeighborRouterIds();

                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            System.out.print(neighborIds.get(j)+"   ");
                        }
                    }
                    System.out.println();

                    //pass routing table to neighbour
                    for(int j=0;j<neighborIds.size();j++){
                        if(routers.get((neighborIds.get(j))-1).getState() == true){
                            routers.get((neighborIds.get(j))-1).updateRoutingTableSimple(routers.get(i));
                        }
                    }



                    /*
                    //getRoutingTable of the router r
                    routingTable = routers.get(i).getRoutingTable();

                    //find routers which are the active neighbors of the current router r
                    for(int j=0;j<routers.size();j++){
                        if(routingTable.get(j).getDistance() == 1){
                            activeRouters.add(routingTable.get(j).getRouterId());
                        }
                    }


                    //Update routingTable of each router t in N using the routing table of r
                    if(activeRouters.size() > 0){
                        for(int j=0;j<activeRouters.size();j++){
                            routers.get(activeRouters.get(j)-1).updateRoutingTable(routers.get(i));
                        }

                        activeRouters.clear();
                    }
                    */




                }
                else{
                    System.out.println("Router : "+routers.get(i).getRouterId()+"    [status : False]");
                }
            }



            if(numberOfUpdateHappened == 0) break;

            //m++;
            //if(m>10) break;
        }

        System.out.println("<------------------------------------------------------- Simple DVR Complete ------------------------------------------------------------->");


    }
    
    
    public static EndDevice getClientDeviceSetup()
    {
        Random random = new Random();
        int r =Math.abs(random.nextInt(clientInterfaces.size()));
        
        System.out.println("Size: "+clientInterfaces.size()+"\n"+r);
        
        IPAddress ip=null;
        IPAddress gateway=null;
        
        int i=0;
        for (Map.Entry<IPAddress, Integer> entry : clientInterfaces.entrySet()) {
            IPAddress key = entry.getKey();
            Integer value = entry.getValue();
            if(i==r)
            {
                gateway = key;
                ip = new IPAddress(gateway.getBytes()[0]+"."+gateway.getBytes()[1]+"."+gateway.getBytes()[2]+"."+(value+2));
                value++;
                clientInterfaces.put(key, value);
                active_client_list.add(ip);
                System.out.println("Active client List:::::");
                for(int j=0;j<active_client_list.size();j++){
                    System.out.println(active_client_list.get(j));
                }
                break;
            }
            i++;
        }
        
        EndDevice device = new EndDevice(ip, gateway);
        System.out.println("Device : "+ip+"::::"+gateway);
        return device;
    }
    
    public static void printRouters()
    {
        for(int i=0;i<routers.size();i++)
        {
            System.out.println("------------------\n"+routers.get(i));
        }
    }
    
    public static void readTopology()
    {
        Scanner inputFile = null;
        try {
            inputFile = new Scanner(new File("topology.txt"));
            //skip first 27 lines
            int skipLines = 27;
            for(int i=0;i<skipLines;i++)
            {
                inputFile.nextLine();
            }
            
            //start reading contents
            while(inputFile.hasNext())
            {
                inputFile.nextLine();
                int routerId;
                ArrayList<Integer> neighborRouters = new ArrayList<>();
                ArrayList<IPAddress> interfaceAddrs = new ArrayList<>();
                
                routerId = inputFile.nextInt();
                
                int count = inputFile.nextInt();
                for(int i=0;i<count;i++)
                {
                    neighborRouters.add(inputFile.nextInt());
                }
                count = inputFile.nextInt();
                inputFile.nextLine();
                
                for(int i=0;i<count;i++)
                {
                    String s = inputFile.nextLine();
                    //System.out.println(s);
                    IPAddress ip = new IPAddress(s);
                    interfaceAddrs.add(ip);
                    
                    /**
                     * First interface is always client interface
                     */
                    if(i==0)
                    {
                        //client interface is not connected to any end device yet
                        clientInterfaces.put(ip, 0);
                    }
                }
                Router router = new Router(routerId, neighborRouters, interfaceAddrs);
                routers.add(router);
            }
            
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(NetworkLayerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


}
