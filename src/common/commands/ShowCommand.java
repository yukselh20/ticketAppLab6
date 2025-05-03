package common.commands;

import common.Command;
import common.Response;
import common.models.Ticket;
import server.managers.CollectionManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * show : Koleksiyondaki tüm Ticket’ları gösterir.
 */
public class ShowCommand implements Command {
    private static final long serialVersionUID = 21L;

    // Argüman almaz

    @Override
    public String getName() {
        return "show";
    }

    @Override
    public Response execute(CollectionManager manager) {
        try {
            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }
            // Koleksiyonu al ve isme göre sırala (Lab 6 gereksinimi)
            List<Ticket> sortedList = new ArrayList<>(manager.getCollection().values()) // Önce kopyasını al
                    .stream()
                    .sorted(Comparator.comparing(Ticket::getName)) // İsme göre sırala
                    .collect(Collectors.toList());

            if (sortedList.isEmpty()) {
                return new Response("The collection is empty."); // Mesajı Response içinde gönder
            } else {
                // Sıralanmış liste Response ile döndürülüyor
                return new Response("Collection elements:", sortedList);
            }
        } catch (Exception e) {
            return new Response("Error showing a collection: " + e.getMessage(), false);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}


//    public ShowCommand(Console console, CollectionManager collectionManager) {
//        super("show", "display all items in the collection");
//        this.console = console;
//        this.collectionManager = collectionManager;
//    }
//
//    @Override
//    public boolean apply(String[] arguments) {
//        if(!arguments[1].isEmpty()){
//            console.println("Utilization: " + getName());
//            return false;
//        }
//        console.println(collectionManager.getAllTickets());
//        return true;
//    }

