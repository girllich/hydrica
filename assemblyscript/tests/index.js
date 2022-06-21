import assert from "assert";
import { invoke } from "../build/debug.js";
assert.strictEqual(invoke(1, 2, 3, 4, 32), -1379261062);
console.log("ok");
