package org.fastcode;

import java.io.Closeable;
import java.util.Iterator;

public interface DataIterator<T> extends Iterator<T>, Closeable {

    int getCount();
}
