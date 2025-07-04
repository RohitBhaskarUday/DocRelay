package docrelay.utility;

public class MultiParser {
    private final String boundary;
    private final byte[] requestData;

    public MultiParser(byte[] requestData, String boundary){
        this.boundary=boundary;
        this.requestData=requestData;
    }


    public ParseResult parseResult(){
        //Content - Parser - Custom
        try{

            String dataAsString = new String(requestData); // Now it will work for pdfs, excel, csv etc but for video's use Object
            String contentType = "application/octet-stream";
            String fileNameMarker = "filename=\"";
            int fileNameStartIndex = dataAsString.indexOf(fileNameMarker);
            if(fileNameStartIndex  == -1){
                return null;
            }
            fileNameStartIndex+=fileNameMarker.length();
            int fileNameEndIndex = dataAsString.indexOf("\"", fileNameStartIndex);
            String fileName = dataAsString.substring(fileNameStartIndex, fileNameEndIndex);

            String contentTypeMarker = "Content-Type: ";
            int contentTypeStart = dataAsString.indexOf(contentTypeMarker, fileNameEndIndex);
            if(contentTypeStart != -1){
                contentTypeStart+=contentTypeMarker.length();
                int contentTypeEnd = dataAsString.indexOf("\r\n", contentTypeStart);
                contentType = dataAsString.substring(contentTypeStart, contentTypeEnd);
            }

            String headerEndMarker = "\r\n\r\n";
            int headerEnd = dataAsString.indexOf(headerEndMarker);
            if(headerEnd == -1){
                return null;
            }
            int contentStart = headerEnd + headerEndMarker.length();
            byte[] boundaryBytes = ("\r\n--"+boundary+"--").getBytes();
            int contentEnd = findSequence(requestData, boundaryBytes, contentStart);

            if(contentEnd == -1){
                boundaryBytes = ("\r\n--"+boundary).getBytes();
                contentEnd=findSequence(requestData, boundaryBytes, contentStart);
            }

            if(contentEnd==-1 || contentEnd <= contentStart){
                return null;
            }

            byte[] fileContent = new byte[contentEnd-contentStart];
            System.arraycopy(requestData, contentStart, fileContent, 0, fileContent.length);
            return new ParseResult(fileName, contentType, fileContent);



        } catch (Exception e) {
            System.out.println("Error Parsing the multipart data");
            e.printStackTrace();
            return null;
        }

    }

    private static int findSequence(byte[] requestData, byte[] sequence, int startPos){
        outer:
            for (int i=startPos; i<=requestData.length - sequence.length; i++){
                for(int j=0; j<sequence.length; j++){
                   if(requestData[i+j]!=sequence[j]){
                       continue outer;
                   }
                }
                return i;
            }
            return -1;
    }



}
