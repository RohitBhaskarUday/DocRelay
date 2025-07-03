package docrelay.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.Socket;

public class FileSenderHandler implements Runnable {

    private final Socket clientSocket;
    private final String filePath;

    public FileSenderHandler(Socket clientSocket, String filePath){
        this.clientSocket=clientSocket;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        //Every I/O should be handled using Streams.
        //Even sockets are kind of streams.

        try(FileInputStream fileInputStream = new FileInputStream(filePath)){

            //First, read the file from disk, then send its contents through the socket to the connected client.
            OutputStream outputStream = clientSocket.getOutputStream();
            String fileName = new File(filePath).getName();
            String header = "Filename: "+fileName+"\n";
            outputStream.write(header.getBytes());


            // Read the file and write its contents to the socket output stream
            byte[] buffer = new byte[4096];
            int byteRead=fileInputStream.read(buffer);

            // Keep reading from the file and writing to the socket until the entire file is sent
            while((byteRead) != -1){
                outputStream.write(buffer, 0, byteRead);
                byteRead = fileInputStream.read(buffer);
            }

            System.out.println("File "+fileName + " sent to "+ clientSocket.getInetAddress());

        } catch (Exception e) {
            System.out.println("File Error");
            e.printStackTrace();
        }finally {
            try{
                clientSocket.close();
            }catch (Exception ec){
                System.err.println("Error closing the socket: " +ec.getMessage());
            }
        }


    }
}
