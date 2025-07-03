package docrelay.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.net.Socket;

public class DownloadHandler implements HttpHandler {
    /**
     * Handle the given request and generate an appropriate response.
     * See {@link HttpExchange} for a description of the steps
     * involved in handling an exchange.
     *
     * @param exchange the exchange containing the request from the
     *                 client and used to send the response
     * @throws NullPointerException if exchange is {@code null}
     * @throws IOException          if an I/O error occurs
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.add("Access-Control-Allow-Origin","*");

        if(!exchange.getRequestMethod().equalsIgnoreCase("GET")){
            String response = "Method not allowed";
            exchange.sendResponseHeaders(405, response.getBytes().length);
            try(OutputStream outputStream = exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String portString = path.substring(path.lastIndexOf('/'+1));
        try{
            int port = Integer.parseInt(portString);
            //reading the file
            try(Socket socket = new Socket("localhost", port)){
                InputStream inputStream = socket.getInputStream();
                File tempFile = File.createTempFile("download-", ".tmp");
                String fileName = "downloaded-file";
                try(FileOutputStream fileOutputStream = new FileOutputStream(tempFile)){

                    byte[] buffer = new byte[4096];
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

                    int byteNormal = inputStream.read();
                    while (byteNormal !=-1){
                        if(byteNormal =='\n') break;
                        byteArrayOutputStream.write(byteNormal);
                        byteNormal =inputStream.read();
                    }
                    String header = byteArrayOutputStream.toString().trim();
                    if(header.startsWith("Filename: ")){
                        fileName = header.substring("Filename ".length());
                    }

                    int byteRead= inputStream.read();
                    while(byteRead!= -1){
                        fileOutputStream.write(buffer, 0, byteRead);
                        byteRead = inputStream.read();
                    }

                }
                //placing everything into the buffer
                headers.add("Content-Disposition: ", "attachment; filename=\"" + fileName+"\"");
                headers.add("Content-Type", "application/octet-stream");
                exchange.sendResponseHeaders(200, tempFile.length());
                try(OutputStream outputStream = exchange.getResponseBody()){
                    try( FileInputStream fileInputStream = new FileInputStream(tempFile)){
                        byte[] bufferStore = new byte[4096];
                        int bytesRead=fileInputStream.read(bufferStore);
                        while(bytesRead!=-1){
                            outputStream.write(bufferStore, 0, bytesRead);
                            bytesRead=fileInputStream.read(bufferStore);
                        }
                    }
                }
                tempFile.delete();
            }
        } catch (Exception e) {
            System.out.println("Error Downloading the File "+ e.getMessage());
            String response = "Error Downloading the File "+ e.getMessage();
            headers.add("Content-Type", "text/plain");
            exchange.sendResponseHeaders(400, response.getBytes().length);
            try(OutputStream outputStream = exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }
            e.printStackTrace();

        }
    }
}
