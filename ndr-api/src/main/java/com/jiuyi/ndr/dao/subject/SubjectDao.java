package com.jiuyi.ndr.dao.subject;

import com.jiuyi.ndr.dao.subject.sql.SubjectDaoSql;
import com.jiuyi.ndr.domain.subject.BorrowInfo;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayScheduleQuery;
import com.jiuyi.ndr.domain.subject.VehicleInfoPic;
import com.jiuyi.ndr.domain.transferstation.AgricultureLoanInfo;
import com.jiuyi.ndr.domain.transferstation.LoanIntermediaries;
import com.jiuyi.ndr.dto.subject.SubjectDto;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Created by lixiaolei on 2017/6/8.
 */
@Mapper
public interface SubjectDao {

    @Select("SELECT * FROM ndr_subject WHERE id = #{id, jdbcType=INTEGER}")
    Subject findById(Integer id);

    @Select("SELECT * FROM ndr_subject WHERE id = #{id, jdbcType=INTEGER} FOR UPDATE")
    Subject findByIdForUpdate(Integer id);

    @Select("SELECT * FROM ndr_subject WHERE subject_id = #{subjectId, jdbcType=VARCHAR}")
    Subject findBySubjectId(String subjectId);


    @Select("select * from ndr_subject where withdraw_status != 1  AND reloan_subject_id is null and raise_status = 3 AND lend_time >= '20180328 00:00:00' ")
    List<Subject> findByStatus(Integer status);

    @Select("<script>"
            + "SELECT * FROM ndr_subject WHERE subject_id IN "
            + "<foreach item='item' index='index' collection='subjectIds' open='(' separator=',' close=')'>"
                + "#{item}"
            + "</foreach>"
            + "</script>")
    List<Subject> findBySubjectIds(@Param(value = "subjectIds") List<String> subjectIds);

    @Select("SELECT * FROM ndr_subject WHERE subject_id = #{subjectId, jdbcType=VARCHAR} FOR UPDATE")
    Subject findBySubjectIdForUpdate(String subjectId);

    @Select(value = "<script>"
            + "SELECT * FROM ndr_subject WHERE raise_status = 0 AND open_flag = 1 AND push_status = 1 AND type IN "
            + "<foreach item='type' index='index' collection='types' open='(' separator=',' close=')'>"
                + "#{type}"
            + "</foreach>"
            +" AND open_channel IN"
            + "<foreach item='openChannel' index='index' collection='openChannels' open='(' separator=',' close=')'>"
                + " #{openChannel}"
            + "</foreach>"
            + "ORDER by open_time asc"
            +"</script>")
    List<Subject> findInvestable(@Param(value = "types") String[] types, @Param(value = "openChannels") Integer[] openChannels);

    @Select("SELECT s.* FROM ndr_subject s WHERE s.raise_status = 1 and s.open_flag = 1 and s.open_channel in (1,2,4,8) ORDER BY s.open_time")
    List<Subject> findCouldLend();

