package net.java.sip.communicator.impl.replacement.smiley;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.java.sip.communicator.service.replacement.smilies.Smiley;
import net.java.sip.communicator.service.replacement.smilies.SmiliesReplacementService;
import net.java.sip.communicator.util.GuiUtils;
import net.java.sip.communicator.util.Logger;

public class ReplacementServiceSmileyImpl implements SmiliesReplacementService {
    public static final String SMILEY_SOURCE = "SMILEY";
    private static final Logger logger = Logger.getLogger(ReplacementServiceSmileyImpl.class);
    public static String smileyRegex;
    private static final List<String> smileyStrings = new ArrayList();

    public String getReplacement(String sourceString) {
        try {
            Smiley smiley = Resources.getSmiley(sourceString.trim());
            if (smiley != null) {
                return smiley.getImagePath();
            }
            return sourceString;
        } catch (Exception e) {
            logger.error("Failed to get smiley replacement for " + sourceString, e);
            return sourceString;
        }
    }

    private static String getSmileyPattern(Collection<Smiley> smileys) {
        String str;
        synchronized (smileyStrings) {
            boolean smileyStringsIsEqual;
            if (smileyRegex == null) {
                smileyStringsIsEqual = false;
            } else {
                smileyStringsIsEqual = true;
                int smileyStringIndex = 0;
                int smileyStringCount = smileyStrings.size();
                loop2:
                for (Smiley smiley : smileys) {
                    for (String smileyString : smiley.getSmileyStrings()) {
                        if (smileyStringIndex >= smileyStringCount || !smileyString.equals(smileyStrings.get(smileyStringIndex))) {
                            smileyStringsIsEqual = false;
                            break loop2;
                        }
                        smileyStringIndex++;
                    }
                }
                if (smileyStringsIsEqual && smileyStringIndex != smileyStringCount) {
                    smileyStringsIsEqual = false;
                }
            }
            if (!smileyStringsIsEqual) {
                smileyStrings.clear();
                StringBuffer regex = new StringBuffer();
                regex.append("(?<!(alt='|alt=\"))(");
                for (Smiley smiley2 : smileys) {
                    for (String smileyString2 : smiley2.getSmileyStrings()) {
                        smileyStrings.add(smileyString2);
                        regex.append(GuiUtils.replaceSpecialRegExpChars(smileyString2)).append("|");
                    }
                }
                regex = regex.deleteCharAt(regex.length() - 1);
                regex.append(')');
                smileyRegex = regex.toString();
            }
            str = smileyRegex;
        }
        return str;
    }

    public String getSourceName() {
        return SMILEY_SOURCE;
    }

    public String getPattern() {
        return getSmileyPattern(Resources.getDefaultSmileyPack());
    }

    public Collection<Smiley> getSmiliesPack() {
        return Resources.getDefaultSmileyPack();
    }

    public void reloadSmiliesPack() {
        Resources.reloadResources();
    }
}
