import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Encoder;
import sun.misc.BASE64Decoder;

public class Encryptor {
	
	private static final String KEYGENSOURCE = "HignDlPs";
    private String algo;
	
    public Encryptor(String algo) {
        this.algo = algo; 
    }
    
	public String encrypt(String str) throws Exception {
	         Cipher ecipher =  Cipher.getInstance(algo);  
	         byte k[] = KEYGENSOURCE .getBytes();   
	         SecretKeySpec key = new SecretKeySpec(k,algo.split("/")[0]);  
	         ecipher.init(Cipher.ENCRYPT_MODE, key);  
			// Encode the string into bytes using utf-8
			byte[] utf8 = str.getBytes("UTF8");

			// Encrypt
			byte[] enc = ecipher.doFinal(utf8);

			// Encode bytes to base64 to get a string
			return new sun.misc.BASE64Encoder().encode(enc);
	}

	public String decrypt(String str) throws Exception{
			// Decode base64 to get bytes
			byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
			Cipher dcipher =  Cipher.getInstance(algo);
	         byte k[] = KEYGENSOURCE .getBytes();   
	         SecretKeySpec key = new SecretKeySpec(k,algo.split("/")[0]);  
			dcipher.init(Cipher.DECRYPT_MODE, key);  
			// Decrypt
			byte[] utf8 = dcipher.doFinal(dec);

			// Decode using utf-8
			return new String(utf8, "UTF8");
	}
}