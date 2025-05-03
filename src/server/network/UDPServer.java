package server.network;

import common.Command;
import common.Response;
import common.exceptions.SerializationException;
import common.utility.SerializationUtils;
import server.managers.CollectionManager; // CollectionManager'ı kullanacak

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.Set;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Handles UDP network communication for the server using non-blocking I/O and Selector.
 */
public class UDPServer {

    // private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    private static final int BUFFER_SIZE = 8192; // Gelen/giden veriler için buffer boyutu

    private final int port;
    private final CollectionManager collectionManager; // Komutları çalıştırmak için
    private DatagramChannel channel;
    private Selector selector;
    private boolean running = true; // Sunucu döngüsünü kontrol etmek için flag

    public UDPServer(int port, CollectionManager collectionManager) {
        this.port = port;
        this.collectionManager = collectionManager;
    }

    /**
     * Starts the server: opens channel, binds port, initializes selector, and runs the main loop.
     */
    public void run() {
        try {
            // 1. Selector ve Kanalı Başlat
            initializeServer();
            // logger.info("UDP Server started successfully on port {}", port);
            System.out.println("UDP Server started successfully on port " + port);


            // 2. Ana İşleme Döngüsü
            while (running) { // 'running' flag'i ile kontrol edilebilir
                try {
                    // 2a. Olayları Bekle (Selector ile)
                    // select() metodu, en az bir kanal hazır olana kadar veya timeout'a kadar bloke olur.
                    // Timeout eklemezsek (veya 0 verirsek), sonsuza kadar bekleyebilir.
                    // Timeout eklemek (örn. 1000ms), sunucunun başka işler yapmasına olanak tanır (örn. konsol komutları).
                    int readyCount = selector.select(1000); // 1 saniye timeout

                    if (readyCount == 0) {
                        // Timeout oldu veya başka bir nedenle uyandırıldı, önemli bir olay yok.
                        // Burada başka periyodik işler yapılabilir (gerekirse).
                        continue;
                    }

                    // 2b. Hazır Anahtarları İşle
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        // Anahtarı işledikten sonra listeden çıkarmak ÇOK ÖNEMLİ!
                        keyIterator.remove();

                        if (!key.isValid()) {
                            continue; // Anahtar geçersizse atla
                        }

                        // 2c. Sadece Okuma Olaylarını İşle (UDP için genellikle bu yeterli)
                        if (key.isReadable()) {
                            handleIncomingData(key);
                        }
                        // Yazma olayları (isWritable) genellikle buffer dolduğunda
                        // tekrar denemek için kullanılır, şimdilik basit tutalım.
                    }
                } catch (IOException e) {
                    // logger.error("Error during selector operation:", e);
                    System.err.println("ERROR: Network I/O error in selector loop: " + e.getMessage());
                    // Döngüye devam etmeyi deneyebilir veya ciddi hataysa durabiliriz
                } catch (CancelledKeyException e) {
                    // logger.warn("SelectionKey was cancelled.");
                    System.err.println("WARN: SelectionKey was cancelled.");
                    // Anahtar zaten iptal edilmiş, görmezden gel
                }
            } // while(running) sonu

        } catch (IOException e) {
            // logger.error("Failed to initialize server:", e);
            System.err.println("FATAL: Server initialization failed: " + e.getMessage());
        } finally {
            // 3. Sunucu Kapanışı
            closeServer();
        }
    }

    /**
     * Initializes the DatagramChannel and Selector.
     * @throws IOException If an I/O error occurs.
     */
    private void initializeServer() throws IOException {
        selector = Selector.open();
        channel = DatagramChannel.open();
        channel.configureBlocking(false); // Non-blocking moda ayarla
        channel.bind(new InetSocketAddress(port));
        channel.register(selector, SelectionKey.OP_READ); // Sadece okuma olaylarını dinle
        // logger.info("Selector and DatagramChannel initialized.");
        System.out.println("Selector and DatagramChannel initialized.");

    }

    /**
     * Handles incoming data on a readable channel.
     * Reads the datagram, deserializes the command, executes it,
     * serializes the response, and prepares to send it back.
     * @param key The SelectionKey associated with the readable channel.
     */
    private void handleIncomingData(SelectionKey key) {
        DatagramChannel currentChannel = (DatagramChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE); // Her istek için yeni buffer
        SocketAddress clientAddress = null;

        try {
            // 1. Veriyi Oku (Non-blocking)
            buffer.clear(); // Buffer'ı yeniden kullanıma hazırla
            clientAddress = currentChannel.receive(buffer);

            if (clientAddress == null) {
                // logger.warn("Received null address, possibly spurious wakeup?");
                System.err.println("WARN: Received null address from channel.");

                return; // Gelen geçerli bir datagram yok
            }
            // logger.debug("Received {} bytes from {}", buffer.position(), clientAddress);
            System.out.println("DEBUG: Received " + buffer.position() + " bytes from " + clientAddress);


            buffer.flip(); // Okuma moduna geç
            if (buffer.remaining() == 0) {
                // logger.warn("Received empty datagram from {}", clientAddress);
                System.err.println("WARN: Received empty datagram from " + clientAddress);

                return; // Boş paket
            }
            byte[] receivedData = new byte[buffer.remaining()];
            buffer.get(receivedData);

            // 2. Komutu Deserileştir
            Command command;
            try {
                Object receivedObject = SerializationUtils.deserialize(receivedData);
                if (receivedObject instanceof Command) {
                    command = (Command) receivedObject;
                    // logger.info("Deserialized command '{}' from {}", command.getName(), clientAddress);
                    System.out.println("INFO: Received command '" + command.getName() + "' from " + clientAddress);

                } else {
                    // logger.error("Received non-Command object from {}: {}", clientAddress, receivedObject.getClass().getName());
                    sendResponse(new Response("Error: Invalid object type received.", false), clientAddress);
                    return;
                }
            } catch (SerializationException e) {
                // logger.error("Failed to deserialize command from {}: {}", clientAddress, e.getMessage());
                sendResponse(new Response("Error: Could not deserialize command. " + e.getMessage(), false), clientAddress);
                return;
            }

            // 3. Komutu Çalıştır
            Response response = command.execute(collectionManager);
            // logger.info("Executed command '{}' for {}. Success: {}", command.getName(), clientAddress, response.isSuccess());
            System.out.println("INFO: Executed command '" + command.getName() + "' for " + clientAddress + ". Success: " + response.isSuccess());


            // 4. Yanıtı Gönder
            sendResponse(response, clientAddress);

        } catch (IOException e) {
            // logger.error("Error handling incoming data from {}: {}", clientAddress != null ? clientAddress : "unknown", e.getMessage());
            System.err.println("ERROR: I/O error handling data from " + (clientAddress != null ? clientAddress : "unknown") + ": " + e.getMessage());
            // İstemciye hata yanıtı göndermeyi deneyebiliriz ama kanal kapalı olabilir
            if (clientAddress != null) {
                sendResponse(new Response("Internal server error during request processing.", false), clientAddress);
            }
        }
    }

    /**
     * Serializes the Response object and sends it back to the client via UDP.
     * @param response The Response object to send.
     * @param clientAddress The address of the client to send the response to.
     */
    private void sendResponse(Response response, SocketAddress clientAddress) {
        try {
            byte[] responseBytes = SerializationUtils.serialize(response);
            ByteBuffer sendBuffer = ByteBuffer.wrap(responseBytes);
            // logger.debug("Sending response ({} bytes) to {}: {}", responseBytes.length, clientAddress, response.getMessage());
            System.out.println("DEBUG: Sending response ("+responseBytes.length+" bytes) to " + clientAddress);

            int sentBytes = channel.send(sendBuffer, clientAddress);
            if (sentBytes == 0) {
                // logger.warn("Could not send response immediately to {}. Client might not receive it.", clientAddress);
                System.err.println("WARN: Response could not be sent immediately to " + clientAddress);
            }
            // logger.trace("Sent {} bytes of response to {}", sentBytes, clientAddress);

        } catch (SerializationException e) {
            // logger.error("Failed to serialize response for {}: {}", clientAddress, response, e);
            System.err.println("ERROR: Failed to serialize response for " + clientAddress + ": " + e.getMessage());

        } catch (IOException e) {
            // logger.error("I/O error sending response to {}: {}", clientAddress, e.getMessage());
            System.err.println("ERROR: I/O error sending response to " + clientAddress + ": " + e.getMessage());

        }
    }

    /**
     * Stops the server loop and closes network resources.
     */
    public void stopServer() {
        // logger.info("Stopping server...");
        System.out.println("Stopping server...");

        this.running = false;
        // Selector'ı uyandırarak select() metodundan çıkmasını sağla
        if (selector != null) {
            selector.wakeup();
        }
    }

    /**
     * Closes the selector and channel.
     */
    private void closeServer() {
        // logger.info("Closing server network resources...");
        System.out.println("Closing server network resources...");

        try {
            if (selector != null && selector.isOpen()) {
                selector.close();
                // logger.info("Selector closed.");
                System.out.println("Selector closed.");

            }
            if (channel != null && channel.isOpen()) {
                channel.close();
                // logger.info("Server DatagramChannel closed.");
                System.out.println("Server DatagramChannel closed.");

            }
        } catch (IOException e) {
            // logger.error("Error closing server resources:", e);
            System.err.println("ERROR: Failed to close server network resources: " + e.getMessage());

        }
    }
}