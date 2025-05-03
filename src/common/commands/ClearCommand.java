package common.commands;

import common.Response;
import server.managers.CollectionManager;
import common.Command;

//Lab 5 Hali: Console ve CollectionManager alıyordu, apply metodu doğrudan koleksiyonu temizleyip konsola mesaj yazıyordu.
//Lab 6 Değişiklikleri:
//Console bağımlılığı kaldırıldı (mesaj Response ile dönecek).
//CollectionManager bağımlılığı constructor'dan kaldırıldı, execute metoduna parametre olarak gelecek.
//apply metodu execute ile değiştirildi ve Response döndürecek.
//Neden? Komut nesnesi artık sadece komutun ne olduğunu ve (varsa) verisini taşır. İşlem (execute) sunucuda yapılır. Sonuç (Response) istemciye iletilir.

public class ClearCommand implements Command {
    private static final long serialVersionUID = 10L;

    // Constructor'dan bağımlılıklar kaldırıldı. Argüman almadığı için boş olabilir.
    public ClearCommand() {
        // Eskiden super("clear", "...") çağrısı vardı, şimdi getName() ile sağlanacak
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public Response execute(CollectionManager manager){
        //işlem mantiği execute metodunda
        try{
            manager.clearCollection();
            //consol yerine responsa dödürülüyor.
            return new Response("Collection successfully cleared.");

        } catch (Exception e) {
            return new Response("There is an error occured when clearing the collection:" + e.getMessage(), false);
            // Beklenmedik bir durum olursa yakala (örn. ConcurrentModificationException gibi?)
            // logger.error("Koleksiyon temizlenirken hata:", e); // Loglama eklenecek

        }
    }

    @Override
    public String toString() {
        return getName(); // Debugging için basit toString
    }

//    private final Console console;
//    private final CollectionManager collectionManager;
//
//    public ClearCommand(Console console, CollectionManager collectionManager) {
//        super("clear", "clean up the collection");
//        this.console = console;
//        this.collectionManager = collectionManager;
//    }
//
//    @Override
//    public boolean apply(String[] arguments) {
//        if(!arguments[1].isEmpty()){
//            console.println("Usage: " + getName());
//            return false;
//        }
//        collectionManager.clearCollection();
//        console.println("The collection has been cleared!");
//        return true;
//    }
}