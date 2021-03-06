package ch.aimservices.android.plugin.action.filesystem;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.webkit.WebView;

import ch.aimservices.android.plugin.SenseServicesContext;
import ch.sysmosoft.sense.android.core.service.context.File;

public class MoveItemAction extends AbstractFileSystemAction {
	private final Logger logger = LoggerFactory.getLogger(MoveItemAction.class);

	public MoveItemAction(final WebView webview, final CordovaInterface cordova,
			final SenseServicesContext senseServicesContext) {
		super(webview, cordova, senseServicesContext);
	}

	@Override
	public boolean supports(final String action) {
		return "moveItemAtPath".equals(action);
	}

	@Override
	void execute(String action, JSONObject options, CallbackContext callbackContext) {
		String path;
		String newPath;
		try {
			path = options.getString("currentPath");
			newPath = options.getString("newPath");
		} catch (JSONException e) {
			logger.error("Problem retrieving parameters. Returning error.", e);
			this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, false));
			return;
		}
		logger.debug("moving file \"" + path + "\" to \"" + newPath + "\"" );
		java.io.File userDir = getSenseContext().getFilesDir();
		File src = new File(userDir, path, getSenseContext());
		File dest = new File(userDir, newPath, getSenseContext());
		boolean result = src.renameTo(dest);
		this.callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, result));
	}
}
