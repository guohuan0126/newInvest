package com.jiuyi.ndr.rest.page;

import java.io.Serializable;
import java.util.List;

/**
 * Created by WangGang on 2017/5/3.
 */
public class PageData<T> implements Serializable{
    private static final long serialVersionUID = 767755909294344406L;

    int page;//当前页码
    int size;//当前页的条数
    int totalPages;//页码总数
    long total;//总记录数
    List<T> list;

    private String introduceUrl;// 介绍地址

    public String getIntroduceUrl() {
        return introduceUrl;
    }

    public void setIntroduceUrl(String introduceUrl) {
        this.introduceUrl = introduceUrl;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
