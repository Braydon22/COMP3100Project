import java.io.*;  
import java.net.*;
import javax.xml.parsers.DocumentBuilderFactory;  
import javax.xml.parsers.DocumentBuilder;  
import org.w3c.dom.Document;  
import org.w3c.dom.NodeList;  
import org.w3c.dom.Node;  
import org.w3c.dom.Element;  
import java.io.File; 

public class MyDsClient {  
public static void main(String[] args) {  
try{
      
Socket s=new Socket("localhost", 50000);  
DataOutputStream os=new DataOutputStream(s.getOutputStream());
BufferedReader in=new BufferedReader(new InputStreamReader(s.getInputStream()));

// default largest
ServerInfo largestServer = new ServerInfo("N/A", 0, "N/A", 0, 0, 0);
int nextLargestServerIdx = 1;

// retrieve largest server xml file and find largest server
File dsXmlFile = new File("ds-system.xml");
DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
Document doc = docBuilder.parse(dsXmlFile);

NodeList dsNodeList = doc.getElementsByTagName("server");

for(int i = 0; i < dsNodeList.getLength(); i++){
int serverCores = Integer.parseInt(dsNodeList.item(i).getAttributes().getNamedItem("cores").getNodeValue());
if(largestServer.cores < serverCores){
String serverType = dsNodeList.item(i).getAttributes().getNamedItem("type").getNodeValue();
int serverMemory = Integer.parseInt(dsNodeList.item(i).getAttributes().getNamedItem("memory").getNodeValue());
int serverDisk = Integer.parseInt(dsNodeList.item(i).getAttributes().getNamedItem("disk").getNodeValue());
largestServer = new ServerInfo(serverType, 0, "in-active", serverCores, serverMemory, serverDisk);
}
}

String serverMsg = "", helo="HELO\n", auth="AUTH braydon\n";

// establish connection
os.write(helo.getBytes("US-ASCII"));
os.write(auth.getBytes("US-ASCII"));

// get and schedule jobs 
while(!serverMsg.equals("NONE")) {
serverMsg = in.readLine();
//System.out.println("Server: "+serverMsg);
String[] serverMsgVals = serverMsg.split(" ");
//System.out.println("Server: "+serverMsgVals[0]);
 

if(serverMsgVals[0].equals("DATA")){

String serverInfo; 
int numberOfLargestServer = Integer.parseInt(serverMsgVals[1]);

for(int i = 0; i < numberOfLargestServer; i++) {
serverInfo = in.readLine();
String[] serverInfoVals = serverInfo.split(" ");

if(nextLargestServerIdx == i){

ServerInfo sInfo = new ServerInfo(
  serverInfoVals[0], 
  Integer.parseInt(serverInfoVals[1]), 
  serverInfoVals[2], 
  Integer.parseInt(serverInfoVals[3]), 
  Integer.parseInt(serverInfoVals[4]), 
  Integer.parseInt(serverInfoVals[5])
 );
 
 largestServer = sInfo;
 nextLargestServerIdx++;
 
 break;
}
 
 //sInfo.printServerInfo();
}

if(nextLargestServerIdx == numberOfLargestServer){
nextLargestServerIdx = 0;
}

}


if(serverMsg.equals("OK")){
os.write(("REDY\n").getBytes("US-ASCII"));

os.write(("GETS Type "+largestServer.type+"\n").getBytes("US-ASCII"));

os.write(("OK\n").getBytes("US-ASCII"));
os.write(("OK\n").getBytes("US-ASCII"));


}

if(serverMsgVals[0].equals("JOBN")) {
os.write(("SCHD "+serverMsgVals[2]+" "+largestServer.type+" "+largestServer.id+"\n").getBytes("US-ASCII"));
System.out.println("SCHD "+serverMsgVals[2]+" "+largestServer.type+" "+largestServer.id);
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
int cores;
int memory;
int disk;

public ServerInfo(String type, int id, String state, int cores, int memory, int disk){
  this.type = type;
  this.id = id;
  this.state = state;
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
