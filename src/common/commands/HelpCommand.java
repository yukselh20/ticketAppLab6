package common.commands;

import common.Command;
import common.Response;
import server.managers.CollectionManager;

/**
 * help : Kullanılabilir komutları listeler.
 */
public class HelpCommand implements Command {
    private static final long serialVersionUID = 15L;

//    public HelpCommand() {
//        super("help", "display help for available common.commands");
//        this.console = console;
//        this.commandManager = commandManager;
//    }
// ARGÜMAN ALMAZ

    @Override
    public String getName(){
        return "help_command";
    }

    @Override
    public Response execute(CollectionManager manager){
        // Sunucu tarafında çalışacak kod.
        // Sunucu, istemcinin kullanabileceği komutların listesini ve açıklamalarını
        // içeren bir yanıt oluşturabilir. Bu bilgi statik olabilir veya CommandManager'dan alınabilir.
        // Şimdilik basit bir yardım metni döndürelim.
        // Gerçek implementasyonda burası daha dinamik olabilir.

        try {
            String helpText = """
                Available commands:
                help : Display help for available commands
                info : Display collection information
                show : Display all items in the collection
                insert <key> {element} : Add a new item with the specified key
                update <id> {element} : Update the value of the collection item whose id is equal to the given one
                remove_key <key> : Remove an item from the collection by its key
                clear : Clear the collection
                execute_script <file_name> : Read and execute a script from a specified file
                exit : End the program
                remove_lower {element} : Remove from the collection all items smaller than the specified value
                replace_if_lower <key> {element} : Replace the value by key if the new value is less than the old one
                count_greater_than_discount <discount> : Output the number of items whose discount field value is greater than the specified value
                filter_starts_with_name <name> : Output items whose name field value starts with the specified substring
                print_descending : Output the elements of the collection in descending order
                """;
            return new Response(helpText);
        } catch (Exception e) {
            // logger.error("Yardım oluşturulurken hata:", e);
            return new Response("Yardım bilgisi alınırken hata oluştu: " + e.getMessage(), false);
        }

    }

    @Override
    public String toString() {
        return getName();
    }
}