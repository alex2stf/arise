package com.arise.core.models;

import java.io.File;

public interface Unarchiver {
    boolean extract(File source, File destination);
}
