#!/usr/bin/env node

var fs = require('fs'),
	path = require('path'),
	plist = require('plist'),
	util = require('util');

var rootdir = process.argv[2];

if (rootdir) {
	console.log("SENSE - Running hook to add iOS keychain access group entitlements");

	var iosPlatform = path.join('platforms', 'ios');
	var iosFolder = fs.existsSync(iosPlatform) ? iosPlatform : context.opts.projectRoot;

	fs.readdir(iosFolder, function (err, data) {
		if (err) {
			throw err;
		}

		var projFolder;
		var projName;

		// Find the project folder by looking for *.xcodeproj
		if (data && data.length) {
			data.forEach(function (folder) {
				if (folder.match(/\.xcodeproj$/)) {
					projFolder = path.join(iosFolder, folder);
					projName = path.basename(folder, '.xcodeproj');
				}
			});
		}

		if (!projFolder || !projName) {
			throw new Error("Could not find an .xcodeproj folder in: " + iosFolder);
		}

		var projectPlistPath = path.join(iosFolder, projName, util.format('%s-Info.plist', projName));
		var projectPlistJson = plist.parse(fs.readFileSync(projectPlistPath, 'utf8'));
		var bundleID = projectPlistJson.CFBundleIdentifier;

		var entitlementsFiles = ['Entitlements-Debug.plist', 'Entitlements-Release.plist']
		for (var x=0; x<entitlementsFiles.length; x++) {
			var entitlementsPath = path.join(iosFolder, projName, entitlementsFiles[x]);
			var entitlementsJson = plist.parse(fs.readFileSync(entitlementsPath, 'utf8'));

			var accessGroup = "$(AppIdentifierPrefix)" + bundleID
			var keychainAccessGroup = entitlementsJson["keychain-access-groups"];
			if (keychainAccessGroup != null) {
				if (keychainAccessGroup.indexOf(accessGroup) < 0) {
					keychainAccessGroup.splice(0, 0, accessGroup);
				}
			} else {
				keychainAccessGroup = [accessGroup];
			}
			entitlementsJson["keychain-access-groups"] = keychainAccessGroup;

			fs.writeFileSync(entitlementsPath, plist.build(entitlementsJson));
		}
	});
}
