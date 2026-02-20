package io.github.georgeakulov.json_schema.common.unicode;

public class Punycode {

    private static final int BASE = 36;
    private static final int TMIN = 1;
    private static final int TMAX = 26;
    private static final int SKEW = 38;
    private static final int DAMP = 700;

    private static final int INITIAL_BIAS = 72;
    private static final int INITIAL_N = 0x80;

    private static final int ACE_MAX_LENGTH = 256;
    private static final int DELIMITER = 0x2D; // Hyphe

    public static String decode(String input)  {
        StringBuilder output = new StringBuilder();

        int n = INITIAL_N;
        int i = 0;
        int bias = INITIAL_BIAS;

        // handle the basic code points at the start of the label
        // let b the number of input code points before the last delimiter or 0 if there is none
        // then copy the first b code pints to the output
        if (ACE_MAX_LENGTH * 2 < input.length()) {
            throw new PunycodeException("Output would exceed space");
        }

        // b marks the delimiter character position
        int b = Math.max(0, input.lastIndexOf(DELIMITER));
        if (b > ACE_MAX_LENGTH) {
            throw new PunycodeException("Output would exceed space");
        }

        // copy the basic code points until delimiter character
        for (int j = 0; j < b; j++) {
            char c = input.charAt(j);
            if (!isBasic(c)) {
                throw new PunycodeException("Invalid input character");
            }
            output.append(c);
        }

        // Main decoding loop: Start just after the last delimiter if any basic code points were copied.
        // or if not start at the beginning otherwise
        int index = (b > 0) ? b + 1 : 0;
        while (index < input.length()){
            // Decode a generalize variable-length integer into delta, which gets added to i.
            // the overflow checking is easier if we increase i as we go, then subtract off its
            // startig value at the end to obtain delta
            int w = 1;
            int oldi = i;
            for (int k = BASE; ; k += BASE) {
                if (index >= input.length()) {
                    throw new PunycodeException("Input is invalid!");
                }
                int codepoint = input.charAt(index++);
                int digit = decodeDigit(codepoint);
                if (digit >= BASE || digit > (Integer.MAX_VALUE - i) / w) {
                    throw new PunycodeException("Overflow");
                }

                i += digit * w;
                int t = (k <= bias) ? TMIN : k >= bias + TMAX ? TMAX : k - bias;

                if (digit < t) break;
                if (w > Integer.MAX_VALUE / (BASE - t)) {
                    throw new PunycodeException("Input needs wider integers");
                }
                w *= (BASE - t);
            }

            int out = output.length();
            bias = adapt(i - oldi, out + 1, oldi == 0);

            // 'i' was supposed to wrap from output.length + 1 to 0,
            // incrementing n each time, so we'll fix that now
            if (i / (out + 1) > Integer.MAX_VALUE - n) {
                throw new PunycodeException("Input needs wider integers");
            }
            n = n + i / (out + 1);
            i = i % (out + 1);

            if (out >= ACE_MAX_LENGTH) {
                throw new PunycodeException("Output would exceed space");
            }

            output.insert(i, (char)n);
            i++;
        }

        return output.toString();
    }

    private static int decodeDigit(int codepoint) {
        if (codepoint - 48 < 10) return codepoint - 22;
        if (codepoint - 65 < 26) return codepoint - 65;
        if (codepoint - 97 < 26) return codepoint - 97;
        return BASE;
    }

    private static boolean isBasic(int codepoint) {
        return (codepoint < 0x80);
    }

    private static int adapt(int delta, int numpoints, boolean firsttime) {
        delta = firsttime ? delta / DAMP : (delta >> 1);
        delta += delta / numpoints;

        int difference = BASE - TMIN;
        int k = 0;
        while (delta > ((difference * TMAX) >> 1)) {
            delta /= difference;
            k += BASE;
        }
        return k + (difference + 1) * delta / (delta + SKEW);
    }

    private static class PunycodeException extends RuntimeException {
        public PunycodeException(String message) {
            super(message);
        }
    }
}
