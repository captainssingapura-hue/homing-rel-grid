// =============================================================================
// MinesweeperBoard — the game, and nothing it is drawn by (RFC 0050, appendix
// Keyboard Minesweeper): a board of rows by columns, its mines laid at the first
// reveal - never under it nor beside it - and the four moves. DOMAIN CODE: pure,
// it touches nothing on a page.
//
//   new MinesweeperBoard({ rows?, cols?, mines?, seed? })   9 by 9, 10 mines, a seeded draw for tests
//   reveal(r, c) / flag(r, c) / chord(r, c)   → the positions whose tile changed, [{ r, c }]
//   restart()                                   → every position
//   tile(r, c)   → { shown, count } - shown: hidden | flagged | revealed | mine | boom | wrong
//   state()      → ready | playing | won | lost        minesLeft() → mines less flags
//
// A REVEAL of a tile with no mine beside it opens its neighbours, and theirs, as
// far as the empty ground runs. A reveal of a mine is lost: that tile booms,
// every other mine is shown, and a flag that was wrong says so. A CHORD on a
// revealed tile whose flagged neighbours number its count reveals the rest of
// its neighbours. Every non-mine revealed is won, and the mines are flagged. A
// board won or lost takes no move but restart.
// =============================================================================

class MinesweeperBoard {

    constructor(opts) {
        var o = opts || {};
        this._R = o.rows || 9;
        this._C = o.cols || 9;
        this._M = o.mines || 10;
        if (this._M >= this._R * this._C - 9) throw new Error("[MinesweeperBoard] too many mines for a " + this._R + " by " + this._C + " board");
        this._seed = (o.seed === undefined) ? null : o.seed >>> 0;
        this.restart();
    }

    rows() { return this._R; }
    cols() { return this._C; }
    mines() { return this._M; }
    state() { return this._state; }
    minesLeft() { return this._M - this._flags; }

    /** A fresh board, its mines not yet laid. Every position changes. */
    restart() {
        var n = this._R * this._C;
        this._mine = new Array(n).fill(false);
        this._shown = new Array(n).fill("hidden");
        this._count = new Array(n).fill(0);
        this._flags = 0;
        this._left = n - this._M;               // tiles to reveal before the board is won
        this._state = "ready";
        this._rand = MinesweeperBoard._random(this._seed);
        var all = [];
        for (var r = 0; r < this._R; r++) for (var c = 0; c < this._C; c++) all.push({ r: r, c: c });
        return all;
    }

    tile(r, c) {
        var k = this._at(r, c);
        return Object.freeze({ shown: this._shown[k], count: this._count[k] });
    }

    reveal(r, c) {
        var k = this._at(r, c), changed = [];
        if (this._over() || this._shown[k] !== "hidden") return changed;
        if (this._state === "ready") this._lay(r, c);
        if (this._mine[k]) { this._lose(k, changed); return changed; }
        this._open(r, c, changed);
        if (this._left === 0) this._win(changed);
        return changed;
    }

    flag(r, c) {
        var k = this._at(r, c);
        if (this._over()) return [];
        if (this._shown[k] === "hidden") { this._shown[k] = "flagged"; this._flags++; }
        else if (this._shown[k] === "flagged") { this._shown[k] = "hidden"; this._flags--; }
        else return [];
        return [{ r: r, c: c }];
    }

    chord(r, c) {
        var k = this._at(r, c), self = this, changed = [];
        if (this._over() || this._shown[k] !== "revealed" || this._count[k] === 0) return changed;
        var flagged = 0;
        this._around(r, c, function (rr, cc) { if (self._shown[self._at(rr, cc)] === "flagged") flagged++; });
        if (flagged !== this._count[k]) return changed;
        this._around(r, c, function (rr, cc) {
            if (self._over() || self._shown[self._at(rr, cc)] !== "hidden") return;
            changed.push.apply(changed, self.reveal(rr, cc));
        });
        return changed;
    }

    _over() { return this._state === "won" || this._state === "lost"; }

    _at(r, c) {
        if (r < 0 || c < 0 || r >= this._R || c >= this._C) throw new Error("[MinesweeperBoard] no tile " + r + "," + c);
        return r * this._C + c;
    }

    _around(r, c, fn) {
        for (var dr = -1; dr <= 1; dr++) for (var dc = -1; dc <= 1; dc++) {
            var rr = r + dr, cc = c + dc;
            if ((dr || dc) && rr >= 0 && cc >= 0 && rr < this._R && cc < this._C) fn(rr, cc);
        }
    }

    /** The mines, laid at the first reveal: never on the tile revealed, nor beside it. */
    _lay(r, c) {
        var spare = [], self = this;
        for (var rr = 0; rr < this._R; rr++) for (var cc = 0; cc < this._C; cc++)
            if (Math.abs(rr - r) > 1 || Math.abs(cc - c) > 1) spare.push(this._at(rr, cc));
        for (var m = 0; m < this._M; m++) {
            var pick = m + Math.floor(this._rand() * (spare.length - m));
            var t = spare[m]; spare[m] = spare[pick]; spare[pick] = t;
            this._mine[spare[m]] = true;
        }
        for (var k = 0; k < this._mine.length; k++) {
            var n = 0;
            this._around(Math.floor(k / this._C), k % this._C, function (ar, ac) { if (self._mine[self._at(ar, ac)]) n++; });
            this._count[k] = n;
        }
        this._state = "playing";
    }

    /** A tile opened, and the empty ground around it as far as it runs. */
    _open(r, c, changed) {
        var stack = [[r, c]], self = this;
        while (stack.length) {
            var p = stack.pop(), k = this._at(p[0], p[1]);
            if (this._shown[k] !== "hidden" || this._mine[k]) continue;
            this._shown[k] = "revealed";
            this._left--;
            changed.push({ r: p[0], c: p[1] });
            if (this._count[k] === 0) this._around(p[0], p[1], function (rr, cc) { stack.push([rr, cc]); });
        }
    }

    _lose(boom, changed) {
        this._state = "lost";
        for (var k = 0; k < this._mine.length; k++) {
            var was = this._shown[k], now = was;
            if (k === boom) now = "boom";
            else if (this._mine[k] && was === "hidden") now = "mine";
            else if (!this._mine[k] && was === "flagged") now = "wrong";
            if (now !== was) { this._shown[k] = now; changed.push({ r: Math.floor(k / this._C), c: k % this._C }); }
        }
    }

    _win(changed) {
        this._state = "won";
        for (var k = 0; k < this._mine.length; k++)
            if (this._mine[k] && this._shown[k] === "hidden") {
                this._shown[k] = "flagged";
                this._flags++;
                changed.push({ r: Math.floor(k / this._C), c: k % this._C });
            }
    }

    /** A draw: the browser's, or a seeded one a test can repeat. */
    static _random(seed) {
        if (seed === null) return Math.random;
        var s = seed || 1;
        return function () { s = (Math.imul(s, 1664525) + 1013904223) >>> 0; return s / 4294967296; };
    }
}
