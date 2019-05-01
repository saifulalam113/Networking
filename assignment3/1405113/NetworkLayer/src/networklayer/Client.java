/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


import static networklayer.NetworkLayerServer.clientInterfaces;
import static networklayer.NetworkLayerServer.routers;

/**
 *
 * @author samsung
 */
public class Client {
    public static void main(String[] args)
    {
        Socket socket;
        IPAddress ip=null;
        IPAddress gateway;
        ObjectInputStream input;
        ObjectOutputStream output;
        ArrayList<IPAddress> active_client_list = new ArrayList<>();
        BufferedReader in = null;
        PrintWriter out = null;
        ArrayList<Integer> hopCounts = new ArrayList<>();
        
        try {
            socket = new Socket("localhost", 1234);
            /*input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            output.flush();*/

            out = new PrintWriter(socket.getOutputStream(),true);
            in =  new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ip = new IPAddress(in.readLine());
            gateway = new IPAddress(in.readLine());
            System.out.println("\n\n\n\n\n\nGet Ip : "+ip);
            out.println("hello");
        } catch (IOException ex) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("Connected to server");



        /**
         * Tasks
         */




        //delay();

        for(int i=1;i<=100;i++){
            System.out.println("\n----------------------------------------------------------------------");
            System.out.println("                  Iteration number : "+(i));
            System.out.println("----------------------------------------------------------------------\n");



            //delay(1000);
            //update active client list
            try {
                String ip_string;
                active_client_list.clear();
                while(!(ip_string=in.readLine()).equals("1") ){
                    if(!(ip.getString().equals(ip_string))){
                        active_client_list.add(new IPAddress(ip_string));
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            



            /*System.out.println("\n\nactive client list receiving from server :"+active_client_list.size());
            for(int l=0;l<active_client_list.size();l++){

                System.out.println(active_client_list.get(l).getString());

            }
            System.out.println("\n\n");
            */



            if(active_client_list.size() != 0){
                String message = "I am an interesting Client";

                // Random random = new Random();
                //  random.nextInt(active_client_list.size())
                Random random = new Random();
                int randomReceiverIndex;
                if(active_client_list.size() == 1) randomReceiverIndex=0;
                else{
                    //int randomReceiverIndex = random.nextInt() % (active_client_list.size());
                    randomReceiverIndex= random.nextInt(active_client_list.size()-1);
                }


               // System.out.println("random receiver index :"+randomReceiverIndex+":::::::active_client_size: "+active_client_list.size());


                IPAddress randomReceiverIPAddress = active_client_list.get(randomReceiverIndex);





                if(i==20){
                    out.println("1");
                    out.println(message);
                    out.println("SHOW_ROUTE");
                    out.println(ip.getString());
                    out.println(randomReceiverIPAddress.getString());





                    //receive message from server
                    String result_from_server = null;
                    String path=new String();
                    int hopcount = 0;
                    int total_router = 0;
                    try {



                        System.out.println("Source : "+in.readLine());
                        System.out.println("Destination : "+in.readLine());

                        result_from_server = in.readLine();  //out.println("successful");  or  out.println("dropped");
                        if(result_from_server.equals("successful")){
                            hopcount = Integer.parseInt(in.readLine());
                            hopCounts.add(hopcount);  //out.println(hopCount);
                        }


                        path = in.readLine();  //out.println(path);


                        total_router = Integer.parseInt(in.readLine());  //out.println(routers.size());





                        if(result_from_server.equals("successful")){
                            System.out.println("Route : "+path+"   [successful]");
                            System.out.println("Hop count : "+hopcount);
                        }
                        else if(result_from_server.equals("dropped")){
                            System.out.println("Path : "+path+"   [dropped]");
                            //System.out.println("Hop count : "+hopcount);
                        }
                        else if(result_from_server.equals("dropped for infinite loop")){
                            System.out.println("Path : "+path+"   [dropped for infinite loop]");
                            //System.out.println("Hop count : "+hopcount);
                        }



                        String table = new String();
                        for(int k=1;k<=total_router;k++)
                        {
                            System.out.println("------------------\n");
                            System.out.println("Router : "+k+"\n");
                            table = in.readLine();    //out.println(routers.get(k));
                            int start_index = 0;
                            int end_index = table.indexOf('#');
                            while(end_index>0){
                                System.out.println(table.substring(start_index,end_index));
                                start_index = end_index+1;
                                end_index = table.indexOf('#',end_index+1);
                            }

                        }






                    } catch (IOException e) {
                        e.printStackTrace();
                    }







                }
                else{
                    out.println("2");
                    out.println(message);
                    out.println(ip.getString());
                    out.println(randomReceiverIPAddress.getString());







                    //receive message from server
                    String result_from_server = null;
                    String path=new String();
                    int hopcount = 0;
                    try {

                        System.out.println("Source : "+in.readLine());
                        System.out.println("Destination : "+in.readLine());

                        result_from_server = in.readLine();  //out.println("successful");  or  out.println("dropped");
                        if(result_from_server.equals("successful")){
                            hopcount = Integer.parseInt(in.readLine());
                            hopCounts.add(hopcount);  //out.println(hopCount);
                        }


                        path = in.readLine();  //out.println(path);



                        if(result_from_server.equals("successful")){
                            System.out.println("Route : "+path+"   [successful]");
                            System.out.println("Hop count : "+hopcount);
                        }
                        else if(result_from_server.equals("dropped")){
                            System.out.println("Path : "+path+"   [dropped]");
                            //System.out.println("Hop count : "+hopcount);
                        }
                        else if(result_from_server.equals("dropped for infinite loop")){
                            System.out.println("Path : "+path+"   [dropped for infinite loop]");
                            //System.out.println("Hop count : "+hopcount);
                        }





                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }
            else{
                System.out.println("No active client found");
                out.println("3");
            }

        }

            /*
            1. Receive EndDevice configuration from server
            2. [Adjustment in NetworkLayerServer.java: Server internally
                handles a list of active clients.]
            3. for(int i=0;i<100;i++)
            4. {
            5.      Generate a random message
            6.      [Adjustment in ServerThread.java] Assign a random receiver from active client list
            7.      if(i==20)
            8.      {
            9.            Send the messageto server and a special request "SHOW_ROUTE"
            10.           Display routing path, hop count and routing table of each router [You need to receive
                                all the required info from the server in response to "SHOW_ROUTE" request]
            11.     }
            12.     else
            13.     {
            14.           Simply send the message and recipient IP address to server.
            15.     }
            16.     If server can successfully send the message, client will get an acknowledgement along with hop count
                        Otherwise, client will get a failure message [dropped packet]
            17. }
            18. Report average number of hops and drop rate
            */



        /*System.out.println("Active client List:::::");
        for(int i=0;i<active_client_list.size();i++){
            System.out.println(active_client_list.get(i));
        }*/

        //to remove client from active_client_list


        try {
            String ip_string;
            active_client_list.clear();
            while(!(ip_string=in.readLine()).equals("1") ){
                if(!(ip.getString().equals(ip_string))){
                    active_client_list.add(new IPAddress(ip_string));
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("sending end connection");
        out.println("4");


        out.println(ip.getString());

        //Calculate avg hop count
        double sum_of_hop_count = 0;
        for(int i=0;i<hopCounts.size();i++){
            sum_of_hop_count += hopCounts.get(i);
        }

        double avg_hop_count = 0.0;
        if(hopCounts.size()>0){
            avg_hop_count = sum_of_hop_count/hopCounts.size();
        }


        //avg_hop_count = sum_of_hop_count/100;




        System.out.println("Average Hop Count :"+avg_hop_count);
        //System.out.println("Sucess rate : "+(hopCounts.size())+"%");
        System.out.println("Drop rate : "+(100-(hopCounts.size()))+"%");





    }

    public static void delay(int timeInmili){
        long start_time = System.currentTimeMillis()+timeInmili;

        while(System.currentTimeMillis()<start_time);

    }
}
