package client.utility;

import client.forms.TicketForm; // Formları kullanacak
import client.network.UDPClient; // Ağı kullanacak
import client.utility.console.Console;
import common.Response;
import common.commands.*; // Komut sınıfları
import common.exceptions.*; // Exception'lar
import common.exceptions.NoSuchElementException;
import common.models.*; // Modeller
import common.Command;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
/**
 * Manages interactive and script modes for the client application.
 * Parses user input, creates Command objects, sends them via UDPClient,
 * and displays the response.
 */


//Uygulamanın interaktif ve script modlarını yöneten sınıf.
// CommandManager bağımlılığını kaldıralım. Komut nesneleri artık Runner içinde
// (veya yardımcı bir sınıfta) oluşturulacak.
// CollectionManager bağımlılığını kaldıralım.
// Komutlar artık doğrudan koleksiyonu değil, ağ üzerinden sunucuyu hedefleyecek.


public class Runner {

    public enum ExitCode { OK, ERROR, EXIT }

    private final Console console;
    private final UDPClient udpClient; // Ağ yöneticisi
    private final List<String> scriptStack = new ArrayList<>();
    // private static final String SCRIPT_BASE_DIR = ...; // Script yolu

    // CommandManager kaldırıldı, UDPClient eklendi
    public Runner(Console console, UDPClient udpClient) {
        this.console = console;
        this.udpClient = udpClient;
    }

    /**
     * Runs the client in interactive mode.
     */
    public void interactiveMode() {
        Scanner scanner = Interrogator.getUserScanner();
        Interrogator.setUserMode(); // Script modunda olmadığımızdan emin olalım
        try {
            ExitCode commandStatus;
            String[] userCommand;

            do {
                console.ps1();
                userCommand = (scanner.nextLine().trim() + " ").split(" ", 2);
                userCommand[1] = userCommand[1].trim();
                commandStatus = launchCommand(userCommand);
            } while (commandStatus != ExitCode.EXIT);

        } catch (IllegalStateException exception) {
            console.printError("Unexpected error!");
        }
    }

    /**
     * Runs the client in script mode.
     * @param fileName Script file name.
     * @return Exit code.
     */
    public ExitCode scriptMode(String fileName) {
        // !!! Bu metodun implementasyonu Lab 6 için yeniden düşünülmeli !!!
        // İstemci mi scripti satır satır okuyup komut gönderecek,
        // yoksa sadece execute_script komutunu mu gönderecek?
        // Görev metni ExecuteScript'in sunucuda çalıştırılmasını ima ediyor gibi.
        // Şimdilik Lab 5'teki gibi bırakalım, ama execute_script komutunu
        // sunucuya gönderecek şekilde launchCommand'ı uyarlayacağız.

        String[] userCommand = {"", ""}; // Başlangıç değeri
        ExitCode commandStatus;
        scriptStack.add(fileName);
        // fileName = SCRIPT_BASE_DIR + fileName; // Tam dosya yolu

        try (Scanner scriptScanner = new Scanner(new File(fileName))) {
            if (!scriptScanner.hasNext()) {
                throw new NoSuchElementException("There is no such element.");
            }
            Scanner tmpScanner = Interrogator.getUserScanner();
            Interrogator.setUserScanner(scriptScanner);
            Interrogator.setFileMode();
            do {
                userCommand = (scriptScanner.nextLine().trim() + " ").split(" ", 2);
                userCommand[1] = userCommand[1].trim();
                while (scriptScanner.hasNextLine() && userCommand[0].isEmpty()) {
                    userCommand = (scriptScanner.nextLine().trim() + " ").split(" ", 2);
                    userCommand[1] = userCommand[1].trim();
                }
                console.println(console.getPS1() + String.join(" ", userCommand));
                if (userCommand[0].equals("execute_script")) {
                    for (String script : scriptStack) {
                        if (userCommand[1].equals(script)) throw new ScriptRecursionException();
                    }
                }
                commandStatus = launchCommand(userCommand); // Script içindeki komutları çalıştır
            } while (commandStatus == ExitCode.OK && scriptScanner.hasNextLine());
            Interrogator.setUserScanner(tmpScanner);
            Interrogator.setUserMode();
            if (commandStatus == ExitCode.ERROR /*&& !(userCommand[0].equals("execute_script") && !userCommand[1].isEmpty())*/) {
                console.println("Script execution aborted due to error.");
            }
            return commandStatus; // Scriptin son durumunu döndür

        } catch (FileNotFoundException exception) {
            console.printError("Script file not found!");
        } catch (NoSuchElementException exception) {
            console.printError("Script file is empty!");
        } catch (ScriptRecursionException exception) {
            console.printError("Scripts cannot be called recursively!");
        } catch (IllegalStateException exception) {
            console.printError("Unexpected error!");
            // System.exit(0); // İstemciyi durdurmak yerine hata kodu dönebiliriz
        } finally {
            scriptStack.remove(scriptStack.size() - 1);
            Interrogator.setUserMode(); // Her zaman kullanıcı moduna dön
        }
        return ExitCode.ERROR;
    }

