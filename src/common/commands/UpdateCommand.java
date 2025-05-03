package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;
import common.models.Ticket;

public class UpdateCommand implements Command {
    private static final long serialVersionUID = 22L;
    private final int id;
    private final Ticket newTicketData; // Yeni verileri içeren Ticket


    public UpdateCommand(int id, Ticket newTicketData) {
        this.id = id;
        this.newTicketData = newTicketData;
    }

    @Override
    public String getName() {
        return "update";
    }

    @Override
    public Response execute(CollectionManager manager) {
        try {
            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }

            if (newTicketData == null) {
                return new Response("Error: Reference object not provided for update.", false);
            }
            Ticket existingTicket = manager.getById(id);
            if (existingTicket == null) {
                return new Response("Bu ID'ye sahip Ticket bulunamadı: " + id, false);
            }
            // Var olan nesneyi yeni verilerle güncelle
            existingTicket.update(newTicketData);
            // Belki güncellenen nesneyi tekrar yerine koymak gerekir mi?
            // Eğer getById kopyasını döndürmüyorsa gerekmez. Emin olmak için
            // manager.getCollection().put(existingTicket.getKey(), existingTicket); // Eğer key varsa ve biliniyorsa
            // Veya CollectionManager'da bir update metodu olmalı.
            // Şimdilik direkt nesnenin güncellendiğini varsayalım.

            return new Response("ID, Successfully updated with new id: " + id);
        } catch (Exception e) {
            return new Response("Mistake while updating collection: " + e.getMessage(), false);
        }
    }

    // Getterlar
    public int getId() { return id; }
    public Ticket getNewTicketData() { return newTicketData; }

    @Override
    public String toString() {
        return getName() + " [ID: " + id + "]"; // toString'de tüm veriyi göstermeye gerek yok
    }
}
//    public Update(Console console, CollectionManager collectionManager) {
//        super("update", "update <id> {element} : update the value of the collection item whose id is equal to the given one");
//        this.console = console;
//        this.collectionManager = collectionManager;
//    }
//
//    @Override
//    public boolean apply(String[] arguments) {
//        try {
//            if(arguments[1].isEmpty()){
//                throw new WrongAmountOfElementsException();
//            }
//            String idStr = arguments[1].split(" ")[0];
//            int id = Integer.parseInt(idStr);
//            Ticket existing = collectionManager.getById(id);
//            if(existing == null){
//                console.printError("Ticket with this id was not found.");
//                return false;
//            }
//            console.println("Update Ticket, id=." + id);
//            TicketForm form = new TicketForm(console);
//            Ticket newTicket = form.build();
//            existing.update(newTicket);
//            console.println("Ticket successfully updated.");
//            return true;
//        } catch(WrongAmountOfElementsException e) {
//            console.printError("Wrong number of arguments!");
//        } catch(NumberFormatException e) {
//            console.printError("ID must be a number!");
//        } catch(InvalidFormException e) {
//            console.printError("Ticket has not been updated: " + e.getMessage());
//        } catch(IncorrectInputInScriptException e) {
//            // Script modunda
//        }
//        return false;
//    }
//}
