// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
import java.net.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends

//   Temporary
//   nodes are limited members of the network.  They do not store values
//   or respond to requests.
public class TemporaryNode implements TemporaryNodeInterface {
    private Socket clientSocket;
    private BufferedReader reader;
    private Writer writer;
    private String startingNodeName;
    private String startingNodeAddress;
    private InetAddress startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use
    private boolean isConnected = false; // Keep track of the connection state
    String name = "aram@city.ac.uk:12345";
    String host = "127.0.0.1";
    String port = "6969";
    public boolean start(String startingNodeName, String startingNodeAddress) {
        this.startingNodeName=startingNodeName;
        this.startingNodeAddress=startingNodeAddress;
        try{
            String[] parts = startingNodeAddress.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            String IPAddressString = parts[0];
            startingNodeHost = InetAddress.getByName(IPAddressString);
            startingNodePort = Integer.parseInt(parts[1]);
            clientSocket = new Socket(startingNodeHost, startingNodePort);
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new OutputStreamWriter(clientSocket.getOutputStream());
            writer.write("START 1 " + name +"\n");
            writer.flush();
            String response = reader.readLine();
            System.out.println("For START, The FullNode said : " + response);
            if (response != null && response.startsWith("START"))
            {
                isConnected = true; // Update connection status
                return true;
            }
        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public boolean store(String key, String value) {
        int min=99999;
        String minNodeName=this.startingNodeName;
        String minNodeAddress=this.startingNodeAddress;

        ArrayList<String> visitedNodes = new ArrayList<>();
        visitedNodes.add(startingNodeName);
        try {
            while(true){
                int keyLines = key.split("\n").length;
                int valueLines = value.split("\n").length;

                System.out.println("Sending a message to the server");
                writer.write("PUT? " + keyLines + " " + valueLines + "\n" + key + value); //  + "\n" + key + "\n" + value
                writer.flush();

                String response = reader.readLine();
                System.out.println("put response: " + response);

                if (response != null && response.startsWith("SUCCESS")) {
                    clientSocket.close();
                    return true;
                } else if (response.startsWith("FAILED")) {
                    byte[] keyHash = HashID.computeHashID(key);
                    String hexKeyHash = HashID.bytesToHex(keyHash);

                    String nearestNodesInfo = nearest(hexKeyHash);

                    if (nearestNodesInfo == null || nearestNodesInfo.isEmpty()) {
                        System.err.println("Failed to retrieve nearest nodes or none are available.");
                        end("COMPLETE");
                        return false;
                    }

                    // Split the nearestNodesInfo to get individual node details
                    String[] nodeDetails = nearestNodesInfo.split("\n");
                    int numNodes = Integer.parseInt(nodeDetails[0].split(" ")[1]);
                    // Skip the first line which is "NODES X"
                    for (int i = 1; i < numNodes * 2; i += 2) {
                        String nodeName = nodeDetails[i];
                        String nodeAddress = nodeDetails[i + 1];

                        byte[] nodeHashID = HashID.computeHashID(nodeName + "\n");
                        byte[] keyHashId = HashID.computeHashID(key + "\n");
                        int distance = HashID.calculateDistance(nodeHashID, keyHashId);
                        if (distance < min) {
                            min = distance;
                            minNodeName = nodeName;
                            minNodeAddress = nodeAddress;
                        }
                    }

                    end("CANNOT-STORE");
                    clientSocket.close();
                    reader.close();
                    writer.close();

                    //Once smallest node is found,
                    // check if we have already visited it.
                    if(visitedNodes.contains(minNodeName)){
                         return false;
                    }

                    String[] address = minNodeAddress.split(":");
                    int port = Integer.parseInt(address[1]);
                    InetAddress host = InetAddress.getByName(address[0]);
                    clientSocket = new Socket(host, port);

                    reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    writer = new OutputStreamWriter(clientSocket.getOutputStream());


                    writer.write("START 1 " + name + "\n");
                    writer.flush();
                    String r = reader.readLine();
                    System.out.println("here --> :" + r);
                    visitedNodes.add(minNodeName);

                }

            }
        } catch (Exception e){
            System.out.println("Error during PUT? request handling (Store operation): "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public String get(String key) {
        int min=257;
        String minNodeName=this.startingNodeName;
        String minNodeAddress=this.startingNodeAddress;

        ArrayList<String> visitedNodes = new ArrayList<>();
        visitedNodes.add(startingNodeName);
        try {
            while(true){
                int keyLines = key.split("\n").length;

                writer.write("GET? " + keyLines + "\n" + key);
                writer.flush();

                String response = reader.readLine();
                System.out.println("get response: " + response);

                if (response != null && response.startsWith("VALUE")) {
                    clientSocket.close();

                    String[] parts = response.split(" ", 2);
                    int valueLinesCount = Integer.parseInt(parts[1]);
                    StringBuilder valueBuilder = new StringBuilder();
                    for (int i = 0; i < valueLinesCount; i++) {
                        valueBuilder.append(reader.readLine()).append("\n");
                    }

                    String value = valueBuilder.toString();
                    System.out.println("valueeee:\n"+value);
                    return value;
                } else if (response.startsWith("NOPE")) {
                    // Get the hash ID for the key to find nearest nodes
                    byte[] keyHash = HashID.computeHashID(key);
                    String hexKeyHash = HashID.bytesToHex(keyHash);

                    // Call nearest to find nearest nodes
                    String nearestNodesInfo = nearest(hexKeyHash);

                    if (nearestNodesInfo == null || nearestNodesInfo.isEmpty()) {
                        System.err.println("Failed to retrieve nearest nodes or none are available.");
                        end("COMPLETE");
                        return null;
                    }


                    // Split the nearestNodesInfo to get individual node details
                    String[] nodeDetails = nearestNodesInfo.split("\n");
                    System.out.println("nearest nodes: \n" + nearestNodesInfo);
                    int numNodes = Integer.parseInt(nodeDetails[0].split(" ")[1]);
                    // Skip the first line which is "NODES X"
                    for (int i = 1; i < numNodes * 2; i += 2) {
                        String nodeName = nodeDetails[i];
                        String nodeAddress = nodeDetails[i + 1];

                        byte[] nodeHashID = HashID.computeHashID(nodeName + "\n");
                        byte[] keyHashId = HashID.computeHashID(key + "\n");
                        int distance = HashID.calculateDistance(nodeHashID, keyHashId);
                        if (distance < min) {
                            min = distance;
                            minNodeName = nodeName;
                            minNodeAddress = nodeAddress;
                        }
                    }
                    System.out.println("min node: "+ minNodeName);

                    end("CANNOT-STORE");
                    clientSocket.close();
                    reader.close();
                    writer.close();

                    if(visitedNodes.contains(minNodeName)){
                          return null;
                    }

                    String[] address = minNodeAddress.split(":");
                    int port = Integer.parseInt(address[1]);
                    InetAddress host = InetAddress.getByName(address[0]);
                    clientSocket = new Socket(host, port);

                    reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    writer = new OutputStreamWriter(clientSocket.getOutputStream());


                    writer.write("START 1 " + name + "\n");
                    writer.flush();
                    String r = reader.readLine();
                    visitedNodes.add(minNodeName);
                }

            }
        } catch (Exception e){
            System.out.println("Error during GET? request handling: "+e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public void end (String reason){
        try{
            writer.write("END " + reason +"\n");
            writer.flush();
            isConnected = false;
            // Close down the connection
            clientSocket.close();
        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    public boolean echo (){
        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return false;
        }
        try{
            writer.write("ECHO?" +"\n");
            writer.flush();

            String response = reader.readLine();

            if (response != null && response.startsWith("OHCE"))
            {
                isConnected = true; // Update connection status
                return true;
            }

        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public boolean notifyRequest (String request) {
        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return false;
        }

        try{
            String[] parts = request.split("\n");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            String fullNodeName = parts[0];
            String fullNodeAddress = parts[1];

            // Sending a message to the server at the other end of the socket
            writer.write("NOTIFY? \n" + fullNodeName + "\n" + fullNodeAddress + "\n");
            writer.flush();

            String response = reader.readLine();

            if (response != null && response.startsWith("NOTIFIED"))
            {
                isConnected = true; // Update connection status
                return true;
            }

        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }
    public String nearest(String hashID) {
        if (!isConnected) {
            System.out.println("Not connected to any node. Please start connection first.");
            return null;
        }
        try {
            writer.write("NEAREST? " + hashID + "\n");
            writer.flush();

            String response = reader.readLine();
            if (response.startsWith("NODES")) {
                int numberOfNodes = Integer.parseInt(response.split(" ")[1]);
                StringBuilder nodesInfo = new StringBuilder();
                nodesInfo.append(response).append("\n"); // Include the "NODES X" line
                for (int i = 0; i < numberOfNodes; i++) {
                    String nodeName = reader.readLine().trim(); // Trim any trailing newlines
                    String nodeAddress = reader.readLine().trim(); // Trim any trailing newlines
                    nodesInfo.append(nodeName).append("\n").append(nodeAddress).append("\n");
                }
                return nodesInfo.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
    public static void main(String[] args) throws IOException {
        TemporaryNode tNode = new TemporaryNode();

        System.out.println("\n===================\n");
        System.out.println("Start: ");
        tNode.start("mohammed.siddiqui@city.ac.uk:2D#4Impl,0.1,FullNode,0","10.210.71.249:20000");

/*
        System.out.println("\n===================\n");
        System.out.println("Store: ");
        tNode.store("Aram\n","The\nKing!");


        System.out.println("\n===================\n");
        System.out.println("Get: ");
        tNode.get("Aram\n");





        System.out.println("\n===================\n");
        System.out.println("Echo: ");
        tNode.echo();

        System.out.println("\n===================\n");
        System.out.println("Notify: ");
        tNode.notifyRequest("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2\n"+ "127.0.0.1:3456");


 */
        System.out.println("\n===================\n");
        System.out.println("Nearest: ");
        String nearestNodes = tNode.nearest("0f003b106b2ce5e1f95df39fffa34c2341f2141383ca46709269b13b1e6b4832");
        System.out.println(nearestNodes);

        /*
        System.out.println("\n===================\n");
        System.out.println("End: ");
        tNode.end("no requests!");


 */


    }
}