    @Insert("INSERT INTO ndr_subject (`subject_id`, `name`, `borrower_id`, `borrower_id_xm`, `intermediator_id`, `intermediator_id_xm`, `type`, `term`, `period`, `rate`, `overdue_penalty`, " +
            "`advanced_payoff_penalty`, `total_amt`, `fee_amt`, `repay_type`, `invest_param`, `raise_status`, `repay_status`, `publish_time`, `open_time`, `close_time`, `lend_time`, " +
            "`open_flag`, `open_channel`, `available_amt`, `current_term`, `paid_principal`, `paid_interest`, `push_status`, `ext_sn`, `ext_status`, `create_time`, `update_time`, `contract_no`, " +
            "`assets_source`, `direct_flag`, `operator`, `miscellaneous_amt`, `compensation_account`, `profit_account`, `reloan_subject_id`, `withdraw_sn`, `withdraw_status`, `profit_amt`, `reloan_profit_amt`,`repay_time`) " +
            "VALUES (#{subjectId}, #{name}, #{borrowerId}, #{borrowerIdXM}, #{intermediatorId}, #{intermediatorIdXM}, #{type}, #{term}, #{period}, #{rate}, #{overduePenalty}, #{advancedPayoffPenalty}, " +
            "#{totalAmt}, #{feeAmt}, #{repayType}, #{investParam}, #{raiseStatus}, #{repayStatus}, #{publishTime}, #{openTime}, #{closeTime}, #{lendTime}, #{openFlag}, #{openChannel}, #{availableAmt}, " +
            "#{currentTerm}, #{paidPrincipal}, #{paidInterest}, #{pushStatus}, #{extSn}, #{extStatus}, #{createTime}, #{updateTime}, #{contractNo}, #{assetsSource}, #{directFlag}, #{operator},  #{miscellaneousAmt}, " +
            "#{compensationAccount},  #{profitAccount}, #{reloanSubjectId},  #{withdrawSn}, #{withdrawStatus}, #{profitAmt}, #{reloanProfitAmt},#{repayTime})")
    int insert(Subject subject);

    @UpdateProvider(type = SubjectDaoSql.class, method = "updateSql")
    int update(Subject subject);

    @Select(value = "SELECT '金农宝' as loan_type, '信用贷款' as borrow_type, al.loan_term, '月' as operation_type, '正常' as repay_status, " +
            "al.`name`, al.id_card as id_card, al.repayment_source AS revenue_source, al.age, al.money AS loan_amt, " +
            "al.annual_income as annualIncome,"+
            "al.loan_application AS loan_usage,CONCAT(al.province,al.city,al.area) as area, al.marital_status AS is_married, " +
            "al.industry, al.wages, al.bonus, al.other_income, al.rental_income, al.living_expenses, al.education_expenses, " +
            "al.rental_payment, al.vehicle_use_fee, al.other_payment, al.daily_expenses_total, al.max_repayment_amount " +
            "FROM agriculture_loaninfo al WHERE al.contract_id = #{contractNo} limit 1")
    BorrowInfo findAgroBorrowerInfo(@Param("contractNo") String contractNo);

    @Select(value = "SELECT '金农宝' as loan_type, '信用贷款' as borrow_type, al.loan_term, '月' as operation_type, '正常' as repay_status, " +
            "al.`name`, al.id_card as id_card, al.repayment_source AS revenue_source, al.age, al.money AS loan_amt, al.loan_application AS loan_usage, " +
            "al.annual_income as annualIncome,"+
            "al.loan_application AS loan_usage,CONCAT(al.province,al.city,al.area) as area, al.marital_status AS is_married, " +
            "CONCAT(al.province,al.city,al.area) as area, al.marital_status AS is_married, " +
            "al.industry, al.wages, al.bonus, al.other_income, al.rental_income, al.living_expenses, al.education_expenses, " +
            "al.rental_payment, al.vehicle_use_fee, al.other_payment, al.daily_expenses_total, al.max_repayment_amount " +
            "FROM agriculture_fork_loans afl LEFT JOIN agriculture_loaninfo al ON afl.parent_id = al.id " +
            "WHERE afl.child_contractid = #{contractNo} limit 1")
    BorrowInfo findAgroBorrowerInfoBySub(@Param("contractNo") String contractNo);

