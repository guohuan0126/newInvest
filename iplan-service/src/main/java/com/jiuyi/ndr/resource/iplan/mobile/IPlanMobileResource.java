package com.jiuyi.ndr.resource.iplan.mobile;

import com.github.pagehelper.PageInfo;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.credit.CreditOpeningDao;
import com.jiuyi.ndr.dao.iplan.IPlanRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectRepayDetailDao;
import com.jiuyi.ndr.dao.subject.SubjectTransferParamDao;
import com.jiuyi.ndr.domain.account.UserAccount;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.credit.Credit;
import com.jiuyi.ndr.domain.credit.CreditOpening;
import com.jiuyi.ndr.domain.iplan.*;
import com.jiuyi.ndr.domain.redpacket.ActivityMarkConfigure;
import com.jiuyi.ndr.domain.subject.Subject;
import com.jiuyi.ndr.domain.subject.SubjectTransferParam;
import com.jiuyi.ndr.domain.user.RedPacket;
import com.jiuyi.ndr.dto.credit.mobile.AppCreditTransferDetailDto;
import com.jiuyi.ndr.dto.credit.mobile.YjtCreditManageTransferDto;
import com.jiuyi.ndr.dto.iplan.YjtInvestDetailFinishDto;
import com.jiuyi.ndr.dto.iplan.YjtInvestManageFinishDto;
import com.jiuyi.ndr.dto.iplan.mobile.*;
import com.jiuyi.ndr.dto.subject.SubjectYjtDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.resource.subject.mobile.SubjectMobileResource;
import com.jiuyi.ndr.rest.RestResponse;
import com.jiuyi.ndr.rest.RestResponseBuilder;
import com.jiuyi.ndr.rest.page.IPlanListPageData;
import com.jiuyi.ndr.service.account.UserAccountService;
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
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author ke 2017/6/15
 */
@RestController
public class IPlanMobileResource {

    private final static Logger logger = LoggerFactory.getLogger(IPlanMobileResource.class);

    @Autowired
    private IPlanService iPlanService;
    @Autowired
    private UserAccountService userAccountService;
    @Autowired
    private ActivityMarkConfigureService activityMarkConfigureService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private IPlanParamService iPlanParamService;
    @Autowired
    private InvestService investService;
    @Autowired
    private RedPacketService redPacketService;
    @Autowired
    private IPlanRepayScheduleService iPlanRepayScheduleService;
    @Autowired
    private IPlanRepayDetailService iPlanRepayDetailService;
    @Autowired
    private IPlanRepayDetailDao iPlanRepayDetailDao;
    @Autowired
    private MarketService marketService;
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepayDetailDao subjectRepayDetailDao;
    @Autowired
    private CreditService creditService;
    @Autowired
    private CreditOpeningDao creditOpeningDao;
    @Autowired
    private SubjectTransferParamDao subjectTransferParamDao;
    @Autowired
    private UserService userService;
    @Autowired
    private ConfigService configService;
    private static final Integer HOUR_TO_REMIND = 1;

    @Value("${duanrong.subject.transferUrl}")
    private String transferUrl;//转让协议
    @Value("${duanrong.subject.protocolUrl}")
    private String protocolUrl;

    private DecimalFormat df = new DecimalFormat("######0.00");
    private DecimalFormat df2 = new DecimalFormat("######0");
    private static DecimalFormat df3 = new DecimalFormat("0.####");
    private DecimalFormat df4 = new DecimalFormat("######0.##");
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Value("${iPlan.invest.investUserUrl}")
    private String investUserUrl;   //投资达人链接（dr服务端）
    @Value("${iPlan.invest.questionUrl}")
    private String questionUrl;   //问题链接（dr服务端）
    @Value("${iPlan.invest.shareLinkUrl}")
    private String shareLinkUrl;   //分享链接

    @Value("${duanrong.yjt.picUrl}")
    private String picUrl;
    @Value("${duanrong.yjt.redirectUrl}")
    private String redirectUrl;
    @Value("${duanrong.subject.creditDetailUrl}")
    private String creditDetailUrl;//债权明细

    private String imagePrefix = "https://duanrongweb.oss-cn-qingdao.aliyuncs.com";   //oss图片地址

    //还款方式
    public static String decideRepayType(String repayType) {
        if ("IFPA".equals(repayType)) {
            return "按月付息到期还本";
        } else if ("OTRP".equals(repayType)) {
            return "到期还本付息";
        } else if ("MCEI".equals(repayType)) {
            return "等额本息";
        } else {
            return "";
        }
    }

    //app-获取定期理财计划列表
    @GetMapping("/iplan/list/{userId}")
    public RestResponse getIPlan(   @PathVariable("userId") String userId,
                                    @RequestParam("pageNo") int pageNum,
                                    @RequestParam("pageSize") int pageSize,
                                    @RequestParam(value = "requestSource", required = false) String requestSource,
                                    @RequestParam(value = "iplanType", required = false, defaultValue = "0") int iplanType,
                                    @RequestParam(value = "yjtType", required = false, defaultValue = "0") int yjtType) {

        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }

        Double newbieUsable = investService.getNewbieUsable(userId,iplanType);
        List<IPlan> allVisiblePlan;
        if (newbieUsable <= 0) {
            if (yjtType > 0) {
                allVisiblePlan = iPlanService.findYjtNoNewBie(yjtType);
            } else {
                allVisiblePlan = iPlanService.findPlanNoNewBie(iplanType);
            }
        } else {
            if (yjtType > 0) {
                allVisiblePlan = iPlanService.findYjtNewBie(yjtType);
            } else {
                allVisiblePlan = iPlanService.findPlanNewBie(iplanType);
            }
        }
        // 获取用户当前VIP加息利率
        double iplanRate = marketService.getIplanVipRate(userId);

        //根据用户注册来源过滤渠道标
        /*allVisiblePlan = iPlanService.filterIplanByUserSource(allVisiblePlan, userId);*/

        List<IPlanAppListDto> iPlanList = new ArrayList<>();
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

            IPlanAppListDto iPlanListDto = new IPlanAppListDto();

