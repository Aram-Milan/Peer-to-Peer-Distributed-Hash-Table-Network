// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Aram Gholikimilan
// 220036178
// Aram.Gholikimilan@city.ac.uk


import java.io.*;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {
    public ServerSocket serverSocket;
    private final ConcurrentHashMap<String, String> networkMap = new ConcurrentHashMap<>();
    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private ConcurrentHashMap<String, String> keyValueStore = new ConcurrentHashMap<>();
    private String startingNodeName;
    private String startingNodeAddress;
    private String startingNodeHost; // Store the starting node host for potential later use
    private int startingNodePort; // Store the starting node port for potential later use

    public boolean listen(String ipAddress, int portNumber) {
	// Implement this!
	// Return true if the node can accept incoming connections
	// Return false otherwise
	//return true;


        try{
            System.out.println("Opening the server socket on port " + portNumber);
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server waiting for a client...");

            //Socket clientSocket = serverSocket.accept();
            //System.out.println("Client connected!");
            //handleClient(clientSocket);
            return true;

        } catch (Exception e){
            System.out.println("Failed to listen on " + ipAddress + ":" + portNumber + ". Error: " + e.getMessage());
            return false;
        }





        /*
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Listening on " + ipAddress + ":" + portNumber);
            threadPool.execute(this::acceptConnections);
            return true;
        } catch (IOException e) {
            System.err.println("Failed to listen on " + ipAddress + ":" + portNumber + ". Error: " + e.getMessage());
            return false;
        }


         */

        /*
        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            System.out.println("Server started and listening on port " + portNumber);

            // The server should continuously listen for incoming connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                // Handle each connection in a separate thread
                new Thread(() -> handleClient(clientSocket)).start();
            }

        } catch (IOException e) {
            System.err.println("Could not listen on port: " + portNumber);
            e.printStackTrace();
            return false;
        }
         */
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
	// Implement this!
	//return;

        this.startingNodeName=startingNodeName;
        this.startingNodeAddress=startingNodeAddress;
        try{
            String[] parts = startingNodeAddress.split(":");
            if (parts.length != 2) throw new IllegalArgumentException("Invalid address format");
            startingNodeHost = parts[0];
            startingNodePort = Integer.parseInt(parts[1]);

            //serverSocket = new ServerSocket(startingNodePort);
            //Socket clientSocket = serverSocket.accept();
            //System.out.println("Client connected!");
            // handleClient(clientSocket);
            while (true) {
                //System.out.println("Waiting for a client...!");
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected!");
                // Handle each connection in a separate thread

                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Writer out = new OutputStreamWriter(clientSocket.getOutputStream());

                String message = in.readLine();
                handleClient(message, in, out);
                //new Thread(() -> handleClient(in,out)).start();
                System.out.println("The client is handled");
                clientSocket.close();
            }
            /*
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer out = new OutputStreamWriter(clientSocket.getOutputStream());
            System.out.println("here0");

            // We can read what the client has said
            String line = in.readLine();
            System.out.println("The client said : " + line);
            System.out.println("here00");

            if (line.startsWith("START")) {
                // Process START command
                System.out.println("here1");
                handleStartCommand(line, out);
            }

             */
            /*
            while (line  != null) {
                if (line.startsWith("START")) {
                    // Process START command
                    handleStartCommand(line, out);
                        //out.println("START 1 <NodeName>");
                } else if (line.startsWith("PUT?")) {
                    // Process PUT? command
                    handlePutRequest(line, in, out);
                } else if (line.startsWith("GET?")) {
                    // Process GET? command
                    handleGetRequest(line, in, out);
                } else if (line.startsWith("NOTIFY?")) {
                    // Process NOTIFY? command
                    handleNotifyRequest(line, in, out);
                } else if (line.startsWith("NEAREST?")) {
                    // Process NEAREST? command
                    handleNearestRequest(line, in, out);
                } else if (line.startsWith("ECHO?")) {
                    //  This message allows the requester to tell whether the connection is
                    //  still active and the responder is still working correctly.
                    out.write("OHCE");
                } else if (line.startsWith("END")) {
                    break; // Exit the loop and close the connection
                }
            }*/
            //System.out.println("here2");

            //clientSocket.close();
            //System.out.println("here3");

        } catch (Exception e){
            System.out.println("eeerroorr: "+e.getMessage());
        }

        /*
        try {
            // Split the starting node address into host and port
            String[] parts = startingNodeAddress.split(":");
            String host = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (Socket socket = new Socket(host, port);
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // First, initiate the START message exchange
                out.println("START 1 " + this.nodeName);
                String response = in.readLine(); // Read the starting node's START response

                // Now, notify the starting node of your presence
                out.println("NOTIFY " + this.nodeName + " " + socket.getLocalAddress().getHostAddress() + ":" + this.serverSocket.getLocalPort());
                String notifyResponse = in.readLine(); // Expecting a NOTIFIED response

                // Log or handle the NOTIFY response
                System.out.println("NOTIFY response from starting node: " + notifyResponse);

                // Optionally, you can query the starting node for the nearest nodes or for its network map
                // This can help in populating your node's initial view of the network

            } catch (IOException e) {
                System.err.println("Error handling incoming connections: " + e.getMessage());
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number in starting node address.");
        }

         */
    }

//    private void acceptConnections() {
//        while (true) {
//            try {
//                //Socket clientSocket = serverSocket.accept();
//                //threadPool.execute(() -> handleClient(clientSocket));
//            } catch (IOException e) {
//                System.err.println("Error accepting connection. Error: " + e.getMessage());
//            }
//        }
//    }

    private void handleClient(String line,BufferedReader in, Writer out) {
        try{
//            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
//            Writer out = new OutputStreamWriter(clientSocket.getOutputStream());
            //System.out.println("read1");

            //String message = in.readLine();
            //System.out.println(in.readLine());
            //System.out.println("222222");
//            switch (message){
//                case "START ":
//                    handleStartCommand(message,out);
//                case "ECHO":
//                    out.write("OHCE");
//            }
            //System.out.println("read3");
            while (line != null) {
                if (line.startsWith("START")) {
                    // Process START command
                    handleStartCommand(line, out);
                    //out.println("START 1 <NodeName>");
                } else if (line.startsWith("PUT?")) {
                    // Process PUT? command
                    handlePutRequest(line, in, out);
                } else if (line.startsWith("GET?")) {
                    // Process GET? command
                    handleGetRequest(line, in, out);
                } else if (line.startsWith("NOTIFY?")) {
                    // Process NOTIFY? command
                    handleNotifyRequest(line, in, out);
                } else if (line.startsWith("NEAREST?")) {
                    // Process NEAREST? command
                    handleNearestRequest(line, in, out);
                } else if (line.startsWith("ECHO?")) {
                    //  This message allows the requester to tell whether the connection is
                    //  still active and the responder is still working correctly.
                    out.write("OHCE");
                } else if (line.startsWith("END")) {
                    break; // Exit the loop and close the connection
                }
                line=null;
            }



        } catch (IOException e) {
            System.err.println("Error handling client. Error: " + e.getMessage());
        }
       // finally {
            //try {
                //clientSocket.close();
           // } catch (IOException e) {
            //    System.err.println("Error closing client socket. Error: " + e.getMessage());
           // }
       // }
    }

    // Placeholder for request handling methods

    private void handleStartCommand(String line, Writer out) throws IOException {
        // Split the line by spaces to extract the parts
        String[] parts = line.split(" ");
        if (parts.length >= 3) {
            // Assuming the START command format is: START <number> <string>
            String protocolVersion = parts[1];
            String nodeName = parts[2]; // This could potentially include more parts if the name contains spaces
            out.write("START " + protocolVersion + " " + startingNodeName +"\n");
            out.flush();

        } else {
            // Handle invalid START command
            System.err.println("Invalid START command received: " + line);
        }
    }

    private void handlePutRequest(String line, BufferedReader in, Writer out) throws IOException {

        // Extract and process the PUT? request according to the 2D#4 protocol
        try {
            //   PUT? 1 2
            //   Welcome
            //   Hello
            //   World!
            String[] parts = line.split(" ", 3);
            int keyLinesCount = Integer.parseInt(parts[1]);
            int valueLinesCount = Integer.parseInt(parts[2]);

            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < keyLinesCount; i++) {
                keyBuilder.append(in.readLine()).append("\n");
            }

            StringBuilder valueBuilder = new StringBuilder();
            for (int i = 0; i < valueLinesCount; i++) {
                valueBuilder.append(in.readLine()).append("\n");
            }

            String key = keyBuilder.toString();
            String value = valueBuilder.toString();

            System.out.println("key: \n" + key);
            System.out.println("value: \n" + value);
            keyValueStore.put(key,value);
            // When the responder gets a PUT request it must compute the hashID for the value to be stored.
            // Here, implement logic to decide whether to store the key-value pair
            // For simplicity, this example assumes the storage operation is successful
            // You should include your logic for checking hashID distance, etc.

            out.write("SUCCESS\n"); // Or "FAILED" based on your logic
            out.flush();
        } catch (Exception e) {
            System.err.println("Error processing PUT? request: " + e.getMessage());
            out.write("FAILED\n");
            out.flush();
        }
    }

    //TODO: Extract and process the GET? request according to the 2D#4 protocol and the output
    private void handleGetRequest(String line, BufferedReader in, Writer out) {
        //Extract and process the GET? request according to the 2D#4 protocol
        try {
            int keyLinesCount = Integer.parseInt(line.split(" ")[1]); // GET? <number>

            StringBuilder keyBuilder = new StringBuilder();
            for (int i = 0; i < keyLinesCount; i++) {
                keyBuilder.append(in.readLine()).append("\n");
            }

            String key = keyBuilder.toString();

            //The responder MUST compute the hashID of the key.
            // Here, implement logic to retrieve the value associated with the key
            // For simplicity, assume we have a method getValue(key) to get the value
            String value = getValue(key); // Placeholder method
            System.out.println("key: \n" + key);
            System.out.println("value: \n" + value);
            if (value != null) {
                int valueLines = value.split("\n", -1).length - 1; // Adjusted to correctly handle the last newline
                out.write("VALUE " + valueLines +"\n");
                out.write(value+"\n");
                out.flush();
            } else {
                out.write("NOPE\n");
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("Error processing GET? request: " + e.getMessage());
            // Potentially respond with "NOPE" or a custom error message
        }
    }

    //TODO: Extract and process the NOTIFY? request according to the 2D#4 protocol
    private void handleNotifyRequest(String line, BufferedReader in, Writer out) {
        // Extract and process the NOTIFY? request according to the 2D#4 protocol
        // The requester MAY send a NOTIFY request.  This informs the responder of the address of a full node.
        try {
            String nodeName = in.readLine(); // Read node name
            String nodeAddress = in.readLine(); // Read node address

            // Here, update your network map with the new node
            // For simplicity, assume we have a method updateNetworkMap(nodeName, nodeAddress)
            //updateNetworkMap(nodeName, nodeAddress); // Placeholder method

            out.write("NOTIFIED\n");
            out.flush();
        } catch (Exception e) {
            System.err.println("Error processing NOTIFY? request: " + e.getMessage());
            // Handle error case, possibly with a specific protocol message
        }
    }

    // TODO: Extract and process the NEAREST? request according to the 2D#4 protocol
    private void handleNearestRequest(String line, BufferedReader in, Writer out) {
        // Extract and process the NEAREST? request according to the 2D#4 protocol
        try {
            String hashID = line.split(" ")[1];

            // Here, find the three closest nodes to the given hashID
            // For simplicity, assume we have a method findClosestNodes(hashID) that returns a list of node info
            List<String> closestNodes = findClosestNodes(hashID); // Placeholder method

            out.write("NODES " + closestNodes.size() +"\n");
            for (String nodeInfo : closestNodes) {
                out.write(nodeInfo); // Node name
                out.flush();
                //out.println(nodeAddress); // Node address, assume nodeInfo contains this info
            }
        } catch (Exception e) {
            System.err.println("Error processing NEAREST? request: " + e.getMessage());
            // Handle error case
        }
    }

    // Additional helper methods as needed

    private String getValue(String key) {
        // Directly return the value from the store; if key is not present, this returns null
        return keyValueStore.get(key);
    }
    private void updateNetworkMap(String nodeName, String nodeAddress) {
        // Simply put the nodeName and nodeAddress into the map. This updates existing entries or adds new ones.
        networkMap.put(nodeName, nodeAddress);
    }
    private List<String> findClosestNodes(String targetHashIDHex) {
        // This list will hold nodes and their distances to the target hashID
        List<NodeDistance> distances = new ArrayList<>();

        // Calculate the distance between each node's hashID and the target hashID
        for (Map.Entry<String, String> entry : networkMap.entrySet()) {
            String nodeName = entry.getKey();
            // Assuming a method to get the node's hashID in hex format
            String nodeHashIDHex;
            try {
                byte[] nodeHashID = HashID.computeHashID(nodeName + "\n"); // Ensure node names end with a newline character for consistency
                nodeHashIDHex = bytesToHex(nodeHashID); // Convert the byte array to hex string
            } catch (Exception e) {
                e.printStackTrace();
                continue; // Skip this node on error
            }

            int distance = calculateDistance(nodeHashIDHex, targetHashIDHex);
            distances.add(new NodeDistance(entry.getValue(), distance)); // entry.getValue() is assumed to be the node address
        }

        // Sort by distance
        distances.sort(Comparator.comparingInt(NodeDistance::getDistance));

        // Select the top three closest nodes
        return distances.stream().limit(3)
                .map(NodeDistance::getAddress)
                .collect(Collectors.toList());
    }
    private int calculateDistance(String hashID1, String hashID2) {
        // Convert hashIDs from hex to byte arrays
        byte[] hash1 = hexStringToByteArray(hashID1);
        byte[] hash2 = hexStringToByteArray(hashID2);

        // Call your existing HashID.calculateDistance method
        return HashID.calculateDistance(hash1, hash2);
    }
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
    public static byte[] hexStringToByteArray(String hexString) {
        // Normalize the hex string to remove the "0x" prefix if present
        hexString = hexString.startsWith("0x") ? hexString.substring(2) : hexString;

        // Handle the case where the hex string length is odd by prepending a "0"
        if (hexString.length() % 2 != 0) {
            hexString = "0" + hexString;
        }

        // Convert the hex string to a byte array
        byte[] byteArray = new BigInteger(hexString, 16).toByteArray();

        // BigInteger's toByteArray() method returns a byte array containing the two's-complement representation of the BigInteger.
        // The most significant bit is the sign bit (the "0" bit is reserved for positive numbers). If the first byte is 0x00,
        // it means the rest of the array will contain the positive representation of the number. This leading zero byte needs to be removed.
        if (byteArray[0] == 0) {
            byte[] tmp = new byte[byteArray.length - 1];
            System.arraycopy(byteArray, 1, tmp, 0, tmp.length);
            byteArray = tmp;
        }

        return byteArray;
    }


    public static void main(String[] args) throws IOException {
        /*
        // IP Addresses will be discussed in detail in lecture 4
        String IPAddressString = "127.0.0.1";
        InetAddress host = InetAddress.getByName(IPAddressString);

        // Port numbers will be discussed in detail in lecture 5
        int port = 4567;

        // The server side is slightly more complex
        // First we have to create a ServerSocket
        System.out.println("Opening the server socket on port " + port);
        ServerSocket serverSocket = new ServerSocket(port);

        // The ServerSocket listens and then creates as Socket object
        // for each incoming connection
        System.out.println("Server waiting for client...");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client connected!");

        // Like files, we use readers and writers for convenience
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

        // We can read what the client has said
        String message = reader.readLine();
        System.out.println("The client said : " + message);

        // Sending a message to the client at the other end of the socket
        System.out.println("Sending a message to the client");
        writer.write("Nice to meet you\n");
        writer.flush();
        // To make better use of bandwidth, messages are not sent
        // until the flush method is used

        // Close down the connection
        clientSocket.close();

         */

        FullNode fNode = new FullNode();
        if (fNode.listen("127.0.0.1", 4567)) {
            fNode.handleIncomingConnections("martin.brain@city.ac.uk:MyCoolImplementation,1.41,test-node-2", "127.0.0.1:4567");
            System.out.println("DONE!");
        }


    }
}
