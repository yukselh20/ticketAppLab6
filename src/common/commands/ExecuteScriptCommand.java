package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;

public class ExecuteScriptCommand implements Command {
    private static final long serialVersionUID = 13L;
    private final String scriptFileName; //Taşınacak veri : script dosya adı

    public ExecuteScriptCommand(String scriptFileName){
        this.scriptFileName=scriptFileName;
    }

    @Override
    public String getName(){
        return "execute_script";
    }

    @Override
    public Response execute(CollectionManager manager){
        // Bu execute metodu SUNUCUDA çalışacak.
        // Gerçek script işleme mantığı burada çağrılmalı (daha sonra eklenecek).
        // Şimdilik sadece scriptin alındığını belirten bir mesaj döndürelim.
        // Sunucu, bu komutu aldığında script dosyasını okuyup işleyecek.
        return new Response("The server has received a request to process the script: " + scriptFileName , true, null);
        // Gerçek implementasyonda, sunucu scripti işledikten sonra
        // scriptin sonucunu içeren bir Response döndürebilir.

    }

    // İstemcinin veya sunucunun dosya adını alması için getter
    public String getScriptFileName() {
        return scriptFileName;
    }

    @Override
    public String toString() {
        return getName() + " " + scriptFileName;
    }

//    private final Console console;
//
//    public ExecuteScriptCommand(Console console) {
//        super("execute_script", "execute_script <file_name> : read and execute a script from a specified file");
//        this.console = console;
//    }
//
//    @Override
//    public boolean apply(String[] arguments) {
//        if(arguments[1].isEmpty()){
//            console.println("Utilization: " + getName());
//            return false;
//        }
//        console.println("Script Execution: " + arguments[1]);
//        // Runner sınıfı script modunu devralıyor.
//        return true;
//    }
}
