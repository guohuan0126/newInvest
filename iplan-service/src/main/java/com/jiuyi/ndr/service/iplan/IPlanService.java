package com.jiuyi.ndr.service.iplan;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.jiuyi.ndr.constant.GlobalConfig;
import com.jiuyi.ndr.dao.iplan.IPlanDao;
import com.jiuyi.ndr.datasource.ProductSlave;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.iplan.IPlan;
import com.jiuyi.ndr.domain.iplan.IPlanTransLog;
import com.jiuyi.ndr.domain.user.User;
import com.jiuyi.ndr.dto.iplan.IPlanTalentDto;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.rest.customannotation.PutRedis;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.invest.InvestService;
import com.jiuyi.ndr.service.subject.SubjectAccountService;
import com.jiuyi.ndr.service.user.UserService;
import com.jiuyi.ndr.service.xm.TransactionService;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.util.redis.RedisClient;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.RequestEstablishIntelligentProject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lixiaolei on 2017/6/9.
 */
@Service
public class IPlanService {

    private final static Logger logger = LoggerFactory.getLogger(IPlanService.class);

    @Autowired
    private IPlanDao iPlanDao;
    @Autowired
    private UserService userService;
    @Autowired
    private IPlanAccountService iPlanAccountService;
    @Autowired
    private IPlanTransLogService iPlanTransLogService;
    @Autowired
    private InvestService investService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private SubjectAccountService subjectAccountService;
    @Autowired
    private ConfigService configService;

    public static final String IPLANID_TO_CODE = "IPLANID_TO_CODE_";

    //创建投资包
    public IPlan create(IPlan iPlan) {
        return this.insert(iPlan);
    }

