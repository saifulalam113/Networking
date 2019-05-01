/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package networklayer;

import java.util.ArrayList;
import java.util.Random;

import static networklayer.NetworkLayerServer.active_client_list;
import static networklayer.NetworkLayerServer.numberOfUpdateHappened;
import static networklayer.NetworkLayerServer.routers;

/**
 *
 * @author samsung
 */
public class Router {
    private int routerId;
    private int numberOfInterfaces;
    private ArrayList<IPAddress> interfaceAddrs;//list of IP address of all interfaces of the router
    private ArrayList<RoutingTableEntry> routingTable;//used to implement DVR
    private ArrayList<Integer> neighborRouterIds;//Contains both "UP" and "DOWN" state routers
    private Boolean state;//true represents "UP" state and false is for "DOWN" state

    public Router() {
        interfaceAddrs = new ArrayList<>();
        routingTable = new ArrayList<>();
        neighborRouterIds = new ArrayList<>();
        
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.90) state = true;
        else state = false;
        //state = true;
        
        numberOfInterfaces = 0;
    }
    
    public Router(int routerId, ArrayList<Integer> neighborRouters, ArrayList<IPAddress> interfaceAddrs)
    {
        this.routerId = routerId;
        this.interfaceAddrs = interfaceAddrs;
        this.neighborRouterIds = neighborRouters;
        routingTable = new ArrayList<>();
        
        /**
         * 80% Probability that the router is up
         */
        Random random = new Random();
        double p = random.nextDouble();
        if(p<=0.90) state = true;
        else state = false;

        //state = true;
        
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    @Override
    public String toString() {
        String temp = "";
        temp+="Router ID: "+routerId+"\n";
        temp+="Intefaces: \n";
        for(int i=0;i<numberOfInterfaces;i++)
        {
            temp+=interfaceAddrs.get(i).getString()+"\t";
        }
        temp+="\n";
        temp+="Neighbors: \n";
        for(int i=0;i<neighborRouterIds.size();i++)
        {
            temp+=neighborRouterIds.get(i)+"\t";
        }
        temp += "\n state: "+state;
        return temp;
    }
    
    
    
    /**
     * Initialize the distance(hop count) for each router.
     * for itself, distance=0; for any connected router with state=true, distance=1; otherwise distance=Constants.INFTY;
     */
    public void initiateRoutingTable()
    {
        if(state == true) {
            System.out.println("Initiate Routing Table : Router "+this.routerId);

            int routerId;
            double distance;
            int gatewayRouterId;
            Router router;
            Boolean state;
            for (int i = 0; i < routers.size(); i++) {
                //System.out.println("------------------\n"+routers.get(i));
                router = routers.get(i);
                state = router.getState();

                routerId = router.routerId;

                if (routerId == this.routerId) {
                    routingTable.add(new RoutingTableEntry(routerId, 0, routerId));
                } else if (isNeighbour(routerId) && (state == true)) {
                    //System.out.println(routerId+" is a neighbor of "+this.routerId);
                    routingTable.add(new RoutingTableEntry(routerId, 1, routerId));
                } else {
                    routingTable.add(new RoutingTableEntry(routerId, Constants.INFTY, -1));
                    //System.out.println(routerId+" is not a neighbor of "+this.routerId);

                }


            }

            this.printRoutingTable();
        }
    }
    
    /**
     * Delete all the routingTableEntry
     */
    public void clearRoutingTable()
    {
        routingTable.clear();
        
    }
    
    /**
     * Update the routing table for this router using the entries of Router neighbor
     * @param neighbor 
     */
    public void updateRoutingTable(Router neighbor)
    {
        ArrayList<RoutingTableEntry> neighborRoutingTable;
        ArrayList<RoutingTableEntry> this_RoutingTable;

        int neighborId;

        neighborRoutingTable = neighbor.getRoutingTable();

        neighborId = neighbor.getRouterId();

        double distanceFromThisRouter;
        double distanceFromNeighborRouter;
        int nexthopFromThisRouter;
        int nexthopFromNeighborRouter;

        //System.out.println(neighborId + "pass routong table to "+ this.getRouterId());

        System.out.println();
        System.out.println("Router "+this.getRouterId() + " gets routong table from router "+ neighborId );
        System.out.println("Router : "+this.routerId+" -------> routing table [before update]");
        this.printRoutingTable();

        if(routingTable.size() > 0){
            for(int i=0;i<routers.size();i++){

                distanceFromThisRouter = routingTable.get(i).getDistance();
                distanceFromNeighborRouter = neighborRoutingTable.get(i).getDistance();
                nexthopFromThisRouter = routingTable.get(i).getGatewayRouterId();
                nexthopFromNeighborRouter = neighborRoutingTable.get(i).getGatewayRouterId();

                if(( (nexthopFromThisRouter == neighborId) && (distanceFromThisRouter != distanceFromNeighborRouter+1)) || ((distanceFromNeighborRouter+1 < distanceFromThisRouter ) && ( this.getRouterId() != nexthopFromNeighborRouter))){
                   // System.out.println("(( ("+nexthopFromThisRouter+" == "+neighborId+") && ("+distanceFromThisRouter+" != "+(distanceFromNeighborRouter+1)+")) || (("+(distanceFromNeighborRouter+1)+" < "+distanceFromThisRouter+" ) && ( "+this.getRouterId()+" !="+ nexthopFromNeighborRouter+"))){"
                   // );
                    //update routing table
                    routingTable.get(i).setDistance(distanceFromNeighborRouter+1);
                    routingTable.get(i).setGatewayRouterId(neighborId);


                    //System.out.println("\n\n\n\n");
                /*for(int k=0;k<routers.size();k++)
                {
                    System.out.println(neighbor.routingTable.get(i).getRouterId()+" is updated by "+neighborId);
                    routers.get(k).printRoutingTable();
                    System.out.println("\n");
                }*/
                    //this.printRoutingTable();
                    numberOfUpdateHappened++;
                }

            }
        }


        System.out.println("Router : "+this.routerId+" -------> routing table [after update]");
        this.printRoutingTable();
        System.out.println();


    }



    public void updateRoutingTableSimple(Router neighbor)
    {
        ArrayList<RoutingTableEntry> neighborRoutingTable;
        ArrayList<RoutingTableEntry> this_RoutingTable;

        int neighborId;

        neighborRoutingTable = neighbor.getRoutingTable();

        neighborId = neighbor.getRouterId();

        double distanceFromThisRouter;
        double distanceFromNeighborRouter;
        int nexthopFromThisRouter;
        int nexthopFromNeighborRouter;

        //System.out.println(neighborId + "pass routong table to "+ this.getRouterId());

        System.out.println();
        System.out.println("Router "+this.getRouterId() + " gets routong table from router "+ neighborId );
        System.out.println("Router : "+this.routerId+" -------> routing table [before update]");
        this.printRoutingTable();

        if(routingTable.size() > 0){
            for(int i=0;i<routers.size();i++){

                distanceFromThisRouter = routingTable.get(i).getDistance();
                distanceFromNeighborRouter = neighborRoutingTable.get(i).getDistance();
                nexthopFromThisRouter = routingTable.get(i).getGatewayRouterId();
                nexthopFromNeighborRouter = neighborRoutingTable.get(i).getGatewayRouterId();

                if((distanceFromNeighborRouter+1 < distanceFromThisRouter )){
                    // System.out.println("(( ("+nexthopFromThisRouter+" == "+neighborId+") && ("+distanceFromThisRouter+" != "+(distanceFromNeighborRouter+1)+")) || (("+(distanceFromNeighborRouter+1)+" < "+distanceFromThisRouter+" ) && ( "+this.getRouterId()+" !="+ nexthopFromNeighborRouter+"))){"
                    // );
                    //update routing table
                    routingTable.get(i).setDistance(distanceFromNeighborRouter+1);
                    routingTable.get(i).setGatewayRouterId(neighborId);


                    //System.out.println("\n\n\n\n");
                /*for(int k=0;k<routers.size();k++)
                {
                    System.out.println(neighbor.routingTable.get(i).getRouterId()+" is updated by "+neighborId);
                    routers.get(k).printRoutingTable();
                    System.out.println("\n");
                }*/
                    //this.printRoutingTable();
                    numberOfUpdateHappened++;
                }

            }
        }


        System.out.println("Router : "+this.routerId+" -------> routing table [after update]");
        this.printRoutingTable();
        System.out.println();


    }
    
    /**
     * If the state was up, down it; if state was down, up it
     */
    public void revertState()
    {
        state=!state;
        if(state==true) this.initiateRoutingTable();
        else {
            this.clearRoutingTable();

            //to remove client from active_client_list
            IPAddress gateway = this.getInterfaceAddrs().get(0);
            String router_network_portion = gateway.getBytes()[0]+"."+gateway.getBytes()[1]+"."+gateway.getBytes()[2];
            for(int i=0;i<active_client_list.size();i++){
                IPAddress client_ip = active_client_list.get(i);
                String client_network_portion = client_ip.getBytes()[0]+"."+client_ip.getBytes()[1]+"."+client_ip.getBytes()[2];

                /*if(router_network_portion.equals(client_network_portion)){
                    active_client_list.remove(i);
                }*/
            }
        }


    }
    
    public int getRouterId() {
        return routerId;
    }

    public void setRouterId(int routerId) {
        this.routerId = routerId;
    }

    public int getNumberOfInterfaces() {
        return numberOfInterfaces;
    }

    public void setNumberOfInterfaces(int numberOfInterfaces) {
        this.numberOfInterfaces = numberOfInterfaces;
    }

    public ArrayList<IPAddress> getInterfaceAddrs() {
        return interfaceAddrs;
    }

    public void setInterfaceAddrs(ArrayList<IPAddress> interfaceAddrs) {
        this.interfaceAddrs = interfaceAddrs;
        numberOfInterfaces = this.interfaceAddrs.size();
    }

    public ArrayList<RoutingTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void addRoutingTableEntry(RoutingTableEntry entry) {
        this.routingTable.add(entry);
    }

    public ArrayList<Integer> getNeighborRouterIds() {
        return neighborRouterIds;
    }

    public void setNeighborRouterIds(ArrayList<Integer> neighborRouterIds) {
        this.neighborRouterIds = neighborRouterIds;
    }

    public Boolean getState() {
        return state;
    }

    public void setState(Boolean state) {
        this.state = state;
    }




    ///////////////////////////////////////////////////////
    public Boolean isNeighbour(int routerId){
        ArrayList<Integer> neighborRouterIds;
        neighborRouterIds = getNeighborRouterIds();
       // System.out.print("Neighbors of "+this.routerId+": ");
        for(int i=0;i<neighborRouterIds.size();i++){
            //System.out.print(neighborRouterIds.get(i));
        }
        //System.out.println();
        for(int i=0;i<neighborRouterIds.size();i++){
            if(routerId == neighborRouterIds.get(i)){
                //System.out.println(routerId+" is  a neighbor of "+this.routerId);
                return true;
            }
        }
        return false;
    }



    public void printRoutingTable(){
        for(int i=0;i<routingTable.size();i++){
            System.out.println(routingTable.get(i).getRouterId()+"   "+routingTable.get(i).getDistance()+"   "+routingTable.get(i).getGatewayRouterId());
        }
    }

    public String getRoutingTableInString(){
        String table=new String();
        for(int i=0;i<routingTable.size();i++){
            table += routingTable.get(i).getRouterId()+"   "+routingTable.get(i).getDistance()+"   "+routingTable.get(i).getGatewayRouterId()+"#";
        }

        return table;
    }


    public void updateRoutingTableRowDistence(int routerId,double distance){
        for(int i=0;i<routers.size();i++){
            if(routerId == this.routingTable.get(i).getRouterId()){
                this.routingTable.get(i).setDistance(Constants.INFTY);
            }
        }
    }
    
    
}
