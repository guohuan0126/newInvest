package com.jiuyi.ndr.resource.subject;

import com.duanrong.util.jedis.DRJedisCacheUtil;
import com.jiuyi.ndr.constant.CreditConstant;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanRepayDetail;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.transferstation.AgricultureLoanInfo;
import com.jiuyi.ndr.domain.transferstation.LoanIntermediaries;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.credit.CreditManageHoldDto;
import com.jiuyi.ndr.dto.credit.CreditTransferDetailDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditFinishDetailDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditManageFinishDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditManageHoldDto;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferFinishDetailDto;
import com.jiuyi.ndr.dto.subject.*;
import com.jiuyi.ndr.dto.subject.mobile.SubjectAppPurchaseDto;
import com.jiuyi.ndr.dto.subject.mobile.SubjectAppRepayDetailDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.resource.subject.mobile.SubjectMobileResource;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.PageData;
import com.jiuyi.ndr.service.account.PlatformTransferService;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayDetailService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.*;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.PageUtil;
import com.jiuyi.ndr.util.redis.RedisCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class SubjectResource {

    private final static Logger logger = LoggerFactory.getLogger(SubjectResource.class);

    @Autowired
    private SubjectService subjectService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;
    @Autowired
    private SubjectAdvancedPayOffService subjectAdvancedPayOffService;
    @Autowired
    private SubjectPayoffRegService payoffRegService;
    @Autowired
    private PlatformTransferService platformTransferService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private InvestService investService;
    @Autowired
    private SubjectInvestParamService subjectInvestParamService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private SubjectTransLogService subjectTransLogService;
    @Autowired
    private UserService userService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private SubjectTransferParamService subjectTransferParamService;
    @Autowired
    private SubjectRepayDetailService subjectRepayDetailService;
    @Autowired
    private IPlanRepayDetailService iPlanRepayDetailService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private ConfigService configService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;

    @Value("${duanrong.subject.protocolUrl}")
    private String protocolUrl;
    @Value("${duanrong.subject.detailUrl}")
    private String detailUrl;   //债权信息
    @Value("${duanrong.subject.transferUrl}")
    private String transferUrl;//转让协议

    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private DecimalFormat df = new DecimalFormat("######0.00");

    @RequestMapping("/iplan/subject/repay/{subjectId}/{term}")
    public RestResponse<Void> subjectRepay(@PathVariable("subjectId") String subjectId, @PathVariable("term") Integer term){
        subjectService.repay(subjectId, term);
        return new RestResponseBuilder<Void>().success(null);
    }

    @RequestMapping("/iplan/subject/advancedPayOff/{subjectId}")
    public RestResponse<Void> advancedPayOff(@PathVariable("subjectId") String subjectId){
        subjectService.advancedPayOff(subjectId);
        return new RestResponseBuilder<Void>().success(null);
    }

    @GetMapping("/subject/payoff")
    public RestResponse<List> getNeedPayoffSubject(
            @RequestParam(value = "subjectName", required = false) String subjectName,
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "openChannel") String openChannel,
            @RequestParam(value = "isDirect", required = false) String isDirect) {
        Map<String, String> conditions = new HashMap<>();
        conditions.put("subjectName", subjectName);
        conditions.put("intermediatorId", userId);
        conditions.put("isDirect", isDirect);
        conditions.put("openChannel", openChannel);
        List<SubjectPayoffReg> subjectPayoffRegs = payoffRegService.findByConditions(conditions);
        List<SubjectPayoffDto> payoffDtos = new ArrayList<>();
        for (SubjectPayoffReg subjectPayoffReg : subjectPayoffRegs) {
            List<SubjectRepaySchedule> needPayOffRepaySchedules = repayScheduleService.findRepayScheduleBySubjectId(subjectPayoffReg.getSubjectId())
                    .stream().filter(repaySchedule -> repaySchedule.getStatus().equals(SubjectRepaySchedule.STATUS_NOT_REPAY)).collect(Collectors.toList());
            int residualPrincipal = needPayOffRepaySchedules.stream().map(SubjectRepaySchedule :: getDuePrincipal).reduce((a, b) -> a + b).orElse(0);
            //计算提前结清罚息
            int payOffPenalty = subjectAdvancedPayOffService.advancedPayOff(subjectPayoffReg.getSubjectId());
            int residualInterest = needPayOffRepaySchedules.get(0).getDueInterest() + payOffPenalty;
            //int residualInterest = needPayOffRepaySchedules.stream().map(SubjectRepaySchedule :: getDueInterest).reduce((a, b) -> a + b).orElse(0);
            //int residualPenalty = needPayOffRepaySchedules.stream().map(SubjectRepaySchedule :: getDuePenalty).reduce((a, b) -> a + b).orElse(0);
            //int residualFee = needPayOffRepaySchedules.stream().map(SubjectRepaySchedule :: getDueFee).reduce((a, b) -> a + b).orElse(0);
            Subject subject = subjectService.getBySubjectId(subjectPayoffReg.getSubjectId());
            SubjectPayoffDto payoffDto = new SubjectPayoffDto();
            payoffDto.setSubjectId(subjectPayoffReg.getSubjectId());
            payoffDto.setSubjectName(subject.getName());
            payoffDto.setResidualPrincipal(residualPrincipal / 100.0);
            payoffDto.setResidualInterest(residualInterest / 100.0);
            payoffDto.setDirectFlag(subject.getDirectFlag());
            payoffDto.setRepayType(subject.getRepayType());
            payoffDto.setStatus(needPayOffRepaySchedules.get(0).getStatus());
            payoffDto.setOpenChannel(subjectPayoffReg.getOpenChannel());
            payoffDto.setResidualTerm(needPayOffRepaySchedules.size());
            payoffDto.setOffLineEndDate(subjectPayoffReg.getRepayDate());
            payoffDto.setOnLineEndDate(needPayOffRepaySchedules.get(payoffDto.getResidualTerm() - 1).getDueDate());
            payoffDtos.add(payoffDto);
        }
        return new RestResponseBuilder<List>().success(payoffDtos);
    }

    @PostMapping("/subject/payoff")
    public RestResponse<Void> batchPayoff(@RequestBody Map<String, String> params) {
        String subjectIds = params.get("subjectIds");
        String intermediatorId = params.get("userId");
        Double amt = Double.valueOf(params.get("amt"));
        Integer isDirect = Integer.valueOf(params.get("isDirect"));
        Integer openChannel = Integer.valueOf(params.get("openChannel"));
        if (!StringUtils.hasText(subjectIds)) {
            throw new IllegalArgumentException("标的id不能为空");
        }
        if (!StringUtils.hasText(intermediatorId)) {
            throw new IllegalArgumentException("居间人id不能为空");
        }
        if (Subject.DIRECT_FLAG_YES.equals(isDirect)) {
            //查询居间人在营销款账户金额是否充足
            double totalActualMoney = platformTransferService.selectTotalSctualMoneyByInterviewerId(intermediatorId);
            //PlatformAccount repayer = platformAccountService.getPlatformAccount(GlobalConfig.MARKETING_ACCOUNT_01_DR);
            if (amt > totalActualMoney) {
                throw new ProcessException(Error.NDR_0424.getCode(), Error.NDR_0424.getMessage() + ", 营销款账户" + GlobalConfig.MARKETING_ACCOUNT_01_DR);
            }
        } else {
            //查询居间人账户是否充足
            UserAccount repayer = userAccountService.getUserAccount(intermediatorId);
            if (amt > repayer.getAvailableBalance()) {
                throw new ProcessException(Error.NDR_0425.getCode(), Error.NDR_0425.getMessage() + ", 居间人" + intermediatorId);
            }
        }
        String[] subjectIdsArr = subjectIds.trim().split(",");
        for (String subjectId : subjectIdsArr) {
            Subject subject = subjectService.getBySubjectId(subjectId);
            if (subject == null) {
                throw new ProcessException(Error.NDR_0403.getCode(), Error.NDR_0403.getMessage() + ", " + subjectId);
            }
            if (!subject.getIntermediatorId().equals(intermediatorId.trim())) {
                throw new ProcessException(Error.NDR_0423.getCode(), Error.NDR_0423.getMessage() + ", 标的居间人" + subject.getIntermediatorId() + ", 还款居间人" + intermediatorId);
            }
        }
        for (String subjectId : subjectIdsArr) {
            repayScheduleService.markAdvanceSubject(subjectId);
        }
        logger.warn("提前结清[{}]条", subjectIdsArr.length);
        return new RestResponseBuilder().success(null);
    }

    @GetMapping("/subject/{subjectId}/borrower/info")
    public RestResponse<BorrowInfo> getBorrowerInfo(@PathVariable("subjectId") String subjectId,
                                                         @RequestParam(value = "creditId") int creditId) {
        BorrowInfo borrowerInfo = subjectService.getBorrowerInfo(subjectId, creditId);
        return new RestResponseBuilder<BorrowInfo>().success(borrowerInfo);
    }

    @GetMapping("/iplan/subject/list")
    public RestResponse getSubjectList(@RequestParam(value = "iPlanCode", defaultValue = "0", required = false) String iPlanCode) {
        List<SubjectDto> subjectDtos = subjectService.getSubjectInIplan(iPlanCode);
        return new RestResponseBuilder<>().success(subjectDtos);
    }

    @GetMapping(value = "/iplan/subject/riskGrade")
    public RestResponse getSubjectRiskGrade(@RequestParam(value = "subjectId") String subjectId) {
        Map<String, String> result = subjectService.getSubjectRiskGradeBySubjectId(subjectId);
        return new RestResponseBuilder<>().success(result);
    }
    //pc端-新散标列表
    @GetMapping("/pc/subject/list")
    public RestResponse getSubject(   @RequestParam("pageNo") int pageNum,
                                    @RequestParam("pageSize") int pageSize,
                                    @RequestParam(value = "userId", required = false) String userId,
                                    @RequestParam(value = "type") String type) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 15;
        }
        //PageHelper.startPage(pageNum, pageSize);
        Double newbieUsable  = investService.getNewbieUsable(userId,null);
        List<Subject> allVisibleSubject=null;
        if (!StringUtils.isEmpty(userId)) {
            if(newbieUsable>0){//有额度
            	allVisibleSubject = subjectService.findSubjectNewBieAll(type,pageNum,pageSize);
            }else{
            	allVisibleSubject = subjectService.findSubjectNOAnyNewBie(type,pageNum,pageSize);
            	
            }
        }else{
        	allVisibleSubject = subjectService.findSubjectNewBieAll(type,pageNum,pageSize);
        }
       
        
        List<SubjectDto> subjectList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(allVisibleSubject)) {
            for (Subject subject : allVisibleSubject) {
                SubjectDto subjectDto = new SubjectDto();
                BeanUtils.copyProperties(subject, subjectDto);
                Integer activityId = subject.getActivityId();
                if (activityId != null) {
                    ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
                    if(amc!=null) {
                        subjectDto.setActivityName(amc.getActivityName());
                        subjectDto.setIncreaseInterest(amc.getIncreaseInterest());
                        subjectDto.setFontColor(amc.getFontColorPc());
                        subjectDto.setBackground(amc.getBackgroundPc());
                        if(amc.getIncreaseTerm() != null){
                            subjectDto.setAddTerm(amc.getIncreaseTerm());
                        }else{
                            subjectDto.setAddTerm(0);
                        }
                    }
                }
                //新手标额度固定
                subjectDto.setNewbieAmt(investService.getNewbieAmt(null)<0D?0D:investService.getNewbieAmt(null));
                //剩余新手额度
                subjectDto.setRemainNewbieAmt(newbieUsable/100.0<0D?0D:newbieUsable/100.0);
                if(subject.getPeriod()<30){
                    subjectDto.setSubjectType("天标");
                }else{
                    subjectDto.setSubjectType("月标");
                }
                //还款类型
                subjectDto.setSubjectRepayType(SubjectMobileResource.repayType(subject.getRepayType()));

                String transferParamCode=subject.getTransferParamCode();
                String exitLockDaysStr=DRJedisCacheUtil.get("LD"+transferParamCode);
                if(exitLockDaysStr!=null){
                	subjectDto.setExitLockDaysStr(exitLockDaysStr);
                }else{
                	 SubjectTransferParam sp=subjectTransferParamService.getByTransferParamCode(transferParamCode);
                     //返回锁定期
                       if(sp!=null&&sp.getFullInitiateTransfer()!=null){
                       	if(sp.getFullInitiateTransfer()<31){
                       		subjectDto.setExitLockDaysStr(sp.getFullInitiateTransfer()+"天");
                       	}else{
                       		int month=sp.getFullInitiateTransfer()/31;
                       		subjectDto.setExitLockDaysStr(month+"个月");
                       	}
                       	DRJedisCacheUtil.set("LD"+transferParamCode, subjectDto.getExitLockDaysStr(), 24*60*60);
                       }else{
                       	subjectDto.setExitLockDaysStr("0天");
                       }
                }
                subjectList.add(subjectDto);
            }
        }
        List<SubjectDto> list = new PageUtil().ListSplit(subjectList, pageNum, pageSize);
        PageData<SubjectDto> pageData = new PageData<>();
        pageData.setList(list);
        pageData.setPage(pageNum);
        pageData.setSize(pageSize);
        pageData.setTotalPages(subjectList.size() % pageSize != 0 ? subjectList.size() / pageSize + 1:subjectList.size() / pageSize);
        pageData.setTotal(subjectList.size());

        return new RestResponseBuilder<>().success(pageData);


    }


    /**
     *pc散标详情页
     * @param subjectId
     * @return
     */
    @GetMapping("/pc/subject/{subjectId}/details")
    public RestResponse<SubjectDto> getSubjectDetails(@PathVariable("subjectId") String subjectId,
                                                      @RequestParam(value = "userId",required = false) String userId
                                                      ) {
        if(subjectId==null){
            throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage() + ", " + subjectId);
        }
        SubjectDto subjectDto = new SubjectDto();
        Subject subject = subjectService.getBySubjectId(subjectId);
        if(subject==null){
            logger.warn("该散标[id={}]不存在", subjectId);
            throw new ProcessException(Error.NDR_0805);
        }
        BeanUtils.copyProperties(subject, subjectDto);
        subjectDto.setTotalAmt(subject.getTotalAmt());
        subjectDto.setAvailableAmt(subject.getAvailableAmt());
        subjectDto.setSubjectRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
        Integer activityId = subject.getActivityId();
        if (null != activityId) {
            ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
            subjectDto.setActivityName(amc.getActivityName());
            subjectDto.setIncreaseInterest(amc.getIncreaseInterest());
            subjectDto.setFontColor(amc.getFontColorPc());
            subjectDto.setBackground(amc.getBackgroundPc());
            if(amc.getIncreaseTerm() != null){
                subjectDto.setAddTerm(amc.getIncreaseTerm());
            }else{
                subjectDto.setAddTerm(0);
            }
        }

        Double newbieUsable = 0D;
        //if (!StringUtils.isEmpty(userId)) {
            newbieUsable = investService.getNewbieUsable(userId,null);
        //}
        //新手标额度固定
        subjectDto.setNewbieAmt(investService.getNewbieAmt(null));
        //剩余新手额度
        subjectDto.setRemainNewbieAmt(newbieUsable/100.0<0D?0D:newbieUsable/100.0);
        Config iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);
        if(iPlanNewbieAmtConfig==null || "0".equals(iPlanNewbieAmtConfig.getValue().toString())){
        	subjectDto.setNewBieTip("(限散标项目)");
        }else{
        	subjectDto.setNewBieTip("(限任意定期项目)");
        }
        subjectDto.setNewbieOnly(subject.getNewbieOnly());
        SubjectInvestParamDef param = subjectInvestParamService.getInvestParamDef(subject.getInvestParam());
        if(param!=null){
            subjectDto.setInvestOriginMoney(param.getMinAmt());
            subjectDto.setInvestIncreaseMoney(param.getIncrementAmt());
            subjectDto.setInvestMaxMoney(param.getMaxAmt());
        }


        if(subject.getPeriod()<30){
            subjectDto.setSubjectType("天标");
        }else{
            subjectDto.setSubjectType("月标");
        }
        //查询车贷借款信息
        List<LoanIntermediaries> loanIntermediaries = subjectService.getVehicleLoanInformation(subject.getContractNo());
        String  guaranteeType ="";
        if(!loanIntermediaries.isEmpty()){
              guaranteeType =loanIntermediaries.get(0).getGuaranteeType();
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(guaranteeType) && "A".equals(guaranteeType)){
            subjectDto.setGuaranteeType("质押");
        }else{
            subjectDto.setGuaranteeType("抵押");
        }
        subjectDto.setOpenTime(DateUtil.parseDateTime(subject.getOpenTime(), DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        //红包列表
        List<SubjectAppPurchaseDto.RedPacketApp> redPacketAppList = new ArrayList<>();
        IPlan iPlan = new IPlan();
        iPlan.setId(subject.getId());
        iPlan.setNewbieOnly(subject.getNewbieOnly());
        iPlan.setRateType(0);
        iPlan.setFixRate(subject.getInvestRate());
        iPlan.setTerm(subject.getTerm());
        iPlan.setActivityId(subject.getActivityId());
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userId)){
            List<RedPacket> redPacketList = redPacketService.getUsablePacketCreditAll(userId, subject, "pc","subject");
            if (!CollectionUtils.isEmpty(redPacketList)) {
                for (RedPacket redPacket : redPacketList) {
                    SubjectAppPurchaseDto.RedPacketApp redPacketApp = new SubjectAppPurchaseDto.RedPacketApp();
                    redPacketApp.setId(redPacket.getId());
                    redPacketApp.setAmt(redPacket.getMoney());
                    redPacketApp.setAmt2(df.format(redPacket.getMoney()));
                    redPacketApp.setRate(redPacket.getRate());
                    redPacketApp.setRate2(df.format(redPacket.getRate() * 100) + "%");
                    redPacketApp.setRateDay(redPacket.getRateDay());
                    redPacketApp.setName(redPacket.getName());
                    redPacketApp.setDeadLine(redPacket.getDeadLine().substring(0,10));
                    redPacketApp.setIntroduction(redPacketService.getRedPacketInvestMoneyStr(redPacket));
                    redPacketApp.setType(redPacket.getType());
                    redPacketApp.setRuleId(redPacket.getRuleId());
                    redPacketApp.setInvestMoney(redPacket.getInvestMoney());
                    redPacketApp.setUseStatus(redPacket.getUseStatus());
                    redPacketAppList.add(redPacketApp);
                }
            }
            subjectDto.setRedPacketAppList(redPacketAppList);
        }

        String transferParamCode=subject.getTransferParamCode();
        String exitLockDaysStr=DRJedisCacheUtil.get("LD2"+transferParamCode);
        if(exitLockDaysStr!=null){
        	subjectDto.setExitLockDaysStr(exitLockDaysStr);
        }else{
        	 SubjectTransferParam sp=subjectTransferParamService.getByTransferParamCode(transferParamCode);
             //返回锁定期
               if(sp!=null&&sp.getFullInitiateTransfer()!=null){
               	//if(sp.getFullInitiateTransfer()<31){
               		subjectDto.setExitLockDaysStr(sp.getFullInitiateTransfer()+"天");
               	/*}else{
               		int month=sp.getFullInitiateTransfer()/31;
               		subjectDto.setExitLockDaysStr(month+"个月");
               	}*/
               		DRJedisCacheUtil.set("LD2"+transferParamCode, subjectDto.getExitLockDaysStr(), 24*60*60);
               }else{
               	subjectDto.setExitLockDaysStr("0天");
               }
        }

        return new RestResponseBuilder<SubjectDto>().success(subjectDto);
    }

    /**
     *pc还款计划
     * @param subjectId
     * @return
     */
    @GetMapping("/pc/subject/{subjectId}/repay/schedule")
    public RestResponse<List<SubjectRepayScheduleDto>> getSubjectRepaySchedule(@PathVariable("subjectId") String subjectId) {
        if(subjectId==null){
            throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage() + ", " + subjectId);
        }
        Subject subject = subjectService.getBySubjectId(subjectId);
        //查询某个标的还款计划
        List<SubjectRepaySchedule> scheduleList = subjectRepayScheduleService.findRepayScheduleBySubjectId(subjectId);
        List<SubjectRepayScheduleDto> schedules = new ArrayList<>();
        for (SubjectRepaySchedule schedule:scheduleList) {
            SubjectRepayScheduleDto subjectRepayScheduleDto = new SubjectRepayScheduleDto();
            BeanUtils.copyProperties(schedule, subjectRepayScheduleDto);
            subjectRepayScheduleDto.setDueDate(schedule.getDueDate().substring(0,4) +"-"+schedule.getDueDate().substring(4,6)+"-"+schedule.getDueDate().substring(6,8));
            subjectRepayScheduleDto.setTotalTerm(subject.getTerm());
            subjectRepayScheduleDto.setTotalAmt(schedule.getDuePrincipal()+schedule.getDueInterest());
            schedules.add(subjectRepayScheduleDto);
        }
        return new RestResponseBuilder<List<SubjectRepayScheduleDto>>().success(schedules);
    }

    //投资达人
    @GetMapping("/subject/{subjectId}/investor")
    public RestResponse<List> getIPlanTalent(@PathVariable("subjectId") String subjectId) {
        List<SubjectInvestorDto> subjectInvestorDtos = subjectService.getInvestorAcct(subjectId);
        return new RestResponseBuilder<List>().success(subjectInvestorDtos);
    }

    /**
     * 车辆图片信息
     * @param subjectId
     * @return
     */
    @GetMapping("/subject/{subjectId}/vehicleInfoPic")
    public RestResponse<List> getVehicleInfoPic(@PathVariable("subjectId") String subjectId) {
       if(StringUtils.isEmpty(subjectId)){
           throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage() + ", " + subjectId);
       }
       List<VehicleInfoPic>  pics =  (List<VehicleInfoPic>) DRJedisCacheUtil.hget(
                RedisCacheKey.SUBJECT + subjectId, VehicleInfoPic.class, RedisCacheKey.SUBJECT_PICS).get(RedisCacheKey.SUBJECT_PICS);
        if(pics == null || pics.isEmpty()){
            pics = subjectService.getVehicleInfoPic(subjectId);
            if(pics != null && !pics.isEmpty()){
                Map<String, Object> hash = new HashMap<>();
                hash.put(RedisCacheKey.SUBJECT_PICS, pics);
                DRJedisCacheUtil.hset(RedisCacheKey.SUBJECT + subjectId, hash);
            }
        }
       // List<VehicleInfoPic> vehicleInfoPicList= subjectService.getVehicleInfoPic(subjectId);
       return new RestResponseBuilder<List>().success(pics);

    }

    /**
     * 更新标的表数据
     * @param subject
     * @return
     */
    @PutMapping("/subject/updateSubject")
    public RestResponse<Subject> updateSubjectRateParam(@RequestBody Subject subject) {
        Subject subjectParam = new Subject();
        BeanUtils.copyProperties(subject, subjectParam);
        subject = subjectService.update(subjectParam);
        return new RestResponseBuilder<Subject>().success(subject);
    }

    /**
     * 查询散标借款人信息
     * @param subjectId
     * @return
     */
    //
    @GetMapping("/subject/{subjectId}/subjectLoanInformation")
    public RestResponse<List> getSubjectLoanInformation(@PathVariable("subjectId") String subjectId) {
        //通过标的号查询
        Subject subject = subjectService.getBySubjectId(subjectId);
        //贷款类型
        String type = subject.getType();
        //合同号
        String contractNo = subject.getContractNo();
        //如果是车贷就去车贷中转站查询数据，否则就去农贷中转站查
        if (Subject.SUBJECT_TYPE_CAR.equals(type)){
            //查询车贷借款信息
            List<LoanIntermediaries> loanIntermediaries = subjectService.getVehicleLoanInformation(contractNo);
            return new RestResponseBuilder<List>().success(loanIntermediaries);
        }else{
            //查询农贷借款信息
            List<AgricultureLoanInfo> agricultureLoanInformation = subjectService.getAgricultureLoanInformation(contractNo);
            return new RestResponseBuilder<List>().success(agricultureLoanInformation);
        }
    }

    /**
     * pc还款日历(根据userId)
     * @param userId
     * @return
     */
    @GetMapping("/pc/subject/repay/schedule")
    public RestResponse<List<SubjectRepayDetailDto>> getSubjectRepayDetail(@RequestParam("userId") String userId) {
        //散标未还款计划
        List<SubjectRepayDetailDto> details = new ArrayList<>();
        //查询散标未还款债权
        List<Credit> creditList = creditService.findByUserIdAndChannelsInAndStatusForRepay(userId,new HashSet<>(Arrays.asList(Credit.SOURCE_CHANNEL_SUBJECT,Credit.SOURCE_CHANNEL_YJT)),Credit.CREDIT_STATUS_HOLDING);
        //根据标的id分组
        Map<String, List<Credit>> maps = creditList.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
        for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
            String subjectId = entry.getKey();
            Subject subject = subjectService.getBySubjectId(subjectId);
            //根据subjectId查询未还还款计划
            List<SubjectRepaySchedule> schedules = repayScheduleService.getSubjectRepayScheduleBySubjectIdNotRepay(subjectId);
            Integer totalPrincipal = 0;
            //获取这个散标下所有的债权
            List<Credit> creditArray = entry.getValue();
            if(!schedules.isEmpty()) {
                for (Credit credit:creditArray) {
                    Integer principal = credit.getHoldingPrincipal();
                    totalPrincipal = schedules.stream().map(SubjectRepaySchedule::getDuePrincipal).reduce(Integer::sum).orElse(0);
                    for (SubjectRepaySchedule schedule:schedules) {
                        if(principal>0){
                            SubjectRepayDetail repayDetail =  repayScheduleService.repayDetailBySchedule(schedule,userId,credit,principal,totalPrincipal);
                            if(repayDetail==null){
                                break;
                            }
                            Integer currTerm = repayDetail.getTerm();
                            SubjectRepayDetailDto dto = new SubjectRepayDetailDto();
                            dto.setInterest(repayDetail.getInterest()/100.0);
                            dto.setPrincipal(repayDetail.getPrincipal()/100.0);
                            dto.setBonusInterest(repayDetail.getBonusInterest()/100.0);
                            dto.setBonusReward(repayDetail.getBonusReward()/100.0);
                            String name = subject.getName();
                            Integer term = subject.getTerm();
                            if(Credit.SOURCE_CHANNEL_YJT == credit.getSourceChannel()) {
                                IPlan iplan = iPlanService.getIPlanById(iPlanAccountService.findById(credit.getSourceAccountId()).getIplanId());
                                if(iplan!=null) {
                                    /*if(IPlan.PACKAGING_TYPE_CREDIT.equals(iplan.getPackagingType())) {
                                        currTerm = iPlanRepayScheduleService.getCurrentTermByIplanId(iplan.getId(), schedule.getDueDate());
                                    }*/
                                    name = iplan.getName();
                                    term = iplan.getTerm();
                                    dto.setSecondName(subject.getName());
                                }
                            }
                            dto.setCurrentTerm(currTerm);
                            dto.setTerm(term);
                            dto.setRepayDate(schedule.getDueDate().substring(0,4)+"-"+schedule.getDueDate().substring(4,6)+"-"+schedule.getDueDate().substring(6,8));
                            dto.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
                            dto.setSubjectId(repayDetail.getSubjectId());
                            dto.setName(name);
                            dto.setStatus(0);//未还
                            dto.setVipInterest(0D);
                            details.add(dto);
                            principal -=repayDetail.getPrincipal();
                        }
                        totalPrincipal -= schedule.getDuePrincipal();
                    }
                }
            }
        }
        //月月盈未还款计划
        List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailService.getByUserIdAndStatus(userId);
        if(!iPlanRepayDetails.isEmpty()){
            for (IPlanRepayDetail repayDetail : iPlanRepayDetails) {
                SubjectRepayDetailDto iPlanRepayDetailDto = new SubjectRepayDetailDto();
                IPlan iPlan = iPlanService.getIPlanById(repayDetail.getIplanId());
                iPlanRepayDetailDto.setName(iPlan.getName());//名称
                iPlanRepayDetailDto.setCurrentTerm(repayDetail.getTerm());//当前第几期
                iPlanRepayDetailDto.setTerm(iPlan.getTerm());//总期数
                iPlanRepayDetailDto.setPrincipal(repayDetail.getDuePrincipal()/100.0);
                iPlanRepayDetailDto.setInterest(repayDetail.getDueInterest()/100.0);
                iPlanRepayDetailDto.setBonusInterest(repayDetail.getDueBonusInterest()/100.0);
                iPlanRepayDetailDto.setSubjectId(String.valueOf(iPlan.getId()));
                iPlanRepayDetailDto.setVipInterest(repayDetail.getDueVipInterest()/100.0);
                iPlanRepayDetailDto.setRepayDate(repayDetail.getDueDate());
                iPlanRepayDetailDto.setRepayType(SubjectMobileResource.repayType(iPlan.getRepayType()));
                iPlanRepayDetailDto.setBonusReward(0.0);
                iPlanRepayDetailDto.setStatus(0);
                details.add(iPlanRepayDetailDto);
            }
        }
        return new RestResponseBuilder<List<SubjectRepayDetailDto>>().success(details);
    }

    /**
     * pc近7日还款计划
     * @param userId
     * @return
     */
    @GetMapping("/pc/subject/seven/repay/schedule")
    public RestResponse<List<SubjectRepayDetailDto>> getSubjectRepayDetailForSev(@RequestParam("userId") String userId
                                                                                    ) {
        List<SubjectRepayDetailDto> details = new ArrayList<>();
        String dateNow = DateUtil.getCurrentDateShort();
        //查询散标未还款债权
        List<Credit> creditList = creditService.findByUserIdAndChannelsInAndStatusForRepay(userId,new HashSet<>(Arrays.asList(Credit.SOURCE_CHANNEL_SUBJECT,Credit.SOURCE_CHANNEL_YJT)),Credit.CREDIT_STATUS_HOLDING);
        //根据标的id分组
        Map<String, List<Credit>> maps = creditList.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
        for (Map.Entry<String, List<Credit>> entry : maps.entrySet()) {
            String subjectId = entry.getKey();
            Subject subject = subjectService.getBySubjectId(subjectId);
            //根据subjectId查询未还还款计划
            List<SubjectRepaySchedule> schedules = repayScheduleService.getSubjectRepayScheduleBySubjectIdNotRepay(subjectId);
            //过滤出还款日是最近7天的
            schedules =  schedules.stream().filter(s -> DateUtil.betweenDays(dateNow,s.getDueDate()) <= 7).collect(Collectors.toList());
            //获取这个散标下所有的账户
            List<Credit> accountList = entry.getValue();
            Integer totalPrincipal = 0;
            if(!schedules.isEmpty()){
                for (Credit credit:accountList) {
                    Integer principal = credit.getHoldingPrincipal();
                    totalPrincipal = schedules.stream().map(SubjectRepaySchedule::getDuePrincipal).reduce(Integer::sum).orElse(0);
                    for (SubjectRepaySchedule schedule:schedules) {
                        if(principal>0){
                            SubjectRepayDetail repayDetail =  repayScheduleService.repayDetailBySchedule(schedule,userId,credit,principal,totalPrincipal);
                            if(repayDetail==null){
                                break;
                            }
                            SubjectRepayDetailDto dto = new SubjectRepayDetailDto();
                            dto.setInterest(repayDetail.getInterest()/100.0);
                            dto.setPrincipal(repayDetail.getPrincipal()/100.0);
                            dto.setBonusInterest(repayDetail.getBonusInterest()/100.0);
                            dto.setBonusReward(repayDetail.getBonusReward()/100.0);
                            dto.setCurrentTerm(schedule.getTerm());
                            dto.setTerm(subject.getTerm());
                            dto.setRepayDate(schedule.getDueDate().substring(0,4)+"-"+schedule.getDueDate().substring(4,6)+"-"+schedule.getDueDate().substring(6,8));
                            dto.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
                            dto.setSubjectId(repayDetail.getSubjectId());
                            dto.setName(subject.getName());
                            dto.setStatus(0);
                            dto.setRepayTerm(schedule.getTerm()+"/"+subject.getTerm());
                            details.add(dto);
                            principal -= repayDetail.getPrincipal();
                        }
                        totalPrincipal -= schedule.getDuePrincipal();
                    }
                }
            }
        }
        return new RestResponseBuilder<List<SubjectRepayDetailDto>>().success(details);
    }

    /**
     * 散标管理 --转让中/已完成
     */
    @GetMapping("/pc/subject/user/management")
    public RestResponse subjectInvestManagement(@RequestParam("userId") String userId,
                                          @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                          @RequestParam("type") int type){
        if (type == CreditManageHoldDto.PAGE_TYPE_TRANSFERRING) {
            SubjectCreditTransferDto subjectCreditTransferDto = new SubjectCreditTransferDto();
            List<SubjectCreditTransferDto.Detail> details = new ArrayList<>();

            //总转让金额
            Double amount = 0.0;
            //已成交金额
            Double finishAmt = 0.0;

            //转让中
            List<CreditOpening> creditOpenings = creditOpeningService.getByUserIdAndStatusAndOpenChannel(userId, CreditOpening.OPEN_CHANNEL);
            List<CreditOpening> newCreditOpenings = new ArrayList<>();
            List<CreditOpening> allCreditOpenings = new ArrayList<>();
            if(creditOpenings != null && creditOpenings.size() > 0){
                allCreditOpenings = creditOpeningService.sortByConditionNoPage(creditOpenings,Credit.TARGET_SUBJECT);
                if(allCreditOpenings.size() > 0){
                    for (CreditOpening creditOpening : allCreditOpenings) {
                        amount += creditOpening.getTransferPrincipal() / 100.0;
                        Double transferAmtNew = (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
                        if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                            SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
                            transferAmtNew= subjectTransLog.getProcessedAmt() / 100.0;
                        }
                        finishAmt += transferAmtNew;
                    }
                }
                newCreditOpenings = creditOpeningService.sortByCondition(creditOpenings,Credit.TARGET_SUBJECT,pageNum,pageSize);
            }
            if (newCreditOpenings.size() > 0) {
                //持有金额
                for (CreditOpening creditOpening : newCreditOpenings) {
                    Credit credit = creditService.getById(creditOpening.getCreditId());
                    Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
                    SubjectCreditTransferDto.Detail detail = new SubjectCreditTransferDto.Detail();
                    detail.setId(creditOpening.getId());
                    detail.setSubjectId(subject.getSubjectId());
                    detail.setName(subject.getName());
                    BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
                    Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(),creditOpening);
                    detail.setRate(totalRate.doubleValue());
                    detail.setRateStr(df4.format(detail.getRate() * 100));
                    //回款方式
                    detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                    //申请转让金额
                    detail.setHoldingAmt(creditOpening.getTransferPrincipal() / 100.0);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    //转让中金额
                    Double transferAmt = (creditOpening.getAvailablePrincipal()) / 100.0;
                    detail.setTransferAmt(transferAmt);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));

                    //已成交金额
                    Double finAmt = (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
                    if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                        SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
                        finAmt= subjectTransLog.getProcessedAmt() / 100.0;
                    }
                    detail.setHasFinishAmt(finAmt);
                    detail.setHasFinishAmtStr(df4.format(detail.getHasFinishAmt()));

                    //剩余时间
                    String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
                    String endDate = credit.getEndTime().substring(0, 8);
                    long days = DateUtil.betweenDays(currentDate, endDate);
                    detail.setResidualDay((int) days);

                    //是否可撤销
                    Integer status = 1;
                    if(creditOpening.getAvailablePrincipal() == 0 || CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                        status = 0;
                    }
                    detail.setStatus(status);

                    //红包相关
                    detail.setRedPacket("");
                    SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                    SubjectTransLog log = subjectTransLogService.getById(subjectAccount.getTransLogId());
                    if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                        RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                        String redMsg = redPacketService.getRedpackeMsg(packet);
                        detail.setRedPacket(redMsg);
                    }
                    //活动相关
                    detail.setActivityName("");
                    detail.setIncreaseInterest(0.0);
                    detail.setFontColor("");
                    detail.setBackground("");
                    Integer activityId = subject.getActivityId();
                    if (activityId != null) {
                        ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
                        detail.setActivityName(amc.getActivityName());
                        detail.setIncreaseInterest(amc.getIncreaseInterest());
                        detail.setFontColor(amc.getFontColorPc());
                        detail.setBackground(amc.getBackgroundPc());
                        if(amc.getIncreaseTerm() != null){
                            detail.setAddTerm(amc.getIncreaseTerm());
                        }else{
                            detail.setAddTerm(0);
                        }
                    }
                    details.add(detail);
                }
            }
            subjectCreditTransferDto.setAmount(amount);
            subjectCreditTransferDto.setAmountStr(df4.format(amount));
            subjectCreditTransferDto.setFinishAmt(finishAmt);
            subjectCreditTransferDto.setFinishAmtStr(df4.format(finishAmt));
            subjectCreditTransferDto.setPageType(CreditManageHoldDto.PAGE_TYPE_TRANSFERRING);
            subjectCreditTransferDto.setDetails(details);

            //分页相关
            subjectCreditTransferDto.setPage(pageNum);
            subjectCreditTransferDto.setSize(newCreditOpenings.size());
            subjectCreditTransferDto.setTotalPages(newCreditOpenings.size() % pageSize != 0 ? newCreditOpenings.size() / pageSize + 1 : newCreditOpenings.size() / pageSize);
            subjectCreditTransferDto.setTotal(newCreditOpenings.size());
            return new RestResponseBuilder<>().success(subjectCreditTransferDto);
        }else if(type == AppCreditManageHoldDto.PAGE_TYPE_FINISH){
            AppCreditManageFinishDto appCreditManageFinishDto = new AppCreditManageFinishDto();
            List<AppCreditManageFinishDto.Detail> details = new ArrayList<>();

            //总转让金额
            Double amount = 0.0;
            //实际到账金额
            Double totalActualAmt = 0.0;

            //正常还款已完成
            List<Credit> credits = creditService.getCreditFinishByUserId(userId,Credit.TARGET_SUBJECT,Credit.CREDIT_STATUS_FINISH);
            if(credits != null && credits.size() > 0){
                for (Credit credit : credits) {
                    AppCreditManageFinishDto.Detail detail = new AppCreditManageFinishDto.Detail();
                    //账户
                    SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                    detail.setId(subjectAccount.getId());
                    //标的
                    Subject subject = subjectService.getBySubjectId(credit.getSubjectId());
                    detail.setName(subject.getName());
                    detail.setSubjectId(subject.getSubjectId());
                    //利率
                    BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                    detail.setRate(totalRate.doubleValue());
                    detail.setRateStr(df4.format(detail.getRate() * 100));
                    //还款类型
                    detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                    //完成方式
                    detail.setType(CreditConstant.REPAY_FINISH);
                    //本金
                   Integer principal = subjectRepayDetailService.getPrincipal(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
                    //利息
                   Integer interest = subjectRepayDetailService.getInterest(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
                    //持有金额
                    detail.setHoldingAmt(principal/100.0);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    amount += detail.getHoldingAmt();
                    //回款金额
                    detail.setTransferAmt(0.0);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                    //实际到账金额
                    detail.setActualAmt((principal + interest) / 100.0);
                    detail.setActualAmtStr(df4.format(detail.getActualAmt()));
                    totalActualAmt += detail.getActualAmt();
                    //红包相关
                    detail.setRedPacket("");
                    SubjectTransLog log = subjectTransLogService.getById(subjectAccount.getTransLogId());
                    if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                        RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                        String redMsg = redPacketService.getRedpackeMsg(packet);
                        detail.setRedPacket(redMsg);
                    }
                    //活动相关
                    detail.setActivityName("");
                    detail.setIncreaseInterest(0.0);
                    detail.setFontColor("");
                    detail.setBackground("");
                    Integer activityId = subject.getActivityId();
                    ActivityMarkConfigure amc = null;
                    if (activityId != null) {
                        amc = activityMarkConfigureService.findById(activityId);
                        detail.setActivityName(amc.getActivityName());
                        detail.setIncreaseInterest(amc.getIncreaseInterest());
                        detail.setFontColor(amc.getFontColorPc());
                        detail.setBackground(amc.getBackgroundPc());
                        if(amc.getIncreaseTerm() != null){
                            detail.setAddTerm(amc.getIncreaseTerm());
                        }else{
                            detail.setAddTerm(0);
                        }
                    }
                    if (Objects.equals(subject.getNewbieOnly(), Subject.NEWBIE_ONLY_Y)) {
                        detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                        if(amc.getIncreaseTerm() != null){
                            detail.setAddTerm(amc.getIncreaseTerm());
                        }else {
                            detail.setAddTerm(0);
                        }
                    }
                    if((principal+interest)>0){
                        details.add(detail);
                    }
                }
            }
            //转让完成
            List<SubjectTransLog> subjectTransLogs = subjectTransLogService.getSubjectTransLogByUserIdAndStatus(userId);
            List<SubjectTransLog> logs = new ArrayList<>();
            if(subjectTransLogs != null && subjectTransLogs.size() > 0){
                logs = subjectTransLogService.sortBySource(subjectTransLogs,SubjectAccount.SOURCE_SUBJECT);
            }
            if(logs != null && logs.size() > 0){
                for (SubjectTransLog subjectTransLog : logs) {
                    AppCreditManageFinishDto.Detail detail = new AppCreditManageFinishDto.Detail();
                    SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectTransLog.getAccountId());
                    //transLogId
                    detail.setId(subjectTransLog.getId());
                    //利率
                    Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
                    BigDecimal totalRate = creditOpeningService.calcTotalRate(credit.getSubjectId());
                    detail.setRate(totalRate.doubleValue());
                    detail.setRateStr(df4.format(detail.getRate() * 100));
                    //项目名称
                    Subject subject = subjectService.getBySubjectId(subjectTransLog.getSubjectId());
                    detail.setName(subject.getName());
                    detail.setSubjectId(subject.getSubjectId());
                    //还款类型
                    detail.setRepayType(subjectService.getRepayType(subjectTransLog.getSubjectId()));
                    //完成方式
                    detail.setType(CreditConstant.TRANSFER_FINISH);
                    //购买金额
                    detail.setHoldingAmt(subjectAccount.getInitPrincipal() / 100.0);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    //转让金额
                    detail.setTransferAmt(subjectTransLog.getProcessedAmt()/ 100.0);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                    amount += detail.getTransferAmt();
                    //实际到账金额
                    detail.setActualAmt(subjectTransLog.getActualPrincipal() / 100.0);
                    detail.setActualAmtStr(df4.format(detail.getActualAmt()));
                    totalActualAmt += detail.getActualAmt();
                    //红包相关
                    detail.setRedPacket("");
                    SubjectTransLog log = subjectTransLogService.getById(subjectAccount.getTransLogId());
                    if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                        RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                        String redMsg = redPacketService.getRedpackeMsg(packet);
                        detail.setRedPacket(redMsg);
                    }
                    //活动相关
                    detail.setActivityName("");
                    detail.setIncreaseInterest(0.0);
                    detail.setFontColor("");
                    detail.setBackground("");
                    Integer activityId = subject.getActivityId();
                    if (activityId != null) {
                        ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
                        detail.setActivityName(amc.getActivityName());
                        detail.setIncreaseInterest(amc.getIncreaseInterest());
                        detail.setFontColor(amc.getFontColorPc());
                        detail.setBackground(amc.getBackgroundPc());
                        if(amc.getIncreaseTerm() != null){
                            detail.setAddTerm(amc.getIncreaseTerm());
                        }else{
                            detail.setAddTerm(0);
                        }
                    }
                    details.add(detail);
                }
            }
            List<AppCreditManageFinishDto.Detail> list = new PageUtil().ListSplit(details, pageNum, pageSize);
            appCreditManageFinishDto.setPage(pageNum);
            appCreditManageFinishDto.setSize(pageSize);
            appCreditManageFinishDto.setTotal(details.size());
            appCreditManageFinishDto.setTotalPages(details.size() % pageSize != 0 ? details.size() / pageSize + 1:details.size() / pageSize);
            appCreditManageFinishDto.setAmount(amount);
            appCreditManageFinishDto.setAmountStr(df4.format(amount));
            appCreditManageFinishDto.setTotalActualAmt(totalActualAmt);
            appCreditManageFinishDto.setTotalActualAmtStr(df4.format(totalActualAmt));
            appCreditManageFinishDto.setPageType(AppCreditManageHoldDto.PAGE_TYPE_FINISH);
            appCreditManageFinishDto.setDetails(list);
            return new RestResponseBuilder<>().success(appCreditManageFinishDto);
        } else{
            throw new IllegalArgumentException("参数type不支持！");
        }

    }

    /**
     * 散标管理-PC持有中详情
     * @param subjectAccountId
     * @param userId
     * @param type
     * @return
     */
    @GetMapping("/pc/subject/manage/subjectDetail")
    public RestResponse getSubjectDetailDto(@RequestParam("id") int subjectAccountId,
                                                   @RequestParam(value = "userId") String userId,
                                                    @RequestParam(value = "type") int type) {
        if (SubjectDetailDto.TYPE_TRANSFER_FINISH == type){
            AppCreditTransferFinishDetailDto appCreditTransferFinishDetailDto = new AppCreditTransferFinishDetailDto();
            SubjectTransLog subjectTransLog = subjectTransLogService.getById(subjectAccountId);
            if (subjectTransLog == null) {
                throw new ProcessException(Error.NDR_0202);
            }
            CreditOpening creditOpening = creditOpeningService.getBySourceChannelId(subjectTransLog.getId(), CreditOpening.SOURCE_CHANNEL_SUBJECT);
            appCreditTransferFinishDetailDto.setId(creditOpening.getId());
            Subject subject = subjectService.getBySubjectId(subjectTransLog.getSubjectId());
            appCreditTransferFinishDetailDto.setName(subject.getName());
            appCreditTransferFinishDetailDto.setRepayType(subjectService.getRepayType(subjectTransLog.getSubjectId()));
            //年化利率
            Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(),creditOpening);
            BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
            ActivityMarkConfigure activityMarkConfigure = null;
            if (subject.getActivityId() != null && subject.getActivityId() > 0) {
                activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
                if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                    totalRate = subject.getInvestRate();
                }
            }
            appCreditTransferFinishDetailDto.setRate(totalRate.doubleValue());
            appCreditTransferFinishDetailDto.setRateStr(df4.format(appCreditTransferFinishDetailDto.getRate() * 100));

            //持有金额
            SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectTransLog.getAccountId());
            appCreditTransferFinishDetailDto.setHoldingAmt(subjectAccount.getInitPrincipal() / 100.0);
            appCreditTransferFinishDetailDto.setHoldingAmtStr(df4.format(appCreditTransferFinishDetailDto.getHoldingAmt()));

            //已到账金额
            appCreditTransferFinishDetailDto.setReceivedAmt(subjectTransLog.getActualPrincipal() / 100.0);
            appCreditTransferFinishDetailDto.setReceivedAmtStr(df4.format(appCreditTransferFinishDetailDto.getReceivedAmt()));
            //转让发起时间
            appCreditTransferFinishDetailDto.setTransferTime(subjectTransLog.getCreateTime().substring(0,10));
            //转让金额
            appCreditTransferFinishDetailDto.setTransferAmt(subjectTransLog.getTransAmt() / 100.0);
            appCreditTransferFinishDetailDto.setTransferAmtStr(df4.format(appCreditTransferFinishDetailDto.getTransferAmt()));
            //已成交金额
            appCreditTransferFinishDetailDto.setProcessedAmt(subjectTransLog.getProcessedAmt() / 100.0);
            appCreditTransferFinishDetailDto.setProcessedAmtStr(df4.format(appCreditTransferFinishDetailDto.getProcessedAmt()));
            //债权取消金额
            appCreditTransferFinishDetailDto.setCancelAmt(appCreditTransferFinishDetailDto.getTransferAmt() - appCreditTransferFinishDetailDto.getProcessedAmt());
            appCreditTransferFinishDetailDto.setCancelAmtStr(df4.format(appCreditTransferFinishDetailDto.getCancelAmt()));

            //折让率
            appCreditTransferFinishDetailDto.setTransDiscount(creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue());
            appCreditTransferFinishDetailDto.setTransDiscountStr(df4.format(appCreditTransferFinishDetailDto.getTransDiscount()));

            //散标交易配置信息
            SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

            //转让服务费
            if(subjectTransLog.getTransFee() == 0){
                Double feeRate = subjectAccountService.calcTransFeeFinish(subjectAccount.getTransLogId(), subject, subjectTransferParam,subjectAccountId);
                appCreditTransferFinishDetailDto.setFee((subjectTransLog.getProcessedAmt()/100.0) * feeRate / 100.0);
            }else{
                appCreditTransferFinishDetailDto.setFee(0.0);
            }
            appCreditTransferFinishDetailDto.setFeeStr(df4.format(appCreditTransferFinishDetailDto.getFee()));

            //扣除红包奖励
            Credit credit = creditService.getById(creditOpening.getCreditId());
            Double redFee = subjectAccountService.calcRedFeeFinish(subjectAccount, credit,subjectAccountId);
            appCreditTransferFinishDetailDto.setRedFee((subjectTransLog.getProcessedAmt() /100.0)   * redFee);
            appCreditTransferFinishDetailDto.setRedFeeStr(df4.format(appCreditTransferFinishDetailDto.getRedFee() ));

            //溢价手续费
            Double overFee = 0.0;
            Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
            if(transferDiscount > 100){
                overFee = (subjectTransLog.getProcessedAmt() /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
            }
            appCreditTransferFinishDetailDto.setOverFee(overFee);
            appCreditTransferFinishDetailDto.setOverFeeStr(df4.format(overFee));

            appCreditTransferFinishDetailDto.setCreditTransferUrl(detailUrl+"?subjectId="+subject.getSubjectId());
            appCreditTransferFinishDetailDto.setPageType(AppCreditFinishDetailDto.PAGE_TYPE_TRANSFERRFINISH);
            return new RestResponseBuilder<>().success(appCreditTransferFinishDetailDto);
        }
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectAccountId);
        if (subjectAccount == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        SubjectDetailDto subjectDetailDto = new SubjectDetailDto();
        SubjectDetailDto.SubjectDetail subjectDetail = new SubjectDetailDto.SubjectDetail();
        Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());
        Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
        SubjectTransLog subjectTransLog = subjectTransLogService.getTransLogByAccountId(subjectAccountId);
        subjectDetail.setId(subjectAccountId);
        subjectDetail.setName(subject.getName());
        subjectDetail.setTotalAmt(subject.getTotalAmt());
        subjectDetail.setAvailableAmt(subject.getAvailableAmt());
        subjectDetail.setSubjectId(subject.getSubjectId());
        subjectDetail.setTerm(subject.getTerm());
        subjectDetail.setPeriod(subject.getPeriod());
        subjectDetail.setInvestRate(subject.getInvestRate());
        subjectDetail.setBonusRate(subject.getBonusRate());
        subjectDetail.setNewbieOnly(subject.getNewbieOnly());
        subjectDetail.setSubjectRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
        if(subject.getPeriod()<30){
            subjectDetail.setTermStr(subject.getPeriod()+"天");
            subjectDetail.setSubjectType("天标");
        }else{
            subjectDetail.setTermStr(subject.getTerm()+"个月");
            subjectDetail.setSubjectType("月标");
        }
        //还款状态
        if (type == SubjectDetailDto.TYPE_HOLDING) {
            subjectDetail.setRaiseStatus(CreditConstant.JOINING);
            if(subject.getRaiseStatus()==3) {
                subjectDetail.setRaiseStatus(CreditConstant.REPAYING);
            }
        }else if (type == SubjectDetailDto.TYPE_FINISH) {
            subjectDetail.setRaiseStatus(CreditConstant.REPAYFINSH);
        }
        //标的利率
        BigDecimal totalRate = creditOpeningService.calcTotalRate(subject.getSubjectId());
        ActivityMarkConfigure activityMarkConfigure = null;
        if (subject.getActivityId() != null && subject.getActivityId() > 0) {
            activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                totalRate = subject.getInvestRate();
            }
        }
        subjectDetail.setRateStr(df4.format(totalRate.doubleValue() * 100)+"%");
        subjectDetail.setProtocolUrl(protocolUrl);
        if(credit != null){
            String contractId = credit.getContractId();
            if(org.apache.commons.lang3.StringUtils.isNotBlank(contractId)){
                subjectDetail.setProtocolUrl(creditService.getContractViewPdfUrlByContractId(contractId));
            }
        }
        String endTime = "加入中";
        SubjectDetailDto.SubjectInvest subjectInvest = new SubjectDetailDto.SubjectInvest();
        //投资金额
        subjectInvest.setHoldingPrincipal(subjectAccount.getCurrentPrincipal()/ 100.0);
        subjectInvest.setHoldingPrincipalStr(df4.format(subjectAccount.getCurrentPrincipal()/ 100.0));
        Integer principals =0;
        Integer interests=0;
        subjectInvest.setBuyTime(subjectTransLog.getCreateTime());
        if(credit != null){
            endTime = credit.getEndTime() != null ? DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString() : "生成中";
            subjectInvest.setBuyTime(credit.getCreateTime());
            //投资金额
            subjectInvest.setHoldingPrincipal(credit.getInitPrincipal() / 100.0);
            subjectInvest.setHoldingPrincipalStr(df4.format(credit.getInitPrincipal() / 100.0));
            //已到账本金
            principals = subjectRepayDetailService.getPrincipal(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
            //已到账利息
            interests = subjectRepayDetailService.getInterest(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
        }
        subjectDetail.setEndTime(endTime);
        //是否可转让
        subjectDetail.setTransfer(subjectService.checkCondition(subject,subjectAccount));
        subjectDetail.setMessage(subjectService.checkConditionStr(subject,subjectAccount));
        //红包信息
        RedPacket redPacket =  null;
        if (subjectTransLog.getRedPacketId() > 0){
            redPacket = redPacketService.getRedPacketById(subjectTransLog.getRedPacketId());
        }
        String redPacketMsg = "";
        if (redPacket != null) {
            redPacketMsg = redPacketService.getRedpackeMsg(redPacket);
        }
        subjectInvest.setRedPacketMsg(redPacketMsg);
        subjectInvest.setRedPacket(redPacket);
       //购买状态
        subjectInvest.setBuyStatus(CreditConstant.BUY_STATUS_SUCCESS);
        //债权信息
        User user = userService.getUserById(subject.getBorrowerId());
        SubjectDetailDto.CreditHoldDetail creditHoldDetail = new SubjectDetailDto.CreditHoldDetail();

        if(user!=null){
           // throw new ProcessException(Error.NDR_0767);
        creditHoldDetail.setUserName(user.getRealname().substring(0, 1) + "**");
        creditHoldDetail.setCardId(user.getIdCard().substring(0, 6) + "********" + user.getIdCard().substring(14));
        if(credit != null) {
            creditHoldDetail.setCreditId(credit.getId().toString());
        }
        //投资金额
        creditHoldDetail.setHoldingPrincipal(subjectInvest.getHoldingPrincipal());
        creditHoldDetail.setHoldingPrincipalStr(subjectInvest.getHoldingPrincipalStr());
        }
        //投资总本金
        subjectDetail.setTotalPrincipal(subjectInvest.getHoldingPrincipalStr());
        //预计总利息
        double investRate = subject.getInvestRate() != null ? subject.getInvestRate().doubleValue() : 0;
        double bonusRate = subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0;
        int holdingPrincipal=(int)(subjectInvest.getHoldingPrincipal()*100);
        double expectedInterest=subjectService.getInterestByRepayType(holdingPrincipal,BigDecimal.valueOf(investRate),subject.getRate(),
                subject.getTerm(),subject.getPeriod(),subject.getRepayType());

        double expectedBonusInterest=subjectService.getInterestByRepayType(holdingPrincipal,BigDecimal.valueOf(bonusRate),subject.getRate(),
                    subject.getTerm(),subject.getPeriod(),subject.getRepayType());

        subjectDetail.setTotalInterest(String.valueOf(expectedInterest+expectedBonusInterest));

        subjectDetailDto.setDetails(subjectDetail);
        subjectDetailDto.setInvests(subjectInvest);
        subjectDetailDto.setCreditHold(creditHoldDetail);


        //回款跟踪
        //查询未还的还款计划
        List<SubjectRepaySchedule> schedules = repayScheduleService.getSubjectRepayScheduleBySubjectIdNotRepay(subject.getSubjectId());
       //已还款
        List<SubjectRepayDetail> details = subjectRepayDetailDao.findBySubjectIdAndStatusAndAccountId(subject.getSubjectId(),userId,subjectAccountId,Credit.SOURCE_CHANNEL_SUBJECT);
        List<SubjectAppRepayDetailDto.RepayDetail>  detailsForRepay = new ArrayList<>();
        if(credit != null) {
            detailsForRepay = repayScheduleService.commonRepaymentMethod(credit,schedules,details);
        }
        subjectDetailDto.setRepayDetails(detailsForRepay);
        return new RestResponseBuilder<SubjectDetailDto>().success(subjectDetailDto);
    }

    @GetMapping("/pc/subject/manage/creditTransferDetail")
    public RestResponse getSubjectCreditTransferDetailDto(@RequestParam("id") int creditOpeningId,
                                                   @RequestParam(value = "userId") String userId) {

        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        CreditTransferDetailDto creditTransferDetailDto = new CreditTransferDetailDto();
        creditTransferDetailDto.setId(creditOpening.getId());
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        creditTransferDetailDto.setName(subject.getName());

        creditTransferDetailDto.setReturnStatus(CreditConstant.TRANSFERING);
        Double tansferAmt= (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
        //成交金额
        creditTransferDetailDto.setFinishPrincipal(tansferAmt);
        creditTransferDetailDto.setFinishPrincipalStr(df4.format(creditTransferDetailDto.getFinishPrincipal()));
        //剩余时间
        Credit credit = creditService.getById(creditOpening.getCreditId());
        String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
        String endDate = credit.getEndTime().substring(0, 8);
        long days = DateUtil.betweenDays(currentDate, endDate);
        creditTransferDetailDto.setResidualDay((int) days);

        //已到账收益
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
        creditTransferDetailDto.setReceivedAmt(subjectAccount.getPaidInterest() / 100.0);
        creditTransferDetailDto.setReceivedAmtStr(df4.format(creditTransferDetailDto.getReceivedAmt()));

        //年利化率
        BigDecimal totalRate = creditOpeningService.calcTotalRate(creditOpening.getSubjectId());
        Double newRate = creditOpeningService.calcNewRate(creditOpening.getSubjectId(),creditOpening);
        ActivityMarkConfigure activityMarkConfigure = null;
        if (subject.getActivityId() != null && subject.getActivityId() > 0) {
            activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                totalRate = subject.getInvestRate();
            }
        }
        creditTransferDetailDto.setRate(totalRate.doubleValue());
        creditTransferDetailDto.setRateStr(df4.format(creditTransferDetailDto.getRate() * 100));

        //还款方式
        creditTransferDetailDto.setRepayType(subjectService.getRepayType(creditOpening.getSubjectId()));

        //项目结束时间
        creditTransferDetailDto.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        //购买时间
        creditTransferDetailDto.setBuyTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());

        //购买金额
        creditTransferDetailDto.setHoldingPrincipal(credit.getInitPrincipal() / 100.0);
        creditTransferDetailDto.setHoldingPrincipalStr(df4.format(creditTransferDetailDto.getHoldingPrincipal()));

        //购买状态
        creditTransferDetailDto.setBuyStatus(CreditConstant.BUY_STATUS_SUCCESS);

        //出售金额
        creditTransferDetailDto.setSaleAmt(creditOpening.getTransferPrincipal() / 100.0);
        creditTransferDetailDto.setSaleAmtStr(df4.format(creditTransferDetailDto.getSaleAmt()));

        //折让率
        creditTransferDetailDto.setTransDiscount(creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue());
        creditTransferDetailDto.setTransDiscountStr(df4.format(creditTransferDetailDto.getTransDiscount()));

        //持有天数
        String startDate = credit.getCreateTime().substring(0, 10).replace("-","");
        long holdDays = DateUtil.betweenDays(startDate,currentDate);
        creditTransferDetailDto.setHoldDay((int) holdDays);

        //转让时间
        creditTransferDetailDto.setTransferTime(creditOpening.getCreateTime().substring(0,10));

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

        //转让服务费
        SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
        if(subjectTransLog.getTransFee() == 0){
            Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
            creditTransferDetailDto.setFee((creditOpening.getTransferPrincipal()/100.0) * feeRate / 100.0);
        }else{
            creditTransferDetailDto.setFee(0.0);
        }
        creditTransferDetailDto.setFeeStr(df4.format(creditTransferDetailDto.getFee()));

        //扣除红包奖励
        Double redFee = subjectAccountService.calcRedFee(subjectAccount, credit);
        creditTransferDetailDto.setRedFee((creditOpening.getTransferPrincipal() /100.0)   * redFee);
        creditTransferDetailDto.setRedFeeStr(df4.format(creditTransferDetailDto.getRedFee() ));

        //溢价手续费
        Double overFee = 0.0;
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if(transferDiscount > 100){
            overFee = (creditOpening.getTransferPrincipal() /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        }
        creditTransferDetailDto.setOverFee(overFee);
        creditTransferDetailDto.setOverFeeStr(df4.format(overFee));

        //预计到账金额
        Double expectAmt = ArithUtil.calcExp((creditOpening.getTransferPrincipal() /100.0) * (transferDiscount / 100.0), creditTransferDetailDto.getRedFee(),creditTransferDetailDto.getOverFee(),creditTransferDetailDto.getFee());

        creditTransferDetailDto.setExpectAmt(expectAmt);
        creditTransferDetailDto.setExpectAmtStr(df4.format(creditTransferDetailDto.getExpectAmt()));

        //是否可撤销
        Integer status = 1;
        if(creditOpening.getAvailablePrincipal() == 0 || CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
            status = 0;
        }
        creditTransferDetailDto.setStatus(status);

        creditTransferDetailDto.setCreditTransferUrl(transferUrl);
        creditTransferDetailDto.setCreditDetailUrl(detailUrl+"?subjectId="+subject.getSubjectId());

        return new RestResponseBuilder<>().success(creditTransferDetailDto);
    }
}
