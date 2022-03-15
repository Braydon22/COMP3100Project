import java.io.*;  
import java.net.*;  
public class MyDsClient {  
public static void main(String[] args) {  
try{      
Socket s=new Socket("localhost",50000);  
DataOutputStream dout=new DataOutputStream(s.getOutputStream());
DataInputStream din=new DataInputStream(s.getInputStream());
BufferedReader br=new BufferedReader(new InputStreamReader(System.in));

String serverMsg = "", cmd = "";
while(!cmd.equals("stop")){
cmd = br.readLine();
dout.writeUTF(cmd);  
serverMsg = din.readUTF();
System.out.println("Server: "+serverMsg);
dout.flush();
}

din.close();  
s.close();  
}catch(Exception e){System.out.println(e);}  
}  
}  
