import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {

    // Server address and port  
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 5000;

    public static void main(String[] args) {

        try {

            /*
             * Establish connection with server
             */
            Socket socket = new Socket(SERVER_IP, PORT);

            System.out.println("Connected to server.");

            /*
             * Stream for receiving server messages
             */
            BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            /*
             * Stream for sending messages to server
             */
            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            /*
             * Thread for receiving messages from server.
             * This runs continuously so responses are not missed.
             */
            Thread receiveThread = new Thread(() -> {

                try {

                    String response;

                    while ((response = input.readLine()) != null) {

                        System.out.println("Server: " + response);

                    }

                } catch (IOException e) {

                    System.out.println("Server connection closed.");

                }

            });

            /*
             * Thread for sending messages to server.
             * Allows user input to be transmitted without blocking
             * the receiving thread.
             */
            Thread sendThread = new Thread(() -> {

            while (true) {

                String message = scanner.nextLine();

                try {
                    if (message.startsWith("img ")) {

                            String path = message.substring(4);

                            byte[] imageBytes = java.nio.file.Files.readAllBytes(
                                    java.nio.file.Paths.get(path));

                            String base64 = java.util.Base64.getEncoder().encodeToString(imageBytes);

                            String encrypted = EncryptionUtil.encrypt(base64);

                            output.println("IMG:" + encrypted);

                            System.out.println("Image sent.");

                        } else {
                            // text mssage
                            String encrypted = EncryptionUtil.encrypt(message);

                            // shows encryption happening
                            System.out.println("Original: " + message);
                            System.out.println("Encrypted: " + encrypted);

                            output.println("TEXT:" + encrypted);
                        }

                    } catch (Exception e) {
                        System.out.println("Error sending image.");
                    }
                }
            });

            /*
             * Start both threads simultaneously
             */
            receiveThread.start();
            sendThread.start();

        } catch (IOException e) {

            e.printStackTrace();

        }
    }
}