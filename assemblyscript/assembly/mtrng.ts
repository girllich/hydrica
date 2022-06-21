// ============================================================================
//   Copyright 2006-2012 Daniel W. Dyer
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
// ============================================================================
const SEED_SIZE_INTS : i32 = 4;

  // Magic numbers from original C version.
const N : i32 = 624;
const M : i32 = 397;
const MAG01 : StaticArray<i32> = StaticArray.fromArray<i32>([0, 0x9908b0df]);
const UPPER_MASK : i32 = 0x80000000;
const LOWER_MASK : i32 = 0x7fffffff;
const BOOTSTRAP_SEED : i32 = 19650218;
const BOOTSTRAP_FACTOR : i32 = 1812433253;
const SEED_FACTOR1 : i32 = 1664525;
const SEED_FACTOR2 : i32 = 1566083941;
const GENERATE_MASK1 : i32 = 0x9d2c5680;
const GENERATE_MASK2 : i32 = 0xefc60000;


export class MersenneTwisterRNG {
  // The actual seed size isn't that important, but it should be a multiple
  // of 4.
  
  mt : StaticArray<i32> = new StaticArray<i32>(N); // State vector.
  mtIndex : i32 = 0;             // Index i32o state vector.

  public constructor(seedi32s:StaticArray<i32>) {
   
    // This section is translated from the init_genrand code in the C version.
    this.mt[0] = BOOTSTRAP_SEED;
    for (this.mtIndex = 1; this.mtIndex < N; this.mtIndex++) {
      this.mt[this.mtIndex] =
          (BOOTSTRAP_FACTOR * (this.mt[this.mtIndex - 1] ^ (this.mt[this.mtIndex - 1] >>> 30)) +
           this.mtIndex);
    }

    // This section is translated from the init_by_array code in the C version.
    var i : i32  = 1;
    var j : i32  = 0;
    var k : i32;
    for (k = max<i32>(N, seedi32s.length); k > 0; k--) {
      this.mt[i] = (this.mt[i] ^ ((this.mt[i - 1] ^ (this.mt[i - 1] >>> 30)) * SEED_FACTOR1)) +
              seedi32s[j] + j;
      i++;
      j++;
      if (i >= N) {
        this.mt[0] = this.mt[N - 1];
        i = 1;
      }
      if (j >= seedi32s.length) {
        j = 0;
      }
    }
    for (k = N - 1; k > 0; k--) {
      this.mt[i] = (this.mt[i] ^ ((this.mt[i - 1] ^ (this.mt[i - 1] >>> 30)) * SEED_FACTOR2)) - i;
      i++;
      if (i >= N) {
        this.mt[0] = this.mt[N - 1];
        i = 1;
      }
    }
    this.mt[0] = UPPER_MASK; // Most significant bit is 1 - guarantees non-zero
                        // initial array.
  }

  /**
   * {@inheritDoc}
   */
  
  protected next(bits:i32) : i32 {
    var y : i32;  
    {
      if (this.mtIndex >= N) // Generate N i32s at a time.
      {
        var kk : i32;
        for (kk = 0; kk < N - M; kk++) {
          y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
          this.mt[kk] = this.mt[kk + M] ^ (y >>> 1) ^ MAG01[y & 0x1];
        }
        for (; kk < N - 1; kk++) {
          y = (this.mt[kk] & UPPER_MASK) | (this.mt[kk + 1] & LOWER_MASK);
          this.mt[kk] = this.mt[kk + (M - N)] ^ (y >>> 1) ^ MAG01[y & 0x1];
        }
        y = (this.mt[N - 1] & UPPER_MASK) | (this.mt[0] & LOWER_MASK);
        this.mt[N - 1] = this.mt[M - 1] ^ (y >>> 1) ^ MAG01[y & 0x1];

        this.mtIndex = 0;
      }

      y = this.mt[this.mtIndex++];
    }

    // Tempering
    y ^= (y >>> 11);
    y ^= (y << 7) & GENERATE_MASK1;
    y ^= (y << 15) & GENERATE_MASK2;
    y ^= (y >>> 18);

    return y >>> (32 - bits);
  }
}

export function invoke(a: i32, b: i32, c:i32, d:i32, e:i32): i32 {
   var mers = new MersenneTwisterRNG(StaticArray.fromArray<i32>([a,b,c,d]));
   return mers.next(e);
}