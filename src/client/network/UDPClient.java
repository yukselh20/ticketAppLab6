package client.network;

import common.Command;
import common.Response;
import common.exceptions.SerializationException;
import common.utility.SerializationUtils; // Yardımcı sınıfımızı import ediyoruz

import java.io.IOException;
import java.net.*; // SocketAddress, InetSocketAddress
import java.nio.ByteBuffer; // ByteBuffer kullanacağız
import java.nio.channels.DatagramChannel; // DatagramChannel kullanacağız
import java.nio.channels.SelectionKey; // Gerekirse Selector için
import java.nio.channels.Selector;    // Gerekirse Selector için
import java.util.Iterator; // Gerekirse Selector için
// import java.net.*;
// import java.nio.channels.DatagramChannel;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Handles UDP network communication for the client.
 * Sends serialized Command objects to the server and receives Response objects.
 * Operates in non-blocking mode with timeouts.
 */
public class UDPClient {

    // private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private static final int BUFFER_SIZE = 8192; // Alınacak yanıt için buffer boyutu (64KB'a kadar olabilir)
    private static final int TIMEOUT_MS = 5000; //5sn yanıt bekleme süresi
    private final String host;
    private final int port;
    private DatagramChannel channel;
    private SocketAddress serverAddress;
    private Selector selector; // Yanıtı beklerken selector kullanmak daha gelişmiş bir yöntem olabilir

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
        // connect(); // Veya bağlantı ilk istekte kurulur
        try {
            // Sunucu adresini oluştur
            this.serverAddress = new InetSocketAddress(InetAddress.getByName(host), port);
            // Datagram Kanalını aç
            this.channel = DatagramChannel.open();
            // Engellemeyen (Non-blocking) moda ayarla - ÇOK ÖNEMLİ!
            this.channel.configureBlocking(false);
            // logger.info("UDP Client channel opened and configured non-blocking for server {}:{}", host, port);
            System.out.println("UDP Client channel opened for " + host + ":" + port);

            // Gelişmiş: Yanıt beklemek için Selector kullanmak isterseniz:
            this.selector = Selector.open();
            this.channel.register(selector, SelectionKey.OP_READ);

        } catch (UnknownHostException e) {
            // logger.error("Server host could not be found: {}", host, e);
            System.err.println("Server host could not be found: " + host);
            // Burada istemciyi durdurmak veya hata durumunu yönetmek gerekebilir
            System.exit(1);
        } catch (IOException e) {
            // logger.error("Failed to open DatagramChannel:", e);
            System.err.println("Failed to open DatagramChannel: " + e.getMessage());
            System.exit(1);
        }
    }


    /**
     * Sends a command object to the server and waits for a response.
     * Handles serialization and deserialization.
     * Includes logic for server unavailability.
     *
     * @param command The command object to send.
     * @return The Response object received from the server, or null if failed.
     */
    public Response sendCommandAndGetResponse(Command command) {
        try {
            // 1. komutu serileştir
            byte[] commandBytes = SerializationUtils.serialize(command);
            ByteBuffer sendBuffer = ByteBuffer.wrap(commandBytes);
            // logger.debug("Sending command {} ({} bytes) to {}", command.getName(), commandBytes.length, serverAddress);
            System.out.println("DEBUG: Sending " + command.getName() + " (" + commandBytes.length + " bytes) to " + serverAddress);


            //2. komut gönderme
            int sentBytes = channel.send(sendBuffer, serverAddress);
            if (sentBytes == 0) {
                // logger.warn("Could not send data immediately (buffer full?). Might need retry logic.");
                System.err.println("WARN: Data could not be sent immediately.");
                // Basitlik için şimdilik hata olarak kabul edelim
                return new Response("Failed to send command to server (network buffer might be full).", false);
            }
            // logger.trace("sent {} bytes.", sentBytes);

            // 3. Yanıtı Bekle (Engellemeyen mod ve timeout ile)
            ByteBuffer receiveBuffer = ByteBuffer.allocate(BUFFER_SIZE);
            long startTime = System.currentTimeMillis();

            while (System.currentTimeMillis() - startTime < TIMEOUT_MS) {
                // Non-blocking receive çağrısı
                SocketAddress fromAddress = channel.receive(receiveBuffer);

                if (fromAddress != null) {
                    // Yanıt geldi!
                    if (fromAddress.equals(serverAddress)) { // Yanıtın doğru sunucudan geldiğini kontrol et
                        // logger.debug("Received response ({} bytes) from {}", receiveBuffer.position(), fromAddress);
                        System.out.println("DEBUG: Received response (" + receiveBuffer.position() + " bytes) from " + fromAddress);


                        receiveBuffer.flip();//Buffer okuma moduna al
                        byte[] responseBytes = new byte[receiveBuffer.remaining()];
                        receiveBuffer.get(responseBytes);

                        try {
                            Object responseObject = SerializationUtils.deserialize(responseBytes);
                            if (responseObject instanceof Response) {
                                // logger.info("Successfully received and deserialized response for command {}", command.getName());
                                return (Response) responseObject;
                            } else {
                                // logger.error("Received unexpected object type: {}", responseObject.getClass().getName());
                                return new Response("Received unexpected data type from server.", false);
                            }
                        } catch (SerializationException e) {
                            // logger.error("Failed to deserialize response:", e);
                            return new Response("Failed to process server response: " + e.getMessage(), false);
                        }
                    } else {
                        // logger.warn("Received packet from unexpected address: {}", fromAddress);
                        System.err.println("WARN: Received packet from unexpected address: " + fromAddress);

                        receiveBuffer.clear(); // Beklenmeyen paketi atla ve dinlemeye devam et
                        // continue; // Döngüye devam et
                    }
                }

                // Yanıt gelmediyse, kısa bir süre bekleyip tekrar dene (CPU'yu %100 kullanmamak için)
                // Daha verimli yöntem Selector kullanmaktır.
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); return new Response("Interrupted while waiting for response.", false); }
            }

            // 5. Timeout
            // logger.warn("No response received from server within {} ms.", TIMEOUT_MS);
            System.err.println("ERROR: No response from server within timeout.");
            return new Response("Server did not respond within the timeout period (" + TIMEOUT_MS + "ms).", false);

        } catch (SerializationException e) {
            // logger.error("Failed to serialize command:", e);
            return new Response("Internal client error (serialization): " + e.getMessage(), false);
        } catch (IOException e) {
            // logger.error("Network I/O error:", e);
            return new Response("Network communication error: " + e.getMessage(), false);
        } catch (Exception e) {
            // logger.error("Unexpected error in send/receive:", e);
            return new Response("Unexpected client error: " + e.getMessage(), false);
        }
    }

    /**
     * Closes the DatagramChannel.
     */
    public void close() {
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                // logger.info("Client UDP channel closed.");
                System.out.println("Client UDP channel closed.");
            }
            if (selector != null && selector.isOpen()) {
                 selector.close();
                //logger.info("Client selector closed.");
            }
        } catch (IOException e) {
            // logger.error("Failed to close client channel/selector:", e);
            System.err.println("ERROR: Failed to close client network resources: " + e.getMessage());
        }
    }
}