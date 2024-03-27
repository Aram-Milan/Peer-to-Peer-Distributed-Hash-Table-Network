// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

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
    public boolean start(String startingNodeName, String startingNodeAddress) {
        // Implement this!
        // Return true if the 2D#4 network can be contacted
        // Return false if the 2D#4 network can't be contacted
        //return true;
            this.startingNodeName=startingNodeName;
            this.startingNodeAddress=startingNodeAddress;
            try{
                String[] parts = startingNodeAddress.split(":");
                if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
                String IPAddressString = parts[0];
                startingNodeHost = InetAddress.getByName(IPAddressString);
                startingNodePort = Integer.parseInt(parts[1]);

                //System.out.println("TCPClient connecting to " + startingNodeAddress);
                //System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
                clientSocket = new Socket(startingNodeHost, startingNodePort);
                reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                writer = new OutputStreamWriter(clientSocket.getOutputStream());

                // Sending a message to the server at the other end of the socket
//                System.out.println("Sending a message to the server");
//                System.out.println(startingNodeName);
                writer.write("START 1 " + startingNodeName +"\n");
                writer.flush();

                String response = reader.readLine();
                //System.out.println("The server said : " + response);

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
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
	//return true;
        if (!isConnected) {
            System.err.println("Not connected to any node. Please start connection first.");
            return false;
        }

        try{
            // Append new line if not present
//            if (!key.endsWith("\n")) key += "\n";
            if (!value.endsWith("\n")) value += "\n";

            // Count the number of lines in both key and value
            int keyLines = key.split("\n").length;
            int valueLines = value.split("\n", -1).length-1; // Adjusted to correctly handle the last newline

            // you have the host and port from start
            //System.out.println("TCPClient connecting to " + startingNodeAddress);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("PUT? " + keyLines + " " + valueLines + "\n"); //  + "\n" + key + "\n" + value
            writer.write(key);
            writer.write(value+"\n");
            System.out.println("the value in temp: \n"+value);
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);

            if (response != null && response.startsWith("SUCCESS"))
            {
                //isConnected = true; // Update connection status
                return true;
            } else{
                // call nearest find the distance between those three nodes and the key,
                // start a connection with the closest node, and try storing.
                // if that was failed we continue the process.
            }

        } catch (Exception e){
            System.out.println("Error during PUT? request handling (Store operation): "+e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public String get(String key) {
	// Implement this!
	// Return the string if the get worked
	// Return null if it didn't
	//return "Not implemented";

        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return null;
        }

        try{
            // ensure the key ends with a newline
            //if (!key.endsWith("\n")) key += "\n";
            // Count the number of lines in both key and value
            int keyLines = key.split("\n").length;

            // you have the host and port from start
            //System.out.println("TCPClient connecting to " + startingNodeAddress);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            //String threeClosestNodes = nearest(key);
            // Sending a message to the server at the other end of the socket
            //System.out.println("Sending a message to the server");
            writer.write("GET? " + keyLines + "\n"+key);
            writer.flush();

            String response = reader.readLine();
            //System.out.println("the response, sdfof: "+ response);
            //String[] parts = response.split(" ", 1);

            //int valueLinesCount = Integer.parseInt(parts[1]);
            int valueLinesCount2 = Integer.parseInt(response.split(" ")[1]);
            StringBuilder valueBuilder = new StringBuilder();
            valueBuilder.append(response).append("\n");
            for (int i = 0; i < valueLinesCount2; i++) {
                valueBuilder.append(reader.readLine()).append("\n");
            }

            String valueResponse = valueBuilder.toString();

            System.out.println("The sdjcbshkhgsraubserver said : \n" + valueResponse); //valueResponse
            //String response2 = reader.readLine();
            //System.out.println("The server said2 : " + response2);

            if (response.startsWith("VALUE"))
            {
                return valueResponse;

            }

        } catch (Exception e){
            System.out.println("Error during GET? request handling: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void end (String reason){

        try{
            System.out.println("TCPClient connecting to " + startingNodeAddress);
            System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
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
            System.out.println("TCPClient connecting to " + startingNodeAddress);
            System.out.println(startingNodeHost.toString() + "  :  "+startingNodePort);
            //Socket clientSocket = new Socket(startingNodeHost, startingNodePort);
            //BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("ECHO?" +"\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);

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
            System.out.println("Sending a message to the server");
            writer.write("NOTIFY? \n" + fullNodeName + "\n" + fullNodeAddress + "\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);

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
                // Print the complete nodes information for debugging before returning
                System.out.println("Complete nodes information received:\n" + nodesInfo);
                return nodesInfo.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }


/*
    public String nearest (String hash){// the string is a hashID written in hex
        if (!isConnected){
            System.out.println("Not connected to any node. Please start connection first");
            return null;
        }
        try{
            // Sending a message to the server at the other end of the socket
            System.out.println("Sending a message to the server");
            writer.write("NEAREST? ");
            writer.write(hash + "\n");
            writer.flush();

            String response = reader.readLine();

            String[] parts = response.split(" ", 2);

            System.out.println(Arrays.toString(parts));
            int nodesCount = Integer.parseInt(parts[1]);
            System.out.println(nodesCount);
            StringBuilder valueBuilder = new StringBuilder();
            valueBuilder.append(response).append("\n");
            for (int i = 0; i < nodesCount ; i++) {
                valueBuilder.append(reader.readLine()).append("\n");
                valueBuilder.append(reader.readLine()).append("\n");
                System.out.println(i+":"+valueBuilder);
            }

            String valueResponse = valueBuilder.toString();
            System.out.println("last: "+valueBuilder);

            System.out.println("The server said : \n" + valueResponse);


            if (response.startsWith("NODES"))
            {
                isConnected = true; // Update connection status
                return valueResponse;
            }

        } catch (Exception e){
            System.out.println("Connecting attempt failed: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }


 */
    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void main(String[] args) throws IOException {
        TemporaryNode tNode = new TemporaryNode();

        System.out.println("\n===================\n");
        System.out.println("Start: ");
        tNode.start("aram.gholikimilan@city.ac.uk:MyCoolImplementation,1.41,test-node-2","127.0.0.1:6969");
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
