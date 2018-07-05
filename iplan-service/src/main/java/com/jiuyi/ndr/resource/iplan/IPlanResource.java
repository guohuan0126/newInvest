package com.jiuyi.ndr.resource.iplan;

import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.*;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.redpacket.InvestRedpacket;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.dto.iplan.*;
import com.jiuyi.ndr.dto.iplan.mobile.*;
import com.jiuyi.ndr.dto.subject.SubjectYjtDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.resource.iplan.mobile.IPlanMobileResource;
import com.jiuyi.ndr.resource.subject.mobile.SubjectMobileResource;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.customannotation.AutoLogger;
import com.jiuyi.ndr.rest.page.IPlanPcListPageData;
import com.jiuyi.ndr.rest.page.PageData;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.credit.CreditService;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.iplan.*;
import com.jiuyi.ndr.service.marketing.MarketService;
import com.jiuyi.ndr.service.subject.SubjectService;
import com.jiuyi.ndr.service.user.ActivityMarkConfigureService;
import com.jiuyi.ndr.service.user.RedPacketService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.util.ArithUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.FinanceCalcUtils;
import com.jiuyi.ndr.util.PageUtil;
import com.jiuyi.ndr.util.redis.RedisClient;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lixiaolei 2017/6/8
 */
@RestController
public class IPlanResource {

    private final static Logger logger = LoggerFactory.getLogger(IPlanResource.class);

    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private IPlanRepayDetailService iPlanRepayDetailService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private IPlanParamService iPlanParamService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private InvestService investService;
    @Autowired
    private CreditService creditService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;
    @Autowired
    private MarketService marketService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private UserService userService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private ConfigService configService;
    
    private DecimalFormat df = new DecimalFormat("######0.####");
    private DecimalFormat df2 = new DecimalFormat("######0.##");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    //首页精选
    @GetMapping("/iplan/featured")
    public RestResponse<List> getFeaturedIPlan(@RequestParam(value = "userId", required = false) String userId, @RequestParam(required = false, defaultValue = "0") int iplanType) {
        List<IPlan> iPlans = iPlanService.getFeaturedIPlansTemporary(userId, iplanType);
        List<IPlanDto> iPlanDtos = new ArrayList<>(iPlans.size());
        // 获取用户当前VIP加息利率
        double vipRate = marketService.getIplanVipRate(userId);
        for (IPlan iPlan : iPlans) {
            IPlanDto iPlanDto = new IPlanDto();
            BeanUtils.copyProperties(iPlan, iPlanDto);
            iPlanDto.setVipRate(vipRate);
            iPlanDtos.add(iPlanDto);
        }
        return new RestResponseBuilder<List>().success(iPlanDtos);
    }

    //首页精选
    @GetMapping("/yjt/featured")
    @AutoLogger
    public RestResponse<List> getFeaturedYjt(@RequestParam(value = "userId", required = false) String userId) {
        List<IPlan> iPlans = iPlanService.getFeaturedYjtTemporary(userId);
        List<IPlanDto> iPlanDtos = new ArrayList<>(iPlans.size());
        // 获取用户当前VIP加息利率
        double vipRate = marketService.getIplanVipRate(userId);
        for (IPlan iPlan : iPlans) {
            IPlanDto iPlanDto = new IPlanDto();
            BeanUtils.copyProperties(iPlan, iPlanDto);
            iPlanDto.setVipRate(vipRate);
            iPlanDtos.add(iPlanDto);
        }
        return new RestResponseBuilder<List>().success(iPlanDtos);
    }

    //投资达人
    @GetMapping("/iplan/{iPlanId}/talent")
    public RestResponse<List> getIPlanTalent(@PathVariable("iPlanId") Integer iPlanId) {
        //IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        List<IPlanTalentDto> iPlanTalentDtos = null;
        /*if (IPlan.STATUS_RAISING_FINISH.equals(iPlan.getStatus())||IPlan.STATUS_EARNING.equals(iPlan.getStatus())||IPlan.STATUS_END.equals(iPlan.getStatus())){
            try {
                iPlanTalentDtos = redisClient.getStringToList(GlobalConfig.IPLAN_TALENT+iPlanId,IPlanTalentDto.class);
                System.out.println("redis获取对象");
                if (iPlanTalentDtos==null){
                    iPlanTalentDtos = iPlanService.getInvestorAcct(iPlanId);
                    String iPlanTransLog = JSON.toJSONString(iPlanTalentDtos);
                    System.out.println("iPlanTransLog:"+iPlanTransLog);
                    redisClient.set(GlobalConfig.IPLAN_TALENT+iPlanId,iPlanTransLog);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {*/
            iPlanTalentDtos = iPlanService.getInvestorAcct(iPlanId);
        //}
        return new RestResponseBuilder<List>().success(iPlanTalentDtos);
    }

