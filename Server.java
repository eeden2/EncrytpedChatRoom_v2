import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.*;

public class Server
{
    private PublicKey servPublicKey;
    private PrivateKey servPrivateKey;
    public ServerSocket server;
    public HashMap<Socket, String> userRemover = new HashMap<>();
    public HashMap<ClientHandler, PublicKey> linker = new HashMap<>();
    public String[] userList;
    public String tempMessage = "No Messages Yet";

    public Server(ServerSocket ss) {
        this.server = ss;
        // Initialise RSA
        try{
            KeyPairGenerator RSAKeyGen = KeyPairGenerator.getInstance("RSA");
            RSAKeyGen.initialize(2048);
            KeyPair pair = RSAKeyGen.generateKeyPair();
            this.servPublicKey = pair.getPublic();
            this.servPrivateKey = pair.getPrivate();
        } catch (GeneralSecurityException e) {
            System.out.println(e.getLocalizedMessage() + "\n");
            System.out.println("Error initialising encryption. Exiting.\n");
            System.exit(0);
        }
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
                ClientHandler cH = new ClientHandler(socket);

                //Send the server Public Key to Client
                try{
                    ByteBuffer bb = ByteBuffer.allocate(4);
                    bb.putInt(servPublicKey.getEncoded().length);
                    socket.getOutputStream().write(bb.array());
                    socket.getOutputStream().write(servPublicKey.getEncoded());
                    socket.getOutputStream().flush();
                }catch(IOException e){
                    System.out.println("IO Problem");
                }

                System.out.println(cH.user+" has joined.\nIP: " + ((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress().getHostAddress() +" | Port: " + socket.getLocalPort());
                //Setup User removal for Server GUI
                userRemover.put(socket,cH.user);
                linker.put(cH,cH.publicKey);
                
                if(userRemover.size()==1){
                                ArrayList<String> users = new ArrayList<>();
                                for(ClientHandler ch : linker.keySet())
                                {
                                    users.add(ch.user +" "+((InetSocketAddress) ch.s.getRemoteSocketAddress()).getAddress().getHostAddress());
                                }
                                String[] user = new String[users.size()];
                                for(int i = 0;i<users.size();i++)
                                {
                                    user[i] = users.get(i);
                                }
                                userList = user;
                      
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String recieveMessage;
                        while(cH.s.isConnected())
                        {
                            try
                            {
                                recieveMessage = cH.br.readLine();
                                setCurrentMessage(serverDecryptor(recieveMessage));
                                announcement(serverDecryptor(recieveMessage));
                            }catch(Exception e)
                            {

                                cH.closer(cH.s,cH.br,cH.bw);
                                break;
                            }
                        }
                        announcement("SERVER: "+cH.user+" has left.");
                        linker.remove(cH);
                        userRemover.remove(cH.s);
                    }
                }).start();

            }
            
        }
        catch(IOException i) {}
    }

    public String serverDecryptor(String s) throws Exception
    {
        Base64.Decoder decoder = Base64.getDecoder();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,servPrivateKey);
        return new String(cipher.doFinal(decoder.decode(s)));
    }

    public void setCurrentMessage(String s) {tempMessage = s;}
    public String getcurrentMessage(){return tempMessage;}

    public void announcement(String m)
    {
        Base64.Encoder base64 = Base64.getEncoder();
        for(ClientHandler c : linker.keySet())
        {
            if(c.s.isConnected()) {
                try
                {
                    String encryptedString = base64.encodeToString(c.encrypter(m));
                    c.bw.write(encryptedString);
                    c.bw.newLine();
                    c.bw.flush();

                } catch(Exception e)
                {
                    System.out.println(c.user + " has left abruptly.");
                    c.closer(c.s,c.br,c.bw);
                }
            }

        }
    }
    public void closeSocket()
    {
        try
        {   
            server.close();
        }catch(IOException e) {}
    }

    public boolean serverStatus()
    {
        return server.isBound();
    }

    public String[] getUsers()
    {
        try{
            ArrayList<String> users = new ArrayList<>();
            for(ClientHandler ch : linker.keySet())
            {
                users.add(ch.user +" "+((InetSocketAddress) ch.s.getRemoteSocketAddress()).getAddress().getHostAddress());
            }
            String[] user = new String[users.size()];
            for(int i = 0;i<users.size();i++)
            {
                user[i] = users.get(i);
            }
            userList = user;
            return user;
    }catch(ConcurrentModificationException cme){}
        return new String[]{"No Users Connected"};
    }

 
    public static void main(String args[]) throws IOException
    {
        //Using port 5000 
        ServerSocket ss = new ServerSocket(5000);
        Server s = new Server(ss);
        s.startServer();
    }
}