import javax.crypto.*;
import java.security.*;
import javax.crypto.spec.*;

import java.util.Random;

import java.io.PrintStream;

public class ParallelDES implements Runnable{
	// Cipher for the class
	Cipher des_cipher;
	
	// Key for the class
	SecretKeySpec the_key = null;

	// max key
	long maxKey = 0L;

	// number of threads
	int nThreads = 1;

	// self-defined thread id from 1 to nTheads
	int threadId = 1;

	// the sample String
	String plainString = null;

	// Byte arrays that hold key block
	byte[] deskeyIN = new byte[8];
	byte[] deskeyOUT = new byte[8];

	// start time of program
	long runStart = 0L;
		
	// Constructor: initialize the cipher
	public ParallelDES(long maxkey, int nthreads, String plainstring, int threadid, long thekey, long runstart) 
	{
		try 
		{
			des_cipher = Cipher.getInstance("DES");
			this.maxKey = maxkey;
			this.nThreads = nthreads;
			this.plainString = plainstring;
			this.threadId = threadid;
			this.setKey(thekey);
			this.runStart = runstart;
		} 
		catch ( Exception e )
		{
			System.out.println("Failed to create cipher.  Exception: " + e.toString() +
							   " Message: " + e.getMessage()) ; 
		}
	}
	
