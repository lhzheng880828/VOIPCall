package net.java.sip.communicator.impl.replacement.smiley;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.java.sip.communicator.service.replacement.smilies.Smiley;

public class SmileyImpl implements Smiley {
    private final String description;
    private final String imageID;
    private final List<String> smileyStrings;

    public SmileyImpl(String imageID, String[] smileyStrings, String description) {
        this.imageID = imageID;
        this.smileyStrings = Collections.unmodifiableList(Arrays.asList((Object[]) smileyStrings.clone()));
        this.description = description;
    }

    public List<String> getSmileyStrings() {
        return this.smileyStrings;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDefaultString() {
        return (String) this.smileyStrings.get(0);
    }

    public String getImageID() {
        return this.imageID;
    }

    public String getImagePath() {
        URL url = SmileyActivator.getResources().getImageURL(this.imageID);
        if (url == null) {
            return null;
        }
        return url.toString();
    }
}
