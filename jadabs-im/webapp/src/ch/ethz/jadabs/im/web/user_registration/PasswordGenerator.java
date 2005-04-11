/*
 * Created on Dec 5, 2004
 */
package ch.ethz.jadabs.im.web.user_registration;

import java.util.*;

/**
 * @author Jean-Luc Geering
 */
public final class PasswordGenerator {
	
	private static final Random rand = new Random();
	private static final String [] possible = new String []{
			"a","b","c","e","d","f","g","h","i","j","k","l","m",
			"n","o","p","q","r","s","t","u","v","w","x","y","z",
			"a","b","c","e","d","f","g","h","i","j","k","l","m",
			"n","o","p","q","r","s","t","u","v","w","x","y","z",
			"A","B","C","D","E","F","G","H","I","J","K","L","M",
			"N","O","P","Q","R","S","T","U","V","W","X","Y","Z",
			"1","2","3","4","5","6","7","8","9","0",
			"1","2","3","4","5","6","7","8","9","0",
			".",",",";","!","*","^","-","_","@","~"};
	
	public static final String newPassword(int length) {
		String password = "";
		for (int i=0; i<length; i++){
			password += possible[rand.nextInt(possible.length)];
		}
		return password;
	}
}
