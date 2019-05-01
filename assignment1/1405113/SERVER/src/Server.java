import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;

public class Server {

    public final static int SOCKET_PORT = 13267;  // you may change this
    public final static String FILE_TO_SEND = "D:\\Study\\Versity_study\\Level3_term2\\Course_Outline_CSE317.pdf";  // you may change this
    public final static int FILE_SIZE = 704326; // file size temporary hard coded

    String a = "D:\\Study\\Versity_study\\Level3_term2\\notice";

    public static byte [] ultimate = new byte[3000000];
    public static int used = 0;
    public static int start = 0;
    public static int end = 0;


    public static LinkedList<Client> clients = new LinkedList<Client>();


    public static int[][] arr = new int[100][2];
    public static int arrLength = 0;






    public static void main (String [] args ) throws IOException {
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        OutputStream os = null;
        InputStream is = null;
        BufferedReader in = null;
        PrintWriter out = null;

        ServerSocket servsock = null;
        Socket sock = null;

        int flagg = 0;
        int id =0;
        String input = null;

        Iterator<Client> itr;


        //to Maintain buffer size,exactly to clear buffer
        Buffer b;
        b = new Buffer();
        Thread thread = new Thread(b);
        thread.start();




        try {
            servsock = new ServerSocket(SOCKET_PORT);
            while (true) {
                ClientWorker w;
                System.out.println("Waiting...");

                    sock = servsock.accept();
                    System.out.println("Accepted connection : " + sock);

                    try {
                        os = sock.getOutputStream();
                        is = sock.getInputStream();
                        out = new PrintWriter(sock.getOutputStream(),true);
                        in = new BufferedReader(new InputStreamReader(is));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //to retrive student id from client
                    if((input=in.readLine())!=null){
                        id = Integer.parseInt(input);
                    }

                    itr = clients.iterator();
                    flagg = 0;
                    //to check if he is already logged in
                    while(itr.hasNext()){
                        Client client = itr.next();
                        System.out.println("id :"+client.id);
                        if(client.id==id){
                            flagg = 1;
                            break;
                        }
                    }


                    if(flagg==0){
                        //client do not logged in so he can log in now
                        System.out.println("New clientds added with id :"+id);
                        clients.add(new Client(id,sock));
                        //to send confirmation that he is now connected
                        out.println(1);
                        w = new ClientWorker(id,sock);
                        Thread t = new Thread(w);
                        t.start();
                    }
                    else{
                        //to send confirmation that he can not connect now
                        out.println(0);
                    }






            }
        }
        finally {
            if (servsock != null) servsock.close();
        }
    }
}


class ClientWorker implements Runnable {

    private int id;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int start;
    private int end;


    //Constructor
    ClientWorker(int id,Socket socket) {
        this.id=id;
        this.socket = socket;



    }

