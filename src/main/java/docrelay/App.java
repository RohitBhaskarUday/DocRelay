package docrelay;

import docrelay.controller.FileController;

import javax.xml.transform.Source;
import java.io.IOException;

public class App {
    public static void main(String[] args) {
        // we start the server here.
        try{
            FileController fileController = new FileController(9000);
            fileController.start();
            System.out.println("DocRelay server started on port 9000");
            System.out.println("UI available at http://locahost:3000");
            Runtime.getRuntime().addShutdownHook(
                    new Thread(()->{
                        System.out.println("Shutting down the server safely");
                        fileController.stop();
                    })
            );

            System.out.println("Press enter to stop the server");
            System.in.read(); // todo: When someone hits enter stop the server.



        }catch (Exception e){
            System.err.println("Failed to start the server port 9000");
            e.printStackTrace();
        }




    }

}
