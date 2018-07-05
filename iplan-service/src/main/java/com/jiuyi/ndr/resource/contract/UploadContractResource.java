package com.jiuyi.ndr.resource.contract;

import com.jiuyi.ndr.domain.contract.ContractTemplate;
import com.jiuyi.ndr.service.contract.ContractService;
import com.jiuyi.ndr.service.xm.util.IdUtil;
import com.jiuyi.ndr.xm.http.BaseResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * @author ke 2017/5/15
 */
@RestController
@RequestMapping("/contract/file")
public class UploadContractResource {

    private Logger logger = LoggerFactory.getLogger(ContractService.class);

    @Autowired
    private ContractService contractService;

    @RequestMapping("/iplan")
    @PostMapping
    @Transactional
    public String upload(@RequestParam(value = "file")MultipartFile multipartFile) throws IOException {
        File file = new File(""+multipartFile.getOriginalFilename());
        this.inputStreamToFile(multipartFile.getInputStream(), file);
        BaseResponse baseResponse = contractService.uploadTemplatePDF(IdUtil.randomUUID(), file,
                ContractTemplate.SIGN_TYPE_IPLAN_SERVICE, ContractTemplate.SIGN_TYPE_IPLAN_SERVICE);
        if (!file.delete()) {
            file.delete();
        }
        return baseResponse.toString();
    }

    //债权转让及居间服务协议
    @RequestMapping("/assignment")
    @PostMapping
    @Transactional
    public String upload2(@RequestParam(value = "file")MultipartFile multipartFile) throws IOException {
        File file = new File(""+multipartFile.getOriginalFilename());
        this.inputStreamToFile(multipartFile.getInputStream(), file);
        BaseResponse baseResponse = contractService.uploadTemplatePDF(IdUtil.randomUUID(), file,
                ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT, ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT);
        if (!file.delete()) {
            file.delete();
        }
        return baseResponse.toString();
    }

    //直贷标借款及居间服务协议
    @RequestMapping("/direct")
    @PostMapping
    @Transactional
    public String upload3(@RequestParam(value = "file")MultipartFile multipartFile) throws IOException {
        File file = new File(""+multipartFile.getOriginalFilename());
        this.inputStreamToFile(multipartFile.getInputStream(), file);
        BaseResponse baseResponse = contractService.uploadTemplatePDF(IdUtil.randomUUID(), file,
                ContractTemplate.SIGN_TYPE_LOAN, ContractTemplate.SIGN_TYPE_LOAN);
        if (!file.delete()) {
            file.delete();
        }
        return baseResponse.toString();
    }

    //债转标投资协议
    @RequestMapping("/invest")
    @PostMapping
    @Transactional
    public String upload4(@RequestParam(value = "file")MultipartFile multipartFile) throws IOException {
        File file = new File(""+multipartFile.getOriginalFilename());
        this.inputStreamToFile(multipartFile.getInputStream(), file);
        BaseResponse baseResponse = contractService.uploadTemplatePDF(IdUtil.randomUUID(), file,
                ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT_INVEST, ContractTemplate.SIGN_TYPE_CREDIT_ASSIGNMENT_INVEST);
        if (!file.delete()) {
            file.delete();
        }
        return baseResponse.toString();
    }

    //读取流到文件
    private void inputStreamToFile(InputStream ins, File file) throws IOException {
        OutputStream os = new FileOutputStream(file);
        int bytesRead = 0;
        byte[] buffer = new byte[8192];
        while ((bytesRead = ins.read(buffer, 0, 8192)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        logger.info("已读取到文件[{}]，开始上传",file.getName());
        os.close();
        ins.close();
    }
}
