package tukano.impl.java.servers;

import static java.lang.String.format;

/**
 * @authors Liliane Correia (58427) & Wilker Martins (58535)
 */

 import static tukano.api.java.Result.error;
 import static tukano.api.java.Result.ok;
 import static tukano.api.java.Result.ErrorCode.BAD_REQUEST;
 import static tukano.api.java.Result.ErrorCode.CONFLICT;
 //import static tukano.api.java.Result.ErrorCode.FORBIDDEN;
 import static tukano.api.java.Result.ErrorCode.INTERNAL_ERROR;
// import static tukano.api.java.Result.ErrorCode.NOT_FOUND;

import tukano.api.java.Result;
import tukano.impl.api.java.ExtendedBlobs;
import tukano.impl.java.clients.Clients;
import utils.DropBox.DeleteFileArgs;
import utils.DropBox.DownloadFileArgs;
import utils.DropBox.UploadFileArgs;


import java.util.function.Consumer;
import java.util.logging.Logger;

import org.pac4j.scribe.builder.api.DropboxApi20;

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.google.gson.Gson;


public class JavaBlobsDB implements ExtendedBlobs {

    private static final String apiKey = "4x3y2numjowm5ch";
	private static final String apiSecret = "k3kuog142icr71c";
	//private static final String accessTokenStr = "";
    
	private static final int HTTP_SUCCESS = 200;
	private static final String CONTENT_TYPE_HDR = "Content-Type";
	private static final String JSON_CONTENT_TYPE = "application/octet-stream";
    private static final String DROP_BOX_API_ARG = "Dropbox-API-Arg";   

	private static final String UPLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/upload";
    private static final String DELETE_FILE_URL = "https://api.dropboxapi.com/2/files/delete_v2";
    private static final String DOWNLOAD_FILE_URL = "https://content.dropboxapi.com/2/files/download";
	
    private static final String path = "/teste/";

    private static Logger Log = Logger.getLogger(JavaBlobsDB.class.getName());

    private final Gson json;
	private final OAuth20Service service;
	private final OAuth2AccessToken accessToken;

    public JavaBlobsDB() {
        json = new Gson();
        accessToken = new OAuth2AccessToken(accessTokenStr);
        service = new ServiceBuilder(apiKey).apiSecret(apiSecret).build(DropboxApi20.INSTANCE);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        Log.info("Uploading video " + blobId);
        if (blobId == null || bytes == null)
            return error(BAD_REQUEST);

         if (!validBlobId(blobId))
			return error(BAD_REQUEST);

        Result<byte[]> matchBytes = download(blobId);

        //TODO: verify if is not found??
        if (matchBytes.isOK() && matchBytes.value() != bytes) {
            
            return error(CONFLICT);
        }

        byte[] fileContent = bytes;


        var uploadFile = new OAuthRequest(Verb.POST, UPLOAD_FILE_URL);
        uploadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);
        uploadFile.addHeader(DROP_BOX_API_ARG, json.toJson(new UploadFileArgs(path + blobId)));
        
        uploadFile.setPayload(fileContent);

        service.signRequest(accessToken, uploadFile);
        
       
        try {

           Response r = service.execute(uploadFile);

            if( r.getCode() != HTTP_SUCCESS ) {
                return error(INTERNAL_ERROR); //??

            }
        } catch (Exception e) {
         
            e.printStackTrace();
        }
        
        
        return ok();
      
    }


    @Override
    public Result<byte[]> download(String blobId) {
        Log.info("Downloading video " + blobId);

        if (blobId == null)
        return error(BAD_REQUEST);

        var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);
        
        
        downloadFile.addHeader(DROP_BOX_API_ARG, json.toJson(new DownloadFileArgs(path +blobId)));
        downloadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        service.signRequest(accessToken, downloadFile);
        
        byte[] fileContent = null;

        try {
            Response r = service.execute(downloadFile);
            
            if( r.getCode() != HTTP_SUCCESS ) {
                return error(INTERNAL_ERROR); //??
            }

            fileContent = r.getBody().getBytes();

        } catch (Exception e) {
         
            e.printStackTrace();
        }

        
        return ok(fileContent);
    }

    //TODO ?????
    @Override
	public Result<Void> downloadToSink(String blobId, Consumer<byte[]> sink) {
		Log.info(() -> format("downloadToSink : blobId = %s\n", blobId));

        if (blobId == null)
        return error(BAD_REQUEST);

        var downloadFile = new OAuthRequest(Verb.POST, DOWNLOAD_FILE_URL);
        
        
        downloadFile.addHeader(DROP_BOX_API_ARG, json.toJson(new DownloadFileArgs(path +blobId)));
        downloadFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        service.signRequest(accessToken, downloadFile);
        

        try {
            Response r = service.execute(downloadFile);
            
            if( r.getCode() != HTTP_SUCCESS ) {
                return error(INTERNAL_ERROR); //??
            }

            
            sink.accept(r.getBody().getBytes()); //?????

        } catch (Exception e) {
         
            e.printStackTrace();
        }

        return ok();
        /*
       
       var file = toFilePath(blobId);

       if (file == null)
           return error(BAD_REQUEST);

       if( ! file.exists() )
           return error(NOT_FOUND);

       try (var fis = new FileInputStream(file)) {
           int n;
           var chunk = new byte[CHUNK_SIZE];
           while ((n = fis.read(chunk)) > 0)
               sink.accept(Arrays.copyOf(chunk, n));

           return ok();
       } catch (IOException x) {
           return error(INTERNAL_ERROR);
       }
        */
          
	}


    @Override
    public Result<Void> delete (String blobId, String token)
    {
        Log.info("Deleting video " + blobId);
       
        var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_URL);
        deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        deleteFile.setPayload(json.toJson(new DeleteFileArgs(path + blobId)));

        service.signRequest(accessToken, deleteFile);
        
        try {
           
            Response r = service.execute(deleteFile);
        
            if( r.getCode() != HTTP_SUCCESS ) {
                return error(INTERNAL_ERROR); //??
            }
        } catch (Exception e) {
           
            e.printStackTrace();
        }
        
        return Result.ok();
    }

    

    //!!WRONG!!!
    @Override
    public Result<Void> deleteAllBlobs(String userId, String token) {
      
        var deleteFile = new OAuthRequest(Verb.POST, DELETE_FILE_URL);
        deleteFile.addHeader(CONTENT_TYPE_HDR, JSON_CONTENT_TYPE);

        deleteFile.setPayload(json.toJson(new DeleteFileArgs(path + userId)));

        service.signRequest(accessToken, deleteFile);
        
        try {
           
            Response r = service.execute(deleteFile);
        
            if( r.getCode() != HTTP_SUCCESS ) {
                return error(INTERNAL_ERROR); //??
            }
        } catch (Exception e) {
           
            e.printStackTrace();
        }
        
        return Result.ok();
    }

    private boolean validBlobId(String blobId) {
		return Clients.ShortsClients.get().getShort(blobId).isOK();
	}

    

}