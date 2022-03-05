import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.net.Socket;
import java.security.PublicKey;

public class ClientHandler implements Runnable
{
    
    public static ArrayList<ClientHandler> client = new ArrayList<>();
    private Socket s;
    private BufferedReader br;
    private BufferedWriter bw;
    private String user;
    private PublicKey publicKey;
    
    
    public ClientHandler(ServerSocket ss)
    {
        try
        {
            this.s = s;
            if(bw == null) this.bw =  new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            if(br == null) this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.user = br.readLine();
            client.add(this);
            announcement("Server" + user + "has connected.");


        } catch(IOException i)
        {
                closer(s,br,bw);
        }
    }

    @Override
    public void run()
    {
        String recieveMessage;
        while(s.isConnected())
        {
            try
            {
                recieveMessage = br.readLine();
                announcement(recieveMessage);
            }catch(IOException e)
            {
                closer(s,br,bw);
                break;
            }
        }
    }
    public void announcement(String m)
    {
        for(ClientHandler c : client)
        {
            try
            {
                if(!c.user.equals(user))
                { 
                    c.bw.write(m);
                    c.bw.newLine();
                    c.bw.flush();
                }
            
            } catch(IOException e)
            {
                closer(s,br,bw);
            }
        } 
    }

    public void removeUser()
    {
        client.remove(this);
        announcement("Server: "+ user + "has left.");
    }

    public void closer(Socket s, BufferedReader br, BufferedWriter bw)
    {
        removeUser();
        try{
        s.close();
        br.close();
        bw.close();;
        } catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}
