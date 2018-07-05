package com.jiuyi.ndr.dao.credit.sql;

import com.jiuyi.ndr.constant.CreditConstant;
import com.jiuyi.ndr.domain.credit.CreditCondition;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.util.StringUtils;

/**
 * Created by drw on 2017/6/13.
 */
public class CreditOpeningDaoSql {

    public String updateSql(final CreditOpening creditOpening){
        return new SQL(){

            {
                UPDATE("ndr_credit_opening");

                if(creditOpening.getCreditId()!=null){
                    SET("credit_id = #{creditId}");
                }

                if(!StringUtils.isEmpty(creditOpening.getSubjectId())){
                    SET("subject_id=#{subjectId}");
                }

                if(!StringUtils.isEmpty(creditOpening.getTransferorId())){
                    SET("transferor_id=#{transferorId}");
                }

                if(!StringUtils.isEmpty(creditOpening.getTransferorIdXM())){
                    SET("transferor_id_xm=#{transferorIdXM}");
                }

                if(creditOpening.getTransferPrincipal()!=null){
                    SET("transfer_principal = #{transferPrincipal}");
                }

                if(creditOpening.getTransferDiscount()!=null){
                    SET("transfer_discount = #{transferDiscount}");
                }

                if(creditOpening.getStatus()!=null){
                    SET("status = #{status}");
                }

                if(creditOpening.getSourceChannel()!=null){
                    SET("source_channel = #{sourceChannel}");
                }

                if(creditOpening.getSourceChannelId()!=null){
                    SET("source_channel_id = #{sourceChannelId}");
                }

                if(creditOpening.getSourceAccountId()!=null){
                    SET("source_account_id = #{sourceAccountId}");
                }

                if(creditOpening.getIplanId()!=null){
                    SET("iplan_id = #{iplanId}");
                }

                if(!StringUtils.isEmpty(creditOpening.getPublishTime())){
                    SET("publish_time = #{publishTime}");
                }

                if(!StringUtils.isEmpty(creditOpening.getOpenTime())){
                    SET("open_time = #{openTime}");
                }

                if(!StringUtils.isEmpty(creditOpening.getCloseTime())){
                    SET("close_time = #{closeTime}");
                }

                if(!StringUtils.isEmpty(creditOpening.getEndTime())){
                    SET("end_time = #{endTime}");
                }

                if(creditOpening.getOpenFlag()!=null){
                    SET("open_flag = #{openFlag}");
                }

                if(creditOpening.getOpenChannel()!=null){
                    SET("open_channel = #{openChannel}");
                }

                if(creditOpening.getAvailablePrincipal()!=null){
                    SET("available_principal = #{availablePrincipal}");
                }

                if(creditOpening.getPackPrincipal()!=null){
                    SET("pack_principal = #{packPrincipal}");
                }

                if(creditOpening.getExtStatus()!=null){
                    SET("ext_status = #{extStatus}");
                }

                if(!StringUtils.isEmpty(creditOpening.getExtSn())){
                    SET("ext_sn = #{extSn}");
                }

                if(!StringUtils.isEmpty(creditOpening.getCreateTime())){
                    SET("create_time = #{createTime}");
                }

                if(!StringUtils.isEmpty(creditOpening.getUpdateTime())){
                    SET("update_time = #{updateTime}");
                }

                WHERE("id=#{id}");
            }
        }.toString();
    }