    //开放
    public void open(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null when open iPlan");
        }
        IPlan iPlan = iPlanDao.findById(id);
        if (iPlan == null) {
            logger.warn("can not find iPlan by iPlan id " + id);
            throw new ProcessException("", "");
        }
        if (!IPlan.STATUS_NOT_OPEN.equals(iPlan.getStatus())) {
            logger.warn("iPlan has been opened! please don't open again!");
            throw new ProcessException("", "");
        }
        iPlan.setStatus(IPlan.STATUS_ANNOUNCING);
        this.update(iPlan);
    }

    //募集
    @Transactional
    public void collect(Integer iPlanId, String investorId, Integer amount) {

    }

    //手动满标
    public void fillIPlanManual() {

    }

    //匹配标的、债权
    public void match() {

    }

    //推送
    public BaseResponse pushToXM(IPlan iPlan) {

        RequestEstablishIntelligentProject intelligentProject = new RequestEstablishIntelligentProject();
        intelligentProject.setIntelProjectNo(iPlan.getCode());
        intelligentProject.setIntelProjectName(iPlan.getName());
        intelligentProject.setIntelProjectDescription(iPlan.getName());
        intelligentProject.setAnnualInterestRate(iPlan.getFixRate().doubleValue());
        // 厦门银行-创建标的
        BaseResponse response = transactionService.establishIntelligentProject(intelligentProject);
        return response;

    }

    //到期
    public void exit() {

    }

    /**
     * 获取精选定期计划临时版
     * 2017-10-9 15:28:19 PC首页月月盈变为4个项目
     *
     * @return
     */
    @ProductSlave
    public List<IPlan> getFeaturedIPlansTemporary(String userId, @RequestParam(required = false, defaultValue = "0") int iplanType) {

        //查询用户注册来源
        /*String userSource = userService.getUserRegisterSource(userId);
        boolean registerFlag = false;
        if (org.apache.commons.lang3.StringUtils.isNotBlank(userSource) && userSource.contains(User.FAN_LI_WANG)) {
            registerFlag = true;
        }*/

        List<IPlan> featuredIPlans = new ArrayList<>(5);
        List<IPlan> yjtList = iPlanDao.findIPlanNotNewbieInvestable(IPlan.IPLAN_TYPE_TP).stream().sorted(Comparator.comparing(iPlan -> iPlan.getTerm())).collect(Collectors.toList());
        featuredIPlans.addAll(yjtList);
        if(yjtList != null && yjtList.size() < 5){
            List<IPlan> finishedYjtList =  iPlanDao.findIplanFinished(IPlan.IPLAN_TYPE_TP);
            for (int i = 0; i < 5-yjtList.size()  ; i++) {
                if(finishedYjtList != null && finishedYjtList.size() > 1){
                    featuredIPlans.add(finishedYjtList.get(i));
                }
            }
        }

        return featuredIPlans;
    }

    /**
     * 获取精选定期计划临时版
     * 2017-10-9 15:28:19 PC首页一键投变为4个项目
     *
     * @return
     */
    @ProductSlave
    public List<IPlan> getFeaturedYjtTemporary(String userId) {
        List<IPlan> featuredIPlans = new ArrayList<>(5);

        List<IPlan> yjtList = iPlanDao.findIPlanNotNewbieInvestable(IPlan.IPLAN_TYPE_YJT).stream().sorted(Comparator.comparing(iPlan -> iPlan.getTerm())).collect(Collectors.toList());
        featuredIPlans.addAll(yjtList);
        if(yjtList != null && yjtList.size() < 5){
            List<IPlan> finishedYjtList =  iPlanDao.findIplanFinished(IPlan.IPLAN_TYPE_YJT);
            for (int i = 0; i < 5-yjtList.size()  ; i++) {
                if(finishedYjtList != null && finishedYjtList.size() > 1){
                    featuredIPlans.add(finishedYjtList.get(i));
                }
            }
        }
        return featuredIPlans;
    }

    private List getIPlanListAll(List... params) {
        List resultList = new ArrayList<>();
        if (params != null && params.length > 0) {
            for (List list : params) {
                if (list != null && list.size() > 0) {
                    resultList.addAll(list);
                }
            }
        }
        return resultList;
    }
    /**
     * 获取精选定期计划
     */
    public List<IPlan> getFeaturedIPlans() {
        List<IPlan> featuredIPlans = new ArrayList<>(3);
        List<IPlan> oneTermIPlans = this.get1TermInvestable();
        List<IPlan> threeTermIPlans = this.get3TermInvestable();
        List<IPlan> sixTermIPlans = this.get6TermInvestable();
        List<IPlan> twelveTermIPlans = this.get12TermInvestable();

        List<IPlan> oneTermHeraldIPlans = this.get1TermHerald();
        List<IPlan> threeTermHeraldIPlans = this.get3TermHerald();
        List<IPlan> sixTermHeraldIPlans = this.get6TermHerald();
        List<IPlan> twelveTermHeraldIPlans = this.get12TermHerald();
        IPlan one = null;
        IPlan two = null;
        IPlan three = null;
        if (!oneTermIPlans.isEmpty() && oneTermIPlans.size() >= 1) {//一期可投
            one = oneTermIPlans.get(0);
            if (!threeTermIPlans.isEmpty() && threeTermIPlans.size() >= 1) {//三期可投
                two = threeTermIPlans.get(0);
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            } else if (!threeTermHeraldIPlans.isEmpty() && threeTermHeraldIPlans.size() >= 1) {//三期预告
                two = threeTermHeraldIPlans.get(0);
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            } else {
                two = this.get3SellOut();
                if (null == two) {
                    two = new IPlan();
                }
                two.setName("3期已售罄！");
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            }
        } else if (!oneTermHeraldIPlans.isEmpty() && oneTermHeraldIPlans.size() >= 1) {//一期预告
            one = oneTermHeraldIPlans.get(0);
            if (!threeTermIPlans.isEmpty() && threeTermIPlans.size() >= 1) {//三期可投
                two = threeTermIPlans.get(0);
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            } else if (!threeTermHeraldIPlans.isEmpty() && threeTermHeraldIPlans.size() >= 1) {//三期预告
                two = threeTermHeraldIPlans.get(0);
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            } else {
                two = this.get3SellOut();
                if (null == two) {
                    two = new IPlan();
                }
                two.setName("3期已售罄！");
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            }
        } else {//一期售罄
            one = this.get1SellOut();
            if (null == one) {
                one = new IPlan();
            }
            one.setName("1期已售罄！");
            if (!threeTermIPlans.isEmpty() && threeTermIPlans.size() >= 1) {//三期可投
                two = threeTermIPlans.get(0);
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            } else if (!threeTermHeraldIPlans.isEmpty() && threeTermHeraldIPlans.size() >= 1) {//三期预告
                two = threeTermHeraldIPlans.get(0);
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            } else {
                two = this.get3SellOut();
                if (null == two) {
                    two = new IPlan();
                }
                two.setName("3期已售罄！");
                if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//六期可投
                    three = sixTermIPlans.get(0);
                } else if (!twelveTermIPlans.isEmpty() && twelveTermIPlans.size() >= 1) {
                    three = twelveTermIPlans.get(0);
                } else if (!sixTermHeraldIPlans.isEmpty() && sixTermHeraldIPlans.size() >= 1) {
                    three = sixTermHeraldIPlans.get(0);
                } else if (!twelveTermHeraldIPlans.isEmpty() && twelveTermHeraldIPlans.size() >= 1) {
                    three = twelveTermHeraldIPlans.get(0);
                } else {
                    three = this.get6SellOut();
                    if (null == three) {
                        three = new IPlan();
                    }
                    three.setName("6/12期已售罄！");
                }
            }
        }



        /*if (!oneTermIPlans.isEmpty() && oneTermIPlans.size() >= 1) {//一期可投
            one = oneTermIPlans.get(0);
            if (!threeTermIPlans.isEmpty() && threeTermIPlans.size() >= 1) {//三期可投
                two = threeTermIPlans.get(0);
            } else if (!threeTermHeraldIPlans.isEmpty() && threeTermHeraldIPlans.size() >= 1) {//三期预告
                two = threeTermHeraldIPlans.get(0);
            } else {//三期已售罄
                two = null;
            }
        } else if (!oneTermHeraldIPlans.isEmpty() && oneTermHeraldIPlans.size() >= 1) {//一期预告
            one = oneTermHeraldIPlans.get(0);
        } else {
            if (!threeTermIPlans.isEmpty() && threeTermIPlans.size() >= 1) {//三期有可投
                one = threeTermIPlans.get(0);
                if (threeTermIPlans.size() >= 2) {
                    two = threeTermIPlans.get(1);
                } else
            } else if (!sixTermIPlans.isEmpty() && sixTermIPlans.size() >= 1) {//三期没可投，六期有可投

            }
            one = null;
        }*/
        featuredIPlans.addAll(Arrays.asList(one, two, three));
        return featuredIPlans;
    }

    /**
     * 获取预告定期计划
     */
    public List<IPlan> getHeraldIPlans() {
        List<IPlan> heraldIPlans = new ArrayList<>(3);
        List<IPlan> oneTermIPlans = this.get1TermHerald();
        List<IPlan> threeTermIPlans = this.get3TermHerald();
        List<IPlan> sixTermIPlans = this.get6TermHerald();
        List<IPlan> twelveTermIPlans = this.get12TermHerald();
        IPlan one = null;
        IPlan two = null;
        IPlan three = null;

        heraldIPlans.addAll(Arrays.asList(one, two, three));
        return heraldIPlans;
    }

    @ProductSlave
    @PutRedis(key = GlobalConfig.IPLAN_TALENT,fieldKey = "#iPlanId",expire = 600)
    public List<IPlanTalentDto> getInvestorAcct(Integer iPlanId) {
        List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByIPlanIdAndTransStatusAndTransTypeIn(iPlanId, "0,6", "0,1");
        List<IPlanTalentDto> iPlanTalentDtos = new ArrayList<>();
        for (IPlanTransLog iPlanTransLog : iPlanTransLogs) {
            IPlanTalentDto iPlanTalentDto = new IPlanTalentDto();
            User investor = userService.findByUsername(iPlanTransLog.getUserId());
            String realName = investor.getRealname();
            String idCard = investor.getIdCard();
            iPlanTalentDto.setUserName(realName);
            iPlanTalentDto.setGender(this.getGender(idCard));
            iPlanTalentDto.setAmount(iPlanTransLog.getTransAmt());
            iPlanTalentDto.setInvestTime(iPlanTransLog.getTransTime());
            iPlanTalentDto.setDevice(iPlanTransLog.getTransDevice());
            if ("auto_invest".equals(iPlanTransLog.getTransDevice())) {
                iPlanTalentDto.setInvestWay("智能抢标");
            } else {
                iPlanTalentDto.setInvestWay("手动投标");
            }
            iPlanTalentDtos.add(iPlanTalentDto);
        }
        return iPlanTalentDtos.stream().sorted(Comparator.comparing(IPlanTalentDto::getInvestTime).reversed()).collect(Collectors.toList());//按时间倒序
    }

    private String getGender(String idCard) {
        if (StringUtils.isNotBlank(idCard)) {
            if (idCard.length() == 18 || idCard.length() == 15) {
                // 0是男
                String sex = idCard.substring(idCard.length() - 2,
                        idCard.length() - 1);
                int se = Integer.parseInt(sex);
                if (se % 2 == 0) {
                    return "女";
                } else {
                    // 男
                    return "男";
                }
            }
        }
        return null;
    }

    private IPlan get1SellOut() {
        List<IPlan> iPlans = this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING_FINISH, IPlan.STATUS_EARNING, IPlan.STATUS_END)), 1);
        if (iPlans == null || iPlans.isEmpty()) {
            return null;
        }
        return iPlans.get(0);
    }

    private IPlan get3SellOut() {
        List<IPlan> iPlans = this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING_FINISH, IPlan.STATUS_EARNING, IPlan.STATUS_END)), 3);
        if (iPlans == null || iPlans.isEmpty()) {
            return null;
        }
        return iPlans.get(0);
    }

    private IPlan get6SellOut() {
        List<IPlan> iPlans = this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING_FINISH, IPlan.STATUS_EARNING, IPlan.STATUS_END)), 6);
        if (iPlans == null || iPlans.isEmpty()) {
            return null;
        }
        return iPlans.get(0);
    }

    private IPlan get12SellOut() {
        List<IPlan> iPlans = this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING_FINISH, IPlan.STATUS_EARNING, IPlan.STATUS_END)), 12);
        if (iPlans == null || iPlans.isEmpty()) {
            return null;
        }
        return iPlans.get(0);
    }

    public List<IPlan> get1TermInvestable() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING)), 1);
    }

    public List<IPlan> get3TermInvestable() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING)), 3);
    }

    public List<IPlan> get6TermInvestable() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING)), 6);
    }

    public List<IPlan> get12TermInvestable() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_RAISING)), 12);
    }

    public List<IPlan> get1TermHerald() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_ANNOUNCING)), 1);
    }

    public List<IPlan> get3TermHerald() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_ANNOUNCING)), 3);
    }

    public List<IPlan> get6TermHerald() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_ANNOUNCING)), 6);
    }

    public List<IPlan> get12TermHerald() {
        return this.getByStatuses(new HashSet<>(Arrays.asList(IPlan.STATUS_ANNOUNCING)), 12);
    }

    public List<IPlan> getByStatuses(Set<Integer> statuses, Integer term) {
        if (statuses == null || statuses.isEmpty() || term == null) {
            throw new IllegalArgumentException("statuses and term is can not null when find iPlan by statuses and term");
        }
        return iPlanDao.findByStatusIn(statuses, term);
    }

    /*private IPlan findTop1ByStatus(Integer status, Integer term) {
        if (status == null || term == null) {
            throw new IllegalArgumentException("statuses and term is can not null when find iPlan by statuses and term");
        }
        return iPlanDao.findTop1ByStatus(status, term);
    }*/

    public IPlan insert(IPlan iPlan) {
        if (iPlan == null) {
            throw new IllegalArgumentException("iPlan is can not null");
        }
        iPlan.setCreateTime(DateUtil.getCurrentDateTime19());
        iPlanDao.insert(iPlan);
        return iPlan;
    }

    public IPlan update(IPlan iPlan) {
        if (iPlan == null) {
            throw new IllegalArgumentException("iPlan is can not null");
        }
        iPlan.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanDao.update(iPlan);
        return iPlan;
    }

    /**
     * 查询出所有可见的理财计划
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     */
    public List<IPlan> findAllVisiblePlan(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return iPlanDao.findAllVisiblePlan();
    }

    //查询用户已投金额
    public Double findInvestedAmtByUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId不能为空");
        }
        Double investTotalMoney = investService.getInvestTotalMoney(userId);
        Long iPlanTotalMoney = iPlanAccountService.getIPlanTotalMoney(userId);
        //新增散标的投资额度
        Long subjectTotalMoney = subjectAccountService.getSubjectTotalMoney(userId);
        return iPlanTotalMoney + subjectTotalMoney + investTotalMoney * 100;

    }

    /**
     * iPlan是否可投
     */
    public Boolean iPlanInvestable(Integer id, int autoInvest) {
        if (id == null) {
            throw new IllegalArgumentException("iPlanCode is can not null");
        }
        IPlan iPlan = null;
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN, 0, -1);
        if (DOUBLE_11_IPLAN.contains(String.valueOf(id))) {
            iPlan = redisClient.get(GlobalConfig.IPLAN_REDIS + id, IPlan.class);
            if (iPlan == null) {
                iPlan = iPlanDao.findById(id);
                String iplan = JSON.toJSONString(iPlan);
                try {
                    redisClient.set(GlobalConfig.IPLAN_REDIS + id, iplan);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.info("理财计划详情：{}", iPlan.toString());
            if (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())) {
                if (DateUtil.compareDateTime(DateUtil.getCurrentDateTime19(), iPlan.getRaiseOpenTime())) {
                    iPlan.setStatus(IPlan.STATUS_RAISING);
                    String iplan = JSON.toJSONString(iPlan);
                    try {
                        redisClient.set(GlobalConfig.IPLAN_REDIS + id, iplan);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } else {
            iPlan = iPlanDao.findById(id);
        }

        if (iPlan != null) {
            if (!iPlan.getPushStatus().equals(IPlan.PUSH_STATUS_Y)) {
                return false;
            }
            if (autoInvest == 0) {
                if (iPlan.getStatus().equals(IPlan.STATUS_RAISING)) {
                    return true;
                }
            }
            if (autoInvest == 1) {
                if (iPlan.getStatus().equals(IPlan.STATUS_ANNOUNCING) || iPlan.getStatus().equals(IPlan.STATUS_RAISING)) {
                    return true;
                }
            }
        }
        return false;
    }


    public IPlan getIPlanByIdForUpdate(int iPlanId) {
        if (iPlanId == 0) {
            throw new IllegalArgumentException("iPlanId can not be null");
        }
        return iPlanDao.findByIdForUpdate(iPlanId);
    }

    @ProductSlave
    public IPlan getIPlanById(int iPlanId) {
        if (iPlanId == 0) {
            throw new IllegalArgumentException("iPlanId can not be null");
        }
        List<String> DOUBLE_11_IPLAN = redisClient.getVByList(GlobalConfig.DOUBLE_11_IPLAN, 0, -1);
        if (DOUBLE_11_IPLAN.contains(String.valueOf(iPlanId))) {
            IPlan iPlan = redisClient.get(GlobalConfig.IPLAN_REDIS + iPlanId, IPlan.class);
            if (iPlan == null) {
                iPlan = iPlanDao.findById(iPlanId);
                String iplan = JSON.toJSONString(iPlan);
                try {
                    redisClient.set(GlobalConfig.IPLAN_REDIS + iPlanId, iplan);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            logger.info("理财计划详情：{}", iPlan.toString());
            if (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())) {
                if (DateUtil.compareDateTime(DateUtil.getCurrentDateTime19(), iPlan.getRaiseOpenTime())) {
                    iPlan.setStatus(IPlan.STATUS_RAISING);
                    String iPlanJson = JSON.toJSONString(iPlan);
                    try {
                        redisClient.set(GlobalConfig.IPLAN_REDIS + iPlanId, iPlanJson);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return iPlan;
        }
        return iPlanDao.findById(iPlanId);
    }

    public IPlan pushIPlan(IPlan iPlan) {
        IPlan iPlan1;
        if (iPlan == null) {
            logger.warn("定期理财计划：" + iPlan.getCode() + "不存在");
            throw new ProcessException(Error.NDR_0428);
        } else {
            iPlan1 = iPlanDao.findByCode(iPlan.getCode());
            if (iPlan1.getId() == null) {
                logger.warn("定期理财计划：" + iPlan1.getId() + "不存在");
                throw new ProcessException(Error.NDR_0428);
            }
        }

        BaseResponse response = this.pushToXM(iPlan);
        logger.info(response.toString());
        if (!BaseResponse.STATUS_SUCCEED.equals(response.getStatus())) {//推标失败
            //如果智能投资包已经存在改变状态
            if ("1".equals(response.getCode()) && response.getDescription().toString().contains("智能投资包已存在")) {
                iPlan.setPushStatus(IPlan.PUSH_STATUS_Y);
                //推送状态和开放状态保持一致
                if (iPlan1.getStatus().equals(IPlan.STATUS_NOT_OPEN)) {
                    iPlan.setStatus(IPlan.STATUS_RAISING);
                    iPlan.setRaiseOpenTime(DateUtil.getCurrentDateTime19());
                }
            } else {
                iPlan.setPushStatus(IPlan.PUSH_STATUS_N);
                logger.error("failed to  push iplan{}", iPlan.getCode());
                throw new ProcessException(Error.NDR_0514.getCode(), Error.NDR_0514.getMessage() + response.getDescription());
            }
        } else {
            iPlan.setPushStatus(IPlan.PUSH_STATUS_Y);
            //推送状态和开放状态保持一致
            if (iPlan1.getStatus().equals(IPlan.STATUS_NOT_OPEN)) {
                iPlan.setStatus(IPlan.STATUS_RAISING);
                iPlan.setRaiseOpenTime(DateUtil.getCurrentDateTime19());
            }
        }
        iPlan.setId(iPlan1.getId());
        iPlan.setUpdateTime(DateUtil.getCurrentDateTime19());
        iPlanDao.update(iPlan);
        return iPlan;
    }

    //查询理财计划列表-包括新手（app和pc）
    @ProductSlave
    public List<IPlan> findPlanNewBie(int iplanType) {
        iplanType = iplanType > 0 ? iplanType : 0;
        List<IPlan> list = new ArrayList<>();

        List<IPlan> investableNewbieIplans = iPlanDao.findIPlanNewbieInvestable(iplanType);

        List<IPlan> investableNotNewbieIplans = iPlanDao.findIPlanNotNewbieInvestable(iplanType);

        List<IPlan> finishedIplans =  iPlanDao.findIplanFinished(iplanType);

        if (investableNewbieIplans != null && investableNewbieIplans.size() > 0) {
            investableNewbieIplans = this.yjtSortByConfig(investableNewbieIplans);
            list.addAll(investableNewbieIplans);
        }
        if (investableNotNewbieIplans != null && investableNotNewbieIplans.size() > 0) {
            investableNotNewbieIplans = this.yjtSortByConfig(investableNotNewbieIplans);
            list.addAll(investableNotNewbieIplans);
        }
        if (finishedIplans != null && finishedIplans.size() > 0) {
            list.addAll(finishedIplans);
        }
        return list;
        //iPlanDao.findPlanNewBie()
    }

  //查询理财计划列表-不包括新手（app和pc）
    @ProductSlave
    public List<IPlan> findPlanNoAnyNewBies(int iplanType) {
        iplanType = iplanType > 0 ? iplanType : 0;
        List<IPlan> list = new ArrayList<>();

        List<IPlan> iplanFinishedNoAnyNewBies = iPlanDao.findPlanNoNewBieFinished(iplanType);

        List<IPlan> investableNotNewbieIplans = iPlanDao.findIPlanNotNewbieInvestable(iplanType);


        if (investableNotNewbieIplans != null && investableNotNewbieIplans.size() > 0) {
            investableNotNewbieIplans = this.yjtSortByConfig(investableNotNewbieIplans);
        	list.addAll(investableNotNewbieIplans);
        }
        if (iplanFinishedNoAnyNewBies != null && iplanFinishedNoAnyNewBies.size() > 0) {
            list.addAll(iplanFinishedNoAnyNewBies);
        }
        return list;
        //iPlanDao.findPlanNewBie()
    }
    
    //查询理财计划列表-不包括新手（app和pc）
    @ProductSlave
    public List<IPlan> findPlanNoNewBie(int iplanType) {
        iplanType = iplanType > 0 ? iplanType : 0;
        List<IPlan> list = new ArrayList<>();
        /*List<IPlan> investableNewbieIplans = iPlanDao.findPlanNoNewBieRaising(iplanType);
        List<IPlan> investableNotNewbieIplans = iPlanDao.findPlanNoNewBieAnnouncing(iplanType);*/
        List<IPlan> investableNotNewbieIplans = iPlanDao.findIPlanNotNewbieInvestable(iplanType);
        List<IPlan> finishedIplans = iPlanDao.findPlanNoNewBieFinished(iplanType);

        /*if (investableNewbieIplans != null && investableNewbieIplans.size() > 0) {
            list.addAll(investableNewbieIplans);
        }*/
        if (investableNotNewbieIplans != null && investableNotNewbieIplans.size() > 0) {
            investableNotNewbieIplans = this.yjtSortByConfig(investableNotNewbieIplans);
            list.addAll(investableNotNewbieIplans);
        }
        if (finishedIplans != null && finishedIplans.size() > 0) {
            list.addAll(finishedIplans);
        }
        return list;
//        return iPlanDao.findPlanNoNewBie();
    }

    //查询省心投列表列表-不包括新手（app和pc）
    @ProductSlave
    public List<IPlan> findYjtNoNewBie(int yjtType) {
        List<IPlan> list = new ArrayList<>();
        //可投的普通省心投
        List<IPlan> investableNotNewbieIplans = iPlanDao.findIPlanNotNewbieInvestable(IPlan.IPLAN_TYPE_YJT);
        //已完成省心投
        List<IPlan> finishedIplans = new ArrayList<>();
        if (yjtType == IPlan.YJT_ORIGINAL) {
            if (CollectionUtils.isNotEmpty(investableNotNewbieIplans)) {
                investableNotNewbieIplans = investableNotNewbieIplans.stream().filter(iPlan -> !iPlanAccountService.isNewIplan(iPlan)).collect(Collectors.toList());
            }
            finishedIplans = iPlanDao.findOriginalYjtNoNewBieFinished();
        } else if (yjtType == IPlan.YJT_NEW) {
            if (CollectionUtils.isNotEmpty(investableNotNewbieIplans)) {
                investableNotNewbieIplans = investableNotNewbieIplans.stream().filter(iPlan -> iPlanAccountService.isNewIplan(iPlan)).collect(Collectors.toList());
            }
            finishedIplans = iPlanDao.findNewYjtNoNewBieFinished();
        }
        if (CollectionUtils.isNotEmpty(investableNotNewbieIplans)) {
            investableNotNewbieIplans = this.yjtSortByConfig(investableNotNewbieIplans);
            list.addAll(investableNotNewbieIplans);
        }
        if (CollectionUtils.isNotEmpty(finishedIplans)) {
            list.addAll(finishedIplans);
        }
        return list;
    }

    //查询理财计划列表-包括新手（app和pc）
    @ProductSlave
    public List<IPlan> findYjtNewBie(int yjtType) {
        List<IPlan> list = new ArrayList<>();
        //可投新手省心投
        List<IPlan> investableNewbieIplans = iPlanDao.findIPlanNewbieInvestable(IPlan.IPLAN_TYPE_YJT);
        //可投不同省心投
        List<IPlan> investableNotNewbieIplans = iPlanDao.findIPlanNotNewbieInvestable(IPlan.IPLAN_TYPE_YJT);
        //已完成省心投
        List<IPlan> finishedIplans = new ArrayList<>();
        //普通省心投
        if (yjtType == IPlan.YJT_ORIGINAL) {
            if (CollectionUtils.isNotEmpty(investableNewbieIplans)) {
                investableNewbieIplans = investableNewbieIplans.stream().filter(iPlan -> !iPlanAccountService.isNewIplan(iPlan)).collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(investableNotNewbieIplans)) {
                investableNotNewbieIplans = investableNotNewbieIplans.stream().filter(iPlan -> !iPlanAccountService.isNewIplan(iPlan)).collect(Collectors.toList());
            }
            finishedIplans = iPlanDao.findOriginalYjtFinished();
        } else
        //新省心投
        if (yjtType == IPlan.YJT_NEW) {
            if (CollectionUtils.isNotEmpty(investableNewbieIplans)) {
                investableNewbieIplans = investableNewbieIplans.stream().filter(iPlan -> iPlanAccountService.isNewIplan(iPlan)).collect(Collectors.toList());
            }
            if (CollectionUtils.isNotEmpty(investableNotNewbieIplans)) {
                investableNotNewbieIplans = investableNotNewbieIplans.stream().filter(iPlan -> iPlanAccountService.isNewIplan(iPlan)).collect(Collectors.toList());
            }
            finishedIplans = iPlanDao.findNewYjtFinished();
        }

        if (CollectionUtils.isNotEmpty(investableNewbieIplans)) {
            investableNewbieIplans = this.yjtSortByConfig(investableNewbieIplans);
            list.addAll(investableNewbieIplans);
        }
        if (CollectionUtils.isNotEmpty(investableNotNewbieIplans)) {
            investableNotNewbieIplans = this.yjtSortByConfig(investableNotNewbieIplans);
            list.addAll(investableNotNewbieIplans);
        }
        if (CollectionUtils.isNotEmpty(finishedIplans)) {
            list.addAll(finishedIplans);
        }
        return list;
    }

    /**
     * 一键投根据配置规则排序
     * @param iPlanList
     * @return
     */
    private List<IPlan> yjtSortByConfig(List<IPlan> iPlanList) {
        if (CollectionUtils.isNotEmpty(iPlanList)) {
            Config yjtSort = configService.getConfigById(Config.YJT_SORT);
            if (yjtSort != null && StringUtils.isNotBlank(yjtSort.getValue())) {
                switch (yjtSort.getValue()) {
                    //可用额度排序
                    case Config.YJT_SORT_AVAILABLEQUOTA_ASC:
                        //sql查询结果满足额度排序
                        break;
                    //发标时间倒叙排序
                    case Config.YJT_SORT_PUBLISHTIME_DESC:
                        return iPlanList.stream().sorted(Comparator.comparing(IPlan::getId)).collect(Collectors.toList());
                    //新省心投排序在前排序，额度
                    case Config.YJT_SORT_NEWYJT_FIRST:
                        Collections.sort(iPlanList, (IPlan o1, IPlan o2) -> {
                            int term1 = iPlanAccountService.isNewIplan(o1) ? 1 : 0;
                            int term2 = iPlanAccountService.isNewIplan(o2)? 1 : 0;
                            return term2 - term1;
                        });
                        return iPlanList;
                    //未配置，根据新省心投锁定期与普通省心投期限排序，默认
                    default:
                        Collections.sort(iPlanList, (IPlan o1, IPlan o2) -> {
                            int term1 = iPlanAccountService.isNewIplan(o1) ? o1.getExitLockDays() : o1.getTerm()*30;
                            int term2 = iPlanAccountService.isNewIplan(o2)? o2.getExitLockDays() : o2.getTerm()*30;
                            return term1 - term2;
                        });
                        break;
                }
            } else {
                //根据新省心投锁定期与普通省心投期限排序，默认
                Collections.sort(iPlanList, (IPlan o1, IPlan o2) -> {
                    int term1 = iPlanAccountService.isNewIplan(o1) ? o1.getExitLockDays() : o1.getTerm()*30;
                    int term2 = iPlanAccountService.isNewIplan(o2)? o2.getExitLockDays() : o2.getTerm()*30;
                    return term1 - term2;
                });
            }
            iPlanList = iPlanList.stream().sorted(Comparator.comparing(IPlan::getRank)).collect(Collectors.toList());
        }
        return iPlanList;
    }

    //查询理财计划列表-wap端
    @ProductSlave
    public List<IPlan> findPlanNewBieWap(int iplanType) {
        List<IPlan> list = new ArrayList<>();
        List<IPlan> investableNewbieIplans = iPlanDao.findIPlanNewbieInvestable(iplanType);
        List<IPlan> investableNotNewbieIplans = iPlanDao.findIPlanNotNewbieInvestable(iplanType);

        if (investableNewbieIplans != null && investableNewbieIplans.size() > 0) {
            list.addAll(investableNewbieIplans);
        }
        if (investableNotNewbieIplans != null && investableNotNewbieIplans.size() > 0) {
            list.addAll(investableNotNewbieIplans);
        }
//        if (finishedIplans != null && finishedIplans.size() > 0) {
//            list.addAll(finishedIplans);
//        }
        return list;
//        return iPlanDao.findPlanNewBieWap();
    }

    @ProductSlave
    public List<IPlan> findIplanFinishedForWap(int iplanType) {
        List<IPlan> finishedIplans = iPlanDao.findIplanFinishedForWap(iplanType);
        return  finishedIplans;
    }

    @ProductSlave
    public IPlan getByCode(String code) {
        if (code == null || "".equals(code)) {
            throw new IllegalArgumentException("code can not be null");
        }
        String id = "";
        try {
            id = redisClient.get(GlobalConfig.IPLANID_TO_CODE + code);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (StringUtils.isNotBlank(id)) {
            return iPlanDao.findById(Integer.valueOf(id));
        }
        return iPlanDao.findByCode(code);
    }

    //删除未开放未推送存管通的(作废没投资的)计划
    public IPlan delete(String id) {
        if (id == null || "".equals(id)) {
            throw new IllegalArgumentException("id can not be null");
        }
        //查询如果存在并且还没推送存管通的就可以删除
        IPlan iPlan = iPlanDao.findById(Integer.parseInt(id));
        if (iPlan != null && iPlan.getQuota().equals(iPlan.getAvailableQuota())) {
            iPlanDao.delete(Integer.parseInt(id));
        } else {
            throw new IllegalArgumentException("id can not be find");
        }
        return iPlan;
    }

    public Integer findActualQuotaById(Integer id) {
        return iPlanDao.findActualQuotaById(id);
    }

    /**
     * 查询天天赚转入月月盈项目
     *
     * @return
     */
    public List<IPlan> getTtzToIplanList() {
        return iPlanDao.getTtzToIplanList();
    }

    /**
     * 强制满标服务
     *
     * @param iPlanId
     * @return
     */
    public IPlan mandatoryIPlan(Integer iPlanId) {
        if (iPlanId == null) {
            throw new IllegalArgumentException("iPlanId is can not null");
        }
        IPlan iPlan = iPlanDao.findById(iPlanId);
        if (iPlan != null) {
            // 判断改计划下的投资记录是不是有待处理和待待等待的记录如果有就不能满标
            //查询状态只有0处理中(购买成功)，3超时，4待确认（只有充值并投资有这个状态，在iplan中查询并流标）
            List<IPlanTransLog> iPlanTransLogs = iPlanTransLogService.getByIPlanIdAndTransStatusAndTransTypeIns(iPlanId, "0,6", "0,3");
            List<IPlanTransLog> iPlanTransLogsConfirm = iPlanTransLogService.getByIPlanIdAndTransStatusAndTransTypeIns(iPlanId, "0,6", "4");
            if (iPlanTransLogs.size() == 0) {
                for (IPlanTransLog iPlanTransLog : iPlanTransLogsConfirm) {
                    //有充值投资待确认的记录就流标
                    iPlanAccountService.rechargeAndInvestCancel(iPlanTransLog.getId());
                }
                IPlan iPlanNew = new IPlan();
                // Integer quota = iPlan.getQuota() - iPlan.getAvailableQuota();
                iPlanNew.setId(iPlan.getId());
                iPlanNew.setAvailableQuota(0);
                iPlanNew.setStatus(IPlan.STATUS_RAISING_FINISH);// 由募集中改为募集完成
                iPlanNew.setUpdateTime(DateUtil.getCurrentDateTime19());
                iPlanNew.setRaiseCloseTime(DateUtil.getCurrentDateTime19());// 结束募集时间
                iPlan = this.update(iPlanNew);
            } else {
                logger.warn("定期理财计划：" + iPlan.getCode() + "有正在处理中的投资记录，请稍等");
//                throw new ProcessException(Error.NDR_0464);
                return null;
            }
        }
        return iPlan;
    }

    /**
     * 根据用户来源过滤iplan列表，规则如下：
     * 1、新手标：
     *  1.1、可投新手标（募集中，预告）：（新手标中渠道与非渠道互斥）
     *      1.1.1、渠道新手标：特定渠道用户可见，其他渠道与非渠道用户不可见；
     *      1.1.2、普通新手标：非渠道用户可见，渠道用户不可见；
     *  1.2、已完成新手标：所有用户可见，渠道用户与非渠道用户都可见；
     * 2、普通标：
     *  2.1、可投普通标（募集中，预告）：（普通标中渠道用户可见包含普通标）
     *      2.1.1、渠道标：特定渠道用户可见；其他渠道或非渠道普通用户不可见；
     *      2.1.2、普通标：所有用户可见，渠道用户与非渠道用户都可见；
     *  2.2、已完成普通标：所有用户可见，渠道用户与非渠道用户都可见；
     * @param allVisiblePlan
     * @param userId
     * @return
     */
    public List<IPlan> filterIplanByUserSource(List<IPlan> allVisiblePlan, String userId) {
        //查询用户注册来源
        String userSource = userService.getUserRegisterSource(userId);
        boolean fanLiWangFlag = false;

        if (org.apache.commons.lang3.StringUtils.isNotBlank(userSource)) {
            if (userSource.contains(User.FAN_LI_WANG)) {
                fanLiWangFlag = true;
            }
            //渠道用户过滤非渠道可投新手标，过滤非此渠道普通标
            if (userSource.contains(User.FENG_CHE_LI_CAI)) {

                allVisiblePlan = allVisiblePlan.stream()
                        .filter(iPlan ->
                                !(IPlan.NEWBIE_ONLY_Y.equals(iPlan.getNewbieOnly())
                                        && (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())
                                        || IPlan.STATUS_RAISING.equals(iPlan.getStatus()))
                                        && !IPlan.CHANNEL_NAME_FENGCHELICAI.equals(iPlan.getChannelName())
                                ))
                        .filter(iPlan ->
                                !(IPlan.NEWBIE_ONLY_N.equals(iPlan.getNewbieOnly())
                                        && (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())
                                        || IPlan.STATUS_RAISING.equals(iPlan.getStatus()))
                                        && iPlan.getChannelName() != null && iPlan.getChannelName() > 0
                                        && !IPlan.CHANNEL_NAME_FENGCHELICAI.equals(iPlan.getChannelName())
                                ))
                        .collect(Collectors.toList());
            } else {
                //其他渠道的用户
                allVisiblePlan = allVisiblePlan.stream()
                        .filter(iPlan ->
                                !(IPlan.NEWBIE_ONLY_Y.equals(iPlan.getNewbieOnly())
                                        && (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())
                                        || IPlan.STATUS_RAISING.equals(iPlan.getStatus()))
                                        && iPlan.getChannelName() != null && iPlan.getChannelName() > 0
                                ))
                        .filter(iPlan ->
                                !(IPlan.NEWBIE_ONLY_N.equals(iPlan.getNewbieOnly())
                                        && (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())
                                        || IPlan.STATUS_RAISING.equals(iPlan.getStatus()))
                                        && iPlan.getChannelName() != null && iPlan.getChannelName() > 0
                                ))
                        .collect(Collectors.toList());
            }

        } else {
            //未登录或无渠道用户
            allVisiblePlan = allVisiblePlan.stream()
                    .filter(iPlan ->
                            !(IPlan.NEWBIE_ONLY_Y.equals(iPlan.getNewbieOnly())
                                    && (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())
                                    || IPlan.STATUS_RAISING.equals(iPlan.getStatus()))
                                    && iPlan.getChannelName() != null && iPlan.getChannelName() > 0
                            ))
                    .filter(iPlan ->
                            !(IPlan.NEWBIE_ONLY_N.equals(iPlan.getNewbieOnly())
                                    && (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus())
                                    || IPlan.STATUS_RAISING.equals(iPlan.getStatus()))
                                    && iPlan.getChannelName() != null && iPlan.getChannelName() > 0
                            ))
                    .collect(Collectors.toList());
        }

        //返利网过滤新手标
        if (fanLiWangFlag) {
            allVisiblePlan = allVisiblePlan.stream().filter(iPlan ->
                    !(IPlan.NEWBIE_ONLY_Y.equals(iPlan.getNewbieOnly())
                            && (IPlan.STATUS_ANNOUNCING.equals(iPlan.getStatus()) || IPlan.STATUS_RAISING.equals(iPlan.getStatus()))
                    )
            ).collect(Collectors.toList());
        }
        return allVisiblePlan;
    }
}
