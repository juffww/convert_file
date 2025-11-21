package utils;

import io.imagekit.sdk.ImageKit;
import io.imagekit.sdk.config.Configuration;

public class ImageKitConnection {

    private static ImageKit imageKit;

    public static ImageKit getInstance() {
        if (imageKit == null) {
            imageKit = ImageKit.getInstance();
            Configuration config = new Configuration(
                    "public_jKU5hzTx8Xk7etZmYccARVn4Mok=",
                    "private_3PeBvD74MsvDfL7a1/+xUURsniM=",
                    "https://ik.imagekit.io/s3bxwjz5c"
            );
            imageKit.setConfig(config);
        }
        return imageKit;
    }
}
