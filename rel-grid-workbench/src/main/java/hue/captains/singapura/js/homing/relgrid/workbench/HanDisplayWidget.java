package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.LifecycleHint;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;

import java.util.List;

/** A read-only display over the same article. It moves when the editor commits, and its grid is never told. */
public final class HanDisplayWidget extends WorkspaceWidget<WorkspaceWidget._None, HanDisplayWidget> {

    public static final HanDisplayWidget INSTANCE = new HanDisplayWidget();

    private HanDisplayWidget() {}

    private record construct() implements WorkspaceWidget._Construct<_None, HanDisplayWidget> {}

    @Override protected _Construct<_None, HanDisplayWidget> construct() { return new construct(); }
    @Override public Class<_None> paramsType() { return _None.class; }
    @Override public String title() { return "Display"; }
    @Override public LifecycleHint lifecycleHint() { return LifecycleHint.MULTI; }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() { return HanArticle.imports(); }

    @Override
    protected List<String> constructBodyJs() { return HanArticle.bodyJs(false); }
}
