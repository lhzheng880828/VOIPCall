package net.sf.fmj.registry;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

interface RegistryIO {
    void load(InputStream inputStream) throws IOException;

    void write(OutputStream outputStream) throws IOException;
}