            BeanUtils.copyProperties(iPlan, iPlanListDto);
            // 对省心投项目名称进行处理
            if (iPlan.getIplanType().equals(IPlan.IPLAN_TYPE_YJT)) {
                iPlanListDto.setName(IPlanAppListDto.dealName(iPlan));
                boolean dd=iPlanAccountService.isNewIplan(iPlan);
                iPlanListDto.setNewYjtFlag(dd?"1":"0");
            }
            //按天计息还是按月计息，天数，期数
           /* iPlanListDto.setInterestAccrualType(iPlan.getInterestAccrualType());//0月标1天标
            iPlanListDto.setDay(iPlan.getDay());//天数
            iPlanListDto.setTerm(iPlan.getTerm());//期数*/
            Integer activityId = iPlan.getActivityId();
            ActivityMarkConfigure activityMarkConfigure = null;
            if (null != activityId) {
                iPlanListDto.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                    iPlanListDto.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                }else {
                    iPlanListDto.setAddTerm(0);
                }
            }
            if (Objects.equals(iPlan.getNewbieOnly(), IPlan.NEWBIE_ONLY_Y)) {
                iPlanListDto.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                if(iPlan.getIplanType() == 2 && activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                    iPlanListDto.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                }
            }

            Double availableQuota = iPlan.getAvailableQuota() / 100.0;
            if (availableQuota < 10000) {
                iPlanListDto.setAvailableQuota(df2.format(availableQuota));
            } else {
                iPlanListDto.setAvailableQuota(df.format(availableQuota/10000.0)+ "万");
            }

            double fixRate = iPlan.getFixRate().doubleValue();
            double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;

            String vipFlag = iplanRate > 0 ? IPlanAppListDto.VIP_FLAG_Y : "";
            iPlanListDto.setVipFlag(vipFlag);
            String fixRateStr = df4.format(fixRate * 100);
            if (bonusRate > 0) {
                fixRateStr += "+" + df4.format(bonusRate * 100);
            }
            if (iplanRate > 0) {
                fixRateStr += "+" + df4.format(iplanRate * 100);
            }
            iPlanListDto.setFixRate(fixRateStr + "%");
            if (Objects.equals(iPlan.getStatus(), IPlan.STATUS_ANNOUNCING)) {
                String raiseOpenTime = iPlan.getRaiseOpenTime();
                LocalDate raiseOpenDate = DateUtil.parseDate(raiseOpenTime, DateUtil.DATE_TIME_FORMATTER_19);
                LocalDate now = LocalDate.now();
                if (raiseOpenDate.compareTo(now) == 0) {
                    //今日开售2017-06-17 16:12:30
                    iPlanListDto.setMillsTimeStr(raiseOpenTime.substring(11,16)+"开售");
                } else if (raiseOpenDate.compareTo(now) > 0) {
                    //募集开放时间在现在之后
                    LocalDate now1 = now.plusDays(1);
                    if (now1.compareTo(raiseOpenDate) > 0) {
                        //募集期 今日明日之间
                        iPlanListDto.setMillsTimeStr("明日开售");
                    } else if (now1.compareTo(raiseOpenDate) < 0) {
                        //募集期 明日后
                        iPlanListDto.setMillsTimeStr(Integer.valueOf(raiseOpenTime.substring(5,7))+"月"+raiseOpenTime.substring(8,10)+"日开售");
                    } else {
                        //募集期=今日
                        iPlanListDto.setMillsTimeStr(raiseOpenTime.substring(11,16)+"开售");
                    }
                } else {
                    //募集开放时间在现在之前
                    iPlanListDto.setMillsTimeStr("加入时间："+raiseOpenTime);
                }
            }

          //返回锁定期
            if(iPlan.getExitLockDays()!=null){
            	if (IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())&&"1".equals(iPlan.getInterestAccrualType()+"")){
            		iPlanListDto.setExitLockDaysStr("锁定期"+iPlan.getExitLockDays()+"天");
                }else{
                	if(iPlan.getExitLockDays()<31){
                		iPlanListDto.setExitLockDaysStr("锁定期"+iPlan.getExitLockDays()+"天");
                	}else{
                		int month=iPlan.getExitLockDays()/31;
                		iPlanListDto.setExitLockDaysStr("锁定期"+month+"个月");
                	}
                }
            }

            //前端显示期限格式
            iPlanListDto.setTermStr(iPlanListDto.getTerm()+"个月");
            if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
            	/*if("1".equals(iPlan.getInterestAccrualType()+"")){
            		iPlanListDto.setName("月月盈"+iPlan.getDay()+"天");
            	}else{
            		iPlanListDto.setName("月月盈"+iPlan.getTerm()+"个月");
            	}*/

            	 //新老版本兼容，5.6.2版本之前剔除月月盈天标
                if("1".equals(iPlan.getInterestAccrualType()+"")){
                	iPlanListDto.setTermStr(iPlanListDto.getDay()+"天");
    	            if(StringUtils.isBlank(requestSource)||StringUtils.isBlank(requestSource)||(requestSource.contains("android")&&requestSource.compareTo("android5.6.2")<=0)||
    	            		(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
    	            	continue;
    	        	}
                }

            }else{
                if(iPlanAccountService.isNewIplan(iPlan)){
                    if(StringUtils.isBlank(requestSource)||StringUtils.isBlank(requestSource)||(requestSource.contains("android")&&requestSource.compareTo("android5.7.0")<=0)||
                            (requestSource.contains("ios")&&requestSource.compareTo("ios_5.7.0")<0)){
                        continue;
                    }
                }
            	/*iPlanListDto.setName("省心投"+iPlan.getTerm()+"个月");*/
            }
            iPlanList.add(iPlanListDto);
        }
        List<IPlan> pageList = new PageUtil().ListSplit(iPlanList, pageNum, pageSize);
        IPlanListPageData<IPlanAppListDto> pageData = new IPlanListPageData<>();
        pageData.setList(pageList);
        pageData.setPage(pageNum);
        pageData.setSize(pageSize);
        pageData.setTotalPages(iPlanList.size() % pageSize != 0 ? iPlanList.size() / pageSize + 1:iPlanList.size() / pageSize);
        pageData.setTotal(iPlanList.size());
        pageData.setTip("发布时间为工作日10:00,14:00,其余时间随机发布");
        newbieUsable = ArithUtil.round((newbieUsable >= 0 ? newbieUsable/100 : 0), 2);
        pageData.setNewbieAmt(Double.toString(newbieUsable));
        pageData.setPicUrl(picUrl);
        pageData.setRedirectUrl(redirectUrl);
        return new RestResponseBuilder<>().success(pageData);
    }

    //天天赚转入月月盈项目列表
    @GetMapping(value = "/iplan/ttzToIplan/list")
    public RestResponse getTtzToIplanList(@RequestParam("userId") String userId) {
        // 获取用户当前VIP加息利率
        double iplanRate = marketService.getIplanVipRate(userId);
        List<TtzToIPlanAppListDto> listDtos = new ArrayList<>();
        List<IPlan> iPlanList = iPlanService.getTtzToIplanList();
        if (iPlanList != null && iPlanList.size() > 0) {
            for (IPlan iPlan : iPlanList) {
                TtzToIPlanAppListDto dto = new TtzToIPlanAppListDto();
                BeanUtils.copyProperties(iPlan, dto);
                double fixRate = iPlan.getFixRate().doubleValue();
                String fixRateStr = df4.format(fixRate * 100);
                double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
                if (bonusRate > 0) {
                    fixRateStr += "+" + df4.format(bonusRate * 100);
                    fixRate += bonusRate;
                }
                if (iplanRate > 0) {
                    fixRateStr += "+" + df4.format(iplanRate * 100);
                    fixRate += iplanRate;
                }
                dto.setRate(fixRateStr + "%");
                dto.setTotalRate(fixRate);
                String redPacketMsg = "";
                List<RedPacket> redPackets = redPacketService.getRedPacketDetails(new RedPacket(userId, RedPacket.SEND_STATUS_UNUSED, RedPacket.SPECIFIC_TYPE_TTZ_TO_IPLAN));
                long redPacketNum = redPackets.stream().filter(redPacket -> redPacket.getInvestCycle() <= iPlan.getTerm()).count();
                if (redPacketNum > 0) {
                    redPacketMsg = redPacketNum + "张红包可用";
                }
                dto.setRedPacketMsg(redPacketMsg);
                listDtos.add(dto);
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("result", listDtos);
        return new RestResponseBuilder<>().success(map);
    }

    //app-显示理财计划详情页
    @GetMapping("/{iPlanId}/detail")
    public RestResponse<IPlanAppDetailDto> getIPlanDetail(@PathVariable("iPlanId") Integer iPlanId, @RequestParam(value = "userId", required = false) String userId){
        IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        if (null == iPlan) {
            logger.warn("该理财计划[id={}]不存在", iPlanId);
            throw new ProcessException(Error.NDR_0428);
        }
        IPlanAppDetailDto iPlanDetailDto = new IPlanAppDetailDto();

        iPlanDetailDto.setId(iPlan.getId());
        iPlanDetailDto.setName(iPlan.getName());//理财计划名称
        iPlanDetailDto.setPackagingType(iPlan.getPackagingType());
        iPlanDetailDto.setQuota(iPlan.getQuota() / 100.0 + "元");//计划发行额度
        iPlanDetailDto.setAvailableQuota(iPlan.getAvailableQuota() / 100.0 + "元");//当前剩余额度
        iPlanDetailDto.setStatus(iPlan.getStatus());
        if (Objects.equals(iPlan.getStatus(), IPlan.STATUS_RAISING_FINISH) ||
                Objects.equals(iPlan.getStatus(), IPlan.STATUS_EARNING) ||
                Objects.equals(iPlan.getStatus(), IPlan.STATUS_END)) {
            iPlanDetailDto.setSalesPercent(1.0);//已售比例
        } else {
            iPlanDetailDto.setSalesPercent(new BigDecimal(iPlan.getQuota() - iPlan.getAvailableQuota()).divide(new BigDecimal(iPlan.getQuota()), 2, BigDecimal.ROUND_DOWN).doubleValue());//已售比例
        }
        iPlanDetailDto.setInterestAccrualType(iPlan.getInterestAccrualType());//0月标1天标
        iPlanDetailDto.setDay(iPlan.getDay());//天数
        iPlanDetailDto.setTerm(iPlan.getTerm());//期数
        iPlanDetailDto.setRaiseOpenTime(iPlan.getRaiseOpenTime());//开放募集时间
        iPlanDetailDto.setRaiseDays(iPlan.getRaiseDays());//募集期天数
        iPlanDetailDto.setExitLockDays(iPlan.getExitLockDays() + "天");//锁定期天数
        iPlanDetailDto.setIncreaseRate(iPlan.getIncreaseRate());//递增利率
        double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
        double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
        double vipRate = marketService.getIplanVipRate(userId);

        iPlanDetailDto.setFixRate(fixRate);
        iPlanDetailDto.setFixRate2(df4.format(fixRate * 100) + "%");
        iPlanDetailDto.setBonusRate(bonusRate);
        String bonusRate2 = bonusRate > 0 ? df4.format(bonusRate * 100) + "%" : "";
        iPlanDetailDto.setBonusRate2(bonusRate2);
        iPlanDetailDto.setVipRate(vipRate);
        String vipRate2 = vipRate > 0 ? df4.format(vipRate * 100) + "%" : "";
        iPlanDetailDto.setVipRate2(vipRate2);
        iPlanDetailDto.setTotalRate(fixRate + bonusRate + vipRate);
        iPlanDetailDto.setTotalRate2(df4.format((fixRate + bonusRate + vipRate) * 100) + "%");
        if(bonusRate > 0.0){
            iPlanDetailDto.setNewTotalRate(df4.format(fixRate * 100)+"+"+df4.format(bonusRate * 100)+"%");
        }else{
            iPlanDetailDto.setNewTotalRate(df4.format(fixRate * 100) + "%");
        }
        /*BigDecimal fixRate = iPlan.getFixRate();
        BigDecimal bonusRate = iPlan.getBonusRate();
        if (null != fixRate) {
            double fixRate2 = fixRate.doubleValue();
            iPlanDetailDto.setFixRate(fixRate2);//固定利率
            if (fixRate2 * 100 % 1.0 == 0) {
                iPlanDetailDto.setFixRate2((int)(fixRate2 * 100) + "%");//固定利率（显示用）
            } else {
                iPlanDetailDto.setFixRate2(fixRate2 * 100 + "%");//固定利率（显示用）
            }
        }
        if (null != bonusRate) {
            if (bonusRate.compareTo(BigDecimal.ZERO) != 0) {
                double bonusRate2 = bonusRate.doubleValue();
                iPlanDetailDto.setBonusRate(bonusRate2);//加息利率
                if (bonusRate2 * 100 % 1.0 == 0) {
                    iPlanDetailDto.setBonusRate2((int)(bonusRate2 * 100) + "%");//加息利率（显示用）
                } else {
                    iPlanDetailDto.setBonusRate2(bonusRate2 * 100 + "%");//加息利率（显示用）
                }
            }
        }*/

        //一个小时内倒数，否则显示开放募集时间
        if (Objects.equals(iPlan.getStatus(), IPlan.STATUS_ANNOUNCING)) {
            String raiseOpenTime = iPlan.getRaiseOpenTime();
            LocalDate raiseOpenDate = DateUtil.parseDate(raiseOpenTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDateTime raiseOpenDateTime = DateUtil.parseDateTime(raiseOpenTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDate now = LocalDate.now();
            LocalDateTime nowTime = LocalDateTime.now();
            if (raiseOpenDate.compareTo(now) == 0) {
                if (nowTime.plusHours(HOUR_TO_REMIND).compareTo(raiseOpenDateTime) < 0) {
                    //一个小时以外
                    iPlanDetailDto.setStrTime(raiseOpenTime.substring(11,16)+"开售");
                    iPlanDetailDto.setLongTime("");
                } else {
                    //一个小时内、刚好一个小时
                    long time = raiseOpenDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                            nowTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    if (time <= 0) {
                        iPlanDetailDto.setLongTime(String.valueOf(0));
                    } else {
                        iPlanDetailDto.setLongTime(String.valueOf(time));
                    }
                    iPlanDetailDto.setStrTime("");
                }
            } else if (raiseOpenDate.compareTo(now) > 0) {
                //募集开放时间在现在之后
                LocalDate now1 = now.plusDays(1);
                if (now1.compareTo(raiseOpenDate) > 0) {
                    //募集期 今日明日之间
                    iPlanDetailDto.setStrTime("明日开售");
                } else if (now1.compareTo(raiseOpenDate) < 0) {
                    //募集期 明日后
                    iPlanDetailDto.setStrTime(Integer.valueOf(raiseOpenTime.substring(5,7))+"月"+raiseOpenTime.substring(8,10)+"日开售");
                } else {
                    //募集期=今日
                    iPlanDetailDto.setStrTime(raiseOpenTime.substring(11,16)+"开售");
                }
            } else {
                //募集开放时间在现在之前
                iPlanDetailDto.setStrTime("加入时间："+raiseOpenTime);
            }
        }

        //还款方式
        if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
            iPlanDetailDto.setRepayType("一次性还本付息");
        }else {
            iPlanDetailDto.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
        }
        //图标路径
        Integer activityId = iPlan.getActivityId();
        ActivityMarkConfigure activityMarkConfigure = null;
        if (null != activityId) {
            iPlanDetailDto.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
            activityMarkConfigure = activityMarkConfigureService.findById(activityId);
            if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                iPlanDetailDto.setAddTerm(activityMarkConfigure.getIncreaseTerm());
            }else {
                iPlanDetailDto.setAddTerm(0);
            }
        }
        if (Objects.equals(iPlan.getNewbieOnly(), IPlan.NEWBIE_ONLY_Y)) {
            iPlanDetailDto.setImgUrl(activityMarkConfigureService.getNewBieUrl());
            if(iPlan.getIplanType() == 2 && activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                iPlanDetailDto.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
            }
        }
        //返回锁定期
        int month=0;
          if(iPlan.getExitLockDays()!=null){
          	if (IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())&&"1".equals(iPlan.getInterestAccrualType()+"")){
          		iPlanDetailDto.setExitLockDays(iPlan.getExitLockDays()+"天");
              }else{
              	if(iPlan.getExitLockDays()<31){
              		iPlanDetailDto.setExitLockDays(iPlan.getExitLockDays()+"天");
              	}else{
              		 month=iPlan.getExitLockDays()/31;
              		iPlanDetailDto.setExitLockDays(month+"个月");
              	}
              }
          }
        //步骤
        iPlanDetailDto.setJoinStep1("今日加入");
        iPlanDetailDto.setJoinStep2("明日计息");
        iPlanDetailDto.setJoinStep4(iPlanDetailDto.getExitLockDays() + "锁定期");
        iPlanDetailDto.setJoinStep5("转让/到期退出");
        iPlanDetailDto.setNewbieOnly(iPlan.getNewbieOnly());//是否新手专享，0否，1是
        IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
        if (null == iPlanParam) {
            logger.warn("iPlan[id={}]，查询：IPlanParam[id={}]，查无数据", iPlan.getId(), iPlan.getIplanParamId());
            throw new ProcessException(Error.NDR_DATA_ERROR);
        }
        String qx=!IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())?iPlan.getTerm()+ "个月":iPlan.getDay()+"天";
        String needToKnow = "投资期限：" + qx + "\n" +
                "起投金额：" + iPlanParam.getInvestMin() / 100.0 + "元起投，并以"+iPlanParam.getInvestIncrement() / 100.0+"元递增"+"\n" +
                "计划总额：" + iPlan.getQuota() / 100.0 + "元" + "\n" +
                "如何退出：" + "到期自动退出，支持转让提前退出" + "\n" +
                "费用说明：" + "加入不收取任何费用，手动转让退出收取本金的" + iPlanParam.getExitFeeRate().doubleValue()*100+"%";
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
        	iPlanDetailDto.setName("省心投"+iPlan.getName().substring(iPlan.getName().length()-6));//理财计划名称
            iPlanDetailDto.setProjectAdvantage("“省心投”是短融网推出的智能投资工具，" +
                    "可满足用户大额投资需求，背后资产都是来自信息透明的散标。" +
                    "分散投资策略并支持债权转让，从根本上让您的资金安全的流动起来！");
            iPlanDetailDto.setJoinStep3(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
            iPlanDetailDto.setShareTitle("省心投 "+iPlan.getTerm()+"个月期限 期待年回报率"+df.format(iPlan.getFixRate().doubleValue()*100) + "%");
            iPlanDetailDto.setShareContent("短融网平台推出的全新投资产品，有不同期限的产品可供选择，为用户提供更安全、更畅快的投资体验，" +
                    "用户资金加入后，系统将进行自动分配，对符合要求的真实标的进行匹配。");
            	needToKnow = "投资期限：" + iPlan.getTerm() + "个月" + "\n" +
                    "起投金额：" + iPlanParam.getInvestMin() / 100.0 + "元起投，并以"+iPlanParam.getInvestIncrement() / 100.0+"元递增"+"\n" +
                    "计划总额：" + iPlan.getQuota() / 100.0 + "元" + "\n" +
                    "如何退出：" + "到期自动退出，支持转让提前退出" + "\n" +
                    "费用说明：" + "投资不收任何费用，申请转让退出按照本金0.8%收费。\n" +
                    "计息方式：" + "未放款之前按照4%计息，放款后按照项目利息计息。\n" +
                    "其他说明：“省心投”符合监管提出的“为出借人与借款人提供直接借贷”规定。期待年回报率并不一定等于实际的收益率。项目的期限、锁定期的1个月按31天计算。";
            if(iPlanAccountService.isNewIplan(iPlan)){
                needToKnow = "持有期限：最长可持有" + iPlan.getTerm() + "个月" + "\n" +
                        "起投金额：" + iPlanParam.getInvestMin() / 100.0 + "元起投，"+iPlanParam.getInvestIncrement() / 100.0+"元递增"+"\n" +
                        "计划总额：" + iPlan.getQuota() / 100.0 + "元" + "\n" +
                        "计息方式：" + "加入成功次日起息，未放款前享受平台4%补息，放款后按照项目利率计算。\n" +
                        "退出方式：" + "锁定期内不可转出，锁定期结束后可免费转让，继续持有享额外递增利率，项目到期自动退出。\n" +
                        "其他说明：“省心投”符合监管提出的“为出借人与借款人提供直接借贷”规定。期待年回报率、参考递增利率不一定等于实际收益率。项目期限、锁定期的1个月以31天计算。具体债权转让时长视债权市场交易情况而定。转让成功后不会扣除平台发放的奖励。";
                iPlanDetailDto.setJoinStep1("今日加入");
                iPlanDetailDto.setJoinStep2("明日计息");
                iPlanDetailDto.setJoinStep3("募集完成");
                iPlanDetailDto.setJoinStep4("锁定期结束");
                iPlanDetailDto.setJoinStep5("免费转让");
                //为柱状图提供数据
                for(int i=1;i<=iPlan.getTerm();i++){
                	double data=ArithUtil.round(iPlanDetailDto.getTotalRate()*100,2);
                	iPlanDetailDto.getIncreaseRateX().add(i+"");
                	if(i<=month){//如果在锁定期期间，则基本利率
                		iPlanDetailDto.getIncreaseRateY().add(data+"");
                	}else{
                		data=ArithUtil.round((iPlanDetailDto.getTotalRate()*100+
                				(i-month)*iPlan.getIncreaseRate().doubleValue()*100),2);
                    	iPlanDetailDto.getIncreaseRateY().add(data+"");
                	}
                }
            }
            Double increaseRate = iPlan.getIncreaseRate() != null ? iPlan.getIncreaseRate().doubleValue() : 0;
            int term = iPlan.getTerm() != null ? iPlan.getTerm() : 0;
            int exitLockDays = iPlan.getExitLockDays() != null ? iPlan.getExitLockDays() : 0;
            Double increaseMaxRate = fixRate + increaseRate * (term - exitLockDays / 31);
            iPlanDetailDto.setIncreaseMaxRate(ArithUtil.round(increaseMaxRate, 2));
            iPlanDetailDto.setIncreaseMaxRate2(df4.format(increaseMaxRate * 100) + "%");
            Double increaseTotalRate = increaseMaxRate + bonusRate;
            iPlanDetailDto.setIncreaseTotalRate(ArithUtil.round(increaseTotalRate, 2));
            iPlanDetailDto.setIncreaseTotalRate2(df4.format(increaseTotalRate * 100) + "%");
        } else {
            iPlanDetailDto.setProjectAdvantage("月月盈是短融网推出的一款收益高、更灵活的投资工具。" +
                    "投资人认购成功后，由系统智能匹配标的，整个资金流程在银行存管系统下进行。" +
                    "锁定期后，投资人可随时申请转让，以满足投资人的不时之需。");//项目优势
          //还款方式
            iPlanDetailDto.setJoinStep3("按月付息");
            if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                iPlanDetailDto.setJoinStep3("一次性还本付息");
            }
            iPlanDetailDto.setShareTitle("月月盈 "+iPlan.getTerm()+"个月期限 期待年回报率"+df.format(iPlan.getFixRate().doubleValue()*100) + "%");
            iPlanDetailDto.setShareContent("短融网平台推出的全新理财产品，有不同期限的产品可供选择，为用户提供更安全、" +
                    "更畅快的理财体验，用户资金加入后，系统将进行自动分配，对符合要求的真实债权进行自动投标。");
        }

        iPlanDetailDto.setNeedToKnow(needToKnow);//购买须知
        iPlanDetailDto.setInvestUserUrl(investUserUrl + "?iPlanId=" + iPlan.getId() + "&from=app");
        iPlanDetailDto.setQuestionUrl(questionUrl);


        iPlanDetailDto.setShareLinkUrl(shareLinkUrl+iPlan.getId());
        iPlanDetailDto.setShareProUrl(imagePrefix+"/app/loan/share.png");

        iPlanDetailDto.setCreditDetailUrl(creditDetailUrl+"?iPlanCode="+iPlan.getCode());

        List<SubjectYjtDto> subjectYjtDtos = new ArrayList<>();
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
            if (IPlan.PACKAGING_TYPE_CREDIT.equals(iPlan.getPackagingType())) {
                List<CreditOpening> creditOpenings = creditOpeningDao.getByIplanId(iPlanId);
                if (creditOpenings != null && creditOpenings.size() > 0) {
                    Map<String, List<CreditOpening>> creditOpeningMap = creditOpenings.stream().collect(Collectors.groupingBy(CreditOpening::getSubjectId));
                    for (Map.Entry<String, List<CreditOpening>> entry : creditOpeningMap.entrySet()) {
                        String subjectId = entry.getKey();
                        List<CreditOpening> subjectCreditOpenings = entry.getValue();
                        int totalMoney = subjectCreditOpenings.stream().map(creditOpening -> creditOpening.getTransferPrincipal()).reduce(Integer::sum).orElse(0);
                        Subject subject = subjectService.getBySubjectId(subjectId);
                        SubjectYjtDto dto = new SubjectYjtDto();
                        dto.setName(subject.getName());
                        dto.setSubjectId(subject.getSubjectId());
                        dto.setRepayType(subject.getRepayType());
                        dto.setTotalAmt(df.format(totalMoney / 100.0));
                        subjectYjtDtos.add(dto);
                    }
                }

            } else {
                List<Subject> subjects = subjectService.getByIplanId(iPlanId);
                if (subjects != null && subjects.size() > 0) {
                    for (Subject subject : subjects) {
                        SubjectYjtDto dto = new SubjectYjtDto();
                        BeanUtils.copyProperties(subject, dto);
                        dto.setTotalAmt(df.format(subject.getTotalAmt() / 100.0));
                        subjectYjtDtos.add(dto);
                    }
                }
            }
        }
        iPlanDetailDto.setSubjectList(subjectYjtDtos);
        iPlanDetailDto.setSubjectRate(iPlanAccountService.getRate(iPlan).toString());



        if(iPlanAccountService.isNewIplan(iPlan)){
        	iPlanDetailDto.setNewYjtFlag("1");
        	iPlanDetailDto.setHoldingInfo("锁定期后申请退出免手续费 继续持有享"+ArithUtil.round(iPlanDetailDto.getTotalRate()*100,2)
        			+"%~"+ArithUtil.round((iPlanDetailDto.getTotalRate()*100+
        			(iPlan.getTerm()-month)*iPlan.getIncreaseRate().doubleValue()*100),2)+"%递增利率");
        };
        //前端显示期限格式
        iPlanDetailDto.setTermStr(iPlan.getTerm()+"个月");
        if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
            if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                iPlanDetailDto.setTermStr(iPlan.getDay()+"天");
            }
        }
        return new RestResponseBuilder<IPlanAppDetailDto>().success(iPlanDetailDto);
    }

    //app-理财计划购买页的数据
    @GetMapping("/authed/{iPlanId}/{userId}")
    public RestResponse<IPlanAppPurchaseDetailDto> getIPlanAndUserId(   @PathVariable("iPlanId") Integer iPlanId,
                                                                        @PathVariable("userId") String userId,
                                                                        @RequestParam("requestSource") String requestSource){

        IPlanAppPurchaseDetailDto iPlanPurchaseDetailDto = new IPlanAppPurchaseDetailDto();

        IPlan iPlan = iPlanService.getIPlanById(iPlanId);
        if (null == iPlan) {
            logger.warn("该理财计划[id={}]不存在", iPlanId);
            throw new ProcessException(Error.NDR_0428);
        }
        UserAccount userAccount = userAccountService.getUserAccount(userId);
        IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
        if (null == iPlanParam) {
            logger.warn("iPlan[id={}]，查询：IPlanParam[id={}]，查无数据", iPlan.getId(), iPlan.getIplanParamId());
            throw new ProcessException(Error.NDR_DATA_ERROR);
        }

        BeanUtils.copyProperties(iPlan, iPlanPurchaseDetailDto);
        iPlanPurchaseDetailDto.setAvailableBalance(null==userAccount ? 0.0 : userAccount.getAvailableBalance());
        iPlanPurchaseDetailDto.setInputTips(iPlanParam.getInvestMin() / 100.0 + "元起投");
        iPlanPurchaseDetailDto.setInvestIncrement(iPlanParam.getInvestIncrement() / 100.0);
        if (StringUtils.equals(requestSource, "ios_4.6.0")) {
            iPlanPurchaseDetailDto.setInvestIncrement(iPlanParam.getInvestIncrement() / 10000.0);
        }
        iPlanPurchaseDetailDto.setAvailableQuota(iPlan.getAvailableQuota() / 100.0);
        iPlanPurchaseDetailDto.setExitLockDays(iPlan.getExitLockDays() + "天");
        iPlanPurchaseDetailDto.setExitLockDaysCount(iPlan.getExitLockDays());
        iPlanPurchaseDetailDto.setInterestAccrualType(iPlan.getInterestAccrualType());//0月标1天标
        iPlanPurchaseDetailDto.setDay(iPlan.getDay());//天数
        iPlanPurchaseDetailDto.setTerm(iPlan.getTerm());//期数
        BigDecimal fixRate = iPlan.getFixRate();
        BigDecimal bonusRate = iPlan.getBonusRate();
        double bonusRateNew = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
        double fixRateNew = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
        iPlanPurchaseDetailDto.setFixRate(fixRate);
        iPlanPurchaseDetailDto.setFixRate2(df4.format(fixRateNew * 100) + "%");//固定利率（显示）
        iPlanPurchaseDetailDto.setBonusRate(bonusRate);
        if (null != bonusRate) {
            if (bonusRate.compareTo(BigDecimal.ZERO) != 0) {
                iPlanPurchaseDetailDto.setBonusRate2(df.format(bonusRate.doubleValue() * 100) + "%");//加息利率（显示用）
            }
        }
        if (IPlan.IPLAN_TYPE_YJT.equals(iPlan.getIplanType())) {
            iPlanPurchaseDetailDto.setUrl1(protocolUrl);
            if (iPlan.getPackagingType() == 1) {
                iPlanPurchaseDetailDto.setContractTitle("债权转让协议");
                iPlanPurchaseDetailDto.setUrl1(transferUrl);
            }
        } else {
            iPlanPurchaseDetailDto.setUrl1("http://duanrongweb.oss-cn-qingdao.aliyuncs.com/file/%E6%9C%88%E6%9C%88%E7%9B%88%E6%9C%8D%E5%8A%A1%E5%8D%8F%E8%AE%AE.pdf");
        }
        iPlanPurchaseDetailDto.setUrl2("http://m.duanrong.com/html/riskInformed.html");
        iPlanPurchaseDetailDto.setStatus(iPlan.getStatus());

        double vipRate = marketService.getIplanVipRate(userId);
        iPlanPurchaseDetailDto.setVipRate(vipRate);
        iPlanPurchaseDetailDto.setTotalRate((fixRate != null ? fixRate.doubleValue() : 0) + (bonusRate != null ? bonusRate.doubleValue() : 0) + vipRate);
        iPlanPurchaseDetailDto.setTotalRateStr(df4.format(((fixRate != null ? fixRate.doubleValue() : 0) + (bonusRate != null ? bonusRate.doubleValue() : 0) + vipRate) * 100) + "%");
        if(bonusRateNew > 0.0){
            iPlanPurchaseDetailDto.setNewTotalRate(df4.format(fixRateNew * 100)+"+"+df4.format(bonusRateNew * 100)+"%");
        }else{
            iPlanPurchaseDetailDto.setNewTotalRate(df4.format(fixRateNew * 100) + "%");
        }

        //用户风险测评
        String whereAnswer = userService.getComplianceAnswer(userId);
        String setUpDesc = "";
        if(null == whereAnswer || ("").equals(whereAnswer)){
            iPlanPurchaseDetailDto.setWhereAnswer("false");

        }else{
            iPlanPurchaseDetailDto.setWhereAnswer("true");
            if("A".equals(whereAnswer)){
                setUpDesc = "积极型";
            }else if("B".equals(whereAnswer)){
                setUpDesc = "稳健型";
            }else {
                setUpDesc = "保守型";
            }
        }
        iPlanPurchaseDetailDto.setSetUpDesc(setUpDesc);

        List<IPlanAppPurchaseDetailDto.RedPacketApp> redPacketAppList = new ArrayList<>();
        List<RedPacket> redPacketList = redPacketService.getUsablePackets(userId, iPlan, requestSource);
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
        iPlanPurchaseDetailDto.setRedPacketAppList(redPacketAppList);
        if (Objects.equals(iPlan.getStatus(), IPlan.STATUS_ANNOUNCING)) {
            String raiseOpenTime = iPlan.getRaiseOpenTime();
            LocalDate raiseOpenDate = DateUtil.parseDate(raiseOpenTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDateTime raiseOpenDateTime = DateUtil.parseDateTime(raiseOpenTime, DateUtil.DATE_TIME_FORMATTER_19);
            LocalDate now = LocalDate.now();
            LocalDateTime nowTime = LocalDateTime.now();
            if (raiseOpenDate.compareTo(now) == 0) {
                if (nowTime.plusHours(HOUR_TO_REMIND).compareTo(raiseOpenDateTime) < 0) {
                    //一个小时以外
                    iPlanPurchaseDetailDto.setStrTime(raiseOpenTime.substring(11,16)+"开售");
                    iPlanPurchaseDetailDto.setLongTime("");
                } else {
                    //一个小时内、刚好一个小时
                    long time = raiseOpenDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() -
                            nowTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                    if (time <= 0) {
                        iPlanPurchaseDetailDto.setLongTime(String.valueOf(0));
                    } else {
                        iPlanPurchaseDetailDto.setLongTime(String.valueOf(time));
                    }
                    iPlanPurchaseDetailDto.setStrTime("");
                }
            } else if (raiseOpenDate.compareTo(now) > 0) {
                //募集开放时间在现在之后
                LocalDate now1 = now.plusDays(1);
                if (now1.compareTo(raiseOpenDate) > 0) {
                    //募集期 今日明日之间
                    iPlanPurchaseDetailDto.setStrTime("明日开售");
                } else if (now1.compareTo(raiseOpenDate) < 0) {
                    //募集期 明日后
                    iPlanPurchaseDetailDto.setStrTime(Integer.valueOf(raiseOpenTime.substring(5,7))+"月"+raiseOpenTime.substring(8,10)+"日开售");
                } else {
                    //募集期=今日
                    iPlanPurchaseDetailDto.setStrTime(raiseOpenTime.substring(11,16)+"开售");
                }
            } else {
                //募集开放时间在现在之前
                iPlanPurchaseDetailDto.setStrTime("加入时间："+raiseOpenTime);
            }
        }
        Integer activityId = iPlan.getActivityId();
        ActivityMarkConfigure activityMarkConfigure = null;
        if (null != activityId) {
            activityMarkConfigure = activityMarkConfigureService.findById(activityId);
            if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                iPlanPurchaseDetailDto.setAddTerm(activityMarkConfigure.getIncreaseTerm());
            }else {
                iPlanPurchaseDetailDto.setAddTerm(0);
            }
        }
        //是否激活、是否开户
        iPlanPurchaseDetailDto.setIsOpenAccount(userAccountService.checkIfOpenAccount(userId) ? 1 : 0);
        iPlanPurchaseDetailDto.setIsActive(userAccountService.checkIfActive(userId));
        if (IPlan.NEWBIE_ONLY_Y.equals(iPlan.getNewbieOnly())) {
            Double remainQ=investService.getNewbieUsable(userId, iPlan.getIplanType());
            iPlanPurchaseDetailDto.setNewbieOnly(1);
            System.out.println("#########月月盈或者省心投用户购买页查询剩余限额---->"+remainQ/100);
            String iplanName=iPlan.getIplanType()==0?"(限任意月月盈项目)":"(限任意省心投项目)";
            Config iPlanNewbieAmtConfig = configService.getConfigById(Config.IPLAN_NEWBIE_AMT);
            if(iPlanNewbieAmtConfig==null || "0".equals(iPlanNewbieAmtConfig.getValue().toString())){
            	if(requestSource.contains("android")){
            		if(requestSource.compareTo("android5.6.0")>=0){
                		iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元  "+iplanName);
                	}else{
                		iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元");
                	}
            	}
            	
            	if(requestSource.contains("ios")){
            		if(requestSource.compareTo("ios_5.6.0")>=0){
            			iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元  "+iplanName);
            		}else{
            			iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元  ");
            		}
            	}
            }else{
            	if(requestSource.contains("android")){
            		if(requestSource.compareTo("android5.6.0")>=0){
                		iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元  (含任意定期项目)");
                	}else{
                		iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元");
                	}
            	}
            	
            	if(requestSource.contains("ios")){
            		if(requestSource.compareTo("ios_5.6.0")>=0){
            			iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元  (含任意定期项目)");
            		}else{
            			iPlanPurchaseDetailDto.setNewbieTip("剩余可投额度"+remainQ.longValue()/100+"元  ");
            		}
            	}
            }
        } else {
        	iPlanPurchaseDetailDto.setNewbieOnly(0);
            iPlanPurchaseDetailDto.setNewbieTip("");
        }
        //还款方式
        if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
            iPlanPurchaseDetailDto.setRepayType("一次性还本付息");
        }else {
            iPlanPurchaseDetailDto.setRepayType(IPlanMobileResource.decideRepayType(iPlan.getRepayType()));
        }
        //前端显示期限格式
        iPlanPurchaseDetailDto.setTermStr(iPlan.getTerm()+"个月");
        if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
            if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                iPlanPurchaseDetailDto.setTermStr(iPlan.getDay()+"天");
            }
        }
       // String subjectRate = iPlan.getSubjectRate()==null?"0.144":iPlan.getSubjectRate().toString();
        iPlanPurchaseDetailDto.setSubjectRate(iPlanAccountService.getRate(iPlan).toString());

      //返回锁定期
        int month=0;
        if(iPlan.getExitLockDays()!=null){
        	if (IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())&&"1".equals(iPlan.getInterestAccrualType()+"")){
        		iPlanPurchaseDetailDto.setExitLockCountFlag("0");
        		iPlanPurchaseDetailDto.setExitLockCount(iPlan.getExitLockDays());
        		iPlanPurchaseDetailDto.setExitLockDays(iPlan.getExitLockDays()+"天");
            }else{
            	if(iPlan.getExitLockDays()<31){
            		iPlanPurchaseDetailDto.setExitLockCountFlag("0");
            		iPlanPurchaseDetailDto.setExitLockCount(iPlan.getExitLockDays());
            		iPlanPurchaseDetailDto.setExitLockDays(iPlan.getExitLockDays()+"天");
            	}else{
            		iPlanPurchaseDetailDto.setExitLockCountFlag("1");
            		month=iPlan.getExitLockDays()/31;
            		iPlanPurchaseDetailDto.setExitLockCount(month);
            		iPlanPurchaseDetailDto.setExitLockDays(month+"个月");
            	}
            }
        }
      //是否为新省心投
        if(iPlanAccountService.isNewIplan(iPlan)){
        	iPlanPurchaseDetailDto.setNewYjtFlag("1");
        	iPlanPurchaseDetailDto.setHoldingInfo("锁定期后申请退出免手续费 继续持有享"+ArithUtil.round(iPlanPurchaseDetailDto.getTotalRate()*100,2)
        			+"%~"+ArithUtil.round((iPlanPurchaseDetailDto.getTotalRate()*100+
        			(iPlan.getTerm()-month)*iPlan.getIncreaseRate().doubleValue()*100),2)+"%递增利率");
        	iPlanPurchaseDetailDto.setHoldingInfo1("免手续费");
        	iPlanPurchaseDetailDto.setHoldingInfo2(ArithUtil.round(iPlanPurchaseDetailDto.getTotalRate()*100,2)
        			+"%~"+ArithUtil.round((iPlanPurchaseDetailDto.getTotalRate()*100+
                			(iPlan.getTerm()-month)*iPlan.getIncreaseRate().doubleValue()*100),2)+"%");
        }
        //新手额度
        Double newbieUsable = investService.getNewbieUsable(userId,iPlan.getIplanType()) / 100.0;
        if (newbieUsable <= 0) {
            //不是新手
        	iPlanPurchaseDetailDto.setNewbieAmt(0d);
        } else {
        	iPlanPurchaseDetailDto.setNewbieAmt(newbieUsable);
        }
        return new RestResponseBuilder<IPlanAppPurchaseDetailDto>().success(iPlanPurchaseDetailDto);
    }

    @GetMapping("/authed/iplan/invest/toConfirm")
    public RestResponse getConfirmDetail(@RequestParam("userId") String userId, @RequestParam("iplanId") int iplanId) {
        if (StringUtils.isBlank(userId)) {
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

    @GetMapping("/authed/iplan/invest/manage/{userId}/{type}")
    public RestResponse iPlanInvestManage(@PathVariable("userId") String userId,
                                          @PathVariable("type") int type,
                                          @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                          @RequestParam(value = "pageSize", defaultValue = "10") int pageSize,
                                          @RequestParam(value = "requestSource", required = false) String requestSource,
                                          @RequestParam(value = "iplanType", required = false, defaultValue = "0") int iplanType) {

        if (type == IPlanAppInvestManageDto.PAGE_TYPE_HOLDING) {
            IPlanAppInvestManageHoldDto investManageDto = new IPlanAppInvestManageHoldDto();
            List<IPlanAppInvestManageHoldDto.Detail> details = new ArrayList<>();

            //总持有金额，待收收益，处理中金额
            double interest = 0.0;
            double totalPrincipal = 0.0;
            double transAmt = 0.0;
            double processedAmt = 0.0;
            Integer transferAmt = 0;
            long startTime = System.currentTimeMillis();
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
            	 IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
            	//新老版本兼容，5.6.2版本之前剔除月月盈天标
                if("1".equals(iPlan.getInterestAccrualType()+"")){
                	if(StringUtils.isBlank(requestSource)||(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
                		continue;
                	}
                }
                totalPrincipal += iPlanAccount.getCurrentPrincipal() / 100.0;
                interest += (iPlanAccount.getExpectedInterest()
                        + (iPlanAccount.getIplanExpectedBonusInterest() == null ? 0 : iPlanAccount.getIplanExpectedBonusInterest())
                        + (iPlanAccount.getIplanExpectedVipInterest() == null ? 0 : iPlanAccount.getIplanExpectedVipInterest())) / 100.0;
            }
            if(iplanType==IPlan.IPLAN_TYPE_YJT){
                List<IPlanTransLog>  iplanTransLogList = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId,iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING,IPlanTransLog.TRANS_STATUS_SUCCEED)));
                if(iplanTransLogList != null && iplanTransLogList.size() > 0){
                    for (IPlanTransLog iplanTransLog : iplanTransLogList) {
                        IPlanAccount account = iPlanAccountService.findById(iplanTransLog.getAccountId());
                        if(account.getStatus().equals(IPlanAccount.STATUS_NORMAL_EXIT)){
                            transferAmt += 0;
                        }else{
                            if(IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iplanTransLog.getTransStatus())){
                                transferAmt += iplanTransLog.getTransAmt();
                            }else{
                                transferAmt += iplanTransLog.getProcessedAmt();
                            }
                        }
                    }
                }
                investManageDto.setTransferAmt(df4.format(transferAmt/100.0));
            }else{
                List<IPlanTransLog> allTransLogs = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_NORMAL_IN, IPlanTransLog.TRANS_TYPE_INIT_IN)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING, IPlanTransLog.TRANS_STATUS_SUCCEED, IPlanTransLog.TRANS_STATUS_TO_CONFIRM)))
                        .stream().filter(iPlanTransLog -> allPlan.stream().anyMatch(planAccount -> planAccount.getId().equals(iPlanTransLog.getAccountId()))).collect(Collectors.toList());
                for (IPlanTransLog iPlanTransLog : allTransLogs) {
                	IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
                	//新老版本兼容，5.6.2版本之前剔除月月盈天标
                    if("1".equals(iPlan.getInterestAccrualType()+"")){
                    	if(StringUtils.isBlank(requestSource)||(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
                    		continue;
                    	}
                    }
                    if (BaseResponse.STATUS_PENDING.equals(iPlanTransLog.getExtStatus())) {
                        transAmt += iPlanTransLog.getTransAmt() / 100.0;
                        processedAmt += iPlanTransLog.getProcessedAmt() / 100.0;
                    }
                }
                investManageDto.setProcessAmt(transAmt - processedAmt == 0 ? "" : df.format(transAmt - processedAmt));//处理中金额
            }
            investManageDto.setAmount(df.format(totalPrincipal));//总持有金额
            investManageDto.setInterest(df.format(interest));//待收收益
            investManageDto.setNotCallBackAmt(df4.format(totalPrincipal+interest));
            //持有中

            List<IPlanAccount> planList = iPlanAccountService.getByPageHelper(userId,
                    new HashSet<>(Arrays.asList(IPlanAccount.STATUS_PROCEEDS)), false, pageNum, pageSize, iplanType);
            for (IPlanAccount iPlanAccount : planList) {
            	if (iPlanAccount.getCurrentPrincipal() == 0) {
                    if(iplanType==IPlan.IPLAN_TYPE_YJT && (iPlanAccount.getAmtToTransfer()==null ||iPlanAccount.getAmtToTransfer()==0)) {
                        continue;
                    }/*else if(iplanType!=IPlan.IPLAN_TYPE_YJT){
                        continue;
                    }*/
                }
                IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
                if (null == iPlan) {
                    logger.warn("IPlanAccount[id={}]，查询：IPlan[id={}]，查无数据", iPlanAccount.getId(), iPlanAccount.getIplanId());
                    throw new ProcessException(Error.NDR_DATA_ERROR);
                }
            	//新老版本兼容，5.6.2版本之前剔除月月盈天标
                if("1".equals(iPlan.getInterestAccrualType()+"")){
                	if(StringUtils.isBlank(requestSource)||(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
                		continue;
                	}
                }
                IPlanAppInvestManageHoldDto.Detail detail = new IPlanAppInvestManageHoldDto.Detail();
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
                detail.setId(iPlan.getId());//理财计划id
                detail.setName(iPlanAccountService.getShortName(iPlan));
                String endTime = (iPlan.getEndTime() == null || iPlan.getEndTime().length() < 10) ? "生成中" : iPlan.getEndTime().substring(0, 10);
                detail.setEndTime(endTime);
                detail.setStatus(iPlan.getStatus());//2募集中,3募集完成，4收益中
                detail.setTerm(iPlan.getTerm());
                detail.setDay(iPlan.getDay());//天标
                //前端显示期限格式
                detail.setTermStr(iPlan.getTerm()+"个月");
                if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
                    if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                        detail.setTermStr(iPlan.getDay()+"天");
                    }
                }
                detail.setHoldingAmt(String.valueOf(iPlanAccount.getInitPrincipal() / 100.0));//购买金额
                if (iplanType == 2) {
                    detail.setHoldingAmt(String.valueOf(iPlanAccount.getCurrentPrincipal() / 100.0));//购买金额
                }
                detail.setProcessingAmt("");
                detail.setNotBackInterest(df4.format((iPlanAccount.getExpectedInterest()+iPlanAccount.getIplanExpectedBonusInterest())/100.0)+"元");

                Integer activityId = iPlan.getActivityId();
                ActivityMarkConfigure activityMarkConfigure = null;
                if (null != activityId) {
                    detail.setImgUrl(activityMarkConfigureService.getImgUrl(iPlan.getActivityId()));
                    activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                    if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                        detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                    }else {
                        detail.setAddTerm(0);
                    }
                }
                if (Objects.equals(iPlan.getNewbieOnly(), IPlan.NEWBIE_ONLY_Y)) {
                    detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                    detail.setNewbieOnly(IPlan.NEWBIE_ONLY_Y);
                    if(iPlan.getIplanType() == 2 && activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                        detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                    }
                }
                //若是一键投,查询已回收本息(包含加息利息)
                if(iplanType == IPlan.IPLAN_TYPE_YJT){
                    detail.setJoinTime(iPlanAccount.getCreateTime());
                    Integer money =0;
                    Integer amt = 0;
                    List<IPlanTransLog>  iplanTransLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(userId,iPlanAccount.getIplanId(),
                            new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                            new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING,IPlanTransLog.TRANS_STATUS_SUCCEED)));
                    if(iplanTransLogs != null && iplanTransLogs.size() > 0){
                        for (IPlanTransLog iplanTransLog : iplanTransLogs) {
                            if(IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iplanTransLog.getTransStatus())){
                                money = iplanTransLog.getTransAmt();
                            }else{
                                money = iplanTransLog.getProcessedAmt();
                            }
                            amt+=money;
                        }
                    }
                    //是否免费转让
                    if(iPlanAccountService.isNewIplan(iPlan)){
                        detail.setIsFree(IPlanAppInvestManageHoldDto.IS_FREE);
                    }
                    detail.setTransferAmt(df4.format(amt/100.0));
                    detail.setExpectedAmt(df4.format((iPlanAccount.getCurrentPrincipal()+iPlanAccount.getExpectedInterest()+
                            iPlanAccount.getIplanExpectedBonusInterest()+iPlanAccount.getIplanExpectedVipInterest())/100.0));
                    detail.setAccountId(iPlanAccount.getId());
