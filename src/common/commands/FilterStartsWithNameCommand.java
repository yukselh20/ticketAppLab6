package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;
import common.models.Ticket;


import java.util.List;
import java.util.stream.Collectors;

public class FilterStartsWithNameCommand implements Command {
    private static final long serialVersionUID = 14L;
    private final String substring; //Taşınacak veri

    public FilterStartsWithNameCommand(String substring) {
        this.substring = substring;

    }


    @Override
    public Response execute(CollectionManager manager){
        try {
            if (manager.getCollection() == null) {
                return new Response("Error: Collection not initialised.", false);
            }
            //filtreleme sunucuda yapılıyor.
            List<Ticket> filteredList = manager.getCollection().values().stream()
                    .filter(ticket -> ticket.getName().startsWith(substring))
                    .collect(Collectors.toList());


            if (filteredList.isEmpty()){
                return new Response("There is no element starts with: " + substring);

            } else {
                return new Response("Elements start with: "+ substring+ "here"+ filteredList);
            }
        }

        catch (NullPointerException npe){
            // logger.error("Filtreleme sırasında NullPointerException:", npe);
            return new Response("Error during filtering: There may be invalid data in the collection.", false);
        }

        catch (Exception e) {
            return new Response("There is an error occurred when filtering." + e.getMessage(),false);
        }
    }

    @Override
    public String getName() {
        return "filter_starts_with_name";
    }

    public String getSubstring() {
        return substring;
    }

    @Override
    public String toString(){
        return getName() + " " + substring;
    }
}
