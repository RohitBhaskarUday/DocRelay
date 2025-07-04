package docrelay;

import docrelay.controller.FileController;

import javax.xml.transform.Source;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class App {
    public static void main(String[] args) {
        // we start the server here.
        try{
            FileController fileController = new FileController(9000);
            fileController.start();
            System.out.println("DocRelay server started on port 9000");
            System.out.println("UI available at http://localhost:3000");




            /*  shutdown hook ─ runs on Ctrl-C, kill, or System.exit  */
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down the server safely");
                fileController.stop();            // stop HTTP + thread-pool
            }));
            System.out.println("Press <Enter> to stop the server");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

            /*  Trigger the hook by exiting the JVM  */
            System.exit(0);

        }catch (Exception e){
            System.err.println("Failed to start the server port 9000");
            e.printStackTrace();
        }




    }

}
