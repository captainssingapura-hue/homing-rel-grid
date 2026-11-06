package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.design.styling.Styling;
import hue.captains.singapura.js.homing.catalogue.gate.CatalogueGate;
import hue.captains.singapura.js.homing.relgrid.house.GridFrameBaseModule;
import hue.captains.singapura.js.homing.workspace.tree.TreeBaseModule;
import hue.captains.singapura.js.homing.docview.widgets.DocViewBaseModule;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

/**
 * The workbench's gate, strict as the new stack's sites have it: every served module declared,
 * graded under its lane on the text the server serves - its emitted exports, its widget bodies -
 * with no ledger; keys through the party; every name taken imported; no plain module importing a
 * DOM module; the old studio nowhere.
 */
class GridWorkbenchGateTest {

    private static final RelGridWorkbenchCrate CRATE = RelGridWorkbenchCrate.INSTANCE;

    /** What the test serves and reads from. */
    private static final Styling STYLING = HouseStyles.INSTANCE.styling(List.of(GridFrameBaseModule.INSTANCE, MinesweeperBaseModule.INSTANCE, TreeBaseModule.INSTANCE, DocViewBaseModule.INSTANCE));

    @Test void theCrateIsStructurallyComplete()             { CatalogueGate.structurallyComplete(CRATE); }
    @Test void everyServedModuleKeepsItsLane_strictly()      { CatalogueGate.strict(CRATE); }
    @Test void theCssGraphKeepsItsLaws()                     { CatalogueGate.cssLaws(CRATE); }
    @Test void keysComeThroughTheParty()                     { CatalogueGate.keysThroughTheParty(CRATE); }
    @Test void everyNameTakenIsImported() throws IOException { CatalogueGate.everyNameTakenIsImported(CRATE); }
    @Test void noPlainModuleImportsADomModule()              { CatalogueGate.noPlainModuleImportsADomModule(CRATE); }
    @Test void theOldStudioIsNowhere()                       { CatalogueGate.noOldStudio(CRATE); }
}
