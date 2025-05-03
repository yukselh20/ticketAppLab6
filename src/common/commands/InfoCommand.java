package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;

/**
 * info : Koleksiyon hakkında bilgi verir.
 */
public class InfoCommand implements Command {
    private static final long serialVersionUID = 16L;
    //Argüman almaz

    @Override
    public String getName(){
        return "info" ;
    }

    @Override
    public Response execute(CollectionManager manager) {
        try {
            String info = manager.getCollectionInfo();
            if(info == null){
                return new Response("No collection information could be obtained.", false);
            }
            return new Response(info);
        } catch (Exception e) {
            // logger.error("Koleksiyon bilgisi alınırken hata:", e);
            return new Response("Error retrieving collection information: "+ e.getMessage(), false);
        }

    }

    @Override
    public String toString() {
        return getName();
    }
}

