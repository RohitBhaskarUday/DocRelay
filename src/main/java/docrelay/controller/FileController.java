package docrelay.controller;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import docrelay.handler.DownloadHandler;
import docrelay.handler.UploadHandler;
import docrelay.handler.FileSharer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileController {

    //Implement CORS handler.
    public static FileSharer fileSharer;
    public static HttpServer server;
    public static String uploadDirectory;
    public static ExecutorService executorService;

    public FileController() throws IOException{

    }


    public FileController(int port) throws IOException{
        this.fileSharer = new FileSharer();
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.uploadDirectory = System.getProperty("java.io.tmpdir")+ File.separator + "docrelay-uploads";
        this.executorService = Executors.newFixedThreadPool(10);

        File uploadDirectoryFile = new File(uploadDirectory);
        if(!uploadDirectoryFile.exists()){
            uploadDirectoryFile.mkdirs();
        }

        server.createContext("/upload", new UploadHandler());
        server.createContext("/download", new DownloadHandler());
        server.createContext("/", new CORSHandler()); //CORS = cross-origin resource sharing
        server.setExecutor(executorService);


    }

    public void start(){
        server.start();
        System.out.println("API Server started on port "+ server.getAddress().getPort());
    }

    public void stop(){
        server.stop(0);
        executorService.shutdownNow();
        System.out.println("API Server stopped");
    }

    private class CORSHandler implements HttpHandler{

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
            Headers headers = exchange.getRequestHeaders();
            headers.add("Access-Control-Allow-Origin","*");
            headers.add("Access-Control-Allow-Methods","GET, POST, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

            if(exchange.getRequestHeaders().equals("OPTIONS")){
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            String response = "NOT FOUND";
            exchange.sendResponseHeaders(404, response.getBytes().length);
            try(OutputStream outputStream = exchange.getResponseBody()){
                outputStream.write(response.getBytes());
            }

        }
    }



}
