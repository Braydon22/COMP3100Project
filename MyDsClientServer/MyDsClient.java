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

      String serverMsg = "", helo = "HELO\n", auth = "AUTH braydon\n";

      // establish connection
      os.write(helo.getBytes("US-ASCII"));
      os.write(auth.getBytes("US-ASCII"));

      // get largest server type
      serverMsg = in.readLine();
      if (serverMsg.equals("OK")) {
        os.write(("REDY\n").getBytes("US-ASCII"));
        os.write(("GETS All\n").getBytes("US-ASCII"));

        // skip JOBN and read DATA
        serverMsg = in.readLine();
        while (!serverMsg.split(" ")[0].equals("DATA")) {
          serverMsg = in.readLine();
        }

        // loop through data to get largest server type
        os.write(("OK\n").getBytes("US-ASCII"));
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

        os.write(("OK\n").getBytes("US-ASCII"));
        System.out.println("OK");
      }

      // schedule first job
      serverMsg = in.readLine();
      if (serverMsg.equals(".")) {
        os.write(("SCHD 0 " + largestServer.type + " " + largestServer.id + "\n").getBytes("US-ASCII"));
      }

      // get and schedule remaining jobs
      while (!serverMsg.equals("NONE")) {
        serverMsg = in.readLine();
        String[] serverMsgVals = serverMsg.split(" ");
        System.out.println("Server Global Start: " + serverMsg);
        // System.out.println("Server: "+serverMsgVals[0]);

        if (serverMsg.equals("OK")) {
          
          os.write(("REDY\n").getBytes("US-ASCII"));
          
          os.write(("GETS Type " + largestServer.type + "\n").getBytes("US-ASCII"));

          // wait for data 
          while(!serverMsgVals[0].equals("DATA")){
            serverMsg = in.readLine();
            serverMsgVals = serverMsg.split(" ");
            //System.out.println("Waiting for Data: "+serverMsg);

          }
          os.write(("OK\n").getBytes("US-ASCII"));
          //System.out.println("Client: OK");

          // get sersers of largest type
        if (serverMsgVals[0].equals("DATA")) {
          //System.out.println("Server: " + serverMsg);
          String serverInfo;
          int numberOfLargestServer = Integer.parseInt(serverMsgVals[1]);

          for (int i = 0; i < numberOfLargestServer; i++) {
            serverMsg = in.readLine();
            serverInfo = serverMsg;
            System.out.println("Server Info: "+serverInfo);
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

          os.write(("OK\n").getBytes("US-ASCII"));
          //System.out.println("Client: OK");
          
          // ready receie job once all data is received
          serverMsg = in.readLine();
          //System.out.println("DATA END: "+serverMsg);
          if(serverMsg.equals(".")){
            os.write(("REDY\n").getBytes("US-ASCII"));
          }
          
          // skip JCPL 
          serverMsg = in.readLine();
          while(serverMsg.split(" ")[0].equals("JCPL")){
            os.write(("REDY\n").getBytes("US-ASCII"));
            serverMsg = in.readLine();
          }
          
          //schedule job
          if(serverMsg.split(" ")[0].equals("JOBN")){
              os.write(("SCHD " + serverMsg.split(" ")[2] + " " + largestServer.type + " " + largestServer.id + "\n")
              .getBytes("US-ASCII"));
              System.out.println("SCHD " + serverMsg.split(" ")[2] + " " + largestServer.type + " " + largestServer.id);
          }
        }

        if (serverMsgVals[0].equals("ERR:")) {
          break;
        }

        System.out.println("Server Global END: "+serverMsg);
      }

      os.write(("QUIT\n").getBytes("US-ASCII"));

      os.close();
      s.close();
    } catch (Exception e) {
      System.out.println(e);
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