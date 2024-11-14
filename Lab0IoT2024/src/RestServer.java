
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RestServer extends Thread {


    boolean runServer = true;
    ServerSocket ss;

    String sensorValue = "0";

    RestServer() {
        try{
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

                byte[] buffer = new byte[2048];
                is.read(buffer);

                String requestMessage = new String(buffer).trim();
                System.out.println(requestMessage);

                String[] split = requestMessage.split("\r\n");
                String requestLine = split[0];
                System.out.println(requestLine);

                String[] requestLineSplit = requestLine.split(" ");
                String method = requestLineSplit[0];
                String path = requestLineSplit[1];


                String response = "";
                if (method.equalsIgnoreCase("GET")) {
                    System.out.println("GET request path:" + path);

                    if (path.equalsIgnoreCase("/time")) {
                        String time = "" + System.currentTimeMillis();
                        response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/html\r\n" +
                                "\r\n" +
                                "Current time: " + time;
                    } else if (path.equalsIgnoreCase("/sensor")) {
                        String time = "" + System.currentTimeMillis();
                        response = "HTTP/1.1 200 OK\r\n" +
                                "Content-Type: text/html\r\n" +
                                "\r\n" +
                                "Sensor Value: " + sensorValue;
                    } else {
                        response = "HTTP/1.1 404 File not found\r\n" +
                                "Content-Type: text/html\r\n" +
                                "\r\n" +
                                "<h1>File not found</h1>";
                    }

                } else if (method.equalsIgnoreCase("POST")) {
                    System.out.println("POST request");

                    String[] split1 = requestMessage.split("\r\n\r\n");
                    String body = "";
                    if(split1.length > 1){
                        body = split1[1];
                    }
                    System.out.println(body);

                    sensorValue = body;

                    response = "HTTP/1.1 200 OK\r\n" +
                            "Content-Type: text/html\r\n" +
                            "\r\n" +
                            "<h1>Update sucessful</h1>";


                } else {
                    System.out.println("Unsupported request");


                }
                os.write(response.getBytes());
                os.flush();
                os.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    public void shutdown() {
        // Implement the shutdown method
        runServer = false;
    }
}