    @Select(value = "SELECT '车押宝' as loan_type, case lvi.guarantee_type when 'A' then '质押' when 'B' then 'GPS全款' else 'GPS分期' end as borrow_type, li.operation_type, li.deadline as loan_term, '正常' as repay_status, lvi.brand, lvi.license_plate_number, lvi.kilometre_amount, " +
            "lvi.borrower_name AS `name`, lvi.borrower_id_card AS id_card, '其他' AS revenue_source, CASE WHEN LENGTH(lvi.borrower_id_card) = 18 THEN YEAR(NOW()) - SUBSTR(lvi.borrower_id_card, 7, 4) " +
            "WHEN LENGTH(lvi.borrower_id_card) = 15 THEN  YEAR(NOW()) - (1900+SUBSTR(lvi.borrower_id_card, 7, 2)) ELSE NULL END AS age, li.money AS loan_amt, " +
            "lvi.borrowing_purposes AS loan_usage, lvi.item_address AS area, case li.marital_status when '1' then '1' else '0' end AS is_married, " +
            "lvi.identification_number AS identificationNumber, lvi.assess_price AS assessPrice, lvi.manufacture_date AS manufactureDate,lvi.registration_date AS registrationDate,lvi.displacement AS displacement,"+
            "lvi.condition_assessment AS conditionAssessment,lvi.fuel AS fuel,lvi.engineno AS engineno,lvi.lllegal_deduction AS lllegalDeduction,lvi.violation_fines AS violationFines,lvi.transmission AS transmission,"+
            "lvi.buy_amt AS buyAmt,lvi.traffic_insurance_validity AS trafficInsuranceValidity,lvi.inspection_validity AS inspectionValidity,lvi.using_properties,lvi.vehicle_type,"+
            "li.industry, li.wages, li.bonus, li.other_income, li.rental_income, li.living_expenses, li.education_expenses, " +
            "li.rental_payment, li.vehicle_use_fee, li.other_payment, li.daily_expenses_total, li.max_repayment_amount " +
            "FROM loan_vehicle_intermediaries lvi LEFT JOIN loan_intermediaries li ON lvi.loan_id = li.id " +
            "WHERE li.contract_id = #{contractNo} limit 1")
    BorrowInfo findVehicleBorrowerInfo(@Param("contractNo") String contractNo);

    @Select(value = "SELECT '车押宝' as loan_type, case lvi.guarantee_type when 'A' then '质押' when 'B' then 'GPS全款' else 'GPS分期' end as borrow_type, li.operation_type, li.deadline as loan_term, '正常' as repay_status, lvi.brand, lvi.license_plate_number, lvi.kilometre_amount, " +
            "lvi.borrower_name AS `name`, lvi.borrower_id_card AS id_card, '其他' AS revenue_source, CASE WHEN LENGTH(lvi.borrower_id_card) = 18 THEN YEAR(NOW()) - SUBSTR(lvi.borrower_id_card, 7, 4) " +
            "WHEN LENGTH(lvi.borrower_id_card) = 15 THEN  YEAR(NOW()) - (1900+SUBSTR(lvi.borrower_id_card, 7, 2)) ELSE NULL END AS age, li.money AS loan_amt, " +
            "lvi.borrowing_purposes AS loan_usage, lvi.item_address AS area, case li.marital_status when '1' then '1' else '0' end AS is_married, " +
            "lvi.identification_number AS identificationNumber, lvi.assess_price AS assessPrice, lvi.manufacture_date AS manufactureDate,lvi.registration_date AS registrationDate,lvi.displacement AS displacement,"+
            "lvi.condition_assessment AS conditionAssessment,lvi.fuel AS fuel,lvi.engineno AS engineno,lvi.lllegal_deduction AS lllegalDeduction,lvi.violation_fines AS violationFines,lvi.transmission AS transmission,"+
            "lvi.buy_amt AS buyAmt,lvi.traffic_insurance_validity AS trafficInsuranceValidity,lvi.inspection_validity AS inspectionValidity,lvi.using_properties ,lvi.vehicle_type,"+
            "li.industry, li.wages, li.bonus, li.other_income, li.rental_income, li.living_expenses, li.education_expenses, " +
            "li.rental_payment, li.vehicle_use_fee, li.other_payment, li.daily_expenses_total, li.max_repayment_amount " +
            "FROM loan_fork_intermediaries lfi LEFT JOIN loan_intermediaries li ON lfi.parent_id = li.id " +
            "LEFT JOIN loan_vehicle_intermediaries lvi ON lvi.loan_id = li.id WHERE lfi.child_contractid = #{contractNo} limit 1")
    BorrowInfo findVehicleBorrowerInfoBySub(@Param("contractNo") String contractNo);

