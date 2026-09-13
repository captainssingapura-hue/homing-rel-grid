package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The Outlets bench's <b>ledger</b> — the same six dishes sold at three
 * outlets, each outlet its own book. Pure domain code, in memory: it holds
 * what was sold where, derives and publishes each outlet's totals, and tells
 * its subscribers what changed. Nothing is edited; a sale is the only thing
 * that moves it, which is what makes it the specimen the live-feed round
 * will feed.
 */
public record SalesStore() implements DomModule<SalesStore> {

    public record createSalesStore() implements Exportable._Constant<SalesStore> {}
    public record salesStoreShared() implements Exportable._Constant<SalesStore> {}

    public static final SalesStore INSTANCE = new SalesStore();

    @Override public ImportsFor<SalesStore> imports() { return ImportsFor.noImports(); }

    @Override public ExportsOf<SalesStore> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createSalesStore(), new salesStoreShared()));
    }
}