    public String getCreditOpeningSql(String type,String condition) {
        StringBuilder sql = new StringBuilder();
        sql.append(" select nco.id,ns.name,(ns.invest_rate + IFNULL(ns.bonus_rate,0))/nco.transfer_discount AS rate,ns.invest_rate + IFNULL(ns.bonus_rate,0) AS total_rate,nco.transfer_discount,nco.available_principal,nco.end_time,nco.credit_id,ns.invest_rate,ns.bonus_rate  ")
                .append(" from ndr_credit_opening nco LEFT JOIN ndr_subject ns ON nco.subject_id = ns.subject_id ")
                .append(" where nco.available_principal != 0 AND nco.open_flag = 1 AND nco.open_channel = 1").append(condition);
        if(StringUtils.isEmpty(type)){
            sql.append(" order by nco.open_time ");
        }
        if (!StringUtils.isEmpty(type)){
            switch (type){
                case CreditConstant.OLD : sql.append(" order by nco.open_time "); break;
                case CreditConstant.NEW : sql.append(" order by nco.open_time DESC"); break;
                case CreditConstant.BIG : sql.append(" order by rate DESC"); break;
                case CreditConstant.SMALL : sql.append(" order by rate"); break;
                case CreditConstant.MANY : sql.append(" order by nco.end_time DESC"); break;
                case CreditConstant.FEW : sql.append(" order by nco.end_time"); break;
            }
        }
        return sql.toString();
    }
    public String getCreditOpeningPcSql(String type) {
        return new SQL(){{
            SELECT("nco.id,ns.name,(ns.invest_rate + IFNULL(ns.bonus_rate,0))/nco.transfer_discount AS total_rate,nco.available_principal,nco.end_time,nco.transfer_principal  ");
            FROM("ndr_credit_opening nco LEFT JOIN ndr_subject ns ON nco.subject_id = ns.subject_id");
            WHERE("nco.available_principal != 0 AND nco.open_flag = 1 AND nco.open_channel = 1");
            if(StringUtils.isEmpty(type)){
                ORDER_BY("nco.open_time");
            }
            if (!StringUtils.isEmpty(type)){
                switch (type){
                    case CreditConstant.OLD : ORDER_BY("nco.open_time"); break;
                    case CreditConstant.NEW : ORDER_BY("nco.open_time DESC"); break;
                    case CreditConstant.BIG : ORDER_BY("total_rate DESC"); break;
                    case CreditConstant.SMALL : ORDER_BY("total_rate"); break;
                    case CreditConstant.MANY : ORDER_BY("nc.end_time DESC"); break;
                    case CreditConstant.FEW : ORDER_BY("nc.end_time"); break;
                }
            }
        }}.toString();
    }

    public String getCreditOpeningSortSql(CreditCondition creditCondition) {
        StringBuilder sql = new StringBuilder();
        sql.append("select nco.id,ns.name,(ns.invest_rate + IFNULL(ns.bonus_rate,0))/nco.transfer_discount AS total_rate,nco.available_principal,nco.end_time,nco.transfer_principal,nco.transfer_discount,ns.repay_type,ns.invest_rate,ns.bonus_rate,ns.term,ns.current_term   ")
                .append("from  ndr_credit_opening nco LEFT JOIN ndr_subject ns ON nco.subject_id = ns.subject_id ")
                .append(" WHERE nco.available_principal != 0 AND nco.open_flag = 1 AND nco.open_channel = 1 ");
        if (creditCondition != null){
            if (creditCondition.getRateMin() != null) {
                sql.append(" AND (ns.invest_rate + IFNULL(ns.bonus_rate,0))/nco.transfer_discount > " + creditCondition.getRateMin());
            }
            if (creditCondition.getRateMax() != null) {
                sql.append(" AND (ns.invest_rate + IFNULL(ns.bonus_rate,0))/nco.transfer_discount <= " + creditCondition.getRateMax());
            }
            if (creditCondition.getTermMin() != null) {
                sql.append(" AND (ns.term - ns.current_term + 1) > " + creditCondition.getTermMin());
            }
            if (creditCondition.getTermMax() != null) {
                sql.append(" AND (ns.term - ns.current_term + 1) <= " + creditCondition.getTermMax());
            }
            if (creditCondition.getAmountMin() != null) {
                sql.append(" AND nco.available_principal > " + creditCondition.getAmountMin());
            }
            if (creditCondition.getAmountMax() != null) {
                sql.append(" AND nco.available_principal <= " + creditCondition.getAmountMax());
            }
        }
        sql.append("ORDER BY ( ns.term - ns.current_term )");
        return sql.toString();
    }
}
