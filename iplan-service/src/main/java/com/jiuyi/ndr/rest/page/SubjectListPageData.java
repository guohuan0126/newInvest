package com.jiuyi.ndr.rest.page;

import java.util.List;
import java.util.Map;

/**
 * @author YU 2017/11/7
 */
public class SubjectListPageData<T> extends PageData{
    private String newbieAmt;
    private String tip;//提示

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getNewbieAmt() {
        return newbieAmt;
    }

    public void setNewbieAmt(String newbieAmt) {
        this.newbieAmt = newbieAmt;
    }

    private List<Map<String, Object>> InvestItems;

    public List<Map<String, Object>> getInvestItems() {
        return InvestItems;
    }

    public void setInvestItems(List<Map<String, Object>> investItems) {
        InvestItems = investItems;
    }
}

