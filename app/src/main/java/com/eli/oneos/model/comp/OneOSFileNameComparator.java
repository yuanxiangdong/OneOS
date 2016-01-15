package com.eli.oneos.model.comp;

import com.eli.oneos.model.api.OneOSFile;

import java.util.Comparator;

/**
 * Comparator for File Name
 */
public class OneOSFileNameComparator implements Comparator<OneOSFile> {

    @Override
    public int compare(OneOSFile file1, OneOSFile file2) {
        if (file1.isDirectory() && !file2.isDirectory()) {
            return -1;
        }

        if (!file1.isDirectory() && file2.isDirectory()) {
            return 1;
        }

        return file1.getName().compareTo(file2.getName());
    }
}