package com.sandh.billanalyzer.utility;

import org.bytedeco.javacpp.lept;
import org.bytedeco.javacpp.tesseract;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static org.bytedeco.javacpp.lept.pixRead;

/**
 * Created by hamed on 02/12/2015.
 */
public class OCRTransformer extends AbstractTraceableOperator implements Transformer {



    @Override
    public String transform(ImageFilter imageFilter) throws IOException {
        String item =null;

        this.setOriginName(imageFilter.getOriginName());
        this.setDebugMode(imageFilter.isDebugMode());

        Mat imageMat = imageFilter.getImageMat();
        item = applyOCRToImage(imageMat)[0];

        return item;
    }

    private String[] applyOCRToImage(Mat preparedImageMat) {
        String items[] =new String[1];

        String ocrOutPutString = applyOCR(Utility.matToInputStream(preparedImageMat));
        items[0]=ocrOutPutString;
        return items;
    }
    private String applyOCR(InputStream imageStreamIn){
        String outText=null;

        tesseract.TessBaseAPI api = new tesseract.TessBaseAPI();
        String tessdataPath = System.getProperties().getProperty("TESSDATA_PREFIX");
        int tessInitCodeInt = api.Init(tessdataPath,"ENG");
        if (tessInitCodeInt != 0) {
            throw new RuntimeException("Unable to initialise OCR lib");
        }

        File tempFile = Utility.storeImageStreamInTempFile(imageStreamIn, this);

        // Open input image with leptonica library
        lept.PIX image = pixRead(tempFile.getAbsolutePath());
        api.SetImage(image);


        // Get OCR result
        outText = api.GetUTF8Text().getString();

        return outText;
    }
}
