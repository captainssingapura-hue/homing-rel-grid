package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.workspace.site.GroupedWorkspacePageModule;

import java.util.List;

/**
 * The benches' page: the grouped workspace page over {@link Benches#SITE} - the bench its anchor
 * names, each bench a switch away, its states kept by the server.
 */
public record GridBenchesApp() implements AppModule<GroupedWorkspacePageModule.Params, GridBenchesApp> {

    public static final GridBenchesApp INSTANCE = new GridBenchesApp();

    record appMain() implements AppModule._AppMain<GroupedWorkspacePageModule.Params, GridBenchesApp> {}

    @Override public String title()      { return "Grid benches"; }
    @Override public String simpleName() { return "grid-benches"; }
    @Override public Class<GroupedWorkspacePageModule.Params> paramsType() { return GroupedWorkspacePageModule.Params.class; }
    @Override public ParamCodec<GroupedWorkspacePageModule.Params> paramCodec() { return GroupedWorkspacePageModule.CODEC; }

    @Override
    public ImportsFor<GridBenchesApp> imports() {
        return ImportsFor.<GridBenchesApp>builder()
                .add(new ModuleImports<>(List.of(new GroupedWorkspacePageModule.GroupedWorkspacePage()), GroupedWorkspacePageModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchesModule.WORKBENCH_WORKSPACES()), BenchesModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new BenchLayoutModule.WORKBENCH_GROUPS(), new BenchLayoutModule.WORKBENCH_ARRANGEMENTS()), BenchLayoutModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<GridBenchesApp> exports() { return new ExportsOf<>(INSTANCE, List.of(new appMain())); }
}
