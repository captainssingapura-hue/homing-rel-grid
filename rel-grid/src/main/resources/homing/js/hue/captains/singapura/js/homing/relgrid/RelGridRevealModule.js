// =============================================================================
// RelGridRevealModule — RFC 0050 · Episode 2's VIEWPORT FOLLOW, and the two
// facts about focus and containment the layout asks of the DOM. Geometry
// only: nothing here is minted, painted or remembered.
//
// The keyboard moves the cursor; the scrollport has to move with it, or the
// cursor walks out of the visible band and the grid looks dead. Native
// scrollIntoView({block:'nearest'}) has the right SEMANTICS — the least
// movement, and none when the slot already shows — but it takes no inset,
// and a sticky band (a group's header, one day) would park an upward move
// underneath it. So the same arithmetic is done here, in every ancestor that
// actually scrolls and then in the window. Ported from episode 1, where the
// inset carried the sticky header; here it is zero until something is sticky.
//
// The follow is NOT what focus() does. focus() takes the focus WITHOUT
// scrolling — a table taller than its pane would otherwise be pulled into
// view on every resume, moving the rows under the pointer — and the follow
// scrolls to the CURSOR, the one slot a person is looking at, by the least
// amount. The two are different questions with different answers.
//
//   relGridRevealSlot(el)          the least scroll, in every port and then the window, that shows el
//   relGridHasKeyboard(root)       does root, or something in it, hold the focus?
//   relGridWithin(el, ancestor)    is el inside ancestor, or it?
// =============================================================================

/** The least delta that brings [er] inside the port. A slot TALLER than the
 *  port aligns to its top rather than its bottom: seeing where you are beats
 *  seeing where you end. */
function _hrgDelta(er, top, bottom, left, right) {
    var dy = 0, dx = 0;
    if (er.top < top)            dy = er.top - top;
    else if (er.bottom > bottom) dy = Math.min(er.bottom - bottom, er.top - top);
    if (er.left < left)          dx = er.left - left;
    else if (er.right > right)   dx = Math.min(er.right - right, er.left - left);
    return { dy: dy, dx: dx };
}

/** Does this element actually scroll? Overflowing content is not enough —
 *  overflow:visible spills without scrolling. Where no computed style is to
 *  be had, the overflow measurement stands on its own. */
function _hrgScrolls(el) {
    if (!(el.scrollHeight > el.clientHeight || el.scrollWidth > el.clientWidth)) return false;
    var cs = (typeof window !== "undefined" && window.getComputedStyle) ? window.getComputedStyle(el) : null;
    if (!cs) return true;
    return /auto|scroll|overlay/.test((cs.overflowY || "") + " " + (cs.overflowX || "") + " " + (cs.overflow || ""));
}

function _hrgRevealIn(scroller, el, topInset) {
    var sr = scroller.getBoundingClientRect();
    var top  = sr.top  + (scroller.clientTop  || 0);
    var left = sr.left + (scroller.clientLeft || 0);
    var d = _hrgDelta(el.getBoundingClientRect(), top + topInset, top + scroller.clientHeight, left, left + scroller.clientWidth);
    if (d.dy) scroller.scrollTop  += d.dy;
    if (d.dx) scroller.scrollLeft += d.dx;
}

function _hrgRevealInWindow(el, topInset) {
    if (typeof window === "undefined" || !window.scrollBy) return;
    var w = window.innerWidth || 0, h = window.innerHeight || 0;
    if (!w || !h) return;
    var d = _hrgDelta(el.getBoundingClientRect(), topInset, h, 0, w);
    if (d.dy || d.dx) window.scrollBy(d.dx, d.dy);
}

/**
 * The least scroll that shows an element: in every ancestor that actually
 * scrolls, innermost first, and then the window. Nothing moves when it
 * already shows; nothing at all where there is no geometry.
 */
function relGridRevealSlot(el) {
    if (!el || !el.getBoundingClientRect || typeof document === "undefined") return;
    var inset = 0;                                        // a sticky band's height, when there is one
    var node = el.parentNode;
    while (node && node !== document.body && node !== document.documentElement) {
        if (node.getBoundingClientRect && _hrgScrolls(node)) {
            _hrgRevealIn(node, el, inset);
            inset = 0;                                    // claimed by the innermost port
        }
        node = node.parentNode;
    }
    _hrgRevealInWindow(el, inset);
}

/** Is el inside ancestor, or it? Walks parentNode: the stub's elements have no contains(). */
function relGridWithin(el, ancestor) {
    for (var p = el; p; p = p.parentNode) if (p === ancestor) return true;
    return false;
}

/** Does root have the keyboard — itself, or something in it, holding the focus? */
function relGridHasKeyboard(root) {
    if (typeof document === "undefined") return false;
    var a = document.activeElement;
    if (!a || a === document.body) return false;
    return a === root || relGridWithin(a, root);
}
