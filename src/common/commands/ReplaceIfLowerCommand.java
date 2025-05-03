package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;
import common.models.Ticket;

public class ReplaceIfLowerCommand implements Command {
    private static final long serialVersionUID = 20L;
    private final Long key;
    private final Ticket newTicket; // Yeni Ticket nesnesi

    public ReplaceIfLowerCommand(Long key, Ticket newTicket) {
        this.key = key;
        this.newTicket = newTicket;
    }

    @Override
    public String getName() {
        return "replace_if_lower";
    }

    @Override
    public Response execute(CollectionManager manager) {
        try {
            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }
            if (newTicket == null) {
                return new Response("Error: Reference object not provided for replace.", false);
            }

            Ticket existingTicket = manager.getCollection().get(key);
            if (existingTicket == null) {
                return new Response("There is no element with that key: " + key, false);
            }
            // Fiyata göre karşılaştırma
            if (newTicket.getPrice() < existingTicket.getPrice()) {
                // ID ve creationDate korunmalı, bu yüzden update metodu kullanılmalı
                existingTicket.update(newTicket);
                // Var olanı güncelle
                // VEYA manager.getCollection().put(key, newTicket); // Yenisiyle tamamen değiştir (ID değişir!)
                // Lab 5'teki davranışa göre karar ver. Muhtemelen ID korunmalı.
                // ID'yi korumak için CollectionManager'da belki bir replace metodu lazım?
                // Şimdilik var olanı güncellediğimizi varsayalım:
                // manager.replace(key, newTicket); // CollectionManager'a böyle bir metot eklenebilir
                manager.getCollection().put(key, existingTicket); // Güncellenmiş existingTicket'ı geri koyalım (update sonrası)

                return new Response("Previous key, replace with this key:" + key + "(the new price was lower)");

            } else {
                return new Response("New value was not lower, there is no replacing");
            }
        } catch (Exception e) {
            // logger.error("ReplaceIfLower işlemi sırasında hata:", e);
            return new Response("Error when replacing an element:" + e.getMessage(),false);
        }
    }

    // Getterlar
    public Long getKey() { return key; }
    public Ticket getNewTicket() { return newTicket; }

    @Override
    public String toString() {
        return getName() + " [Key: " + key + ", NewTicketPrice: " + newTicket.getPrice() + "]";
    }

}




//    @Override
//    public boolean apply(String[] arguments) {
//        try {
//            if(arguments[1].isEmpty()){
//                throw new WrongAmountOfElementsException();
//            }
//            Long key = Long.parseLong(arguments[1].split(" ")[0]);
//            Ticket existing = collectionManager.getCollection().get(key);
//            if(existing == null){
//                console.printError("Element with this key was not found.");
//                return false;
//            }
//            console.println("Enter the data for the new Ticket:");
//            TicketForm form = new TicketForm(console);
//            Ticket newTicket = form.build();
//            if(newTicket.getPrice() < existing.getPrice()){
//                collectionManager.getCollection().put(key, newTicket);
//                console.println("The element has been successfully replaced.");
//            } else {
//                console.println("The new value is not less than the old value. The substitution has not been performed.");
//            }
//            return true;
//        } catch(WrongAmountOfElementsException e) {
//            console.printError("Wrong number of arguments!");
//        } catch(NumberFormatException e) {
//            console.printError("The key has to be a number!");
//        } catch(InvalidFormException e) {
//            console.printError("Incorrect data: " + e.getMessage());
//        } catch(IncorrectInputInScriptException e) {
//            // Script modunda
//        }
//        return false;
//    }
//}