    @Select(value = "SELECT loan_type, guarantee_type as borrow_type, operation_type, case when operation_type = '月' then month else day end as loan_term, '正常' as repay_status, brand, license_plate_number, kilometre_amount, " +
            "borrower AS name, id_card AS id_card, source_of_repayment AS revenue_source, CASE WHEN LENGTH(id_card) = 18 THEN YEAR(NOW()) - SUBSTR(id_card, 7, 4) WHEN LENGTH(id_card) = 15 " +
            "THEN  YEAR(NOW()) - (1900+SUBSTR(id_card, 7, 2)) ELSE NULL END AS age, total_money AS loan_amt, borrowing_purposes AS loan_usage, location AS area, " +
            "marital_status AS is_married, " +
            "'' as industry, '' as wages, '' as bonus, '' as other_income, '' as rental_income, '' as living_expenses, '' as education_expenses, " +
            "'' as rental_payment, '' as vehicle_use_fee, '' as other_payment, '' as daily_expenses_total, '' as max_repayment_amount " +
            "FROM demand_treasure_loan WHERE loan_name = #{subjectName} limit 1")
    BorrowInfo findOldTTZBorrowerInfo(@Param("subjectName") String subjectName);


    @Select(value = "SELECT s.id,s.subject_id,s.`name` AS subject_name,CONCAT(SUBSTRING(u.realname,1,1),'**') AS borrow_name, " +
            "CONCAT(SUBSTRING(u.id_card,1,6),'********',SUBSTRING(u.id_card,15,4)) AS id_card ,total_amt/100.0 AS amount " +
            "FROM ndr_subject s LEFT JOIN `user` u ON s.borrower_id = u.id WHERE s.raise_status in (0,1,3) " +
            "AND s.open_channel = 2 AND s.push_status = 1 ORDER BY open_time DESC limit 100")
    List<SubjectDto> getSubjectInIplan();
    @Select("SELECT * FROM ndr_subject WHERE contract_no = #{contractNo, jdbcType=VARCHAR}")
    Subject findByContractNo(String contractNo);

 /*   @Select("select * from (select * from ndr_subject i where i.newbie_only = 1 AND i.is_visiable = 1 AND i.open_flag=1 AND i.push_status=1 AND i.raise_status in(0,4) ORDER BY i.raise_status desc, i.open_time DESC ) AS a " +
            "UNION " +
            "select * from (select * from ndr_subject p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status = 0 ORDER BY p.term, p.open_time DESC) AS b " +
            "UNION " +
            "select * from (select * from ndr_subject p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status = 4 ORDER BY p.term, p.open_time DESC) AS c " +
            "UNION " +
            "select * from (select * from ndr_subject p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status in(1,3) ORDER BY p.close_time DESC, p.open_time DESC limit 5) AS d")
    List<Subject> findSubjectNewBieWap();*/

    /**
     * 查询散标列表包含新手标
     * @return
     */
    @Select("<script>"
           +"SELECT * from ("
            + "SELECT * from (select * from ndr_subject i where i.newbie_only = 1 AND i.is_visiable = 1 AND i.open_flag=1 AND i.push_status=1 AND i.raise_status in(0,4) ORDER BY i.raise_status DESC, i.open_time DESC ) AS a "
            +"UNION (select * from (select * from ndr_subject p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status in(0,4) ORDER BY p.raise_status DESC, p.open_time DESC) AS b ) "
            +"UNION (select * from (select * from ndr_subject p where p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status in(1,3) ORDER BY p.close_time DESC,p.open_time DESC) AS c )"
            +")as d where d.open_channel in(1,3,5,7) "
            + "<if test=\"type != null and type != ''\">" +
            "      AND d.type= #{type}" +
            "  </if>"
            + "</script>")
    List<Subject> findSubjectNewBie(@Param("type") String type);

