
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
public class Encryption 
{
    private PublicKey publicKey;
    
    public Encryption() {}   

    public String EncryptMessage(byte[] m, byte[] kb) throws Exception
    {
        Cipher c = Cipher.getInstance("RSA");
        SecretKey sk =  new SecretKeySpec(kb, "RSA");
        c.init(Cipher.ENCRYPT_MODE, sk);
        byte[] encryptMessage = c.doFinal(m);
        return new String(encryptMessage);
    }

    public String decryptMessage(byte[] em, byte[] kb) throws Exception
    {
        Cipher c = Cipher.getInstance("RSA");
        SecretKey sk =  new SecretKeySpec(kb, "RSA");
        c.init(Cipher.DECRYPT_MODE, sk);
        byte[] decryptMessage = c.doFinal(em);
        return new String(decryptMessage);
    }

    public PublicKey returnPublic()
    {
        return publicKey;
    }
}
