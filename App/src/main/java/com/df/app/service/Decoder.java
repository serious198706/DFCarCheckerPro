
package com.df.app.service;

/**
 * 校验
 * 
 * @author zsg
 */
public class Decoder {
    public static String hexString = "0123456789ABCDEF";

    public static String serialNumber() {

        return null;
    }

    /**
     * 校验值
     * 
     * @param data 数组
     * @return
     */
    public static void checkSum(byte[] data) {
        int wSum = 0;
        for (int i = 1; i < data.length-2 ; i++) {// 下标1-(length-2)求和
            if ((int) data[i] < 0) {
                wSum += (int) (data[i] & 0xFF);
            } else {
                wSum += (int) data[i];
            }
        }
        System.out.print("wSum:" + wSum);

        String hexs = ToHexString(wSum);
        char[] array = hexs.toCharArray();

        data[data.length-2] = toByte(array[0], array[1]);
        data[data.length-1] = toByte(array[2], array[3]);
    }

    /**
     * 将数字转换成4位的16进制字符串
     * 
     * @param value
     * @return
     */
    public static String ToHexString(int value) {
        String hex = value < 0 ? Integer.toHexString(value & 0xFF) : Integer.toHexString(value);
        int length = hex.length();
        for (int i = 0; i + length < 4; i++) {
            hex = '0' + hex;
        }
        return hex.toUpperCase();
    }
    
    public static String dumpHexString(byte[] bytes){
    	StringBuilder stringBuilder = new StringBuilder();
    	for(byte b:bytes){
    		stringBuilder.append(ToHexString(b).substring(2)).append(" ");
    	}
    	return stringBuilder.toString();
    }

    /**
     * 将2个字符转成满足要求的byte，例如 'A','6'，转成 (byte)0xA6
     * 
     * @param c1
     * @param c2
     * @return
     */
    public static byte toByte(char c1, char c2) {
        int value = 0;
        for (int i = 0; i < hexString.length(); i++) {
            if (hexString.charAt(i) == c1) {
                value = 16 * i;
                break;
            }
        }

        for (int i = 0; i < hexString.length(); i++) {
            if (hexString.charAt(i) == c2) {
                value += i;
                break;
            }
        }
        return (byte) value;
    }

}
