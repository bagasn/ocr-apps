package test.bagas.ocrapplication;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import java.io.File;

public class ProviderUtils {

    public static String getAuthotize() {
        return BuildConfig.APPLICATION_ID + ".fileprovider";
    }

    public static Uri generateProviderFile(Context context, File file) {
        return FileProvider.getUriForFile(context, getAuthotize(), file);
    }

}
