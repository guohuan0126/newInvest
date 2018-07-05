package com.jiuyi.ndr.resource.subject.mobile;

/**
 * Created by YU on 2017/11/2.
 */


import com.duanrong.util.jedis.DRJedisCacheUtil;
import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.constant.CreditConstant;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayScheduleDao;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.basicdata.Dictionary;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanAccount;
import com.jiuyi.ndr.domain.iplan.IPlanRepaySchedule;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.redpacket.InvestRedpacket;
import com.jiuyi.ndr.domain.subject.*;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.dto.credit.mobile.*;
import com.jiuyi.ndr.dto.iplan.mobile.IPlanAppListDto;
import com.jiuyi.ndr.dto.iplan.mobile.YjtAppRepayDetailDto;
import com.jiuyi.ndr.dto.subject.SubjectDetailDto;
import com.jiuyi.ndr.dto.subject.SubjectDto;
import com.jiuyi.ndr.dto.subject.SubjectRepayDetailDto;
import com.jiuyi.ndr.dto.subject.mobile.*;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.SubjectListPageData;
import com.jiuyi.ndr.service.account.UserAccountService;
import com.jiuyi.ndr.service.basicdata.BasicDataService;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.credit.CreditOpeningService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.iplan.IPlanAccountService;
import com.jiuyi.ndr.service.iplan.IPlanRepayScheduleService;
import com.jiuyi.ndr.service.iplan.IPlanService;
import com.jiuyi.ndr.service.subject.*;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.PageUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gh
 *
 */
@RestController
public class SubjectMobileResource {
    private final static Logger logger = LoggerFactory.getLogger(SubjectMobileResource.class);
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private InvestService investService;
    @Autowired
    private SubjectInvestParamService subjectInvestParamService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private SubjectRepayScheduleService repayScheduleService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private BasicDataService basicDataService;
    @Autowired
    private SubjectTransLogService subjectTransLogService;
    @Autowired
    private CreditOpeningService creditOpeningService;
    @Autowired
    private SubjectTransferParamService subjectTransferParamService;
    @Autowired
    private SubjectRepayDetailService subjectRepayDetailService;
    @Autowired
    private UserService userService;
    @Autowired
    private SubjectRepayScheduleService subjectRepayScheduleService;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanAccountService iPlanAccountService;

    @Autowired
    private SubjectRepayScheduleDao subjectRepayScheduleDao;
    @Autowired
    private ConfigService configService;
    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    private static final Integer HOUR_TO_REMIND = 1;

    @Value("${duanrong.subject.detailUrl}")
    private String detailUrl;   //散标详情页项目详情
    @Value("${duanrong.subject.fengkongUrl}")
    private String fengkongUrl;//风控信息
    @Value("${duanrong.subject.investUrl}")
    private String investUrl;//投资达人
    @Value("${duanrong.subject.shareLinkUrl}")
    private String shareLinkUrl;//散标详情分享连接
    @Value("${duanrong.subject.protocolUrl}")
    private String protocolUrl;
    @Value("${duanrong.subject.riskProtocolUrl}")
    private String riskProtocolUrl;//风险协议
    @Value("${duanrong.subject.transferUrl}")
    private String transferUrl;//转让协议
    @Value("${duanrong.subject.creditDetailUrl}")
    private String creditDetailUrl;//债权明细


    private String imagePrefix = "https://duanrongweb.oss-cn-qingdao.aliyuncs.com";   //oss图片地址

    /**
     * app-获取散标新列表
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/app/subject/list")
    public RestResponse getAppSubject( @RequestParam(value = "userId", required = false) String userId,
                                    @RequestParam("pageNo") int pageNum,
                                    @RequestParam("pageSize") int pageSize,
                                    @RequestParam(value = "quickInvestType", required = false) String quickInvestType) {

        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        List<Map<String, Object>> list = new ArrayList<>();
        List<Dictionary> loanTimeDic = basicDataService.getDataDictionaryByTypeCode(SubjectAppListDto.TYPE_CODE_TIME);
        List<Dictionary> loanRateDic = basicDataService.getDataDictionaryByTypeCode(SubjectAppListDto.TYPE_CODE_RATE);
        if (loanTimeDic != null && loanTimeDic.size() == 5
                && loanRateDic != null) {
            Map<String, Object> quickInvestMap1 = new HashMap<>(3);
            quickInvestMap1.put("quickInvestType", "1");
            quickInvestMap1.put("quickInvestTitle", "短期 ("
                    + loanTimeDic.get(0).getItemName() + "-"
                    + loanTimeDic.get(1).getItemName() + "个月)");
            quickInvestMap1.put("quickInvestRate", loanRateDic.get(0)
                    .getItemName());
            Map<String, Object> quickInvestMap2 = new HashMap<>(3);
            quickInvestMap2.put("quickInvestType", "2");
            quickInvestMap2.put("quickInvestTitle", "中期  ("
                    + loanTimeDic.get(2).getItemName() + "-"
                    + loanTimeDic.get(3).getItemName() + "个月)");
            quickInvestMap2.put("quickInvestRate", loanRateDic.get(1)
                    .getItemName());
            Map<String, Object> quickInvestMap3 = new HashMap<>(3);
            quickInvestMap3.put("quickInvestType", "3");
            quickInvestMap3.put("quickInvestTitle", "长期 ("
                    + loanTimeDic.get(4).getItemName() + "个月以上)");
            quickInvestMap3.put("quickInvestRate", loanRateDic.get(2)
                    .getItemName());
            list.add(quickInvestMap1);
            list.add(quickInvestMap2);
            list.add(quickInvestMap3);
        }
        Integer minMonth=null;
        Integer maxMonth =null;
        if (!StringUtils.isEmpty(quickInvestType) && loanTimeDic != null
                && loanTimeDic.size() == 5) {
            if ( "1".equals(quickInvestType)) {
                minMonth= 1;
                maxMonth =Integer.parseInt(loanTimeDic.get(1).getItemName());
            }
            if ( "2".equals(quickInvestType)) {
                minMonth= Integer.parseInt(loanTimeDic.get(2).getItemName());
                maxMonth =Integer.parseInt(loanTimeDic.get(3).getItemName());
            }
            if ( "3".equals(quickInvestType)) {
                minMonth= Integer.parseInt(loanTimeDic.get(4).getItemName());
            }
        }
        Double newbieUsable = investService.getNewbieUsable(userId,null);
        List<Subject> allVisibleSubject;
        PageHelper.startPage(pageNum, pageSize);
        if (newbieUsable <= 0) {
            if(StringUtils.isEmpty(userId)){
                allVisibleSubject  = subjectService.findSubjectAppNewBieAll("",pageNum,pageSize,minMonth,maxMonth);
            }else{
                allVisibleSubject = subjectService.findSubjectNoNewBieAll("",pageNum,pageSize,minMonth,maxMonth);
            }
        } else {
            allVisibleSubject  = subjectService.findSubjectAppNewBieAll("",pageNum,pageSize,minMonth,maxMonth);
        }
        List<SubjectAppListDto> subjectList = new ArrayList<>();
        SubjectAppListDtoNew subjectAppListDtoNew = new SubjectAppListDtoNew();
        List<SubjectAppListDtoNew.Detail> details = new ArrayList<>();
        for (Subject subject : allVisibleSubject) {
            SubjectAppListDto subjectListDto = new SubjectAppListDto();
            BeanUtils.copyProperties(subject, subjectListDto);
            // 对散标名称进行处理
            subjectListDto.setName(IPlanAppListDto.dealName(subject.getName()));

            Integer activityId = subject.getActivityId();
            ActivityMarkConfigure activityMarkConfigure = null;
            if (null != activityId) {
                activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                if(activityMarkConfigure.getIncreaseTerm() != null){
                    subjectListDto.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                }else {
                    subjectListDto.setAddTerm(0);
                }
                subjectListDto.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
            }
            if (Objects.equals(subject.getNewbieOnly(), Subject.NEWBIE_ONLY_Y)) {
               subjectListDto.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                    subjectListDto.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                }
            }

            Double availableQuota = subject.getAvailableAmt() / 100.0;
            subjectListDto.setAvailableAmt(availableQuota);
            if (availableQuota < 10000) {
                subjectListDto.setAvailableAmtStr(df2.format(availableQuota)+ "元");
            } else {
                subjectListDto.setAvailableAmtStr(df.format(availableQuota/10000.0)+ "万");
            }
            Double totalAmt = subject.getTotalAmt() / 100.0;
            subjectListDto.setTotalAmt(String.valueOf(totalAmt));
            if(subject.getPeriod()<30){
                subjectListDto.setTermStr(subject.getPeriod().toString()+"天");
                subjectListDto.setOperationType(0);
            }else{
                subjectListDto.setTermStr(subject.getTerm().toString()+"个月");
                subjectListDto.setOperationType(1);
            }


            if (subject.getBonusRate() != null){
                double bonusRates =subject.getBonusRate().doubleValue() * 100;
                subjectListDto.setBonusRate(bonusRates);
            }else{
                subjectListDto.setBonusRate(0.0);
            }
            double fixRate = subject.getInvestRate().doubleValue() * 100;
            BigDecimal bonusRate1 = subject.getBonusRate();
            subjectListDto.setInvestRateStr(df4.format(fixRate)  + "%");
            subjectListDto.setInvestRate(fixRate);
            if (fixRate % 1.0 == 0 ) {
                if (null != bonusRate1) {
                    double bonusRate = bonusRate1.doubleValue() * 1000;
                    if (bonusRate % 1.0 == 0) {
                        if (bonusRate != 0) {
                            subjectListDto.setInvestRateStr((int)fixRate + "+" + (int)bonusRate / 10.0 + "%");
                        } else {
                            subjectListDto.setInvestRateStr((int)fixRate + "%");
                        }
                    } else {
                        subjectListDto.setInvestRateStr((int)fixRate + "+" + df4.format(bonusRate/10.0) + "%");
                    }
                }
            } else {
                if (null != bonusRate1) {
                    double bonusRate = bonusRate1.doubleValue() * 1000;
                    if (bonusRate % 1.0 == 0) {
                        if (bonusRate != 0) {
                            subjectListDto.setInvestRateStr(df4.format(fixRate) + "+" + (int)bonusRate / 10.0 + "%");
                        } else {
                            subjectListDto.setInvestRateStr(df4.format(fixRate)  + "%");
                        }
                    } else {
                        subjectListDto.setInvestRateStr(df4.format(fixRate) + "+" + df4.format(bonusRate/10.0) + "%");
                    }
                }
            }

            if (Objects.equals(subject.getRaiseStatus(), Subject.RAISE_ANNOUNCING)) {
                String openTime=DateUtil.parseDateTime(subject.getOpenTime(), DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                LocalDate raiseOpenDate = DateUtil.parseDate(openTime, DateUtil.DATE_TIME_FORMATTER_19);
                LocalDate now = LocalDate.now();
                int timeDiff=0;
                if (raiseOpenDate.compareTo(now) == timeDiff) {
                    //今日开售2017-06-17 16:12:30
                    subjectListDto.setMillsTimeStr(openTime.substring(11,16)+"开售");
                } else if (raiseOpenDate.compareTo(now) > timeDiff) {
                    //募集开放时间在现在之后
                    LocalDate now1 = now.plusDays(1);
                    if (now1.compareTo(raiseOpenDate) > timeDiff) {
                        //募集期 今日明日之间
                        subjectListDto.setMillsTimeStr("明日开售");
                    } else if (now1.compareTo(raiseOpenDate) < timeDiff) {
                        //募集期 明日后
                        subjectListDto.setMillsTimeStr(Integer.valueOf(openTime.substring(5,7))+"月"+openTime.substring(8,10)+"日开售");
                    } else {
                        //募集期=今日
                        subjectListDto.setMillsTimeStr(openTime.substring(11,16)+"开售");
                    }
                } else {
                    //募集开放时间在现在之前
                    subjectListDto.setMillsTimeStr("加入时间："+openTime);
                }
            }
            SubjectAppListDtoNew.Detail detail = new SubjectAppListDtoNew.Detail();
            BeanUtils.copyProperties(subjectListDto,detail);
            details.add(detail);

            String transferParamCode=subject.getTransferParamCode();
            String exitLockDaysStr=DRJedisCacheUtil.get("LD"+transferParamCode);
            if(exitLockDaysStr!=null){
            	subjectListDto.setExitLockDaysStr("锁定期"+exitLockDaysStr);
            }else{
            	 SubjectTransferParam sp=subjectTransferParamService.getByTransferParamCode(transferParamCode);
                 //返回锁定期
                   if(sp!=null&&sp.getFullInitiateTransfer()!=null){
                   	if(sp.getFullInitiateTransfer()<31){
                   		subjectListDto.setExitLockDaysStr("锁定期"+sp.getFullInitiateTransfer()+"天");
                   		DRJedisCacheUtil.set("LD"+transferParamCode, sp.getFullInitiateTransfer()+"天", 24*60*60);
                   	}else{
                   		int month=sp.getFullInitiateTransfer()/31;
                   		subjectListDto.setExitLockDaysStr("锁定期"+month+"个月");
                   		DRJedisCacheUtil.set("LD"+transferParamCode, month+"个月", 24*60*60);
                   	}
                   }else{
                	   subjectListDto.setExitLockDaysStr("锁定期0天");
                   }
            }

            subjectListDto.setName("散标"+subject.getTerm()+"个月");
            subjectList.add(subjectListDto);

        }

        List<SubjectDto> lists = new PageUtil().ListSplit(subjectList, pageNum, pageSize);
        SubjectListPageData<SubjectAppListDto> pageData = new SubjectListPageData<>();
        if (StringUtils.isEmpty(quickInvestType))
        {pageData.setInvestItems(list);}
        pageData.setList(lists);
        pageData.setPage(pageNum);
        pageData.setSize(pageSize);
        pageData.setTotalPages(subjectList.size() % pageSize != 0 ? subjectList.size() / pageSize + 1:subjectList.size() / pageSize);
        pageData.setTotal(subjectList.size());
        pageData.setTip("发布时间为工作日10:00,14:00,其余时间随机发布");
        newbieUsable = ArithUtil.round((newbieUsable >= 0 ? newbieUsable/100 : 0), 2);
        pageData.setNewbieAmt(Double.toString(newbieUsable));

        return new RestResponseBuilder<>().success(pageData);
    }

    /**
     *app散标详情页
     * @param subjectId
     * @return
     */
    @GetMapping("/mobile/subject/{subjectId}/details")
    public RestResponse<SubjectAppDto> getSubjectDetailsForApp(@PathVariable("subjectId") String subjectId,
                                                               @RequestParam(value = "userId",required = false) String userId
                                                            ) {
        if(subjectId==null){
            throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage() + ", " + subjectId);
        }
        SubjectAppDto subjectDto = new SubjectAppDto();
        Subject subject = subjectService.getBySubjectId(subjectId);
        if(subject==null){
            logger.warn("该散标[id={}]不存在", subjectId);
            throw new ProcessException(Error.NDR_0805);
        }
        BeanUtils.copyProperties(subject, subjectDto);
        subjectDto.setTotalAmt(subject.getTotalAmt()/100);
        subjectDto.setAvailableAmt(subject.getAvailableAmt()/100);
        subjectDto.setSubjectRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
        subjectDto.setInvestRateStr(df4.format(subject.getInvestRate().doubleValue()*100) + "%");
        if (subject.getBonusRate().compareTo(BigDecimal.ZERO) == 1) {
            subjectDto.setBonusRateStr(df4.format(subject.getBonusRate().doubleValue()*100) + "%");
            subjectDto.setTotalRate(subject.getInvestRate().add(subject.getBonusRate()));
            subjectDto.setTotalRateStr(df4.format(subject.getInvestRate().doubleValue()*100)+"+"+df4.format(subject.getBonusRate().doubleValue()*100)+"%");
        }else{
            subjectDto.setTotalRate(subject.getInvestRate());
            subjectDto.setTotalRateStr(df4.format(subject.getInvestRate().doubleValue()*100)+"%");
        }
        if(subject.getPeriod()<GlobalConfig.ONEMONTH_DAYS){
            subjectDto.setTermStr(subject.getPeriod().toString()+"天");
            subjectDto.setOperationType(0);
        }else{
            subjectDto.setTermStr(subject.getTerm().toString()+"个月");
            subjectDto.setOperationType(1);
        }

