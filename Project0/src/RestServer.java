package Project0.src;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RestServer extends Thread {
    
    boolean runServer = true;
    ServerSocket ss;

    RestServer() {
        try {
            ss = new ServerSocket(80);
        } catch (Exception e) { 
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while(runServer) {
                Socket s = ss.accept();

                InputStream is = s.getInputStream();
                OutputStream os = s.getOutputStream();

                byte[] buffer = new byte[1024];
                is.read();

                String requestMEssage = new String(buffer).trim();
                System.out.println("Request: " + requestMEssage);

                String[] split = requestMEssage.split("\r\n");
                String requestLine = split[0];
                System.out.println("RequestLine: " + requestLine);

                String[] requestLineSplit = requestLine.split(" ");
                String method = requestLineSplit[0];
                String path = requestLineSplit[1];

                String response = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: text/html\r\n" +
                    "\r\n" +
                    "<h1>Hello World</h1>";

                System.out.println(method + " " + path);
                os.write(response.getBytes());
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        
        // while(runServer) {
          
        // }
         

        System.out.println("Server is running");
    }

    public void shutdown() {
        runServer = false;
        System.out.println("Shutting down the server");
    }
}
