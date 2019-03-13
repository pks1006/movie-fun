package org.superbiz.moviefun.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class S3Store implements BlobStore {
    AmazonS3Client s3Client;
    String photoStorageBucket;


    public S3Store(AmazonS3Client s3Client, String photoStorageBucket){
        this.s3Client = s3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        s3Client.putObject(photoStorageBucket, blob.name, blob.inputStream, new ObjectMetadata());
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {


        if (s3Client.doesObjectExist(photoStorageBucket,name)){
            S3Object s3Object = s3Client.getObject(photoStorageBucket,name);

            Tika tika = new Tika();
            InputStream is = s3Object.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(is);

            String contentType = tika.detect(bytes);

            Blob blob = new Blob(name,new ByteArrayInputStream(bytes),contentType);
            return Optional.of(blob);
        }
        return Optional.empty();
    }

    @Override
    public void deleteAll() {

    }
}
