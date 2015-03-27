package org.mshare.server.ftp;

import android.util.Log;

/**
 * 用于解析FTP命令
 * Created by huangming on 15/3/27.
 */
public class FtpParser {
	private static final String TAG = FtpParser.class.getSimpleName();

	/**
	 * An FTP parameter is that part of the input string that occurs after the first
	 * space, including any subsequent spaces. Also, we want to chop off the trailing
	 * '\r\n', if present.
	 *
	 * Some parameters shouldn't be logged or output (e.g. passwords), so the caller can
	 * use silent==true in that case.
	 * @return 即便没有内容，也会返回""
	 */
	 public static String getParameter(String input, boolean silent) {
		if (input == null) {
			return "";
		}
		int firstSpacePosition = input.indexOf(' ');
		if (firstSpacePosition == -1) {
			return "";
		}
		String retString = input.substring(firstSpacePosition + 1);

		// Remove trailing whitespace
		// todo: trailing whitespace may be significant, just remove \r\n
		// 删除所有的结尾处的空白
		retString = retString.replaceAll("\\s+$", "");

		if (!silent) {
			Log.d(TAG, "Parsed argument: " + retString);
		}
		return retString;
	}

	/**
	 * A wrapper around getParameter, for when we don't want it to be silent.
	 */
	 public static String getParameter(String input) {
		return getParameter(input, false);
	}
}
