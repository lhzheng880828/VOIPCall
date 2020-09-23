package org.jivesoftware.smackx.commands;

public interface LocalCommandFactory {
    LocalCommand getInstance() throws InstantiationException, IllegalAccessException;
}
