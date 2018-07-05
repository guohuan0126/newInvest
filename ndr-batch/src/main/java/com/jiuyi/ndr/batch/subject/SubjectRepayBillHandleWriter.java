package com.jiuyi.ndr.batch.subject;

import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.service.subject.SubjectRepayBillService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;

public class SubjectRepayBillHandleWriter implements ItemWriter<SubjectRepayBill> {
    private static final Logger logger = LoggerFactory.getLogger(SubjectRepayBillHandleWriter.class);
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private SubjectRepayBillService subjectRepayBillService;

    @Override
    public void write(List<? extends SubjectRepayBill> subjectRepayBills) throws Exception {
        for (SubjectRepayBill subjectRepayBill : subjectRepayBills) {
            Subject subject = subjectService.getByContractNo(subjectRepayBill.getContractId());
            if(subject!=null){
                String subjectId = subject.getSubjectId();//查询标的号
                SubjectRepaySchedule subjectRepaySchedule = subjectRepayScheduleService.findRepaySchedule(subjectId, subjectRepayBill.getTerm());
                Integer scheduleId =subjectRepaySchedule.getId();
                boolean newFlag = Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())&&subject.getInvestRate()!=null && subject.getRate().equals(subject.getInvestRate()) && subject.getRate().compareTo(BigDecimal.valueOf(0.144))<0 && subject.getRate().compareTo(BigDecimal.valueOf(0.068))>0;
                if(!newFlag&&Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())&&Subject.SUBJECT_TYPE_CAR.equals(subject.getType())&&Subject.REPAY_TYPE_IFPA.equals(subject.getRepayType())&&SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus())) {
                    //对应当期未还
                    if(SubjectRepaySchedule.STATUS_NOT_REPAY.equals(subjectRepaySchedule.getStatus()) && SubjectRepaySchedule.SIGN_NOT_REPAY.equals(subjectRepaySchedule.getIsRepay())) {
                        //差值
                        Integer differenceValue = subjectRepaySchedule.getDueInterest() - subjectRepayBill.getDueInterest();
                        if(differenceValue>0){
                            subjectRepayBill.setDueInterest(subjectRepayBill.getDueInterest()+differenceValue);
                        }
                    }
                }
                //若是卡贷,非直贷二且是提前结清的,将状态更新成3
                if((Subject.SUBJECT_TYPE_CASH.equals(subject.getType())||Subject.SUBJECT_TYPE_CARD.equals(subject.getType()))&&!Subject.DIRECT_FLAG_YES_01.equals(subject.getDirectFlag())){
                    subjectRepayBillService.update(scheduleId, subjectId, 3, subjectRepayBill.getId());
                }else{
                    subjectRepayBill.setStatus(SubjectRepayBill.STATUS_NOT_REPAY);
                    subjectRepayBill.setScheduleId(scheduleId);
                    subjectRepayBill.setSubjectId(subjectId);
                    subjectRepayBillService.update(subjectRepayBill);
//                    subjectRepayBillService.update(scheduleId, subjectId, SubjectRepayBill.STATUS_NOT_REPAY, subjectRepayBill.getId());
                }
            }else{
                logger.info("处理直贷二还款数据异常,合同号{}找不到对应标的信息",subjectRepayBill.getContractId());
            }
        }
    }

}
