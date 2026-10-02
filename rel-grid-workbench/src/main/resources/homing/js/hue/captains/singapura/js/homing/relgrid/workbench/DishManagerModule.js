// =============================================================================
// DishManager — the shop manager's table - edits price, and runs a day of trade. What it is otherwise is a DishTable's.
//
//   new DishManager(container, params)   params: none
// =============================================================================

class DishManager extends DishTable {
    constructor(container, params) { super(container, params, "manager"); }
}
