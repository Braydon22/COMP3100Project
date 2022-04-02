import java.io.*;
import java.net.*;

public class MyDsClient {
  public static void main(String[] args) {
    try {

      Socket s = new Socket("localhost", 50000);
      DataOutputStream os = new DataOutputStream(s.getOutputStream());
      BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

      // default largest
      ServerInfo largestServer = new ServerInfo("N/A", 0, "N/A", 0, 0, 0);
      int nextLargestServerIdx = 1;

      String serverMsg = "";

      // establish connection
      
      sendMessageToServer("HELO", os);
      
      sendMessageToServer("AUTH braydon", os);

      // get largest server type
      serverMsg = in.readLine();
      if (serverMsg.equals("OK")) {
        sendMessageToServer("REDY", os);
        sendMessageToServer("GETS All", os);

        // skip JOBN and read DATA
        serverMsg = in.readLine();
        while (!serverMsg.split(" ")[0].equals("DATA")) {
          serverMsg = in.readLine();
        }

        // loop through data to get largest server type
        sendMessageToServer("OK", os);
        System.out.println("OK");
        String[] dataVals = serverMsg.split(" ");
        if (dataVals[0].equals("DATA")) {
          int numberOfServers = Integer.parseInt(dataVals[1]);

          for (int i = 0; i < numberOfServers; i++) {
            String serverInfo = in.readLine();
            String[] serverInfoVals = serverInfo.split(" ");

            int serverCores = Integer.parseInt(serverInfoVals[4]);
            if (largestServer.cores < serverCores) {
              String serverType = serverInfoVals[0];
              int serverId = Integer.parseInt(serverInfoVals[1]);
              String serverState = serverInfoVals[2];
              int serverMemory = Integer.parseInt(serverInfoVals[5]);
              int serverDisk = Integer.parseInt(serverInfoVals[6]);

              largestServer = new ServerInfo(serverType, serverId, serverState, serverCores, serverMemory, serverDisk);
              System.out.println(largestServer.type);
            }

          }

        }
        sendMessageToServer("OK", os);
        System.out.println("OK");
      }

      // schedule first job
      serverMsg = in.readLine();
      if (serverMsg.equals(".")) {
        sendMessageToServer("SCHD 0 " + largestServer.type + " " + largestServer.id, os);
      }

      // get and schedule remaining jobs
      int curJobId = 0;
      while (!serverMsg.equals("NONE")) {
        
        serverMsg = in.readLine();
        String[] serverMsgVals = serverMsg.split(" ");
        
        if(serverMsg.equals("OK") || serverMsgVals[0].equals("JCPL")) {
          sendMessageToServer("REDY", os);
        }

        if (serverMsgVals[0].equals("JOBN")) {
          curJobId = Integer.parseInt(serverMsgVals[2]);

          sendMessageToServer("GETS Type " + largestServer.type, os);
          
          // get sersers of largest type
          serverMsg = in.readLine();
          serverMsgVals = serverMsg.split(" ");
          if (serverMsgVals[0].equals("DATA")) {
            String serverInfo;
            int numberOfLargestServer = Integer.parseInt(serverMsgVals[1]);
            sendMessageToServer("OK", os);
            
          for (int i = 0; i < numberOfLargestServer; i++) {
            serverMsg = in.readLine();
            serverInfo = serverMsg;
            String[] serverInfoVals = serverInfo.split(" ");

            if (nextLargestServerIdx == i) {

              ServerInfo sInfo = new ServerInfo(
                  serverInfoVals[0],
                  Integer.parseInt(serverInfoVals[1]),
                  serverInfoVals[2],
                  Integer.parseInt(serverInfoVals[3]),
                  Integer.parseInt(serverInfoVals[4]),
                  Integer.parseInt(serverInfoVals[5]));

              largestServer = sInfo;
            }

          }

          nextLargestServerIdx++;
          if (nextLargestServerIdx == numberOfLargestServer) {
            nextLargestServerIdx = 0;
          }

        }

          
          sendMessageToServer("OK", os);
          
          // schedule job once all data is received
          serverMsg = in.readLine();
          if(serverMsg.equals(".")){
            sendMessageToServer("SCHD " + curJobId + " " + largestServer.type + " " + largestServer.id, os);
              System.out.println("SCHD " + curJobId + " " + largestServer.type + " " + largestServer.id);
          }
        }

        if (serverMsg.split(" ")[0].equals("ERR:")) {
          break;
        }

        System.out.println("Server Global END: "+serverMsg);
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
