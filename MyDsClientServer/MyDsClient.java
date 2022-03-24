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

// get and schedule jobs 

int jobId = 0;

while(!serverMsg.equals("NONE")) {
serverMsg = in.readLine();
//System.out.println("Server: "+serverMsg);
String[] serverMsgVals = serverMsg.split("\\s+");
//System.out.println("Server: "+serverMsgVals[0]);
 

ServerInfo largestServer = new ServerInfo("N/A", 0, "N/A", 0, 0, 0, 0);

if(serverMsgVals[0].equals("DATA")){

String serverInfo; 

for(int i = 0; i < Integer.parseInt(serverMsgVals[1]); i++) {
serverInfo = in.readLine();
String[] serverInfoVals = serverInfo.split("\\s+");

ServerInfo sInfo = new ServerInfo(
  serverInfoVals[0], 
  Integer.parseInt(serverInfoVals[1]), 
  serverInfoVals[2], 
  Integer.parseInt(serverInfoVals[3]), 
  Integer.parseInt(serverInfoVals[4]), 
  Integer.parseInt(serverInfoVals[5]), 
  Integer.parseInt(serverInfoVals[6])
 );
 
 sInfo.printServerInfo();

 if(sInfo.cores > largestServer.cores) {
   largestServer = sInfo;
 }
 
}

System.out.println("largest server type: " + largestServer.type + " " + largestServer.id);
}


if(serverMsg.equals("OK")){
os.write(("REDY\n").getBytes("US-ASCII"));

os.write(("GETS All\n").getBytes("US-ASCII"));

os.write(("OK\n").getBytes("US-ASCII"));
os.write(("OK\n").getBytes("US-ASCII"));


os.write(("SCHD "+jobId+" joon 1\n").getBytes("US-ASCII"));
jobId++;
}




if(serverMsgVals[0].equals("JCPL")){
os.write(("REDY\n").getBytes("US-ASCII"));
}

}

os.write(("QUIT\n").getBytes("US-ASCII"));



os.close(); 
s.close();  
}catch(Exception e){System.out.println(e);}  
}  
}  


class ServerInfo {  

String type;
int id;
String state;
int curStartTime;
int cores;
int memory;
int disk;

public ServerInfo(String type, int id, String state, int curStartTime, int cores, int memory, int disk){
  this.type = type;
  this.id = id;
  this.state = state;
  this.curStartTime = curStartTime;
  this.cores = cores;
  this.memory = memory;
  this.disk = disk;
}

public void printServerInfo(){
System.out.println(
"Server name: " + type + " " +
"Server id: " + id + " " +
"Server cores: " + cores + " " +
"Server memory: " + memory + " " +
"Server disk: " + disk 
);
}

}