//                    detail.setTransFlag(iPlanAccountService.checkCondition(iPlanAccount));
                }else{
                    List<IPlanRepayDetail> iPlanRepayDetails = iPlanRepayDetailService.getByUserIdAndIPlanId(userId, iPlanAccount.getIplanId());
                    Integer totalRepayAmt = 0;
                    for (IPlanRepayDetail repayDetail : iPlanRepayDetails) {
                        if (repayDetail.getStatus().equals(IPlanRepayDetail.STATUS_REPAY_FINISH)) {
                            totalRepayAmt += repayDetail.getDuePrincipal() + repayDetail.getDueInterest() + repayDetail.getDueBonusInterest() + repayDetail.getDueVipInterest();
                        }
                    }
                    detail.setInterest(String.valueOf(totalRepayAmt / 100.0));//已赚利息
                    String vipFlag = "";
                    if (iPlanAccount.getVipRate() != null && iPlanAccount.getVipRate().doubleValue() > 0) {
                        vipFlag = "vip";
                    }
                    detail.setVipFlag(vipFlag);
                }

                details.add(detail);
            }
            details = details.stream().sorted(Comparator.comparing(IPlanAppInvestManageHoldDto.Detail::getConfirmCount).reversed()).collect(Collectors.toList());
            investManageDto.setPageType(IPlanAppInvestManageDto.PAGE_TYPE_HOLDING);
            investManageDto.setDetails(details);
            return new RestResponseBuilder<>().success(investManageDto);
        } else if (type == IPlanAppInvestManageDto.PAGE_TYPE_TRANSFERRING) {
            IPlanAppInvestManageTransDto investManageDto = new IPlanAppInvestManageTransDto();
            List<IPlanAppInvestManageTransDto.Detail> details = new ArrayList<>();
            //总金额，转出费用，预计到账
            double principal = 0.0;
            double exitFee = 0.0;
            List<IPlanTransLog> allTransPlan = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iplanType,
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT, IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING)));
            for (IPlanTransLog iPlanTransLog : allTransPlan) {
            	IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
            	//新老版本兼容，5.6.2版本之前剔除月月盈天标
                if("1".equals(iPlan.getInterestAccrualType()+"")){
                	if(StringUtils.isBlank(requestSource)||(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
                		continue;
                	}
                }
                //总持有金额
                IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
                principal += iPlanAccount.getInitPrincipal() / 100.0;
                if (iPlanTransLog.getTransType().equals(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT)) {
                    //转出费用
                    exitFee += iPlanAccount.getExitFee() / 100.0;
                }
            }
            investManageDto.setAmount(df.format(principal));//总持有金额
            investManageDto.setInterest(df.format(exitFee));//转出费用
            investManageDto.setProcessAmt(df.format(principal - exitFee));//预计到账

            List<IPlanTransLog> transLogList = iPlanTransLogService.getByPageHelper(userId, iplanType,
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT, IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING)), pageNum, pageSize);
            for (IPlanTransLog iPlanTransLog : transLogList) {
                IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
                if (null == iPlan) {
                    logger.warn("IPlanTransLog[id={}]，查询：IPlan[id={}]，查无数据", iPlanTransLog.getId(), iPlanTransLog.getIplanId());
                    throw new ProcessException(Error.NDR_DATA_ERROR);
                }
                //新老版本兼容，5.6.2版本之前剔除月月盈天标
                if("1".equals(iPlan.getInterestAccrualType()+"")){
                	if(StringUtils.isBlank(requestSource)||(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
                		continue;
                	}
                }
                IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
                if (null == iPlanAccount) {
                    logger.warn("IPlanTransLog[id={}]，查询：IPlanAccount[id={}]，查无数据", iPlanTransLog.getId(), iPlanTransLog.getIplanId());
                    throw new ProcessException(Error.NDR_DATA_ERROR);
                }
                IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
                if (null == iPlanParam) {
                    logger.warn("iPlan[id={}]，查询：IPlanParam[id={}]，查无数据", iPlan.getId(), iPlan.getIplanParamId());
                    throw new ProcessException(Error.NDR_DATA_ERROR);
                }
                IPlanAppInvestManageTransDto.Detail detail = new IPlanAppInvestManageTransDto.Detail();
                String vipFlag = "";
                if (iPlanAccount.getVipRate() != null && iPlanAccount.getVipRate().doubleValue() > 0) {
                    vipFlag = "vip";
                }
                detail.setVipFlag(vipFlag);
                detail.setId(iPlanTransLog.getId());//转出中页面，id是iPlan_trans_log的id
                detail.setName(iPlan.getName());
                String endTime = (iPlan.getEndTime() == null || iPlan.getEndTime().length() < 10) ? "生成中" : iPlan.getEndTime().substring(0, 10);
                detail.setEndTime(endTime);
                detail.setTerm(iPlan.getTerm());
                detail.setDay(iPlan.getDay());//天标
                //前端显示期限格式
                detail.setTermStr(iPlan.getTerm()+"个月");
                if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
                    if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                        detail.setTermStr(iPlan.getDay()+"天");
                    }
                }
                detail.setHoldingAmt(df.format(iPlanAccount.getInitPrincipal() / 100.0));//购买金额
                detail.setTime(iPlanTransLog.getTransTime());//申请转让时间
                detail.setUrl("");
                Integer activityId = iPlan.getActivityId();
                ActivityMarkConfigure activityMarkConfigure = null;
                if (null != activityId) {
                    detail.setImgUrl(activityMarkConfigureService.getImgUrl(iPlan.getActivityId()));
                    activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                    if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                        detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                    }else {
                        detail.setAddTerm(0);
                    }
                }
                if (Objects.equals(iPlan.getNewbieOnly(), IPlan.NEWBIE_ONLY_Y)) {
                    detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                    detail.setNewbieOnly(IPlan.NEWBIE_ONLY_Y);
                    if(iPlan.getIplanType() == 2 && activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                        detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                    }
                }
                detail.setInterest(String.valueOf((iPlanAccount.getCurrentPrincipal()
                        + iPlanAccount.getIplanPaidInterest()
                        + iPlanAccount.getIplanPaidBonusInterest()
                        + iPlanAccount.getIplanPaidVipInterest()) / 100.0));//已到账收益

                // 月月盈提前还款
                if (IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())) {
                    detail.setBeforeRepayFlag(BaseIPlanDto.BEFORE_REPAY_FLAG_Y);
                    detail.setBeforeRepayTime(iPlanAccount.getUpdateTime());
                }

                details.add(detail);
            }
            investManageDto.setPageType(IPlanAppInvestManageDto.PAGE_TYPE_TRANSFERRING);
            investManageDto.setDetails(details);
            return new RestResponseBuilder<>().success(investManageDto);
        } else if (type == IPlanAppInvestManageDto.PAGE_TYPE_FINISH) {
            //一键投已完成列表
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
                        detail.setId(iPlanAccount.getId());
                        detail.setName(iPlanAccountService.getShortName(iPlan));
                        detail.setCreateTime(iPlanAccount.getCreateTime());
                        if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                            ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                            if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                                //活动标相关
                                detail.setImgUrl(activityMarkConfigureService.getImgUrl(iPlan.getActivityId()));
                                if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                                    detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                                }else {
                                    detail.setAddTerm(0);
                                }
                            }
                            if (Objects.equals(iPlan.getNewbieOnly(), IPlan.NEWBIE_ONLY_Y)) {
                                detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                                detail.setNewbieOnly(IPlan.NEWBIE_ONLY_Y);
                                if(iPlan.getIplanType() == 2 && activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                                    detail.setImgUrl(activityMarkConfigureService.getImgUrl(iPlan.getActivityId()));
                                }
                            }
                        }
                        detail.setTerm(iPlan.getTerm());
                        detail.setDay(iPlan.getDay());//天标
                        //前端显示期限格式
                        detail.setTermStr(iPlan.getTerm()+"个月");
                        if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
                            if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                                detail.setTermStr(iPlan.getDay()+"天");
                            }
                        }
                        //是否免费转让
                        if(iPlanAccountService.isNewIplan(iPlan)){
                            detail.setIsFree(IPlanAppInvestManageHoldDto.IS_FREE);
                        }
                        detail.setRate(df.format(iPlan.getFixRate().add(iPlan.getBonusRate()).add(iPlanAccount.getVipRate())));
                        detail.setExitLockDays(iPlan.getExitLockDays());
                        detail.setInitPrincipal(df.format(iPlanAccount.getInitPrincipal() / 100.0));
                        detail.setTransferAmt(df.format(transferAmt / 100.0));
                        detail.setArrivedAmt(df.format(arrivedAmt / 100.0));
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

            } else {
                IPlanAppInvestManageFinishDto investManageDto = new IPlanAppInvestManageFinishDto();
                investManageDto.setPageType(IPlanAppInvestManageDto.PAGE_TYPE_FINISH);
                List<IPlanTransLog> exitList = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT,
                                IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED)));
                //累计投资，累计收益
                double principal = 0.0;
                double interest = 0.0;
                for (IPlanTransLog iPlanTransLog : exitList) {
                	IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
                	//新老版本兼容，5.6.2版本之前剔除月月盈天标
                    if("1".equals(iPlan.getInterestAccrualType()+"")){
                    	if(StringUtils.isBlank(requestSource)||(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
                    		continue;
                    	}
                    }
                    IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
                    principal += iPlanAccount.getInitPrincipal() / 100.0;
                    interest += (iPlanAccount.getIplanPaidInterest()
                            + iPlanAccount.getIplanPaidBonusInterest()
                            + iPlanAccount.getIplanPaidVipInterest()) / 100.0;
                }
                investManageDto.setAmount(df.format(principal));//累计投资
                investManageDto.setInterest(df.format(interest));//累计收益
                List<IPlanTransLog> exitList2 = iPlanTransLogService.getByPageHelper(userId, iplanType,
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT,
                                IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED)), pageNum, pageSize);
                List<IPlanAppInvestManageFinishDto.Detail> details = new ArrayList<>();
                for (IPlanTransLog iPlanTransLog : exitList2) {
                    Integer accountId = iPlanTransLog.getAccountId();
                    IPlanAccount iPlanAccount = iPlanAccountService.findById(accountId);
                    if (null == iPlanAccount) {
                        logger.warn("iPlanTransLog[id={}]，查询：iPlanAccount[id={}]，查无数据", iPlanTransLog.getId(), iPlanTransLog.getIplanId());
                        throw new ProcessException(Error.NDR_DATA_ERROR);
                    }
                    IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
                    if (null == iPlan) {
                        logger.warn("iPlanAccount[id={}]，查询：IPlan[id={}]，查无数据", iPlanAccount.getId(), iPlanAccount.getIplanId());
                        throw new ProcessException(Error.NDR_DATA_ERROR);
                    }
                	//新老版本兼容，5.6.2版本之前剔除月月盈天标
                    if("1".equals(iPlan.getInterestAccrualType()+"")){
                    	if(StringUtils.isBlank(requestSource)||(requestSource.contains("ios")&&requestSource.compareTo("ios_5.6.2")<0)){
                    		continue;
                    	}
                    }
                    IPlanAppInvestManageFinishDto.Detail detail = new IPlanAppInvestManageFinishDto.Detail();
                    String vipFlag = "";
                    if (iPlanAccount.getVipRate() != null && iPlanAccount.getVipRate().doubleValue() > 0) {
                        vipFlag = "vip";
                    }
                    detail.setVipFlag(vipFlag);
                    detail.setId(iPlanAccount.getId());//已完成页面没有投资详情
                    detail.setName(iPlan.getName());
                    String endTime = (iPlan.getEndTime() == null || iPlan.getEndTime().length() < 10) ? "生成中" : iPlan.getEndTime().substring(0, 10);
                    detail.setEndTime(endTime);
                    detail.setStatus(iPlanAccount.getStatus());//1已到期，2提前退出
                    detail.setTerm(iPlan.getTerm());
                    detail.setHoldingAmt(String.valueOf(iPlanAccount.getInitPrincipal() / 100.0));//购买金额
                    detail.setInterest(String.valueOf((iPlanAccount.getInitPrincipal()
                            + iPlanAccount.getIplanPaidInterest()
                            + iPlanAccount.getIplanPaidBonusInterest()
                            + iPlanAccount.getIplanPaidVipInterest()
                            - iPlanAccount.getExitFee()) / 100.0));//已赚利息
                    detail.setUrl("");
                    //活动标图片路径
                    Integer activityId = iPlan.getActivityId();
                    ActivityMarkConfigure activityMarkConfigure = null;
                    if (null != activityId) {
                        detail.setImgUrl(activityMarkConfigureService.getImgUrl(iPlan.getActivityId()));
                        activityMarkConfigure = activityMarkConfigureService.findById(activityId);
                        if(iPlan.getIplanType() == 2 && activityMarkConfigure.getIncreaseTerm() != null){
                            detail.setAddTerm(activityMarkConfigure.getIncreaseTerm());
                        }else {
                            detail.setAddTerm(0);
                        }
                    }
                    if (Objects.equals(iPlan.getNewbieOnly(), IPlan.NEWBIE_ONLY_Y)) {
                        detail.setImgUrl(activityMarkConfigureService.getNewBieUrl());
                        detail.setNewbieOnly(IPlan.NEWBIE_ONLY_Y);
                        if(iPlan.getIplanType() == 2 && activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm()!=null){
                            detail.setImgUrl(activityMarkConfigureService.getImgUrl(activityId));
                        }
                    }
                /*if (Objects.equals(iPlanAccount.getStatus(), IPlanAccount.STATUS_ADVANCED_EXIT)) {
                    detail.setTime(iPlanTransLog.getTransTime());//提前退出时间
                } else {
                    detail.setTime(iPlan.getEndTime());//正常退出时间
                }*/
                    //退出时间
                    detail.setTime(iPlanTransLog.getTransTime());
                  //前端显示期限格式
                    detail.setTermStr(iPlan.getTerm()+"个月");
                    if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
                        if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                            detail.setTermStr(iPlan.getDay()+"天");
                        }
                    }
                    // 月月盈提前还款
                    if (IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())) {
                        detail.setBeforeRepayFlag(BaseIPlanDto.BEFORE_REPAY_FLAG_Y);
                        detail.setBeforeRepayTime(iPlanAccount.getUpdateTime());
                        IPlanRepayDetail repayDetail = iPlanRepayDetailDao.findByUserIdAndIPlanIdAndClean(iPlanAccount.getUserId(), iPlanAccount.getIplanId());
                        double beforeRepayPrincipal = 0;
                        double beforeRepayInterest = 0;
                        double beforeRepayBonusInterest = 0;
                        if (repayDetail != null) {
                            beforeRepayPrincipal = repayDetail.getRepayPrincipal() != null ? repayDetail.getRepayPrincipal().doubleValue() : 0;
                            beforeRepayInterest = repayDetail.getRepayInterest() != null ? repayDetail.getRepayInterest().doubleValue() : 0;
                            beforeRepayBonusInterest = repayDetail.getRepayBonusInterest() != null ? repayDetail.getRepayBonusInterest().doubleValue() : 0;
                        }
                        detail.setBeforeRepayAmt(df4.format((beforeRepayPrincipal + beforeRepayInterest + beforeRepayBonusInterest)/100));
                    }

                    details.add(detail);
                }
                investManageDto.setDetails(details);
                return new RestResponseBuilder<>().success(investManageDto);
            }

        } else {
            throw new IllegalArgumentException("参数type不支持！");
        }
    }

    /**
     * app - 理财计划投资详情页（兼容转让中详情，持有中详情，已完成详情）
     *
     * @param userId    用户id
     * @param id        每个页面的id不同，在投资管理页面中传过来的
     */
    @GetMapping("/authed/iplan/invest/detail/{userId}/{type}/{id}")
    public RestResponse getInvestDetailPage(@PathVariable("userId") String userId, @PathVariable("type") int type,
                                            @PathVariable("id") Integer id) {

        if (type == IPlanAppInvestManageDto.PAGE_TYPE_HOLDING) {
            IPlanAccount iPlanAccount =  iPlanAccountService.getByIPlanIdAndUserId(id, userId);
            if (null == iPlanAccount) {
                logger.warn("根据id[{}]和userId[{}]查询IPlanAccount，查无数据",id,userId);
                throw new ProcessException(Error.NDR_0452);
            }IPlan iPlan = iPlanService.getIPlanById(id);
            if (null == iPlan) {
                throw new ProcessException(Error.NDR_0428);
            }
            double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
            double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
            double vipRate = iPlanAccount.getVipRate() != null ? iPlanAccount.getVipRate().doubleValue() : 0;
            IPlanAppInvestDetailHoldDto detail = new IPlanAppInvestDetailHoldDto();
            detail.setiPlanId(iPlan.getId());
            detail.setStatus(iPlan.getStatus());
            detail.setName(iPlan.getName());
            detail.setTxnTime(iPlanAccount.getCreateTime());
            detail.setLockDays(iPlanAccountService.getNewLock(iPlan));
            String raiseCloseTime = iPlan.getRaiseFinishTime();
            if(!StringUtils.isNotBlank(raiseCloseTime)){
                raiseCloseTime = iPlan.getRaiseCloseTime();
            }
            LocalDateTime lockEndDate;
            LocalDateTime endDate;
            String lockEndTimeStr;
            String endTimeStr;
            String expectRaiseCloseTime = "";
            if (iPlan.getRaiseFinishTime() == null) {
                String raiseOpenTime = iPlan.getRaiseOpenTime();
                LocalDateTime raiseOpenDateTime = DateUtil.parseDateTime(raiseOpenTime, DateUtil.DATE_TIME_FORMATTER_19);
                LocalDateTime raiseCloseDateTime = raiseOpenDateTime.plusDays(iPlan.getRaiseDays());//预期募集结束时间
                expectRaiseCloseTime = DateUtil.getDateTimeStr(raiseCloseDateTime, DateUtil.DATE_TIME_FORMATTER_19);
                lockEndDate = raiseCloseDateTime.plusDays(iPlan.getExitLockDays());
                endDate = raiseCloseDateTime.plusMonths(iPlan.getTerm());
                /*lockEndTimeStr = "预计" + lockEndDate.toString().substring(0, 10);*/
                lockEndTimeStr = "生成中";
                endTimeStr = "生成中";
                detail.setRaiseDays("预计" + iPlan.getRaiseDays() + "天");
            } else {
                LocalDateTime raiseCloseDate = DateUtil.parseDateTime(raiseCloseTime, DateUtil.DATE_TIME_FORMATTER_19);
                lockEndDate = raiseCloseDate.plusDays(iPlan.getExitLockDays());
                lockEndTimeStr = lockEndDate.toString().substring(0, 10);
                endTimeStr = iPlan.getEndTime().substring(0,10);
                detail.setRaiseDays(iPlan.getRaiseDays() + "天");
                if(iPlanAccountService.isNewIplan(iPlan)){
                    String date = (String) iPlanAccountService.getMax(iPlanAccount).get("date");
                    if(!"0".equals(date)){
                        LocalDate localDate = DateUtil.parseDate(date, DateUtil.DATE_TIME_FORMATTER_8);
                        endTimeStr =localDate.toString();
                    }
                }
            }
            detail.setRaiseCloseTime(raiseCloseTime);
            detail.setLockEndTime(lockEndTimeStr);
            detail.setEndTime(endTimeStr);
            detail.setNewLock(iPlanAccountService.getNewLock(iPlan));
            detail.setFixRate(df4.format(iPlan.getFixRate().doubleValue() * 100) + "%");
            ActivityMarkConfigure activityMarkConfigure = null;
            if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                    detail.setFixRate(df4.format(iPlan.getFixRate().doubleValue() * 100 + activityMarkConfigure.getIncreaseInterest()) + "%");
                }

            }
            detail.setTotalRateStr(df4.format((fixRate + bonusRate + vipRate) * 100) + "%");
            if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null){
                detail.setTotalRateStr(df4.format((fixRate + vipRate) * 100) + "%");
            }

            //前端显示期限格式
            detail.setTerm(iPlan.getTerm()+"个月");
            if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
                if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                    detail.setTerm(iPlan.getDay()+"天");
                }
            }
            double currentPrincipal = iPlanAccount.getCurrentPrincipal() / 100.0;
            double paidInterest = (iPlanAccount.getIplanPaidInterest() + iPlanAccount.getIplanPaidBonusInterest()) / 100.0;
            detail.setAmt(df4.format(currentPrincipal) + "元");//持有本金
            detail.setPaidInterest(df4.format(paidInterest) + "元");//已付利息
            detail.setDaysToTransfer((int)DateUtil.betweenDays(LocalDate.now(), lockEndDate.toLocalDate()));//剩余多少天可以转出
            //转出页面
            if(iPlan.getIplanType().equals(IPlan.IPLAN_TYPE_YJT)){
                detail.setAccountId(iPlanAccount.getId());
                //查询转让数据
                List<IPlanTransLog> outTransLogList = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                        new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING,IPlanTransLog.TRANS_STATUS_SUCCEED)));
                //转让费用
                Double totalFee = 0.0;
                //转让中金额
                Integer transferingAmt = 0;
                Integer totalTransferAmt = 0;
                if(!outTransLogList.isEmpty()){
                    for (IPlanTransLog iPlanTransLog : outTransLogList) {
                        totalFee += iPlanAccountService.calcTotalFee(iPlanTransLog.getId());
                        transferingAmt +=iPlanAccountService.calcTotalTransferingAmt(iPlanTransLog.getId());
                        if(IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iPlanTransLog.getTransStatus())){
                            totalTransferAmt += iPlanTransLog.getTransAmt();
                        }else{
                            totalTransferAmt += iPlanTransLog.getProcessedAmt();
                        }
                    }
                }
                detail.setTransferFee(df4.format(totalFee)+"元");
                //转让金额
                detail.setTransferAmt(df4.format(totalTransferAmt/100.0)+"元");
                //转让中金额
                detail.setTransferingAmt(transferingAmt/100.0+"元");
                List<IPlanAppInvestDetailDto.Detail> details = new ArrayList<>();
                List<Credit> credits = creditService.findByUserIdAndSourceChannelAndAccountIdAndCreditStatus(iPlanAccount.getUserId(),Credit.SOURCE_CHANNEL_YJT,iPlanAccount.getId());
                Map<String, List<Credit>> creditsMap = credits.stream().collect(Collectors.groupingBy(Credit::getSubjectId));
                if (creditsMap != null && !creditsMap.isEmpty()) {
                    for (Map.Entry<String, List<Credit>> entry : creditsMap.entrySet()) {
                        String subjectId = entry.getKey();
                        List<Credit> creditList = entry.getValue();
                        if (creditList != null && creditList.size() > 0) {
                            Credit credit = creditList.get(0);
                            IPlanAppInvestDetailDto.Detail detail1 = new IPlanAppInvestDetailDto.Detail();
                            Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
                            detail1.setName(subject.getName());
                            detail1.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
                            detail1.setSubjectId(credit.getSubjectId());
                            detail1.setTotalAmt(subject.getTotalAmt()/100.0);
                            detail1.setCreditId(credit.getId());
                            detail1.setStatus(credit.getCreditStatus());
                            details.add(detail1);
                        }
                    }
                }
                /*if(!credits.isEmpty()){
                    for (Credit credit:credits) {
                        IPlanAppInvestDetailDto.Detail detail1 = new IPlanAppInvestDetailDto.Detail();
                        Subject subject = subjectService.findSubjectBySubjectId(credit.getSubjectId());
                        detail1.setName(subject.getName());
                        detail1.setRepayType(SubjectMobileResource.repayType(subject.getRepayType()));
                        detail1.setSubjectId(credit.getSubjectId());
                        detail1.setTotalAmt(subject.getTotalAmt()/100.0);
                        detail1.setCreditId(credit.getId());
                        detail1.setStatus(credit.getCreditStatus());
                        details.add(detail1);
                    }
                }*/
                detail.setDetails(details);
                detail.setRepayType(SubjectMobileResource.repayType(iPlan.getRepayType()));
                //红包奖励
