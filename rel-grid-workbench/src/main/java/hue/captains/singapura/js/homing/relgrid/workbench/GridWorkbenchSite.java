package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.catalogue.site.AppListing;
import hue.captains.singapura.js.homing.designs.HomingDesigns;
import hue.captains.singapura.js.homing.site.Path;
import hue.captains.singapura.js.homing.site.Router;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.catalogue.CatalogueRouter;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.StandardMpa;

/**
 * The workbench's site: its catalogue at the root, listed by the catalogue app, served through
 * the standard MPA under its own brand, with the framework's designs.
 */
public record GridWorkbenchSite() implements Site {

    public static final GridWorkbenchSite INSTANCE = new GridWorkbenchSite();

    /** The site's one MPA: its brand, the designs it offers, and the crate it serves. */
    public static final StandardMpa MPA = StandardMpa.of(Brand.of("Homing · grid benches"), HomingDesigns.REGISTRY, RelGridWorkbenchCrate.INSTANCE);

    /** Read once: the tree is checked when the site is made, not when a request arrives. */
    public static final CatalogueRouter ROUTER = CatalogueRouter.at(Path.ROOT, GridWorkbenchCatalogue.INSTANCE, MPA).listing(AppListing.INSTANCE);

    @Override public String name() { return "rel-grid-workbench"; }
    @Override public Router router() { return ROUTER; }
}
