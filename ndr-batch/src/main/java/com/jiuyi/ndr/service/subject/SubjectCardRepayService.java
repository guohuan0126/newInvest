package com.jiuyi.ndr.service.subject;

import com.jiuyi.ndr.dao.subject.SubjectCardRepayBillDao;
import com.jiuyi.ndr.domain.subject.SubjectCardRepayBill;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lln on 2017/12/5.
 * 处理卡贷相关服务
 */
@Service
public class SubjectCardRepayService {
    @Autowired
    private SubjectCardRepayBillDao subjectCardRepayBillDao;

    /**
     * 根据状态查询
     * @param status
     * @return
     */
    public List<SubjectCardRepayBill> findByStatus(Integer status){
        if(status==null){
            throw new IllegalArgumentException("参数不能为空");
        }
        return subjectCardRepayBillDao.selectByStatus(status);
    }

    public int updateBysubjectId(Integer scheduleId,String subjectId,Integer status,Integer id){
        if (scheduleId == null ||StringUtils.isBlank(subjectId) || status == null || id == null) {
            throw new IllegalArgumentException("scheduleId and subjectId and status and id can not be null");
        }
        return subjectCardRepayBillDao.update(scheduleId,subjectId,status,id);
    }

    /**
     * 根据subjectIs和status查询
     * @param subjectId
     * @param status
     * @return
     */
    public List<SubjectCardRepayBill> findByStatusAndSubjectId(String subjectId, Integer status){
        return subjectCardRepayBillDao.selectByStatusAndSubjectId(subjectId,status);
    }

    public List<SubjectCardRepayBill> findBySubjectId(String subjectId){
        return subjectCardRepayBillDao.selectBySubjectId(subjectId);
    }

    public SubjectCardRepayBill findBySubjectIdAndType(String subjectId,String type){
        return subjectCardRepayBillDao.selectBySubjectIdAndType(subjectId,type);
    }

    /**
     * 借款信息
     */
    public Map<String, Integer> getBorrowerDetails(SubjectCardRepayBill cardRepayBill){
        Map<String, Integer> borrowerDetails = new HashMap<>();
        borrowerDetails.put("duePrincipal", cardRepayBill.getPrincipal());//当期应还本金
        borrowerDetails.put("dueInterest", cardRepayBill.getInterest());//当期应还利息
        borrowerDetails.put("duePenalty", cardRepayBill.getPenalty());//当期应还罚息
        borrowerDetails.put("dueFee", cardRepayBill.getFee());//当期应还费用
        return borrowerDetails;
    }

    /**
     * 根据日期查询需要还的卡贷项目
     * @param dueDate
     * @return
     */
    public List<SubjectRepaySchedule> findNotRepaySchedule(String dueDate){
        return subjectCardRepayBillDao.findNotRepay(dueDate);
    }

    /**
     * 查询状态不等于type的
     * @param subjectId
     * @param status
     * @param type
     * @return
     */
    public List<SubjectCardRepayBill> getByStatusAndSubjectIdAndType(String subjectId,Integer status,String type){
        return subjectCardRepayBillDao.selectByStatusAndSubjectIdAndType(subjectId,status,type);
    }

    public int updateStatusById(Integer status,Integer id){
        if (status == null || id == null) {
            throw new IllegalArgumentException("status and id can not be null");
        }
        return subjectCardRepayBillDao.updateStatusById(status,id);
    }
}
