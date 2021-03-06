package com.example.renyi.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.renyi.service.BasicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping(value = "/token")
public class TokenController {

    private static final Logger LOGGER = LoggerFactory.getLogger(com.example.renyi.controller.TokenController.class);


    @Autowired
    private BasicService basicService;

    //这个里面 主要 用来 接受 code ,刷新 token ，更新对应的数据库

    @RequestMapping(value="/recode", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String recode(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.error("------------------------------- 正式OAuth回调地址  ------------------------------------------");
        String code = request.getParameter("code");
        //第一次授权后，会有这个code,立刻调用 一次 授权码换token接口 ，拿到完整的 token 相关信息，并写入数据库。

        //3月17日思考： 暂时不用接口来访问，直接在线访问后 拿到第一次的数据，并 复制 填入数据库表中接口（后续定时任务来更新）
        return code;
    }


    //测试消息订阅的接口。
    @RequestMapping(value="/ticket", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String reticket(HttpServletRequest request, HttpServletResponse response) {
        LOGGER.error("------------------------------- 正式消息接收地址  ------------------------------------------");





        return "\n" +
                "{\n" +
                "    \"result\":\"success\"\n" +
                "}";
    }


    //系统管理里面  的 消息订阅验证接口
    @RequestMapping(value="/dy2kai", method = {RequestMethod.GET,RequestMethod.POST})
    public @ResponseBody String dy2kai(HttpServletRequest request, HttpServletResponse response) throws  Exception{
        String echostr = request.getParameter("echostr");
        try{
            String nonce = request.getParameter("nonce");
            String signature = request.getParameter("signature");
            String timestamp = request.getParameter("timestamp");

            InputStreamReader reader=new InputStreamReader(request.getInputStream(),"utf-8");
            BufferedReader buffer=new BufferedReader(reader);
            String params=buffer.readLine();
            LOGGER.error("请求参数: "+params);
            JSONObject jsonObject = JSONObject.parseObject(params);
            String code = jsonObject.getString("Code");
            LOGGER.error("Code =============== " + code);
            LOGGER.error("当前操作，0 保存，1 审核，2 弃审，3 删除，4 取消中止，5 中止");
            String state = jsonObject.getString("SendState");
            LOGGER.error("SendState =============== " + state);
            LOGGER.error("-----------------------------------操作结束-----------------------------------");
            String OrgId = jsonObject.getString("OrgId");
            //如果是销货单，并且是 审核 条件，
            if(code.contains("SA") && state.equals("1")){
                //查询对应的这个订单明细。并调用services，访问 红旗
                Map<String,String> pas = new HashMap<String,String>();
                pas.put("OrgId",OrgId);
                pas.put("code",code);
                // 通过 OrgId 来获取 AppKey 和 AppSecret
                pas.put("AppKey",code);
                pas.put("AppSecret",code);
                String reslut = basicService.getSaOrder(pas);
                LOGGER.error("result == " + reslut);//这个销货单 的 明细 内容。
                //调用 新的 services 转换成HQ 的参数，并调用HQ接口，返回结果
                String HQresult = basicService.HQsaorder(reslut);

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return echostr;
    }

}