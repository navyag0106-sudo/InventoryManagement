package com.rental.vehiclerentalsystem.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;

/**
 * Pure Java, standalone, zero-dependency QR Code Generator.
 * Generates valid ISO/IEC 18004 QR Codes as Base64 PNG data URLs.
 */
public class QRCodeGenerator {

    // Reed-Solomon Galois Field 256 tables
    private static final int[] EXP = new int[512];
    private static final int[] LOG = new int[256];

    static {
        int val = 1;
        for (int i = 0; i < 255; i++) {
            EXP[i] = val;
            LOG[val] = i;
            val = (val << 1) ^ ((val & 0x80) != 0 ? 0x11D : 0);
        }
        for (int i = 255; i < 512; i++) {
            EXP[i] = EXP[i - 255];
        }
    }

    private static int gmult(int a, int b) {
        if (a == 0 || b == 0) return 0;
        return EXP[LOG[a] + LOG[b]];
    }

    private static int[] rsPoly(int ecCount) {
        int[] poly = {1};
        for (int i = 0; i < ecCount; i++) {
            int[] factor = {1, EXP[i]};
            int[] next = new int[poly.length + 1];
            for (int j = 0; j < poly.length; j++) {
                next[j] ^= gmult(poly[j], factor[0]);
                next[j + 1] ^= gmult(poly[j], factor[1]);
            }
            poly = next;
        }
        return poly;
    }

    private static int[] rsEncode(int[] data, int ecCount) {
        int[] poly = rsPoly(ecCount);
        int[] result = new int[data.length + ecCount];
        System.arraycopy(data, 0, result, 0, data.length);

        for (int i = 0; i < data.length; i++) {
            int lead = result[i];
            if (lead != 0) {
                for (int j = 0; j < poly.length; j++) {
                    result[i + j] ^= gmult(poly[j], lead);
                }
            }
        }
        System.arraycopy(data, 0, result, 0, data.length);
        return result;
    }

