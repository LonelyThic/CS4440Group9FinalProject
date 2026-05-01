import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    private static final int PORT = 6000;

    private static List<String> mailbox =
            Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new ClientHandler(clientSocket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {

        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {
                BufferedReader input = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                PrintWriter output = new PrintWriter(
                        socket.getOutputStream(), true);

                String message;

                while ((message = input.readLine()) != null) {

                    if (message.startsWith("TEXT:")) {

                        String encrypted = message.substring(5);

                        System.out.println("Encrypted received: " + encrypted);

                        String decrypted = EncryptionUtil.decrypt(encrypted);

                        System.out.println("Decrypted message: " + decrypted);

                        mailbox.add(decrypted);
                        output.println("Message stored securely.");

                    } else if (message.startsWith("IMG:")) {

                        try {
                            String encryptedImage = message.substring(4);

                            // Show partial encrypted image (avoid huge output)
                            System.out.println("Encrypted image received: " +
                                encryptedImage.substring(0, Math.min(60, encryptedImage.length())) + "...");

                            byte[] imageBytes = EncryptionUtil.decryptImage(encryptedImage);

                            if (imageBytes == null) {
                                output.println("Error receiving image.");
                                continue;
                            }

                            System.out.println("Image decrypted successfully.");
                            System.out.println("Image size: " + imageBytes.length + " bytes");

                            String fileName = "received_" +
                                    System.currentTimeMillis() + ".jpg";

                            try (FileOutputStream fos = new FileOutputStream(fileName)) {
                                fos.write(imageBytes);
                            }

                            System.out.println("Image saved as: " + fileName);

                            mailbox.add("[IMAGE RECEIVED: " + fileName + "]");
                            output.println("Image received and stored.");

                        } catch (Exception e) {
                            output.println("Error receiving image.");
                        }

                    } else {
                        output.println("Unknown message type.");
                    }
                }

            } catch (IOException e) {
                System.out.println("Client disconnected.");
            }
        }
    }
}