package hue.captains.singapura.js.homing.relgrid;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridViewMaps}: the identity/position seam, PURE
 * logic. The only place {@code (i, j)} meets {@code (pk, column)}.
 *
 * <p>The row axis is the <b>View</b> and has no base: the rows presented, in
 * order, and no other list — so a grid over a relation of any size holds as
 * many keys as it shows, and every check here is O(rows shown). Membership is
 * the relation's: it refuses an identity it does not own at {@code cellFor},
 * the facade asks for every identity before a slot moves, and a refused remap
 * is undone here and rethrown — half a View is not a View. The column axis is
 * structure and stays listed; a column view is a subset of it.</p>
 */
public record RelGridViewMapsModule() implements DomModule<RelGridViewMapsModule> {

    public record RelGridViewMaps() implements Exportable._Constant<RelGridViewMapsModule> {}

    public static final RelGridViewMapsModule INSTANCE = new RelGridViewMapsModule();

    @Override public ImportsFor<RelGridViewMapsModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<RelGridViewMapsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridViewMaps()));
    }
}
