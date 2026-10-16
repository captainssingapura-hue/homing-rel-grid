package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.catalogue.gate.CatalogueGate;
import hue.captains.singapura.js.homing.design.styling.InstalledStyling;
import hue.captains.singapura.js.homing.relgrid.house.GridFrameBaseModule;
import hue.captains.singapura.js.homing.ui.styles.HouseStyles;
import org.junit.jupiter.api.BeforeAll;
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

    /** The site's styling, installed as the server installs it at bootstrap: the modules are served in it. */
    @BeforeAll
    static void theSitesStyling_installed() { InstalledStyling.install(HouseStyles.INSTANCE.styling(List.of(GridFrameBaseModule.INSTANCE))); }

    @Test void theCrateIsStructurallyComplete()             { CatalogueGate.structurallyComplete(CRATE); }
    @Test void everyServedModuleKeepsItsLane_strictly()      { CatalogueGate.strict(CRATE); }
    @Test void theCssGraphKeepsItsLaws()                     { CatalogueGate.cssLaws(CRATE); }
    @Test void keysComeThroughTheParty()                     { CatalogueGate.keysThroughTheParty(CRATE); }
    @Test void everyNameTakenIsImported() throws IOException { CatalogueGate.everyNameTakenIsImported(CRATE); }
    @Test void noPlainModuleImportsADomModule()              { CatalogueGate.noPlainModuleImportsADomModule(CRATE); }
    @Test void theOldStudioIsNowhere()                       { CatalogueGate.noOldStudio(CRATE); }
}
