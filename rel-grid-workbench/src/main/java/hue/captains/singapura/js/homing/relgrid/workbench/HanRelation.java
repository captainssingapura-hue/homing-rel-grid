package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * A {@code RootRelationContract}-shaped relation over a {@link HanStore}: the
 * article laid out by {@link HanLayout} into rows of square slots, and a cell
 * manager that owns one {@link HanCellModule.HanCell} per slot. Identity is
 * positional — the square — and the glyph is what the square shows; on every
 * change the relation re-lays the article out and sets its own cells. It
 * declares a capacity of rows and presents the prefix in use; it declares the
 * two half-square columns, leading and trailing, and presents whichever the
 * layout has put a mark in. Nothing here mentions a grid, and nothing here
 * commits: the cells are displays.
 */
public record HanRelation() implements DomModule<HanRelation> {

    public record createHanRelation() implements Exportable._Constant<HanRelation> {}

    public static final HanRelation INSTANCE = new HanRelation();

    @Override
    public ImportsFor<HanRelation> imports() {
        return ImportsFor.<HanRelation>builder()
                .add(new ModuleImports<>(List.of(new HanLayout.hanLayout()), HanLayout.INSTANCE))
                .add(new ModuleImports<>(List.of(new HanCellModule.HanCell()), HanCellModule.INSTANCE))
                .build();
    }

    @Override public ExportsOf<HanRelation> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createHanRelation()));
    }
}
