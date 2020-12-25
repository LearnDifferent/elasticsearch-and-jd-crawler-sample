package com.example.escrawlerjd.utils;

/**
 * 分页相关计算
 */
public class PageUtil {

    /**
     * 获取起始 Index（SQL/ES 中的 from）
     *
     * @param currentPage 当前在第几页
     * @param pageSize    页面大小
     * @return 起始的 Index
     */
    public static int getFromIndex(int currentPage, int pageSize) {
        return (currentPage - 1) * pageSize;
    }

    /**
     * 计算总共有多少页
     *
     * @param totalCount 总记录数
     * @param pageSize 页面大小
     * @return 总页数
     */
    public static int getAllPages(int totalCount, int pageSize) {
        return totalCount % pageSize == 0 ? totalCount / pageSize : totalCount / pageSize + 1;
    }
}
