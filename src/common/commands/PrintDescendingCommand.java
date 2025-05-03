package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;
import common.models.Ticket;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;


public class PrintDescendingCommand implements Command {
    private static final long serialVersionUID = 17L;

    // Argüman almaz fiyata göre sıralar.

    @Override
    public String getName() {
        return "print_descending";
    }

    @Override
    public Response execute(CollectionManager manager) {
        try {
            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }
            // Sıralama sunucuda yapılıyor
            List<Ticket> sortedList = manager.getCollection().values().stream()
                    .sorted(Comparator.comparing(Ticket::getPrice).reversed()) // Fiyata göre ters sıralama
                    .collect(Collectors.toList());

            if (sortedList.isEmpty()) {
                return new Response("Collection is empty");
            } else {
                // Sıralı liste Response ile döndürülüyor
                return new Response("Prices in descending order:", sortedList);
            }
        } catch (Exception e) {
            // logger.error("Error when sorting prices:", e);
            return new Response("Error when sorting prices: " + e.getMessage(), false);
        }
    }

    @Override
    public String toString() {
        return getName();
    }
}
//    public PrintDescendingCommand(Console console, CollectionManager collectionManager) {
//        super("print_descending", "output the elements of the collection in descending order");
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
//        List<Ticket> sorted = collectionManager.getCollection().values().stream()
//                .sorted(Comparator.comparing(Ticket::getPrice).reversed())
//                .toList();
//        sorted.forEach(console::println);
//        return true;
//    }
//}
