package com.bean.androidpad;

import android.text.TextUtils;

public class MlDocumentHandler implements DocumentHandler {

	@Override
	public String getFileExtension() {
		return "ml";
	}

	@Override
	public String getFileFormattedString(String fileString) {
		return TextUtils.htmlEncode(fileString).replace("\n", "<br>");
	}

	@Override
	public String getFileMimeType() {
		return "text/html";
	}

	@Override
	public String getFilePrettifyClass() {
		return "prettyprint lang-ml";
	}

	@Override
	public String getFileScriptFiles() {
		return "<script src='file:///android_asset/lang-ml.js' type='text/javascript'></script> ";
	}

}
