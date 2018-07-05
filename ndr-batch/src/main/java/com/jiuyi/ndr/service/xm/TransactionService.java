package com.jiuyi.ndr.service.xm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jiuyi.ndr.constant.CheckFileConsts;
import com.jiuyi.ndr.dao.config.ConfigDao;
import com.jiuyi.ndr.dao.xm.TransactionDetailDao;
import com.jiuyi.ndr.domain.config.Config;
import com.jiuyi.ndr.domain.xm.TransLog;
import com.jiuyi.ndr.domain.xm.TransactionDetail;
import com.jiuyi.ndr.error.Error;
import com.jiuyi.ndr.exception.ProcessException;
import com.jiuyi.ndr.service.config.ConfigService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.util.DateUtil;
import com.jiuyi.ndr.xm.constant.BizType;
import com.jiuyi.ndr.xm.constant.RequestInterfaceXMEnum;
import com.jiuyi.ndr.xm.constant.TransactionType;
import com.jiuyi.ndr.xm.http.BaseResponse;
import com.jiuyi.ndr.xm.http.request.*;
import com.jiuyi.ndr.xm.http.response.*;
import com.jiuyi.ndr.xm.http.response.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author ke
 * @since 2017/4/18 10:56
 */
@Service
public class TransactionService {

    private static Logger logger = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private DirectConnectHttpService directConnectHttpService;
    @Autowired
    private TransLogService transLogService;
    @Autowired
    private TransactionDetailDao transactionDetailDao;
    @Autowired
    private ConfigDao configDao;

    @Autowired
    private ConfigService configService;

    @Value("${xm.response.time}")
    private String TIME_SWITCH;

    /**
     * 创建标的
     */
    public BaseResponse establishProject(RequestEstablishProject requestEstablishProject) {

        String requestNo = requestEstablishProject.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        requestEstablishProject.setRequestNo(requestNo);
        String timestamp = getTimestamp();
        requestEstablishProject.setTimestamp(timestamp);
        return httpPost(requestNo, RequestInterfaceXMEnum.ESTABLISH_PROJECT, requestEstablishProject.getTransCode(),
                "", JSONObject.toJSONString(requestEstablishProject));
    }

    /**
     * 创建批量投标计划
     */
    public BaseResponse establishIntelligentProject(RequestEstablishIntelligentProject intelligentProject) {

        String requestNo = intelligentProject.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        intelligentProject.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        intelligentProject.setTimestamp(timestamp);

        return httpPost(requestNo, RequestInterfaceXMEnum.ESTABLISH_INTELLIGENT_PROJECT, intelligentProject.getTransCode(),
                "", JSONObject.toJSONString(intelligentProject));
    }

    /**
     * 创建批量投标请求
     */
    public BaseResponse purchaseIntelligentProject(RequestPurchaseIntelligentProject purchaseIntelligentProject) {

        String requestNo = purchaseIntelligentProject.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        purchaseIntelligentProject.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        purchaseIntelligentProject.setTimestamp(timestamp);

        return httpPost(requestNo, RequestInterfaceXMEnum.PURCHASE_INTELLIGENT_PROJECT,
                purchaseIntelligentProject.getTransCode(), "", JSONObject.toJSONString(purchaseIntelligentProject));
    }

    /**
     * 批量投标请求解冻
     */
    public BaseResponse intelligentProjectUnfreeze(RequestIntelligentProjectUnfreeze request) {

        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        String intelRequestNo = request.getIntelRequestNo();
        request.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);

        //交易详情新增 只包含：债权转让/放款/还款/代偿/营销红包/派息/代偿还款/分润
        TransactionDetail transactionDetail = new TransactionDetail();
        transactionDetail.setBizType("INTELLIGENG_PROJECT_UNFREEZE");
        transactionDetail.setBusinessType(CheckFileConsts.BIZ_TYPE_41);
        transactionDetail.setAmount(request.getAmount());
        transactionDetail.setSourcePlatformUserNo("");
        transactionDetail.setTargetPlatformUserNo("");
        transactionDetail.setSubjectId("");
        transactionDetail.setRequestNo(intelRequestNo);
        transactionDetail.setCreditUnit(null);//债权份额（债权转让且需校验债权关系的必传）
        transactionDetail.setStatus(TransactionDetail.STATUS_PENDING);
        transactionDetail.setRequestTime(DateUtil.getCurrentDateTime14());
        transactionDetail.setType(TransactionDetail.FILE_OTHERS);//批量投标请求解冻
        transactionDetailDao.insert(transactionDetail);

        BaseResponse baseResponse = httpPost(requestNo, RequestInterfaceXMEnum.INTELLIGENT_PROJECT_UNFREEZE, request.getTransCode(),
                "", JSONObject.toJSONString(request));

        //交易详情更新
        TransactionDetail detail = transactionDetailDao.getByRequestNo(intelRequestNo).get(0);
        //每一笔交易状态都更新
        Integer status;
        if ("0".equals(baseResponse.getCode())) {
            status = TransactionDetail.STATUS_SUCCEED;
        }else if("1".equals(baseResponse.getCode())){
            status = TransactionDetail.STATUS_PENDING;
        }else {
            status = TransactionDetail.STATUS_FAILED;
        }
        detail.setStatus(status);
        detail.setUpdateTime(DateUtil.getCurrentDateTime14());
        transactionDetailDao.update(detail);

