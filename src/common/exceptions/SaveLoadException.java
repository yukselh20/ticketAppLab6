package common.exceptions;

public class SaveLoadException extends Exception {
    public SaveLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}


//Sunucuda koleksiyon kaydedilirken veya yüklenirken hata oluşursa kullanılabilir.
