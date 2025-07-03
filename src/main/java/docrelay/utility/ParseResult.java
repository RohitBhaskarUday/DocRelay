package docrelay.utility;

public class ParseResult {

    public String fileName;
    public String contentType;
    public byte[] fileContent;


    public ParseResult(String fileName, String contentType, byte[] fileContent){
        this.fileName=fileName;
        this.contentType=contentType;
        this.fileContent=fileContent;
    }


}
