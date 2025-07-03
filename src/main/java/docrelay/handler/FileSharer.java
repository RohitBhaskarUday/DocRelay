package docrelay.handler;

import docrelay.utils.UploadCode;

import java.io.File;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class FileSharer {

    // This class is responsible for who's files are being shared on the Thread/Port.
    // It will maintain a history of all the files that are being served on the server.

    private HashMap<Integer, String> availableFiles;

    public FileSharer(){
        availableFiles = new HashMap<>();
    }

    public int offerFile(String filePath){
        int port;

        while(true){
            port = UploadCode.generateCode();
            if(availableFiles.containsKey(port)){
                availableFiles.put(port, filePath);
                return port;
            }
        }
        //if it's going to infinite loop we can put a timeout over here.
    }

    public void startFileServer(int port){
        String filePath = availableFiles.get(port);
        if(filePath==null){
            System.out.println("No File is present with the port: "+port);
            return;
        }

        //socket server established
        try(ServerSocket serverSocket = new ServerSocket(port)){
            System.out.println("Serving file  "+ new File(filePath).getName() + " on port "+ port);
            //client socket
            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connection: "+ clientSocket.getInetAddress());
            new Thread(new FileSenderHandler(clientSocket, filePath)).start();



        } catch (Exception e) {
            System.out.println("Error handling file server on port: "+port);
            e.printStackTrace();
        }

    }







}
