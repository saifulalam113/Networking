/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static networklayer.NetworkLayerServer.*;


/**
 *
 * @author samsung
 */
public class ServerThread implements Runnable {
    private Thread t;
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    BufferedReader in = null;
    PrintWriter out = null;
    public String path = new String();
    public int hopCount = 0;
    public int sourceRouter = 0;
    public int destRouter = 0;
    int count = 0;//to track that it falls in infinite loop
    
    public ServerThread(Socket socket){
        
        this.socket = socket;
        try {
            /*output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();
            input = new ObjectInputStream(socket.getInputStream());*/

            out = new PrintWriter(socket.getOutputStream(),true);
            in =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println("\n\n\n\n\n\n\n"+in.readLine());
            
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Server Ready for client "+NetworkLayerServer.clientCount);
        NetworkLayerServer.clientCount++;
        
        t=new Thread(this);
        t.start();
    }

    @Override
    public void run() {

        String number;
        String message;
        String specialMessage;  //ex: "SHOW_ROUTE" request
        IPAddress destinationIP;
        IPAddress sourceIP;
        String IPAddresssToRemove;
        int m=0;

        try {
            while(true){
                //System.out.println("\n\nactive client list send from server :"+active_client_list.size());
                for(int i=0;i<active_client_list.size();i++){
                    out.println(active_client_list.get(i).getString());
                    //System.out.println(active_client_list.get(i).getString());

                }
                //System.out.println("\n\n");
                out.println(1);


                System.out.println(m);
                number = in.readLine();
                if(number.equals("1")){
                    message = in.readLine();
                    specialMessage = in.readLine();
                    sourceIP = new IPAddress(in.readLine());
                    destinationIP = new IPAddress(in.readLine());
                    System.out.println(message+" "+specialMessage+" "+sourceIP+" "+destinationIP+"    "+(m++));


                    Packet packet = new Packet(message,specialMessage,sourceIP,destinationIP);
                    Boolean result = deliverPacket(packet);


                    //sending source and destination router
                    out.println(sourceRouter);
                    out.println(destRouter);
                    if(result == true){
                        //System.out.println("\n\n\n\n\n\n\n\n packet sending successful from "+sourceIP+" to "+destinationIP+"\n Path: "+path);
                        //System.out.println("hop count :"+hopCount);



                        //Display routing path, hop count and routing table of each router [You need to receive
                        //all the required info from the server in response to "SHOW_ROUTE" request]



                        out.println("successful");

                        out.println(hopCount);



                    }
                    else{
                        //System.out.println("\n\n\n\n\n\n\n\n packet dropped "+"\n Path: "+path);
                        if(count > Constants.INFTY) out.println("dropped for infinite loop");
                        else out.println("dropped");
                    }


                    out.println(path);

                    out.println(routers.size());

                    for(int k=0;k<routers.size();k++)
                    {
                        out.println(routers.get(k).getRoutingTableInString());
                    }



                }
                else if(number.equals("2")){
                    message = in.readLine();
                    sourceIP = new IPAddress(in.readLine());
                    destinationIP = new IPAddress(in.readLine());
                    System.out.println(message+" "+" "+sourceIP+" "+destinationIP+"   "+(m++));


                    Packet packet = new Packet(message,null,sourceIP,destinationIP);
                    Boolean result = deliverPacket(packet);



                    //sending source and destination router
                    out.println(sourceRouter);
                    out.println(destRouter);
                    if(result == true){
                        //System.out.println("\n\n\n\n\n\n\n\n packet sending successful from "+sourceIP+" to "+destinationIP+"\n Path: "+path);
                        //System.out.println("hop count :"+hopCount);
                        out.println("successful");
                        out.println(hopCount);

                    }
                    else{
                        //System.out.println("\n\n\n\n\n\n\n\n packet dropped "+"\n Path: "+path);
                        if(count > Constants.INFTY) out.println("dropped for infinite loop");
                        else out.println("dropped");
                    }

                    out.println(path);

                }
                else if(number.equals("3")){

                    //System.out.println("client sends nothing");

                }
                else if(number.equals("4")){
                    IPAddresssToRemove = in.readLine();

                    //to remove client from active_client_list
                    for(int i=0;i<active_client_list.size();i++){
                        IPAddress client_ip = active_client_list.get(i);

                        if(client_ip.getString().equals(IPAddresssToRemove)){
                            active_client_list.remove(i);
                            break;
                        }
                    }
                    System.out.println("helloooooooooooooooooooooooooooooooooooooooooooooooo");
                    break;


                }

                try {
                    Thread.sleep(1200);
                } catch (InterruptedException ex) {
                    Logger.getLogger(RouterStateChanger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        /**
         * Synchronize actions with client.
         */
        /*
        Tasks:
        1. Upon receiving a packet server will assign a recipient.
        [Also modify packet to add destination]
        2. call deliverPacket(packet)
        3. If the packet contains "SHOW_ROUTE" request, then fetch the required information
                and send back to client
        4. Either send acknowledgement with number of hops or send failure message back to client
        */
    }
    
    /**
     * Returns true if successfully delivered
     * Returns false if packet is dropped
     * @param p
     * @return 
     */
    public Boolean deliverPacket(Packet p)
    {


        count = 0;          //to track that it falls in infinite loop


        //Get source router by using ipaddress of client host
        IPAddress sourceIp = p.getSourceIP();
        Router s;
        int source_router_id=0;
        int source_router_index = 0;
        for(int i=0;i<routers.size();i++){
            source_router_index = i;
            s = routers.get(i);
            source_router_id = s.getRouterId();
            IPAddress source_router_client_interface = s.getInterfaceAddrs().get(0);
            String client_network_portion = sourceIp.getBytes()[0]+"."+sourceIp.getBytes()[1]+"."+sourceIp.getBytes()[2];
            String source_router_network_portion = source_router_client_interface.getBytes()[0]+"."+source_router_client_interface.getBytes()[1]+"."+source_router_client_interface.getBytes()[2];
            if(client_network_portion.equals(source_router_network_portion)){
                break;
            }
        }

        //System.out.println("Sender router id :"+source_router_id);
        sourceRouter = source_router_id;




        //get destination router by using ipaddress of destination host
        IPAddress destinationIp = p.getDestinationIP();
        Router d;
        int destination_router_id=0;
        int destination_router_index= 0;
        for(int i=0;i<routers.size();i++){
            destination_router_index = i;
            d = routers.get(i);
            destination_router_id = d.getRouterId();
            IPAddress destination_router_client_interface = d.getInterfaceAddrs().get(0);
            String host_network_portion = destinationIp.getBytes()[0]+"."+destinationIp.getBytes()[1]+"."+destinationIp.getBytes()[2];
            String destination_router_network_portion = destination_router_client_interface.getBytes()[0]+"."+destination_router_client_interface.getBytes()[1]+"."+destination_router_client_interface.getBytes()[2];
            if(host_network_portion.equals(destination_router_network_portion)){
                break;
            }
        }

        //System.out.println("Destination router id :"+destination_router_id);
        destRouter = destination_router_id;


        /*if(source_router_index == 0 || destination_router_index == 0){
            System.out.println("No source or destination router found");
            return false;
        }*/



        //traverse from source router to destination router
        hopCount=0;
        path = new String();


        if(routers.get(source_router_index).getState() == false){
            path += routers.get(source_router_index).getRouterId()+"-------->";
            return false;
        }
        ArrayList<RoutingTableEntry> routingTable =routers.get(source_router_index).getRoutingTable();//used to implement DVR
        int sender_router_index = source_router_index;
        int receiver_router_index = sender_router_index;
        path += routers.get(sender_router_index).getRouterId();


        while(true) {

            if(count > Constants.INFTY) {
                return false;
            }

            //To check the condition in 3(a)
            if (routers.get(receiver_router_index).getState() == true) {
                routingTable = routers.get(receiver_router_index).getRoutingTable();

                for (int i = 0; i < routers.size(); i++) {
                    if (routers.get(destination_router_index).getRouterId() == routingTable.get(i).getRouterId()) {
                        if(routingTable.get(i).getDistance() == Constants.INFTY){
                            return false;
                        }


                        sender_router_index = receiver_router_index;
                        path += "-------->";
                        path += routingTable.get(i).getGatewayRouterId();
                        hopCount++;
                        count ++;
                        receiver_router_index = (routingTable.get(i).getGatewayRouterId()) - 1;

                        //To check the condition in 3(b)
                        if(routers.get(receiver_router_index).getState() == true){
                            ArrayList<RoutingTableEntry> receiver_routing_table = routers.get(receiver_router_index).getRoutingTable();
                            for(int j=0;j<routers.size();j++){
                                if(routers.get(sender_router_index).getRouterId() == receiver_routing_table.get(j).getRouterId()){
                                    if(receiver_routing_table.get(j).getDistance() == Constants.INFTY){
                                        routers.get(receiver_router_index).updateRoutingTableRowDistence(routers.get(sender_router_index).getRouterId(),1);
                                        NetworkLayerServer.stateChanger.stopThread();
                                        if(routers.get(receiver_router_index).getState() == true){
                                            DVR(routers.get(receiver_router_index).getRouterId());
                                           // simpleDVR(routers.get(receiver_router_index).getRouterId());
                                        }

                                        NetworkLayerServer.stateChanger.startThread();
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                if(receiver_router_index == destination_router_index && (routers.get(destination_router_index).getState()==true)){
                    break;
                }
            } else {
                routers.get(sender_router_index).updateRoutingTableRowDistence(routers.get(receiver_router_index).getRouterId(), Constants.INFTY);
                NetworkLayerServer.stateChanger.stopThread();
                if(routers.get(sender_router_index).getState() == true){
                    DVR(routers.get(sender_router_index).getRouterId());
                    //simpleDVR(routers.get(sender_router_index).getRouterId());

                }
                NetworkLayerServer.stateChanger.startThread();
                //break;
                return false;

            }
        }

            if(receiver_router_index == destination_router_index && (routers.get(destination_router_index).getState()==true)){
                return true;
            }
            else return false;


        //gateway.getBytes()[0]+"."+gateway.getBytes()[1]+"."+gateway.getBytes()[2]+
        /*
        1. Find the router s which has an interface
                such that the interface and source end device have same network address.
        2. Find the router d which has an interface
                such that the interface and destination end device have same network address.
        3. Implement forwarding, i.e., s forwards to its gateway router x considering d as the destination.
                similarly, x forwards to the next gateway router y considering d as the destination, 
                and eventually the packet reaches to destination router d.
                
            3(a) If, while forwarding, any gateway x, found from routingTable of router x is in down state[x.state==FALSE]
                    (i) Drop packet
                    (ii) Update the entry with distance Constants.INFTY
                    (iii) Block NetworkLayerServer.stateChanger.t
                    (iv) Apply DVR starting from router r.
                    (v) Resume NetworkLayerServer.stateChanger.t
                            
            3(b) If, while forwarding, a router x receives the packet from router y, 
                    but routingTableEntry shows Constants.INFTY distance from x to y,
                    (i) Update the entry with distance 1
                    (ii) Block NetworkLayerServer.stateChanger.t
                    (iii) Apply DVR starting from router x.
                    (iv) Resume NetworkLayerServer.stateChanger.t
                            
        4. If 3(a) occurs at any stage, packet will be dropped, 
            otherwise successfully sent to the destination router
        */    

    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

}
