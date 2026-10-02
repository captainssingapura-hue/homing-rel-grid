// =============================================================================
// HanEditor — the Han Article bench's editor: the article as plain text above,
// and as square slots below it; every keystroke told to the han-article party.
// One per bench. What it is otherwise is a HanArticleView's.
//
//   new HanEditor(container, params)   params: none
// =============================================================================

class HanEditor extends HanArticleView {
    constructor(container, params) { super(container, params, true); }
}