    /**
     * Parses user input, creates a Command object, sends it, and handles the response.
     * @param userCommand Command array [command_name, arguments_string].
     * @return Exit code.
     */
    private ExitCode launchCommand(String[] userCommand) {
        String commandName = userCommand[0];
        String commandArgs = userCommand[1];

        if (commandName.isEmpty()) return ExitCode.OK;

        Command commandToSend = null;
        try {
            switch (commandName) {
                case "help":
                    if (!commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    commandToSend = new HelpCommand();
                    break;
                case "info":
                    if (!commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    commandToSend = new InfoCommand();
                    break;
                case "show":
                    if (!commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    commandToSend = new ShowCommand();
                    break;
                case "insert":
                    if (commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    Long insertKey = Long.parseLong(commandArgs); // Argüman parse etme
                    console.println("Creating a new Ticket...");
                    Ticket insertTicket = new TicketForm(console).build(); // Formu kullan
                    commandToSend = new InsertCommand(insertKey, insertTicket);
                    break;
                case "update":
                    if (commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    int updateId = Integer.parseInt(commandArgs); // Argüman parse etme
                    console.println("Enter data to update Ticket ID#" + updateId);
                    Ticket updateTicketData = new TicketForm(console).build();
                    commandToSend = new UpdateCommand(updateId, updateTicketData);
                    break;
                case "remove_key":
                    if (commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    Long removeKey = Long.parseLong(commandArgs);
                    commandToSend = new RemoveKeyCommand(removeKey);
                    break;
                case "clear":
                    if (!commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    commandToSend = new ClearCommand();
                    break;
                case "execute_script":
                    if (commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    // İstemci scripti çalıştırmaz, sadece isteği gönderir
                    commandToSend = new ExecuteScriptCommand(commandArgs);
                    // scriptMode(commandArgs); // Bu çağrı burada olmamalı
                    // break; // break yerine doğrudan komutu gönder
                    break;
                case "exit":
                    if (!commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    console.println("Exiting program...");
                    return ExitCode.EXIT; // Doğrudan çıkış yap
                case "remove_lower":
                    if (!commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    console.println("Enter data for comparison Ticket:");
                    Ticket refTicket = new TicketForm(console).build();
                    commandToSend = new RemoveLowerCommand(refTicket);
                    break;
                case "replace_if_lower":
                    if (commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    Long replaceKey = Long.parseLong(commandArgs);
                    console.println("Enter data for the new Ticket:");
                    Ticket newLowerTicket = new TicketForm(console).build();
                    commandToSend = new ReplaceIfLowerCommand(replaceKey, newLowerTicket);
                    break;
                case "count_greater_than_discount":
                    if (commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    long discount = Long.parseLong(commandArgs);
                    commandToSend = new CountGreaterThanDiscountCommand(discount);
                    break;
                case "filter_starts_with_name":
                    if (commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    commandToSend = new FilterStartsWithNameCommand(commandArgs);
                    break;
                case "print_descending":
                    if (!commandArgs.isEmpty()) throw new WrongAmountOfElementsException();
                    commandToSend = new PrintDescendingCommand();
                    break;
                // case "history": // History komutu kaldırıldıysa
                //     break;
                default:
                    console.printError("Command '" + commandName + "' not found. Type 'help' for help.");
                    return ExitCode.ERROR;
            }
        } catch (WrongAmountOfElementsException e) {
            console.printError("Incorrect number of arguments for command '" + commandName + "'. Usage: ..."); // Kullanım bilgisi eklenebilir
            return ExitCode.ERROR;
        } catch (NumberFormatException e) {
            console.printError("Argument must be a number for command '" + commandName + "'.");
            return ExitCode.ERROR;
        } catch (InvalidFormException e) {
            console.printError("Invalid data entered: " + e.getMessage());
            return ExitCode.ERROR;
        } catch (IncorrectInputInScriptException e) {
            console.printError("Error in script input. Aborting script.");
            return ExitCode.ERROR; // Script modunda hatayı yukarı taşı
        }

        // Komut oluşturulduysa sunucuya gönder ve yanıtı al
        if (commandToSend != null) {
            Response response = udpClient.sendCommandAndGetResponse(commandToSend); // Bu metot sonra implemente edilecek
            if (response != null) {
                // Yanıtı yazdır (Response.toString() bunu halleder)
                console.println(response.toString());
                return response.isSuccess() ? ExitCode.OK : ExitCode.ERROR;
            } else {
                console.printError("Failed to get response from server. It might be unavailable.");
                return ExitCode.ERROR; // Sunucuya ulaşılamadı
            }
        }
        return ExitCode.ERROR; // Komut oluşturulamadıysa
    }
}













//public class Runner {
//
//    public enum ExitCode { OK, ERROR, EXIT }
//
//    private final Console console;
//    private final UDPClient udpClient; //ağ yöneticisi
//    private final List<String> scriptStack = new ArrayList<>();
//    //private static final String SCRIPT_BASE_DIR = System.getProperty("user.dir") +File.separator + "src" + File.separator;
//
//    // CommandManager kaldırıldı, UDPClient eklendi
//    public Runner(Console console, UDPClient udpClient) {
//        this.console = console;
//        this.udpClient=udpClient;
//    }
//
//    /**
//     * Runs the client in interactive mode.
//     */
//    public void interactiveMode() {
//        Scanner scanner = Interrogator.getUserScanner();
//        Interrogator.setUserMode(); //script modunda değiliz
//        try {
//            ExitCode commandStatus;
//            String[] userCommand;
//
//            do {
//                console.ps1();
//                userCommand = (scanner.nextLine().trim() + " ").split(" ", 2);
//                userCommand[1] = userCommand[1].trim();
//                commandStatus = launchCommand(userCommand);
//
//            } while (commandStatus != ExitCode.EXIT);
//        } catch (NoSuchElementException e) {
//            console.printError("No user input detected!");
//        } catch (IllegalStateException e) {
//            console.printError("Unforeseen mistake!");
//        }
//    }
//
//    /**
//     * Runs the client in script mode.
//     * @param fileName Script file name.
//     * @return Exit code.
//     */
//    public ExitCode scriptMode(String fileName) {
//
//        // !!! Bu metodun implementasyonu Lab 6 için yeniden düşünülmeli !!!
//        // İstemci mi scripti satır satır okuyup komut gönderecek,
//        // yoksa sadece execute_script komutunu mu gönderecek?
//        // Görev metni ExecuteScript'in sunucuda çalıştırılmasını ima ediyor gibi.
//        // Şimdilik Lab 5'teki gibi bırakalım, ama execute_script komutunu
//        // sunucuya gönderecek şekilde launchCommand'ı uyarlayacağız.
//
//        String[] userCommand = {"", ""};
//        ExitCode commandStatus;
//        scriptStack.add(fileName);
//
//        //fileName = SCRIPT_BASE_DIR + fileName;
//
//        try (Scanner scriptScanner = new Scanner(new File(fileName))){
//            if (!scriptScanner.hasNext()){
//                throw new NoSuchElementException();
//            }
//            Scanner tmpScanner = Interrogator.getUserScanner();
//            Interrogator.setUserScanner(scriptScanner);
//            Interrogator.setFileMode();
//
//
//        }
//        if(!new File(fileName).exists()){
//            fileName = "../" + fileName;
//        }
//        try (Scanner scriptScanner = new Scanner(new File(fileName))) {
//            if(!scriptScanner.hasNext()) throw new NoSuchElementException();
//            Scanner tempScanner = Interrogator.getUserScanner();
//            Interrogator.setUserScanner(scriptScanner);
//            Interrogator.setFileMode();
//

//            do {
//                String input = scriptScanner.nextLine().trim();
//                commandArgs = (input + " ").split(" ", 2);
//                commandArgs[1] = commandArgs[1].trim();
//                while(scriptScanner.hasNextLine() && commandArgs[0].isEmpty()){
//                    input = scriptScanner.nextLine().trim();
//                    commandArgs = (input + " ").split(" ", 2);
//                    commandArgs[1] = commandArgs[1].trim();
//                }
//                console.println(console.getPS1() + input);
//                if(commandArgs[0].equals("execute_script")){
//                    for(String script : scriptStack){
//                        if(commandArgs[1].equals(script)) throw new ScriptRecursionException();
//                        // Bunu düzeltmek için, script dosyasının zaten scriptStack içindeyse
//                        // tekrar çalıştırılmamasını sağlamanız gerekir.
//                    }
//                }
//                code = launchCommand(commandArgs);
//            } while(code == ExitCode.OK && scriptScanner.hasNextLine());
//
//            Interrogator.setUserScanner(tempScanner);
//            Interrogator.setUserMode();
//
//            if(code == ExitCode.ERROR && !(commandArgs[0].equals("execute_script") && !commandArgs[1].isEmpty())){
//                console.println("Check the script to make sure the data is correct!");
//            }
//            return code;
//        } catch(FileNotFoundException e) {
//            console.printError("Script file not found!");
//        } catch(NoSuchElementException e) {
//            console.printError("The script file is empty!");
//        } catch(ScriptRecursionException e) {
//            console.printError("Scripts cannot be called recursively!");
//        } catch(IllegalStateException e) {
//            console.printError("Unforeseen mistake!");
//            System.exit(0);
//        } finally {
//            if (!scriptStack.isEmpty()) scriptStack.remove(scriptStack.size() - 1);
//        }
//        return ExitCode.ERROR;
//    }
//
//    private ExitCode launchCommand(String[] commandArgs) {
//        if(commandArgs[0].isEmpty()) return ExitCode.OK;
//        var command = commandManager.getCommands().get(commandArgs[0]);
//        if(command == null){
//            console.printError("Command '" + commandArgs[0] + "' not found. Type 'help' for help");
//            return ExitCode.ERROR;
//        }
//        switch(commandArgs[0]){
//            case "exit":
//                if(!command.apply(commandArgs)) return ExitCode.ERROR;
//                else return ExitCode.EXIT;
//            case "execute_script":
//                if(!command.apply(commandArgs)) return ExitCode.ERROR;
//                else return scriptMode(commandArgs[1]);
//            default:
//                if(!command.apply(commandArgs)) return ExitCode.ERROR;
//        }
//        return ExitCode.OK;
//    }
//}