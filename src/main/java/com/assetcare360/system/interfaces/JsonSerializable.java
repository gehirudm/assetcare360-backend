package com.assetcare360.system.interfaces;

public interface JsonSerializable<T> {
    String toJson();
    static <T> String listToJson(Iterable<T> items) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (T item : items) {
            if (!first) sb.append(",");
            sb.append(item.toString());
            first = false;
        }
        sb.append("]");
        return sb.toString();
    }
    T fromJson(String json);
}
