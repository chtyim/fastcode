package org.fastcode.reader;

import org.fastcode.type.GenuineType;

import java.io.InputStream;
import java.util.Iterator;

public interface DataIteratorFactory {

    /**
     * Creates an {@link Iterator} for stream parsing data of type {@code T} from
     * the given input source.
     */
    <T> Iterator<T> create(GenuineType<T> type, InputStream source);
}
