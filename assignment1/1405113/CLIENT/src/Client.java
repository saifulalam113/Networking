import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;

public class Client {

    public final static int SOCKET_PORT = 13267;      // you may change this
    public final static String SERVER = "127.0.0.1";  // localhost
    public final static String
            FILE_TO_RECEIVED = "c:/temp/source___downloaded.pdf";  // you may change this, I give a
    // different name because i don't want to
    // overwrite the one used by server...

    public  static int FILE_SIZE = 60223860; // file size temporary hard coded
    // should bigger than the file to be downloaded


    public  static String FILE_TO_SEND = null;  // you may change this

    public static void main (String [] args ) throws IOException {
        int bytesRead;
        int current = 0;
        int chunkSize = 0;
        int total;
        String  input;
        String input2;
        int student_id_to_send=0;

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        FileInputStream fis = null;
        BufferedInputStream bis = null;

        InputStream is = null;
        OutputStream os = null;
        BufferedReader in = null;
        PrintWriter out = null;
        BufferedReader stdIn = null;


        File myFile=null;
        int fileSize=0;

        Socket sock = null;
        try{
            sock = new Socket(SERVER, SOCKET_PORT);
            System.out.println("Connecting...");
            is = sock.getInputStream();
            os = sock.getOutputStream();
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(os, true);

            stdIn =
                    new BufferedReader(
                            new InputStreamReader(System.in));
        }catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Enter student id:");
        int id;
        if((input=stdIn.readLine())!=null){
            id = Integer.parseInt(input);
            out.println(id);
        }


        int confirmation;
        if((input=in.readLine())!=null){
            confirmation = Integer.parseInt(input);
            if(confirmation==0){
                System.out.println("You cannot connected");
            }
            else{
                System.out.println("You are connected!!!!");
                int num = 0;
                if((input = in.readLine()) != null){
                    //System.out.println("What do you want?\n Send File : 1\nReceive File : 2\nRefresh : 3");


                    while(true){
                        System.out.println("What do you want?");
                        System.out.println("For File Sending : Enter 1");
                        System.out.println("For Receiving File (If Any) : Enter 2");
                        System.out.println("For Refreshing : Enter 3");
                        int message=0;
                        if((input = in.readLine())!=null){
                            message = Integer.parseInt(input);
                            //System.out.println("From server about message: "+message);
                            if(message!=1){
                                System.out.println("No pending message to receive.!!!!!");
                                System.out.println();
                                System.out.println();
                            }
                            else{
                                if((input = in.readLine())!=null){
                                    System.out.println("A pending File to Receive:");
                                    System.out.println(input);
                                    System.out.println();
                                    System.out.println();
                                }


                            }
                        }





                        while((input2 = stdIn.readLine())!=null){
                            num = Integer.parseInt(input2);
                            if(num==1 | num ==2 | num == 3) break;
                            else System.out.println("Please Enter 1 or 2 or 3");
                        }
                        out.println(num);
                        switch(num){
                            case 1:{//client wants to send

                                try {
                                    // send file
                                    System.out.println("Enter student id to whom you want to send file:");
                                    student_id_to_send = Integer.parseInt(stdIn.readLine());
                                    out.println(student_id_to_send);


                                    int student_to_send_coneected=0;
                                    if((input = in.readLine())!=null){
                                        student_to_send_coneected = Integer.parseInt(input);


                                       /* out.println(student_to_send_coneected);//to recheck that if he is here
                                        while()*/

                                        if(student_to_send_coneected==0){
                                            System.out.println("Student not connected to whom you want to send file.");
                                            System.out.println("Sending fail.");
                                            System.out.println();
                                            System.out.println();
                                            break;


                                        }
                                        else{
                                            System.out.println("Enter file name:");
                                            FILE_TO_SEND = stdIn.readLine();
                                            out.println(FILE_TO_SEND);
                                            myFile = new File(FILE_TO_SEND);
                                            fileSize = (int) myFile.length();

                                            System.out.println("File Size :"+fileSize);
                                            out.println(fileSize);

                                            int fileLimit;
                                            if((input=in.readLine())!=null){
                                                fileLimit = Integer.parseInt(input);
                                                if(fileLimit==0){
                                                    System.out.println("Buffer limit exceedes");
                                                    break;
                                                }
                                                else{
                                                    //getting chunk size from server
                                                    System.out.println("Buffer limit within limit");

                                                    String chunk;
                                                    if((chunk=in.readLine())!=null){
                                                        chunkSize = Integer.parseInt(chunk);
                                                        System.out.println("Chunk Size : "+chunkSize);
                                                    }

                                                    //reading from file
                                                    byte [] mybytearray  = new byte [fileSize];
                                                    fis = new FileInputStream(myFile);
                                                    bis = new BufferedInputStream(fis);
                                                    bis.read(mybytearray,0,mybytearray.length);

                                                    //starting sending
                                                    System.out.println("Sending " + FILE_TO_SEND + "(" + mybytearray.length + " bytes)");
                                                    //os.write(mybytearray,0,mybytearray.length);

                                                    total =0;
                                                    int reading_length;
                                                    int remain=fileSize;

                                                    while(total != fileSize){
                                                        os.write(mybytearray,total,chunkSize);
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
                                                    while((input = in.readLine())!=null){
                                                        if(Integer.parseInt(input)==1){
                                                            System.out.println("sending succesful "+input);
                                                            break;
                                                        }

                                                    }
                                                }
                                            }
                                        }

                                    }










                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    if (bis != null) try {
                                        bis.close();
                                        //if (os != null) os.close();
                                        //if (sock!=null) sock.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }
                                break;
                            }//case 1 ends here
                            case 2:{//clients wants to receive

                                try{

                                    System.out.println("hello");

                                    // receive file
                                    byte [] mybytearray  = new byte [FILE_SIZE];
                                    System.out.println("hello 5");

                                    fos = new FileOutputStream(FILE_TO_RECEIVED);
                                    bos = new BufferedOutputStream(fos);

                                    out.println(100);

                                    //to get file size
                                    if((input = in.readLine())!=null){
                                        fileSize = Integer.parseInt(input);
                                    }

                                    total = 0;
                                    chunkSize = 10000;
                                    int remain ;
                                    int s = 1;
                                    while(total!=fileSize){
                                        bytesRead = is.read(mybytearray,total,chunkSize);
               /* while(bytesRead != chunkSize){
                    bytesRead = bytesRead + is.read(mybytearray,total,chunkSize-bytesRead);
                }*/
                                        out.println(s);//to inform that getting 1 chunk
                                        s++;
                                        total = total+bytesRead;
                                        remain = fileSize - total;
                                        System.out.println("bytesRead = "+bytesRead+" ,File  downloaded (" + total + " bytes read)");

                                        if(chunkSize>remain){
                                            chunkSize = remain;
                                        }
                                    }

                                    out.println(50);
                                    bos.write(mybytearray, 0 , total);
                                    bos.flush();
                                    System.out.println("File " + FILE_TO_RECEIVED
                                            + " downloaded (" + current + " bytes read)");
            /*bytesRead = is.read(mybytearray,0,mybytearray.length);
            current = bytesRead;




                do {
                bytesRead =
                        is.read(mybytearray, current, (mybytearray.length-current));
                    System.out.println("Download "+current);

                    if(bytesRead >= 0) current += bytesRead;
            } while(current < 704326);

            bos.write(mybytearray, 0 , current);
            bos.flush();
            System.out.println("File " + FILE_TO_RECEIVED
                    + " downloaded (" + current + " bytes read)");*/


               /* int chunkSize = 10000;
                int remain ;
                int s = 1;
                int total = 0;
                System.out.println("hello 6");

                while(total!=fileSize){
                    System.out.println("hello 07");

                    bytesRead = is.read(mybytearray,total,chunkSize);
                    System.out.println("hello 7");

                    /*while(bytesRead != chunkSize){
                        bytesRead = bytesRead + is.read(mybytearray,total,chunkSize-bytesRead);
                        System.out.println("hello 9");

                    }*/
                    /*System.out.println("hello 8");

                    // out.println(s);//to inform that getting 1 chunk
                    s++;
                    total = total+bytesRead;
                    remain = fileSize - total;
                    System.out.println("bytesRead = "+bytesRead+" ,File  downloaded (" + total + " bytes read)");

                    if(chunkSize>remain){
                        chunkSize = remain;
                    }
                }*/
                /*out.println(1);
                bos.write(mybytearray, 0 , total);
                bos.flush();
                System.out.println("File " + FILE_TO_RECEIVED
                        + " downloaded (" + current + " bytes read)");*/




                                }
                                finally {
                                    if (fos != null) fos.close();
                                    if (bos != null) bos.close();
                                    //if (sock != null) sock.close();
                                }
                                break;
                            }//case 2 ends here
                            case 3:{
                                break;
                            }//case 3 ends here
                        }


                    }
                }




                // byte [] myarray  = new byte [5];
                // myarray[0]=(byte)((int)myFile.length());
                //System.out.println(myarray[0]);



                //final BigInteger bi = BigInteger.valueOf(704326);
                //final byte[] bytes = bi.toByteArray();

                //System.out.println(Arrays.toString(bytes));

                //final int i = new BigInteger(bytes).intValue();
                //System.out.println(i);



                //os.write(bytes,0,bytes.length);

            }
        }









    }

}