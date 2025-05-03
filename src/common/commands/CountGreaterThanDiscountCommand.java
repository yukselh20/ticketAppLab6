package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;

public class CountGreaterThanDiscountCommand implements Command {
    private static final long serialVersionUID = 12L;
    private final long discountThreshold; //taşınacak veri

    public CountGreaterThanDiscountCommand(long discountThreshold) {
        this.discountThreshold=discountThreshold;
    }

    @Override
    public String getName(){
        return "count_greater_than_discount";
    }

    @Override
    public Response execute(CollectionManager manager){
        try {
            //stream işlemi execute içinde yapılıyor
            // Stream işlemi genellikle hata atmaz, ama koleksiyon null ise veya başka bir sorun olursa diye
            // try-catch iyi
            if(manager.getCollection() == null) {
                return new Response("Error Collection not initialised.", false);
            }
            long count = manager.getCollection().values().stream()
                    .filter(ticket-> ticket.getDiscount() > discountThreshold).count();

            //Sonuç response sınıfı ile döner
            return new Response("Number of elements whose discount is greater than " + discountThreshold +": "+ count);

        } catch (Exception e) {
            return new Response("There is an error occured when counting: "+ e.getMessage(),false);
            // logger.error("Sayım yapılırken hata:", e); // Loglama eklenecek
        }
    }

    @Override
    public String toString() {
        return getName() + " " + discountThreshold;
    }
}