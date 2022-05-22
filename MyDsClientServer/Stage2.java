import java.io.*;
import java.net.*;

public class Stage2 {
  public static void main(String[] args) {
    try {

      Socket s = new Socket("localhost", 50000);
      DataOutputStream os = new DataOutputStream(s.getOutputStream());
      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

      String serverMsg = "";

      // establish connection
      sendMessageToServer("HELO", os);
      
      sendMessageToServer("AUTH braydon", os);

  
  
      
      
      // global job information variables
      int curJobId = -1;
      int curJobRequiredCores = 0;
      int curJobRequiredMemory = 0;
      int curJobRequiredDisk = 0;
    
      // target server object to store server information
      ServerInfo targetServerInfo = new ServerInfo("N/A", -1, "N/A", -1, -1, -1);

      // main loop for job scheduling
      while (!serverMsg.equals("NONE")) {
        
        // read and store server messages
        serverMsg = in.readLine();
        String[] serverMsgVals = serverMsg.split(" ");
       

        // handle OK and JCPL
        if(serverMsg.equals("OK") || serverMsgVals[0].equals("JCPL")) {
          sendMessageToServer("REDY", os);
        }
        
        // handle jobs
        if (serverMsgVals[0].equals("JOBN")) {
          
          // store current job id        
          curJobId = Integer.parseInt(serverMsgVals[2]);         
          curJobRequiredCores = Integer.parseInt(serverMsgVals[4]);
          curJobRequiredMemory = Integer.parseInt(serverMsgVals[5]);
          curJobRequiredDisk = Integer.parseInt(serverMsgVals[6]);
         
          sendMessageToServer("GETS Capable " + serverMsgVals[4] + " " + serverMsgVals[5] + " " + serverMsgVals[6], os);

          // skip rest of JOBN
          while(serverMsgVals[0].equals("JOBN")) {
             serverMsgVals = in.readLine().split(" ");  
          }
       
        }

        // handle server data
        if(serverMsgVals[0].equals("DATA")) {
          sendMessageToServer("OK", os);
          
          String curCapableTye = "";
          int curCapableId = 0;
            
          // iterate through all potential capable servers
             for(int i = 0; i < Integer.parseInt(serverMsgVals[1]); i++) {
           
              String[] serverInfo = in.readLine().split(" ");

               // currrent server information
              int curServerCores = Integer.parseInt(serverInfo[4]);
              int curServerMemory = Integer.parseInt(serverInfo[5]);
              int curServerDisk = Integer.parseInt(serverInfo[6]);
              int curServerWaitingJobs = Integer.parseInt(serverInfo[7]);
      
            
              System.out.println("waiting jobs: " + serverInfo[7] + " running jobs: " + serverInfo[8] + " " + "Server: " + serverInfo[0] + " " + serverInfo[1]);
              
              // store capable server information
              curCapableTye = serverInfo[0];
              curCapableId = Integer.parseInt(serverInfo[1]);

                // break once the capable server is found
                 if(curServerMemory >= curJobRequiredMemory && 
                 curServerDisk >= curJobRequiredDisk && 
                 curServerCores >= curJobRequiredCores && 
                 curServerWaitingJobs == 0              
                 ) {

                   break;
                 }
              
              
          }

          // store capable server information globally
          targetServerInfo.type = curCapableTye;
          targetServerInfo.id = curCapableId;

          sendMessageToServer("OK", os);
          
        }

        // after all data received
        if(serverMsgVals[0].equals(".")){
          // schedule job with global server and job information
          sendMessageToServer("SCHD " + curJobId + " "+ targetServerInfo.type + " " + targetServerInfo.id, os);
        }

        
        // quit if errors received 
        if (serverMsg.split(" ")[0].equals("ERR:")) {
          break;
        }
      }

      // quit
      sendMessageToServer("QUIT", os);

      os.close();
      s.close();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static void sendMessageToServer(String message, DataOutputStream outputStream) {
    try {
      outputStream.write((message+"\n").getBytes());
      outputStream.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}

// to store server information
class ServerInfo {

  String type;
  int id;
  String state;
  int cores;
  int memory;
  int disk;

  public ServerInfo(String type, int id, String state, int cores, int memory, int disk) {
    this.type = type;
    this.id = id;
    this.state = state;
    this.cores = cores;
    this.memory = memory;
    this.disk = disk;
  }

  public void printServerInfo() {
    System.out.println(
        "Server name: " + type + " " +
        "Server id: " + id + " " +
        "Server cores: " + cores + " " +
        "Server memory: " + memory + " " +
        "Server disk: " + disk
    );
  }

}
