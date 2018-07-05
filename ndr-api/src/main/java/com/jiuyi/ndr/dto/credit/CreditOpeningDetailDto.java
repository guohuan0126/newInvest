package com.jiuyi.ndr.dto.credit;

import com.jiuyi.ndr.constant.BaseCreditOpeningDetailDto;
import com.jiuyi.ndr.dto.subject.SubjectRepayScheduleDto;
import com.jiuyi.ndr.dto.subject.SubjectTransLogDto;

import java.util.List;

public class CreditOpeningDetailDto extends BaseCreditOpeningDetailDto {

    private String  guaranteeType;//质押、抵押方式
    private Integer residualTerm;//项目剩余期限

    private Double rate;//标的利率

    private Double differRate;//利率

    private String differRateStr;
    private List<RedPacketApp> redPacketAppList;//红包券

    private String principalStr;//债权本金

    public String getPrincipalStr() {
        return principalStr;
    }

    public void setPrincipalStr(String principalStr) {
        this.principalStr = principalStr;
    }


    public String getDifferRateStr() {
        return differRateStr;
    }

    public void setDifferRateStr(String differRateStr) {
        this.differRateStr = differRateStr;
    }

    public Double getDifferRate() {
        return differRate;
    }

    public void setDifferRate(Double differRate) {
        this.differRate = differRate;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }


    private List<SubjectRepayScheduleDto> subjectRepayScheduleDtos;

    private List<SubjectTransLogDto> subjectTransLogDtos;


    public List<SubjectTransLogDto> getSubjectTransLogDtos() {
        return subjectTransLogDtos;
    }

    public void setSubjectTransLogDtos(List<SubjectTransLogDto> subjectTransLogDtos) {
        this.subjectTransLogDtos = subjectTransLogDtos;
    }

    public List<SubjectRepayScheduleDto> getSubjectRepayScheduleDtos() {
        return subjectRepayScheduleDtos;
    }

    public void setSubjectRepayScheduleDtos(List<SubjectRepayScheduleDto> subjectRepayScheduleDtos) {
        this.subjectRepayScheduleDtos = subjectRepayScheduleDtos;
    }


    public String getGuaranteeType() {
        return guaranteeType;
    }

    public void setGuaranteeType(String guaranteeType) {
        this.guaranteeType = guaranteeType;
    }

    public Integer getResidualTerm() {
        return residualTerm;
    }

    public void setResidualTerm(Integer residualTerm) {
        this.residualTerm = residualTerm;
    }
    public static class RedPacketApp {

        private Integer id;
        private double amt;//红包券总额
        private String amt2;//红包券总额（展示）
        private double rate;//加息券专用：利息
        private String rate2;//加息券专用：利息（展示）
        private String type;//类别
        private String name;//名称
        private String deadLine;//截止日期
        private String introduction;//介绍
        private int rateDay;//加息天数
        private String useStatus;//是否可用
        private double investMoney;//起投金额

        public double getInvestMoney() {
            return investMoney;
        }

        public void setInvestMoney(double investMoney) {
            this.investMoney = investMoney;
        }

        public void setUseStatus(String useStatus) {
            this.useStatus = useStatus;
        }

        public String getUseStatus() {
            return useStatus;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public double getAmt() {
            return amt;
        }

        public void setAmt(double amt) {
            this.amt = amt;
        }

        public String getAmt2() {
            return amt2;
        }

        public void setAmt2(String amt2) {
            this.amt2 = amt2;
        }

        public double getRate() {
            return rate;
        }

        public void setRate(double rate) {
            this.rate = rate;
        }

        public String getRate2() {
            return rate2;
        }

        public void setRate2(String rate2) {
            this.rate2 = rate2;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDeadLine() {
            return deadLine;
        }

        public void setDeadLine(String deadLine) {
            this.deadLine = deadLine;
        }

        public String getIntroduction() {
            return introduction;
        }

        public void setIntroduction(String introduction) {
            this.introduction = introduction;
        }

        public int getRateDay() {
            return rateDay;
        }

        public void setRateDay(int rateDay) {
            this.rateDay = rateDay;
        }
    }
    public List<RedPacketApp> getRedPacketAppList() {
        return redPacketAppList;
    }

    public void setRedPacketAppList(List<RedPacketApp> redPacketAppList) {
        this.redPacketAppList = redPacketAppList;
    }

}