    public void run() {
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;

        OutputStream os = null;
        InputStream is = null;
        PrintWriter out = null;
        BufferedReader in = null;

        int bytesRead;
        int current = 0;
        String input=null;
        int fileSize=0;
        String fileName=null;
        int total = 0;
        int chunkSize=0;
        int student_id_to_send=0;
        Iterator<Client> itr=null;
        int present =0;
        int file_start_index=0;
        int file_end_index=0;


        //byte [] myarray = new byte[3];
        //byte [] mybytearray  = new byte [Server.FILE_SIZE];
        byte [] mybytearray  = null;



        try {
            os = socket.getOutputStream();
            is = socket.getInputStream();
            out = new PrintWriter(socket.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(is));
        } catch (IOException e) {
            System.out.println("Line disconnected........");
            itr = Server.clients.iterator();
            while(itr.hasNext()){
                Client client15 = itr.next();
                if(client15.id==this.id){
                    Server.clients.remove(client15);
                    break;
                }

            }
            return;
        }

        out.println(100);//asking client what he wants
        System.out.println("asking client for what he wants?");
        int num;
        int flag ;
        while(true){
            flag = 0;
            System.out.println("14");

            //Send message to clients if he has any pending message to receive
            itr = Server.clients.iterator();
            int message=0;
            Client client;
            while(itr.hasNext()){
                client = itr.next();
                System.out.println("id :"+client.id+", message :"+client.message);
                if(client.id==this.id){
                    message = client.message;
                    if(message==0){
                        out.println(message);
                    }
                    else{
                        out.println(message);
                        System.out.println("Sending message :"+message);
                        String info = "From Id :"+client.student_id_who_send+"File Name : "+client.filename+" , Size:"+client.fileSize;
                        out.println(info);
                    }
                    break;
                }
            }


            try {
                if((input=in.readLine())!=null){

                    num = Integer.parseInt(input);
                    System.out.println(num);
                    switch(num){
                        case 1:{///clients want to sends
                            try{
                                System.out.println("clients want to send file to server");
                                if((input = in.readLine())!=null){
                                    student_id_to_send = Integer.parseInt(input);
                                    itr = Server.clients.iterator();
                                    present = 0;
                                    while(itr.hasNext()){
                                        Client client2 = itr.next();
                                        if(client2.id==student_id_to_send){
                                            //client.message = 1;
                                            present = 1;
                                            System.out.println("Client with id "+student_id_to_send+" is present.............");
                                            break;
                                        }
                                    }

                                }
                                if(present==1){
                                    out.println(1);



                                    //for retriving file name
                                    if((input = in.readLine())!=null){
                                        fileName = input;
                                    }


                                    //for retriving file size
                                    if((input = in.readLine())!=null){
                                        fileSize = Integer.parseInt(input);
                                        mybytearray  = new byte [fileSize];
                                    }



                                    System.out.println("File  size = " + fileSize);
                                    System.out.println("File size + buffer ="+(Server.used+fileSize));
                                    if((Server.used+fileSize)>(Server.ultimate.length)){

                                        out.println(0);
                                    }
                                    else {
                                        out.println(1);

                                        //making place for this file to store
                                        file_start_index = Server.used;
                                        Server.used += fileSize;
                                        file_end_index=Server.used-1;

                                        Server.arr[Server.arrLength][0] =student_id_to_send;
                                        Server.arr[Server.arrLength][1] =fileSize;
                                        Server.arrLength++;



                                        out.println(10000);



                                        //starting file sending...
                                        total = 0;
                                        chunkSize = 10000;
                                        int remain ;
                                        int s = 1;
                                        while(total!=fileSize){
                                            bytesRead = is.read(mybytearray,total,chunkSize);
                                            out.println(s);//to inform that getting 1 chunk
                                            System.out.println("sending "+s);
                                            s++;
                                            total = total+bytesRead;
                                            remain = fileSize - total;
                                            System.out.println("bytesRead = "+bytesRead+" ,File  downloaded (" + total + " bytes read)");

                                            if(chunkSize>remain){
                                                chunkSize = remain;
                                            }
                                        }

                                        //bos.write(mybytearray, 0 , current);
                                        //bos.flush();
                                        System.out.println("File  downloaded (" + current + " bytes read)");
                                        out.println(1);



                                        int m = file_start_index;

                                        for(int j=0;j<fileSize;j++){
                                            Server.ultimate[m] = mybytearray[j];
                                            m++;

                                        }








                                        //to update client profile
                                        itr = Server.clients.iterator();
                                        int dFlag=0;
                                        while(itr.hasNext()){
                                            Client client5 = itr.next();
                                            if(client5.id==student_id_to_send){
                                                System.out.println("Saving student id:"+student_id_to_send);
                                                dFlag=1;
                                                client5.message=1;
                                                client5.start_index=file_start_index;
                                                client5.end_index=file_end_index;
                                                client5.fileSize=fileSize;
                                                client5.student_id_who_send = this.id;
                                                client5.filename = fileName;
                                                break;
                                            }
                                        }


                                        //If the receiver logged out then the file should be deleted
                                        if(dFlag==0){
                                            System.out.println("used ========= "+Server.used);

                                            for(int i=Server.arrLength-1;i>=0;i--){
                                                if(Server.arr[i][0]==student_id_to_send) {
                                                    Server.arr[i][0]=0;
                                                    break;
                                                }


                                            }



                                            System.out.println("used ========= "+Server.used);

                                            int k = Server.arrLength;
                                            for(int i=k-1;i>=0;i--){
                                                if(Server.arr[i][0]==0) {
                                                    Server.used -= Server.arr[i][1];
                                                    Server.arrLength--;
                                                }
                                                else break;
                                            }

                                            System.out.println("used ========= "+Server.used);
                                        }


                                    }
                                }
                                else{
                                    out.println(0);
                                }
                            } catch (IOException e) {
                                System.out.println("Line disconnected........");
                                itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client15 = itr.next();
                                    if(client15.id==this.id){
                                        Server.clients.remove(client15);
                                        break;
                                    }

                                }
                                return;
                            }
                            break;
                        }
                        case 2:{//clients want to receive
                            try {

                                // send file

                                if((input = in.readLine())!=null){
                                    System.out.println("Sending starting "+input);
                                }


                                int startSendingIndex=0;
                                int endSendingIndex=0;
                                itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client10 = itr.next();
                                    if(client10.id==this.id){
                                        fileSize = client10.fileSize;
                                        startSendingIndex=client10.start_index;
                                        endSendingIndex=client10.end_index;
                                        client10.message=0;
                                        break;
                                    }

                                }


                                //sending file size to client
                                out.println(fileSize);

                                total =0;
                                chunkSize=10000;
                                int reading_length;
                                int remain=fileSize;


                                while(total != fileSize){
                                    os.write(Server.ultimate,startSendingIndex+total,chunkSize);
                                    os.flush();
                                    System.out.println("Sending "+chunkSize+"bytes");
                                    if((input = in.readLine())!=null){
                                        System.out.println("server getting confermation"+input);
                                    }


                                    total = total + chunkSize;
                                    remain = fileSize-total;
                                    System.out.println("remain "+remain+"bytes");

                                    if(chunkSize>remain){
                                        chunkSize = remain;
                                    }


                                }
                                os.flush();
                                System.out.println("Done.");
                                if((input = in.readLine())!=null){
                                    System.out.println("sending succesful "+input);
                                }



                                System.out.println("used ========= "+Server.used);

                                for(int i=Server.arrLength-1;i>=0;i--){
                                    if(Server.arr[i][0]==this.id) Server.arr[i][0]=0;

                                }



                                System.out.println("used ========= "+Server.used);

                                int k = Server.arrLength;
                                for(int i=k-1;i>=0;i--){
                                    if(Server.arr[i][0]==0) {
                                        Server.used -= Server.arr[i][1];
                                        Server.arrLength--;
                                    }
                                    else break;
                                }

                                System.out.println("used ========= "+Server.used);

                                //os = socket.getOutputStream();
            /*System.out.println("Sending " + Server.FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
            os.write(mybytearray,0,current);
            os.flush();
            System.out.println("Done.");
            in.readLine();*/

                            } catch (FileNotFoundException e) {
                                System.out.println("Line disconnected........");
                                itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client15 = itr.next();
                                    if(client15.id==this.id){
                                        Server.clients.remove(client15);
                                        break;
                                    }

                                }
                                return;
                            } catch (IOException e) {
                                System.out.println("Line disconnected........");
                                itr = Server.clients.iterator();
                                while(itr.hasNext()){
                                    Client client15 = itr.next();
                                    if(client15.id==this.id){
                                        Server.clients.remove(client15);
                                        break;
                                    }

                                }
                                return;
                            }
                            break;
                        }//case 2 ends here

                        case 3:{
                            //flag = 1;
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Line disconnected........");
                itr = Server.clients.iterator();
                while(itr.hasNext()){
                    Client client15 = itr.next();
                    if(client15.id==this.id){
                        Server.clients.remove(client15);
                        break;
                    }

                }
               return;
            }

            //to break while loop
            /*if(flag == 1){
                break;
            }*/
        }






        /*try {
            if((input = in.readLine())!=null){
                System.out.println(input);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }*/







    }
}






class Buffer implements Runnable{



    public  void run(){
        int i;
        int flag;
        /*while(true){
            for(i=Server.arrLength-1;i>=0;i++){
                if(Server.arr[i][0]==0) Server.used -= Server.arr[i][1];
                else break;
            }
        }*/
        Iterator<Client> itr = null;
        while(true){
            int j = Server.arrLength;
            itr = Server.clients.iterator();
            for(i=0;i<j;i++){
                flag = 0;
                while(itr.hasNext()){
                    Client client= itr.next();
                    if(Server.arr[i][0]==client.id){
                        flag = 1;
                        break;
                    }
                }

                if(flag==0){
                    Server.arr[i][0]=0;
                }
            }

        }
    }
}