package com.eli.oneos.model.phone.comp;

import java.io.File;
import java.util.Comparator;

/** Comparator for File Name */
public class FileNameComparator implements Comparator<File> {

	@Override
	public int compare(File file1, File file2) {
		if (file1.isDirectory() && file2.isFile())
			return -1;
		if (file1.isFile() && file2.isDirectory())
			return 1;

		return file1.getName().compareTo(file2.getName());
	}
}