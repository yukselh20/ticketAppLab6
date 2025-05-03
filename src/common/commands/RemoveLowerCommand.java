package common.commands;

import common.Command;
import common.Response;
import common.models.Ticket;
import server.managers.CollectionManager;

public class RemoveLowerCommand implements Command {
    private static final long serialVersionUID = 19L;
    private final Ticket referenceTicket; // Karşılaştırma için referans nesne

    public RemoveLowerCommand(Ticket referenceTicket) {
        this.referenceTicket = referenceTicket;
    }

    @Override
    public String getName(){
        return "remove_lower";
    }

    @Override
    public Response execute(CollectionManager manager) {
        try {
            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }
            if (referenceTicket == null) {
                return new Response("Error: Reference object not provided for comparison.", false);
            }
            int initialSize = manager.getCollection().size();
            // Fiyata göre karşılaştırma (Lab 5'teki gibi)
            manager.getCollection().values().removeIf(t -> t != null && t.getPrice() < referenceTicket.getPrice());
            int removedCount = initialSize - manager.getCollection().size();
            return new Response(removedCount + " deleted elements (those lower than the reference price).");
        } catch (Exception e) {
            return new Response("Elemanlar silinirken hata: " + e.getMessage(), false);
        }
    }


    public Ticket getReferenceTicket() { return referenceTicket; } // Getter

    @Override
    public String toString() {
        // toString'de tüm ticket bilgisini yazdırmak yerine özet bilgi daha iyi olabilir
        return getName() + " {refPrice=" + referenceTicket.getPrice() + "}";
    }
}




//    @Override
//    public boolean apply(String[] arguments) {
//        try {
//            if(!arguments[1].isEmpty()){
//                throw new WrongAmountOfElementsException();
//            }
//            console.println("Enter data for comparison Ticket.");
//            TicketForm form = new TicketForm(console);
//            Ticket reference = form.build();
//            int initialSize = collectionManager.getCollection().size();
//            // Örneğin, karşılaştırmayı price üzerinden yapalım:
//            collectionManager.getCollection().values().removeIf(t -> t.getPrice() < reference.getPrice());
//            int removed = initialSize - collectionManager.getCollection().size();
//            console.println("Items removed: " + removed);
//            return true;
//        } catch(WrongAmountOfElementsException e) {
//            console.printError("Wrong number of arguments!");
//        } catch(InvalidFormException e) {
//            console.printError("Incorrect data for comparison!");
//        } catch(IncorrectInputInScriptException e) {
//            // Script modunda hata
//        }
//        return false;
//    }
//}