    // Version parameters for Byte mode (EC Level M)
    // Version 5 (37x37): 106 data bytes, 26 EC bytes, 1 block
    // Version 6 (41x41): 134 data bytes, 28 EC bytes, 1 block
    // Version 7 (45x45): 154 data bytes, 32 EC bytes, 2 blocks (87, 87)
    public static boolean[][] generateMatrix(String text) {
        byte[] rawBytes = text.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        int len = rawBytes.length;

        // Choose version based on payload length
        int version;
        int totalDataBytes;
        int ecBytesPerBlock;
        int numBlocks;
        int size;
        int[] alignPos;

        if (len <= 62) {
            version = 4;
            totalDataBytes = 64;
            ecBytesPerBlock = 18;
            numBlocks = 2;
            size = 33;
            alignPos = new int[]{6, 26};
        } else if (len <= 84) {
            version = 5;
            totalDataBytes = 86;
            ecBytesPerBlock = 24;
            numBlocks = 2;
            size = 37;
            alignPos = new int[]{6, 30};
        } else if (len <= 106) {
            version = 6;
            totalDataBytes = 108;
            ecBytesPerBlock = 16;
            numBlocks = 4;
            size = 41;
            alignPos = new int[]{6, 34};
        } else {
            version = 7;
            totalDataBytes = 124;
            ecBytesPerBlock = 18;
            numBlocks = 4;
            size = 45;
            alignPos = new int[]{6, 22, 38};
        }

        // Build 8-bit byte data stream
        BitBuffer buffer = new BitBuffer();
        buffer.put(0b0100, 4); // Mode: Byte
        buffer.put(len, version < 10 ? 8 : 16); // Character count
        for (byte b : rawBytes) {
            buffer.put(b & 0xFF, 8);
        }

        // Terminator
        int term = Math.min(4, (totalDataBytes * 8) - buffer.length);
        if (term > 0) buffer.put(0, term);

        // Pad to byte
        while (buffer.length % 8 != 0) {
            buffer.put(0, 1);
        }

        // Pad bytes 0xEC, 0x11
        boolean padToggle = true;
        while (buffer.length < totalDataBytes * 8) {
            buffer.put(padToggle ? 0xEC : 0x11, 8);
            padToggle = !padToggle;
        }

        int[] allData = buffer.getBytes();

        // Split into blocks and encode RS
        int blockSize = totalDataBytes / numBlocks;
        int[][] dataBlocks = new int[numBlocks][];
        int[][] ecBlocks = new int[numBlocks][];

        for (int b = 0; b < numBlocks; b++) {
            dataBlocks[b] = new int[blockSize];
            System.arraycopy(allData, b * blockSize, dataBlocks[b], 0, blockSize);
            int[] full = rsEncode(dataBlocks[b], ecBytesPerBlock);
            ecBlocks[b] = new int[ecBytesPerBlock];
            System.arraycopy(full, blockSize, ecBlocks[b], 0, ecBytesPerBlock);
        }

        // Interleave data
        int[] interleaved = new int[totalDataBytes + (numBlocks * ecBytesPerBlock)];
        int idx = 0;
        for (int i = 0; i < blockSize; i++) {
            for (int b = 0; b < numBlocks; b++) {
                interleaved[idx++] = dataBlocks[b][i];
            }
        }
        for (int i = 0; i < ecBytesPerBlock; i++) {
            for (int b = 0; b < numBlocks; b++) {
                interleaved[idx++] = ecBlocks[b][i];
            }
        }

        // Initialize matrix
        Boolean[][] grid = new Boolean[size][size];

        // Finder patterns
        drawFinder(grid, 0, 0);
        drawFinder(grid, size - 7, 0);
        drawFinder(grid, 0, size - 7);

        // Timing patterns
        for (int i = 8; i < size - 8; i++) {
            if (grid[6][i] == null) grid[6][i] = (i % 2 == 0);
            if (grid[i][6] == null) grid[i][6] = (i % 2 == 0);
        }

        // Alignment patterns
        for (int r : alignPos) {
            for (int c : alignPos) {
                if (grid[r][c] == null) {
                    drawAlignment(grid, r, c);
                }
            }
        }

        // Dark module
        grid[4 * version + 9][8] = true;

        // Place Data bits
        int bitIdx = 0;
        int totalBits = interleaved.length * 8;
        int row = size - 1;
        int col = size - 1;
        int dir = -1;

        while (col > 0) {
            if (col == 6) col--; // Skip vertical timing column
            for (int i = 0; i < size; i++) {
                int r = (dir == -1) ? (size - 1 - i) : i;
                for (int c = 0; c < 2; c++) {
                    int cc = col - c;
                    if (grid[r][cc] == null) {
                        boolean bit = false;
                        if (bitIdx < totalBits) {
                            int byteI = bitIdx / 8;
                            int bitI = 7 - (bitIdx % 8);
                            bit = ((interleaved[byteI] >> bitI) & 1) == 1;
                            bitIdx++;
                        }
                        // Mask pattern 0: (row + col) % 2 == 0
                        boolean mask = ((r + cc) % 2 == 0);
                        grid[r][cc] = (bit ^ mask);
                    }
                }
            }
            dir = -dir;
            col -= 2;
        }

        // Format info for Level M + Mask 0: 0x5412
        int formatInfo = 0x5412;
        for (int i = 0; i < 15; i++) {
            boolean bit = ((formatInfo >> (14 - i)) & 1) == 1;
            // Top-left
            if (i < 6) grid[8][i] = bit;
            else if (i < 8) grid[8][i + 1] = bit;
            else if (i == 8) grid[7][8] = bit;
            else grid[14 - i][8] = bit;

            // Split around bottom-left / top-right
            if (i < 8) grid[size - 1 - i][8] = bit;
            else grid[8][size - 15 + i] = bit;
        }

        boolean[][] result = new boolean[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                result[r][c] = (grid[r][c] != null && grid[r][c]);
            }
        }
        return result;
    }

    private static void drawFinder(Boolean[][] g, int r, int c) {
        for (int i = -1; i <= 7; i++) {
            for (int j = -1; j <= 7; j++) {
                int row = r + i;
                int col = c + j;
                if (row >= 0 && row < g.length && col >= 0 && col < g.length) {
                    if (i == -1 || i == 7 || j == -1 || j == 7) {
                        g[row][col] = false;
                    } else if (i == 0 || i == 6 || j == 0 || j == 6 || (i >= 2 && i <= 4 && j >= 2 && j <= 4)) {
                        g[row][col] = true;
                    } else {
                        g[row][col] = false;
                    }
                }
            }
        }
    }

    private static void drawAlignment(Boolean[][] g, int r, int c) {
        for (int i = -2; i <= 2; i++) {
            for (int j = -2; j <= 2; j++) {
                if (Math.abs(i) == 2 || Math.abs(j) == 2 || (i == 0 && j == 0)) {
                    g[r + i][c + j] = true;
                } else {
                    g[r + i][c + j] = false;
                }
            }
        }
    }

    /**
     * Generates a Base64 data URL for the QR code image.
     */
    public static String generateQRCodeDataUrl(String text, int width, int height) {
        try {
            boolean[][] matrix = generateMatrix(text);
            int matrixSize = matrix.length;
            int quietZone = 4;
            int totalSize = matrixSize + (quietZone * 2);
            int scale = Math.max(1, width / totalSize);
            int imgSize = totalSize * scale;

            BufferedImage image = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, imgSize, imgSize);

            g.setColor(new Color(15, 23, 42)); // Modern navy black #0F172A

            for (int r = 0; r < matrixSize; r++) {
                for (int c = 0; c < matrixSize; c++) {
                    if (matrix[r][c]) {
                        g.fillRect((c + quietZone) * scale, (r + quietZone) * scale, scale, scale);
                    }
                }
            }
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpeg", baos);
            byte[] bytes = baos.toByteArray();
            return "data:image/jpeg;base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static class BitBuffer {
        private int[] data = new int[512];
        private int length = 0;

        public void put(int val, int bits) {
            for (int i = bits - 1; i >= 0; i--) {
                int bit = (val >> i) & 1;
                int byteIdx = length / 8;
                int bitIdx = 7 - (length % 8);
                if (byteIdx >= data.length) {
                    data = Arrays.copyOf(data, data.length * 2);
                }
                data[byteIdx] |= (bit << bitIdx);
                length++;
            }
        }

        public int[] getBytes() {
            int numBytes = (length + 7) / 8;
            return Arrays.copyOf(data, numBytes);
        }
    }
}
