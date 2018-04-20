package ch.aimservices.android.plugin.action;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.webkit.WebView;
import android.widget.Toast;

import ch.aimservices.android.plugin.SenseServicesContext;

/**
 * Created by IntelliJ IDEA.
 * User: pblanco
 * Date: 17.07.14
 * Time: 08:21
 */
public class UpdateAppAction extends BaseAction {
	public static final String UPDATE_DIRECTORY = "updates";
	public static final String UPDATE_FILE_NAME = "appUpdate.apk";

	private final Logger logger = LoggerFactory.getLogger(UpdateAppAction.class);

	public UpdateAppAction(final WebView webview, final CordovaInterface cordova, final SenseServicesContext
			senseServicesContext) {
		super(webview, cordova, senseServicesContext);
	}

	@Override
	public boolean supports(final String action) {
		return "updateApp".equals(action);
	}

	@Override
	public boolean execute(final String action, final JSONArray args, final CallbackContext callbackContext) {
		logger.debug("UpdateAppAction:execute -> " + action);
		try {
			this.callbackContext = callbackContext;

			final JSONObject options = args.getJSONObject(0);
			final String url = options.getString("url");

			DownloadUpdateAsyncTask downloadUpdateAsyncTask = new DownloadUpdateAsyncTask(getContext(), url);
			downloadUpdateAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			success(0);
		} catch (final JSONException e) {
			logger.error("Problem retrieving parameters. Returning error.", e);
			error(ERR_RETRIEVING_PARAMS);
		}
		return true;
	}

	private class DownloadUpdateAsyncTask extends AsyncTask<Void, Void, File> {
		private final WeakReference<Context> weakContext;
		private final String downloadUrl;

		DownloadUpdateAsyncTask(Context context, String downloadUrl) {
			this.weakContext = new WeakReference<Context>(context);
			this.downloadUrl = downloadUrl;
		}

		@Override
		protected void onPreExecute() {
			Toast.makeText(getContext(), "Launching application update, please wait...", Toast.LENGTH_LONG).show();
		}

		@Override
		protected File doInBackground(Void... params) {
			if (weakContext.get() == null) {
				return null;
			}

			FileOutputStream fos = null;
			try {
				URL url = new URL(downloadUrl);
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				c.setRequestMethod("GET");
				c.setRequestProperty("Accept-Charset", "UTF-8");
				c.setDoOutput(false);
				c.connect();

				File updateFolder = new File(weakContext.get().getFilesDir(), UPDATE_DIRECTORY);
				if (!updateFolder.exists()) {
					updateFolder.mkdirs();
				}

				File apkFile = new File(updateFolder, UPDATE_FILE_NAME);
				fos = new FileOutputStream(apkFile);
				InputStream is = c.getInputStream();
				IOUtils.copy(is, fos);
				IOUtils.closeQuietly(is);
				return apkFile;
			} catch (IOException e) {
				logger.error("Error while downloading file.", e);
				IOUtils.closeQuietly(fos);
			}
			return null;
		}

		@Override
		protected void onPostExecute(File file) {
			if (weakContext.get() == null) {
				return;
			}

			if (file != null) {
				logger.debug("Update downloaded at path {}. Launching package manager", file.getAbsolutePath());

				Intent intent = new Intent(Intent.ACTION_VIEW);
				Uri apkUri;
				if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
					// Workaround to replace MODE_WORLD_READABLE flag that
					// throws a SecurityException since API 25. This is however
					// discouraged by android team.
					file.getParentFile().setExecutable(true, false);
					file.setReadable(true, false);
					apkUri = Uri.fromFile(file);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				} else {
					apkUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".sense.provider",
							file);
					intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
				}
				intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
				getContext().startActivity(intent);
			} else {
				Toast.makeText(getContext(), "Impossible to download update. Try again",
						Toast.LENGTH_LONG).show();
			}
		}
	}
}