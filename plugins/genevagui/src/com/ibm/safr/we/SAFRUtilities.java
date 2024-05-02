package com.ibm.safr.we;

/*
 * Copyright Contributors to the GenevaERS Project. SPDX-License-Identifier: Apache-2.0 (c) Copyright IBM Corporation 2023
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.text.NumberFormat;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.ibm.safr.we.constants.ClientConstants;
import com.ibm.safr.we.exceptions.SAFRFatalException;

/**
 * This utility class is for the functions which can be used by all the classes
 * irrespective of the layers(UI, model, data layer).
 * 
 * 
 */
public class SAFRUtilities {

	private static String WEVersion = "";

	private static final String algorithm = "AES/CBC/PKCS5Padding";;

	private static final byte[] HEXCHAR = new byte[] { '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * Line break string specific to the run time platform. To be used instead
	 * of platform-dependent escaped characters like '\r' and '\n'.
	 */
	public static final String LINEBREAK = System.getProperty("line.separator");

	/**
	 * Decrypts a given string
	 * 
	 * @param str
	 * @return String decrypted password.
	 * @throws RuntimeException
	 */
	public static String decrypt(String str) {
		String decrypted = "";

		if (str.length() > 0) {
			byte[] bytes = new byte[str.length() / 2];
			for (int i = 0; i < bytes.length; i++) {
				bytes[i] = (byte) Integer.parseInt(
						str.substring(i * 2, (i * 2) + 2), 16);
			}
			String encrypted = new String(bytes);
			try {
				SecretKeySpec key = createSecretKey();
				decrypted = decrypt(encrypted, key);
			} catch (IllegalBlockSizeException e) {
				decrypted = "";
			} catch (Exception e) {
				throw new SAFRFatalException(e.getMessage());
			}
		}
		return decrypted;
	}

	/**
	 * Encrypts a given string
	 * 
	 * @param str
	 * @return String encrypted password.
	 * @throws RuntimeException
	 */
	public static String encrypt(String str) {

		String encrypted;
		try {
			SecretKeySpec key = createSecretKey();
			encrypted = encrypt(str, key);
		} catch (Exception e) {
			throw new SAFRFatalException(e.getMessage());
		}
		String retstr = "";
		for (byte b : encrypted.getBytes()) {
			retstr += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
		}
		return retstr;
	}

	/**
	 * Get our DES key
	 * 
	 * @return the DES key
	 */

	private static byte[] getKey() {
		String encKey = "220146077248131221089016";
		return decryptBase64(encKey).getBytes();
	}

	/**
	 * Decrypts a given string. All the user passwords loaded from SAFR Database
	 * should be decrypted using this function.
	 * 
	 * @param key
	 * @return String decrypted string.
	 */
	public static String decryptBase64(String key) {
		if (key == null) {
			return null;
		}
		String pwdChar;
		String result = "";
		int midInt;
		while (key.length() > 0) {
			pwdChar = key.substring(0, 3);
			key = key.substring(3);
			midInt = Integer.parseInt(pwdChar);
			midInt = midInt ^ 255;
			result = result + (char) midInt;
		}
		return result;
	}

	/**
	 * Encrypts a given string. All the user passwords saved in SAFR Database
	 * should be encrypted using this function.
	 * 
	 * @param key
	 * @return String encrypted string.
	 */
	public static String encryptBase64(String key) {
		if (key == null) {
			return null;
		}
		char c;
		String result = "";
		int midInt;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumIntegerDigits(3);
		nf.setMinimumIntegerDigits(3);
		while (key.length() > 0) {
			c = key.charAt(0);
			key = key.substring(1);
			midInt = (int) c;
			midInt = midInt ^ 255;
			result = result + nf.format((long) midInt);
		}
		return result;
	}

	public static final String dumpBytes(byte[] buffer) {
		if (buffer == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < buffer.length; i++) {
			sb.append("0x").append((char) (HEXCHAR[(buffer[i] & 0x00F0) >> 4]))
					.append((char) (HEXCHAR[buffer[i] & 0x000F])).append(" ");
		}
		return sb.toString();
	}

	public static String getWEVersion() {
		return WEVersion;
	}

	public static void setWEVersion(String version) {
		SAFRUtilities.WEVersion = version;
	}

	public static IvParameterSpec generateIv() {
		byte[] iv = new byte[16];
		new SecureRandom().nextBytes(iv);
		return new IvParameterSpec(iv);
	}

	public static SecretKeySpec createSecretKey()
			throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte[] salt = ClientConstants.getSaltvalue().getBytes(); 
		int iterationCount = 400;
		int keyLength = 128;
		String mkey = ClientConstants.getMkeyvalue();
		SecretKeyFactory keyFactory = SecretKeyFactory
				.getInstance("PBKDF2WithHmacSHA512");
		PBEKeySpec keySpec = new PBEKeySpec(mkey.toCharArray(), salt,
				iterationCount, keyLength);
		SecretKey keyTmp = keyFactory.generateSecret(keySpec);
		return new SecretKeySpec(keyTmp.getEncoded(), "AES");
	}

	public static String encrypt(String dataToEncrypt, SecretKeySpec key)
			throws GeneralSecurityException, UnsupportedEncodingException {
		Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		pbeCipher.init(Cipher.ENCRYPT_MODE, key);
		AlgorithmParameters parameters = pbeCipher.getParameters();
		IvParameterSpec ivParameterSpec = parameters
				.getParameterSpec(IvParameterSpec.class);
		byte[] cryptoText = pbeCipher.doFinal(dataToEncrypt.getBytes("UTF-8"));
		byte[] iv = ivParameterSpec.getIV();
		return base64Encode(iv) + ":" + base64Encode(cryptoText);
	}

	private static String base64Encode(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static String decrypt(String inStr, SecretKeySpec key)
			throws GeneralSecurityException, IOException {
		String[] splits = inStr.split(":");
		if(splits.length == 2) {
			String iv =splits[0];
			String property = inStr.split(":")[1];
			Cipher pbeCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			pbeCipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(
					base64Decode(iv)));
			return new String(pbeCipher.doFinal(base64Decode(property)), "UTF-8");
		} else {
			return "";
		}
	}

	private static byte[] base64Decode(String property) throws IOException {
		return Base64.getDecoder().decode(property);
	}
}
