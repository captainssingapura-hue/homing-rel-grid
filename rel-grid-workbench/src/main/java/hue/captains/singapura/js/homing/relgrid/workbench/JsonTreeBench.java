package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.groups.core.models.ArrangedWidget;
import hue.captains.singapura.js.homing.workspace.groups.core.models.Arrangement;
import hue.captains.singapura.js.homing.workspace.groups.core.models.SplitGrid;
import hue.captains.singapura.js.homing.workspace.groups.core.models.WorkspaceArrangements;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;
import hue.captains.singapura.js.homing.workspace.widgets.NoParams;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetDeclaration;
import hue.captains.singapura.js.homing.workspace.widgets.WidgetQuery;
import hue.captains.singapura.js.homing.workspace.widgets.WorkspaceDeclaration;

import java.util.List;

/**
 * The JSON Tree bench: the JSON kit's viewer over one document, typed in beside it - the input
 * and the display, each knowing nothing of the other, meeting in the {@code json-text} party.
 */
public final class JsonTreeBench {

    private JsonTreeBench() {}

    /** The input, declared: {@code json-input}, joining the JSON text party. */
    public record Input() implements WidgetDeclaration<NoParams> {
        public static final Input INSTANCE = new Input();
        @Override public String kind() { return "json-input"; }
        @Override public String title() { return "JSON input"; }
        /** One input: the bench has one document, and one place it is typed. */
        @Override public boolean single() { return true; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(JsonText.TYPE); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new JsonInputModule.JsonInput()), JsonInputModule.INSTANCE);
        }
    }

    /** The display, declared: {@code json-display}, joining the JSON text party. */
    public record Display() implements WidgetDeclaration<NoParams> {
        public static final Display INSTANCE = new Display();
        @Override public String kind() { return "json-display"; }
        @Override public String title() { return "JSON tree"; }
        @Override public Class<NoParams> paramsType() { return NoParams.class; }
        @Override public WidgetQuery<NoParams> query() { return new NoParams.Query(); }
        @Override public List<PartyType<?>> parties() { return List.of(JsonText.TYPE); }
        @Override public ModuleImports<?> constructs() {
            return new ModuleImports<>(List.of(new JsonDisplayModule.JsonDisplay()), JsonDisplayModule.INSTANCE);
        }
    }

    /** The bench, a workspace: {@code json-tree}. */
    public record Workspace() implements WorkspaceDeclaration {
        public static final Workspace INSTANCE = new Workspace();
        @Override public String name() { return "json-tree"; }
        @Override public List<WidgetDeclaration<?>> kinds() { return List.of(Input.INSTANCE, Display.INSTANCE); }
    }

    /** The first time: the input on the left, the tree beside it, half each. */
    public static final WorkspaceArrangements<Workspace> ARRANGED = WorkspaceArrangements.of(Workspace.INSTANCE,
            Arrangement.of(Workspace.INSTANCE,
                    SplitGrid.of(SplitGrid.row(SplitGrid.region("input", "input"), SplitGrid.region("tree", "tree"))),
                    ArrangedWidget.of("input", Input.INSTANCE.kind()),
                    ArrangedWidget.of("tree", Display.INSTANCE.kind())));
}