    /**
     * 查询散标列表新手标
     * @return
     */
    @Select("<script>"
            +"SELECT id,subject_id,name,rate,invest_rate,bonus_rate,term,total_amt,available_amt,repay_type,raise_status," +
            "open_time,close_time,newbie_only,period,type,activity_id,invest_param,transfer_param_code " +
            " from ndr_subject  where newbie_only = 1 AND is_visiable = 1 AND open_flag=1 AND push_status=1 AND raise_status in(0,4)"
            +"  AND  open_channel in(1,3,5,7)"
            + "<if test=\"type != null and type != ''\">" +
            "      AND type= #{type}" +
            "  </if>"
            + "<if test=\"minMonth != null\">" +
            "      AND term &gt;= #{minMonth}" +
            "  </if>"
            + "<if test=\"maxMonth != null\">" +
            "      AND term &lt;= #{maxMonth}" +
            "  </if>"
            +" ORDER BY raise_status DESC, id DESC"
            + "</script>"
    )
    List<Subject> findSubjectNewBie1(@Param("type") String type, @Param(value = "minMonth") Integer minMonth ,@Param(value = "maxMonth") Integer maxMonth);

    /**
     * 查询散标列表普通标募集中的
     * @return
     */
    @Select("<script>"
            +"SELECT id,subject_id,name,rate,invest_rate,bonus_rate,term,total_amt,available_amt,repay_type,raise_status," +
            "open_time,close_time,newbie_only,period,type,activity_id,invest_param ,transfer_param_code" +
            " from ndr_subject  where newbie_only = 0 AND is_visiable = 1 AND open_flag=1 AND push_status=1 AND raise_status in(0,4)"
            +"  AND  open_channel in(1,3,5,7)"
            + "<if test=\"type != null and type != ''\">" +
            "      AND type= #{type}" +
            "  </if>"
            + "<if test=\"minMonth != null\">" +
            "      AND term &gt;= #{minMonth}" +
            "  </if>"
            + "<if test=\"maxMonth != null\">" +
            "      AND term &lt;= #{maxMonth}" +
            "  </if>"
            +" ORDER BY raise_status ,sort_num DESC, id DESC"
            + "</script>"
    )
    List<Subject> findSubjectGeneral(@Param("type") String type, @Param(value = "minMonth") Integer minMonth ,@Param(value = "maxMonth") Integer maxMonth);

