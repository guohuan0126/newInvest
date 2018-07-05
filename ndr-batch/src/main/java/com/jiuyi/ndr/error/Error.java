package com.jiuyi.ndr.error;

/**
 * Created by zhangyibo on 2017/4/10.
 */
public class Error {

    private String code;

    private String message;

    public Error(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public static final Error NDR_0101 = new Error("0101","参数不能为空");//参数不能为空

    public static final Error NDR_0102 = new Error("0102","参数不合法");//参数不合法

    public static final Error NDR_0201 = new Error("0201","不存在的投资渠道");//不存在的投资渠道

    public static final Error NDR_0202 = new Error("0202","不存在该数据");

    public static final Error NDR_0203 = new Error("0203","不存在的开放渠道");

    public static final Error NDR_0204 = new Error("0204","不存在的优先级定义");

    public static final Error NDR_0205 = new Error("0205","不存在的债权来源渠道");

    public static final Error NDR_0206 = new Error("0206","不存在的资产类型");

    public static final Error NDR_0301 = new Error("0301","该债权已经开放，请勿重复操作");

    public static final Error NDR_0302 = new Error("0302","该债权尚未开放，不可购买");

    public static final Error NDR_0303 = new Error("0303","该债权中存在逾期标的，不能进行债转");

    public static final Error NDR_0304 = new Error("0304","该债权对应的标的已经结束，不能进行债转");

    public static final Error NDR_0305 = new Error("0305","该转让的债权已经放过款，请勿重复放款");

    public static final Error NDR_0306 = new Error("0306","转入交易已匹配金额与形成的债权金额不一致，请检查数据");

    public static final Error NDR_0401 = new Error("0401","该标的未开放或已募满，不可购买");

    public static final Error NDR_0402 = new Error("0402","该标的未开放或未募满或已放款，不可放款");

    public static final Error NDR_0403 = new Error("0403","没有该标的");

    public static final Error NDR_0404 = new Error("0404","投资金额低于起投金额");

    public static final Error NDR_0405 = new Error("0405","投资金额超过规定限额");

    public static final Error NDR_0406 = new Error("0406","投资金额未按规则递增");

    public static final Error NDR_0407 = new Error("0407","账户余额不足，还款失败");

    public static final Error NDR_0408 = new Error("0408","已还款或结清完成，请勿重复操作");

    public static final Error NDR_0409 = new Error("0409","标的还款有逾期，请先还清逾期");

    public static final Error NDR_0410 = new Error("0410","投资金额大于标的可投金额，投资失败");
    public static final Error NDR_0411 = new Error("0411","存在处理中的还款交易，请勿重复操作");
    public static final Error NDR_0412 = new Error("0412","还款失败，发放营销款到借款人账户失败");
    public static final Error NDR_0413 = new Error("0413","还款失败，发放营销款到借款人账户异常，请稍后再试");
    public static final Error NDR_0414 = new Error("0414","还款失败，冻结借款人资金失败");
    public static final Error NDR_0415 = new Error("0415","还款失败，冻结借款人资金异常，请稍后再试");
    public static final Error NDR_0416 = new Error("0416","还款失败，还款交易失败");
    public static final Error NDR_0417 = new Error("0417","还款失败，还款交易异常，请稍后再试");
    public static final Error NDR_0418 = new Error("0418","不满足投资条件，投资失败");
    public static final Error NDR_0419 = new Error("0419","不存在的用户");
    public static final Error NDR_0420 = new Error("0420","用户未激活");
    public static final Error NDR_0421 = new Error("0421","用户已注销");
    public static final Error NDR_0422 = new Error("0422","用户未在厦门银行开户");
    public static final Error NDR_0423 = new Error("0422","还款失败，标的居间人不对应还款居间人");
    public static final Error NDR_0424 = new Error("0422","还款失败，营销款01账户余额不足");
    public static final Error NDR_0425 = new Error("0422","还款失败，居间人账户余额不足");
    public static final Error NDR_0426 = new Error("0426", "用户月月盈账户已存在");

    public static final Error NDR_0458 = new Error("0458","自动提现调用接口请求失败");

    public static final Error NDR_0501 = new Error("0501","天天赚重复开户");
    public static final Error NDR_0502 = new Error("0502","投资金额大于新手限额");
    public static final Error NDR_0503 = new Error("0503","投资超出个人累计限额");
    public static final Error NDR_0504 = new Error("0504","投资金额小于起投金额");
    public static final Error NDR_0505 = new Error("0505","剩余可投金额不足");
    public static final Error NDR_0506 = new Error("0506","转出金额大于可转余额");
    public static final Error NDR_0507 = new Error("0507","天天赚未开户");
    public static final Error NDR_0508 = new Error("0508","账户已冻结");
    public static final Error NDR_0509 = new Error("0509","系统异常，待投资金额小于待解冻金额");
    public static final Error NDR_0510 = new Error("0510","转出失败，存在逾期债权");
    public static final Error NDR_0511 = new Error("0511","债权逾期");
    public static final Error NDR_0512 = new Error("0512","超出单日活期提现次数限制");
    public static final Error NDR_0513 = new Error("0513","超出单日活期提现金额限制");
    public static final Error NDR_0514 = new Error("0514","投资失败，调用厦门银行交易失败");
    public static final Error NDR_0515 = new Error("0515","转出失败，尚在锁定期");
    public static final Error NDR_0516 = new Error("0516","转出失败，调用厦门银行交易失败");
    public static final Error NDR_0517 = new Error("0517","投资失败，账户余额不足");
    public static final Error NDR_0518 = new Error("0518","非交易开放时间不允许交易");
    public static final Error NDR_0519 = new Error("0519","转出失败，存在待确认债权");
    public static final Error NDR_0520 = new Error("0520","无效金额");
    public static final Error NDR_0521 = new Error("0521","债权还款中");
    public static final Error NDR_0522 = new Error("0522","转出失败，存在还款中债权");

    public static final Error NDR_0601 = new Error("0601","有尚未追加的额度，请勿重复追加");
    public static final Error NDR_0602 = new Error("0602","该标的未形成全部债权,不能放款");

    public static final Error NDR_0801 = new Error("0801","HTTP调用平台接口异常");
    public static final Error NDR_0802 = new Error("0802","厦门银行签名异常");

    public static final Error NDR_0706 = new Error("0706", "此债权转让交易不存在");
    public static final Error NDR_0707 = new Error("0707", "此债权转让交易已完成,无法撤消");
    public static final Error NDR_0708 = new Error("0708", "剩余转让份额为零,无法撤消");
    public static final Error NDR_0714 = new Error("0714", "不是散标中的债权,不可撤消");
    public static final Error NDR_0715 = new Error("0715", "此债权未开放,不可撤消");
    public static final Error NDR_0716 = new Error("0716", "此债权已被购买完,不可撤消");
    public static final Error NDR_0717 = new Error("0717", "此债权已放款,不可撤消");

    public static final Error INVALID_TOKEN = new Error("40002","无效Token");
    public static final Error INTERNAL_ERROR = new Error("9999","未知错误");

    public static final Error NDR_04190 = new Error("0429", "省心投状态不可投");
    public static final Error NDR_04191 = new Error("04191","用户未注册");
    public static final Error NDR_04192 = new Error("04192","用户账户不存在");
    public static final Error NDR_04193 = new Error("04193","用户账户状态异常");
    public static final Error NDR_0428 = new Error("0428", "月月盈不存在");
    public static final Error NDR_0429 = new Error("0429", "此项目状态不可投");
    public static final Error NDR_0430 = new Error("0430", "月月盈产品定义为空");
    public static final Error NDR_0525 = new Error("0525","新手额度已用完");
    public static final Error NDR_0431 = new Error("0431", "用户手机号不存在");
    public static final Error NDR_0432 = new Error("0432", "没有可用的红包券");
    public static final Error NDR_0433 = new Error("0433", "新手标不可使用该红包券");
    public static final Error NDR_0434 = new Error("0434", "此红包券APP投资专享且新手标不可用");
    public static final Error NDR_0435 = new Error("0435", "此红包券APP投资专享");
    public static final Error NDR_0436 = new Error("0436", "月月盈期数不满足红包券使用要求");
    public static final Error NDR_0437 = new Error("0437", "您选择的红包券已使用");
    public static final Error NDR_0438 = new Error("0438", "您选择的红包券已过期");
    public static final Error NDR_0439 = new Error("0439", "此红包券投资不可用");
    public static final Error NDR_04391 = new Error("04391", "投资月月盈不可用天天赚红包券");
    public static final Error NDR_04392 = new Error("04392", "该红包券不是天天赚转投月月盈专属红包券");
    public static final Error NDR_0440 = new Error("0440", "投资金额小于红包可用限制金额");
    public static final Error NDR_0441 = new Error("0441", "投资利率大于限制利率");
    public static final Error NDR_0442 = new Error("0442", "老用户不能使用此类红包券");
    public static final Error NDR_0443 = new Error("0443", "按天加息红包券加息时间不能大于月月盈锁定时间");
    public static final Error NDR_0524 = new Error("0524", "投资金额不能小于等于抵扣券金额");
    public static final Error NDR_0448 = new Error("0448", "该交易记录不存在");
    public static final Error NDR_0453 = new Error("0453", "此月月盈存在处理中的投资记录，请稍后重试");
    public static final Error NDR_0723 = new Error("0723", "系统正在处理中,请耐心等待");
    public static final Error NDR_0906 = new Error("0906", "您转让的债权已售罄,无法撤销");
    public static final Error NDR_0910 = new Error("0910", "您当前可投金额为");
    public static final Error NDR_0500 = new Error("0500","爆款标不能红包券");
}
