import java.io.*;  
import java.net.*;  
public class MyDsClient {  
public static void main(String[] args) {  
try{      
Socket s=new Socket("localhost",50000);  
DataOutputStream os=new DataOutputStream(s.getOutputStream());
DataInputStream is=new DataInputStream(s.getInputStream());
BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

String serverMsg = "", cmd = "", helo="HELO", auth="AUTH braydon";

// establish connection
os.write(helo.getBytes("US-ASCII"));
os.write(auth.getBytes("US-ASCII"));


while(!cmd.equals("QUIT")) {
cmd = br.readLine();
os.write(cmd.getBytes("US-ASCII"));
serverMsg = is.readUTF();
System.out.println("Server: "+serverMsg);
}

os.close();
is.close(); 
s.close();  
}catch(Exception e){System.out.println(e);}  
}  
}  
