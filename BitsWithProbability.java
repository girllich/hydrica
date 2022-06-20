package hydrica;
import java.util.Random;
public class BitsWithProbability {
  public BitsWithProbability(float prob, Random rnd) {
    this.rnd = rnd;

    if (prob <= 0f) {
      zero = true;
      return;
    }

    // Decode IEEE float
    int probBits = Float.floatToIntBits(prob);
    mantissa = probBits & 0x7FFFFF;
    exponent = probBits >>> 23;
    if (exponent > 0)
      mantissa |= 0x800000;
    exponent -= 150;

    // Convert prob to the form m * 2**e
    int ntz = Integer.numberOfTrailingZeros(mantissa);
    mantissa >>= ntz;
    exponent += ntz;
  }

  /**
   *Determine how many random words we need from the system RNG to
   *  generate one output word with probability P.
   **/
  public int iterationCount() { return -exponent; }

  /** Generate a random number with the desired probability */
  public long nextLong() {
    if (zero)
      return 0L;

    long acc = -1L;
    int shiftReg = mantissa - 1;
    for (int bit = exponent; bit < 0; ++bit) {
      if ((shiftReg & 1) == 0) {
        acc &= rnd.nextLong();
      } else {
        acc |= rnd.nextLong();
      }
      shiftReg >>= 1;
    }
    return acc;
  }

  /** Value of <code>prob</code>, represented as m * 2**e */
  private int exponent;
  private int mantissa;

  /** Random data source */
  private final Random rnd;

  /** Zero flag (special case) */
  private boolean zero;
}
