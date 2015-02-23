/* Luke Zolyak
 * CS-1501
 * Assignment 2
 * 
 * Compresses and expands binary input using LZW compression with variable length codewords.
 * "n" for Do Nothing mode
 * "r" for Reset mode
 * "m" for Monitor mode
 */

public class MyLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
    private static double oldRatio = 0; //old ratio of output size to read bytes
    private static double newRatio = 0; //new ratio of output size to read bytes
    private static boolean hasRatio = false; //checks if a ratio has been set before
    private static double outputSize = 0; //size of output
    private static double readBytes = 0; //read bytes

    
   //compression in normal mode
    public static void compressN() {
    	
    	StdOut.print("n"); //signifies the compression type
        
    	String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        
        int code = R+1;  // R is codeword for EOF

        while (input.length() > 0) {
        	
        	L = (int) Math.pow(2,W);
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            
            if( (W < 16) && ((int)Math.pow(2, W) == code)){ //check if codebook needs expanded
              W++;
              L = (int)Math.pow(2, W);
              st.put(input.substring(0, t + 1), code++);}
            
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 
    
    
    //compression in  reset mode
    public static void compressR() { 
    	
    	StdOut.print("r");
    	
    	String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        
        int code = R+1;  // R is codeword for EOF

        while (input.length() > 0) {
        	
        	L = (int) Math.pow(2,W);
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            
            if( (W < 16) && ((int)Math.pow(2, W) == code)){ //check if codebook needs expanded
              W++;
              L = (int)Math.pow(2, W);
              st.put(input.substring(0, t + 1), code++);}
            
            if (code == 65536){ //check if codebook is full
                st = new TST<Integer>();
                for (int i = 0; i < R; i++)
                    st.put("" + (char) i, i);
                code = R+1;  // R is codeword for EOF
                W = 9;
                L = 512;
            }
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 
    
    
    //compression in monitor mode
    public static void compressM() { 
    	
    	StdOut.print("m");
     	
    	String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++)
            st.put("" + (char) i, i);
        
        int code = R+1;  // R is codeword for EOF
        
  

        while (input.length() > 0) {
        	
        	L = (int) Math.pow(2,W);
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            readBytes += t; //increment readbytes
            outputSize += W; //increment output size
            newRatio = readBytes/outputSize; //find ratio
            
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            
            if( (W < 16) && ((int)Math.pow(2, W) == code)){ //check codebook has room to grow
              W++;
              L = (int)Math.pow(2, W);
              st.put(input.substring(0, t + 1), code++);}
            
            if (code == 65536) //check if codebook is completely full
            {
                if(!hasRatio)
                {
                    oldRatio = newRatio;
                    hasRatio= true;;
                }

                if(oldRatio/newRatio > 1.1) //if old ratio : new ratio exceeds 1.1 reset
                {
                    st = new TST<Integer>();
                    for (int i = 0; i < R; i++)
                    st.put("" + (char) i, i);
                        code = R+1;  // R is codeword for EOF
                    W = 9;
                    L = 512;
                    oldRatio = 0;
                    newRatio = 0;
                    hasRatio = false;
                }
            }
            
            input = input.substring(t);            // Scan past s in input.
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 



    public static void expand() {
        
    	char type = BinaryStdIn.readChar(8); //find which compression mode was used
 
    
        String[] st = new String[(int)Math.pow(2, 16)];
        int i; // next available codeword value
        
        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char) i;
        st[i++] = "";                        // (unused) lookahead for EOF

        int codeword = BinaryStdIn.readInt(W);
        BinaryStdOut.write(codeword);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
       
        while (true) {
            
            BinaryStdOut.write(val);
            codeword = BinaryStdIn.readInt(W);
            
            readBytes += val.length() * 8; //increment the read byte length
            outputSize += W; //increment the outputsize
            newRatio = readBytes/outputSize; // find the ratio
            
            if (codeword == R) break; //break at EOF
            String s = st[codeword];
            
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            
            if (i < L-1) st[i++] = val + s.charAt(0);

            if(i == L-1 && W < 16)
            {
                st[i++] = val + s.charAt(0);
                W++;
                L = (int)Math.pow(2, W);
            }
            val = s;

            if(i == 65535 && type == 'r') //check if reset mode was used
            {
                W=9;
                L=512 ;
                st = new String[(int)Math.pow(2, 16)];
                for (i = 0; i < R; i++)
                    st[i] = "" + (char) i;
                st[i++] = "";   
                
                codeword = BinaryStdIn.readInt(W);
                if (codeword == R) return;           // expanded message is empty string
                val = st[codeword];
            }
            
            
            if (i == 65535 && type=='m') //check if monitor mode was used;
            {
                if(!hasRatio)
                {
                    oldRatio = newRatio;
                    hasRatio = true;
                }
                
                if(oldRatio/newRatio > 1.1)// check if old : new is greater than 1.1
                {
                    W=9;
                    L=512 ;
                    st = new String[(int)Math.pow(2, 16)];
                    for (i = 0; i < R; i++)
                        st[i] = "" + (char) i;
                    st[i++] = "";   
                

                    codeword = BinaryStdIn.readInt(W);
                    if (codeword == R) return;           // expanded message is empty string
                    val = st[codeword];
                    oldRatio = 0;
                    newRatio = 0;
                    hasRatio = false;
                }
            }
            

        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
       
    	if (args[0].equals("-")){
        	
        	if(args[1].equals("n")) compressN();
        	if(args[1].equals("r")) compressR();
        	if(args[1].equals("m")) compressM();}
        
        else if (args[0].equals("+")) expand();
        
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