    /**
     * 查询散标列表普通标募集完成的
     * @return
     */
    @Select("<script>"
            +"SELECT id,subject_id,name,rate,invest_rate,bonus_rate,term,total_amt,available_amt,repay_type,raise_status," +
            "open_time,close_time,newbie_only,period,type,activity_id,invest_param ,transfer_param_code" +
            " from ndr_subject force index (PRIMARY) where is_visiable = 1 AND open_flag=1 AND push_status=1 AND raise_status in(1,3)"
            +"  AND  open_channel in(1,3,5,7)"
            + "<if test=\"type != null and type != ''\">" +
            "      AND type= #{type}" +
            "  </if>"
            + "<if test=\"minMonth != null\">" +
            "      AND term &gt;= #{minMonth}" +
            "  </if>"
            + "<if test=\"maxMonth != null\">" +
            "      AND term &lt;= #{maxMonth}" +
            "  </if>"
            +" ORDER BY id DESC limit 50"
            + "</script>"
    )
    List<Subject> findSubjectFinish(@Param("type") String type, @Param(value = "minMonth") Integer minMonth ,@Param(value = "maxMonth") Integer maxMonth);

    
    /**
     * 查询散标列表普通标募集完成的
     * @return
     */
    @Select("<script>"
            +"SELECT id,subject_id,name,rate,invest_rate,bonus_rate,term,total_amt,available_amt,repay_type,raise_status," +
            "open_time,close_time,newbie_only,period,type,activity_id,invest_param ,transfer_param_code" +
            " from ndr_subject force index (PRIMARY) where newbie_only = 0 AND is_visiable = 1 AND open_flag=1 AND push_status=1 AND raise_status in(1,3)"
            +"  AND  open_channel in(1,3,5,7)"
            + "<if test=\"type != null and type != ''\">" +
            "      AND type= #{type}" +
            "  </if>"
            + "<if test=\"minMonth != null\">" +
            "      AND term &gt;= #{minMonth}" +
            "  </if>"
            + "<if test=\"maxMonth != null\">" +
            "      AND term &lt;= #{maxMonth}" +
            "  </if>"
            +" ORDER BY id DESC limit 50"
            + "</script>"
    )
    List<Subject> findSubjectFinishNoAnyNewBie(@Param("type") String type, @Param(value = "minMonth") Integer minMonth ,@Param(value = "maxMonth") Integer maxMonth);
    
    
    @Select("<script>"
            +"SELECT * from ("
            + "SELECT * from (select * from ndr_subject i where i.newbie_only = 1 AND i.is_visiable = 1 AND i.open_flag=1 AND i.push_status=1 AND i.raise_status in(0,4) ORDER BY i.raise_status asc, i.open_time DESC ) AS a "
            +"UNION (select * from (select * from ndr_subject p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status in(0,4) ORDER BY p.raise_status DESC, p.open_time DESC) AS b ) "
            +"UNION (select * from (select * from ndr_subject p where p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status in(1,3) ORDER BY p.close_time DESC,p.open_time DESC) AS c )"
            +")as d where d.open_channel in(1,3,5,7) "
            + "<if test=\"type != null and type != ''\">" +
            "      AND d.type= #{type}" +
            "  </if>"
            + "<if test=\"minMonth != null\">" +
            "      AND d.term &gt;= #{minMonth}" +
            "  </if>"
            + "<if test=\"maxMonth != null\">" +
            "      AND d.term &lt;= #{maxMonth}" +
            "  </if>"
            + "</script>")
    List<Subject> findSubjectAppNewBie(@Param(value = "type") String type, @Param(value = "minMonth") Integer minMonth ,@Param(value = "maxMonth") Integer maxMonth);

    @Select("<script>"
            +"SELECT * from ("
            + "SELECT * from (select * from ndr_subject p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status in(0,4) ORDER BY p.raise_status asc, p.open_time DESC ) AS a "
            +"UNION (select * from (select * from ndr_subject p where p.newbie_only = 0 AND p.is_visiable = 1 AND p.open_flag=1 AND p.push_status=1 AND p.raise_status in(1,3) ORDER BY p.close_time DESC,p.open_time DESC) AS c )"
            +")as d where d.open_channel in(1,3,5,7) "
            + "<if test=\"type != null and type != ''\">" +
            "      AND d.type= #{type}" +
            "  </if>"
            + "<if test=\"minMonth != null\">" +
            "      AND d.term &gt;= #{minMonth}" +
            "  </if>"
            + "<if test=\"maxMonth != null\">" +
            "      AND d.term &lt;= #{maxMonth}" +
            "  </if>"
            + "</script>")
    List<Subject> findSubjectNoNewBie(@Param(value = "type") String type, @Param(value = "minMonth") Integer minMonth ,@Param(value = "maxMonth") Integer maxMonth);

    /**
     * 查询农贷借款信息
     * @param contractNo
     * @return
     */
    @Select("SELECT * FROM agriculture_loaninfo WHERE contract_id = #{contractNo}")
    List<AgricultureLoanInfo> findAgricultureLoanInformation(String contractNo);

