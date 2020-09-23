package org.jivesoftware.smackx.workgroup.ext.macros;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MacroGroup {
    private List<MacroGroup> macroGroups = new ArrayList();
    private List<Macro> macros = new ArrayList();
    private String title;

    public void addMacro(Macro macro) {
        this.macros.add(macro);
    }

    public void removeMacro(Macro macro) {
        this.macros.remove(macro);
    }

    public Macro getMacroByTitle(String title) {
        for (Macro macro : Collections.unmodifiableList(this.macros)) {
            if (macro.getTitle().equalsIgnoreCase(title)) {
                return macro;
            }
        }
        return null;
    }

    public void addMacroGroup(MacroGroup group) {
        this.macroGroups.add(group);
    }

    public void removeMacroGroup(MacroGroup group) {
        this.macroGroups.remove(group);
    }

    public Macro getMacro(int location) {
        return (Macro) this.macros.get(location);
    }

    public MacroGroup getMacroGroupByTitle(String title) {
        for (MacroGroup group : Collections.unmodifiableList(this.macroGroups)) {
            if (group.getTitle().equalsIgnoreCase(title)) {
                return group;
            }
        }
        return null;
    }

    public MacroGroup getMacroGroup(int location) {
        return (MacroGroup) this.macroGroups.get(location);
    }

    public List<Macro> getMacros() {
        return this.macros;
    }

    public void setMacros(List<Macro> macros) {
        this.macros = macros;
    }

    public List<MacroGroup> getMacroGroups() {
        return this.macroGroups;
    }

    public void setMacroGroups(List<MacroGroup> macroGroups) {
        this.macroGroups = macroGroups;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toXML() {
        StringBuilder buf = new StringBuilder();
        buf.append("<macrogroup>");
        buf.append("<title>" + getTitle() + "</title>");
        buf.append("<macros>");
        for (Macro macro : getMacros()) {
            buf.append("<macro>");
            buf.append("<title>" + macro.getTitle() + "</title>");
            buf.append("<type>" + macro.getType() + "</type>");
            buf.append("<description>" + macro.getDescription() + "</description>");
            buf.append("<response>" + macro.getResponse() + "</response>");
            buf.append("</macro>");
        }
        buf.append("</macros>");
        if (getMacroGroups().size() > 0) {
            buf.append("<macroGroups>");
            for (MacroGroup groups : getMacroGroups()) {
                buf.append(groups.toXML());
            }
            buf.append("</macroGroups>");
        }
        buf.append("</macrogroup>");
        return buf.toString();
    }
}
