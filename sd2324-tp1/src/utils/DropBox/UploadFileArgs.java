package utils.DropBox;

public record UploadFileArgs(String path, String mode, boolean autorename, boolean mute, boolean strict_conflict) {

    public UploadFileArgs(String path) {
        this(path, "add", false, false, false);
    }
    
    
}
