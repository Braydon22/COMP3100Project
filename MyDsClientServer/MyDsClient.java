import java.io.*;  
import java.net.*;  
public class MyDsClient {  
public static void main(String[] args) {  
try{      
Socket s=new Socket("localhost", 50000);  
DataOutputStream os=new DataOutputStream(s.getOutputStream());
BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));

String serverMsg = "", cmd = "", helo="HELO\n", auth="AUTH braydon\n";

// establish connection
os.write(helo.getBytes("US-ASCII"));
os.write(auth.getBytes("US-ASCII"));

os.write(("REDY\n").getBytes("US-ASCII"));

serverMsg = in.readLine();
System.out.println("Server: "+serverMsg);

os.write(("QUIT\n").getBytes("US-ASCII"));



os.close(); 
s.close();  
}catch(Exception e){System.out.println(e);}  
}  
}  
