package com.eli.oneos.model.phone.comp;

import java.io.File;
import java.util.Comparator;

/** Comparator for File LastModified */
public class FileTimeComparator implements Comparator<File> {

	@Override
	public int compare(File file1, File file2) {
		if (file1 == null || file2 == null) {
			return 0;
		}

		if (file1.isFile() && file2.isDirectory()) {
			return 1;
		} else if (file1.isDirectory() && file2.isFile()) {
			return -1;
		} else {
			if (file1.lastModified() < file2.lastModified()) {
				return 1;
			} else if (file1.lastModified() > file2.lastModified()) {
				return -1;
			} else {
				return 0;
			}
		}
	}
}
