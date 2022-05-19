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

  
  
      // get and schedule remaining jobs until server send NONE
      
      int curJobId = -1;
      String targetServerName = "";
      String targetServerID = "";
      ServerInfo targetServerInfo = new ServerInfo("N/A", -1, "N/A", -1, -1, -1);
      while (!serverMsg.equals("NONE")) {
        
        serverMsg = in.readLine();
        String[] serverMsgVals = serverMsg.split(" ");
        //System.out.println(serverMsg);
        // handle OK and JCPL
        if(serverMsg.equals("OK") || serverMsgVals[0].equals("JCPL")) {
          sendMessageToServer("REDY", os);
        }
        
        if (serverMsgVals[0].equals("JOBN")) {
          
          // store current job id
          curJobId = Integer.parseInt(serverMsgVals[2]);
          //System.out.println("GETS Capable " + serverMsgVals[4] + " " + serverMsgVals[5] + " " + serverMsgVals[6]);
          sendMessageToServer("GETS Capable " + serverMsgVals[4] + " " + serverMsgVals[5] + " " + serverMsgVals[6], os);

          
          // while(serverMsgVals[0].equals("JOBN")) {
          //    serverMsgVals = in.readLine().split(" ");  
          // }
       
        }

        if(serverMsgVals[0].equals("DATA")) {
          sendMessageToServer("OK", os);

          String curCapableTye = "";
          int curCapableId = 0;
          int curLargestCore = 0;
             for(int i = 0; i < Integer.parseInt(serverMsgVals[1]); i++) {
           
              String[] serverInfo = in.readLine().split(" ");
              int curServerCores = Integer.parseInt(serverInfo[4]);
              Double curServerWaitingJobs = Double.parseDouble(serverInfo[7]);
              Double curServerRunningJobs = Double.parseDouble(serverInfo[8]);
              Double totalJobs = curServerRunningJobs + curServerWaitingJobs;

              if((curServerCores > curLargestCore)){
              System.out.println("waiting jobs: " + serverInfo[7] + " running jobs: " + serverInfo[8] + " " + "Server: " + serverInfo[0] + " " + serverInfo[1]);
                curLargestCore = Integer.parseInt(serverInfo[4]);

                curCapableTye = serverInfo[0];
                curCapableId = Integer.parseInt(serverInfo[1]);
                
                if(curServerWaitingJobs == 0){
                  break;
                }
              }
              //System.out.println("Current Job Id: " + curJobId);
              
          }

          targetServerInfo.type = curCapableTye;
          targetServerInfo.id = curCapableId;

          sendMessageToServer("OK", os);
          
        }

        if(serverMsgVals[0].equals(".")){
          sendMessageToServer("SCHD " + curJobId + " "+ targetServerInfo.type + " " + targetServerInfo.id, os);
        }

        

        if (serverMsg.split(" ")[0].equals("ERR:")) {
          break;
        }
      }

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
