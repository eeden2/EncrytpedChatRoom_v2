import javax.crypto.Cipher;
import java.net.*;
import java.io.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Server
{
    private PublicKey servPublicKey;
    private PrivateKey servPrivateKey;
    private ServerSocket server;
    public HashMap<ClientHandler, PublicKey> linker = new HashMap<>();

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

                linker.put(cH,cH.publicKey);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String recieveMessage;
                        while(!cH.s.isClosed())
                        {
                            try
                            {

                                //Here is where the DataStream will read the byte[] in order to maintain proper encryption format
                                recieveMessage = cH.br.readLine();
                                announcement(serverDecryptor(recieveMessage));
                            }catch(Exception e)
                            {

                                cH.closer(cH.s,cH.br,cH.bw);
                                break;
                            }
                        }
                        System.out.println(cH.user+" has left.");
                    }
                }).start();

            }
            
        }
        catch(IOException i)
        {
            System.out.println("Houston, we have a problem\n"+i);
        }
    }

    public String serverDecryptor(String s) throws Exception
    {
        Base64.Decoder decoder = Base64.getDecoder();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,servPrivateKey);
        return new String(cipher.doFinal(decoder.decode(s)));
    }

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
            if(server != null) server.close();
        }catch(IOException e) {}
    }
 
    public static void main(String args[]) throws IOException
    {
        //Using port 5000 
        ServerSocket ss = new ServerSocket(5000);
        Server s = new Server(ss);
        s.startServer();
    }
}