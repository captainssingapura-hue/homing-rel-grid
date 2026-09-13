package hue.captains.singapura.js.homing.relgrid.group;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.relgrid.RelGridModule;
import hue.captains.singapura.js.homing.relgrid.RelGridStyles;

import java.util.List;

/**
 * RFC 0050 · Episode 2 — {@code RelGridGroupMint}: the group's minting. The
 * boxes and fence slots on the group's own branch, the domain's fence placed
 * in each slot as a cell's element is placed in a table's, the group's own
 * header grid in 'group' mode, and the ordinary {@code RelGrid} each member is
 * built from — its options verbatim but for the container, the branch (a
 * sub-branch of the group's, the grid's to activate), the header and the
 * reports the group listens to. Imports the grid and the looks it applies.
 */
public record RelGridGroupMintModule() implements DomModule<RelGridGroupMintModule> {

    public record RelGridGroupMint() implements Exportable._Constant<RelGridGroupMintModule> {}

    public static final RelGridGroupMintModule INSTANCE = new RelGridGroupMintModule();

    @Override
    public ImportsFor<RelGridGroupMintModule> imports() {
        return ImportsFor.<RelGridGroupMintModule>builder()
                .add(new ModuleImports<>(List.of(new RelGridModule.RelGrid()), RelGridModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridGroupStyles.hrg_member(), new RelGridGroupStyles.hrg_group_header(), new RelGridGroupStyles.hrg_group_header_sticky(),
                        new RelGridGroupStyles.hrg_fence(), new RelGridGroupStyles.hrg_fence_empty()), RelGridGroupStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new RelGridStyles.hrg_lit()), RelGridStyles.INSTANCE))   // a fence lights under the focus
                .build();
    }

    @Override public ExportsOf<RelGridGroupMintModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new RelGridGroupMint()));
    }
}
