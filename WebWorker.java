/**
* Web worker: an object of this class executes in its own new thread
* to receive and respond to a single HTTP request. After the constructor
* the object executes on its "run" method, and leaves when it is done.
*
* One WebWorker object is only responsible for one client connection. 
* This code uses Java threads to parallelize the handling of clients:
* each WebWorker runs in its own thread. This means that you can essentially
* just think about what is happening on one client at a time, ignoring 
* the fact that the entirety of the webserver execution might be handling
* other clients, too. 
*
* This WebWorker class (i.e., an object of this class) is where all the
* client interaction is done. The "run()" method is the beginning -- think
* of it as the "main()" for a client interaction. It does three things in
* a row, invoking three methods in this class: it reads the incoming HTTP
* request; it writes out an HTTP header to begin its response, and then it
* writes out some HTML content for the response content. HTTP requests and
* responses are just lines of text (in a very particular format). 
*
**/

import java.net.Socket;
import java.lang.Runnable;
import java.io.*;
import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;

public class WebWorker implements Runnable
{

private Socket socket;

/**
* Constructor: must have a valid open socket
**/
public WebWorker(Socket s)
{
   socket = s;
}

/**
* Worker thread starting point. Each worker handles just one HTTP 
* request and then returns, which destroys the thread. This method
* assumes that whoever created the worker created it with a valid
* open socket object.
**/
public void run()
{
   System.err.println("Handling connection...");
   //path to the file
   String filePath = "";
   //type of content being read
   String contentType = "";
   
   try {
      InputStream  is = socket.getInputStream();
      OutputStream os = socket.getOutputStream();
      
      //read in from GET request
      filePath = readHTTPRequest(is);
      //check for .gif
      if ( filePath.toLowerCase().contains(".gif") ) {
          contentType = "image/gif";
          System.err.println("Type of Content Entered: " + contentType );
       } // end if. 
       //check for .jpeg
       else if ( filePath.toLowerCase().contains(".jpeg") ) {
    	   contentType = "image/jpeg";
          System.err.println("Type of Content Entered: " + contentType );
       } // end else-if
       
       //check for .png
       else if ( filePath.toLowerCase().contains(".png") ) {
    	   contentType = "image/png";
          System.err.println("Type of Content Entered: " + contentType );
       } // end else-if
       
       /*EXTRA CREDIT*/
       //Check for the .ico file.
       else if ( filePath.toLowerCase().contains("ico") ) {
    	   contentType = "image/x-icon";
          System.err.println("Type of Content Entered: " + contentType );
       } // end else if.
       
      //default type is a text/html
       else {
    	   contentType = "text/html";
          System.err.println("Type of Content Entered: " + contentType );
       } // end else.
      
      int fileExists = writeHTTPHeader(os,contentType,filePath);
      writeContent(os, contentType ,filePath, fileExists);
      os.flush();
      socket.close();
   } catch (Exception e) {
      System.err.println("Output error: "+e);
   }
   System.err.println("Done handling connection.");
   return;
}

/**
* Read the HTTP request header.
* Make the output of the function to a string that can be read later
**/
private String readHTTPRequest(InputStream is)
{
   String line;
   String originalPath="";
   
   BufferedReader r = new BufferedReader(new InputStreamReader(is));
   
   //use a counting variable that will say when to stop when a space is reached
   int countToSpace = 0;
   
   while (true) {
      try {
         while (!r.ready()) Thread.sleep(1);
         line = r.readLine();
	 System.err.println("Request line: ("+line+")");
	 	
	 	if(line.contains("GET")) {
	 		originalPath = line.substring(4);
	 		while( !(originalPath.charAt(countToSpace) == ' ' )) {
	 			countToSpace++;
	 		}//end while
	 	}//end if
	 
	 	//update original path up to where there's a space
	 	originalPath = originalPath.substring(0,countToSpace);
	 	
          if (line.length()==0) break;
       } catch (Exception e) {
          System.err.println("Request error: "+e);
          break;
       }
 }
   return originalPath;
}

/**
* Write the HTTP header lines to the client network connection.
* @param os is the OutputStream object to write to
* @param contentType is the string MIME content type (e.g. "text/html")
**/
private int writeHTTPHeader(OutputStream os, String contentType, String filepath) throws Exception
{

   //get current date and time
   Date d = new Date();
   DateFormat df = DateFormat.getDateTimeInstance();
   df.setTimeZone(TimeZone.getTimeZone("GMT"));
   
   
   //check if the file exist using a boolean flag variable
   File tmpDir = new File(filepath);
   boolean exists = tmpDir.exists();

   if(exists == true) {
	   os.write("HTTP/1.1 200 OK\n".getBytes());
   }
   
   else  {
	   os.write( "HTTP/1.1 404 Not Found \n".getBytes() );
	   // Print an error message on the terminal. 
	   System.err.println( "File not found: " + filepath + " Please check path or if file exists." );
   }
	   
   // Write the appropriate time and server on the HTTP header.
   os.write("Date: ".getBytes());
   os.write((df.format(d)).getBytes());
   os.write("\n".getBytes());
   os.write("Server: Anthony's very own server!\n".getBytes());
   
   // os.write("Last-Modified: Wed, 08 Jan 2003 23:11:55 GMT\n".getBytes());
   // os.write("Content-Length: 438\n".getBytes()); 
   
   os.write("Connection: close\n".getBytes());
   os.write("Content-Type: ".getBytes());
   os.write(contentType.getBytes());
   // HTTP header ends with 2 newlines
   os.write("\n\n".getBytes()); 
   
   //flags set in order to control behavior of writeContent method
   //if true flag is 1
   if(exists == true)
	   return 1;
   
   return 0;
}

/**
* Write the data content to the client network connection. This MUST
* be done after the HTTP header has been written out.
* @param os is the OutputStream object to write to
**/
private void writeContent(OutputStream os, String contentType, String pathToFile, int fileExists ) throws Exception {

	   // Declare a variable that will allow us to read in the content from the HTML file.
	   String content = "";
	   
	   // Update the path name to include the current working directory.
	   String pathCopy = System.getProperty("user.dir") + pathToFile.substring( 0, pathToFile.length( ) );
	   
	   // Create and update a new Date object.
	   Date d = new Date();
	   DateFormat df = DateFormat.getDateTimeInstance();
	   df.setTimeZone(TimeZone.getTimeZone("MST7MDT"));
	   
	   //for system checking
	   System.err.println("pathToFile = " + pathToFile);
	   System.err.println("fileExists = " + fileExists);
	   
	   if(contentType.equals("text/html")) {
	   //output working web server when fileExists flag is 1
	   while(fileExists == 1) {
		   os.write("<html><head></head><body>\n".getBytes());
		   os.write("<h3>My web server works!</h3>\n".getBytes());
		   os.write("</body></html>\n".getBytes());
		   break;
	   }
	   while(fileExists == 0) {
	   // Try reading from the file and throw an exception if the file isn't found.
	   try {
	   
	      File inputtedFile = new File ( pathCopy );         
	      FileReader readerFile = new FileReader ( inputtedFile );
	      BufferedReader bufferFile = new BufferedReader ( readerFile );
	      
	    	  
	      // Use a while loop that will read the the file until there is nothing to read. In other 
	      // words, read the next line until we reach null.
	      while ( (content = bufferFile.readLine()) != null) {  

	         // If a date tag is encountered, replace it with the current date. 
	         if ( content.contains( "{{cs371date}}") )
	            content = content.replace( "{{cs371date}}", df.format(d) );
	         
	         // If a server tag is encountered, replace it with the current server.  
	         if( content.contains("{{cs371server}}") )
	            content = content.replace( "{{cs371server}}", "Anthony's very own server!" );
	         
	         // Write the corresponding bytes to the out stream.          
	         os.write( content.getBytes( ) );
	         
	         // End the current content with a newline. NOTE: This is only needed
	         // to make the source code easier to read by users. 
	         os.write( "\n".getBytes( ) );
	                    
	      }// end while
	      
	   } // end try.

	   catch ( FileNotFoundException e) {             
	         
	      // Print an error message if the file did not exists and/or if the path given is incorrect. 
	      System.err.println("File not found: " + pathToFile + " Please check path or if file exists." );
	      
	      // Send the corresponding bytes so that the user knows that the 404 error occured. 
	      // NOTE: Extra messages were not needed for this assignment but adding extra context is 
	      // important in the real world. 
	      os.write ( "<html>\n".getBytes( ) );
	      os.write ( "<head>\n<title>ERROR 404</title></head>\n".getBytes( ) );
	      os.write ( "<body>\n".getBytes( ) );
	      os.write ( "<h1>404 Not Found</h1>\n".getBytes() );
	      os.write ( "</body>\n".getBytes() );  
	      os.write ( "</html>\n".getBytes( ) );               
	                
	   }// end catch. 
	   break;
	   }
	   }//end if
	
	   else if(contentType.contains("image")) {
		// Try reading from the file (byte mode) and throw an exception if the file isn't found.
		      try {
		   
		         File inputtedFile = new File ( pathCopy );  
		         FileInputStream imageInput = new FileInputStream ( inputtedFile );
		         
		         // Read in the bytes from the file and store them in an array. 
		         byte imageBytes [ ] = new byte [ (int) inputtedFile.length() ];
		         imageInput.read( imageBytes );
		         
		         // Output the bytes into the os stream. 
		         DataOutputStream imageOutput = new DataOutputStream( os );
		         imageOutput.write( imageBytes );
		      
		      } // end try.
		   
		      catch ( FileNotFoundException e) {             
		         
		         // Print an error message if the file did not exists and/or if the path given is incorrect. 
		         System.err.println("File not found: " + pathToFile + " Please check path or if file exists." );
		      
		         // Send the corresponding bytes so that the user knows that the 404 error occured. 
		         // NOTE: Extra messages were not needed for this assignment but adding extra context is 
		         // important in the real world. 
		       
		         os.write ( "<html>\n".getBytes( ) );
		         os.write ( "<head>\n<title>ERROR 404: IMAGE</title></head>\n".getBytes( ) );
		         os.write ( "<body>\n".getBytes( ) );
		         os.write ( "<h1>404 IMAGE Not Found</h1>\n".getBytes() ); 
		         os.write ( "</body>\n".getBytes() );  
		         os.write ( "</html>\n".getBytes( ) );               
		             
		      }// end catch. 
		   
	   }//end else
	} // end writeContent method. 

} // end class
