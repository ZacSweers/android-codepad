package com.bean.androidpad;

import android.text.TextUtils;

public class LuaDocumentHandler implements DocumentHandler {

	@Override
	public String getFileExtension() {
		return "lua";
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
		return "prettyprint lang-lua";
	}

	@Override
	public String getFileScriptFiles() {
		return "<script src='file:///android_asset/lang-lua.js' type='text/javascript'></script> ";
	}

}
