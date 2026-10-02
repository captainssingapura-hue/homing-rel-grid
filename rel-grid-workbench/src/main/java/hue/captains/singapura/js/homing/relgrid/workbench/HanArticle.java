package hue.captains.singapura.js.homing.relgrid.workbench;

import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.workspace.parties.PartyType;

import java.util.List;

/**
 * What a Han article party carries: the one article the Han Article bench's editor and displays
 * share, kept across visits by the party's steward. A member does - {@link SetText}, {@link
 * Reset}, {@link CurrentRequested}; the party says - {@link Text}, the article and its revision,
 * to every member after every change and to a member that asks. Between the secretary and the
 * steward: {@link Load} and {@link Loaded} when the article is first asked for, {@link Save}
 * after every change.
 */
public sealed interface HanArticle {

    /** A member edited: this is the article now. */
    record SetText(String text) implements HanArticle {}

    /** A member asks for the poem back. */
    record Reset() implements HanArticle {}

    /** A member asks for the article - one that joins late - and is answered alone, once there is one. */
    record CurrentRequested() implements HanArticle {}

    /** The party says: this is the article, at this revision. */
    record Text(String text, int revision) implements HanArticle {}

    /** The secretary asks the steward for the article kept. */
    record Load() implements HanArticle {}

    /** The steward answers: the article kept, when there was one. */
    record Loaded(String text, boolean found) implements HanArticle {}

    /** The secretary asks the steward to keep the article. */
    record Save(String text) implements HanArticle {}

    /**
     * The type: {@code han-article}, served as {@code HAN_ARTICLE}; its root's secretary, unless
     * a workspace puts its own, {@code HanArticleSecretary}; its steward, which keeps the article
     * in the browser's storage, {@code HanArticleSteward}.
     */
    PartyType<HanArticle> TYPE = new PartyType<>("han-article", HanArticle.class)
            .servedFrom(new ModuleImports<>(List.of(new HanArticleModule.HAN_ARTICLE()), HanArticleModule.INSTANCE))
            .withSecretary(new ModuleImports<>(List.of(new HanArticleSecretaryModule.HanArticleSecretary()), HanArticleSecretaryModule.INSTANCE))
            .withSteward(new ModuleImports<>(List.of(new HanArticleStewardModule.HanArticleSteward()), HanArticleStewardModule.INSTANCE));
}
