package org.gasoft.json_schema.common;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

class Ipv6Validator implements Predicate<String> {

    @Override
    public boolean test(String s) {
        return parseIpv6String(s) != null;
    }

    // from here https://gist.github.com/wkgcass/0bec457a6737a54f2f1025ed779118aa
    public static byte[] parseIpv6String(String ipv6) {
        if (ipv6.startsWith("[") && ipv6.endsWith("]")) {
            ipv6 = ipv6.substring(1, ipv6.length() - 1);
        }
        { // check how many ::
            int count = split(ipv6, "::").length - 1;
            if (count > 1)
                return null; // at most 1
        }
        boolean hasDblColon;
        String colonOnly;
        String colonAndDot;
        {
            int idx = ipv6.indexOf("::");
            if (idx == -1) {
                hasDblColon = false;
                colonOnly = null;
                colonAndDot = ipv6;
            } else {
                hasDblColon = true;
                colonOnly = ipv6.substring(0, idx);
                colonAndDot = ipv6.substring(idx + "::".length());
            }
        }
        byte[] ipBytes = new byte[16];
        int consumed = parseIpv6ColonPart(colonOnly, ipBytes, 0);
        if (consumed == -1)
            return null; // parse failed
        int consumed2 = parseIpv6LastBits(colonAndDot, ipBytes);
        if (consumed2 == -1)
            return null; // parse failed
        if (hasDblColon) {
            if (consumed + consumed2 >= 16)
                return null; // wrong len
        } else {
            if (consumed + consumed2 != 16)
                return null; // wrong len
        }
        return ipBytes;
    }

    // -1 for fail
    private static int parseIpv6LastBits(String s, byte[] ipBytes) {
        if (s.contains(".")) {
            int idx = s.indexOf('.');
            idx = s.lastIndexOf(':', idx);
            if (idx == -1) {
                return parseIpv4String(s, ipBytes, ipBytes.length - 4);
            } else {
                String colonPart = s.substring(0, idx);
                String dotPart = s.substring(idx + 1);
                int r = parseIpv4String(dotPart, ipBytes, ipBytes.length - 4);
                if (r == -1) {
                    return -1; // wrong len or parse failed
                }
                return 4 + parseIpv6ColonPart(colonPart, ipBytes, ipBytes.length - 4 - split(colonPart, ":").length * 2);
            }
        } else {
            return parseIpv6ColonPart(s, ipBytes, ipBytes.length - split(s, ":").length * 2);
        }
    }

    // -1 for fail
    private static int parseIpv6ColonPart(String s, byte[] ipBytes, int fromIdx) {
        if (s == null || s.isEmpty())
            return 0;
        if (fromIdx < 0)
            return -1;
        // only hex number splited by `:`
        String[] split = split(s, ":");
        for (int i = 0; i < split.length; ++i) {
            int baseIndex = fromIdx + 2 * i; // every field occupy 2 bytes
            if (baseIndex >= ipBytes.length) {
                return -1; // too long
            }
            char[] field = split[i].toCharArray();
            if (field.length > 4) {
                // four hexadecimal digits
                return -1;
            }
            if (field.length == 0) {
                // there must be at least one numeral in every field
                return -1;
            }
            for (char c : field) {
                if ((c < 'A' || c > 'F') && (c < 'a' || c > 'f') && (c < '0' || c > '9')) {
                    // hexadecimal
                    return -1;
                }
            }
            switch (field.length) {
                case 1:
                    ipBytes[baseIndex + 1] = (byte) Integer.parseInt(field[0] + "", 16);
                    break;
                case 2:
                    ipBytes[baseIndex + 1] = (byte) Integer.parseInt(field[0] + "" + field[1], 16);
                    break;
                case 3:
                    ipBytes[baseIndex] = (byte) Integer.parseInt(field[0] + "", 16);
                    ipBytes[baseIndex + 1] = (byte) Integer.parseInt(field[1] + "" + field[2], 16);
                    break;
                case 4:
                    ipBytes[baseIndex] = (byte) Integer.parseInt(field[0] + "" + field[1], 16);
                    ipBytes[baseIndex + 1] = (byte) Integer.parseInt(field[2] + "" + field[3], 16);
                    break;
                default:
                    // should not happen
                    return -1;
            }
        }
        return split.length * 2;
    }

    public static String[] split(String str, String e) {
        List<String> ls = new LinkedList<>();
        int idx = -e.length();
        int lastIdx = 0;
        while (true) {
            idx = str.indexOf(e, idx + e.length());
            if (idx == -1) {
                ls.add(str.substring(lastIdx));
                break;
            }
            ls.add(str.substring(lastIdx, idx));
            lastIdx = idx + e.length();
        }
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return ls.toArray(new String[ls.size()]);
    }

    private static int parseIpv4String(String ipv4, byte[] ipBytes, int fromIdx) {
        String[] split = split(ipv4, ".");
        if (split.length != 4)
            return -1; // wrong len
        for (int i = 0; i < split.length; ++i) {
            int idx = fromIdx + i;
            if (idx >= ipBytes.length) {
                return -1; // too long
            }
            String s = split[i];
            char[] digits = s.toCharArray();
            if (digits.length > 3 || digits.length == 0) {
                return -1; // invalid for a byte
            }
            for (char c : digits) {
                if (c < '0' || c > '9')
                    return -1; // should be decimal digits
            }
            if (s.startsWith("0") && s.length() > 1)
                return -1; // 0n is invalid
            int num = Integer.parseInt(s);
            if (num > 255)
                return -1; // invalid byte
            ipBytes[idx] = (byte) num;
        }
        return split.length;
    }
}
