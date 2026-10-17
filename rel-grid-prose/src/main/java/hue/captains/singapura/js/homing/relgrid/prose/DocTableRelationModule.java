package hue.captains.singapura.js.homing.relgrid.prose;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * What a doc's table says, as a relation: its columns, its rows by identity, a value for every cell, and its caption -
 * judged whole when made, and frozen. Pure: no element, no branch. What a doc's table may say is said here and only
 * here, so a new invariant is a new check in its constructor, and neither the caller nor the grid changes;
 * {@code ProseTable} takes nothing else.
 */
public record DocTableRelationModule() implements EsModule<DocTableRelationModule> {

    public static final DocTableRelationModule INSTANCE = new DocTableRelationModule();

    public record DocTableRelation() implements Exportable._Class<DocTableRelationModule> {}

    @Override public ImportsFor<DocTableRelationModule> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<DocTableRelationModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DocTableRelation())); }
}
