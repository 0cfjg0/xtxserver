package com.itheima.xiaotuxian.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

@Data
public class Pager<T> {
    /**
     * 总条数
     */
    private long counts;
    /**
     * 每页条数
     */
    private long pageSize;
    /**
     * 总页数
     */
    private long pages;
    /**
     * 当前页数
     */
    private long page;

    /**
     * 当前页数据
     */
    private List<T> items;

    public Pager(IPage page) {
        this.pageSize = page.getSize();
        this.counts = page.getTotal();
        this.page = page.getCurrent();
        this.pages = page.getPages();
        this.items = page.getRecords();
    }

    public Pager() {
    }

    public Pager(long counts, long pageSize, long pages, long page, List<T> items) {
        this.counts = counts;
        this.pageSize = pageSize;
        this.pages = pages;
        this.page = page;
        this.items = items;
    }
}
