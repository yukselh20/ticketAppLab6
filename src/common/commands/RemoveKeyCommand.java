package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;

public class RemoveKeyCommand implements Command {
    private static final long serialVersionUID = 18L;
    private final Long keyToRemove; // Silinecek veri

    public RemoveKeyCommand(Long keyToRemove) {
        this.keyToRemove = keyToRemove;
    }

    @Override
    public String getName() {
        return "remove_key";
    }

    @Override
    public Response execute(CollectionManager manager) {
        try {

            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }
            // Önce elemanın varlığını kontrol et
            if (!manager.getCollection().containsKey(keyToRemove)) {
                return new Response("No elements with this key were found: " + keyToRemove, false);
            }
            manager.removeFromCollection(keyToRemove);
            return new Response("The element was successfully deleted with ‘key: " + keyToRemove);
        } catch (Exception e) {
            // logger.error("Eleman silinirken hata:", e);
            return new Response("Error when deleting an element: " + e.getMessage(), false);
        }
    }
    public Long getKeyToRemove() { return keyToRemove; } // Getter

    @Override
    public String toString() {
        return getName() + " " + keyToRemove;
    }

}










//    public RemoveKey(Console console, CollectionManager collectionManager) {
//        super("remove_key", "remove_key <key> : remove an item from the collection by its key");
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
//            Long key = Long.parseLong(arguments[1]);
//            if(!collectionManager.getCollection().containsKey(key)){
//                console.printError("Element with this key was not found.");
//                return false;
//            }
//            collectionManager.removeFromCollection(key);
//            console.println("The item was successfully deleted.");
//            return true;
//        } catch(WrongAmountOfElementsException e){
//            console.printError("Wrong number of arguments!");
//        } catch(NumberFormatException e){
//            console.printError("The key must be a number!");
//        }
//        return false;
//    }