    @GetMapping("{userId}/repaySchedule")
    public RestResponse<PageData> getPersonalRepaySchedule( @PathVariable("userId") String userId,
                                                            @RequestParam(value = "iPlanId", required = false) Integer iPlanId,
                                                            @RequestParam("pageNo") Integer pageNo,
                                                            @RequestParam("pageSize") Integer pageSize) {
        List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailService.getByPageHelper(userId, iPlanId, pageNo, pageSize);
        List<IPlanRepayDetailDto> iPlanRepayDetailDtos = new ArrayList<>(iPlanRepayDetails.size());
        for (IPlanRepayDetail repayDetail : iPlanRepayDetails) {
            IPlanRepayDetailDto iPlanRepayDetailDto = new IPlanRepayDetailDto();
            BeanUtils.copyProperties(repayDetail, iPlanRepayDetailDto);
            IPlan iPlan = iPlanService.getIPlanById(repayDetail.getIplanId());
            iPlanRepayDetailDto.setiPlanName(iPlan.getName());
            iPlanRepayDetailDto.setTotalTerm(iPlan.getTerm());
            InvestRedpacket investRedpacket = redPacketService.getReceivedRedPacketAmt(userId, String.valueOf(iPlanId), RedPacket.INVEST_REDPACKET_TYPE);
            if (investRedpacket != null && investRedpacket.getRewardMoney() != 0 && investRedpacket.getSendRedpacketTime() != null) {
                iPlanRepayDetailDto.setRedPacketDesc("红包券收益:" + ArithUtil.round(investRedpacket.getRewardMoney(), 2) + "元");
                iPlanRepayDetailDto.setRedPacketDate(DateUtil.SDF_10.format(investRedpacket.getSendRedpacketTime()));
            }
            iPlanRepayDetailDtos.add(iPlanRepayDetailDto);
        }
        PageData<IPlanRepayDetailDto> pageData = new PageData<>();
        PageInfo<IPlanRepayDetail> pageInfo = new PageInfo<>(iPlanRepayDetails);
        pageData.setList(iPlanRepayDetailDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<PageData>().success(pageData);
    }

    /*@GetMapping("{userId}/{iPlanId}/repaySchedule")
    public RestResponse getPlanRePaySchedule(@PathVariable("userId") String userId, @PathVariable("iPlanId") Integer iPlanId, @RequestParam("pageNo") Integer pageNo, @RequestParam("pageSize") Integer pageSize) {
        List<IPlanRepayScheduleDto> iPlanRepayScheduleDtos = new ArrayList<>();
        List<IPlanRepaySchedule> iPlanRepaySchedules = iPlanRepayScheduleService.getRepayScheduleByPageHelper(iPlanId, pageNo, pageSize);
        IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        IPlanAccount iPlanAccount = iPlanAccountService.getIPlanAccount(userId, iPlanId);
        for (IPlanRepaySchedule repaySchedule : iPlanRepaySchedules) {
            IPlanRepayScheduleDto repayScheduleDto = new IPlanRepayScheduleDto();
            BeanUtils.copyProperties(repaySchedule, repayScheduleDto);
            Integer personalInterest = repaySchedule.getDueInterest() * iPlanAccount.getCurrentPrincipal() / (iPlan.getQuota() - iPlan.getAvailableQuota());
            repayScheduleDto.setDuePrincipal(0);
            repayScheduleDto.setDueInterest(personalInterest);
            if (repaySchedule.getTerm().equals(iPlanRepaySchedules.size())) {
                Integer personalPrincipal = repaySchedule.getDuePrincipal() * iPlanAccount.getCurrentPrincipal() / (iPlan.getQuota() - iPlan.getAvailableQuota());
                repayScheduleDto.setDuePrincipal(personalPrincipal);
            }
            iPlanRepayScheduleDtos.add(repayScheduleDto);
        }
        PageData<IPlanRepayScheduleDto> pageData = new PageData<>();
        PageInfo<IPlanRepaySchedule> pageInfo = new PageInfo<>(iPlanRepaySchedules);
        pageData.setList(iPlanRepayScheduleDtos);
        pageData.setPage(pageInfo.getPageNum());
        pageData.setSize(pageInfo.getSize());
        pageData.setTotalPages(pageInfo.getPages());
        pageData.setTotal(pageInfo.getTotal());
        return new RestResponseBuilder<>().success(pageData);
    }*/

    //创建理财计划
    @PutMapping("/iplan")
    public RestResponse<IPlanDto> createIPlan(@RequestBody IPlanDto iPlanDto) {
        IPlan iPlan = new IPlan();
        BeanUtils.copyProperties(iPlanDto, iPlan);
        iPlan = iPlanService.insert(iPlan);
        if (IPlanDto.REDIS_TRUE.equals(iPlanDto.getIsRedis())){
            redisClient.product(GlobalConfig.DOUBLE_11_IPLAN,String.valueOf(iPlan.getId()));
            try {
                redisClient.set(GlobalConfig.IPLANID_TO_CODE+iPlan.getCode(),String.valueOf(iPlan.getId()));
            }catch (Exception e){

            }
        }
        iPlanDto.setId(iPlan.getId());
        return new RestResponseBuilder<IPlanDto>().success(iPlanDto);
    }

    //推送理财计划
    @PutMapping("/iplan/push")
    public RestResponse<IPlanDto> pushIPlan(@RequestBody IPlanDto iPlanDto) {
        IPlan iPlan = new IPlan();
        BeanUtils.copyProperties(iPlanDto, iPlan);
        iPlanService.pushIPlan(iPlan);
        return new RestResponseBuilder<IPlanDto>().success(iPlanDto);
    }

    //pc端-理财计划列表
    @GetMapping("/pc/iplan/list")
    public RestResponse getIPlan(   @RequestParam("pageNo") int pageNum,
                                    @RequestParam("pageSize") int pageSize,
                                    @RequestParam(value = "userId", required = false) String userId,
                                    @RequestParam(value = "iplanType", defaultValue = "0", required = false) int iplanType) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        //查询此人新手额度
       double avalNewbieAmt =investService.getNewbieUsable(userId, iplanType);
       List<IPlan> allVisiblePlan=null;
       if (!StringUtils.isEmpty(userId)) {
    	   if(avalNewbieAmt>0){//有额度
    		   allVisiblePlan = iPlanService.findPlanNewBie(iplanType);
    	   }else{//没有额度
    		   allVisiblePlan=iPlanService.findPlanNoAnyNewBies(iplanType);
    	   }
    	   
       }else{
    	   allVisiblePlan = iPlanService.findPlanNewBie(iplanType);
       }

        // vip加息利率
        double vipRate = marketService.getIplanVipRate(userId);

        //根据用户注册来源过滤渠道标
        /*allVisiblePlan = iPlanService.filterIplanByUserSource(allVisiblePlan, userId);*/

        List<IPlanListDto> iPlanList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(allVisiblePlan)) {
            for (IPlan iPlan : allVisiblePlan) {
                /*//期限为1个月的标的
                if (iPlan.getTerm().equals(1)) {
                    //用户是否为渠道用户
                    if (registerFlag) {
                        //渠道用户只展示渠道标的，非渠道标的过滤
                        if (!iPlan.getChannelName().equals(IPlan.CHANNEL_NAME_FANLI) || iPlan.getNewbieOnly() == 1) {
                            continue;
                        }
                    } else {
                        //非渠道用户不展示渠道标的
                        if (iPlan.getChannelName().equals(IPlan.CHANNEL_NAME_FANLI)) {
                            continue;
                        }
                    }
                }*/
                IPlanListDto iPlanListDto = new IPlanListDto();

                BeanUtils.copyProperties(iPlan, iPlanListDto);

                Integer activityId = iPlan.getActivityId();
                if (null != activityId) {
                    ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
                    if (amc != null) {
                        iPlanListDto.setActivityName(amc.getActivityName());
                        iPlanListDto.setIncreaseInterest(amc.getIncreaseInterest());
                        iPlanListDto.setFontColor(amc.getFontColorPc());
                        iPlanListDto.setBackground(amc.getBackgroundPc());
                        if(iPlan.getIplanType() == 2 && amc.getIncreaseTerm() != null){
                            iPlanListDto.setAddTerm(amc.getIncreaseTerm());
                        }else{
                            iPlanListDto.setAddTerm(0);
                        }
                    }
                }
                iPlanListDto.setNewYjtFlag(iPlanAccountService.isNewIplan(iPlan));
                iPlanListDto.setQuota(iPlan.getQuota() / 100.0);
                iPlanListDto.setAvailableQuota(iPlan.getAvailableQuota() / 100.0);
                IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
                if (null != iPlanParam) {
                    iPlanListDto.setExitRate(iPlanParam.getExitFeeRate().doubleValue());
                }
                iPlanListDto.setNewBieAmt(avalNewbieAmt);
                iPlanListDto.setVipRate(vipRate);
              //返回锁定期
                if(iPlan.getExitLockDays()!=null){
                	if (IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())&&"1".equals(iPlan.getInterestAccrualType()+"")){
                		iPlanListDto.setExitLockDaysStr(iPlan.getExitLockDays()+"天");
                    }else{
                    	if(iPlan.getExitLockDays()<31){
                    		iPlanListDto.setExitLockDaysStr(iPlan.getExitLockDays()+"天");
                    	}else{
                    		int month=iPlan.getExitLockDays()/31;
                    		iPlanListDto.setExitLockDaysStr(month+"个月");
                    	}
                    }
                }
                /*if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())){
                	iPlanListDto.setName(IPlanAppListDto.dealName(iPlan.getName()));
                }*/
                iPlanList.add(iPlanListDto);
            }
        }
        IPlanPcListPageData<IPlanListDto> pageData = new IPlanPcListPageData<>();
        List<IPlan> pageList = new PageUtil().ListSplit(iPlanList, pageNum, pageSize);
        pageData.setList(pageList);
        pageData.setPage(pageNum);
        pageData.setSize(pageSize);
        pageData.setTotalPages(iPlanList.size() % pageSize != 0 ? iPlanList.size() / pageSize + 1:iPlanList.size() / pageSize);
        pageData.setTotal(iPlanList.size());
        IPlanParam iPlanParam = null;
        if(!allVisiblePlan.isEmpty()){
            iPlanParam = iPlanParamService.getIPlanParamById(allVisiblePlan.get(0).getIplanParamId());
        }
        if (null != iPlanParam) {
            pageData.setExitFeeRate(iPlanParam.getExitFeeRate().doubleValue());
        }
        pageData.setNewbieUsable(investService.getNewbieAmt(iplanType));

        return new RestResponseBuilder<>().success(pageData);
    }

    //wap端-理财计划列表
    @GetMapping("/wap/iplan/list")
    public RestResponse getIPlan(@RequestParam(value = "userId", required = false) String userId,
                                 @RequestParam(value = "iplanType", required = false, defaultValue = "0") int iplanType) {
        //新手标额度固定
        double newbieAmt=investService.getNewbieAmt(iplanType)<0D?0D:investService.getNewbieAmt(iplanType);
    	  //查询此人新手额度
        double avalNewbieAmt =investService.getNewbieUsable(userId, iplanType);
        List<IPlan> allVisiblePlan=null;
        if (!StringUtils.isEmpty(userId)) {
     	   if(avalNewbieAmt>0){//有额度
     		   allVisiblePlan = iPlanService.findPlanNewBie(iplanType);
     	   }else{//没有额度
     		   allVisiblePlan=iPlanService.findPlanNoAnyNewBies(iplanType);
     	   }
     	   
        }else{
     	   allVisiblePlan = iPlanService.findPlanNewBie(iplanType);
        }
    	
        //根据用户注册来源过滤渠道标
        /*allVisiblePlan = iPlanService.filterIplanByUserSource(allVisiblePlan, userId);*/

        List<IPlanListDto> iPlanList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(allVisiblePlan)) {
            for (IPlan iPlan : allVisiblePlan) {
                /*
                //期限为1个月的标的
                if (iPlan.getTerm().equals(1)) {
                    //用户是否为渠道用户
                    if (registerFlag) {
                        //渠道用户只展示渠道标的，非渠道标的过滤
                        if (!iPlan.getChannelName().equals(IPlan.CHANNEL_NAME_FANLI) || iPlan.getNewbieOnly() == 1) {
                            continue;
                        }
                    } else {
                        //非渠道用户不展示渠道标的
                        if (iPlan.getChannelName().equals(IPlan.CHANNEL_NAME_FANLI)) {
                            continue;
                        }
                    }
                }*/

                IPlanListDto iPlanListDto = new IPlanListDto();

                BeanUtils.copyProperties(iPlan, iPlanListDto);

                Integer activityId = iPlan.getActivityId();
                if (null != activityId) {
                    ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
                    if (amc != null) {
                        iPlanListDto.setActivityName(amc.getActivityName());
                        iPlanListDto.setIncreaseInterest(amc.getIncreaseInterest());
                        iPlanListDto.setFontColor(amc.getFontColorWap());
                        iPlanListDto.setBackground(amc.getBackgroundWap());
                        if(iPlan.getIplanType() == 2 && amc.getIncreaseTerm() != null){
                            iPlanListDto.setAddTerm(amc.getIncreaseTerm());
                        }else{
                            iPlanListDto.setAddTerm(0);
                        }
                    }
                }
                iPlanListDto.setNewYjtFlag(iPlanAccountService.isNewIplan(iPlan));
                iPlanListDto.setNewBieAmt(newbieAmt);
                iPlanListDto.setQuota(iPlan.getQuota() / 100.0);
                iPlanListDto.setAvailableQuota(iPlan.getAvailableQuota() / 100.0);
                if("1".equals(iPlan.getInterestAccrualType()+"")){
                	iPlanListDto.setRepayType("一次性还本付息");
                }else{
                	iPlanListDto.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
                }
                if(iPlan.getExitLockDays()!=null){
                    if (IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())&&"1".equals(iPlan.getInterestAccrualType()+"")){
                        iPlanListDto.setExitLockDaysStr(iPlan.getExitLockDays()+"天");
                    }else{
                        if(iPlan.getExitLockDays()<31){
                            iPlanListDto.setExitLockDaysStr(iPlan.getExitLockDays()+"天");
                        }else{
                            int month=iPlan.getExitLockDays()/31;
                            iPlanListDto.setExitLockDaysStr(month+"个月");
                        }
                    }
                }
                iPlanList.add(iPlanListDto);
            }
        }

        PageData<IPlanListDto> pageData = new PageData<>();
        pageData.setList(iPlanList);
        PageInfo<IPlan> pageInfo2 = new PageInfo<>(allVisiblePlan);
        pageData.setPage(pageInfo2.getPageNum());
        pageData.setSize(pageInfo2.getSize());
        pageData.setTotalPages(pageInfo2.getPages());
        pageData.setTotal(pageInfo2.getTotal());

        return new RestResponseBuilder<>().success(pageData);
    }

    //pc端-理财计划详情
    @GetMapping("/iplan/{iPlanId}/{userId}")
    public RestResponse<IPlanDetailDto> getIPlan(@PathVariable("iPlanId") String iPlanId,
                                                 @PathVariable("userId") String userId) {
        IPlan iPlan = null;
        IPlanDetailDto iPlanDetailDto = new IPlanDetailDto();
        if (org.apache.commons.lang3.StringUtils.isNotBlank(iPlanId)) {
            if (org.apache.commons.lang3.StringUtils.isNumeric(iPlanId)) {
                iPlan = iPlanService.getIPlanById(Integer.valueOf(iPlanId));
            } else {
                iPlan = iPlanService.getByCode(iPlanId);
            }
        }
        if (null == iPlan) {
            logger.warn("定期理财计划[id={}]不存在", iPlanId);
            throw new ProcessException(Error.NDR_0428);
        }else{
        	logger.info("理财计划详情：{}",iPlan.toString());
        }

        BeanUtils.copyProperties(iPlan, iPlanDetailDto);

        IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
        iPlanDetailDto.setExitFeeRate(iPlanParam.getExitFeeRate().doubleValue());
        iPlanDetailDto.setInvestMinMoney(iPlanParam.getInvestMin() / 100.0);//投资最低金额
        iPlanDetailDto.setInvestIncrementMoney(iPlanParam.getInvestIncrement() / 100.0);//投资递增金额
        iPlanDetailDto.setQuota(iPlan.getQuota() / 100.0);
        iPlanDetailDto.setActualRaiseQuota(0d);//实际募集金额,注释，pc网页不用
        iPlanDetailDto.setAvailableQuota(iPlan.getAvailableQuota() / 100.0);
        if (iPlan.getDay() != null && iPlan.getDay() > 0) {
            iPlanDetailDto.setRepayType("一次性还本付息");
        } else {
            iPlanDetailDto.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));//还款方式
        }
        iPlanDetailDto.setPackageType(iPlan.getPackagingType());

        LocalDate raiseOpenDate = DateUtil.parseDate(iPlan.getRaiseOpenTime(), DateUtil.DATE_TIME_FORMATTER_19);
        iPlanDetailDto.setJoinStartTime(DateUtil.getDateStr(raiseOpenDate, DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        if (iPlan.getRaiseCloseTime() == null) {
            LocalDate raiseEndDate = raiseOpenDate.plusDays(iPlan.getRaiseDays());
            iPlanDetailDto.setJoinEndTime(DateUtil.getDateStr(raiseEndDate, DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        } else {
            LocalDate raiseCloseDate = DateUtil.parseDate(iPlan.getRaiseCloseTime(), DateUtil.DATE_TIME_FORMATTER_19);
            iPlanDetailDto.setJoinEndTime(DateUtil.getDateStr(raiseCloseDate, DateTimeFormatter.ofPattern("yyyy年MM月dd日")));
        }

        Double newbieUsable = investService.getNewbieUsable(userId,iPlan.getIplanType()) / 100.0;
            if (newbieUsable <= 0) {
                //不是新手
                iPlanDetailDto.setNewbieRestQuota(0d);
            } else {
                iPlanDetailDto.setNewbieRestQuota(newbieUsable);
            }
            String iplanName=iPlan.getIplanType()==0?"(限月月盈)":"(限省心投)";
            Config iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);
            if(iPlanNewbieAmtConfig==null || "0".equals(iPlanNewbieAmtConfig.getValue().toString())){
            	iPlanDetailDto.setNewBieTip(iplanName);
            }else{
            	iPlanDetailDto.setNewBieTip("(限任意定期项目)");
            }
        iPlanDetailDto.setNewbieOnly(iPlan.getNewbieOnly());
        Integer activityId = iPlan.getActivityId();
        if (null != activityId) {
            ActivityMarkConfigure amc = activityMarkConfigureService.findById(activityId);
            iPlanDetailDto.setActivityName(amc.getActivityName());
            iPlanDetailDto.setIncreaseInterest(amc.getIncreaseInterest());
            iPlanDetailDto.setFontColor(amc.getFontColorPc());
            iPlanDetailDto.setBackground(amc.getBackgroundPc());
            if(iPlan.getIplanType() == 2 && amc.getIncreaseTerm() != null){
                iPlanDetailDto.setAddTerm(amc.getIncreaseTerm());
            }else {
                iPlanDetailDto.setAddTerm(0);
            }
        }
        List<IPlanAppPurchaseDetailDto.RedPacketApp> redPacketAppList = new ArrayList<>();
        List<IPlanAppPurchaseDetailDto.RedPacketApp> unRedPacketAppList = new ArrayList<>();
        if (!StringUtils.isEmpty(userId)||!"null".equals(userId)){
            List<RedPacket> redPacketList = redPacketService.getUsablePackets(userId, iPlan, "pc");
            if (!CollectionUtils.isEmpty(redPacketList)) {
                for (RedPacket redPacket : redPacketList) {
                    IPlanAppPurchaseDetailDto.RedPacketApp redPacketApp = new IPlanAppPurchaseDetailDto.RedPacketApp();
                    redPacketApp.setId(redPacket.getId());
                    redPacketApp.setAmt(redPacket.getMoney());
                    redPacketApp.setAmt2(df.format(redPacket.getMoney()));
                    redPacketApp.setRate(redPacket.getRate());
                    redPacketApp.setRate2(df.format(redPacket.getRate() * 100) + "%");
                    redPacketApp.setRateDay(redPacket.getRateDay());
                    redPacketApp.setName(redPacket.getName());
                    redPacketApp.setDeadLine(redPacket.getDeadLine().substring(0,10));
                    redPacketApp.setIntroduction(redPacketService.getRedPacketInvestMoneyStrForIplan(redPacket,iPlan));
                    redPacketApp.setType(redPacket.getType());
                    redPacketApp.setUseStatus(redPacket.getUseStatus());
                    redPacketApp.setInvestMoney(redPacket.getInvestMoney());
                    redPacketAppList.add(redPacketApp);
                }
            }
        }

        iPlanDetailDto.setRedPacketAppList(redPacketAppList);
        double vipRate = marketService.getIplanVipRate(userId);
        iPlanDetailDto.setVipRate(vipRate);
        iPlanDetailDto.setNewbieAmt(investService.getNewbieAmt(iPlan.getIplanType()));

        List<SubjectYjtDto> subjectYjtDtos = new ArrayList<>();
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
            if (IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())){
                List<CreditOpening> creditOpenings = creditOpeningDao.getByIplanId(iPlan.getId());
                    if (creditOpenings != null && creditOpenings.size() > 0) {
                        Map<String,List<CreditOpening>> creditOpeningMap = creditOpenings.stream().collect(Collectors.groupingBy(CreditOpening::getSubjectId));
                        for (Map.Entry<String,List<CreditOpening>> entry : creditOpeningMap.entrySet()){
                            String subjectId = entry.getKey();
                            List<CreditOpening> subjectCreditOpenings = entry.getValue();
                            int totalMoney = subjectCreditOpenings.stream().map(creditOpening->creditOpening.getPackPrincipal()!=null?creditOpening.getPackPrincipal():creditOpening.getTransferPrincipal()).reduce(Integer::sum).orElse(0);
                            Subject subject = subjectService.getBySubjectId(subjectId);
                            SubjectYjtDto dto = new SubjectYjtDto();
                            dto.setName(subject.getName());
                            dto.setSubjectId(subject.getSubjectId());
                            dto.setRepayType(subject.getRepayType());
                            dto.setTotalAmt(df.format(totalMoney/100.0));
                            subjectYjtDtos.add(dto);
                        }

                    }

                } else {
                List<Subject> subjects = subjectService.getByIplanId(iPlan.getId());
                if (subjects != null && subjects.size() > 0) {
                    for (Subject subject : subjects) {
                        SubjectYjtDto dto = new SubjectYjtDto();
                        BeanUtils.copyProperties(subject, dto);
                        dto.setTotalAmt(df.format(subject.getTotalAmt()/100.0));
                        subjectYjtDtos.add(dto);
                    }
                }
            }
        }
        iPlanDetailDto.setSubjectList(subjectYjtDtos);
       //String subjectRate = iPlan.getSubjectRate()==null?"0.144":iPlan.getSubjectRate().toString();
        iPlanDetailDto.setSubjectRate(iPlanAccountService.getRate(iPlan).toString());
        iPlanDetailDto.setExitLockDaysStr(iPlan.getExitLockDays()+"天");
        iPlanDetailDto.setExitLockCountFlag("0");
        iPlanDetailDto.setExitLockCount(iPlan.getExitLockDays());
        int month=0;
        if(iPlan.getIplanType()==2&&iPlan.getExitLockDays()>=31){
        	month=iPlan.getExitLockDays()/31;
    		iPlanDetailDto.setExitLockDaysStr(month+"个月");
    		iPlanDetailDto.setExitLockCountFlag("1");
            iPlanDetailDto.setExitLockCount(month);
        }
        if(iPlanAccountService.isNewIplan(iPlan)){
        	double totalRate=(iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0) + (iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0) + vipRate;
        	iPlanDetailDto.setNewYjtFlag(true);
        	iPlanDetailDto.setHoldingInfo(ArithUtil.round(totalRate*100,2)
        			+"%~"+ArithUtil.round((totalRate*100+
                			(iPlan.getTerm()-month)*iPlan.getIncreaseRate().doubleValue()*100),2)+"%");

        	 //为柱状图提供数据
            for(int i=1;i<=iPlan.getTerm();i++){
            	double data=ArithUtil.round(totalRate*100,2);
            	iPlanDetailDto.getIncreaseRateX().add(i+"");
            	if(i<=month){//如果在锁定期期间，则基本利率
            		iPlanDetailDto.getIncreaseRateY().add(data+"");
            	}else{
            		data=ArithUtil.round((totalRate*100+
            				(i-month)*iPlan.getIncreaseRate().doubleValue()*100),2);
                	iPlanDetailDto.getIncreaseRateY().add(data+"");
            	}
            }
        }
        if("1".equals(iPlan.getInterestAccrualType()+"")){
        	iPlanDetailDto.setExitFeeRate(0);//月月盈天标无法转让
        }

        return new RestResponseBuilder<IPlanDetailDto>().success(iPlanDetailDto);
    }

    /**
     * PC端-理财计划投资管理页（持有中，转出中，已完成）
     * @param userId    用户id
     * @param type      页面类型：0持有中 1转出中 2已完成
     * @param pageNum   页码
     * @param pageSize  每页条数
     */
    @GetMapping("/iplan/invest/manage/{userId}/{type}")
    public RestResponse iPlanInvestManage(     @PathVariable("userId") String userId, @PathVariable("type") int type,
                                               @RequestParam("pageNo") int pageNum, @RequestParam("pageSize") int pageSize,
                                               @RequestParam(value = "iplanType", required = false, defaultValue = "0") int iplanType) {
        if (pageNum < 0) {
            pageNum = 1;
        }
        if (pageSize < 0) {
            pageSize = 10;
        }
        if (null == userId) {
            throw new IllegalArgumentException("userId不能为空");
        }

        if (type == IPlanInvestManageDto.PAGE_TYPE_HOLDING) {

            IPlanInvestManageHoldDto investManageDto = new IPlanInvestManageHoldDto();
            investManageDto.setPageType(IPlanAppInvestManageDto.PAGE_TYPE_HOLDING);

            //总持有金额，待收收益，处理中金额，总实收收益
            double interest=0.0;
            double totalPrincipal = 0.0;
            double transAmt = 0.0;
            double processedAmt = 0.0;
            double totalRepayAmt=0.0;
            Integer transferTotalAmt=0;
            List<IPlanAccount> allPlan = iPlanAccountService.findByUserIdAndStatusIn(userId,
                    new HashSet<>(Arrays.asList(IPlanAccount.STATUS_PROCEEDS)), iplanType);
            for (IPlanAccount iPlanAccount : allPlan) {
            	if (iPlanAccount.getCurrentPrincipal() == 0) {
                    if(iplanType==IPlan.IPLAN_TYPE_YJT && (iPlanAccount.getAmtToTransfer()==null ||iPlanAccount.getAmtToTransfer()==0)) {
                        continue;
                    }else if(iplanType!=IPlan.IPLAN_TYPE_YJT){
                        continue;
                    }
                }
                totalPrincipal += iPlanAccount.getCurrentPrincipal() / 100.0;
                interest += (iPlanAccount.getExpectedInterest()
                        + (iPlanAccount.getIplanExpectedBonusInterest() == null ? 0 : iPlanAccount.getIplanExpectedBonusInterest())
                        + (iPlanAccount.getIplanExpectedVipInterest() == null ? 0 : iPlanAccount.getIplanExpectedVipInterest())) / 100.0;
                totalRepayAmt += (iPlanAccount.getIplanPaidInterest() + iPlanAccount.getIplanPaidBonusInterest()) / 100.0;
            }
            if(iplanType==IPlan.IPLAN_TYPE_YJT){
                List<IPlanTransLog> iplanTransLogList = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING, IPlanTransLog.TRANS_STATUS_SUCCEED)));
                if(iplanTransLogList != null && iplanTransLogList.size() > 0){
                    for (IPlanTransLog iplanTransLog : iplanTransLogList) {
                        IPlanAccount account = iPlanAccountService.findById(iplanTransLog.getAccountId());
                        if(account.getStatus().equals(IPlanAccount.STATUS_NORMAL_EXIT)){
                            transferTotalAmt += 0;
                        }else {
                            if (IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iplanTransLog.getTransStatus())) {
                                transferTotalAmt += iplanTransLog.getTransAmt();
                            } else {
                                transferTotalAmt += iplanTransLog.getProcessedAmt();
                            }
                        }
                    }
                }
                investManageDto.setTransferTotalAmt(df.format(transferTotalAmt/100.0));
            }else{
                List<IPlanTransLog> allTransLog = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING, IPlanTransLog.TRANS_STATUS_SUCCEED, IPlanTransLog.TRANS_STATUS_TO_CONFIRM)))
                        .stream().filter(iPlanTransLog -> allPlan.stream().anyMatch(planAccount -> planAccount.getId().equals(iPlanTransLog.getAccountId()))).collect(Collectors.toList());
                for (IPlanTransLog iPlanTransLog : allTransLog) {
                    transAmt += iPlanTransLog.getTransAmt() / 100.0;
                    processedAmt += iPlanTransLog.getProcessedAmt() / 100.0;
                }
                investManageDto.setProcessAmt(df.format(transAmt - processedAmt));//处理中金额
            }


            investManageDto.setAmount(df.format(totalPrincipal));//总持有金额
            investManageDto.setInterest(df.format(interest + totalRepayAmt));//预计总待收收益
            investManageDto.setPaidInterest(df.format(totalRepayAmt));//总实收收益
            investManageDto.setExpectedInterest(df.format(interest));
            //转让总金额

            //持有中
            List<IPlanAccount> planList = iPlanAccountService.getByPageHelper(userId,
                    new HashSet<>(Arrays.asList(IPlanAccount.STATUS_PROCEEDS)), false, pageNum, pageSize, iplanType);

            PageInfo<IPlanAccount> pageInfo = new PageInfo<>(planList);
            investManageDto.setPage(pageInfo.getPageNum());
            investManageDto.setSize(pageInfo.getSize());
            investManageDto.setTotalPages(pageInfo.getPages());
            investManageDto.setTotal(pageInfo.getTotal());

            List<IPlanInvestManageHoldDto.Detail> details = new ArrayList<>();

            if (!CollectionUtils.isEmpty(planList)) {
                for (IPlanAccount iPlanAccount : planList) {
                	if (iPlanAccount.getCurrentPrincipal() == 0) {
                        if(iplanType==IPlan.IPLAN_TYPE_YJT && (iPlanAccount.getAmtToTransfer()==null ||iPlanAccount.getAmtToTransfer()==0)) {
                            continue;
                        }/*else if(iplanType!=IPlan.IPLAN_TYPE_YJT){
                            continue;
                        }*/
                    }
                    IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
                    IPlanInvestManageHoldDto.Detail detail = new IPlanInvestManageHoldDto.Detail();

                    //查询用户一期月月盈是否有待确认记录
                    List<IPlanTransLog> confirmTransLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iPlanAccount.getIplanId(),
                            new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                            new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_TO_CONFIRM)));
                    if (confirmTransLogs != null && confirmTransLogs.size() > 0) {
                        detail.setConfirmCount(confirmTransLogs.size());
                    } else {
                        if (iPlanAccount.getCurrentPrincipal() == 0) {
                            if(iplanType==IPlan.IPLAN_TYPE_YJT && (iPlanAccount.getAmtToTransfer()==null ||iPlanAccount.getAmtToTransfer()==0)) {
                                continue;
                            }else if(iplanType!=IPlan.IPLAN_TYPE_YJT){
                                continue;
                            }
                        }
                    }

                    detail.setId(iPlanAccount.getIplanId());
                    detail.setName(iPlan.getName());
                    detail.setEndTime(iPlan.getEndTime());
                    detail.setiPlanQuota(String.valueOf(iPlan.getQuota() / 100.0));
                    if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                        detail.setRepayType("一次性还本付息");
                    }else {
                        detail.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
                    }
                    detail.setJoinTime(iPlanAccount.getCreateTime());
                    detail.setStatus(iPlan.getStatus());//2募集中 4收益中
                    detail.setTerm(iPlan.getTerm());//期限
                    detail.setHoldingAmt(df.format(iPlanAccount.getCurrentPrincipal() / 100.0));//当前计息本金

                    //待回收利息
                    detail.setExpectedInterest(df.format((iPlanAccount.getExpectedInterest()
                            + (iPlanAccount.getIplanExpectedBonusInterest() == null ? 0 : iPlanAccount.getIplanExpectedBonusInterest())
                            + (iPlanAccount.getIplanExpectedVipInterest() == null ? 0 : iPlanAccount.getIplanExpectedVipInterest())) / 100.0));
                    //已回利息
                    detail.setPaidInterest(df.format((iPlanAccount.getIplanPaidInterest()
                            + iPlanAccount.getIplanPaidBonusInterest()
                            + iPlanAccount.getIplanPaidVipInterest()
                            ) / 100.0));
                    //收益率
                    detail.setFixRate(df.format(iPlan.getFixRate().add(iPlan.getBonusRate()).add(iPlanAccount.getVipRate())));
                    detail.setNewRate(df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue())*100)+"%");
                    if(iPlanAccountService.isNewIplan(iPlan)){
                        if(iPlan.getRaiseFinishTime()!=null){
                            detail.setNewRate(df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue())*100)+"%"+"-"+df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue()+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+"%");
                        }
                    }
                    //vip利率
                    detail.setVipRate(iPlanAccount.getVipRate());
                    detail.setNewLock(iPlanAccountService.getNewLock(iPlan));
                    detail.setDay(iPlan.getDay());
                    detail.setInterestAccrualType(iPlan.getInterestAccrualType());

                    detail.setDay(iPlan.getDay());
                    detail.setInterestAccrualType(iPlan.getInterestAccrualType());

                    if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                        ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                        if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                            //总利率
                            detail.setFixRate(df.format(iPlan.getFixRate().doubleValue() + iPlan.getBonusRate().doubleValue()));
                            //活动标相关
                            detail.setActivityName(activityMarkConfigure.getActivityName());
                            detail.setBackground(activityMarkConfigure.getBackgroundPc());
                            detail.setFontColor(activityMarkConfigure.getFontColorPc());
                            detail.setBonusRate(iPlan.getBonusRate());
                            if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                                detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                            }else {
                                detail.setAddTerm(0);
                            }

                        }
                    }
                    //年化收益
                    detail.setYearRate(df.format(iPlan.getFixRate()));

                    //锁定期
                    detail.setExitLockDays(iPlan.getExitLockDays());
                    //活动加息信息
                    detail.setActivityId(iPlan.getActivityId());

                    detail.setUrl(iPlanAccount.getServiceContract());

                    //若是一键投,查询已回收本息(包含加息利息)
                    if(iplanType==IPlan.IPLAN_TYPE_YJT){
                        Integer totalPaidAmt = subjectRepayDetailDao.findByUserIdAndAccountIdAndChannel(Credit.SOURCE_CHANNEL_YJT,iPlanAccount.getUserId(),iPlanAccount.getId());
                        detail.setPaidTotalAmt(df4.format(totalPaidAmt/100.0));
                        //待回本息
                        detail.setExpectedTotalAmt(df4.format((iPlanAccount.getCurrentPrincipal()+iPlanAccount.getExpectedInterest()+iPlanAccount.getIplanExpectedBonusInterest()+iPlanAccount.getIplanExpectedVipInterest())/100.0));
                        //转让金额
                        Integer money = 0;
                        Integer amt = 0;
                        List<IPlanTransLog> iplanTransLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iPlanAccount.getIplanId(),
                                new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                                new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING, IPlanTransLog.TRANS_STATUS_SUCCEED)));
                        if(iplanTransLogs != null && iplanTransLogs.size() > 0){
                            for (IPlanTransLog iplanTransLog : iplanTransLogs) {
                                if(IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iplanTransLog.getTransStatus())){
                                    money = iplanTransLog.getTransAmt();
                                }else{
                                    money = iplanTransLog.getProcessedAmt();
                                }
                                transferTotalAmt += money;
                                amt +=money;
                            }
                        }
                        //是否免费转让
                        if(iPlanAccountService.isNewIplan(iPlan)){
                            detail.setIsFree(IPlanAppInvestManageHoldDto.IS_FREE);
                        }
                        detail.setTransferAmt(df4.format(amt/100.0));
                        detail.setAccountId(iPlanAccount.getId());
                        detail.setCanTransfer(iPlanAccountService.checkCondition(iPlanAccount));
                    }else{
                        //是否已匹配债权
                        List<Credit> creditList = creditService.findBySourceAccountId(iPlanAccount.getId(),iplanType==1?Credit.SOURCE_CHANNEL_YJT:Credit.SOURCE_CHANNEL_IPLAN);
                        if (CollectionUtils.isEmpty(creditList)) {
                            //没有债权匹配
                            detail.setCreditMatched(IPlanInvestManageDto.CREDIT_MATCHED_N);
                        } else {
                            detail.setCreditMatched(IPlanInvestManageDto.CREDIT_MATCHED_Y);
                        }
                        //是否可以转出
                        LocalDateTime raiseOpenDateTime = DateUtil.parseDateTime(iPlan.getRaiseOpenTime(), DateUtil.DATE_TIME_FORMATTER_19);
                        LocalDateTime raiseCloseDateTime;
                        //实际结束募集时间
                        String raiseCloseTime = iPlan.getRaiseCloseTime();
                        if (null == raiseCloseTime) {
                            //如果实际募集时间为空，则取开始募集时间+募集天数=预计的募集满的时间
                            raiseCloseDateTime = raiseOpenDateTime.plusDays(iPlan.getRaiseDays());
                        } else {
                            raiseCloseDateTime = DateUtil.parseDateTime(raiseCloseTime, DateUtil.DATE_TIME_FORMATTER_19);
                        }
                        //锁定期结束时间
                        LocalDateTime localDateTimeLockEnd = raiseCloseDateTime.plusDays(iPlan.getExitLockDays());
                        detail.setRestDays((int)DateUtil.betweenDays(LocalDate.now(), raiseCloseDateTime.toLocalDate().plusMonths(iPlan.getTerm())));
                        boolean after = Arrays.asList(IPlan.STATUS_RAISING_FINISH, IPlan.STATUS_EARNING).stream().anyMatch(status -> status.equals(iPlan.getStatus())) && LocalDateTime.now().isAfter(localDateTimeLockEnd);
                        //如果可以转出
                        if (after) {
                            IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
                            double iPlanExitAmt = this.getExitAmt(iPlan, userId, iPlanAccount.getCurrentPrincipal(), raiseCloseTime) / 100.0;
                            if (null != iPlanParam) {
                                double exitFee = iPlanParam.getExitFeeRate().multiply(new BigDecimal(iPlanAccount.getCurrentPrincipal() / 100.0)).doubleValue();
                                detail.setExitFee(df.format(exitFee));
                                detail.setExitableAmt(iPlanExitAmt - exitFee);
                            }
                        }
                        detail.setCanTransfer(after ? IPlanInvestManageDto.CAN_TRANSFER_Y : IPlanInvestManageDto.CAN_TRANSFER_N);
                        //用来判断是否使用红包
                        Integer sumRedpacketNum = iPlanTransLogService.getSumCountUseRedpacket(userId, iPlan.getId(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                                new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING, IPlanTransLog.TRANS_STATUS_SUCCEED, IPlanTransLog.TRANS_STATUS_TO_CONFIRM)));
                        detail.setRedPacketNum(sumRedpacketNum);

                    }

                    if("1".equals(iPlan.getInterestAccrualType()+"")){
                    	detail.setCanTransfer(IPlanInvestManageDto.CAN_TRANSFER_N);
                    	detail.setRepayType("一次性还本付息");
                    }
                    details.add(detail);
                }
            }
            investManageDto.setDetails(details);

            return new RestResponseBuilder<>().success(investManageDto);
        }


        if (type == IPlanInvestManageDto.PAGE_TYPE_TRANSFERRING) {

            IPlanInvestManageTransferDto investManageDto = new IPlanInvestManageTransferDto();
            investManageDto.setPageType(IPlanAppInvestManageDto.PAGE_TYPE_TRANSFERRING);

            //总金额，转出费用，预计到账
            double principal = 0.0;
            double interest = 0.0;
            double paidInterest = 0.0;
            List<IPlanTransLog> allTransPlan = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iplanType,
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT,
                            IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING)));
            for (IPlanTransLog iPlanTransLog : allTransPlan) {
                IPlanAccount iPlanAccount =  iPlanAccountService.findById(iPlanTransLog.getAccountId());
                principal += iPlanAccount.getInitPrincipal() / 100.0;
                /*if (iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT)) {
                    IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
                    double exitFee = iPlanAccount.getExitFee() / 100.0;
                    interest += exitFee;
                }*/
            }

            investManageDto.setAmount(df.format(principal));//总持有金额

            //页面-1转出中
            List<IPlanTransLog> transLogList = iPlanTransLogService.getByPageHelper(userId, iplanType,
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT,
                            IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING)), pageNum, pageSize);

            PageInfo<IPlanTransLog> pageInfo = new PageInfo<>(transLogList);
            investManageDto.setPage(pageInfo.getPageNum());
            investManageDto.setSize(pageInfo.getSize());
            investManageDto.setTotalPages(pageInfo.getPages());
            investManageDto.setTotal(pageInfo.getTotal());


            List<IPlanInvestManageTransferDto.Detail> details = new ArrayList<>();
            if (!CollectionUtils.isEmpty(transLogList)) {
                for (IPlanTransLog iPlanTransLog : transLogList) {
                    IPlanInvestManageTransferDto.Detail detail = new IPlanInvestManageTransferDto.Detail();
                    if (iPlanTransLog.getTransAmt() == 0) {
                        continue;
                    }
                    IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
                    IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
                    double exitFee = iPlanAccount.getExitFee() / 100.0;
                    detail.setId(iPlanTransLog.getId());
                    detail.setiPlanId(iPlan.getId());
                    detail.setEndTime(iPlan.getEndTime());
                    detail.setExitFee(df.format(exitFee));
                    detail.setName(iPlan.getName());
                    detail.setTerm(iPlan.getTerm());
                    interest += (iPlanAccount.getExpectedInterest()
                            + iPlanAccount.getIplanExpectedBonusInterest()
                            + iPlanAccount.getIplanExpectedVipInterest()) / 100.0;
                    paidInterest += (iPlanAccount.getIplanPaidInterest()
                            + iPlanAccount.getIplanPaidBonusInterest()
                            + iPlanAccount.getIplanPaidVipInterest()) / 100.0;
                    detail.setHoldingAmt(df.format(iPlanAccount.getInitPrincipal() / 100.0));
                    detail.setExpectedInterest((iPlanAccount.getExpectedInterest()
                            + iPlanAccount.getIplanExpectedBonusInterest()
                            + iPlanAccount.getIplanExpectedVipInterest()) / 100.0);
                    detail.setPaidInterest((iPlanAccount.getIplanPaidInterest()
                            + iPlanAccount.getIplanPaidBonusInterest()
                            + iPlanAccount.getIplanPaidVipInterest()
                            ) / 100.0);
                    detail.setFixRate(df.format(iPlan.getFixRate().add(iPlan.getBonusRate()).add(iPlanAccount.getVipRate())));
                    detail.setVipRate(iPlanAccount.getVipRate());
                    detail.setDay(iPlan.getDay());
                    detail.setInterestAccrualType(iPlan.getInterestAccrualType());
                    /*if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                        ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                        if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                            detail.setFixRate(df.format(iPlan.getFixRate().doubleValue() + activityMarkConfigure.getIncreaseInterest()/100.0));
                        }
                    }*/
                    //新增年化收益，红包使用信息，活动加息信息
                    //用来判断是否使用红包
                    Integer sumRedpacketNum = iPlanTransLogService.getSumCountUseRedpacket(userId, iPlan.getId(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                            new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED)));
                    detail.setRedPacketNum(sumRedpacketNum);
                    //活动加息信息
                    Integer activityId = iPlan.getActivityId();
                    detail.setActivityId(activityId);

                    detail.setYearRate(df.format(iPlan.getFixRate()));//年化收益
                    if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                        ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                        if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                            detail.setBonusRate(iPlan.getBonusRate());//加息利率
                            //  detail.setFixRate(df.format(activityMarkConfigure.getIncreaseInterest()/100.0));//加息收益
                            if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                                detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                            }else {
                                detail.setAddTerm(0);
                            }
                        }
                    }
                    //锁定期
                    detail.setExitLockDays(iPlan.getExitLockDays());
                    //转出费用   IPlan iPlan, String userId, Integer principal, String raiseCloseTime
                    detail.setExitFee(String.valueOf(iPlanAccount.getExitFee()/100.0));
                    //实际到账金额
                    Integer sunMoney = getExitAmt(iPlan,userId, iPlanAccount.getCurrentPrincipal(),iPlan.getRaiseCloseTime());//用的是当前的本金
                    detail.setActualMoney((sunMoney-iPlanAccount.getExitFee())/100.0);

                    detail.setTime(iPlanTransLog.getTransTime());//申请转让时间
                    detail.setCreditMatched(IPlanInvestManageDto.CREDIT_MATCHED_Y);
                    detail.setUrl(iPlanAccount.getServiceContract());

                    // 月月盈提前还款
                    if (IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())) {
                        detail.setBeforeRepayFlag(BaseIPlanDto.BEFORE_REPAY_FLAG_Y);
                    }
                    if("1".equals(iPlan.getInterestAccrualType()+"")){
                    	detail.setRepayType("一次性还本付息");
                    }else{
                    	 detail.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
                    }
                    details.add(detail);
                }
            }
            investManageDto.setInterest(df.format(interest + paidInterest));
            investManageDto.setPaidInterest(paidInterest);//已付收益
            investManageDto.setDetails(details);

            return new RestResponseBuilder<>().success(investManageDto);
        }


        if (type == IPlanInvestManageDto.PAGE_TYPE_FINISH) {

            //查询一键投已完成投资管理列表
            if (iplanType == IPlan.IPLAN_TYPE_YJT) {
                YjtInvestManageFinishDto yjtInvestManageFinishDto = new YjtInvestManageFinishDto();
                //累计投资金额
                double initTotalPrincipal=0.0;
                //累计转让金额
                double transferTotalAmt=0.0;
                //累计已到账本息
                double arrivedTotalAmt = 0.0;

                //查询已完成一键投账户
                List<IPlanAccount> allAccounts = iPlanAccountService.getByUserIdAndIplanTypeAndStatus(userId, IPlan.IPLAN_TYPE_YJT, IPlanAccount.STATUS_NORMAL_EXIT);
                if (allAccounts != null && allAccounts.size() > 0) {
                    for (IPlanAccount iPlanAccount : allAccounts) {
                        initTotalPrincipal += iPlanAccount.getInitPrincipal();
                        //正常结束的一键投总还款金额
                        Integer normalFinishAmt = subjectRepayDetailDao.findByUserIdAndAccountIdAndChannel(Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getUserId(), iPlanAccount.getId());
                        arrivedTotalAmt += normalFinishAmt;
                        //转让交易记录
                        List<IPlanTransLog> transLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED))).stream().filter(iPlanTransLog -> iPlanTransLog.getActualAmt() > 0).collect(Collectors.toList());
                        int transferAmt = 0;
                        int arrivedAmt = 0;
                        if (transLogs != null && transLogs.size() > 0) {
                            for (IPlanTransLog transLog : transLogs) {
                                transferAmt += transLog.getTransAmt();
                                arrivedAmt += transLog.getActualAmt();
                            }
                        }
                        transferTotalAmt += transferAmt;
                        arrivedTotalAmt += arrivedAmt;
                    }
                }

                yjtInvestManageFinishDto.setInitTotalPrincipal(df.format(initTotalPrincipal / 100.0));
                yjtInvestManageFinishDto.setTransferTotalAmt(df.format(transferTotalAmt / 100.0));
                yjtInvestManageFinishDto.setArrivedTotalAmt(df.format(arrivedTotalAmt / 100.0));

                List<YjtInvestManageFinishDto.Detail> details = new ArrayList<>();
                //查询已完成一键投账户
                List<IPlanAccount> iPlanAccounts = iPlanAccountService.getByUserIdAndIplanTypeAndStatusByPageHelper(userId, IPlan.IPLAN_TYPE_YJT, IPlanAccount.STATUS_NORMAL_EXIT, pageNum, pageSize);
                if (iPlanAccounts != null && iPlanAccounts.size() > 0) {
                    for (IPlanAccount iPlanAccount : iPlanAccounts) {
                        YjtInvestManageFinishDto.Detail detail = new YjtInvestManageFinishDto.Detail();
                        IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
                        //正常结束的一键投总还款金额
                        Integer normalFinishAmt = subjectRepayDetailDao.findByUserIdAndAccountIdAndChannel(Credit.SOURCE_CHANNEL_YJT,iPlanAccount.getUserId(),iPlanAccount.getId());
                        //查询转让完成的交易记录，获取转让金额与转让实际到账金额
                        int transferAmt = 0;
                        int arrivedAmt = normalFinishAmt;
                        List<IPlanTransLog> transLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED))).stream().filter(iPlanTransLog -> iPlanTransLog.getActualAmt() > 0).collect(Collectors.toList());
                        if (transLogs != null && transLogs.size() > 0) {
                            for (IPlanTransLog transLog : transLogs) {
                                transferAmt += transLog.getProcessedAmt();
                                arrivedAmt += transLog.getActualAmt();
                            }
                        }
                        detail.setId(iPlan.getId());
                        detail.setName(iPlan.getName());
                        if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                            ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                                //活动标相关
                                detail.setActivityName(activityMarkConfigure.getActivityName());
                                detail.setBackground(activityMarkConfigure.getBackgroundPc());
                                detail.setFontColor(activityMarkConfigure.getFontColorPc());
                                if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                                    detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                                }else {
                                    detail.setAddTerm(0);
                                }
                            }
                        }
                        detail.setTerm(iPlan.getTerm());
                        detail.setRate(df.format(iPlan.getFixRate().add(iPlan.getBonusRate()).add(iPlanAccount.getVipRate())));
                        detail.setNewRate(df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue())*100)+"%");
                        if(iPlanAccountService.isNewIplan(iPlan)){
                            detail.setNewRate(df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue())*100)+"%"+"-"+df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue()+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+"%");
                        }
                        detail.setExitLockDays(iPlan.getExitLockDays());
                        detail.setInitPrincipal(df.format(iPlanAccount.getInitPrincipal() / 100.0));
                        detail.setTransferAmt(df.format(transferAmt / 100.0));
                        detail.setArrivedAmt(df.format(arrivedAmt / 100.0));
                        detail.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
                        detail.setNewLock(iPlanAccountService.getNewLock(iPlan));
                        detail.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
                        details.add(detail);
                    }
                }
                yjtInvestManageFinishDto.setDetails(details);

                PageInfo<IPlanAccount> pageInfo = new PageInfo<>(iPlanAccounts);
                yjtInvestManageFinishDto.setPage(pageInfo.getPageNum());
                yjtInvestManageFinishDto.setSize(pageInfo.getSize());
                yjtInvestManageFinishDto.setTotalPages(pageInfo.getPages());
                yjtInvestManageFinishDto.setTotal(pageInfo.getTotal());

                return new RestResponseBuilder<>().success(yjtInvestManageFinishDto);

            } else {//月月盈已完成投资管理列表
                IPlanInvestManageFinishDto investManageDto = new IPlanInvestManageFinishDto();
                investManageDto.setPageType(IPlanAppInvestManageDto.PAGE_TYPE_FINISH);
                //累计投资，累计收益
                double amt=0.0;
                double interest=0.0;
                double paidInterest = 0.0;
                List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT,
                                IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED)));
                for (IPlanTransLog iPlanTransLog : iPlanTransLogs) {
                    IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
                    amt += iPlanAccount.getInitPrincipal() / 100.0;
                    interest += (iPlanAccount.getExpectedInterest()
                            + iPlanAccount.getIplanExpectedBonusInterest()
                            + iPlanAccount.getIplanExpectedVipInterest()) / 100.0;
                    paidInterest += (iPlanAccount.getIplanPaidInterest()
                            + iPlanAccount.getIplanPaidBonusInterest()
                            + iPlanAccount.getIplanPaidVipInterest()) / 100.0;
                }
                investManageDto.setAmount(df.format(amt));
                investManageDto.setInterest(df.format(interest + paidInterest));
                investManageDto.setPaidInterest(paidInterest);

                //页面-2已完成（包括正常退出和提前退出）
                List<IPlanTransLog> planList = iPlanTransLogService.getByPageHelper(userId, iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT,
                                IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED)), pageNum, pageSize);
                PageInfo<IPlanTransLog> pageInfo = new PageInfo<>(planList);
                investManageDto.setPage(pageInfo.getPageNum());
                investManageDto.setSize(pageInfo.getSize());
                investManageDto.setTotalPages(pageInfo.getPages());
                investManageDto.setTotal(pageInfo.getTotal());

                List<IPlanInvestManageFinishDto.Detail> details = new ArrayList<>();
                if (!CollectionUtils.isEmpty(planList)) {
                    for (IPlanTransLog iPlanTransLog : planList) {
                        IPlanInvestManageFinishDto.Detail detail = new IPlanInvestManageFinishDto.Detail();
                        IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
                        IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
                        detail.setId(iPlan.getId());
                        detail.setName(iPlan.getName());
                        detail.setEndTime(iPlan.getEndTime());
                        detail.setTerm(iPlan.getTerm());
                        detail.setStatus(iPlanAccount.getStatus());//0收益中，1已到期，2提前退出
                        detail.setHoldingAmt(df.format(iPlanAccount.getInitPrincipal() / 100.0));//购买金额
                        detail.setPaidInterest(df.format((iPlanAccount.getIplanPaidInterest()
                                + iPlanAccount.getIplanPaidBonusInterest()
                                + iPlanAccount.getIplanPaidVipInterest()
                                + iPlanAccount.getInitPrincipal() - iPlanAccount.getExitFee()) / 100.0));//实际收益
                        detail.setFixRate(df.format(iPlan.getFixRate().add(iPlan.getBonusRate()).add(iPlanAccount.getVipRate())));//收益率
                        detail.setVipRate(iPlanAccount.getVipRate());
                        detail.setDay(iPlan.getDay());
                        detail.setInterestAccrualType(iPlan.getInterestAccrualType());
                    /*if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                        ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                        if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                            detail.setFixRate(df.format(iPlan.getFixRate().doubleValue() + activityMarkConfigure.getIncreaseInterest()/100.0));
                        }
                    }*/
                        //新增年化收益，红包使用信息，活动加息信息
                        //用来判断是否使用红包
                        Integer sumRedpacketNum = iPlanTransLogService.getSumCountUseRedpacket(userId, iPlan.getId(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                                new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED)));
                        detail.setRedPacketNum(sumRedpacketNum);
                        //活动加息信息
                        Integer activityId = iPlan.getActivityId();
                        detail.setActivityId(activityId);
                        detail.setYearRate(df.format(iPlan.getFixRate()));//年化收益
                        if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                            ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                                detail.setBonusRate(iPlan.getBonusRate());//加息利率
                                // detail.setFixRate(df.format(activityMarkConfigure.getIncreaseInterest()/100.0));//加息收益
                            }
                        }
                        //锁定期
                        detail.setExitLockDays(iPlan.getExitLockDays());
                        //转出费用   IPlan iPlan, String userId, Integer principal, String raiseCloseTime
                        detail.setExitFee(String.valueOf(iPlanAccount.getExitFee()/100.0));
                        //实际到账金额
                        Integer sunMoney = getExitAmt(iPlan,userId, iPlanAccount.getCurrentPrincipal(),iPlan.getRaiseCloseTime());//用的是当前的本金
                        detail.setActualMoney((sunMoney-iPlanAccount.getExitFee())/100.0);


                        //退出时间
                        if (Objects.equals(IPlanAccount.STATUS_ADVANCED_EXIT, iPlanAccount.getStatus())) {
                            detail.setTime(iPlanTransLog.getTransTime());//提前退出
                        } else {
                            detail.setTime(iPlan.getEndTime());//正常退出
                        }
                        //退出方式
                        if (Objects.equals(iPlanAccount.getStatus(), IPlanAccount.STATUS_ADVANCED_EXIT)) {
                            detail.setExitWay(IPlanInvestManageDto.WAY_ADVANCED_EXIT);
                        } else if (Objects.equals(iPlanAccount.getStatus(), IPlanAccount.STATUS_NORMAL_EXIT)) {
                            detail.setExitWay(IPlanInvestManageDto.WAY_NORMAL_EXIT);
                        }
                        detail.setCreditMatched(IPlanInvestManageDto.CREDIT_MATCHED_Y);
                        detail.setUrl(iPlanAccount.getServiceContract());

                        // 月月盈提前还款
                        if (IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())) {
                            detail.setBeforeRepayFlag(BaseIPlanDto.BEFORE_REPAY_FLAG_Y);
                        }
                        if("1".equals(iPlan.getInterestAccrualType()+"")){
                        	detail.setRepayType("一次性还本付息");
                        }else{
                        	 detail.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
                        }
                        details.add(detail);
                    }
                }
                investManageDto.setDetails(details);
                return new RestResponseBuilder<>().success(investManageDto);
            }

        }

        return new RestResponseBuilder<>().success(null);
    }


    /**
     * PC端-理财计划投资详情页（兼容转让中详情，持有中详情，已完成详情）
     * @param id    每个页面的id不同，在投资管理页面中传过来的
     */
    @GetMapping("/iplan/invest/detail/{userId}/{type}/{id}")
    public RestResponse getInvestDetailPage(@PathVariable("userId") String userId,
                                            @PathVariable("type") int type,
                                            @PathVariable("id") Integer id) {

        IPlanAppInvestDetailDto investDetailDto = new IPlanAppInvestDetailDto();

        if (type == IPlanInvestManageDto.PAGE_TYPE_HOLDING) {

            IPlan iPlan = iPlanService.getIPlanById(id);
            if (null == iPlan) {
                throw new ProcessException(Error.NDR_0428);
            }
            IPlanAccount iPlanAccount =  iPlanAccountService.getByIPlanIdAndUserId(id, userId);
            if (null == iPlanAccount) {
                throw new ProcessException(Error.NDR_0452);
            }
            investDetailDto.setServiceContract(iPlanAccount.getServiceContract());
            investDetailDto.setDay(iPlan.getDay());
            investDetailDto.setInterestAccrualType(iPlan.getInterestAccrualType());
            investDetailDto.setiPlanId(id);
            investDetailDto.setName(iPlan.getName());
            investDetailDto.setLockDays(String.valueOf(iPlan.getExitLockDays()) + "天");
            investDetailDto.setExpectedInterest(df4.format((iPlanAccount.getExpectedInterest()+iPlanAccount.getIplanExpectedBonusInterest())/100.0));
            double totalRate = iPlan.getFixRate().doubleValue()
                    + (iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue(): 0);
            if(iPlanAccountService.isNewIplan(iPlan)){
                totalRate = iPlan.getFixRate().doubleValue()
                        + (iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue(): 0);
            }
            investDetailDto.setNewLock(iPlanAccountService.getNewLock(iPlan));
            ActivityMarkConfigure activityMarkConfigure = null;
            if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                    totalRate = iPlan.getFixRate().doubleValue();
                    if(iPlanAccountService.isNewIplan(iPlan)){
                        totalRate = iPlan.getFixRate().doubleValue();
                        if(iPlan.getRaiseFinishTime()!=null){
                            investDetailDto.setNewRate(df.format((iPlan.getFixRate().doubleValue())*100)+"%"+"-"+df.format((iPlan.getFixRate().doubleValue()+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+"%");
                        }
                    }
                }
            }
            investDetailDto.setFixRate(df2.format(totalRate * 100) + "%");
            investDetailDto.setTerm(String.valueOf(iPlan.getTerm()) + "个月");
            investDetailDto.setNewRate(df2.format(totalRate * 100) + "%");
            //锁定期结束时间=实际结束募集时间+锁定期天数
            if(iPlan.getRaiseFinishTime()==null){
                investDetailDto.setLockEndTime("生成中");
                investDetailDto.setEndTime("生成中");
            }else{
                LocalDate raiseCloseDate = DateUtil.parseDate(iPlan.getRaiseFinishTime(), DateUtil.DATE_TIME_FORMATTER_19);
                LocalDate lockEndDate = raiseCloseDate.plusDays(iPlan.getExitLockDays());
                investDetailDto.setLockEndTime(lockEndDate.toString());
                investDetailDto.setEndTime(iPlan.getEndTime().toString().substring(0,10));
                investDetailDto.setDaysToTransfer((int)DateUtil.betweenDays(LocalDate.now(), lockEndDate));
                if(iPlanAccountService.isNewIplan(iPlan)){
                    String date = (String) iPlanAccountService.getMax(iPlanAccount).get("date");
                    if(!"0".equals(date)){
                        LocalDate localDate = DateUtil.parseDate(date, DateUtil.DATE_TIME_FORMATTER_8);
                        investDetailDto.setEndTime(localDate.toString());
                    }
                }
                if(iPlanAccountService.isNewIplan(iPlan)){
                    investDetailDto.setNewRate(df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue())*100)+"%"+"-"+df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue()+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+"%");
                    investDetailDto.setTerm(((int)iPlanAccountService.getMax(iPlanAccount).get("term")==0?iPlan.getTerm():(int)iPlanAccountService.getMax(iPlanAccount).get("term"))+ "个月");
                }
            }
            investDetailDto.setAmt(df.format(iPlanAccount.getCurrentPrincipal() / 100.0) + "元");
            investDetailDto.setPaidInterest(df.format(iPlanAccount.getPaidInterest() / 100.0) + "元");
            investDetailDto.setRepayType(SubjectMobileResource.repayType(iPlan.getRepayType()));
            //若是一键投
            if(iPlan.getIplanType().equals(IPlan.IPLAN_TYPE_YJT)){
                //新版省心投相关
                if(iPlanAccountService.isNewIplan(iPlan)){
                    investDetailDto.setIsFree(IPlanAppInvestManageHoldDto.IS_FREE);
                    if(iPlan.getRaiseFinishTime() != null){
                        investDetailDto.setFixRate(df4.format((totalRate) * 100) + "%"+"-"+df4.format((totalRate+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+ "%");
                        if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null){
                            investDetailDto.setFixRate(df4.format((iPlan.getFixRate().doubleValue()) * 100) + "%"+"-"+df4.format((iPlan.getFixRate().doubleValue()+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+ "%");
                        }
                    }
                    investDetailDto.setFixRate(df2.format(totalRate * 100) + "%");
                }
                //查询对应散标信息
                List<IPlanAppInvestDetailDto.Detail> details = new ArrayList<>();
                List<Credit> credits = creditService.findByUserIdAndSourceChannelAndAccountId(iPlanAccount.getUserId(),Credit.SOURCE_CHANNEL_YJT,iPlanAccount.getId(), Credit.CREDIT_STATUS_HOLDING);
                if(!credits.isEmpty()){
                    for (Credit credit:credits) {
                        IPlanAppInvestDetailDto.Detail detail = new IPlanAppInvestDetailDto.Detail();
                        Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
                        detail.setName(subject.getName());
                        detail.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
                        detail.setSubjectId(credit.getSubjectId());
                        detail.setTotalAmt(subject.getTotalAmt()/100.0);
                        detail.setCreditId(credit.getId());
                        details.add(detail);
                    }
                }
                investDetailDto.setDetailList(details);
                //查询对应加入记录
                List<IPlanTransLog> joinTransLogList = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_TO_CONFIRM,IPlanTransLog.TRANS_STATUS_PROCESSING,IPlanTransLog.TRANS_STATUS_SUCCEED)));
                List<IPlanAppInvestDetailDto.IPlanJoinTransLog> joinTransLogs = new ArrayList<>();
                for (IPlanTransLog translog:joinTransLogList) {
                    IPlanAppInvestDetailDto.IPlanJoinTransLog transLog = new IPlanAppInvestDetailDto.IPlanJoinTransLog();
                    transLog.setJoinAmt(translog.getTransAmt()/100.0);
                    transLog.setJoinTime(translog.getCreateTime());
                    if(translog.getRedPacketId()!=null && translog.getRedPacketId()>0){
                        RedPacket redPacket = redPacketService.getRedPacketById(translog.getRedPacketId());
                        String redPacketMsg = "";
                        if (redPacket != null) {
                            if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                                redPacketMsg = df2.format(redPacket.getMoney())
                                        + "元现金券";
                            } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                                redPacketMsg = df2.format(redPacket.getRate() * 100)
                                        + "%加息券";
                            } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                                redPacketMsg = df2.format(redPacket.getMoney())
                                        + "元抵扣券";
                            } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                                redPacketMsg = df2.format(redPacket.getRate() * 100)
                                        + "%"+redPacket.getRateDay()+"天加息券";
                            }
                        }
                        transLog.setRedRemark(redPacketMsg);
                    }
                    String statusMsg = "";
                    if(translog.getTransStatus()==1 || translog.getTransStatus()==0){
                        statusMsg="成功";
                    }else if(translog.getTransStatus()==4){
                        statusMsg="待匹配";
                    }
                    transLog.setStatus(statusMsg);
                    joinTransLogs.add(transLog);
                }
                investDetailDto.setJoinTransLogs(joinTransLogs);
                Integer totalTransferAmt=0;//转让总金额
                //查询对应的转出记录(债权转让,债权撤销)
                List<IPlanTransLog> outTransLogList = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING,IPlanTransLog.TRANS_STATUS_SUCCEED)));
                List<IPlanAppInvestDetailDto.IPlanOutTransLog> outTransLogs = new ArrayList<>();
                if(!outTransLogList.isEmpty()){
                    for (IPlanTransLog translog:outTransLogList) {
                        IPlanAppInvestDetailDto.IPlanOutTransLog transLog = new IPlanAppInvestDetailDto.IPlanOutTransLog();
                        transLog.setTransferAmt(translog.getTransAmt()/100.0);
                        transLog.setTransferTime(translog.getCreateTime());
                        transLog.setTransLogId(translog.getId());
                        transLog.setTransferFee(iPlanAccountService.calcTotalFee(translog.getId()));
                        transLog.setHaveDealAmt( iPlanAccountService.calcDealAmt(translog.getId())/100.0);
                        String statusMsg = "";
                        if(translog.getTransStatus()==4 || translog.getTransStatus()==0){
                            statusMsg="转让中";
                        }else if(translog.getTransStatus()==1){
                            if(translog.getTransType() == 10 && translog.getActualAmt()!=0 && (!translog.getTransAmt().equals(translog.getProcessedAmt()))){
                                statusMsg="部分完成";
                            }else if(translog.getTransType() == 10 && translog.getActualAmt()==0){
                                statusMsg="全部撤销";
                            }else{
                                statusMsg="已完成";
                            }
                        }
                        transLog.setStatusStr(statusMsg);
                        transLog.setStatus(iPlanAccountService.cancelStatus(translog.getId()));
                        outTransLogs.add(transLog);
                        if(IPlanTransLog.TRANS_STATUS_PROCESSING.equals(translog.getTransStatus())){
                            totalTransferAmt += translog.getTransAmt();
                        }else{
                            totalTransferAmt += translog.getProcessedAmt();
                        }
                    }
                }
                investDetailDto.setOutTransLogs(outTransLogs);
                //转让中金额
                investDetailDto.setTransferingAmt(totalTransferAmt/100.0);
                //红包奖励
                InvestRedpacket investRedpacket = redPacketService.getReceivedRedPacketAmt(userId, String.valueOf(iPlanAccount.getIplanId()), RedPacket.INVEST_REDPACKET_TYPE);
                if (investRedpacket != null && investRedpacket.getRewardMoney() != 0 && investRedpacket.getSendRedpacketTime() != null) {
                    investDetailDto.setRedAmt(ArithUtil.round(investRedpacket.getRewardMoney(), 2) );
                    investDetailDto.setRedTime(DateUtil.SDF_10.format(investRedpacket.getSendRedpacketTime()));
                    investDetailDto.setStatus(1);
                }
                investDetailDto.setTransFlag(iPlanAccountService.checkCondition(iPlanAccount));
                investDetailDto.setMessage(iPlanAccountService.checkConditionStr(iPlanAccount));
                investDetailDto.setAccountId(iPlanAccount.getId());
            }

        } else if (type == IPlanInvestManageDto.PAGE_TYPE_TRANSFERRING) {
            IPlanTransLog iPlanTransLog = iPlanTransLogService.getById(id);
            if (null == iPlanTransLog) {
                throw new ProcessException(Error.NDR_0428);
            }
            IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
            if (null == iPlan) {
                throw new ProcessException(Error.NDR_0428);
            }
            investDetailDto.setDay(iPlan.getDay());
            investDetailDto.setInterestAccrualType(iPlan.getInterestAccrualType());
            investDetailDto.setAmt(df.format(iPlanTransLog.getTransAmt() / 100.0) + "元");//转让总额（元）
            investDetailDto.setTransferTime(iPlanTransLog.getTransTime());//申请转让时间
            //转出费用
            IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
            investDetailDto.setServiceContract(iPlanAccount.getServiceContract());
            double exitFee = iPlanAccount.getExitFee() / 100.0;
            investDetailDto.setExpectedAmt(df.format(iPlanTransLog.getTransAmt() / 100.0 - exitFee));//预计到账
            investDetailDto.setTransferFee(df.format(exitFee));

            //持有天数
            LocalDate raiseCloseDate = DateUtil.parseDate(iPlan.getRaiseCloseTime(), DateUtil.DATE_TIME_FORMATTER_19);//募集结束时间
            //申请转让时间
            String transTime = iPlanTransLog.getTransTime();
            LocalDate localDateTransfer = DateUtil.parseDate(transTime, DateUtil.DATE_TIME_FORMATTER_19);//转出时间
            LocalDate localDateJoin = DateUtil.parseDate(iPlanAccount.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19);//开放募集时间
            long holdingDays = DateUtil.betweenDays(localDateJoin, localDateTransfer);
            investDetailDto.setHoldingDays((int)holdingDays);
            LocalDate iPlanEndTime = DateUtil.parseDate(iPlan.getEndTime(), DateUtil.DATE_TIME_FORMATTER_19);//计划结束时间
            //剩余天数
            long restDays = DateUtil.betweenDays(localDateTransfer, iPlanEndTime);
            investDetailDto.setRestDays((int)restDays);

        } else if (type == IPlanInvestManageDto.PAGE_TYPE_FINISH) {

            IPlanAccount iPlanAccount =  iPlanAccountService.getByIPlanIdAndUserId(id, userId);
            if (null == iPlanAccount) {
                logger.warn("用户[userId={}]未投资此定期理财计划[id={}]",userId,id);
                throw new ProcessException(Error.NDR_0452);
            }
            IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
            if (null == iPlan) {
                logger.warn("数据错误，用户[userId={}]的定期理财计划[accountId={}]，没有对应的理财计划[iPlanId={}]",userId,iPlanAccount.getId(),iPlanAccount.getIplanId());
                throw new ProcessException(Error.NDR_DATA_ERROR);
            }
            investDetailDto.setServiceContract(iPlanAccount.getServiceContract());
            if (iPlan.getIplanType().equals(IPlan.IPLAN_TYPE_YJT)) {
                //一键投
                YjtInvestDetailFinishDto dto = new YjtInvestDetailFinishDto();
                dto.setServiceContract(iPlanAccount.getServiceContract());
                dto.setId(iPlanAccount.getId());
                dto.setIplanId(iPlan.getId());
                dto.setName(iPlan.getName());
                dto.setRate(df.format((iPlan.getFixRate().doubleValue() + (iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue(): 0)) * 100) + "%");
                ActivityMarkConfigure activityMarkConfigure = null;
                if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                    activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                    if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null) {
                        dto.setRate(df.format((iPlan.getFixRate().doubleValue()) * 100) + "%");
                    }
                }
                dto.setLockDays(String.valueOf(iPlan.getExitLockDays()) + "天");
                dto.setInitPrincipal(df.format(iPlanAccount.getInitPrincipal() / 100.0));
                dto.setTerm(String.valueOf(iPlan.getTerm()) + "个月");
                if(iPlan.getRaiseFinishTime()!=null){
                    if(iPlanAccountService.isNewIplan(iPlan)){
                        dto.setRate(df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue())*100)+"%"+"-"+df.format((iPlan.getFixRate().doubleValue()+iPlan.getBonusRate().doubleValue()+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+"%");
                        dto.setTerm(((int)iPlanAccountService.getMax(iPlanAccount).get("term")==0?iPlan.getTerm():(int)iPlanAccountService.getMax(iPlanAccount).get("term"))+ "个月");
                    }
                }
                //锁定期结束时间=实际结束募集时间+锁定期天数
                String raiseCloseTime = iPlan.getRaiseFinishTime() == null ? iPlan.getRaiseCloseTime():iPlan.getRaiseFinishTime();
                LocalDate raiseCloseDate = DateUtil.parseDate(raiseCloseTime, DateUtil.DATE_TIME_FORMATTER_19);
                LocalDate lockEndDate = raiseCloseDate.plusDays(iPlan.getExitLockDays());
                dto.setLockEndTime(lockEndDate.toString());
                dto.setRepayType(SubjectMobileResource.repayType(iPlan.getRepayType()));
                dto.setCreateTime(iPlanAccount.getCreateTime());
                dto.setNewLock(iPlanAccountService.getNewLock(iPlan));
                List<YjtInvestDetailFinishDto.YjtInvest> yjtInvests = new ArrayList<>();
                List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_TO_CONFIRM,IPlanTransLog.TRANS_STATUS_PROCESSING,IPlanTransLog.TRANS_STATUS_SUCCEED)));
                if (iPlanTransLogs != null && iPlanTransLogs.size() > 0) {
                    for (IPlanTransLog log : iPlanTransLogs) {
                        YjtInvestDetailFinishDto.YjtInvest yjtInvest = new YjtInvestDetailFinishDto.YjtInvest();
                        yjtInvest.setTransAmt(df.format(log.getTransAmt() / 100.0));
                        yjtInvest.setTransTime(log.getTransTime());
                        yjtInvest.setStatus(IPlanTransLog.getTransStatus(log.getTransStatus()));
                        String redpackeMsg = "";
                        if (log.getRedPacketId() != null && log.getRedPacketId() > 0) {
                            RedPacket redPacket = redPacketService.getRedPacketById(log.getRedPacketId());
                            if (redPacket != null) {
                                if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                                    redpackeMsg = ArithUtil.round(redPacket.getMoney(),2) + "元现金券";
                                } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                                    redpackeMsg = ArithUtil.round(redPacket.getRate() * 100,2)
                                            + "%加息券";
                                } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                                    redpackeMsg = ArithUtil.round(redPacket.getMoney(),2)
                                            + "元抵扣券";
                                } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                                    redpackeMsg = ArithUtil.round(redPacket.getRate() * 100,2)
                                            + "%"+redPacket.getRateDay()+"天加息券";
                                }
                            }
                        }
                        yjtInvest.setRedPacketMsg(redpackeMsg);
                        yjtInvests.add(yjtInvest);
                    }
                }
                dto.setInvestList(yjtInvests);
                List<YjtInvestDetailFinishDto.YjtTransfer> yjtTransfers = new ArrayList<>();
                //转让交易记录
                List<IPlanTransLog> transLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED))).stream().filter(iPlanTransLog -> iPlanTransLog.getActualAmt() > 0).collect(Collectors.toList());
                int transferTotalAmt = 0;
                Double transferTotalFee = 0.0;
                if (transLogs != null && transLogs.size() > 0) {
                    for (IPlanTransLog transLog : transLogs) {
                        transferTotalAmt += transLog.getProcessedAmt();
                        Double fee = iPlanAccountService.calcTotalFee(transLog.getId());
                        transferTotalFee += fee;
                        YjtInvestDetailFinishDto.YjtTransfer yjtTransfer = new YjtInvestDetailFinishDto.YjtTransfer();
                        yjtTransfer.setTransTime(transLog.getTransTime());
                        yjtTransfer.setTransAmt(df.format(transLog.getProcessedAmt() / 100.0));
                        yjtTransfer.setTransferFee(df4.format(fee));
                        yjtTransfer.setActualAmt(df.format(transLog.getActualAmt() / 100.0));
                        yjtTransfer.setStatusStr(IPlanTransLog.getTransStatus(transLog.getTransStatus()));
                        yjtTransfer.setStatus(transLog.getTransStatus());
                        yjtTransfer.setTransLogId(transLog.getId());
                        yjtTransfers.add(yjtTransfer);
                    }
                }
                dto.setTransferAmt(df.format(transferTotalAmt / 100.0));
                dto.setTransferFee(df.format(transferTotalFee / 100.0));
                dto.setTransferList(yjtTransfers);
                dto.setRewardTotalAmt(df.format(iPlanAccount.getIplanPaidBonusInterest() / 100.0));
                List<YjtInvestDetailFinishDto.YjtSubject> yjtSubjects = new ArrayList<>();
                List<Credit> creditList = creditService.findByUserIdAndSourceChannelAndAccountId(iPlanAccount.getUserId(),Credit.SOURCE_CHANNEL_YJT,iPlanAccount.getId(), Credit.CREDIT_STATUS_FINISH);
                for (Credit credit:creditList) {
                    Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
                    YjtInvestDetailFinishDto.YjtSubject yjtSubject = new YjtInvestDetailFinishDto.YjtSubject();
                    yjtSubject.setSubjectId(credit.getSubjectId());
                    yjtSubject.setSubjectName(subject.getName());
                    yjtSubject.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
                    yjtSubject.setBorrowAmt(df.format(subject.getTotalAmt()/100.0));
                    yjtSubject.setCreditId(credit.getId());
                    yjtSubjects.add(yjtSubject);
                }
                dto.setSubjectList(yjtSubjects);
                return new RestResponseBuilder<>().success(dto);
            } else {
                //月月盈
                investDetailDto.setiPlanId(iPlan.getId());
                investDetailDto.setDay(iPlan.getDay());
                investDetailDto.setInterestAccrualType(iPlan.getInterestAccrualType());
                investDetailDto.setDay(iPlan.getDay());
                investDetailDto.setInterestAccrualType(iPlan.getInterestAccrualType());
                investDetailDto.setName(iPlan.getName());
                investDetailDto.setTxnTime(iPlanAccount.getCreateTime());
                investDetailDto.setLockDays(String.valueOf(iPlan.getExitLockDays()) + "天");
                investDetailDto.setFixRate(iPlan.getFixRate().doubleValue() * 100 + "%");
                investDetailDto.setTerm(String.valueOf(iPlan.getTerm()) + "个月");
                investDetailDto.setAmt(df.format(iPlanAccount.getCurrentPrincipal() / 100.0) + "元");
                //锁定期结束时间=实际结束募集时间+锁定期天数
                String raiseCloseTime = iPlan.getRaiseFinishTime() == null ? iPlan.getRaiseCloseTime():iPlan.getRaiseFinishTime();
                LocalDate raiseCloseDate = DateUtil.parseDate(raiseCloseTime, DateUtil.DATE_TIME_FORMATTER_19);
                LocalDate lockEndDate = raiseCloseDate.plusDays(iPlan.getExitLockDays());
                investDetailDto.setLockEndTime(lockEndDate.toString());

                IPlanTransLog transLog = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iPlan.getIplanType(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT, IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN))
                        , new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED))).stream().filter(iPlanTransLog -> iPlanTransLog.getAccountId().equals(iPlanAccount.getId())).collect(Collectors.toList()).get(0);
                Integer allIPlanExitAmt = transLog.getTransAmt();
                Integer exitFee = iPlanAccount.getExitFee();
                Integer iPlanExitAmt = allIPlanExitAmt - exitFee;
                investDetailDto.setActualTransferReturn(df.format(iPlanExitAmt / 100.0));

                LocalDate localDateJoin = DateUtil.parseDate(iPlan.getRaiseOpenTime(), DateUtil.DATE_TIME_FORMATTER_19);//开放募集时间
                LocalDate localDateTransfer = DateUtil.parseDate(transLog.getTransTime(), DateUtil.DATE_TIME_FORMATTER_19);//转出时间
                LocalDate localDate = localDateJoin.plusMonths(iPlan.getTerm());
                long holdingDays = DateUtil.betweenDays(localDateJoin, localDateTransfer);
                LocalDate iPlanEndTime = DateUtil.parseDate(iPlan.getEndTime(), DateUtil.DATE_TIME_FORMATTER_19);//计划结束时间
                //剩余天数
                long restDays = DateUtil.betweenDays(localDateTransfer, iPlanEndTime);
                investDetailDto.setHoldingDays((int) holdingDays);
                if (Objects.equals(IPlanAccount.STATUS_ADVANCED_EXIT, iPlanAccount.getStatus())) {
                    investDetailDto.setRestDays((int) restDays);
                } else {
                    investDetailDto.setRestDays(0);
                }
                investDetailDto.setTransferFee(df.format(iPlanAccount.getExitFee()/ 100.0));
                investDetailDto.setStatus(iPlanAccount.getStatus());
            }

        } else {
            throw new IllegalArgumentException("参数type不支持！");
        }

        return new RestResponseBuilder<IPlanAppInvestDetailDto>().success(investDetailDto);
    }


    //根据code查询IPlan详情
    @RequestMapping(value = "/iplan/{code}", method = RequestMethod.GET)
    public RestResponse<IPlan> getIPlanByCode(@PathVariable("code") String code) {
        IPlan iPlan=iPlanService.getByCode(code);
        if (iPlan == null) {
            return new RestResponseBuilder<IPlan>().success(null);
        }
        return new RestResponseBuilder<IPlan>().success(iPlan);
    }



    //修改IPlan信息
    @PostMapping("/iplan/update")
    public RestResponse<IPlanDto> updateIPlan(@RequestBody IPlanDto iPlanDto) {
        //查询如果存在并且还没推送存管通的就可以删除
        IPlan oldIPlan = iPlanService.getIPlanById(iPlanDto.getId());
        if (oldIPlan != null) {
            if (IPlan.IPLAN_TYPE_YJT.equals(oldIPlan.getIplanType())) {
                if ((oldIPlan.getIsRedis()== null ||IPlan.REDIS_FALSE.equals(oldIPlan.getIsRedis())) && IPlan.REDIS_TRUE.equals( iPlanDto.getIsRedis()) && oldIPlan.getPushStatus().equals(IPlan.PUSH_STATUS_N)) {
                    redisClient.product(GlobalConfig.DOUBLE_11_IPLAN, String.valueOf(iPlanDto.getId()));
                    try {
                        redisClient.set(GlobalConfig.IPLANID_TO_CODE + iPlanDto.getCode(), String.valueOf(iPlanDto.getId()));
                    } catch (Exception e) {

                    }
                }
            }
        }
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN,0,-1);
        if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanDto.getId()))){
            redisClient.del(GlobalConfig.IPLAN_REDIS+iPlanDto.getId());
        }
        IPlan iPlan = new IPlan();
        BeanUtils.copyProperties(iPlanDto, iPlan);
        iPlanService.update(iPlan);
        return new RestResponseBuilder<IPlanDto>().success(iPlanDto);
    }

    //根据id删除没有推送的计划
    @PostMapping("/iplan/delete/{iPlanId}")
    public RestResponse<IPlanDto> deleteIPlan(@PathVariable("iPlanId") String iPlanId) {
        IPlan iPlan = iPlanService.delete(iPlanId);
        IPlanDto iPlanDto = new IPlanDto();
        BeanUtils.copyProperties(iPlanDto, iPlan);
        return new RestResponseBuilder<IPlanDto>().success(iPlanDto);
    }

    private Integer getExitAmt(IPlan iPlan, String userId, Integer principal, String raiseCloseTime) {
        String raiseCloseDate = raiseCloseTime.substring(0, 10).replace("-", "");
        List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailDao.findByUserIdAndIPlanId(userId, iPlan.getId());
        Collections.sort(iPlanRepayDetails,Comparator.comparing(IPlanRepayDetail::getTerm).reversed());
        for(IPlanRepayDetail iPlanRepayDetail:iPlanRepayDetails){
            if(iPlanRepayDetail.getStatus().equals(IPlanRepayDetail.STATUS_REPAY_FINISH)){
                raiseCloseDate = iPlanRepayDetail.getDueDate().replaceAll("-","");
                break;
            }
        }
        long iPlanHoldingDays = DateUtil.betweenDays(raiseCloseDate, DateUtil.getCurrentDateShort());
        if (iPlanHoldingDays < 0) {
            throw new ProcessException(Error.NDR_DATA_ERROR.getCode(), "iPlanHoldingDays is negative");
        }
        return FinanceCalcUtils.calcPrincipalInterest(principal, iPlan.getFixRate().add(iPlan.getBonusRate()), (int) iPlanHoldingDays);
    }

    @GetMapping("/iplan/invest/toConfirm")
    public RestResponse getConfirmDetail(@RequestParam("userId") String userId, @RequestParam("iplanId") int iplanId) {
        if (org.apache.commons.lang3.StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        if (iplanId == 0) {
            throw new IllegalArgumentException("iplanId不能为空");
        }
        //查询用户一期月月盈是否有待确认记录
        List<IPlanTransLog> confirmTransLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId, iplanId,
                new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_TO_CONFIRM)));
        List<IPlanAppInvestToConfirmDto> details = new ArrayList<>();
        if (confirmTransLogs != null && confirmTransLogs.size() > 0) {
            for (IPlanTransLog transLog : confirmTransLogs) {
                IPlan iPlan = iPlanService.getIPlanById(transLog.getIplanId());
                IPlanAppInvestToConfirmDto detail = new IPlanAppInvestToConfirmDto();
                detail.setTransLogId(transLog.getId());
                detail.setMoney(Double.toString(ArithUtil.round(transLog.getTransAmt()/100.0,2)));
                double addRate = 0;
                if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                    ActivityMarkConfigure configure = activityMarkConfigureService.findById(iPlan.getActivityId());
                    if (configure != null && configure.getIncreaseInterest() != null && configure.getIncreaseInterest() > 0) {
                        addRate = configure.getIncreaseInterest();
                    }
                }
                double interest = (transLog.getTransAmt()/100.0) * (iPlan.getFixRate().doubleValue() + addRate/100) / 12 * iPlan.getTerm();
                detail.setInterest(Double.toString(ArithUtil.round(interest, 2)));
                detail.setTransTime(transLog.getTransTime());
                Long countDown = 0L;
                try {
                    Date transTime = sdf1.parse(transLog.getTransTime());
                    Date countDownTime = DateUtils.addMinutes(transTime, 3);
                    if (countDownTime.getTime() >= (new Date()).getTime()) {
                        countDown = countDownTime.getTime() - (new Date()).getTime();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                detail.setCountDown(Long.toString(countDown));
                if (transLog.getRedPacketId() != null && transLog.getRedPacketId() > 0) {
                    RedPacket redPacket = redPacketService.getRedPacketById(transLog.getRedPacketId());
                    String redpackeMsg = "";
                    if (redPacket != null) {
                        if (RedPacket.TYPE_MONEY.equals(redPacket.getType())) {
                            redpackeMsg = ArithUtil.round(redPacket.getMoney(),2) + "元现金券";
                        } else if (RedPacket.TYPE_RATE.equals(redPacket.getType())) {
                            redpackeMsg = ArithUtil.round(redPacket.getRate() * 100,2)
                                    + "%加息券";
                        } else if (RedPacket.TYPE_DEDUCT.equals(redPacket.getType())) {
                            redpackeMsg = ArithUtil.round(redPacket.getMoney(),2)
                                    + "元抵扣券";
                        } else if (RedPacket.TYPE_RATE_BY_DAY.equals(redPacket.getType())) {
                            redpackeMsg = ArithUtil.round(redPacket.getRate() * 100,2)
                                    + "%"+redPacket.getRateDay()+"天加息券";
                        }
                    }
                    detail.setRedPacketDesc(redpackeMsg);
                }
                details.add(detail);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("data", details);
        return new RestResponseBuilder<>().success(data);
    }

    //强制满标
    @PostMapping("/iplan/mandatoryIPlan")
    public RestResponse<IPlanDto> mandatoryIPlan(@RequestBody IPlanDto iPlanDto) {
        if (iPlanDto.getId() == 0) {
            throw new IllegalArgumentException("iplanId不能为空");
        }
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN,0,-1);
        if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanDto.getId()))){
            redisClient.del(GlobalConfig.IPLAN_REDIS+iPlanDto.getId());
        }
        IPlan iPlan = iPlanService.mandatoryIPlan(iPlanDto.getId());
        if(iPlan!=null) {
            BeanUtils.copyProperties(iPlan, iPlanDto);
        }else
        {
            iPlanDto=null;
        }
        return new RestResponseBuilder<IPlanDto>().success(iPlanDto);
    }
}
