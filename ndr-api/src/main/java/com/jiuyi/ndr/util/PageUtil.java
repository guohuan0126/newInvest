package com.jiuyi.ndr.util;

import java.util.ArrayList;
import java.util.List;

public class PageUtil<T> {

    /**
     * 当前页面
     */
    private int page = 1;

    /**
     * 显示多少行
     */
    private int rows = 10;

    /**
     * 总记录条数
     */
    private int total;

    /**
     * @return the page
     */
    public int getPage() {
        return page;
    }

    /**
     * @param page the page to set
     */
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * @return the total
     */
    public int getTotal() {
        return total;
    }

    /**
     * @param total the total to set
     */
    public void setTotal(int total) {
        this.total = total;
    }

    /**
     * 对list集合进行分页处理
     *
     * @return
     */
    public List<T> ListSplit(List<T> list,int page,int rows) {
        List<T> newList=new ArrayList<T>();
        total=list.size();
        if ((rows*(page - 1)) <= total){
            newList=list.subList(rows*(page-1), ((rows*page)>= total?total:(rows*page)));
        }
        return newList;
    }
}
