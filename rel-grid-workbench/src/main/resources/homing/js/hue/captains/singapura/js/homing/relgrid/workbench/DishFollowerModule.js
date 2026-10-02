// =============================================================================
// DishFollower — a follower - a read-only replica of the bench's dishes; dock several. What it is otherwise is a DishTable's.
//
//   new DishFollower(container, params)   params: none
// =============================================================================

class DishFollower extends DishTable {
    constructor(container, params) { super(container, params, "follower"); }
}
