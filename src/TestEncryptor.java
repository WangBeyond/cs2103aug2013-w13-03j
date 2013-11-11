import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Test;

//@author A0105667B
public class TestEncryptor {
	private static String encryptAlgo = "DES/ECB/PKCS5Padding";
	private static Encryptor encryptor;
	
	@BeforeClass
	public static void testSetup() {
		encryptor = new Encryptor(encryptAlgo);
	}
	
	@Test
	public void test() {
		testCase1();
		testCase2();
		testCase3();
		testCase4();
		testCase5();
	}
	
	public void testCase1() {
		String plainText = "HelloWorld";
		try {
			String encryptedString = encryptor.encrypt(plainText);
			assertTrue("Encryption fail",!encryptedString.equals(plainText));
			assertEquals("Decrypted text is different from original plain text",encryptor.decrypt(encryptedString), plainText);
		} catch (Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testCase2() {
		String plainText = "Ping#Pong";
		try {
			String encryptedString = encryptor.encrypt(plainText);
			assertTrue("Encryption fail",!encryptedString.equals(plainText));
			assertEquals("Decrypted text is different from original plain text",encryptor.decrypt(encryptedString), plainText);
		} catch (Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testCase3() {
		String plainText = "BattleField2";
		try {
			String encryptedString = encryptor.encrypt(plainText);
			assertTrue("Encryption fail",!encryptedString.equals(plainText));
			assertEquals("Decrypted text is different from original plain text",encryptor.decrypt(encryptedString), plainText);
		} catch (Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testCase4() {
		String plainText = "?%werwe#oiweru";
		try {
			String encryptedString = encryptor.encrypt(plainText);
			assertTrue("Encryption fail",!encryptedString.equals(plainText));
			assertEquals("Decrypted text is different from original plain text",encryptor.decrypt(encryptedString), plainText);
		} catch (Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
	}
	
	public void testCase5() {
		String plainText = "!@#$%^&wertyuioiopip[]jkljl()\';.,/]:|?>";
		try {
			String encryptedString = encryptor.encrypt(plainText);
			assertTrue("Encryption fail",!encryptedString.equals(plainText));
			assertEquals("Decrypted text is different from original plain text",encryptor.decrypt(encryptedString), plainText);
		} catch (Exception e) {
			fail("Some exception thrown "+e.getMessage());
		}
	}
}