    /**
     * 查询车贷借款信息
     * @param contractNo
     * @return
     */
    @Select("SELECT l.*,v.assess_price,v.brand,v.borrowing_purposes,v.item_address,v.kilometre_amount,v.license_plate_number,v.guarantee_rate,v.guarantee_type FROM loan_intermediaries l left join loan_vehicle_intermediaries v " +
            "on l.id=v.loan_id WHERE contract_id = #{contractNo}")
    List<LoanIntermediaries> findVehicleLoanInformation(String contractNo);

    /**
     * 查询车辆信息
     * @param subjectId
     * @return
     */
    @Select(value = "SELECT p.id,p.title,p.url,p.ods_update_time from loan_info_pics_intermediaries p LEFT JOIN loan_intermediaries l " +
            "on p.loan_id=l.id " +
            "LEFT JOIN ndr_subject n on n.contract_no= l.contract_id  " +
            "WHERE n.contract_no is not NULL and n.subject_id = #{subjectId} ORDER BY p.ods_update_time limit 10")
    List<VehicleInfoPic> findVehicleInfoPic(@Param(value="subjectId") String subjectId);
    /**
     * 查询开放到一键投状态是募集中的标的
     *
     */
    @Select(value = "select * from ndr_subject where iplan_id=#{iplanId} and raise_status = #{raiseStatus} and open_flag = 1 AND push_status = 1 and open_channel=8")
    List<Subject> findByIplanAndStatus(@Param(value = "iplanId")int iplanId,@Param(value = "raiseStatus") int raiseStatus);

    /**
     * 查询iplan下的subject
     * @param iPlanId
     * @return
     */
    @Select(value = "select * from ndr_subject where iplan_id=#{iPlanId}")
    List<Subject> getSubjectByIplanId(Integer iPlanId);
    @Select(value = "SELECT t.term as 'totalTerm',e.term as 'term',e.due_date as 'dueDate',e.due_principal as 'duePrincipal',e.due_interest as 'dueInterest',e.status as 'status' FROM ndr_subject t,ndr_subject_repay_schedule e WHERE t.subject_id = e.subject_id AND e.subject_id = #{subjectId}")
    List<SubjectRepayScheduleQuery> findLoanInfoBySubjectId(@Param(value="subjectId")String subjectId);

    @Select("SELECT cs.subject_id from change_subject cs\n" +
            "LEFT JOIN ndr_subject nn\n" +
            "on cs.subject_id = nn.subject_id\n" +
            "where nn.current_term > 1 and nn.open_channel = 2")
    List<String> getSubjectByRate();

    /**
     * 查询中转站原始利率
     */
    @Select(value = "SELECT rate FROM agriculture_loaninfo WHERE contract_id= #{contractId}")
    BigDecimal findRateByContractNoFromAgricultureLoaninfo(String contractId);

    /**
     * 查询车贷中转站原始利率
     */
    @Select(value = "SELECT rate FROM loan_intermediaries WHERE contract_id = #{contractId}")
    BigDecimal findRateByContractNoFromLoanIntermediaries(String contractId);

    /**
     * 查询企业贷借款信息
     * @param contractNo
     * @return
     */
    @Select(value = "SELECT '企业贷' as loan_type, '企业贷款' as borrow_type, al.loan_term, '月' as operation_type, '正常' as repay_status, " +
            "al.`name`, al.id_card as id_card, al.repayment_source AS revenue_source, al.age, al.money AS loan_amt, " +
            "al.annual_income as annualIncome,al.company_establishing_time,al.last_financial_situation,al.recent_financial_situation,"+
            "al.loan_application AS loan_usage,CONCAT(al.province,al.city,al.area) as area,al.repayment_guarantee," +
            "al.industry, al.wages, al.bonus, al.other_income, al.rental_income, al.living_expenses, al.education_expenses, " +
            "al.rental_payment, al.vehicle_use_fee, al.other_payment, al.daily_expenses_total, al.max_repayment_amount " +
            "FROM agriculture_loaninfo al WHERE al.contract_id = #{contractNo} limit 1")
	BorrowInfo findCompanyBorrowerInfoBySub(String contractNo);
}
