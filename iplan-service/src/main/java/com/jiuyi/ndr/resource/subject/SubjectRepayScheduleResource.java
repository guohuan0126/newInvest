package com.jiuyi.ndr.resource.subject;

import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.domain.account.PlatformAccount;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectRepaySchedule;
import com.jiuyi.ndr.dto.subject.SubjectRepayScheduleDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.service.account.PlatformAccountService;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.subject.SubjectRepayScheduleService;
import com.jiuyi.ndr.service.subject.SubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class SubjectRepayScheduleResource {

    private static Logger logger = LoggerFactory.getLogger(SubjectRepayScheduleResource.class);

    @Autowired
    private SubjectRepayScheduleService repayScheduleService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private PlatformTransferService platformTransferService;

    @GetMapping("/repaySchedules")
    public RestResponse<List<SubjectRepayScheduleDto>> getSubjectRepaySchedules(
            @RequestParam(value = "subjectName", required = false) String subjectName,
            @RequestParam(value = "isDirect", required = false) String isDirect,
            @RequestParam(value = "intermediatorId") String intermediatorId,
            @RequestParam(value = "openChannel") String openChannel,
            @RequestParam(value = "startDate", required = false) String startDate,
            @RequestParam(value = "endDate", required = false) String endDate) {
        Map<String, String> conditions = new HashMap<>();
        conditions.put("subjectName", subjectName);
        conditions.put("isDirect", isDirect);
        conditions.put("intermediatorId", intermediatorId);
        conditions.put("openChannel", openChannel);
        conditions.put("startDate", startDate);
        conditions.put("endDate", endDate);
        List<SubjectRepaySchedule> subjectRepaySchedules = repayScheduleService.findByConditions(conditions);
        List<SubjectRepayScheduleDto> subjectRepayScheduleDtos = new ArrayList<>();
        for (SubjectRepaySchedule subjectRepaySchedule : subjectRepaySchedules) {
            SubjectRepayScheduleDto subjectRepayScheduleDto = new SubjectRepayScheduleDto();
            BeanUtils.copyProperties(subjectRepaySchedule, subjectRepayScheduleDto);
            subjectRepayScheduleDto.setSubjectName(subjectRepaySchedule.getExtSn());
            subjectRepayScheduleDto.setDirectFlag(subjectRepaySchedule.getExtStatus());
            subjectRepayScheduleDtos.add(subjectRepayScheduleDto);
        }
        return new RestResponseBuilder<List<SubjectRepayScheduleDto>>().success(subjectRepayScheduleDtos);
    }

    @PostMapping("/repaySchedules/manual")
    @Transactional
    public RestResponse repayManual(@RequestBody Map<String, String> params) {
        String userId = params.get("userId");
        String subjectIdAndTerms = params.get("subjectIdAndTerms");
        Double amt = Double.valueOf(params.get("amt"));
        Integer isDirect = Integer.valueOf(params.get("isDirect"));
        if (Subject.DIRECT_FLAG_YES.equals(isDirect)) {
            //查询营销款账户金额是否充足
            //PlatformAccount repayer = platformAccountService.getPlatformAccount(GlobalConfig.MARKETING_ACCOUNT_01_DR);
            double totalActualMoney = platformTransferService.selectTotalSctualMoneyByInterviewerId(userId);
            if (amt > totalActualMoney) {
                throw new ProcessException(Error.NDR_0424.getCode(), Error.NDR_0424.getMessage() + ", 营销款账户" + GlobalConfig.MARKETING_ACCOUNT_01_DR);
            }
        } else {
            //查询居间人账户是否充足
            UserAccount repayer = userAccountService.getUserAccount(userId);
            if (amt > repayer.getAvailableBalance()) {
                throw new ProcessException(Error.NDR_0425.getCode(), Error.NDR_0425.getMessage() + ", 居间人" + userId);
            }
        }
        String[] subjectIdAndTermArr = subjectIdAndTerms.split(";");
        Arrays.asList(subjectIdAndTermArr).parallelStream().forEach(subjectIdAndTermStr->{
            String[] subjectIdAndTerm = subjectIdAndTermStr.split(",");
            String subjectId = subjectIdAndTerm[0];
            Integer term = Integer.valueOf(subjectIdAndTerm[1]);
            //正常还款打标记
            repayScheduleService.markRepaySubject(subjectId,term);
        });
        return new RestResponseBuilder<>().success(null);
    }

}