	// Decrypt the SealedObject
	//
	//   arguments: SealedObject that holds on encrypted String
	//   returns: plaintext String or null if a decryption error
	//     This function will often return null when using an incorrect key.
	//
	public String decrypt ( SealedObject cipherObj )
	{
		try 
		{
			return (String)cipherObj.getObject(the_key);
		}
		catch ( Exception e )
		{
			//      System.out.println("Failed to decrypt message. " + ". Exception: " + e.toString()  + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}
	
	// Encrypt the message
	//
	//  arguments: a String to be encrypted
	//  returns: a SealedObject containing the encrypted string
	//
	public SealedObject encrypt ( String plainstr )
	{
		try 
		{
			des_cipher.init ( Cipher.ENCRYPT_MODE, the_key );
			return new SealedObject( plainstr, des_cipher );
		}
		catch ( Exception e )
		{
			System.out.println("Failed to encrypt message. " + plainstr +
							   ". Exception: " + e.toString() + ". Message: " + e.getMessage()) ; 
		}
		return null;
	}
	
	//  Build a DES formatted key
	//
	//  Convert an array of 7 bytes into an array of 8 bytes.
	//
	private static void makeDESKey(byte[] in, byte[] out)  
	{
		out[0] = (byte) ((in[0] >> 1) & 0xff);
		out[1] = (byte) ((((in[0] & 0x01) << 6) | (((in[1] & 0xff)>>2) & 0xff)) & 0xff);
		out[2] = (byte) ((((in[1] & 0x03) << 5) | (((in[2] & 0xff)>>3) & 0xff)) & 0xff);
		out[3] = (byte) ((((in[2] & 0x07) << 4) | (((in[3] & 0xff)>>4) & 0xff)) & 0xff);
		out[4] = (byte) ((((in[3] & 0x0F) << 3) | (((in[4] & 0xff)>>5) & 0xff)) & 0xff);
		out[5] = (byte) ((((in[4] & 0x1F) << 2) | (((in[5] & 0xff)>>6) & 0xff)) & 0xff);
		out[6] = (byte) ((((in[5] & 0x3F) << 1) | (((in[6] & 0xff)>>7) & 0xff)) & 0xff);
		out[7] = (byte) (   in[6] & 0x7F);
	
		for (int i = 0; i < 8; i++) {
			out[i] = (byte) (out[i] << 1);
		}
	}

	// Set the key (convert from a long integer)
	public void setKey ( long theKey )
	{
		try 
		{
			// convert the integer to the 8 bytes required of keys
			deskeyIN[0] = (byte) (theKey        & 0xFF );
			deskeyIN[1] = (byte)((theKey >>  8) & 0xFF );
			deskeyIN[2] = (byte)((theKey >> 16) & 0xFF );
			deskeyIN[3] = (byte)((theKey >> 24) & 0xFF );
			deskeyIN[4] = (byte)((theKey >> 32) & 0xFF );
			deskeyIN[5] = (byte)((theKey >> 40) & 0xFF );
			deskeyIN[6] = (byte)((theKey >> 48) & 0xFF );

			// theKey should never be larger than 56-bits, so this should always be 0
			deskeyIN[7] = (byte)((theKey >> 56) & 0xFF );
			
			// turn the 56-bits into a proper 64-bit DES key
			makeDESKey(deskeyIN, deskeyOUT);
			
			// Create the specific key for DES
			the_key = new SecretKeySpec ( deskeyOUT, "DES" );
		}
		catch ( Exception e )
		{
			System.out.println("Failed to assign key" +  theKey +
							   ". Exception: " + e.toString() + ". Message: " + e.getMessage()) ;
		}
	}

	// Implement the run() method
	public void run(){
		SealedObject sldObj = encrypt ( plainString );
		int res = (int) (maxKey % nThreads);
		long len = (maxKey-res)/nThreads;
		long startIdx = (threadId - 1) * len;
		long endIdx = (threadId==nThreads)?(maxKey):(threadId * len);

		// Search for the right key
		for ( long i = startIdx; i < endIdx; i++ )
		{
			// Set the key and decipher the object
			setKey ( i );
			String decryptstr = decrypt ( sldObj );
			
			// Does the object contain the known plaintext
			if (( decryptstr != null ) && ( decryptstr.indexOf ( "Hopkins" ) != -1 ))
			{
				//  Remote printlns if running for time.
				System.out.println("Thread "+ threadId + " Found decrypt key " + i + " producing message: " + decryptstr);
				long elapsed = System.currentTimeMillis() - runStart;
				System.out.println ( "Thread " + threadId + " Found decrypt key " + i + " at " + elapsed + " milliseconds.");
				//System.out.println (  "Found decrypt key " + i + " producing message: " + decryptstr );
			}
			
			// Update progress every once in awhile.
			//  Remote printlns if running for time.
			if ( i % 100000 == 0 )
			{ 
				long elapsed = System.currentTimeMillis() - runStart;
				System.out.println ( "Thread " + threadId + " Searched key number " + i + " at " + elapsed + " milliseconds.");
			}
			if (i + 1 == maxKey)
			{
				long elapsed = System.currentTimeMillis() - runStart;
				long keys = maxKey + 1;
				System.out.println ( "Completed search of " + keys + " keys at " + elapsed + " milliseconds.");
			}
		}

	}
	
	public static void main(String[] args){

		if ( 2 != args.length )
		{
			System.out.println ("Usage: java SealedDES #threads key_size_in_bits");
			return;
		}
		
		// create object to printf to the console
		PrintStream p = new PrintStream(System.out);

		// Get the arguments
		int nthreads = Integer.parseInt( args[0] );
		long keybits = Long.parseLong ( args[1] );

    		long maxkey = ~(0L);
    		maxkey = maxkey >>> (64 - keybits);
		
		
		// Get a number between 0 and 2^64 - 1
		Random generator = new Random ();
		long key =  generator.nextLong();
		
		// Mask off the high bits so we get a short key
		key = key & maxkey;
		
	
		// Generate a sample string
		String plainstr = "Johns Hopkins afraid of the big bad wolf?";
		
		
		// Get and store the current time -- for timing
		long runstart;
		runstart = System.currentTimeMillis();

		// threads pool
		Thread[] threadPool = new Thread[nthreads];
		
		for(int i = 1; i <= nthreads; i++){
		
			threadPool[i-1]=new Thread(new ParallelDES(maxkey,nthreads,plainstr,i,key,runstart));

		}

		long serialtime;
		serialtime = System.currentTimeMillis() - runstart;
		System.out.println ( "Serial Code Used " + serialtime + " milliseconds.");

		for(int i = 0; i < nthreads; i++){
			threadPool[i].start();
		}

		// Output search time
	}
	
}
