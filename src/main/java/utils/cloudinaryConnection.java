package utils;

import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.Cloudinary;

public class cloudinaryConnection {
    private static Cloudinary cloudinary;

    public static Cloudinary getInstance() {
        if(cloudinary == null)
        {
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dig7iy6dw",
                    "api_key", "715439156541162",
                    "api_secret", "ZhXvalBU6yunHm5yAAGU2ZlGU-E",
                    "secure", true
            ));
        }
        return cloudinary;
    }
}
