package eu.ludiq.sgn.rooster.exception;

/**
 * An exception for situations where the user has given a wrong password, or the
 * username doesn't exist
 * 
 * @author Ludo Pulles
 * 
 */
public class WrongPasswordException extends Exception {

	public WrongPasswordException() {
		super("Incorrect wachtwoord");
	}
}
