package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.studio.base.ClasspathMarkdownDoc;
import hue.captains.singapura.js.homing.studio.base.Reference;

import java.util.List;
import java.util.UUID;

/** Landing page for the grid workbench studio — what a bench is, how to use the one there is, how to add one. */
public record GridWorkbenchIntroDoc() implements ClasspathMarkdownDoc {
    private static final UUID ID = UUID.fromString("d72469da-aa28-4e93-a9ef-215bc91a3ba5");
    public static final GridWorkbenchIntroDoc INSTANCE = new GridWorkbenchIntroDoc();

    @Override public UUID   uuid()    { return ID; }
    @Override public String title()   { return "The Grid Workbenches"; }
    @Override public String summary() { return "A studio of its own for benching the Relation Grid. A bench is a workspace of specimens a person docks side by side, and each bench asks one question a demo cannot. Replicating Tables asks whether a grid that holds no value needs telling when a value changes — and shows that it does not."; }
    @Override public String category(){ return "INTRO"; }

    @Override public List<Reference> references() {
        return List.of();
    }
}