        return baseResponse;

    }

    /**
     * 单笔债权出让
     */
    public BaseResponse debentureSale(RequestDebentureSale reqDebentureSale) {

        String requestNo = reqDebentureSale.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        reqDebentureSale.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        reqDebentureSale.setTimestamp(timestamp);

        return httpPost(requestNo, RequestInterfaceXMEnum.DEBENTURE_SALE, reqDebentureSale.getTransCode(),
                "", JSONObject.toJSONString(reqDebentureSale));
    }

    /**
     * 批量债权出让
     */
    public BaseResponse intelligentProjectDebentureSale(RequestIntelligentProjectDebentureSale request) {

        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        request.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);

        return httpPost(requestNo, RequestInterfaceXMEnum.INTELLIGENT_PROJECT_DEBENTURE_SALE,
                request.getTransCode(), "", JSONObject.toJSONString(request));
    }

    /**
     * 授权预处理
     */
    public BaseResponse userAutoPreTransaction(RequestUserAutoPreTransaction requestUserAutoPreTransaction) {

        String requestNo = requestUserAutoPreTransaction.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        try {
            requestUserAutoPreTransaction.setRequestNo(requestNo);
            String timestamp = getTimestamp();

            requestUserAutoPreTransaction.setTimestamp(timestamp);

            String reqData = JSONObject.toJSONString(requestUserAutoPreTransaction);

            //================新增HTTP请求日志==================//
            TransLog transLog = this.addTransLog(requestNo, RequestInterfaceXMEnum.USER_AUTO_PRE_TRANSACTION,
                    requestUserAutoPreTransaction.getTransCode(), "", reqData);

            //=======直连接口=======//
            String jsonResponse = null;
            try {
                logger.info("授权预处理，核心请求报文是 \n [{}]", reqData);
                jsonResponse = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.USER_AUTO_PRE_TRANSACTION);
            } catch (ProcessException e) {
                if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "",
                            "厦门银行签名异常 - 授权预处理");
                    logger.warn("厦门银行签名异常 - 授权预处理");
                    return new BaseResponse("厦门银行签名异常 - 授权预处理", BaseResponse.STATUS_FAILED, requestNo);

                } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "",
                            "HTTP调用平台接口异常 - 授权预处理");
                    logger.warn("HTTP调用平台接口异常 - 授权预处理");
                    return new BaseResponse("HTTP调用平台接口异常 - 授权预处理", BaseResponse.STATUS_PENDING, requestNo);
                }
            }

            // 解析返回报文
            ResponseUserAutoPreTransaction responseUserAutoPreTransaction = JSONObject.parseObject(jsonResponse,
                    ResponseUserAutoPreTransaction.class);

            BaseResponse response = new BaseResponse();
            String code = responseUserAutoPreTransaction.getCode();
            String description = responseUserAutoPreTransaction.getDescription();
            response.setCode(code);
            response.setDescription(description);
            //若预处理失败
            if(StringUtils.isEmpty(responseUserAutoPreTransaction.getRequestNo())){
                response.setRequestNo(requestNo);
            }else{
                response.setRequestNo(responseUserAutoPreTransaction.getRequestNo());
            }

            Integer status;
            if ("0".equals(code)) {
                status = TransLog.STATUS_SUCCEED;
                response.setStatus(BaseResponse.STATUS_SUCCEED);
            } else {
                status = TransLog.STATUS_PENDING;
                response.setStatus(BaseResponse.STATUS_PENDING);
            }

            //================更新HTTP请求日志==================//
            transLogService.update(transLog.getId(), jsonResponse, status, code, description);

            return response;
        } catch (Exception e) {
            return new BaseResponse("厦门银行交易异常", BaseResponse.STATUS_PENDING, requestNo);
        }
    }

    /**
     * 平台预处理
     */
    public BaseResponse platformPreTransaction(RequestUserAutoPreTransaction requestUserAutoPreTransaction) {

        String requestNo = requestUserAutoPreTransaction.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        try {
            requestUserAutoPreTransaction.setRequestNo(requestNo);
            String timestamp = getTimestamp();

            requestUserAutoPreTransaction.setTimestamp(timestamp);

            String reqData = JSONObject.toJSONString(requestUserAutoPreTransaction);

            //================新增HTTP请求日志==================//
            TransLog transLog = this.addTransLog(requestNo, RequestInterfaceXMEnum.PLATFORM_PRE_TRANSACTION,
                    requestUserAutoPreTransaction.getTransCode(), "", reqData);

            //=======直连接口=======//
            String jsonResponse = null;
            try {
                logger.info("平台预处理，核心请求报文是 \n [{}]", reqData);
                jsonResponse = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.PLATFORM_PRE_TRANSACTION);
            } catch (ProcessException e) {
                if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "",
                            "厦门银行签名异常 - 平台预处理");
                    logger.warn("厦门银行签名异常 - 平台预处理");
                    return new BaseResponse("厦门银行签名异常 - 平台预处理", BaseResponse.STATUS_FAILED, requestNo);

                } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "",
                            "HTTP调用平台接口异常 - 平台预处理");
                    logger.warn("HTTP调用平台接口异常 - 平台预处理");
                    return new BaseResponse("HTTP调用平台接口异常 - 平台预处理", BaseResponse.STATUS_PENDING, requestNo);
                }
            }

            // 解析返回报文
            ResponseUserAutoPreTransaction responseUserAutoPreTransaction = JSONObject.parseObject(jsonResponse,
                    ResponseUserAutoPreTransaction.class);

            BaseResponse response = new BaseResponse();
            String code = responseUserAutoPreTransaction.getCode();
            String description = responseUserAutoPreTransaction.getDescription();
            response.setCode(code);
            response.setDescription(description);
            //若预处理失败
            if(StringUtils.isEmpty(responseUserAutoPreTransaction.getRequestNo())){
                response.setRequestNo(requestNo);
            }else{
                response.setRequestNo(responseUserAutoPreTransaction.getRequestNo());
            }

            Integer status;
            if ("0".equals(code)) {
                status = TransLog.STATUS_SUCCEED;
                response.setStatus(BaseResponse.STATUS_SUCCEED);
            } else {
                status = TransLog.STATUS_PENDING;
                response.setStatus(BaseResponse.STATUS_PENDING);
            }

            //================更新HTTP请求日志==================//
            transLogService.update(transLog.getId(), jsonResponse, status, code, description);

            return response;
        } catch (Exception e) {
            return new BaseResponse("厦门银行交易异常", BaseResponse.STATUS_PENDING, requestNo);
        }
    }

    /**
     * 变更标的
     */
    public BaseResponse modifyProject(RequestModifyProject modifyProject) {

        String requestNo = modifyProject.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        try {
            modifyProject.setRequestNo(requestNo);
            String timestamp = getTimestamp();

            modifyProject.setTimestamp(timestamp);

            String reqData = JSONObject.toJSONString(modifyProject);

            //================插入HTTP请求日志==================//
            TransLog transLog = this.addTransLog(requestNo, RequestInterfaceXMEnum.MODIFY_PROJECT, modifyProject.getTransCode(),
                    "", reqData);

            //=======直连接口=======//
            String jsonResponse = null;
            try {
                logger.info("变更标的，核心请求报文是 \n [{}]", reqData);
                jsonResponse = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.MODIFY_PROJECT);
            } catch (ProcessException e) {
                if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED,
                            "", "厦门银行签名异常 - 变更标的");
                    logger.warn("厦门银行签名异常 - 变更标的");
                    return new BaseResponse("厦门银行签名异常 - 变更标的", BaseResponse.STATUS_FAILED, requestNo);

                } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING,
                            "", "HTTP调用平台接口异常 - 变更标的");
                    logger.warn("HTTP调用平台接口异常 - 变更标的");
                    return new BaseResponse("HTTP调用平台接口异常 - 变更标的", BaseResponse.STATUS_PENDING, requestNo);
                }
            }

            // 解析返回报文
            ResponseModifyProject responseModifyProject = JSONObject.parseObject(jsonResponse, ResponseModifyProject.class);

            //业务层返回
            BaseResponse response = new BaseResponse();
            String code = responseModifyProject.getCode();
            String description = responseModifyProject.getDescription();
            response.setCode(code);
            response.setDescription(description);
            response.setRequestNo(requestNo);

            Integer status;
            if ("0".equals(code)) {
                status = TransLog.STATUS_SUCCEED;
                response.setStatus(BaseResponse.STATUS_SUCCEED);
            } else {
                status = TransLog.STATUS_FAILED;
                response.setStatus(BaseResponse.STATUS_FAILED);
            }

            //================更新HTTP请求日志==================//
            transLogService.update(transLog.getId(), jsonResponse, status, code, description);

            return response;
        } catch (Exception e) {
            return new BaseResponse("厦门银行交易异常", BaseResponse.STATUS_PENDING, requestNo);
        }
    }

    /**
     * 单笔交易
     */
    public BaseResponse singleTrans(RequestSingleTrans request) {

        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo(); //流水号
        }
        try {
            String timestamp = getTimestamp();

            request.setTimestamp(timestamp);

            String reqData = JSONObject.toJSONString(request);

            //================插入HTTP请求日志==================//
            TransLog transLog = this.addTransLog(requestNo, RequestInterfaceXMEnum.SYNC_TRANSACTION, request.getTransCode(),
                    request.getTradeType().toString(), reqData);

            //交易详情新增 只包含：债权转让/放款/还款/代偿/营销红包/派息/代偿还款/分润
            for (RequestSingleTrans.Detail detail : request.getDetails()) {
                TransactionDetail transactionDetail = new TransactionDetail();
                transactionDetail.setRequestNo(requestNo);
                String businessType;
                switch (detail.getBizType()) {
                    case TENDER://投标就是放款
                        businessType = CheckFileConsts.BIZ_TYPE_03;
                        break;
                    case REPAYMENT:
                        businessType = CheckFileConsts.BIZ_TYPE_04;
                        break;
                    case CREDIT_ASSIGNMENT:
                        businessType = CheckFileConsts.BIZ_TYPE_05;
                        break;
                    case MARKETING:
                        businessType = CheckFileConsts.BIZ_TYPE_07;
                        break;
                    case COMMISSION:
                        businessType = CheckFileConsts.BIZ_TYPE_11;
                        break;
                    case PROFIT:
                        businessType = CheckFileConsts.BIZ_TYPE_14;
                        break;
                    //TODO 投标批量追加是？
                    case APPEND_FREEZE:
                        businessType = CheckFileConsts.BIZ_TYPE_40;
                        break;
                    case COMPENSATORY:
                        businessType = CheckFileConsts.BIZ_TYPE_06;
                        break;
                    case COMPENSATORY_REPAYMENT:
                        businessType = CheckFileConsts.BIZ_TYPE_09;
                        break;
                    default:
                        businessType = "88";
                }
                transactionDetail.setBizType(detail.getBizType().getCode());
                transactionDetail.setBusinessType(businessType);
                transactionDetail.setAmount(detail.getAmount());
                transactionDetail.setSourcePlatformUserNo(detail.getSourcePlatformUserNo());
                transactionDetail.setTargetPlatformUserNo(detail.getTargetPlatformUserNo());
                transactionDetail.setSubjectId(request.getProjectNo());
                transactionDetail.setRequestNo(requestNo);
                transactionDetail.setCreditUnit(detail.getShare());//债权份额（债权转让且需校验债权关系的必传）
                transactionDetail.setStatus(TransactionDetail.STATUS_PENDING);
                transactionDetail.setRequestTime(DateUtil.getCurrentDateTime14());
                if (BizType.COMMISSION == detail.getBizType()) {
                    transactionDetail.setType(TransactionDetail.FILE_COMMISSION);//佣金文件
                } else {
                    transactionDetail.setType(TransactionDetail.FILE_TRANSACTION);//交易处理文件
                }
                transactionDetailDao.insert(transactionDetail);
            }

            //=======直连接口=======//
            String jsonResp = null;
            try {
                logger.info("单笔交易，核心请求报文是 \n [{}]", reqData);
                jsonResp = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.SYNC_TRANSACTION);
            } catch (ProcessException e) {
                if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED,
                            "", "厦门银行签名异常 - 单笔交易");
                    logger.warn("厦门银行签名异常 - 单笔交易");
                    return new BaseResponse("厦门银行签名异常 - 单笔交易", BaseResponse.STATUS_FAILED, requestNo);
                } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING,
                            "", "HTTP调用平台接口异常 - 单笔交易");
                    logger.warn("HTTP调用平台接口异常 - 单笔交易");
                    return new BaseResponse("HTTP调用平台接口异常 - 单笔交易", BaseResponse.STATUS_PENDING, requestNo);
                }
            } catch (Exception e) {
                return new BaseResponse("厦门银行交易异常 - 单笔交易", BaseResponse.STATUS_PENDING, requestNo);
            }

            // 解析
            ResponseSingleTrans responseSingleTrans = JSONObject.parseObject(jsonResp, ResponseSingleTrans.class);

            if (StringUtils.isEmpty(responseSingleTrans.getRequestNo())) {
                responseSingleTrans.setRequestNo(requestNo);
            }

            //交易详情更新
            List<TransactionDetail> listDetails = transactionDetailDao.getByRequestNo(requestNo);
            //每一笔交易状态都更新
            for (TransactionDetail detail : listDetails) {
                Integer status;
                if ("0".equals(responseSingleTrans.getCode())) {
                    status = TransactionDetail.STATUS_SUCCEED;
                } else if("1".equals(responseSingleTrans.getCode())) {
                    status = TransactionDetail.STATUS_PENDING;
                }else{
                    status = TransactionDetail.STATUS_FAILED;
                }
                detail.setStatus(status);
                detail.setUpdateTime(DateUtil.getCurrentDateTime14());
                transactionDetailDao.update(detail);
            }

            //业务层返回
            BaseResponse response = new BaseResponse();
            String code = responseSingleTrans.getCode();
            String description = responseSingleTrans.getDescription();
            response.setCode(code);
            response.setDescription(description);
            response.setRequestNo(responseSingleTrans.getRequestNo());

            Integer status;
            if ("0".equals(code) && "SUCCESS".equalsIgnoreCase(responseSingleTrans.getStatus())) {
                response.setStatus(BaseResponse.STATUS_SUCCEED);
                status = TransLog.STATUS_SUCCEED;
            } else if("1".equals(code)){
                response.setStatus(BaseResponse.STATUS_PENDING);
                status = TransLog.STATUS_PENDING;
            }else{
                response.setStatus(BaseResponse.STATUS_FAILED);
                status = TransLog.STATUS_FAILED;
            }

            //================更新HTTP请求日志==================//
            transLogService.update(transLog.getId(), jsonResp, status, code, description);

            return response;
        } catch (Exception e) {
            return new BaseResponse("厦门银行交易异常", BaseResponse.STATUS_PENDING, requestNo);
        }
    }

    //预处理取消
    public BaseResponse cancelPreTransactionOld(RequestCancelPreTransaction request){
        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        request.setRequestNo(requestNo);
        request.setTimestamp(DateUtil.getCurrentDateTime14());

        return httpPost(requestNo, RequestInterfaceXMEnum.CANCEL_PRE_TRANSACTION, request.getTransCode(), "", JSONObject.toJSONString(request));
    }
    //预处理取消最新版
    public BaseResponse cancelPreTransaction(String sourcePlatformUserNo,String targetPlatformUserNo,String subjectId,RequestCancelPreTransactionNew request){
        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        request.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);
        if (request.getCommission() != null && request.getCommission()>0){
            TransactionDetail transactionDetail = new TransactionDetail();
            transactionDetail.setBizType("COMMISSION");
            transactionDetail.setBusinessType(CheckFileConsts.BIZ_TYPE_11);
            transactionDetail.setAmount(request.getCommission());
            transactionDetail.setSourcePlatformUserNo(sourcePlatformUserNo);
            transactionDetail.setTargetPlatformUserNo(targetPlatformUserNo);
            transactionDetail.setSubjectId(subjectId);
            transactionDetail.setRequestNo(request.getRequestNo());
            transactionDetail.setCreditUnit(null);//债权份额（债权转让且需校验债权关系的必传）
            transactionDetail.setStatus(TransactionDetail.STATUS_SUCCEED);
            transactionDetail.setRequestTime(DateUtil.getCurrentDateTime14());
            transactionDetail.setType(TransactionDetail.FILE_COMMISSION);//批量投标请求解冻
            transactionDetailDao.insert(transactionDetail);
        }
        return httpPost(requestNo, RequestInterfaceXMEnum.CANCEL_PRE_TRANSACTION, request.getTransCode(), "", JSONObject.toJSONString(request));
    }
    //预处理取消
    public BaseResponse IntetligentProjectUnfreeze(RequestUnfreezeProject request){
        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        request.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);

        return httpPost(requestNo, RequestInterfaceXMEnum.INTELLIGENT_PROJECT_UNFREEZE, request.getTransCode(), "", JSONObject.toJSONString(request));
    }

    /**
     * 批量交易
     */
    public BaseResponse batchTrans(RequestBatchTrans request) {
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);

        String reqData = JSONObject.toJSONString(request);

        //================插入HTTP请求日志==================//
        TransLog transLog = this.addTransLog("", RequestInterfaceXMEnum.ASYNC_TRANSACTION, request.getTransCode(),
                "", reqData);

        //=======直连接口=======//
        String jsonResp = null;
        try {
            logger.info("批量交易，核心请求报文是 \n [{}]", reqData);
            jsonResp = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.ASYNC_TRANSACTION);
        } catch (ProcessException e) {
            if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "1", "厦门银行签名异常 - 批量交易");
                logger.warn("厦门银行签名异常 - 批量交易");
                return new BaseResponse("厦门银行签名异常 - 批量交易", BaseResponse.STATUS_FAILED, request.getBatchNo());

            } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "1", "HTTP调用平台接口异常 - 批量交易");
                logger.warn("HTTP调用平台接口异常 - 批量交易");
                return new BaseResponse("HTTP调用平台接口异常 - 批量交易", BaseResponse.STATUS_PENDING, request.getBatchNo());
            }
        }

        // 解析
        ResponseBatchTrans responseBatchTrans = JSONObject.parseObject(jsonResp, ResponseBatchTrans.class);

        String code = responseBatchTrans.getCode();

        BaseResponse response = new BaseResponse();
        response.setRequestNo(responseBatchTrans.getBatchNo());
        response.setCode(code);
        response.setDescription(responseBatchTrans.getDescription());

        Integer status;
        if ("0".equals(code)) {
            response.setStatus(BaseResponse.STATUS_SUCCEED);
            status = TransLog.STATUS_SUCCEED;
        } else {
            response.setStatus(BaseResponse.STATUS_FAILED);
            status = TransLog.STATUS_FAILED;
        }

        //================更新HTTP请求日志==================//
        transLogService.update(transLog.getId(), jsonResp, status, response.getCode(), response.getDescription());

        return response;
    }

    /**
     * 资金解冻
     */
    public BaseResponse unfreeze(RequestUnFreeze request){

        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        request.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);

        return httpPost(requestNo, RequestInterfaceXMEnum.UNFREEZE, request.getTransCode(), "", JSONObject.toJSONString(request));
    }

    /**
     * 资金冻结
     */
    public BaseResponse freeze(RequestFreeze request){

        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        request.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);

        return httpPost(requestNo, RequestInterfaceXMEnum.FREEZE, request.getTransCode(), "", JSONObject.toJSONString(request));
    }

    /**
     * 用户信息查询
     */
    public ResponseQueryUserInformation queryUserInformation(String platformUserNo) {

        String requestNo = IdUtil.getRequestNo();
        String timestamp = getTimestamp();

        HashMap<String, String> paramMap = new HashMap<>();
        paramMap.put("platformUserNo", platformUserNo);
        paramMap.put("timestamp", timestamp);//时间戳

        String reqData = JSONObject.toJSONString(paramMap);

        //================插入HTTP请求日志==================//
        TransLog transLog = this.addTransLog(requestNo, RequestInterfaceXMEnum.QUERY_USER_INFORMATION, "用户信息查询", "", reqData);

        //=======直连接口=======//
        String jsonResponse = null;
        try {
            logger.info("用户信息查询，核心请求报文是 \n [{}]", reqData);
            jsonResponse = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.QUERY_USER_INFORMATION);
        } catch (ProcessException e) {

            if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "", "厦门银行签名异常 - 用户信息查询");
                logger.warn("厦门银行签名异常 - 用户信息查询");
                return null;

            } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "", "HTTP调用平台接口异常 - 用户信息查询");
                logger.warn("HTTP调用平台接口异常 - 用户信息查询");
                return null;
            }
        }

        // 解析返回报文
        ResponseQueryUserInformation response = JSONObject.parseObject(jsonResponse, ResponseQueryUserInformation.class);

        logger.info("解析后，用户信息为 \n {}", response);

        Integer status;
        if ("0".equals(response.getCode())) {
            status = TransLog.STATUS_SUCCEED;
            response.setStatus(BaseResponse.STATUS_SUCCEED);
        } else {
            status = TransLog.STATUS_FAILED;
            response.setStatus(BaseResponse.STATUS_FAILED);
        }

        //================更新HTTP请求日志==================
        transLogService.update(transLog.getId(), jsonResponse, status, response.getCode(), response.getDescription());

        return response;
    }

    /**
     * 标的信息查询
     */
    public ResponseQueryProjectInformation queryProjectInformation(String projectNo) {
        String timestamp = getTimestamp();

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("projectNo", projectNo);
        paramMap.put("timestamp", timestamp);//时间戳

        String reqData = JSONObject.toJSONString(paramMap);

        //================插入HTTP请求日志==================//
        TransLog transLog = this.addTransLog("", RequestInterfaceXMEnum.QUERY_PROJECT_INFORMATION, "标的信息查询", "", reqData);

        //=======直连接口=======//
        String jsonResponse = null;
        try {
            logger.info("标的信息查询，核心请求报文是 \n [{}]", reqData);
            jsonResponse = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.QUERY_PROJECT_INFORMATION);
        } catch (ProcessException e) {

            if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "", "厦门银行签名异常 - 标的信息查询");
                logger.warn("厦门银行签名异常 - 标的信息查询");
                return null;

            } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "", "HTTP调用平台接口异常 - 标的信息查询");
                logger.warn("HTTP调用平台接口异常 - 标的信息查询");
                return null;
            }
        }

        // 解析返回报文
        ResponseQueryProjectInformation response = JSONObject.parseObject(jsonResponse, ResponseQueryProjectInformation.class);

        logger.info("解析后，标的信息为 \n {}", response);

        Integer status;
        if ("0".equals(response.getCode())) {
            status = TransLog.STATUS_SUCCEED;
        } else {
            status = TransLog.STATUS_FAILED;
        }

        //================更新HTTP请求日志==================
        transLogService.update(transLog.getId(), jsonResponse, status, response.getCode(), response.getDescription());

        return response;

    }

    /**
     * 单笔交易查询
     */
    public ResponseSingleTransQuery singleTransQuery(RequestSingleTransQuery request) {
        // 流水号
        String requestNo = request.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();
        }
        String timestamp = getTimestamp();

        request.setTimestamp(timestamp);

        String reqData = JSONObject.toJSONString(request);

        //================插入HTTP请求日志==================//
        TransLog transLog = this.addTransLog(requestNo, RequestInterfaceXMEnum.QUERY_TRANSACTION, request.getTransCode(),
                request.getTransactionType().toString(), reqData);

        //=======直连接口=======//
        String jsonResp = null;
        try {
            logger.info("单笔交易查询，核心请求报文是 \n [{}]", reqData);
            jsonResp = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.QUERY_TRANSACTION);
        } catch (ProcessException e) {
            if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "", "厦门银行签名异常 - 单笔交易查询");
                logger.warn("厦门银行签名异常 - 单笔交易查询");
                return new ResponseSingleTransQuery("厦门银行签名异常 - 单笔交易查询", BaseResponse.STATUS_FAILED, requestNo);

            } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "", "HTTP调用平台接口异常 - 单笔交易查询");
                logger.warn("HTTP调用平台接口异常 - 单笔交易查询");
                return new ResponseSingleTransQuery("HTTP调用平台接口异常 - 单笔交易查询", BaseResponse.STATUS_PENDING, requestNo);
            }
        }

        //解析
        ResponseSingleTransQuery response = this.getQueryResponse(jsonResp, request.getTransactionType());

        String code = response.getCode();
        //交易详情更新
        List<TransactionDetail> listDetails = transactionDetailDao.getByRequestNo(requestNo);
        //每一笔交易状态都更新
        for (TransactionDetail detail : listDetails) {
            Integer status;
            if ("0".equals(code)) {
                status = TransactionDetail.STATUS_SUCCEED;
            } else if("1".equals(code)) {
                status = TransactionDetail.STATUS_PENDING;
            }else{
                status = TransactionDetail.STATUS_FAILED;
            }
            detail.setStatus(status);
            detail.setUpdateTime(DateUtil.getCurrentDateTime14());
            transactionDetailDao.update(detail);
        }

        Integer status;
        if ("0".equals(code)) {
            response.setStatus(BaseResponse.STATUS_SUCCEED);
            status = TransLog.STATUS_SUCCEED;
        } else {
            response.setStatus(BaseResponse.STATUS_FAILED);
            status = TransLog.STATUS_FAILED;
        }

        //================更新HTTP请求日志==================//
        transLogService.update(transLog.getId(), jsonResp, status, response.getCode(), response.getDescription());

        return response;
    }

    /**
     * 批量投标请求流水查询
     */
    public ResponseQueryIntelligentProjectOrder queryIntelligentProjectOrder(String requestNo) {
        String timestamp = getTimestamp();

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("requestNo", requestNo);
        paramMap.put("timestamp", timestamp);

        String reqData = JSONObject.toJSONString(paramMap);

        //================插入HTTP请求日志==================//
        TransLog transLog = this.addTransLog(requestNo, RequestInterfaceXMEnum.QUERY_INTELLIGENT_PROJECT_ORDER,
                "批量投标请求流水查询", "", reqData);

        //=======直连接口=======//
        String jsonResponse = null;
        try {
            logger.info("标的信息查询，核心请求报文是 \n [{}]", reqData);
            jsonResponse = directConnectHttpService.doConnect(reqData, RequestInterfaceXMEnum.QUERY_INTELLIGENT_PROJECT_ORDER);
        } catch (ProcessException e) {

            if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "",
                        "厦门银行签名异常 - 批量投标请求流水查询");
                logger.warn("厦门银行签名异常 - 批量投标请求流水查询");
                return null;

            } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "",
                        "HTTP调用平台接口异常 - 批量投标请求流水查询");
                logger.warn("HTTP调用平台接口异常 - 批量投标请求流水查询");
                return null;
            }
        }

        // 解析返回报文
        ResponseQueryIntelligentProjectOrder response = JSONObject.parseObject(jsonResponse, ResponseQueryIntelligentProjectOrder.class);

        logger.info("解析后，标的信息为 \n {}", response);

        return response;
    }

    /**
     * 公共代码
     *
     * @param requestNo 请求流水号
     * @param type      请求厦门银行接口
     * @param transCode 业务层交易码
     * @param tradeType 交易码
     * @param reqData   请求报文
     * @return BaseResponse
     */
    private BaseResponse httpPost(String requestNo, RequestInterfaceXMEnum type, String transCode, String tradeType, String reqData) {
        try {
            //================插入HTTP请求日志==================//
            TransLog transLog = this.addTransLog(requestNo, type, transCode, tradeType, reqData);
            //=======直连接口=======//
            String jsonResponse = null;
            try {
                logger.info("厦门银行接口，核心请求报文是 \n [{}]", reqData);
                jsonResponse = directConnectHttpService.doConnect(reqData, type);
            } catch (ProcessException e) {

                if (Error.NDR_0802.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_FAILED, "", "厦门银行签名异常");
                    logger.warn("厦门银行签名异常 -");
                    return new BaseResponse("厦门银行签名异常", TransLog.STATUS_FAILED, requestNo);

                } else if (Error.NDR_0801.getCode().equals(e.getErrorCode())) {
                    transLogService.update(transLog.getId(), "", TransLog.STATUS_PENDING, "", "HTTP调用平台接口异常");
                    logger.warn("厦门银行签名异常 -");
                    return new BaseResponse("HTTP调用平台接口异常", TransLog.STATUS_PENDING , requestNo);
                }
            }

            //解析返回报文
            BaseResponse response = JSONObject.parseObject(jsonResponse, BaseResponse.class);

            if (StringUtils.isEmpty(response.getRequestNo())) {
                response.setRequestNo(requestNo);
            }

            Integer status;
            if ("0".equals(response.getCode())) {
                status = TransLog.STATUS_SUCCEED;
                response.setStatus(BaseResponse.STATUS_SUCCEED);
            }else if("1".equals(response.getCode())){
                status = TransLog.STATUS_PENDING;
                response.setStatus(BaseResponse.STATUS_PENDING);
            }else {
                status = TransLog.STATUS_FAILED;
                response.setStatus(BaseResponse.STATUS_FAILED);
            }
            //================更新HTTP请求日志==================//
            transLogService.update(transLog.getId(), jsonResponse, status, response.getCode(), response.getDescription());

            return response;
        } catch (Exception e) {
            return new BaseResponse("厦门银行交易异常", BaseResponse.STATUS_PENDING, requestNo);
        }
    }

    /**
     * 解析单笔交易查询返回报文
     *
     * @param jsonResp        返回报文
     * @param transactionType 交易类型
     * @return 返回报文
     */
    private ResponseSingleTransQuery getQueryResponse(String jsonResp, TransactionType transactionType) {
        ResponseSingleTransQuery response = JSONObject.parseObject(jsonResp, ResponseSingleTransQuery.class);

        if (null == JSONObject.parseObject(jsonResp).getString("records")) {
            return response;
        }

        List<Record> records = new ArrayList<>();
        if (TransactionType.RECHARGE.getCode().equals(transactionType.getCode())) {//充值查询
            List<RechargeQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<RechargeQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.WITHDRAW.getCode().equals(transactionType.getCode())) {//提现查询
            List<WithdrawQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<WithdrawQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.PRETRANSACTION.getCode().equals(transactionType.getCode())) {//交易预处理查询
            List<PerTransactionQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<PerTransactionQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.TRANSACTION.getCode().equals(transactionType.getCode())) {//交易确认查询
            List<TransactionQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<TransactionQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.FREEZE.getCode().equals(transactionType.getCode())) {//冻结查询
            List<FreezeQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<FreezeQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.DEBENTURE_SALE.getCode().equals(transactionType.getCode())) {//债权出让查询
            List<DebentureSaleQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<DebentureSaleQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.COMMISSION_DEDUCTING.getCode().equals(transactionType.getCode())) {//佣金扣除查询
            List<CommissionDeductingQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<CommissionDeductingQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.CANCEL_PRETRANSACTION.getCode().equals(transactionType.getCode())) {//取消预处理查询
            List<CancelPreTransactionQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<CancelPreTransactionQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.UNFREEZE.getCode().equals(transactionType.getCode())) {//解冻查询
            List<UnFreezeQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<UnFreezeQueryRecord>>() {
            });
            records.addAll(list);
        }
        if (TransactionType.INTERCEPT_WITHDRAW.getCode().equals(transactionType.getCode())) {//提现拦截查询
            List<InterceptWithdrawQueryRecord> list = JSON.parseObject(JSONObject.parseObject(jsonResp).getString("records"),
                    new TypeReference<List<InterceptWithdrawQueryRecord>>() {
            });
            records.addAll(list);
        }
        response.setRecords(records);
        return response;
    }



    /**
     * 日志记录
     *
     * @param requestNo   请求流水号
     * @param serviceName xm服务接口名称
     * @param transCode   我们这边的服务调用名称
     * @param tradeType   xm服务接口下的交易类型
     * @param reqData     请求核心报文
     * @return 交易日志
     */
    private TransLog addTransLog(String requestNo, RequestInterfaceXMEnum serviceName, String transCode, String tradeType, String reqData) {
        TransLog transLog = new TransLog(requestNo, serviceName.toString(), transCode, tradeType, reqData,TransLog.STATUS_PENDING,"1");
        return transLogService.add(transLog);
    }

    /**
     * 解冻用户资金并收取佣金
     * @param sourcePlatformUserNo
     * @param unFreezeAmount
     * @param commission
     * @return
     */

    public BaseResponse unFreezeAmount(String sourcePlatformUserNo,String investRequestNo ,Integer unFreezeAmount, Integer commission) {
        RequestUnfreezeProject request = constructRequest(investRequestNo,unFreezeAmount,commission);
        BaseResponse response = IntetligentProjectUnfreeze(request);
        if (commission>0){
            TransactionDetail transactionDetail = new TransactionDetail();
            transactionDetail.setBizType("COMMISSION");
            transactionDetail.setBusinessType(CheckFileConsts.BIZ_TYPE_11);
            transactionDetail.setAmount(commission/100.0);
            transactionDetail.setSourcePlatformUserNo(sourcePlatformUserNo);
            transactionDetail.setTargetPlatformUserNo("");
            transactionDetail.setSubjectId("");
            transactionDetail.setRequestNo(request.getRequestNo());
            transactionDetail.setCreditUnit(null);//债权份额（债权转让且需校验债权关系的必传）
            transactionDetail.setStatus(TransactionDetail.STATUS_SUCCEED);
            transactionDetail.setRequestTime(DateUtil.getCurrentDateTime14());
            transactionDetail.setType(TransactionDetail.FILE_COMMISSION);//批量投标请求解冻
            transactionDetailDao.insert(transactionDetail);
        }
        return response;
    }
    private RequestUnfreezeProject constructRequest(String freezeNo,Integer unFreezeAmount,Integer commission){
        RequestUnfreezeProject request = new RequestUnfreezeProject();
        request.setRequestNo(IdUtil.getRequestNo());
        request.setIntelRequestNo(freezeNo);
        request.setAmount(unFreezeAmount/100.0);
        if (commission>0){
            request.setCommission(commission/100.0);
        }
        return request;
    }

    public BaseResponse cancelDebentureSale(RequestCancelDebentureSale requestCancelDebentureSale){
        logger.info("开始调用厦门银行取消债权出让接口->{}", JSON.toJSONString(requestCancelDebentureSale));
        String requestNo = requestCancelDebentureSale.getRequestNo();
        if (StringUtils.isEmpty(requestNo)) {
            requestNo = IdUtil.getRequestNo();//流水号
        }
        requestCancelDebentureSale.setRequestNo(requestNo);
        String timestamp = getTimestamp();

        requestCancelDebentureSale.setTimestamp(timestamp);
        return httpPost(requestNo, RequestInterfaceXMEnum.CANCEL_DEBENTURE_SALE, requestCancelDebentureSale.getTransCode(),
                "", JSONObject.toJSONString(requestCancelDebentureSale));
    }

    private String getTimestamp(){
        AtomicReference<String> timestamp = new AtomicReference<>(DateUtil.getCurrentDateTime14());
        String time = timestamp.get().substring(8);
        Optional<String> timeOnOff = configService.getValueById(Config.TIME_ON_OFF);
        timeOnOff.ifPresent(t->{
            if (Config.IPLAN_OPEN_ON.equals(t)){
                Optional<String> timeSwitch = configService.getValueById(Config.TIME_SWITCH);
                timeSwitch.ifPresent(s -> {
                    timestamp.set(new StringBuilder().append(s).append(time).toString());
                });
            }
        });
        return timestamp.get();
    }
}
