import java.io.*;
import java.net.*;
import java.util.*;

public class Client {

    // Server address and port
    private static final String SERVER_IP = "127.0.0.1";
    private static final int PORT = 6000;

    public static void main(String[] args) {

        try {
            Socket socket = new Socket(SERVER_IP, PORT);

            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

            Scanner scanner = new Scanner(System.in);

            /*
             * Thread for receiving messages from server
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
             * Thread for sending messages to server
             */
            Thread sendThread = new Thread(() -> {
                while (true) {
                    String message = scanner.nextLine();

                    try {
                        if (isImageFile(message)) {
                            File file = new File(message);

                            if (!file.exists()) {
                                System.out.println("No File Was Found.");
                            } else {
                                try {
                                    byte[] imageBytes = java.nio.file.Files.readAllBytes(file.toPath());
                                    String encryptedImage = EncryptionUtil.encryptImage(imageBytes);

                                    if (encryptedImage != null) {
                                        output.println("IMG:" + encryptedImage);
                                    } else {
                                        System.out.println("Image Failed To Send.");
                                    }

                                } catch (Exception e) {
                                    System.out.println("Image Failed To Send.");
                                }
                            }

                        } else {
                            String encrypted = EncryptionUtil.encrypt(message);

                            if (encrypted != null) {
                                
                                System.out.println("");

                                System.out.print("Enter message or image path: ");
                                
                                System.out.println("Original: " + message);
                                System.out.println("Encrypted: " + encrypted);
                                output.println("TEXT:" + encrypted);
                            } else {
                                System.out.println("Message Failed To Send.");
                            }
                        }

                    } catch (Exception e) {
                        System.out.println("Error sending data.");
                    }
                }
            });

            receiveThread.start();
            sendThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Checks whether user input appears to be an image file path
     */
    private static boolean isImageFile(String message) {
        String lower = message.toLowerCase();
        return lower.endsWith(".jpg") ||
               lower.endsWith(".jpeg") ||
               lower.endsWith(".png") ||
               lower.endsWith(".gif") ||
               lower.endsWith(".bmp");
    }
}