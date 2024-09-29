package com.gy11233.utils;

/**
 * 距离工具类 计算两个位置的距离
 */
public class DistanceUtil {
    /**
     * 赤道半径（单位：米）
     */
    private static final double EQUATOR_RADIUS = 6378137;

    /**
     * 方法一：（反余弦计算方式）
     *
     * @param longitude1 第一个点的经度
     * @param latitude1  第一个点的纬度
     * @param longitude2 第二个点的经度
     * @param latitude2  第二个点的纬度
     * @return 返回距离，单位m
     */
    public static double getDistance(double longitude1, double latitude1, double longitude2, double latitude2) {
        // 纬度
        double lat1 = Math.toRadians(latitude1);
        double lat2 = Math.toRadians(latitude2);
        // 经度
        double lon1 = Math.toRadians(longitude1);
        double lon2 = Math.toRadians(longitude2);
        // 纬度之差
        double a = lat1 - lat2;
        // 经度之差
        double b = lon1 - lon2;
        // 计算两点距离的公式
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(b / 2), 2)));
        // 弧长乘赤道半径, 返回单位: 米
        s = s * EQUATOR_RADIUS;
        return s;
    }

    // 地球半径，单位：米
    private static final double EARTH_RADIUS = 6371000; // 平均半径为6371公里（米）

    // 将角度转换为弧度
    private static double toRadians(double degrees) {
        return degrees * Math.PI / 180;
    }

    /**
     * 使用 Haversine 公式计算两点间的距离
     * @param lat1 点1的纬度
     * @param lon1 点1的经度
     * @param lat2 点2的纬度
     * @param lon2 点2的经度
     * @return 两点之间的距离（单位：米）
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将纬度和经度转换为弧度
        double radLat1 = toRadians(lat1);
        double radLat2 = toRadians(lat2);
        double deltaLat = radLat2 - radLat1;
        double deltaLon = toRadians(lon2 - lon1);

        // Haversine 公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(radLat1) * Math.cos(radLat2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离
        return EARTH_RADIUS * c;
    }
}
