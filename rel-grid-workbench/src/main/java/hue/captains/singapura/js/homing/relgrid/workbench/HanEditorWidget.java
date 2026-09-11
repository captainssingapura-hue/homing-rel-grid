package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/** The editor: every square commits to the shared article. One instance. */
public final class HanEditorWidget extends WorkspaceWidget<WorkspaceWidget._None, HanEditorWidget> {

    public static final HanEditorWidget INSTANCE = new HanEditorWidget();

    private HanEditorWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, HanEditorWidget> {}

    @Override protected _Construct<_None, HanEditorWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Editor"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() { return HanArticle.imports(); }

    @Override
    protected List<String> constructBodyJs() { return HanArticle.bodyJs(true); }
}
