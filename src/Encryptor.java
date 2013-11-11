import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

//@A0105667B
public class Encryptor {
	
	//Key generator source
	private static final String KEYGENSOURCE = "HignDlPs";
	//The algorithm for the encryption, eg: DES/ECB/PKCS5Padding
    private String algo;
	
    public Encryptor(String algo) {
        this.algo = algo; 
    }
    
    /**
     * encrypt a information string
     * @param str  a string to be encrypted
     * @return a encrypted string
     * @throws Exception
     */
	@SuppressWarnings("restriction")
	public String encrypt(String str) throws Exception {
        Cipher ecipher =  Cipher.getInstance(algo);  
        byte k[] = KEYGENSOURCE .getBytes();   
        SecretKeySpec key = new SecretKeySpec(k,algo.split("/")[0]);  
        ecipher.init(Cipher.ENCRYPT_MODE, key);  
		// Encode the string into bytes using utf-8
		byte[] utf8 = str.getBytes("UTF8");
		byte[] enc = ecipher.doFinal(utf8);
		// Encode bytes to base64 to get a string
		return new sun.misc.BASE64Encoder().encode(enc);
	}
	
	/**
	 * decrypt a encrypted string
	 * @param str   encrypted string
	 * @return  a decrupted string
	 * @throws Exception
	 */
	@SuppressWarnings("restriction")
	public String decrypt(String encryptedStr) throws Exception{
		// Decode base64 to get bytes
		byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(encryptedStr);
		Cipher dcipher =  Cipher.getInstance(algo);
         byte k[] = KEYGENSOURCE .getBytes();   
         SecretKeySpec key = new SecretKeySpec(k,algo.split("/")[0]);  
		dcipher.init(Cipher.DECRYPT_MODE, key);  
		byte[] utf8 = dcipher.doFinal(dec);
		// Decode using utf-8
		return new String(utf8, "UTF8");
	}
}