//                InvestRedpacket investRedpacket = redPacketService.getReceivedRedPacketAmt(userId, String.valueOf(iPlanAccount.getIplanId()), RedPacket.INVEST_REDPACKET_TYPE);
//                if (investRedpacket != null && investRedpacket.getRewardMoney() != 0 && investRedpacket.getSendRedpacketTime() != null) {
//                    //红包收益
//                    detail.setRedAmt(ArithUtil.round(investRedpacket.getRewardMoney(), 2) +"元");
//                }
                detail.setTransFlag(iPlanAccountService.checkCondition(iPlanAccount));
                detail.setMessage(iPlanAccountService.checkConditionStr(iPlanAccount));
//                Integer totalPaidAmt = subjectRepayDetailDao.findByUserIdAndAccountIdAndChannel(Credit.SOURCE_CHANNEL_YJT,iPlanAccount.getUserId(),iPlanAccount.getId());
//                detail.setPaidTotalAmt(df4.format(totalPaidAmt/100.0)+"元");

                //加入记录
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
                detail.setYjtInvests(yjtInvests);
                //新版省心投相关
                if(iPlanAccountService.isNewIplan(iPlan)){
                    detail.setIsFree(IPlanAppInvestManageHoldDto.IS_FREE);
                    if(iPlan.getRaiseFinishTime() != null){
                        detail.setTotalRateStr(df4.format((fixRate + bonusRate + vipRate) * 100) + "%"+"-"+df4.format((fixRate + bonusRate + vipRate+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+ "%");
                        if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null){
                            detail.setTotalRateStr(df4.format((fixRate + vipRate) * 100) + "%"+"-"+df4.format((fixRate + vipRate+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+ "%");
                        }
                        detail.setTerm(((int)iPlanAccountService.getMax(iPlanAccount).get("term")==0?iPlan.getTerm():(int)iPlanAccountService.getMax(iPlanAccount).get("term"))+ "个月");
                    }
                    detail.setTip(GlobalConfig.NEW_IPLAN_RATE_TIP);
                }
            }else{
                if (Arrays.asList(IPlan.STATUS_RAISING_FINISH, IPlan.STATUS_EARNING).stream().noneMatch(status -> status.equals(iPlan.getStatus()))) {
                    detail.setTransferFee("");
                    detail.setActualTransferReturn("");
                } else {
                    double iPlanExitAmt = this.getExitAmt(iPlan, userId, iPlanAccount.getCurrentPrincipal(), raiseCloseTime) / 100.0;
                    IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
                    double exitFee = iPlanParam.getExitFeeRate().multiply(new BigDecimal(currentPrincipal)).intValue();
                    detail.setTransferFee(df.format(exitFee));
                    detail.setActualTransferReturn(df.format(iPlanExitAmt - exitFee));
                }
                detail.setTransferAmt(df.format(iPlanAccount.getInitPrincipal() / 100.0));
            }
            detail.setRestTerms(iPlanRepayScheduleService.getCurrentRepayTerm(iPlan.getId()));
            detail.setExpectedInterest(df4.format((iPlanAccount.getExpectedInterest()+iPlanAccount.getIplanExpectedBonusInterest())/100.0));
            detail.setUrl1(iPlanAccount.getServiceContract());
            String vipFlag = "";
            if (iPlanAccount.getVipRate() != null && iPlanAccount.getVipRate().doubleValue() > 0) {
                vipFlag = "vip";
            }
            detail.setVipFlag(vipFlag);
            return new RestResponseBuilder<>().success(detail);

        } else if (type == IPlanAppInvestManageDto.PAGE_TYPE_TRANSFERRING) {
            IPlanAppInvestDetailTransferDto detail = new IPlanAppInvestDetailTransferDto();
            IPlanTransLog iPlanTransLog = iPlanTransLogService.getById(id);
            if (null == iPlanTransLog) {
                throw new ProcessException(Error.NDR_0428);
            }
            IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
            if (null == iPlan) {
                logger.warn("iPlanTransLog[id={}]，查询：IPlan[id={}]，查无数据", iPlanTransLog.getId(), iPlanTransLog.getIplanId());
                throw new ProcessException(Error.NDR_DATA_ERROR);
            }
            IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());
            if (null == iPlanAccount) {
                logger.warn("iPlanTransLog[id={}]，查询：IPlanAccount[id={}]，查无数据", iPlanTransLog.getId(), iPlanTransLog.getAccountId());
                throw new ProcessException(Error.NDR_DATA_ERROR);
            }
            detail.setId(iPlan.getId());
            detail.setName(iPlan.getName());
            //转让总额（元）
            detail.setAmt(String.valueOf(iPlanAccount.getInitPrincipal() / 100.0)+"元");
            //转出费用
            double exitFee = iPlanAccount.getExitFee() / 100.0;
            detail.setTransferFee(df.format(exitFee));
            //预计到账
            detail.setExpectedAmt(df.format(iPlanTransLog.getTransAmt()/100.0-exitFee));
            //申请转让时间
            String transTime = iPlanTransLog.getTransTime();
            detail.setTxnTime(transTime);
            //持有天数
            LocalDate raiseCloseDate = DateUtil.parseDate(iPlan.getRaiseCloseTime(), DateUtil.DATE_TIME_FORMATTER_19);//募集结束时间
            LocalDate localDateTransfer = DateUtil.parseDate(transTime, DateUtil.DATE_TIME_FORMATTER_19);//转出时间
            LocalDate localDateJoin = DateUtil.parseDate(iPlanAccount.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19);//开放募集时间
            long holdingDays = DateUtil.betweenDays(localDateJoin, localDateTransfer);
            detail.setHoldingDays((int)holdingDays);
            //剩余天数=项目结束日期-转让时间
            LocalDate iPlanEndTime = DateUtil.parseDate(iPlan.getEndTime(), DateUtil.DATE_TIME_FORMATTER_19);//开放募集时间
            long restDays = DateUtil.betweenDays(localDateTransfer, iPlanEndTime);
            double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
            double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
            double vipRate = iPlanAccount.getVipRate() != null ? iPlanAccount.getVipRate().doubleValue() : 0;
            detail.setRestDays((int)restDays);
            detail.setStatus(iPlan.getStatus());
            detail.setRaiseCloseTime(iPlan.getRaiseCloseTime());
            detail.setTransferContract(iPlanAccount.getServiceContract());
            detail.setTotalRateStr(df4.format((fixRate + bonusRate + vipRate) * 100) + "%");
            String vipFlag = "";
            if (iPlanAccount.getVipRate() != null && iPlanAccount.getVipRate().doubleValue() > 0) {
                vipFlag = "vip";
            }
            detail.setVipFlag(vipFlag);

            // 月月盈提前还款
            if (IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN.equals(iPlanTransLog.getTransType())) {
                detail.setBeforeRepayFlag(BaseIPlanDto.BEFORE_REPAY_FLAG_Y);
                detail.setBeforeRepayTime(iPlanAccount.getUpdateTime());
            }

            return new RestResponseBuilder<>().success(detail);

        } else if (type == IPlanAppInvestManageDto.PAGE_TYPE_FINISH) {
            IPlanAccount iPlanAccount =  iPlanAccountService.findById(id);
            if (null == iPlanAccount) {
                logger.warn("根据id[{}]和userId[{}]查询IPlanAccount，查无数据",id,userId);
                throw new ProcessException(Error.NDR_0452);
            }
            IPlan iPlan = iPlanService.getIPlanById(iPlanAccount.getIplanId());
            if (null == iPlan) {
                throw new ProcessException(Error.NDR_0428);
            }
            if (iPlan.getIplanType().equals(IPlan.IPLAN_TYPE_YJT)) {
                //一键投
                YjtInvestDetailFinishDto dto = new YjtInvestDetailFinishDto();
                dto.setId(iPlanAccount.getId());
                dto.setIplanId(iPlanAccount.getIplanId());
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
                //锁定期结束时间=实际结束募集时间+锁定期天数
                String raiseCloseTime = iPlan.getRaiseFinishTime() == null ? iPlan.getRaiseCloseTime():iPlan.getRaiseFinishTime();
                LocalDate raiseCloseDate = DateUtil.parseDate(raiseCloseTime, DateUtil.DATE_TIME_FORMATTER_19);
                LocalDate lockEndDate = raiseCloseDate.plusDays(iPlan.getExitLockDays());
                dto.setLockEndTime(lockEndDate.toString());
                dto.setRepayType(SubjectMobileResource.repayType(iPlan.getRepayType()));
                dto.setCreateTime(iPlanAccount.getCreateTime());
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
                dto.setTransferFee(df.format(transferTotalFee));
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
                //已到账本息
                double arrivedAmt = 0.0;
                //正常结束的一键投总还款金额
                arrivedAmt +=  subjectRepayDetailDao.findByUserIdAndAccountIdAndChannel(Credit.SOURCE_CHANNEL_YJT, iPlanAccount.getUserId(), iPlanAccount.getId());
                //转让交易记录
                List<IPlanTransLog> transerLogs = iPlanTransLogService.getByUserIdAndIPlanIdAndTransStatusAndTransTypeIn(iPlanAccount.getUserId(),iPlanAccount.getIplanId(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED))).stream().filter(iPlanTransLog -> iPlanTransLog.getActualAmt() > 0).collect(Collectors.toList());
                arrivedAmt += transerLogs.stream().map(IPlanTransLog::getActualAmt).reduce(Integer::sum).orElse(0);
                dto.setArrivedAmt(df.format(arrivedAmt / 100.0));
                double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
                double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
                double vipRate = iPlanAccount.getVipRate() != null ? iPlanAccount.getVipRate().doubleValue() : 0;
                //新版省心投相关
                if(iPlanAccountService.isNewIplan(iPlan)){
                    dto.setRate(df4.format((fixRate + bonusRate + vipRate) * 100) + "%"+"-"+df4.format((fixRate + bonusRate + vipRate+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+ "%");
                    if(activityMarkConfigure != null && activityMarkConfigure.getIncreaseTerm() != null){
                        dto.setRate(df4.format((fixRate + vipRate) * 100) + "%"+"-"+df4.format((fixRate + vipRate+iPlanAccountService.getActualMaxRate(iPlanAccount))*100)+ "%");
                    }
                    dto.setIsFree(IPlanAppInvestManageHoldDto.IS_FREE);
                    dto.setTerm(((int)iPlanAccountService.getMax(iPlanAccount).get("term")==0?iPlan.getTerm():(int)iPlanAccountService.getMax(iPlanAccount).get("term"))+ "个月");

                }
                return new RestResponseBuilder<>().success(dto);
            } else {
                //月月盈
                IPlanAppInvestDetailFinishDto detail = new IPlanAppInvestDetailFinishDto();

                //已完成（正常退出和提前退出）

                IPlanTransLog transLog = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(userId, iPlanAccount.getIplanType(), new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_ADVANCED_EXIT, IPlanTransLog.TRANS_TYPE_NORMAL_EXIT, IPlanTransLog.TRANS_TYPE_IPLAN_CLEAN))
                        , new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED))).stream().filter(iPlanTransLog -> iPlanTransLog.getAccountId().equals(iPlanAccount.getId())).collect(Collectors.toList()).get(0);

                detail.setId(iPlan.getId());
                detail.setContract(iPlanAccount.getServiceContract());
                detail.setName(iPlan.getName());
                detail.setTxnTime(iPlanAccount.getCreateTime());
                detail.setLockDays(iPlanAccountService.getNewLock(iPlan));
                detail.setRaiseCloseTime(iPlan.getRaiseCloseTime());
                detail.setFixRate(iPlan.getFixRate().doubleValue()*100 + "%");
                if (iPlan.getActivityId() != null && iPlan.getActivityId() > 0) {
                    ActivityMarkConfigure activityMarkConfigure = activityMarkConfigureService.findById(iPlan.getActivityId());
                    if (activityMarkConfigure != null && activityMarkConfigure.getIncreaseInterest() != null && activityMarkConfigure.getIncreaseInterest() > 0) {
                        detail.setFixRate(df3.format(iPlan.getFixRate().doubleValue() * 100 + activityMarkConfigure.getIncreaseInterest()) + "%");
                    }
                }
                Integer allIPlanExitAmt = transLog.getTransAmt();
                Integer exitFee = iPlanAccount.getExitFee();
                Integer iPlanExitAmt = allIPlanExitAmt - exitFee;
                IPlanParam iPlanParam = iPlanParamService.getIPlanParamById(iPlan.getIplanParamId());
                //前端显示期限格式
                detail.setTerm(iPlan.getTerm()+"个月");
                if(IPlan.IPLAN_TYPE_TP.equals(iPlan.getIplanType())){
                    if(IPlan.INTEREST_ACCRUAL_TYPE_DAY.equals(iPlan.getInterestAccrualType())){
                        detail.setTerm(iPlan.getDay()+"天");
                    }
                }
                detail.setAmt(df.format(iPlanAccount.getInitPrincipal() / 100.0) + "元");
                Integer iPlanPaidInterest = iPlanAccount.getIplanPaidInterest() + (iPlanAccount.getIplanPaidBonusInterest() == null ? 0 : iPlanAccount.getIplanPaidBonusInterest());
                detail.setPaidInterest(String.valueOf(iPlanPaidInterest / 100.0) + "元");//已获收益
                detail.setTransferFee(df.format(exitFee / 100.0));
                detail.setActualTransferReturn(df.format(iPlanExitAmt / 100.0));
                String raiseCloseTime = iPlan.getRaiseFinishTime() == null ? iPlan.getRaiseCloseTime():iPlan.getRaiseFinishTime();
                LocalDate raiseCloseDate = DateUtil.parseDate(raiseCloseTime, DateUtil.DATE_TIME_FORMATTER_19);
                LocalDate lockEndDate = raiseCloseDate.plusDays(iPlan.getExitLockDays());
                detail.setLockEndTime(lockEndDate.toString());
                LocalDate localDateJoin = DateUtil.parseDate(iPlanAccount.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19);//开放募集时间
                LocalDate localDateTransfer = DateUtil.parseDate(transLog.getTransTime(), DateUtil.DATE_TIME_FORMATTER_19);//转出时间
                LocalDate localDate = localDateJoin.plusMonths(iPlan.getTerm());
                long holdingDays = DateUtil.betweenDays(localDateJoin, localDateTransfer);
                LocalDate iPlanEndTime = DateUtil.parseDate(iPlan.getEndTime(), DateUtil.DATE_TIME_FORMATTER_19);//计划结束时间
                //剩余天数
                long restDays = DateUtil.betweenDays(localDateTransfer, iPlanEndTime);
                detail.setHoldingDays((int) holdingDays);
                if (Objects.equals(IPlanAccount.STATUS_ADVANCED_EXIT, iPlanAccount.getStatus())) {
                    detail.setRestDays((int) restDays);
                } else {
                    detail.setRestDays(0);
                }
                double fixRate = iPlan.getFixRate() != null ? iPlan.getFixRate().doubleValue() : 0;
                double bonusRate = iPlan.getBonusRate() != null ? iPlan.getBonusRate().doubleValue() : 0;
                double vipRate = iPlanAccount.getVipRate() != null ? iPlanAccount.getVipRate().doubleValue() : 0;
                detail.setTotalRateStr(df4.format((fixRate + bonusRate + vipRate) * 100) + "%");
                String vipFlag = "";
                if (iPlanAccount.getVipRate() != null && iPlanAccount.getVipRate().doubleValue() > 0) {
                    vipFlag = "vip";
                }
                detail.setVipFlag(vipFlag);

                // 月月盈提前还款
                if (IPlanAccount.STATUS_CLEAN.equals(iPlanAccount.getStatus())) {
                    detail.setBeforeRepayFlag(BaseIPlanDto.BEFORE_REPAY_FLAG_Y);
                    detail.setBeforeRepayTime(iPlanAccount.getUpdateTime());
                }

                return new RestResponseBuilder<>().success(detail);
            }

        } else {
            throw new IllegalArgumentException("参数type不支持！");
        }
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


    //一键投转让管理
    @GetMapping("/yjt/credit/manage")
    public RestResponse creditInvestManage(@RequestParam("userId") String userId,
                                           @RequestParam("id") Integer accountId,
                                           @RequestParam("type") Integer type,
                                           @RequestParam(value = "pageNo", defaultValue = "1") int pageNum,
                                           @RequestParam(value = "pageSize", defaultValue = "10") int pageSize){
        if(type.equals(YjtCreditManageTransferDto.PAGE_TYPE_HOLDING)){
            YjtCreditManageTransferDto creditManageTransferDto = new YjtCreditManageTransferDto();
            List<YjtCreditManageTransferDto.Detail> details = new ArrayList<>();
            IPlanAccount iPlanAccount = iPlanAccountService.findById(accountId);
            //查询转让数据
            List<IPlanTransLog> outTransLogList = iPlanTransLogService.getYjtTransLog(iPlanAccount.getUserId(),iPlanAccount.getIplanId(),
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_PROCESSING,IPlanTransLog.TRANS_STATUS_SUCCEED)),pageNum,pageSize);
            //转让费用
            Double totalFee = 0.0;
            Integer totalAmt = 0;
            for (IPlanTransLog iPlanTransLog : outTransLogList) {
                totalFee += iPlanAccountService.calcTotalFee(iPlanTransLog.getId());
                if(IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iPlanTransLog.getTransStatus())){
                    totalAmt += iPlanTransLog.getTransAmt();
                }else{
                    totalAmt += iPlanTransLog.getProcessedAmt();
                }
            }
            creditManageTransferDto.setAmount(totalAmt/100.0);
            creditManageTransferDto.setAmountStr(df4.format(creditManageTransferDto.getAmount()));

            creditManageTransferDto.setFeeAmt(totalFee);
            creditManageTransferDto.setFeeAmtStr(df4.format(creditManageTransferDto.getFeeAmt()));

            for (IPlanTransLog iPlanTransLog : outTransLogList) {
                YjtCreditManageTransferDto.Detail detail = new YjtCreditManageTransferDto.Detail();
                detail.setId(iPlanTransLog.getId());
                detail.setTransferAmt(iPlanTransLog.getTransAmt() / 100.0);
                detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                detail.setDealAmt(iPlanAccountService.calcDealAmt(iPlanTransLog.getId())/100.0);
                detail.setDealAmtStr(df4.format(detail.getDealAmt()));
                detail.setStatus(iPlanAccountService.cancelStatus(iPlanTransLog.getId()));
                detail.setTime(iPlanTransLog.getCreateTime().substring(0,10));
                if(iPlanTransLog.getTransStatus()==4 || iPlanTransLog.getTransStatus()==0){
                    detail.setType("转让中");
                }else if(iPlanTransLog.getTransStatus()==1){
                    if(iPlanTransLog.getTransType() == 10 && iPlanTransLog.getActualAmt()!=0 && (!iPlanTransLog.getTransAmt().equals(iPlanTransLog.getProcessedAmt()))){
                        detail.setType("部分完成");
                    }else if(iPlanTransLog.getTransType() == 10 && iPlanTransLog.getActualAmt()==0){
                        detail.setType("全部撤销");
                    }else{
                        detail.setType("已完成");
                    }
                }
                details.add(detail);
            }
            creditManageTransferDto.setDetails(details);
            return new RestResponseBuilder<>().success(creditManageTransferDto);
        }else{
            YjtCreditManageTransferDto creditManageTransferDto = new YjtCreditManageTransferDto();
            List<YjtCreditManageTransferDto.Detail> details = new ArrayList<>();
            IPlanAccount iPlanAccount = iPlanAccountService.findById(accountId);
            //查询转让数据
            List<IPlanTransLog> outTransLogList = iPlanTransLogService.getByUserIdAndTransTypeInAndTransStatusIn(iPlanAccount.getUserId(),IPlan.IPLAN_TYPE_YJT,
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER, IPlanTransLog.TRANS_TYPE_IPLAN_TRANSFER_CANCEL)),
                    new HashSet<>(Arrays.asList(IPlanTransLog.TRANS_STATUS_SUCCEED)));
            List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByCondition(outTransLogList, pageNum, pageSize);
            //转让费用
            Double totalFee = 0.0;
            Integer totalAmt = 0;
            for (IPlanTransLog iPlanTransLog : iPlanTransLogs) {
                totalFee += iPlanAccountService.calcTotalFee(iPlanTransLog.getId());
                totalAmt += iPlanTransLog.getTransAmt();
            }
            creditManageTransferDto.setAmount(totalAmt/100.0);
            creditManageTransferDto.setAmountStr(df4.format(creditManageTransferDto.getAmount()));

            creditManageTransferDto.setFeeAmt(totalFee);
            creditManageTransferDto.setFeeAmtStr(df4.format(creditManageTransferDto.getFeeAmt()));

            for (IPlanTransLog iPlanTransLog : iPlanTransLogs) {
                YjtCreditManageTransferDto.Detail detail = new YjtCreditManageTransferDto.Detail();
                detail.setId(iPlanTransLog.getId());
                detail.setTransferAmt(iPlanTransLog.getTransAmt() / 100.0);
                detail.setTransferAmtStr(df4.format(detail.getTransferAmt()));
                detail.setDealAmt(iPlanAccountService.calcDealAmt(iPlanTransLog.getId())/100.0);
                detail.setDealAmtStr(df4.format(detail.getDealAmt()));
                detail.setStatus(iPlanAccountService.cancelStatus(iPlanTransLog.getId()));
                detail.setTime(iPlanTransLog.getCreateTime().substring(0,10));
                if(iPlanTransLog.getTransStatus()==4 || iPlanTransLog.getTransStatus()==0){
                    detail.setType("转让中");
                }else if(iPlanTransLog.getTransStatus()==1){
                    if(iPlanTransLog.getTransType() == 10 && iPlanTransLog.getActualAmt()!=0 && (!iPlanTransLog.getTransAmt().equals(iPlanTransLog.getProcessedAmt()))){
                        detail.setType("部分完成");
                    }else if(iPlanTransLog.getTransType() == 10 && iPlanTransLog.getActualAmt()==0){
                        detail.setType("全部撤销");
                    }else{
                        detail.setType("已完成");
                    }
                }
                details.add(detail);
            }
            creditManageTransferDto.setDetails(details);
            return new RestResponseBuilder<>().success(creditManageTransferDto);
        }

    }

    @GetMapping("/yjt/manage/creditTransferDetail")
    public RestResponse getAppCreditTransferDetailDto(@RequestParam("id") Integer transLogId,
                                                      @RequestParam(value = "userId") String userId) {

        IPlanTransLog iPlanTransLog = iPlanTransLogService.getByIdLocked(transLogId);
        if (iPlanTransLog == null) {
            throw new ProcessException(Error.NDR_0202);
        }
        AppCreditTransferDetailDto appCreditTransferDetailDto = new AppCreditTransferDetailDto();
        CreditOpening creditOpening = creditOpeningDao.findByTransLogIdAllNoConditon(transLogId);
        appCreditTransferDetailDto.setId(transLogId);

        appCreditTransferDetailDto.setName("暂时不用");
        //还款方式
        appCreditTransferDetailDto.setRepayType("暂时不用");
        //出售金额
        appCreditTransferDetailDto.setSaleAmt(iPlanTransLog.getTransAmt() / 100.0);
        appCreditTransferDetailDto.setSaleAmtStr(df4.format(appCreditTransferDetailDto.getSaleAmt()));

        //折让率
        appCreditTransferDetailDto.setTransDiscount(creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue());
        appCreditTransferDetailDto.setTransDiscountStr(df4.format(appCreditTransferDetailDto.getTransDiscount())+"%");

        //投资金额
        appCreditTransferDetailDto.setInvestAmt(0.0);
        appCreditTransferDetailDto.setInvestAmtStr("暂时不用");

        //投资日期
        appCreditTransferDetailDto.setInvestTime("暂时不用");
        IPlan iPlan = iPlanService.getIPlanById(iPlanTransLog.getIplanId());
        IPlanAccount iPlanAccount = iPlanAccountService.findById(iPlanTransLog.getAccountId());

        //暂时不用
        appCreditTransferDetailDto.setHoldDay(0);

        //成交金额
        Integer totalDealAmt = iPlanAccountService.calcDealAmt(iPlanTransLog.getId());
        appCreditTransferDetailDto.setReceivedAmt(totalDealAmt/100.0);
        appCreditTransferDetailDto.setReceivedAmtStr(df4.format(appCreditTransferDetailDto.getReceivedAmt()));

        //转让时间
        appCreditTransferDetailDto.setTransferTime(creditOpening.getCreateTime().substring(0,10));

        //散标交易配置信息
        SubjectTransferParam subjectTransferParam = subjectTransferParamDao.findByTransferParamCode(iPlan.getTransferParamCode());

        if(StringUtils.isNotBlank(iPlanTransLog.getUpdateTime())){
            appCreditTransferDetailDto.setEndTime(DateUtil.parseDate(iPlanTransLog.getUpdateTime(), DateUtil.DATE_TIME_FORMATTER_19).toString());
        }else{
            LocalDate date = DateUtil.parseDate(iPlanTransLog.getCreateTime(), DateUtil.DATE_TIME_FORMATTER_19).plusDays(subjectTransferParam.getAutoRevokeTime());
            appCreditTransferDetailDto.setEndTime(date.toString());
        }
        //转让服务费
        Double feeRate = iPlanAccountService.calcTransFee(subjectTransferParam,iPlan);
        appCreditTransferDetailDto.setFee((totalDealAmt/100.0) * feeRate / 100.0);
        appCreditTransferDetailDto.setFeeStr(df4.format(appCreditTransferDetailDto.getFee()));

        //扣除红包奖励
        Double redFee = iPlanAccountService.calcRedFeeFinish(iPlanAccount,iPlanTransLog.getCreateTime(),subjectTransferParam);
        appCreditTransferDetailDto.setRedFee((totalDealAmt /100.0)   * redFee);
        appCreditTransferDetailDto.setRedFeeStr(df4.format(appCreditTransferDetailDto.getRedFee()));

        //溢价手续费
        Double overFee = 0.0;
        Double transferDiscount = creditOpening.getTransferDiscount().multiply(new BigDecimal(100)).doubleValue();
        if(transferDiscount > 100){
            overFee = (totalDealAmt /100.0) * (transferDiscount - 100) / 100.0 * 0.2;
        }
        appCreditTransferDetailDto.setOverFee(overFee);
        appCreditTransferDetailDto.setOverFeeStr(df4.format(overFee));

        //预计到账金额
        Double expectAmt = ArithUtil.calcExp((creditOpening.getTransferPrincipal() /100.0) * (transferDiscount / 100.0),appCreditTransferDetailDto.getRedFee(),appCreditTransferDetailDto.getOverFee(),appCreditTransferDetailDto.getFee());
        appCreditTransferDetailDto.setExpectAmt(0.0);
        appCreditTransferDetailDto.setExpectAmtStr("暂时不用");


        //是否可撤销
        Integer status = iPlanAccountService.cancelStatus(transLogId);
        appCreditTransferDetailDto.setStatus(status);

        if(IPlanTransLog.TRANS_STATUS_PROCESSING.equals(iPlanTransLog.getTransStatus())){
            appCreditTransferDetailDto.setDesc("转让中");
        }else  if(iPlanTransLog.getTransStatus()==1){
            if(iPlanTransLog.getTransType() == 10 && iPlanTransLog.getActualAmt()!=0 && (!iPlanTransLog.getTransAmt().equals(iPlanTransLog.getProcessedAmt()))){
                appCreditTransferDetailDto.setDesc("部分完成");
            }else if(iPlanTransLog.getTransType() == 10 && iPlanTransLog.getActualAmt()==0){
                appCreditTransferDetailDto.setDesc("全部撤销");
            }else{
                appCreditTransferDetailDto.setDesc("已完成");
            }
        }
        appCreditTransferDetailDto.setCreditTransferUrl(transferUrl);
        appCreditTransferDetailDto.setCreditDetailUrl("暂时不用");

        return new RestResponseBuilder<>().success(appCreditTransferDetailDto);
    }

    @GetMapping("/yjt/increaseRate/{id}")
    public RestResponse getIPlanRateDto(@PathVariable("id") Integer accountId){
        if(accountId == null){
           throw new ProcessException(Error.NDR_0101);
        }
        List<IPlanRateDto> list = iPlanAccountService.getIPlanRateDto(accountId);
        return new RestResponseBuilder<>().success(list);

    }


    public static void main(String[] args) {
		System.out.println("android5.6.0".compareTo("android5.6.0"));
	}
}
