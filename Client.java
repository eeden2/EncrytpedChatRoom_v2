import javax.crypto.Cipher;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.io.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Client
{
    // initialize socket and input output
    public Socket               socket  = null;
    public BufferedReader       input   = null;
    public BufferedWriter       out     = null;
    public String user;
    private PublicKey publicKey;

    public String currentMessage = "No Messages Yet";
    private PrivateKey privateKey;
    private PublicKey serverPubKey;
    public Client(Socket socket, String username) throws Exception
    {
       // establish a connection
        try
        {
            this.socket = socket;
            this.input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.user = username;


            // Initialise RSA
            try{
                KeyPairGenerator RSAKeyGen = KeyPairGenerator.getInstance("RSA");
                RSAKeyGen.initialize(2048);
                KeyPair pair = RSAKeyGen.generateKeyPair();
                this.publicKey = pair.getPublic();
                this.privateKey = pair.getPrivate();
            } catch (GeneralSecurityException e) {
                System.out.println(e.getLocalizedMessage() + "\n");
                System.out.println("Error initialising encryption. Exiting.\n");
                System.exit(0);
            }
            //Sending Username to Server
            out.write(user);
            out.newLine();
            out.flush();
            //SEnding PublicKey to Server
            try{
                ByteBuffer bb = ByteBuffer.allocate(4);
                bb.putInt(publicKey.getEncoded().length);
                socket.getOutputStream().write(bb.array());
                socket.getOutputStream().write(publicKey.getEncoded());
                socket.getOutputStream().flush();
            }catch(IOException e){
                System.out.println("IO Problem");
            }


            //Read-In the Server Public Key
            try{
                byte[] l = new byte[4];
                socket.getInputStream().read(l,0,4);
                ByteBuffer bb = ByteBuffer.wrap(l);
                int len = bb.getInt();
                byte[] pubKeyByte = new byte[len];
                socket.getInputStream().read(pubKeyByte);


                X509EncodedKeySpec kys = new X509EncodedKeySpec(pubKeyByte);
                KeyFactory kfs = KeyFactory.getInstance("RSA");
                serverPubKey = kfs.generatePublic(kys);

            }catch(Exception e){
                e.printStackTrace();
            }



        }
        catch(IOException i)
        {
            closer(socket,input,out);
        }
        
    }

    public void setCurrentMessage(String s) {currentMessage = s;}
    public String getcurrentMessage(){return currentMessage;}

    public String decrypter(String message) throws Exception
    {
        Base64.Decoder decoder = Base64.getDecoder();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);
        return new String(cipher.doFinal(decoder.decode(message)));
    }

    public byte[] encrypter(String message) throws Exception
    {
        Cipher encrypter = Cipher.getInstance("RSA");
        encrypter.init(Cipher.ENCRYPT_MODE,serverPubKey);
        byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encrypter.doFinal(secretMessageBytes);
        return encryptedMessageBytes;
        //Need to figure out how to send byte array over a network.
    }

    public void sender() throws Exception
    {
        try
        {

            Scanner s = new Scanner(System.in);
            Base64.Encoder base64 = Base64.getEncoder();
            while(socket.isConnected())
            {
                if(socket.isClosed()) break;
                //Encrypting the String to send to the server with the ServerPublicKey.
                String line = s.nextLine();
                String encryptedString = base64.encodeToString(encrypter(user + ": "+line));
                while(line.length()>0)
                {
                        out.write(encryptedString);
                        out.newLine();
                        out.flush();
                        line = "";
                       //Sends Base64 Encrypted String over the Socket.
                }

            }
            System.out.println("Server was Closed.");
            s.close();
            System.exit(1);
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
                    setCurrentMessage(decrypter(msgFromChat));
                   if(msgFromChat == null){
                       break;
                   }
                    System.out.println(decrypter(msgFromChat));
                } catch(Exception e)
                {
                    e.printStackTrace();
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
        System.out.print("Enter an IP: ");
        String ip = s.nextLine();
        System.out.print("Enter a Port: ");
        int port = s.nextInt();

        try{
            Socket socket = new Socket(ip, port);
            Client client = new Client(socket,u);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        client.sender();
                    }catch(Exception e){}
                }
            }).start();
            client.listener();
            while(true){
                if(socket.isClosed()){
                    System.out.println("The Server Connection has been Disconnected");
                    System.exit(1);
                }
            }
        }catch(Exception e){
            System.out.println("Connection Refused. Try again");
            System.out.print("Enter a Username: ");
            u = s.nextLine();
            System.out.print("Enter an IP: ");
            ip = s.nextLine();
            System.out.print("Enter a Port: ");
            port = s.nextInt();
            Socket socket = new Socket(ip, port);
            Client client = new Client(socket,u);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try{
                        client.sender();
                    }catch(Exception e){}
                }
            }).start();
            client.listener();
            while(true){
                if(socket.isClosed()){
                    System.out.println("The Server Connection has been Disconnected");
                    System.exit(1);
                    s.close();
                }
            }
        }

    }
}