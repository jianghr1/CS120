package physics;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class utils {
    public static boolean[] bytesToBits(byte[] bytes) {
        boolean[] bits = new boolean[bytes.length * 8];

        int bitIndex = 0;

        for (byte b : bytes) {
            for (int i = 7; i >= 0; i--) {
                bits[bitIndex++] = ((b >> i) & 1) == 1;
            }
        }
        return bits;
    }

    public static int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        if (a > b) {
            int temp = a;
            a = b;
            b = temp;
        }
        int result = 0;

        while (a != 0) {
            if ((a & 1) == 1) {
                result += b;
            }
            a >>= 1;
            b <<= 1;
        }
        return result;
    }

    public static float multiply(float floatValue, int intValue) {
        // 预先定义一个常量，表示2^15
        final int powerOfTwo = 1 << 15;

        // 将float值映射到[-powerOfTwo, powerOfTwo]范围内
        int mappedValue = (Float.floatToRawIntBits(floatValue) << 15);

        // 使用位运算和加法将int值与映射后的float相乘
        int result = 0;
        int intValueAbs = Math.abs(intValue);

        for (int i = 0; i < 32; i++) {
            if ((intValueAbs & (1 << i)) != 0) {
                result += mappedValue;
            }
            mappedValue >>= 1;
        }

        // 根据原始int值和float值的符号来设置结果的符号
        if ((intValue < 0) ^ (Float.floatToRawIntBits(floatValue) < 0)) {
            result = -result;
        }

        // 将结果映射回[-1,1]范围内的float
        float finalFloatResult = Float.intBitsToFloat(result);

        return finalFloatResult;
    }

    public static float multiply(int intValue,float floatValue) {
        // 预先定义一个常量，表示2^15
        final int powerOfTwo = 1 << 15;

        // 将float值映射到[-powerOfTwo, powerOfTwo]范围内
        int mappedValue = (Float.floatToRawIntBits(floatValue) << 15);

        // 使用位运算和加法将int值与映射后的float相乘
        int result = 0;
        int intValueAbs = Math.abs(intValue);

        for (int i = 0; i < 32; i++) {
            if ((intValueAbs & (1 << i)) != 0) {
                result += mappedValue;
            }
            mappedValue >>= 1;
        }

        // 根据原始int值和float值的符号来设置结果的符号
        if ((intValue < 0) ^ (Float.floatToRawIntBits(floatValue) < 0)) {
            result = -result;
        }

        // 将结果映射回[-1,1]范围内的float
        float finalFloatResult = Float.intBitsToFloat(result);

        return finalFloatResult;
    }

    public static float multiply(float a, float b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        int signA = Float.floatToRawIntBits(a) >>> 31;
        int signB = Float.floatToRawIntBits(b) >>> 31;
        int signResult = signA ^ signB;

        int absA = Float.floatToRawIntBits(a) & 0x7FFFFFFF;
        int absB = Float.floatToRawIntBits(b) & 0x7FFFFFFF;

        int exponentA = (absA >>> 23) - 127;
        int exponentB = (absB >>> 23) - 127;
        int exponentResult = exponentA + exponentB + 127;

        int mantissaA = (absA & 0x007FFFFF) | 0x00800000;
        int mantissaB = (absB & 0x007FFFFF) | 0x00800000;
        long mantissaResult = multiply (mantissaA, mantissaB);
        mantissaResult >>>= 23;

        int result;
        if (mantissaResult >= (1L << 23)) {
            mantissaResult >>= 1;
            exponentResult++;
        }

        result = (signResult << 31) | ((exponentResult) << 23) | ((int) (mantissaResult & 0x007FFFFF));
        return Float.intBitsToFloat(result);
    }
    public static float divide(float numerator, int denominator) {
        // 如果分母为0，返回正或负无穷大，具体符号取决于numerator的符号
        if (denominator == 0) {
            return numerator >= 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }

        // 如果分子为0，返回0
        if (numerator == 0) {
            return 0.0f;
        }

        // 提取分子的符号位和绝对值的整数表示
        int signNumerator = Float.floatToRawIntBits(numerator) >>> 31;
        int absNumerator = Float.floatToRawIntBits(numerator) & 0x7FFFFFFF;

        // 提取分母的符号位和绝对值
        int signDenominator = denominator >= 0 ? 0 : 1;
        int absDenominator = Math.abs(denominator);

        // 计算指数部分，它等于分子的指数部分减去15（2^15的指数部分）
        int exponentResult = ((absNumerator >>> 23) - 15) + 127;

        // 计算尾数部分，将分子的尾数部分左移14位
        int mantissaNumerator = (absNumerator & 0x007FFFFF) | 0x00800000; // 添加隐藏的1
        int mantissaResult = mantissaNumerator << 14;

        // 使用位运算代替除法
        int quotient = 0;
        int remainder = mantissaResult;

        for (int i = 0; i < 24; i++) {
            quotient <<= 1;
            remainder <<= 1;

            if (remainder >= absDenominator) {
                quotient |= 1;
                remainder -= absDenominator;
            }
        }

        // 合并符号、指数和尾数，得到最终结果的整数表示
        int result = (signNumerator << 31) | ((exponentResult + 127) << 23) | (quotient & 0x007FFFFF);

        // 将整数表示转换回float
        return Float.intBitsToFloat(result);
    }
    public static float divide(int numerator, float denominator) {
        // 预先定义一个常量，表示2^15
        final int powerOfTwo = 1 << 15;

        // 将float值映射到[-1,1]范围内的整数表示
        int mappedValue = (int) (Float.floatToRawIntBits(denominator) >> 16);

        // 提取分子的符号位和绝对值
        int signNumerator = numerator >= 0 ? 0 : 1;
        int absNumerator = Math.abs(numerator);

        // 提取分母的符号位和绝对值
        int signDenominator = mappedValue >= 0 ? 0 : 1;
        int absDenominator = Math.abs(mappedValue);

        // 计算指数部分，它等于分子的指数部分减去15（2^15的指数部分）
        int exponentResult = ((absNumerator >>> 23) - 15) - ((absDenominator >>> 23) - 127);

        // 计算尾数部分，将分子的尾数部分左移14位
        int mantissaNumerator = (absNumerator & 0x007FFFFF) | 0x00800000; // 添加隐藏的1

        // 使用位运算代替除法
        int mantissaResult = 0;
        int remainder = mantissaNumerator;

        for (int i = 0; i < 24; i++) {
            remainder <<= 1;
            mantissaResult <<= 1;

            if (remainder >= absDenominator) {
                mantissaResult |= 1;
                remainder -= absDenominator;
            }
        }

        // 合并符号、指数和尾数，得到最终结果的整数表示
        int result = (signNumerator << 31) | ((exponentResult + 127) << 23) | (mantissaResult & 0x007FFFFF);

        // 将整数表示转换回float
        return Float.intBitsToFloat(result);
    }


    public static float divide(float numerator, float denominator) {
        // 如果分母为0，返回正或负无穷大，具体符号取决于numerator的符号
        if (denominator == 0.0f) {
            return numerator >= 0 ? Float.POSITIVE_INFINITY : Float.NEGATIVE_INFINITY;
        }

        // 如果分子为0，返回0
        if (numerator == 0.0f) {
            return 0.0f;
        }

        // 提取分子和分母的符号位和绝对值的整数表示
        int signNumerator = Float.floatToRawIntBits(numerator) >>> 31;
        int absNumerator = Float.floatToRawIntBits(numerator) & 0x7FFFFFFF;

        int signDenominator = Float.floatToRawIntBits(denominator) >>> 31;
        int absDenominator = Float.floatToRawIntBits(denominator) & 0x7FFFFFFF;

        // 计算指数部分，它等于分子的指数部分减去分母的指数部分
        int exponentResult = ((absNumerator >>> 23) - (absDenominator >>> 23)) + 127;

        // 计算尾数部分，将分子的尾数部分左移14位
        int mantissaNumerator = (absNumerator & 0x007FFFFF) | 0x00800000; // 添加隐藏的1

        // 使用位运算代替除法
        int mantissaResult = 0;
        int remainder = mantissaNumerator;

        for (int i = 0; i < 24; i++) {
            remainder <<= 1;
            mantissaResult <<= 1;

            if (remainder >= absDenominator) {
                mantissaResult |= 1;
                remainder -= absDenominator;
            }
        }

        // 合并符号、指数和尾数，得到最终结果的整数表示
        int result = (signNumerator << 31) | ((exponentResult + 127) << 23) | (mantissaResult & 0x007FFFFF);

        // 将整数表示转换回float
        return Float.intBitsToFloat(result);
    }

    public static int divide(int numerator, int denominator) {
        // 处理除数为0的情况
        if (denominator == 0) {
            throw new ArithmeticException("Division by zero");
        }

        // 处理特殊情况：最小的负数除以-1，会溢出成正数，这里特殊处理
        if (numerator == Integer.MIN_VALUE && denominator == -1) {
            return Integer.MAX_VALUE;
        }

        // 提取分子和分母的符号位和绝对值
        int signNumerator = numerator >= 0 ? 0 : 1;
        int signDenominator = denominator >= 0 ? 0 : 1;

        long absNumerator = Math.abs((long)numerator);
        long absDenominator = Math.abs((long)denominator);

        // 初始化结果和位移计数器
        int result = 0;
        int shift = 31;

        while (shift >= 0) {
            if ((absNumerator >> shift) >= absDenominator) {
                // 找到一个可以被除的部分
                result += (1 << shift);
                absNumerator -= (absDenominator << shift);
            }
            shift--;
        }

        // 设置结果的符号
        if ((signNumerator ^ signDenominator) == 1) {
            result = -result;
        }

        return result;
    }


    public static byte[] bitsToBytes(boolean[] bits) {
        byte[] bytes = new byte[bits.length / 8];

        for (int i = 0; i < bytes.length; i++) {
            int b = 0;
            for (int j = 0; j < 8; j++) {
                b = b << 1;
                b += bits[8 * i + j] ? 1 : 0;
            }
            bytes[i] = (byte) b;
        }

        return bytes;
    }

    public static int getCRC(byte[] bytes) {
        //CRC寄存器全为1
        int CRC = 0x0000ffff;
        //多项式校验值
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        return CRC;
    }

    public static byte[] readBinaryFile(String filePath) {
        File file = new File(filePath);
        byte[] fileBytes = new byte[(int) file.length()];

        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileBytes;
    }

    public static void writeBinaryFile(String filePath, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}