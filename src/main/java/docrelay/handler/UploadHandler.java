package docrelay.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import docrelay.controller.FileController;
import docrelay.utility.MultiParser;
import docrelay.utility.ParseResult;
import org.apache.commons.io.IOUtils;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;


public class UploadHandler implements HttpHandler {
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
        headers.add("Access-Control-Allow-Origin", "*");

        System.out.println("file uploading.... begins");

        if(!exchange.getRequestMethod().equalsIgnoreCase("POST")){
            String response= "Method not allowed";
            exchange.sendResponseHeaders(405, response.getBytes().length);
            try(OutputStream outputStream =exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }
            return;
        }

        Headers requestHeaders = exchange.getRequestHeaders();
        String contentType = requestHeaders.getFirst("Content-Type");

        if(contentType == null || !contentType.startsWith("multipart/form-data")){
            String response ="Bad Request: Content-Type must be multipart/form-data";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            try(OutputStream outputStream =exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }
            return;
        }

        try{
            // if both the above conditions are satisfied and errors are not flagged
            // That means the content is correct, and we need to parse the content.
            //now Lets parse.
            String boundary = contentType.substring(contentType.indexOf("boundary=")+ 9);
            System.out.println("U-1  boundary = " + boundary);
            ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
            IOUtils.copy(exchange.getRequestBody(), arrayOutputStream);
            byte[] requestData = arrayOutputStream.toByteArray();
            System.out.println("U-2  request bytes = " + requestData.length);

            MultiParser multiparser = new MultiParser(requestData, boundary);
            ParseResult result = multiparser.parseResult();
            System.out.println(result+" ----- result");

            if(result==null){
                String response = "Bad Request: Could not parse file content";
                exchange.sendResponseHeaders(400, response.getBytes().length);
                try(OutputStream outputStream = exchange.getResponseBody()){
                    outputStream.write(response.getBytes());
                }
                return;
            }



            //if result is not null
            String fileName = result.fileName;

            if(fileName==null|| fileName.trim().isEmpty()){
                fileName="unnamed-file";
            }

            System.out.println("U-3  filename = " + result.fileName
                    + "  bytes = " + result.fileContent.length);


            String uniqueFileName = UUID.randomUUID().toString() +"_" + new File(fileName).getName();

            File targetFile = new File(FileController.uploadDirectory, uniqueFileName);

            System.out.println("uniqueFileName → " + uniqueFileName);
            System.out.println("filePath      → " + targetFile.getAbsolutePath());

            try(FileOutputStream fileOutputStream = new FileOutputStream(targetFile)){
                fileOutputStream.write(result.fileContent);
                System.out.println("File output Stream--->" + fileOutputStream.toString());
            }

            int port = FileController.fileSharer.offerFile(targetFile.getAbsolutePath());

            System.out.println("port details--> "+port);
            new Thread(()-> FileController.fileSharer.startFileServer(port)).start();

            String jsonResponse = "{\"port\":"+port + "}";
            headers.add("Content-Type","application/json");
            byte[] body = jsonResponse.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);

            try(OutputStream outputStream = exchange.getResponseBody()) {
                outputStream.write(jsonResponse.getBytes());
            }

        } catch (Exception e) {
            System.out.println("Error Processing File Upload");
            String response = "Server Error: "+ e.getMessage();
            exchange.sendResponseHeaders(500, response.getBytes().length);
            try(OutputStream outputStream = exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }
            e.printStackTrace();
        }



    }
}
