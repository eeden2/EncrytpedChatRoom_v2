import java.net.*;
import java.io.*;
import java.util.*;

public class Server
{
    private ServerSocket server;

    // constructor with port
    public Server(ServerSocket ss) {
        this.server = ss;
        // starcdts server and waits for a connection
    }
    public void startServer()
    {
        try
        {
            System.out.println("Server started");
 
            System.out.println("Waiting for a client ...");
 
            while(!server.isClosed())
            {
                Socket socket = server.accept();
                System.out.println("Client accepted");
 
                ClientHandler cH = new ClientHandler(server);
                Thread t = new Thread(cH);
                t.start();
            }
            
        }
        catch(IOException i)
        {
            System.out.println("HOuston, we have a problem\n"+i);
        }
    }
    public void closeSocket()
    {
        try
        {   
            if(server != null) server.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
 
    public static void main(String args[]) throws IOException
    {
        //Using port 5000 
        ServerSocket ss = new ServerSocket(5000);
        Server s = new Server(ss);
        s.startServer();
    }
}