import javax.crypto.Cipher;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.HashSet;
import java.net.Socket;
import java.util.HashSet;
import java.util.HashMap;

public class ClientHandler
{
    public Socket s;
    public BufferedReader br;
    public BufferedWriter bw;
    public String user;
    public PublicKey publicKey;
    
    public ClientHandler(Socket ss)
    {
        try
        {
            this.s = ss;
            if(bw == null) this.bw =  new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
            if(br == null) this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
            this.user = br.readLine();


            //Read-In the public Key to hashmap
            //When the Client connects, they automatically send their public key to the server
            try{
                byte[] l = new byte[4];
                s.getInputStream().read(l,0,4);
                ByteBuffer bb = ByteBuffer.wrap(l);
                int len = bb.getInt();
                byte[] pubKeyByte = new byte[len];
                s.getInputStream().read(pubKeyByte);


                X509EncodedKeySpec kys = new X509EncodedKeySpec(pubKeyByte);
                KeyFactory kfs = KeyFactory.getInstance("RSA");
                publicKey = kfs.generatePublic(kys);

            }catch(Exception e){
                e.printStackTrace();
            }

        } catch(IOException i)
        {
                closer(s,br,bw);
        }
    }


    public byte[] encrypter(String message) throws Exception
    {
        //Using the Base64 Encoder allows for the String to be sent over a network
        Cipher encrypter = Cipher.getInstance("RSA");
        encrypter.init(Cipher.ENCRYPT_MODE,publicKey);
        byte[] secretMessageBytes = message.getBytes(StandardCharsets.UTF_8);
        byte[] encryptedMessageBytes = encrypter.doFinal(secretMessageBytes);
        return encryptedMessageBytes;
    }

    public void closer(Socket s, BufferedReader br, BufferedWriter bw)
    {
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
