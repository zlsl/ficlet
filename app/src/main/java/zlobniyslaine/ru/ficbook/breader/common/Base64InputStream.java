package zlobniyslaine.ru.ficbook.breader.common;

import java.util.Arrays;

public class Base64InputStream
//extends InputStream
{
    private static final String s_base64String = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    public static final byte[] s_charMap = new byte[128];

    static {
        Arrays.fill(s_charMap, (byte) -1);

        for (int i = 0; i < 64; i++)
            s_charMap[s_base64String.charAt(i)] = (byte) i;

        s_charMap['='] = s_charMap['A'];
    }

    public static byte[] processBase64(byte[] data, int offset, int length) {
        int[] resultLength = {0};
        return processBase64(data, offset, length, resultLength);
    }

    public static byte[] processBase64(byte[] data, int offset, int length, int[] resultLength) {
        byte[] result = new byte[length * 3 / 4];
        byte[] block = new byte[4];

        int roffset = 0;
        int i = offset;
        length += offset;
        int j = 0;

        while (i < length) {
            byte b = data[i++];

            if (b == '\r' || b == '\n' || b == ' ')
                continue;

            if (b <= 0 || s_charMap[b] == -1) {
                return roffset == 0 ? null : result;
            }

            block[j++] = s_charMap[b];

            if (j == 4 || i == length) {
                result[roffset] = (byte) ((block[0] << 2) | (block[1] >>> 4));
                result[roffset + 1] = (byte) (((block[1] & 0xf) << 4) | (block[2] >>> 2));
                result[roffset + 2] = (byte) (((block[2] & 3) << 6) | block[3]);
                roffset += 3;
                resultLength[0] = roffset;

                block[0] = 'A';
                block[1] = 'A';
                block[2] = 'A';
                block[3] = 'A';
                j = 0;
            }
        }
        return result;
    }
}
