package com.jiuyi.ndr.service.contract;

import com.alibaba.fastjson.JSONObject;
import com.fadada.sample.client.FddClientBase;
import com.fadada.sample.client.FddClientExtra;
import com.jiuyi.ndr.service.contract.Response.ResponseFDD;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Map;

/**
 * 调用法大大接口
 *
 * @author ke 2017/5/16
 */
@Service
public class ContractFDDService {

    private Logger logger = LoggerFactory.getLogger(ContractService.class);

    private static String fontSize = "10";//字体大小，参考 word 字体设置，例如：10,12,12.5,14；不传则为默认值 9
    private static String fontType = "0";//字体类型，0-宋体；1-仿宋；2-黑体；3-楷体；4-微软雅黑

    @Autowired
    private FddClientBase client;
    @Autowired
    private FddClientExtra clientExtra;

    /**
     * 6.1 个人CA申请接口
     *
     * @param customerName 	客户中文姓名
     * @param email 		邮箱
     * @param idCard 		身份证（默认）
     * @param identType 	身份证（默认）
     * @param mobile 		手机号
     */
    public ResponseFDD syncPersonAuto(String customerName, String email, String idCard, String identType, String mobile) {
        String response = client.invokeSyncPersonAuto(customerName, email, idCard, identType, mobile);
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        return JSONObject.parseObject(response, ResponseFDD.class);
    }

    /**
     * 6.2 文档传输接口
     *
     * @param contractId 	合同编号，只允许长度<=32 的英文或数字字符
     * @param docTitle		合同标题，如“xx 投资合同”
     * @param file 			PDF文档，File 文件 doc_url 和 file 两个参数必选一
     * @param docUrl 		文档地址，文档地址 doc_url 和 file 两个参数必选一
     * @param docType 		文档类型， 如 .pdf
     */
    public ResponseFDD uploadDocs(String contractId, String docTitle, File file, String docUrl, String docType){
        String response = client.invokeUploadDocs(contractId, docTitle, file, docUrl, docType);
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        return null;
    }

    /**
     * 6.3 合同模板传输接口（PDF）
     *
     * @param templateId 	模板编号，只允许长度<=32 的英文或数字字符
     * @param file 			PDF模板
     */
    public ResponseFDD uploadTemplate(String templateId, File file){
        String response = client.invokeUploadTemplate(templateId, file, "");
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        return JSONObject.parseObject(response, ResponseFDD.class);
    }

    /**
     * 6.3 合同模板传输接口（DOC）
     *
     * @param templateId 	模板编号，只允许长度<=32 的英文或数字字符
     * @param docUrl 		文档地址，字段类型：字符串，须为 URL doc_url 和 file 两个参数必选一
     */
    public ResponseFDD uploadTemplate(String templateId, String docUrl){
        String response = client.invokeUploadTemplate(templateId, null, docUrl);
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        return JSONObject.parseObject(response, ResponseFDD.class);
    }

    /**
     * 6.4 生成合同接口
     *
     * @param contractId 	合同编号
     * @param templateId 	模板编号，上传合同时保留的编号
     * @param docTitle 		文档标题，如“xx投资合同”
     * @param map 			填充内容，json对象转字符串；key为文本域，value为要填充的值;示例：{"borrower":"小明","platformName":"法大大"}
     * @param dynamicTables 动态表格
     */
    public ResponseFDD generateContract(String contractId, String templateId, String docTitle, Map<String, String> map, String dynamicTables){

        String response = client.invokeGenerateContract(contractId, templateId, docTitle, fontSize, fontType, map, dynamicTables);
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        return JSONObject.parseObject(response, ResponseFDD.class);
    }

    /**
     * 6.6 文档签署接口（自动签）
     *      该接口为自动签署接口，接入平台在合同需要签章的位置写入用来定位的关键字（同一份合同里的关键字要保持唯一），
     *      在自动签约时法大大按此关键字（见 sign_keyword）进行签章位置的定位。
     *
     * @param transactionId 交易号 每次请求视为一个交易。只允许长度<=32的英文或数字字符。交易号为接入平台生成，必须保证唯一并自行记录。
     * @param customerId 	客户编号 CA注册时获取。
     * @param clientRole 	客户角色 1-接入平台，2-担保公司，3-投资人，4-借款人默认情况下，只有 1 和 3 可以使用自动签，其他角色如果需要自动签，请联系法大大的商务
     * @param contractId 	合同编号 根据合同编号指定在哪份文档上进行签署。合同编号在文档传输或合同生成时设定。
     * @param docTitle 		文档标题 如“xx 投资合同”
     * @param signKeyword 	定位关键字 关键字为文档中的文字内容（要能使用ctrl+f 搜索到）。法大大按此关键字进行签章位置的定位，将电子章盖在这个关键字上面。
     * @param notifyUrl 	签署结果异步通知URL 如果指定，当签章完成后，法大大将向此URL 发送签署结果。参见 签署结果异步通知规范（notify_url）
     */
    public ResponseFDD extSignAuto(String transactionId, String customerId, String clientRole, String contractId,
                                    String docTitle, String signKeyword, String notifyUrl){
        logger.info("法大大请求参数：transactionId："+transactionId+",customerId:"+customerId+",clientRole:"+clientRole+",contractId:"+contractId+",docTitle:"+docTitle+",signKeyword:"+signKeyword+",notifyUrl:"+notifyUrl);
        String response = client.invokeExtSignAuto(transactionId, customerId, clientRole, contractId, docTitle, signKeyword, notifyUrl);
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        logger.info("法大大返回报文  \n  [{}]", response);
        return JSONObject.parseObject(response, ResponseFDD.class);
    }

    /**
     * 6.10 合同归档接口
     *	    接入平台更新合同签署状态为-签署完成，法大大将把合同所有相关操作记录进行归档存证。归档后将不能再对文档再进行签署操作。
     *
     * @param contractId    合同编号
     */
    public ResponseFDD contractFilling(String contractId){
        String response = client.invokeContractFilling(contractId);
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        return JSONObject.parseObject(response, ResponseFDD.class);
    }

    /**
     * 7.3 下载已签署接口
     *     调用场景：
     *      在客户完成签章后，接入平台可以自法大大下载已签章的文档（PDF 格式），自行存储或者提供下载链接给客户。
     *
     * @param path 			保存路径
     * @param contractId 	合同号
     */
    public ResponseFDD downloadPdf(String path, String contractId){
        String response = clientExtra.invokeDownloadPdf(path, contractId);
        if (logger.isDebugEnabled()) {
            logger.info("法大大返回报文  \n  [{}]", response);
        }
        return JSONObject.parseObject(response, ResponseFDD.class);
    }
}