        subjectDto.setAvailableAmtStr(subject.getAvailableAmt()/100+"元");

        Integer activityId = subject.getActivityId();
        ActivityMarkConfigure activityMarkConfigure = null;
        if (null != activityId) {
            activityMarkConfigure = activityMarkConfigureService.findById(activityId);
            subjectDto.setActivityUrl(activityMarkConfigureService.getImgUrl(activityId));
            if(activityMarkConfigure.getIncreaseTerm() != null){
                subjectDto.setAddTerm(activityMarkConfigure.getIncreaseTerm());
            }else {
                subjectDto.setAddTerm(0);
            }
        }
        if (Objects.equals(subject.getNewbieOnly(), Subject.NEWBIE_ONLY_Y)) {
            subjectDto.setActivityUrl(activityMarkConfigureService.getNewBieUrl());
            if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                subjectDto.setActivityUrl(activityMarkConfigureService.getImgUrl(activityId));
            }
        }


        Double newbieUsable = 0D;
        if(Subject.NEWBIE_ONLY_Y.equals(subject.getNewbieOnly())){
      	  newbieUsable = investService.getNewbieUsable(userId,null);
	      	subjectDto.setNewbieOnly(1);
	      	subjectDto.setNewbieAmt(newbieUsable/100.0<0D?0D:newbieUsable/100.0);
      	  System.out.println("#########散标用户购买页查询剩余限额---->"+newbieUsable.longValue());
	      }else{
	    	  subjectDto.setNewbieOnly(0);
	    	  if (!StringUtils.isEmpty(userId)) {
	              newbieUsable = investService.getNewbieUsable(userId,null);
	          }
	          subjectDto.setNewbieAmt(newbieUsable/100.0<0D?0D:newbieUsable/100.0);
	      }

        SubjectInvestParamDef param = subjectInvestParamService.getInvestParamDef(subject.getInvestParam());
        if(param!=null){
            subjectDto.setInvestOriginMoney(param.getMinAmt()/100);
            subjectDto.setInvestIncreaseMoney(param.getIncrementAmt()/100);
            subjectDto.setInvestMaxMoney(param.getMaxAmt()/100);
        }
        subjectDto.setDetailUrl(detailUrl+"?subjectId="+subjectId);
        subjectDto.setFengkongUrl(fengkongUrl+"?subjectId="+subjectId+"&from=app");
        subjectDto.setInvestUrl(investUrl+"?subjectId="+subjectId+"&from=app");
        Map<String,String> shareDetailsMap = new HashMap<>(4);
        shareDetailsMap.put("shareTitle","期待年回报率8~12%-短期理财平台");
        shareDetailsMap.put("shareContent","注册送360元红包+1万元体验金,1元起投超低使用门槛.厦门银行资金存管,安全透明合规.");
        shareDetailsMap.put("shareLinkUrl",shareLinkUrl+subjectId);
        shareDetailsMap.put("shareProUrl",imagePrefix+"/app/loan/share.png");
        subjectDto.setShareDetailsMap(shareDetailsMap);
        //预告中
        if(Subject.RAISE_ANNOUNCING.equals(subject.getRaiseStatus())){
            //倒计时时间戳
            String openTime=DateUtil.parseDateTime(subject.getOpenTime(), DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDate raiseOpenDate = DateUtil.parseDate(openTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDateTime raiseOpenDateTime = DateUtil.parseDateTime(openTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDate now = LocalDate.now();
            LocalDateTime nowTime = LocalDateTime.now();
            if (raiseOpenDate.compareTo(now) == 0) {
                if (nowTime.plusHours(HOUR_TO_REMIND).compareTo(raiseOpenDateTime) < 0) {
                    //一个小时以外
                    subjectDto.setMillsTimeStr(openTime.substring(11,16)+"开售");
                    subjectDto.setMillsTime("");
                } else {
                    //一个小时内、刚好一个小时
                    long time = raiseOpenDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                            nowTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    if (time <= 0) {
                        subjectDto.setMillsTime(String.valueOf(0));
                    } else {
                        subjectDto.setMillsTime(String.valueOf(time));
                    }
                    subjectDto.setMillsTimeStr("");
                }
            } else if (raiseOpenDate.compareTo(now) > 0) {
                //募集开放时间在现在之后
                LocalDate now1 = now.plusDays(1);
                if (now1.compareTo(raiseOpenDate) > 0) {
                    //募集期 今日明日之间
                    subjectDto.setMillsTimeStr("明日开售");
                } else if (now1.compareTo(raiseOpenDate) < 0) {
                    //募集期 明日后
                    subjectDto.setMillsTimeStr(Integer.valueOf(openTime.substring(5,7))+"月"+openTime.substring(8,10)+"日开售");
                } else {
                    //募集期=今日
                    subjectDto.setMillsTimeStr(openTime.substring(11,16)+"开售");
                }
            } else {
                //募集开放时间在现在之前
                subjectDto.setMillsTimeStr("加入时间："+openTime);
            }
        }


        String transferParamCode=subject.getTransferParamCode();
        String exitLockDaysStr=DRJedisCacheUtil.get("LD2"+transferParamCode);
        if(exitLockDaysStr!=null){
        	subjectDto.setExitLockDaysStr(exitLockDaysStr);
        }else{
        	 SubjectTransferParam sp=subjectTransferParamService.getByTransferParamCode(transferParamCode);
             //返回锁定期
               if(sp!=null&&sp.getFullInitiateTransfer()!=null){
               		subjectDto.setExitLockDaysStr(sp.getFullInitiateTransfer()+"天");
               	DRJedisCacheUtil.set("LD2"+transferParamCode, subjectDto.getExitLockDaysStr(), 24*60*60);
               }else{
            	   subjectDto.setExitLockDaysStr("0天");
               }
        }
        return new RestResponseBuilder<SubjectAppDto>().success(subjectDto);
    }

    //app-散标抢购页
    @GetMapping("/authed/{subjectId}/{userId}/purchase")
    public RestResponse<SubjectAppPurchaseDto> getSubjectPurchase(@PathVariable("subjectId") String subjectId,
                                                                 @PathVariable("userId") String userId,
                                                                  @RequestParam("requestSource") String requestSource
                                                                 ){
        SubjectAppPurchaseDto subjectAppPurchaseDto = new SubjectAppPurchaseDto();
        Subject subject = subjectService.getBySubjectId(subjectId);
        if(subject==null){
            logger.warn("该散标[id={}]不存在", subjectId);
            throw new ProcessException(Error.NDR_0805);
        }
        UserAccount userAccount = userAccountService.getUserAccount(userId);
        SubjectInvestParamDef param = subjectInvestParamService.getInvestParamDef(subject.getInvestParam());
        BeanUtils.copyProperties(subject, subjectAppPurchaseDto);
        subjectAppPurchaseDto.setAvailMoney(null==userAccount ? 0.0 : userAccount.getAvailableBalance());
        subjectAppPurchaseDto.setMarkedWords(param.getMinAmt()/100.0 + "元起投");
        subjectAppPurchaseDto.setInvestOriginMoney(param.getMinAmt()/100);
        subjectAppPurchaseDto.setInvestIncreaseMoney(param.getIncrementAmt()/100);
        subjectAppPurchaseDto.setInvestMaxMoney(param.getMaxAmt()/100);
        subjectAppPurchaseDto.setTotalAmt(subject.getTotalAmt()/100);
        subjectAppPurchaseDto.setAvailableAmt(subject.getAvailableAmt()/100);
        subjectAppPurchaseDto.setSubjectRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
        subjectAppPurchaseDto.setInvestRateStr(df4.format(subject.getInvestRate().doubleValue()*100) + "%");
        if (subject.getBonusRate().compareTo(BigDecimal.ZERO) == 1) {
            subjectAppPurchaseDto.setBonusRateStr(df4.format(subject.getBonusRate().doubleValue()*100) + "%");
            subjectAppPurchaseDto.setTotalRate(subject.getInvestRate().add(subject.getBonusRate()));
            subjectAppPurchaseDto.setTotalRateStr(df4.format(subjectAppPurchaseDto.getTotalRate().doubleValue()*100) + "%");
            Integer activityId = subject.getActivityId();
            ActivityMarkConfigure activityMarkConfigure = null;
            if (null != activityId) {
                activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                if(activityMarkConfigure.getIncreaseTerm() != null){
                    subjectAppPurchaseDto.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                    subjectAppPurchaseDto.setTotalRateStr(df4.format(subject.getInvestRate().doubleValue()*100) + "+"+df4.format(subject.getBonusRate().doubleValue()*100) + "%");
                }else {
                    subjectAppPurchaseDto.setAddTerm(0);
                }
            }
        }else{
            subjectAppPurchaseDto.setTotalRate(subject.getInvestRate());
            subjectAppPurchaseDto.setTotalRateStr(df4.format(subject.getInvestRate().doubleValue()*100) + "%");
        }
        if(subject.getPeriod()<GlobalConfig.ONEMONTH_DAYS){
            subjectAppPurchaseDto.setTermStr(subject.getPeriod().toString()+"天");
            subjectAppPurchaseDto.setOperationType(0);
        }else{
            subjectAppPurchaseDto.setTermStr(subject.getTerm().toString()+"个月");
            subjectAppPurchaseDto.setOperationType(1);
        }
        subjectAppPurchaseDto.setAvailableAmtStr(String.valueOf(subject.getAvailableAmt()/100)+"元");
        Double newbieUsable = 0D;
        subjectAppPurchaseDto.getNewbieOnly();
        if(Subject.NEWBIE_ONLY_Y.equals(subject.getNewbieOnly())){
        	  newbieUsable = investService.getNewbieUsable(userId,null);
        	  subjectAppPurchaseDto.setNewbieOnly(1);
        	  subjectAppPurchaseDto.setNewbieAmt(newbieUsable/100);
              Config iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);
        	  System.out.println("#########散标用户购买页查询剩余限额---->"+newbieUsable.longValue());
        	  if(iPlanNewbieAmtConfig==null || "0".equals(iPlanNewbieAmtConfig.getValue().toString())){
              	if(requestSource.contains(GlobalConfig.APP_ANDROID)){
              		if(requestSource.compareTo(GlobalConfig.APP_ANDROID_VERSION)>=0){
              			subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元 (限任意散标项目)");
                  	}else{
                  		 subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元");
                  	}
              	}

              	if(requestSource.contains(GlobalConfig.APP_IOS)){
              		if(requestSource.compareTo(GlobalConfig.APP_IOS_VERSION)>=0){
              			subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元 (限任意散标项目)");
              		}else{
              			 subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元");
              		}
              	}
              }else{
            	  if(requestSource.contains(GlobalConfig.APP_ANDROID)){
                		if(requestSource.compareTo(GlobalConfig.APP_ANDROID_VERSION)>=0){
                			subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元 (含任意定期项目)");
                    	}else{
                    		 subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元");
                    	}
                	}
                	
                	if(requestSource.contains(GlobalConfig.APP_IOS)){
                		if(requestSource.compareTo(GlobalConfig.APP_IOS_VERSION)>=0){
                			subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元 (含任意定期项目)");
                		}else{
                			 subjectAppPurchaseDto.setNewbieTip("剩余可投额度"+newbieUsable.longValue()/100+"元");
                		}
                	}
              }
        }else{
        	 subjectAppPurchaseDto.setNewbieOnly(0);
        	 subjectAppPurchaseDto.setNewbieTip("");
        }


        List<SubjectAppPurchaseDto.RedPacketApp> redPacketAppList = new ArrayList<>();
        IPlan iPlan = new IPlan();
        iPlan.setId(subject.getId());
        iPlan.setNewbieOnly(subject.getNewbieOnly());
        iPlan.setRateType(0);
        iPlan.setFixRate(subject.getInvestRate());
        iPlan.setTerm(subject.getTerm());
        iPlan.setActivityId(subject.getActivityId());
        List<RedPacket> redPacketList = redPacketService.getUsablePacketCreditAll(userId, subject, "ios_4.6.0","subject");
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
                redPacketApp.setUseStatus(redPacket.getUseStatus());
                redPacketApp.setInvestMoney(redPacket.getInvestMoney());
                redPacketAppList.add(redPacketApp);
            }
        }
        subjectAppPurchaseDto.setRedPacketAppList(redPacketAppList);
        subjectAppPurchaseDto.setRedCount(redPacketList==null?0:redPacketList.size());

        //用户风险测评
        String whereAnswer = userService.getComplianceAnswer(userId);
        String setUpDesc = "";
        if(null == whereAnswer || ("").equals(whereAnswer)){
            subjectAppPurchaseDto.setWhereAnswer("false");

        }else{
            subjectAppPurchaseDto.setWhereAnswer("true");
            if("A".equals(whereAnswer)){
                setUpDesc = "积极型";
            }else if("B".equals(whereAnswer)){
                setUpDesc = "稳健型";
            }else {
                setUpDesc = "保守型";
            }
        }
        subjectAppPurchaseDto.setSetUpDesc(setUpDesc);

        //是否激活、是否开户,是否绑卡
        subjectAppPurchaseDto.setAccountFlag(userAccountService.checkIfOpenAccount(userId) ? 1 : 0);
        subjectAppPurchaseDto.setActivateFlag(userAccountService.checkIfActive(userId));
        subjectAppPurchaseDto.setBankCardFlag(userAccountService.checkBankCardFlag(userId)>0?1:0);
        //预告中
        if(Subject.RAISE_ANNOUNCING.equals(subject.getRaiseStatus())){
            //倒计时时间戳
            String openTime=DateUtil.parseDateTime(subject.getOpenTime(), DateUtil.DATE_TIME_FORMATTER_17).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            LocalDate raiseOpenDate = DateUtil.parseDate(openTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDateTime raiseOpenDateTime = DateUtil.parseDateTime(openTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDate now = LocalDate.now();
            LocalDateTime nowTime = LocalDateTime.now();
            if (raiseOpenDate.compareTo(now) == 0) {
                if (nowTime.plusHours(HOUR_TO_REMIND).compareTo(raiseOpenDateTime) < 0) {
                    //一个小时以外
                    subjectAppPurchaseDto.setMillsTimeStr(openTime.substring(11,16)+"开售");
                    subjectAppPurchaseDto.setMillsTime("");
                } else {
                    //一个小时内、刚好一个小时
                    long time = raiseOpenDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                            nowTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    if (time <= 0) {
                        subjectAppPurchaseDto.setMillsTime(String.valueOf(0));
                    } else {
                        subjectAppPurchaseDto.setMillsTime(String.valueOf(time));
                    }
                    subjectAppPurchaseDto.setMillsTimeStr("");
                }
            } else if (raiseOpenDate.compareTo(now) > 0) {
                //募集开放时间在现在之后
                LocalDate now1 = now.plusDays(1);
                if (now1.compareTo(raiseOpenDate) > 0) {
                    //募集期 今日明日之间
                    subjectAppPurchaseDto.setMillsTimeStr("明日开售");
                } else if (now1.compareTo(raiseOpenDate) < 0) {
                    //募集期 明日后
                    subjectAppPurchaseDto.setMillsTimeStr(Integer.valueOf(openTime.substring(5,7))+"月"+openTime.substring(8,10)+"日开售");
                } else {
                    //募集期=今日
                    subjectAppPurchaseDto.setMillsTimeStr(openTime.substring(11,16)+"开售");
                }
            } else {
                //募集开放时间在现在之前
                subjectAppPurchaseDto.setMillsTimeStr("加入时间："+openTime);
            }
        }
        subjectAppPurchaseDto.setProtocolUrl(protocolUrl);
        subjectAppPurchaseDto.setRiskProtocolUrl(riskProtocolUrl);

        String transferParamCode=subject.getTransferParamCode();
        String exitLockDaysStr=DRJedisCacheUtil.get("LD2"+transferParamCode);
        if(exitLockDaysStr!=null){
        	subjectAppPurchaseDto.setExitLockDaysStr(exitLockDaysStr);
        }else{
        	 SubjectTransferParam sp=subjectTransferParamService.getByTransferParamCode(transferParamCode);
             //返回锁定期
               if(sp!=null&&sp.getFullInitiateTransfer()!=null){
            	   subjectAppPurchaseDto.setExitLockDaysStr(sp.getFullInitiateTransfer()+"天");
               	DRJedisCacheUtil.set("LD2"+transferParamCode, subjectAppPurchaseDto.getExitLockDaysStr(), 24*60*60);
               }else{
            	   subjectAppPurchaseDto.setExitLockDaysStr("0天");
               }
        }
        return new RestResponseBuilder<SubjectAppPurchaseDto>().success(subjectAppPurchaseDto);
    }

    /**
     * 散标投资管理
     * @param userId
     * @param pageNum
     * @param pageSize
     * @param type
     * @return
     */
    @GetMapping("/subject/user/management")
    public RestResponse subjectManagement(@RequestParam("userId") String userId,
                                           @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                           @RequestParam("type") int type){
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new ProcessException(Error.NDR_0101);
        }
        if(type == AppCreditManageHoldDto.PAGE_TYPE_TRANSFERRING){
            SubjectAppCreditTransferDto subjectAppCreditTransferDto = new SubjectAppCreditTransferDto();
            List<SubjectAppCreditTransferDto.Detail> details = new ArrayList<>();

            //总转让金额
            Double amount = 0.0;
            //已成交金额
            Double finishAmt = 0.0;

            //单个转让金额
            Double holdingAmt = 0.0;

            //单个成交金额
            Double transferAmt = 0.0;

            //转让中
            List<CreditOpening> creditOpenings = creditOpeningService.getByUserIdAndStatusAndOpenChannel(userId,CreditOpening.OPEN_CHANNEL);
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
                for (CreditOpening creditOpening : newCreditOpenings) {
                    Credit credit = creditService.getById(creditOpening.getCreditId());
                    Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
                    SubjectAppCreditTransferDto.Detail detail = new SubjectAppCreditTransferDto.Detail();
                    detail.setId(creditOpening.getId());
                    detail.setName(subject.getName());
                    detail.setStatus(CreditConstant.TRANSFERING);
                    detail.setRepayType(subjectService.getRepayType(credit.getSubjectId()));
                    detail.setBuyTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
                    //持有金额
                    holdingAmt = creditOpening.getTransferPrincipal() / 100.0;
                    detail.setHoldingAmt(holdingAmt);
                    detail.setHoldingAmtStr(df4.format(detail.getHoldingAmt()));
                    //成交金额
                    transferAmt = (creditOpening.getTransferPrincipal() - creditOpening.getAvailablePrincipal()) / 100.0;
                    if(CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
                        SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
                        transferAmt= subjectTransLog.getProcessedAmt() / 100.0;
                    }
                    detail.setTransferAmt(transferAmt);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));

                    //剩余时间
                    String currentDate = DateUtil.getCurrentDateShort().substring(0, 8);
                    String endDate = credit.getEndTime().substring(0, 8);
                    long days = DateUtil.betweenDays(currentDate, endDate);
                    detail.setResidualDay((int) days);

                    //红包相关
                    detail.setRedPacket("");
                    SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
                    SubjectTransLog log = subjectTransLogService.getById(subjectAccount.getTransLogId());
                    if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                        RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                        detail.setRedPacket(redPacketService.getRedpackeMsg(packet));
                    }
                    //活动相关
                    detail.setActivityName("");
                    detail.setIncreaseInterest(0.0);
                    detail.setFontColor("");
                    detail.setBackground("");
                    Integer activityId = subject.getActivityId();
                    ActivityMarkConfigure activityMarkConfigure = null;
                    if (null != activityId) {
                        detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                        activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                        if(activityMarkConfigure.getIncreaseTerm() != null){
                            detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                        }else {
                            detail.setAddTerm(0);
                        }
                    }
                    if (Objects.equals(subject.getNewbieOnly(), Subject.NEWBIE_ONLY_Y)) {
                        detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                        detail.setNewbieOnly(Subject.NEWBIE_ONLY_Y);
                        if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                            detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                        }
                    }

                    detail.setSubjectId(subject.getSubjectId());
                    details.add(detail);
                }
            }
            subjectAppCreditTransferDto.setAmount(amount);
            subjectAppCreditTransferDto.setAmountStr(df4.format(amount));
            subjectAppCreditTransferDto.setFinishAmt(finishAmt);
            subjectAppCreditTransferDto.setFinishAmtStr(df4.format(finishAmt));
            subjectAppCreditTransferDto.setPageType(AppCreditManageHoldDto.PAGE_TYPE_TRANSFERRING);
            subjectAppCreditTransferDto.setDetails(details);
            return new RestResponseBuilder<>().success(subjectAppCreditTransferDto);
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
                    //回款金额
                    detail.setTransferAmt((principal + interest) / 100.0);
                    detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                    amount += detail.getHoldingAmt();
                    //实际到账金额
                    detail.setActualAmt(detail.getTransferAmt());
                    detail.setActualAmtStr(df4.format(detail.getTransferAmt()));
                    totalActualAmt += detail.getActualAmt();
                    detail.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());
                    //红包相关
                    detail.setRedPacket("");
                    SubjectTransLog log = subjectTransLogService.getById(subjectAccount.getTransLogId());
                    if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                        RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                        detail.setRedPacket(redPacketService.getRedpackeMsg(packet));
                    }
                    //活动相关
                    detail.setActivityName("");
                    detail.setIncreaseInterest(0.0);
                    detail.setFontColor("");
                    detail.setBackground("");
                    Integer activityId = subject.getActivityId();
                    ActivityMarkConfigure activityMarkConfigure = null;
                    if (null != activityId) {
                        detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                        activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                        if( activityMarkConfigure.getIncreaseTerm() != null){
                            detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                        }else {
                            detail.setAddTerm(0);
                        }
                    }
                    if (Objects.equals(subject.getNewbieOnly(), Subject.NEWBIE_ONLY_Y)) {
                        detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                        detail.setNewbieOnly(Subject.NEWBIE_ONLY_Y);
                        if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                            detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                        }
                    }
                    detail.setSubjectId(subject.getSubjectId());

                    // 散标提前还款
                    List<SubjectRepaySchedule> repaySchedules = subjectRepayScheduleDao.findBeforeRepayBySubjectIdAnsStatus(credit.getSubjectId());
                    if (!CollectionUtils.isEmpty(repaySchedules)) {
                        detail.setBeforeRepayFlag(AppCreditManageFinishDto.BEFORE_REPAY_FLAG_Y);
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
                        detail.setRedPacket(redPacketService.getRedpackeMsg(packet));
                    }
                    //活动相关
                    detail.setActivityName("");
                    detail.setIncreaseInterest(0.0);
                    detail.setFontColor("");
                    detail.setBackground("");
                    Integer activityId = subject.getActivityId();
                    ActivityMarkConfigure activityMarkConfigure = null;
                    if (null != activityId) {
                        detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                        activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                        if( activityMarkConfigure.getIncreaseTerm() != null){
                            detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                        }else {
                            detail.setAddTerm(0);
                        }
                    }
                    if (Objects.equals(subject.getNewbieOnly(), Subject.NEWBIE_ONLY_Y)) {
                        detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                        detail.setNewbieOnly(Subject.NEWBIE_ONLY_Y);
                        if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                            detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                        }
                    }
                    details.add(detail);
                }
            }
            List<AppCreditManageFinishDto.Detail> list = new PageUtil().ListSplit(details, pageNum, pageSize);
            appCreditManageFinishDto.setAmount(amount);
            appCreditManageFinishDto.setAmountStr(df4.format(amount));
            appCreditManageFinishDto.setTotalActualAmt(totalActualAmt);
            appCreditManageFinishDto.setTotalActualAmtStr(df4.format(totalActualAmt));
            appCreditManageFinishDto.setPageType(AppCreditManageHoldDto.PAGE_TYPE_FINISH);
            appCreditManageFinishDto.setDetails(list);
            return new RestResponseBuilder<>().success(appCreditManageFinishDto);
        }

        //散标投资管理列表
        SubjectAppManagementDto subjectAppManagementDto = this.getSubjectManageMethod(userId, type, pageNum, pageSize);
        return new RestResponseBuilder<>().success(subjectAppManagementDto);
    }

    private SubjectAppManagementDto getSubjectManageMethod(String userId, int type, int pageNo, int pageSize) {
        SubjectAppManagementDto subjectAppManagementDto = new SubjectAppManagementDto();
        List<SubjectAppManagementDto.Detail> details = new ArrayList<>();
        //本金
        double principal = 0;
        //收益
        double interest = 0;
        //提前还款金额
        double advanceRepayAmt = 0;
        List<SubjectAccount> accounts = new ArrayList<>();
        if (type == SubjectAccount.STATUS_PROCEEDS) {
            accounts =  subjectAccountService.getHoldingSubjectAccountByUserId(userId);
        }
        subjectAppManagementDto.setPageNo(pageNo);
        subjectAppManagementDto.setPageSize(pageSize);
        if (accounts != null && accounts.size() > 0) {
            for (SubjectAccount account : accounts) {
                principal += account.getCurrentPrincipal()/100.0;
                interest += account.getExpectedInterest()/100.0 + account.getSubjectExpectedBonusInterest()/100.0;
            }
            subjectAppManagementDto.setTotalSize(accounts.size());
            //总页数=（总记录数+每页显示数-1）/每页显示数
            subjectAppManagementDto.setTotalPages((accounts.size()+pageSize-1)/pageSize);
        }
        List<SubjectAccount> subjectAccounts =  subjectAccountService.getHoldingSubjectAccountByUserId(userId, pageNo, pageSize);
        if (subjectAccounts != null && subjectAccounts.size() > 0) {
            for (SubjectAccount account : subjectAccounts) {
                SubjectAppManagementDto.Detail detail = new SubjectAppManagementDto.Detail();
                detail.setAccountId(Integer.toString(account.getId()));
                detail.setAccountStatus(account.getStatus());
                detail.setSubjectId(account.getSubjectId());
                Subject subject = subjectService.getBySubjectId(account.getSubjectId());
                SubjectTransLog log = subjectTransLogService.getById(account.getTransLogId());
                detail.setSubjectName(subject.getName());
                detail.setRepayType(subjectAppManagementDto.getRepayTypeStr(subject.getRepayType()));
                detail.setStatus(subject.getRaiseStatus());
                detail.setInvestAmt(df4.format(log.getTransAmt()/100.0));
                double rate = subject.getInvestRate().doubleValue()
                        + (subject.getBonusRate() != null ? subject.getBonusRate().doubleValue() : 0);
                ActivityMarkConfigure activityMarkConfigure = null;
                if (subject.getActivityId() != null && subject.getActivityId() > 0) {
                    activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
                    if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                        rate = subject.getInvestRate().doubleValue();
                    }
                }
                detail.setRate(df4.format(rate * 100 ) + "%");
                detail.setExpectTotalAmt(df4.format( (account.getCurrentPrincipal()+ account.getExpectedInterest()+ account.getSubjectExpectedBonusInterest())/100.0));
                detail.setAdvanceRepayAmt("");
                String term = "";
                if (subject.getPeriod() < 30) {
                    term = subject.getPeriod() + "天";
                } else {
                    term = subject.getTerm() + "个月";
                }
                detail.setTerm(term);
                String redPacketMsg = "";
                if (log != null && log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                    detail.setRedPacketId(log.getRedPacketId());
                    RedPacket packet = redPacketService.getRedPacketById(log.getRedPacketId());
                    if (packet != null) {
                        redPacketMsg = redPacketService.getRedpackeMsg(packet);
                    }
                }
                Long countDown = 0L;
                try {
                    Date transTime = sdf1.parse(log.getTransTime());
                    Date countDownTime = DateUtils.addMinutes(transTime, 3);
                    if (countDownTime.getTime() >= (new Date()).getTime()) {
                        countDown = countDownTime.getTime() - (new Date()).getTime();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                detail.setCountDown(Long.toString(countDown));
                detail.setTransLogId(log.getId());
                detail.setRedPacketMsg(redPacketMsg);
                detail.setExpectInterest(df4.format(account.getExpectedInterest()/100.0 + account.getSubjectExpectedBonusInterest()/100.0));
                detail.setPaidInterest(df4.format(account.getPaidInterest()/100.0 + account.getSubjectPaidBonusInterest()/100.0));
                Integer activityId = subject.getActivityId();
                detail.setImgUrl("");
                if (null != activityId) {
                    detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                    detail.setActivityMarkConfigure(activityMarkConfigureService.findById(activityId));
                    activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                    if( activityMarkConfigure.getIncreaseTerm() != null){
                        detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                    }else {
                        detail.setAddTerm(0);
                    }
                }
                if (Objects.equals(subject.getNewbieOnly(), Subject.NEWBIE_ONLY_Y)) {
                    detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                    detail.setNewbieOnly(Subject.NEWBIE_ONLY_Y);
                    if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                        detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                    }
                }
                String creditUrl = "";
                String endTime = "生成中";
                Credit credit = creditService.findBySourceAccountIdAndTarget(account.getId(), Credit.TARGET_SUBJECT);
                if (credit != null && (Credit.CREDIT_STATUS_HOLDING == credit.getCreditStatus())) {
                    creditUrl = creditDetailUrl+"?subjectId=" + account.getSubjectId() + "&creditId=" + credit.getId();
                    endTime = credit.getEndTime() != null && credit.getEndTime().length() >= 8 ? credit.getEndTime().substring(0, 4)+"-"+credit.getEndTime().substring(4, 6)+"-"+credit.getEndTime().substring(6, 8) : "生成中";
                }
                detail.setEndTime(endTime);
                detail.setCreditUrl(creditUrl);
                //是否可转让
                detail.setTransfer(subjectService.checkCondition(subject,account));
                detail.setMessage(subjectService.checkConditionStr(subject,account));
                details.add(detail);
            }
        }
        subjectAppManagementDto.setPageType(type);
        subjectAppManagementDto.setPrincipal(df4.format(principal));
        subjectAppManagementDto.setInterest(df4.format(interest));
        subjectAppManagementDto.setAdvanceRepayAmt(df4.format(advanceRepayAmt));
        subjectAppManagementDto.setDetails(details);
        return subjectAppManagementDto;
    }
    /**
     * 回款跟踪
     * @param subjectId  标的号
     * @param userId    用户ID
     * @param accountId 散标账户id
     * @return
     */
    @GetMapping("/subject/repay/record")
    public RestResponse<SubjectAppRepayDetailDto> getSubjectRepayDetail(@RequestParam("subjectId") String subjectId,
                                                                              @RequestParam("userId") String userId,
                                                                              @RequestParam("accountId") Integer accountId) {
        if(subjectId==null||userId==null||accountId==null){
            throw new ProcessException(Error.NDR_0101.getCode(), Error.NDR_0101.getMessage());
        }
        SubjectAccount account = subjectAccountService.findAccountById(accountId);
        Subject subject = subjectService.findSubjectBySubjectId(subjectId);
        //查询对应债权
        Credit credit = creditService.findBySourceAccountIdAndUserId(accountId,userId,subjectId,Credit.SOURCE_CHANNEL_SUBJECT);
        //查询未还的还款计划
        List<SubjectRepaySchedule> schedules = repayScheduleService.getSubjectRepayScheduleBySubjectIdNotRepay(subjectId);
        SubjectAppRepayDetailDto detailDto = new SubjectAppRepayDetailDto();
        detailDto.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
        detailDto.setSubjectId(subjectId);
        //计息时间
        detailDto.setRecordTime(DateUtil.getDateStr(DateUtil.parseDate(account.getCreateTime().substring(0, 10), DateUtil.DATE_TIME_FORMATTER_10).plusDays(1), DateUtil.DATE_TIME_FORMATTER_10));
        //查询明细
        List<SubjectRepayDetail> details = subjectRepayDetailDao.findBySubjectIdAndStatusAndAccountId(subjectId,userId,accountId,Credit.SOURCE_CHANNEL_SUBJECT);
        List<SubjectAppRepayDetailDto.RepayDetail> detailsForRepay = new ArrayList<>();
        if(credit != null){
            detailsForRepay = repayScheduleService.commonRepaymentMethod(credit,schedules,details);
        }
        detailDto.setDetails(detailsForRepay);
        InvestRedpacket investRedpacket = redPacketService.getReceivedRedPacketAmt(userId, String.valueOf(subjectId), RedPacket.INVEST_REDPACKET_TYPE_SUBJECT);
        if (investRedpacket != null && investRedpacket.getRewardMoney() != 0 && investRedpacket.getSendRedpacketTime() != null) {
            RedPacket red = redPacketService.getRedPacketById(investRedpacket.getRepackedId());
            if(red!=null){
                String msg = redPacketService.getRedpackeMsg(red);
                detailDto.setRedPacketDesc(msg + ArithUtil.round(investRedpacket.getRewardMoney(), 2) + "元");
                detailDto.setRedPacketDate(DateUtil.SDF_10.format(investRedpacket.getSendRedpacketTime()));
                detailDto.setRedFlag(1);
            }
        }
        return new RestResponseBuilder<SubjectAppRepayDetailDto>().success(detailDto);
    }

    @GetMapping("/yjt/management/repay/detail")
    public RestResponse getYjtRepayDetail(@RequestParam("userId") String userId, @RequestParam("iplanId") int iplanId) {
        // 省心投还款跟踪
        YjtAppRepayDetailDto yjtAppRepayDetailDto = new YjtAppRepayDetailDto();

        IPlan iPlan = iPlanService.getIPlanById(iplanId);
        if (iPlan == null) {
            throw new IllegalArgumentException("iplan is null");
        }
        if (!(iPlan.getStatus().equals(IPlan.STATUS_EARNING) || iPlan.getStatus().equals(IPlan.STATUS_END))) {
            return new RestResponseBuilder<>().success(yjtAppRepayDetailDto);
        }
        //查询用户省心投账户
        IPlanAccount iPlanAccount = iPlanAccountService.getByIPlanIdAndUserId(iplanId,userId);
        if (iPlanAccount == null) {
            throw new IllegalArgumentException("iPlanAccount is null");
        }
        double repaidPrincipal = 0;// 已回本金
        double repaidInterest = 0;// 已回利息
        double notRepayPrincipal = 0;// 未回本金
        double notRepayInterest = 0;// 未回利息
        // 列表
        List<YjtAppRepayDetailDto.RepaySchedule> repaySchedules = new ArrayList<>();

        Map<Integer, Map<String, Object>> detailMapTemp = new HashMap<>(16);
        // 省心投还款计划列表
        List<IPlanRepaySchedule> iPlanRepaySchedules = iPlanRepayScheduleService.getRepaySchedule(iplanId);
        for (IPlanRepaySchedule iPlanRepaySchedule : iPlanRepaySchedules) {
            YjtAppRepayDetailDto.RepaySchedule repaySchedule = new YjtAppRepayDetailDto.RepaySchedule();
            repaySchedule.setTitle("第"+iPlanRepaySchedule.getTerm()+"期");
            repaySchedule.setTerm(iPlanRepaySchedule.getTerm());
            // 还款时间，展示为第一笔还款时间-最后一笔还款时间
            repaySchedule.setDate(iPlanRepaySchedule.getDueDate());
            YjtAppRepayDetailDto.RepayScheduleDetail repayScheduleDetail = new YjtAppRepayDetailDto.RepayScheduleDetail();
            // 还款时间，展示为第一笔还款时间-最后一笔还款时间
            repayScheduleDetail.setTitle("第"+iPlanRepaySchedule.getTerm()+"期");
            repaySchedule.setRepayScheduleDetail(repayScheduleDetail);
            repaySchedules.add(repaySchedule);
            detailMapTemp.put(repaySchedule.getTerm(), new HashMap<>(16));
        }

        // 查询用户省心投下的债权
        List<Credit> yjtCredits  = creditService.findYJTByUserIdAndSourceChannelAndAccountId(userId,Credit.SOURCE_CHANNEL_YJT,iPlanAccount.getId());
        // 对债权按照subjectId进行分组，累计holdingPrincipal, startTime取最近的时间
        Map<String, List<Credit>> creditsBySubject = yjtCredits.stream().collect(Collectors.groupingBy(Credit::getSubjectId));
        for(Map.Entry<String, List<Credit>> entry : creditsBySubject.entrySet()){
            String subjectId = entry.getKey();
            Subject subject = subjectService.getBySubjectId(subjectId);
            List<Credit> credits = entry.getValue();
            Credit credit = credits.get(0);
            credit.setHoldingPrincipal(credits.stream().map(Credit::getHoldingPrincipal).reduce(Integer::sum).orElse(0));
            credit.setStartTime(credits.stream().max(Comparator.comparing(Credit::getStartTime)).get().getStartTime());
            //查询这个标的开始期数
            int iplanTerm = subjectRepayScheduleService.getCurrentRepayScheduleTerm(subjectId,credit.getStartTime().substring(0,8))-1;
            //已还的，正常完成，提前结清
            List<SubjectRepaySchedule> finishedSubjectRepaySchedules = subjectRepayScheduleService.getFinishedBySubjectId(subjectId, iplanTerm);
            for(SubjectRepaySchedule schedule : finishedSubjectRepaySchedules){
                SubjectRepayDetail detail = subjectRepayDetailDao.findYJTBySubjectIdAndStatusAndAccountId(subjectId,userId,credit.getSourceAccountId(),schedule.getId());
                if (detail == null) {
                    continue;
                }
                int termIndex = schedule.getTerm()-1;
                // 债权打包省心投
                if (iPlan.getPackagingType() == 1) {
                    termIndex = iPlanRepayScheduleService.getCurrentTermByIplanId(iPlan.getId(),schedule.getRepayDate()) -1;
                }
                YjtAppRepayDetailDto.RepaySchedule repaySchedule = repaySchedules.get(termIndex);
                Map<String, Object> repayDetailMap = detailMapTemp.get(termIndex + 1);
                List<YjtAppRepayDetailDto.RepayDetail> repayDetails = (List<YjtAppRepayDetailDto.RepayDetail>)repayDetailMap.get(detail.getCreateTime().substring(0, 10));
                if (repayDetails == null) {
                    repayDetails = new ArrayList<>();
                    repayDetailMap.put(detail.getCreateTime().substring(0, 10), repayDetails);
                }
                YjtAppRepayDetailDto.RepayDetail repayDetail = new YjtAppRepayDetailDto.RepayDetail();
                repayDetail.setName(subject.getName());
                repayDetail.setPrincipal(df4.format(detail.getPrincipal()/100.0));
                repayDetail.setInterest(df4.format(detail.getInterest()/100.0));
                double bonusInterest = (detail.getBonusInterest() != null ? detail.getBonusInterest() : 0)/100.0;
                double bonusReward = (detail.getBonusReward() != null ? detail.getBonusReward() : 0)/100.0;
                repayDetail.setBonusInterest(df4.format(bonusInterest+bonusReward));
                if (bonusInterest+bonusReward > 0) {
                    repaySchedule.setBonusFlag(YjtAppRepayDetailDto.BONUS_FLAG_Y);
                }
                if(detail.getStatus()==0 || (detail.getStatus()==1 && detail.getCurrentStep()!=3)){
                    repayDetail.setStatus(YjtAppRepayDetailDto.STATUS_REPAING);
                    repaySchedule.setTotalNotRepayAmt(repaySchedule.getTotalNotRepayAmt() + (detail.getPrincipal() + detail.getInterest() + detail.getBonusInterest() + detail.getBonusReward()) / 100.0);
                    notRepayPrincipal += ArithUtil.round(detail.getPrincipal() / 100.0, 2);
                    notRepayInterest += ArithUtil.round((detail.getInterest() + (detail.getBonusInterest() != null ? detail.getBonusInterest() : 0) + (detail.getBonusReward() != null ? detail.getBonusReward() : 0)) / 100.0, 2);
                }else{
                    repayDetail.setStatus(YjtAppRepayDetailDto.STATUS_ALREADY_REPAID);
                    repaySchedule.setTotalRepaiedAmt(repaySchedule.getTotalRepaiedAmt() + (detail.getPrincipal() + detail.getInterest() + detail.getBonusInterest()+detail.getBonusReward())/100.0);
                    repaidPrincipal += ArithUtil.round(detail.getPrincipal() / 100.0, 2);
                    repaidInterest += ArithUtil.round((detail.getInterest() + (detail.getBonusInterest() != null ? detail.getBonusInterest() : 0)+(detail.getBonusReward() != null ? detail.getBonusReward() : 0)) / 100.0, 2);
                }
                if (schedule.getStatus() >= 4) {
                    repayDetail.setBeforeRepayFlag(YjtAppRepayDetailDto.BEFORE_REPAY_FLAG_Y);
                    repaySchedule.setBeforeRepayFlag(YjtAppRepayDetailDto.BEFORE_REPAY_FLAG_Y);
                }
                repayDetails.add(repayDetail);
//                repaySchedule.setTotalRepaiedAmt(repaySchedule.getTotalRepaiedAmt() + (detail.getPrincipal() + detail.getInterest() + detail.getBonusInterest()+detail.getBonusReward())/100.0);

            }
            //未还的
            List<SubjectRepaySchedule> notRepaySchedules = subjectRepayScheduleService.findSubjectRepayScheduleBySubjectIdAndTermNotRepay(subjectId, iplanTerm);
            Integer principal = credit.getHoldingPrincipal();
            if(org.apache.commons.collections.CollectionUtils.isNotEmpty(notRepaySchedules)){
                Integer totalNotRepayPrincipal=notRepaySchedules.stream().map(SubjectRepaySchedule::getDuePrincipal).reduce(Integer::sum).orElse(0);
                for (SubjectRepaySchedule schedule : notRepaySchedules) {
                    if(principal>0) {
                        SubjectRepayDetail detail = repayScheduleService.repayDetailBySchedule(schedule, userId, credit, principal, totalNotRepayPrincipal);
                        if (detail == null) {
                            continue;
                        }

                        principal -= detail.getPrincipal();
                        totalNotRepayPrincipal -= schedule.getDuePrincipal();

                        int termIndex = schedule.getTerm() - 1;
                        // 债权打包省心投
                        if (iPlan.getPackagingType() == 1) {
                            termIndex = iPlanRepayScheduleService.getCurrentTermByIplanId(iPlan.getId(), schedule.getDueDate()) - 1;
                        }

                        YjtAppRepayDetailDto.RepaySchedule repaySchedule = repaySchedules.get(termIndex);
                        Map<String, Object> repayDetailMap = detailMapTemp.get(termIndex + 1);
                        List<YjtAppRepayDetailDto.RepayDetail> repayDetails = (List<YjtAppRepayDetailDto.RepayDetail>) repayDetailMap.get(schedule.getDueDate().substring(0, 4) + "-" + schedule.getDueDate().substring(4, 6) + "-" + schedule.getDueDate().substring(6, 8));
                        if (repayDetails == null) {
                            repayDetails = new ArrayList<>();
                            repayDetailMap.put(schedule.getDueDate().substring(0, 4) + "-" + schedule.getDueDate().substring(4, 6) + "-" + schedule.getDueDate().substring(6, 8), repayDetails);
                        }
                        YjtAppRepayDetailDto.RepayDetail repayDetail = new YjtAppRepayDetailDto.RepayDetail();
                        repayDetail.setName(subject.getName());
                        repayDetail.setPrincipal(df4.format(detail.getPrincipal() / 100.0));
                        repayDetail.setInterest(df4.format(detail.getInterest() / 100.0));
                        double bonusInterest = (detail.getBonusInterest() != null ? detail.getBonusInterest() : 0) / 100.0;
                        double bonusReward = (detail.getBonusReward() != null ? detail.getBonusReward() : 0) / 100.0;
                        repayDetail.setBonusInterest(df4.format(bonusInterest + bonusReward));
                        if (bonusInterest+bonusReward > 0) {
                            repaySchedule.setBonusFlag(YjtAppRepayDetailDto.BONUS_FLAG_Y);
                        }
                        repayDetail.setStatus(YjtAppRepayDetailDto.STATUS_NOT_REPAY);
                        repayDetails.add(repayDetail);
                        repaySchedule.setTotalNotRepayAmt(repaySchedule.getTotalNotRepayAmt() + (detail.getPrincipal() + detail.getInterest() + detail.getBonusInterest() + detail.getBonusReward()) / 100.0);

                        notRepayPrincipal += ArithUtil.round(detail.getPrincipal() / 100.0, 2);
                        notRepayInterest += ArithUtil.round((detail.getInterest() + (detail.getBonusInterest() != null ? detail.getBonusInterest() : 0) + (detail.getBonusReward() != null ? detail.getBonusReward() : 0)) / 100.0, 2);
                    }
                }
            }
        }
        Iterator<YjtAppRepayDetailDto.RepaySchedule> iterator = repaySchedules.iterator();
        while (iterator.hasNext()) {
            YjtAppRepayDetailDto.RepaySchedule repaySchedule = iterator.next();
            if (repaySchedule.getTerm() >= 1) {
                double totalRepaiedAmt = repaySchedule.getTotalRepaiedAmt();
                double totalNotRepayAmt = repaySchedule.getTotalNotRepayAmt();
                if (totalNotRepayAmt <= 0 && totalRepaiedAmt <= 0) {
                    iterator.remove();
                    continue;
                }
                String status = YjtAppRepayDetailDto.STATUS_NOT_REPAY;
                if (totalRepaiedAmt <= 0 && totalNotRepayAmt > 0) {
                    status = YjtAppRepayDetailDto.STATUS_NOT_REPAY;
                } else if (totalRepaiedAmt > 0 && totalNotRepayAmt > 0) {
                    status = YjtAppRepayDetailDto.STATUS_REPAING;
                } else if (totalRepaiedAmt > 0 && totalNotRepayAmt <= 0) {
                    status = YjtAppRepayDetailDto.STATUS_ALREADY_REPAID;
                }
                String content = "已回本息" + df4.format(totalRepaiedAmt) + "元 待回本息" + df4.format(totalNotRepayAmt) + "元";
                repaySchedule.setStatus(status);
                repaySchedule.setContent(content);

                Map<String, Object> repayDetailMap = detailMapTemp.get(repaySchedule.getTerm());
                List<Map<String, Object>> repayDetailLists = new ArrayList<>();
                List<String> dateList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : repayDetailMap.entrySet()) {
                    String date = entry.getKey();
                    dateList.add(date);
                    List<YjtAppRepayDetailDto.RepayDetail> repayDetails = (List<YjtAppRepayDetailDto.RepayDetail>)entry.getValue();
                    Map<String, Object> map = new HashMap<>(2);
                    map.put("date", date);
                    map.put("repayDetails", repayDetails);
                    repayDetailLists.add(map);
                }

                Collections.sort(dateList);

                String firstDate = dateList.get(0);
                String lastDate = dateList.get(dateList.size()-1);
                String firstAndLastDateRepaySchedule = dateFormate(firstDate) + "-" + dateFormate(lastDate);
                String firstAndLastDateTitle = dateToCNFormate(firstDate) + "-" + dateToCNFormate(lastDate);
                if (org.apache.commons.lang3.StringUtils.equals(firstDate, lastDate)) {
                    firstAndLastDateRepaySchedule = dateFormate(firstDate);
                    firstAndLastDateTitle = dateToCNFormate(firstDate);
                }

                repaySchedule.setDate(firstAndLastDateRepaySchedule);
                repaySchedule.getRepayScheduleDetail().setTitle(repaySchedule.getRepayScheduleDetail().getTitle() +"（"+ firstAndLastDateTitle+ "）");

                Collections.sort(repayDetailLists, Comparator.comparing(o -> (String)o.get("date")));

                repaySchedule.getRepayScheduleDetail().setRepayDetails(repayDetailLists);
            }
        }

        // 红包发送记录
        InvestRedpacket investRedpacket = redPacketService.getReceivedRedPacketAmt(userId, String.valueOf(iplanId), RedPacket.INVEST_REDPACKET_TYPE);
        if (investRedpacket != null && investRedpacket.getRewardMoney() != 0 && investRedpacket.getSendRedpacketTime() != null) {
            YjtAppRepayDetailDto.RepaySchedule repaySchedule = new YjtAppRepayDetailDto.RepaySchedule();
            repaySchedule.setTitle("红包奖励");
            repaySchedule.setDate(DateUtil.SDF_10.format(investRedpacket.getSendRedpacketTime()));
            repaySchedule.setContent("红包奖励" + df4.format(investRedpacket.getRewardMoney())+"元");
            repaySchedule.setStatus(YjtAppRepayDetailDto.STATUS_ALREADY_REPAID);
            repaySchedule.setRedPacketFlag(YjtAppRepayDetailDto.RED_PACKET_FLAG_Y);
            repaySchedules.add(0, repaySchedule);
        }

        String interestStartTime = "生成中";
        if (iPlan != null && iPlan.getRaiseCloseTime() != null) {
            interestStartTime = iPlan.getRaiseCloseTime().substring(0, 10);
            try {
                interestStartTime = sdf2.format(DateUtils.addDays(sdf1.parse(iPlan.getRaiseFinishTime()), 1));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        yjtAppRepayDetailDto.setInterestStartTime(interestStartTime);
        yjtAppRepayDetailDto.setRepayType(SubjectMobileResource.repayType(iPlan.getRepayType()));
        yjtAppRepayDetailDto.setRepaySchedules(repaySchedules);
        yjtAppRepayDetailDto.setRepaidPrincipal(repaidPrincipal);
        yjtAppRepayDetailDto.setRepaidInterest(repaidInterest);
        yjtAppRepayDetailDto.setNotRepayPrincipal(notRepayPrincipal);
        yjtAppRepayDetailDto.setNotRepayInterest(notRepayInterest);
        return new RestResponseBuilder<>().success(yjtAppRepayDetailDto);
    }

    private String dateFormate(String date) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(date)) {
            if (date.contains("-")) {
                return date.replaceAll("-", ".");
            }
            return date;
        }
        return date;
    }

    private String dateToCNFormate(String date) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(date)) {
            //2017-02-12
            if (date.length() == 10) {
                return date.substring(5,7)+"月"+date.substring(8,10) + "日";
            }
            return date;
        }
        return date;
    }

    /**
     * 还款类型
     * @param repayType
     * @return
     */
    public static String repayType(String repayType){
        if(GlobalConfig.MCEI.equals(repayType)){
           return  "等额本息";
        }else if(GlobalConfig.IFPA.equals(repayType)){
            return "先息后本";
        }else if(GlobalConfig.OTRP.equals(repayType)){
           return "一次性到期还本付息";
        }else{
            return "";
        }
    }

    /**
     * app还款日历
     * @param userId
     * @param time  当前日期
     * @return
     */
    @GetMapping("/subject/repay/record/calendar")
    public RestResponse<List<SubjectRepayDetailDto>> getAppSubjectRepayRL(@RequestParam("userId") String userId,
                                                                        @RequestParam(value = "time",required = false) String time
                                                                        ) {
        List<SubjectRepayDetailDto> repayDetailDtoList = new ArrayList<>();

        if(time==null){
            time = DateUtil.getCurrentDateShort();
        }else{
            time = time.replace("-","");
        }
        final String date = time.substring(0,6);
        List<Credit> creditList = creditService.findByUserIdAndChannelsInAndStatusForRepay(userId,new HashSet<>(Arrays.asList(Credit.SOURCE_CHANNEL_SUBJECT,Credit.SOURCE_CHANNEL_YJT)),Credit.CREDIT_STATUS_HOLDING);
        //根据标的subjectId分组
        Map<String, List<Credit>> maps = creditList.stream().collect(Collectors.groupingBy(Credit :: getSubjectId));
        for (Map.Entry<String, List<Credit>> entry:maps.entrySet()) {
            String subjectId = entry.getKey();
            Subject subject = subjectService.getBySubjectId(subjectId);
            List<Credit> accountList = entry.getValue();
            //根据subjectId查询未还还款计划
            List<SubjectRepaySchedule> schedules = repayScheduleService.getSubjectRepayScheduleBySubjectIdNotRepay(subjectId);
            //过滤出还款日是当月的
            Integer totalPrincipal = 0;
            if(!schedules.isEmpty() && schedules.stream().anyMatch(schedule->schedule.getDueDate().substring(0,6).equals(date))){
                for (Credit credit:accountList) {
                    Integer principal = credit.getHoldingPrincipal();
                    totalPrincipal = schedules.stream().map(SubjectRepaySchedule::getDuePrincipal).reduce(Integer::sum).orElse(0);
                    IPlan iplan = null;
                    for (SubjectRepaySchedule schedule:schedules) {
                        if(principal>0){
                            //若大于当月的既不进入计算
                            if(schedule.getDueDate().substring(0,6).compareTo(date)>0){
                                break;
                            }
                            SubjectRepayDetail repayDetail =  repayScheduleService.repayDetailBySchedule(schedule,userId,credit,principal,totalPrincipal);
                            if(repayDetail == null){
                                break;
                            }
                            if(schedule.getDueDate().substring(0,6).equals(date)){
                                Integer term = subject.getTerm();
                                String name = subject.getName();
                                Integer currTerm = repayDetail.getTerm();
                                SubjectRepayDetailDto dto = new SubjectRepayDetailDto();
                                dto.setInterest(repayDetail.getInterest()/100.0);
                                dto.setPrincipal(repayDetail.getPrincipal()/100.0);
                                dto.setBonusInterest(repayDetail.getBonusInterest()/100.0);
                                dto.setBonusReward(repayDetail.getBonusReward()/100.0);
                                dto.setRepayDate(schedule.getDueDate().substring(0,4)+"-"+schedule.getDueDate().substring(4,6)+"-"+schedule.getDueDate().substring(6,8));
                                dto.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
                                dto.setSubjectId(repayDetail.getSubjectId());
                                if(Credit.SOURCE_CHANNEL_YJT == credit.getSourceChannel()) {
                                    iplan = iPlanService.getIPlanById(iPlanAccountService.findById(credit.getSourceAccountId()).getIplanId());
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
                                dto.setName(name);
                                //未还
                                dto.setStatus(0);
                                dto.setRepayTerm(currTerm+"/"+term);
                                repayDetailDtoList.add(dto);
                            }/*else if(schedule.getDueDate().substring(0,6).compareTo(date)>0){
                                //若当前期回款日大于用户查询月份,直接结束当前循环
                                break;
                            }*/
                            principal -= repayDetail.getPrincipal();
                        }
                        totalPrincipal -= schedule.getDuePrincipal();
                    }
                }
            }
        }

        //查询当月已还的明细
        List<SubjectRepayDetail> details = subjectRepayDetailDao.findDetailByTime(userId,date);
        for (SubjectRepayDetail detail:details) {
            Subject subject = subjectService.findSubjectBySubjectId(detail.getSubjectId());
            SubjectRepaySchedule schedule = repayScheduleService.findById(detail.getScheduleId());
            Integer currTerm = schedule.getTerm();
            Integer term = subject.getTerm();
            String name = subject.getName();
            SubjectRepayDetailDto dto = new SubjectRepayDetailDto();
            dto.setInterest(detail.getInterest()/100.0);
            dto.setPrincipal(detail.getPrincipal()/100.0);
            if(detail.getBonusInterest()!=null){
                dto.setBonusInterest(detail.getBonusInterest()/100.0);
            }else{
                dto.setBonusInterest(0.0);
            }
            if(detail.getBonusReward()!=null){
                dto.setBonusReward(detail.getBonusReward()/100.0);
            }else{
                dto.setBonusReward(0.0);
            }
            dto.setRepayDate(detail.getCreateTime().substring(0,10));
            dto.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
            dto.setSubjectId(detail.getSubjectId());
            if(Credit.SOURCE_CHANNEL_YJT == detail.getChannel()){
                IPlan iplan = iPlanService.getIPlanById(iPlanAccountService.findById(detail.getSourceAccountId()).getIplanId());
                if(iplan!=null) {
                    if(IPlan.PACKAGING_TYPE_CREDIT.equals(iplan.getPackagingType())) {
                        currTerm = iPlanRepayScheduleService.getCurrentTermByIplanId(iplan.getId(), schedule.getRepayDate());
                    }
                    name = iplan.getName();
                    term = iplan.getTerm();
                    dto.setSecondName(subject.getName());
                }
            }
            dto.setCurrentTerm(currTerm);
            dto.setTerm(term);
            dto.setName(name);
            if(detail.getStatus() == 0 || (detail.getStatus()==1 && detail.getCurrentStep()!=3)){
                dto.setStatus(0);
            }else{
                //已还
                dto.setStatus(1);
            }
            dto.setRepayTerm(currTerm+"/"+term);
            repayDetailDtoList.add(dto);
        }
        return new RestResponseBuilder<List<SubjectRepayDetailDto>>().success(repayDetailDtoList);
    }

    @GetMapping("/app/subject/manage/creditTransferDetail")
    public RestResponse getAppCreditTransferDetailDto(@RequestParam("id") int creditOpeningId,
                                                      @RequestParam(value = "userId") String userId) {

        CreditOpening creditOpening = creditOpeningService.getById(creditOpeningId);
        if (creditOpening == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        AppCreditTransferDetailDto appCreditTransferDetailDto = new AppCreditTransferDetailDto();
        appCreditTransferDetailDto.setId(creditOpening.getId());
        Subject subject = subjectService.getBySubjectId(creditOpening.getSubjectId());
        appCreditTransferDetailDto.setName(subject.getName());
        //还款方式
        appCreditTransferDetailDto.setRepayType(subjectService.getRepayType(creditOpening.getSubjectId()));
        //出售金额
        appCreditTransferDetailDto.setSaleAmt(creditOpening.getTransferPrincipal() / 100.0);
        appCreditTransferDetailDto.setSaleAmtStr(df4.format(appCreditTransferDetailDto.getSaleAmt()));

        //折让率
        appCreditTransferDetailDto.setTransDiscount(creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue());
        appCreditTransferDetailDto.setTransDiscountStr(df4.format(appCreditTransferDetailDto.getTransDiscount()));

        //投资金额
        Credit credit = creditService.getById(creditOpening.getCreditId());
        appCreditTransferDetailDto.setInvestAmt(credit.getInitPrincipal() / 100.0);
        appCreditTransferDetailDto.setInvestAmtStr(df4.format(appCreditTransferDetailDto.getInvestAmt()));

        //投资日期
        appCreditTransferDetailDto.setInvestTime(DateUtil.parseDate(credit.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
        appCreditTransferDetailDto.setEndTime(DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString());

        //持有天数
        String startDate = credit.getCreateTime().substring(0, 10).replace("-","");
        String currentDate = DateUtil.getCurrentDate().substring(0, 10).replace("-","");
        long days = DateUtil.betweenDays(startDate,currentDate);
        appCreditTransferDetailDto.setHoldDay((int) days);

        //已到账收益
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(credit.getSourceAccountId());
        appCreditTransferDetailDto.setReceivedAmt(subjectAccount.getPaidInterest() / 100.0);
        appCreditTransferDetailDto.setReceivedAmtStr(df4.format(appCreditTransferDetailDto.getReceivedAmt()));

        //转让时间
        appCreditTransferDetailDto.setTransferTime(creditOpening.getCreateTime().substring(0,10));

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamService.getByTransferParamCode(subject.getTransferParamCode());

        //转让服务费
        SubjectTransLog subjectTransLog = subjectTransLogService.getById(creditOpening.getSourceChannelId());
        if(subjectTransLog.getTransFee() == 0){
            Double feeRate = subjectAccountService.calcTransFeeNew(subjectAccount.getTransLogId(), subject, subjectTransferParam);
            appCreditTransferDetailDto.setFee((creditOpening.getTransferPrincipal()/100.0) * feeRate / 100.0);
        }else{
            appCreditTransferDetailDto.setFee(0.0);
        }
        appCreditTransferDetailDto.setFeeStr(df4.format(appCreditTransferDetailDto.getFee()));
        //扣除红包奖励
        Double redFee = subjectAccountService.calcRedFee(subjectAccount, credit);
        appCreditTransferDetailDto.setRedFee((creditOpening.getTransferPrincipal() /100.0)   * redFee);
        appCreditTransferDetailDto.setRedFeeStr(df4.format(appCreditTransferDetailDto.getRedFee() ));

        //溢价手续费
        Double overFee = 0.0;
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if(transferDiscount > 100){
            overFee = (creditOpening.getTransferPrincipal() /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        }
        appCreditTransferDetailDto.setOverFee(overFee);
        appCreditTransferDetailDto.setOverFeeStr(df4.format(overFee));

        //预计到账金额
        Double expectAmt = ArithUtil.calcExp((creditOpening.getTransferPrincipal() /100.0) * (transferDiscount / 100.0) ,appCreditTransferDetailDto.getRedFee() , appCreditTransferDetailDto.getOverFee() , appCreditTransferDetailDto.getFee());
        appCreditTransferDetailDto.setExpectAmt(expectAmt);
        appCreditTransferDetailDto.setExpectAmtStr(df4.format(appCreditTransferDetailDto.getExpectAmt()));


        //是否可撤销
        Integer status = 1;
        if(creditOpening.getAvailablePrincipal() == 0 || CreditOpening.STATUS_CANCEL_PENDING.equals(creditOpening.getStatus())){
            status = 0;
        }
        appCreditTransferDetailDto.setStatus(status);

        appCreditTransferDetailDto.setCreditTransferUrl(transferUrl);
        appCreditTransferDetailDto.setCreditDetailUrl(detailUrl+"?subjectId="+subject.getSubjectId());

        return new RestResponseBuilder<>().success(appCreditTransferDetailDto);
    }


    /**
     * 散标管理-APP持有中详情
     * @param subjectAccountId
     * @param userId
     * @param type
     * @return
     */
    @GetMapping("/app/subject/manage/subjectDetail")
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
            appCreditTransferFinishDetailDto.setCreditTransferUrl(transferUrl);
            String contractId = credit.getContractId();
            if(org.apache.commons.lang3.StringUtils.isNotBlank(contractId)){
                appCreditTransferFinishDetailDto.setCreditTransferUrl(creditService.getContractViewPdfUrlByContractId(contractId));
            }
            appCreditTransferFinishDetailDto.setPageType(AppCreditFinishDetailDto.PAGE_TYPE_TRANSFERRFINISH);
            return new RestResponseBuilder<>().success(appCreditTransferFinishDetailDto);
        }
        SubjectAccount subjectAccount = subjectAccountService.findAccountById(subjectAccountId);
        if (subjectAccount == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        SubjectAppDetailDto subjectDetailDto = new SubjectAppDetailDto();
        Subject subject = subjectService.getBySubjectId(subjectAccount.getSubjectId());
        Credit credit = creditService.getBySubjectAccountId(subjectAccount.getId());
        SubjectTransLog subjectTransLog = subjectTransLogService.getTransLogByAccountId(subjectAccountId);
        subjectDetailDto.setId(subjectAccountId);
        subjectDetailDto.setName(subject.getName());
        //标的ID
        subjectDetailDto.setSubjectId(subject.getSubjectId());
        subjectDetailDto.setTotalAmt(subject.getTotalAmt());
        subjectDetailDto.setAvailableAmt(subject.getAvailableAmt());
        subjectDetailDto.setSubjectRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
        if(subject.getPeriod()<GlobalConfig.ONEMONTH_DAYS){
            subjectDetailDto.setTermStr(subject.getPeriod()+"天");
            subjectDetailDto.setSubjectType("天标");
        }else{
            subjectDetailDto.setTermStr(subject.getTerm()+"个月");
            subjectDetailDto.setSubjectType("月标");
        }

        subjectDetailDto.setRaiseStatus(String.valueOf(subject.getRaiseStatus()));
        //标的利率
        BigDecimal totalRate = creditOpeningService.calcTotalRate(subject.getSubjectId());
        ActivityMarkConfigure activityMarkConfigure = null;
        if (subject.getActivityId() != null && subject.getActivityId() > 0) {
            activityMarkConfigure = activityMarkConfigureService.findById(subject.getActivityId());
            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                totalRate = subject.getInvestRate();
            }
        }
        subjectDetailDto.setRateStr(df4.format(totalRate.doubleValue() * 100)+"%");
        String endTime = "生成中";
        //投资金额
        subjectDetailDto.setHoldingPrincipal(subjectAccount.getCurrentPrincipal()/ 100.0);
        subjectDetailDto.setHoldingPrincipalStr(df4.format(subjectAccount.getCurrentPrincipal()/ 100.0));
        Integer principal =0;
        Integer interest=0;
        subjectDetailDto.setBuyTime(subjectTransLog.getCreateTime());
        if(credit != null){
            endTime = credit.getEndTime() != null ? DateUtil.parseDate(credit.getEndTime(), DateUtil.DATE_TIME_FORMATTER_17).toString() : "生成中";
            subjectDetailDto.setBuyTime(credit.getCreateTime());
            //投资金额
            subjectDetailDto.setHoldingPrincipal(credit.getInitPrincipal() / 100.0);
            subjectDetailDto.setHoldingPrincipalStr(df4.format(credit.getInitPrincipal() / 100.0));
            //已到账本金
            principal = subjectRepayDetailService.getPrincipal(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
            //已到账利息
            interest = subjectRepayDetailService.getInterest(credit.getSubjectId(), credit.getUserId(), credit.getSourceAccountId());
        }
        //已到账本金
        subjectDetailDto.setPrincipal(String.valueOf(principal / 100.0));
        //已到账利息
        subjectDetailDto.setInterest(String.valueOf(interest/100.0));
        subjectDetailDto.setEndTime(endTime);
        subjectDetailDto.setProtocolUrl(protocolUrl);
        subjectDetailDto.setCreditUrl(detailUrl+"?subjectId="+subjectAccount.getSubjectId());
        //是否可转让
        subjectDetailDto.setStatus(subjectService.checkCondition(subject,subjectAccount));
        subjectDetailDto.setMessage(subjectService.checkConditionStr(subject,subjectAccount));
        //红包信息
        RedPacket redPacket =  null;
        if (subjectTransLog.getRedPacketId() > 0){
            redPacket = redPacketService.getRedPacketById(subjectTransLog.getRedPacketId());
        }
        String redPacketMsg = "";
        if (redPacket != null) {
            if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                redPacketMsg = df4.format(redPacket.getMoney())
                        + "元现金券";
            } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                redPacketMsg = df4.format(redPacket.getRate() * 100)
                        + "%加息券";
            } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                redPacketMsg = df4.format(redPacket.getMoney())
                        + "元抵扣券";
            } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                redPacketMsg = df4.format(redPacket.getRate() * 100)
                        + "%"+redPacket.getRateDay()+"天加息券";
            }
        }
        subjectDetailDto.setRedPacketMsg(redPacketMsg);
        subjectDetailDto.setRedPacket(redPacket);
        if(credit != null){
            String contractId = credit.getContractId();
            if(org.apache.commons.lang3.StringUtils.isNotBlank(contractId)){
                subjectDetailDto.setProtocolUrl(creditService.getContractViewPdfUrlByContractId(contractId));
            }
        }

        //判断是完成详情还是持有中详情
        if (type == SubjectDetailDto.TYPE_HOLDING) {
            Integer currentPrincipal =subjectAccount.getCurrentPrincipal();//当前计息本金
            Integer expectedInterest=subjectAccount.getExpectedInterest();//预期利息
            Integer expectedBonusInterest=subjectAccount.getSubjectExpectedBonusInterest();//预期加息利息
            Integer totalAmt=currentPrincipal+expectedInterest+expectedBonusInterest;
            subjectDetailDto.setPrincipalAndInterest(String.valueOf(totalAmt/100.0));//未还本息
        }else if (type == SubjectDetailDto.TYPE_FINISH) {
            subjectDetailDto.setPrincipalAndInterest(String.valueOf(0));//未还本息
            subjectDetailDto.setPaidTotalAmt(String.valueOf((principal + interest) / 100.0));//已还款本息
        }
        return new RestResponseBuilder<SubjectAppDetailDto>().success(subjectDetailDto);
    }
}
