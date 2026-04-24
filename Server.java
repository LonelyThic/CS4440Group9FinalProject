import java.io.*;
import java.net.*;
import java.util.*;

public class Server {

    // Port number used by server socket
    private static final int PORT = 5000;

    /*
     * Mailbox data storage
     * Using synchronizedList to ensure thread-safe access
     * when multiple client threads store messages concurrently.
     */
    private static List<String> mailbox = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {

        System.out.println("Server started and listening on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            /*
             * Continuous listening loop.
             * The server never stops accepting new connections.
             * This satisfies the "Continuous Listening" requirement.
             */
            while (true) {

                // Wait for a client connection request
                Socket clientSocket = serverSocket.accept();

                System.out.println("Client connected: "
                        + clientSocket.getInetAddress());

                /*
                 * Create a new thread for each client.
                 * This allows the server to handle multiple clients
                 * simultaneously without blocking.
                 */
                new ClientHandler(clientSocket).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * ClientHandler Thread
     *
     * Each client connection runs inside its own thread.
     * This allows simultaneous sending and receiving
     * without interrupting the main server loop.
     */
    static class ClientHandler extends Thread {

        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {

            try {

                /*
                 * Input stream reads messages from the client
                 */
                BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                /*
                 * Output stream sends responses back to client
                 */
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                String message;

                /*
                 * Loop continuously receiving messages from client.
                 * The connection stays active until the client disconnects.
                 */
                while ((message = input.readLine()) != null) {

                    if (message.startsWith("TEXT:")) {

                        String encrypted = message.substring(5);

                        // Decrypt message
                        System.out.println("Encrypted received: " + encrypted);
                        String decrypted = EncryptionUtil.decrypt(encrypted);
                        System.out.println("Decrypted message: " + decrypted);

                        mailbox.add(decrypted);

                        output.println("Message stored securely.");

                    } else if (message.startsWith("IMG:")) {

                        try {
                            String encrypted = message.substring(4);

                            String decoded = EncryptionUtil.decrypt(encrypted);

                            byte[] imageBytes = Base64.getDecoder().decode(decoded);

                            String fileName = "received_" + System.currentTimeMillis() + ".jpg";

                            FileOutputStream fos = new FileOutputStream(fileName);
                            fos.write(imageBytes);
                            fos.close();

                            mailbox.add("[IMAGE RECEIVED: " + fileName + "]");

                            System.out.println("Image saved as: " + fileName);

                            output.println("Image received and stored.");

                        } catch (Exception e) {
                            output.println("Error receiving image.");
                        }

                    } else {
                        // fallback (shouldn't happen)
                        System.out.println("Unknown message type");
                    }
                }

            } catch (IOException e) {

                System.out.println("Client disconnected.");

            }
        }
    }
}