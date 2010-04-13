package com.bean.androidpad;

import android.text.TextUtils;

public class SqlDocumentHandler implements DocumentHandler {

	@Override
	public String getFileExtension() {
		return "sql";
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
		return "prettyprint lang-sql";
	}

	@Override
	public String getFileScriptFiles() {
		return "<script src='file:///android_asset/lang-sql.js' type='text/javascript'></script> ";
	}

}
