package common; // Veya shared

import common.models.Ticket;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

public class Response implements Serializable {
    private static final long serialVersionUID = 2L; // Versiyonlama için

    private final String message;
    private final boolean success;
    private final List<Ticket> collectionData; // 'show' gibi komutlar için

    // Constructorlar (Önceki yanıttaki gibi farklı kombinasyonlar)
    public Response(String message) { this(message, true, null); }
    public Response(String message, boolean success) { this(message, success, null); }
    public Response(String message, List<Ticket> collectionData) { this(message, true, collectionData); }

    // Ana constructor
    public Response(String message, boolean success, List<Ticket> collectionData) {
        this.message = message;
        this.success = success;
        this.collectionData = collectionData;
    }

    // Getterlar
    public String getMessage() { return message; }
    public boolean isSuccess() { return success; }
    public List<Ticket> getCollectionData() { return collectionData; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (message != null && !message.isEmpty()) {
            sb.append(message);
        }
        // Koleksiyon verisini daha yapılandırılmış yazdırma (ShowCommand komutu için)
        if (collectionData != null && !collectionData.isEmpty()) {
            if (!sb.isEmpty()) sb.append("\n---\n"); // Ayırıcı
            sb.append(collectionData.stream()
                    .map(Ticket::toString) // Veya daha özel bir format
                    .collect(Collectors.joining("\n")));
        } else if (collectionData != null && collectionData.isEmpty()) {
            if (!sb.isEmpty()) sb.append("\n---\n");
            sb.append("Koleksiyon boş."); // Boş koleksiyon mesajı
        }
        return sb.toString();
    }
}