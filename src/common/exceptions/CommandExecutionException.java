package common.exceptions;

import common.commands.ExecuteScriptCommand;

public class CommandExecutionException extends Exception {
    public CommandExecutionException(String message) {
        super(message);
    }
    public CommandExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    //Sunucu tarafında bir komut yürütülürken genel bir hata oluştuğunda
    // Diğer sunucu taraflı exception'lar bundan türeyebilir.
}
