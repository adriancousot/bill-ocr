package com.sandh.billanalyzer.utility;

import org.apache.commons.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by hamed on 02/12/2015.
 */
public class Utility {
    public static final String TEST_RECEIPTS_OUTPUT ="TEST_RECEIPTS_OUTPUT";

    private static Logger LOG = LoggerFactory.getLogger(Utility.class.getName());

    public static  File getTempFile(String originName,String paramters,String transformerName) throws IOException {
        String debugOutputFolder = System.getProperty(TEST_RECEIPTS_OUTPUT);
        String fileName=originName + "_" +
                transformerName+"_"
                +paramters+"_";

        DateTimeFormatter FORMATTER =
                DateTimeFormatter.ofPattern("u-MMM-dd-HHMMSS");
        String datetime= ZonedDateTime.now().format(FORMATTER);
        String fileSuffix =String.valueOf(datetime + ".png");
        File dir = null;
        File tempFile = null;
        if(null != debugOutputFolder){
            Path path = Paths.get(debugOutputFolder);
            if (!Files.isDirectory(path)){
                Files.createDirectory(path);
            }
            dir = path.toFile();
            tempFile = File.createTempFile(fileName,fileSuffix,dir);
        }else{
            tempFile = File.createTempFile(
                    fileName, fileSuffix, null);
        }


        return tempFile;
    }
    public static void storeImageMatInTempFile(Mat imageMat,
                                           TraceableOperator traceableOperator){

        File file =storeImageStreamInTempFile(
                matToInputStream(imageMat),
                traceableOperator);
    }

    public static File storeImageStreamInTempFile(InputStream imageStreamIn,
                                            TraceableOperator traceableOperator) {
        File tempFile = null;

        try {
            byte[] imageBytes = IOUtils.toByteArray(imageStreamIn);
            tempFile = Utility.getTempFile(
                    traceableOperator.getOriginName(),
                    traceableOperator.getParameters(),
                    traceableOperator.getOperation());
            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(imageBytes);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(traceableOperator.isDebugMode()) {
            LOG.info("File loc:{}", tempFile.getAbsolutePath());
        }else{
            tempFile.deleteOnExit();
        }
        return tempFile;
    }



    public static Mat readInputStreamIntoMat(InputStream inputStream) throws IOException {
        byte[] temporaryImageInMemory = IOUtils.toByteArray(inputStream);
        // Decode into mat. Use any IMREAD_ option that describes your image appropriately
        Mat outputImage = Imgcodecs.imdecode(new MatOfByte(temporaryImageInMemory), Imgcodecs.IMREAD_COLOR);
        return outputImage;
    }

    public static InputStream matToInputStream(Mat imageMatIn) {
        // create a temporary buffer
        MatOfByte buffer = new MatOfByte();
        // encode the frame in the buffer, according to the PNG format
        Imgcodecs.imencode(".png", imageMatIn, buffer);
        // build and return an Image created from the image encoded in the
        // buffer
        return new ByteArrayInputStream(buffer.toArray());
    }

}
