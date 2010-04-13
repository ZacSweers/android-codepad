package com.bean.androidpad;

import android.text.TextUtils;

public class TextDocumentHandler implements DocumentHandler {

	@Override
	public String getFileExtension() {
		return "txt";
	}

	@Override
	public String getFileFormattedString(String fileString) {
		return TextUtils.htmlEncode(fileString).replace("\n", "<br>");
	}

	@Override
	public String getFileMimeType() {
		return "text/plain";
	}

	@Override
	public String getFilePrettifyClass() {
		return "prettyprint";
	}

	@Override
	public String getFileScriptFiles() {
		return "";
	}

}
