package com.jiuyi.ndr.rest.page;

import java.util.List;

public class CreditPageData<T> extends PageData {
    private List<String> terms;
    private List<String> rates;
    private List<String> discounts;

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public List<String> getRates() {
        return rates;
    }

    public void setRates(List<String> rates) {
        this.rates = rates;
    }

    public List<String> getDiscounts() {
        return discounts;
    }

    public void setDiscounts(List<String> discounts) {
        this.discounts = discounts;
    }
}
