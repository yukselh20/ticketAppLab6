package common.commands;

import common.Command;
import common.Response;
import common.models.Ticket;
import server.managers.CollectionManager;

public class InsertCommand implements Command {
    private static final long serialVersionUID = 11L;
    private final Long key;
    private final Ticket ticket;

    public InsertCommand(Long key, Ticket ticket) {
        this.key = key;
        this.ticket = ticket;
    }


    @Override
    public String getName() {
        return "insert";
    }

    @Override
    public Response execute(CollectionManager manager) {
        // ID ve creationDate gibi alanlar CollectionManager.add içinde atanmalı
        try {
            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }
            // Belki key'in zaten var olup olmadığını kontrol et?
            if (manager.getCollection().containsKey(key)) {
                return new Response("This key already exists: " + key, false);
            }
            manager.addToCollection(key, ticket);
            return new Response("Ticked added. Key: " + key);
        }

        catch (IllegalArgumentException iae) { // Belki add metodu geçersiz veri için bunu atar?
            // logger.warn("Geçersiz Ticket ekleme denemesi: {}", iae.getMessage());
            return new Response("Ticket eklenemedi, geçersiz veri: " + iae.getMessage(), false);
        }

        catch (Exception e) {
            // logger.error("Ticket eklenirken hata:", e);
            return new Response("Error while adding ticket: " + e.getMessage(), false);
        }
    }

    // Argümanları almak için getter'lar (Sunucu tarafında gerekirse)
    public Long getKey() {
        return key;
    }

    public Ticket getTicket() {
        return ticket;
    }


    @Override
    public String toString() {
        return getName() + " [Key: " + key + ", Ticket: " + ticket + "]";
    }

}
//    @Override
//    public boolean apply(String[] arguments) {
//        try {
//            if (arguments.length < 2 || arguments[1].trim().isEmpty()) {
//                throw new WrongAmountOfElementsException();
//            }
//
//            long key;
//            try {
//                key = Long.parseLong(arguments[1].trim());
//            } catch (NumberFormatException e) {
//                console.printError("The key must be a number.");
//                return false;
//            }
//
//            console.println("Creating a new Ticket...");
//            TicketForm form = new TicketForm(console);
//
//            Ticket ticket = form.build(); // Yeni bir Ticket nesnesi oluştur
//            // Artık ticket.setKey(key) çağrısına gerek yok
//
//            console.println("New Ticket created: " + ticket); // Kontrol için
//
//            collectionManager.addToCollection(key, ticket); // Koleksiyona ekle
//            console.println("Ticket has been successfully added!");
//
//            return true;
//        } catch (WrongAmountOfElementsException e) {
//            console.printError("Wrong number of arguments!");
//        } catch (InvalidFormException e) {
//            console.printError("Ticket has not been created: " + e.getMessage());
//        } catch (IncorrectInputInScriptException e) {
//            // Script modunda hata olduğunda
//        }
//        return false;
//    }

