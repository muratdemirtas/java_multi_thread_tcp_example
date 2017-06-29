/**
 * Multi-Thread Based TCP Server Example
 * @author muratdemirtas <muratdemirtastr@gmail.com>
 * @version 1.0.0
 * @since  2017-06-29
 */

/*import libraries*/
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

import com.google.gson.*;


/**
 * create  class extend from thread
 * @see java.lang.Thread
 */
public class tcpServerListener extends Thread {

    /*create socket for clients*/
    protected Socket clientSocket;

    /*set socket descriptor and start thread for connected clients*/
    public tcpServerListener(Socket clientSocket){
        this.clientSocket = clientSocket;
        start();
    }

    /*main function of this class*/
    public static void main(String[] args) {


        int m_serverPort = 5000;  /*set server port*/

        /*Create a server socket,*/
        ServerSocket serverSocket = null;

        /*try to open port*/
        try{
            /*and bound server to the specified port.*/
            serverSocket = new ServerSocket(m_serverPort);

            /*forever loop of main method*/
            while (1==1)
            {

                try
                {
                    System.out.println("TCP Server listening on port : " + m_serverPort);
                    new tcpServerListener(serverSocket.accept());

                }   catch (IOException e) {
                    System.err.println("Error occurred while opening TCP Server err: " +e);
                }
            }
        } catch (IOException e) {
            System.out.println("TCP server cant listen port: "+ m_serverPort);
            System.out.println("port already open or another process using it?");
            System.exit(2);
        }
    }

    @Override
    public void run(){
        System.out.println("[TCP]New thread created for this session");
        System.out.println("[TCP]Thread ID is: " + Thread.currentThread().getId());

        try{
            String m_clientRecvData= "";
            String m_clientIPAddress = clientSocket.getInetAddress().toString();
            System.out.println("[TCP]New Connection!! IP: " + m_clientIPAddress );


            if(m_clientIPAddress.equals("/127.0.0.1"))
                System.out.println("[TCP]This connection coming from localhost");

            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));

            while ((m_clientRecvData = in.readLine()) != null)
            {
                System.out.println("[TCP]Received Data From: " + m_clientIPAddress +
                        " "+ m_clientRecvData );

                if(m_clientRecvData.equals("exit"))
                {
                    System.out.println("[TCP]exit command received, connection closing");
                    out.println("[TCP]Connection closing by server, bye bye");
                    clientSocket.close();
                }

                String response = parseJsonMessage(m_clientRecvData);
                out.println(response);




            }
            System.out.println("[TCP]Remote client closed connection,thread exiting");
            clientSocket.close();

        } catch(IOException e)
        {
            System.err.println(e.getMessage());
        }
    }


    private String parseJsonMessage(String data)
    {

        String response = "";

        try{

            JsonElement root = new JsonParser().parse(data);

            if (root.isJsonObject()) {
                JsonObject  root_object = root.getAsJsonObject();
                String FROM = root_object.get("FROM").getAsString();
                System.out.println("[TCP]Request From: "+ FROM);

                JsonArray cmd_list = root_object.getAsJsonArray("REQUESTS");
                JsonArray mainArray = new JsonArray();

                /*
                System.out.println(root_object.toString());
                System.out.println(cmd_list.toString());
                */
                int array_count=0;
                for(JsonElement cmds : cmd_list){

                    if(cmd_list.get(array_count).getAsString().equals("GET_SYS_TIME"))
                    {
                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();
                        mainArray.add(dateFormat.format(date));
                    }

                    if(cmd_list.get(array_count).getAsString().equals("GET_SYS_UPTIME"))
                        mainArray.add(System.currentTimeMillis());


                    if(cmd_list.get(array_count).getAsString().equals("GET_IP_ADDR"))
                    {
                        try
                        {
                            InetAddress IP= InetAddress.getLocalHost();
                            mainArray.add(IP.getHostAddress());
                        }
                        catch (IOException x){
                            System.err.println("error occurred while reading IP");
                        }
                    }

                    array_count++;
                }

                return mainArray.toString();

            }
            else{
                System.out.println("data cant parse with gson");
            }

        }
        catch (JsonParseException e){

        }

        return response;
    }

    }
