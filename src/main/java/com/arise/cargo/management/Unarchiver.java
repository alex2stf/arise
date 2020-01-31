package com.arise.cargo.management;

import java.io.File;

public interface Unarchiver {
    boolean extract(File source, File destination);
}
