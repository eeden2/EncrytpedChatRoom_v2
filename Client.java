import java.net.*;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.io.*;
import java.util.*; 

public class Client
{
    // initialize socket and input output
    private Socket               socket  = null;
    private BufferedReader       input   = null;
    private BufferedWriter       out     = null;
    private String user;
    private final  byte[] keyByte = ";iosadlifdsbvufb".getBytes();
    private HashSet<PublicKey> getPubs = new HashSet<>();
    private final Encryption encryptionLink = new Encryption();

    // constructor to put ip address and port
    public Client(Socket socket, String username) throws Exception
    {
       // establish a connection
        try
        {
            this.socket = socket;
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.user = username;
            
        }
        catch(IOException i)
        {
            closer(socket,input,out);
        }
        
    }

    public void sender() throws Exception
    {
        try
        {
            out.write(user);
            out.newLine();
            out.flush();

            
            
            // Sending the Public Key over the network
            ByteBuffer bb = ByteBuffer.allocate(4);
            bb.putInt(encryptionLink.returnPublic().getEncoded().length);
            socket.getOutputStream().write(bb.array());
            socket.getOutputStream().write(encryptionLink.returnPublic().getEncoded());
            //En Encryption
            
            
            Scanner s = new Scanner(System.in);
            while(socket.isConnected())
            {
                String line = s.nextLine();
                System.out.print("Do you want to Encrypt the Message?(Y/N) ");
                String temp = s.next();
                if(temp.equals("Y"))
                {
                    encryptionLink.EncryptMessage(line.getBytes(), keyByte);
                    out.write(user + ": "+line);
                    out.newLine();
                    out.flush();
                }
                else
                {
                    out.write(user + ": "+line);
                    out.newLine();
                    out.flush();
                }
            }
        } catch (IOException e)
        {
            closer(socket,input,out);
        }
    }

    public void listener()
    {
        
        new Thread(new Runnable() {
           @Override
           public void run()
           {
            String msgFromChat;
            while(socket.isConnected())
            {
                
                try
                {
                    msgFromChat = input.readLine();
                    System.out.println(msgFromChat);
                } catch(IOException e)
                {
                    closer(socket,input,out);
                }
            }

           }
        }).start();
    }
    
    public void closer(Socket s, BufferedReader br, BufferedWriter out)
    {
        try
        {
            s.close();
            br.close();
            out.close();
        }catch(IOException e)
        {
            e.printStackTrace();
        }
    }
 
    public static void main(String args[]) throws Exception
    {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter a Username: ");
        String u = s.nextLine();
        Socket socket = new Socket("172.20.10.2", 5000);
        Client client = new Client(socket,u);
        client.listener();
        client.sender();
    }
}