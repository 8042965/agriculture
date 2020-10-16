package cn.agriculture.utils;

import com.google.code.kaptcha.Constants;
import com.google.code.kaptcha.Producer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;


/**
 * @Auther: truedei
 * @Date: 2020 /20-5-17 08:33
 * @Description:
 */
@RequestMapping("/rest/agriculture/kaptchaController")
@RestController
@Api("验证码工具类")
public class KaptchaController {


    /*
     * @Description //获取验证码
     * @Param [request, response]
     * @return void
     **/
    //这里的captchaProducer要和KaptchaConfig里面的bean命名一样
    @Autowired
    private Producer captchaProducer;

    @GetMapping(value = "/getKaptchaImage")
    @ApiOperation("获取验证码")
    public void getKaptchaImage(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //用字节数组存储
        byte[] captchaChallengeAsJpeg = null;
        ByteArrayOutputStream jpegOutputStream = new ByteArrayOutputStream();
        ServletOutputStream responseOutputStream = response.getOutputStream();
        final HttpSession httpSession=request.getSession();
        try {
            //生产验证码字符串并保存到session中
            String createText = captchaProducer.createText();
            //打印随机生成的字母和数字
//            log.debug(createText);
            System.out.println(createText);
            httpSession.setAttribute(Constants.KAPTCHA_SESSION_KEY, createText);
            //使用生产的验证码字符串返回一个BufferedImage对象并转为byte写入到byte数组中
            BufferedImage challenge = captchaProducer.createImage(createText);
            ImageIO.write(challenge, "jpg", jpegOutputStream);
            captchaChallengeAsJpeg = jpegOutputStream.toByteArray();
            response.setHeader("Cache-Control", "no-store");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            response.setContentType("image/jpeg");
            //定义response输出类型为image/jpeg类型，使用response输出流输出图片的byte数组
            responseOutputStream.write(captchaChallengeAsJpeg);
            responseOutputStream.flush();
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }finally {
            responseOutputStream.close();
        }
    }


    /*
     * @Description //校验验证码
     * @Param [request]
     * @return java.lang.String
     **/
    @PostMapping(value = "/checkcode",  produces = "text/html; charset=utf-8")
    @ApiOperation("验证验证码")
    public String checkcode(HttpServletRequest request) {
        JSONObject json = new JSONObject();

        String captchaId = (String)
                request.getSession().getAttribute(Constants.KAPTCHA_SESSION_KEY);
        String parameter = request.getParameter("veritycode");
        System.out.println("Session  vrifyCode "+captchaId+" form veritycode "+parameter);
        if (!captchaId.equals(parameter)) {
            json.put("msg","验证码错误");
        } else {
            json.put("msg","验证码正确");
        }

        return json.toString();
    }



    /**
     * 生成验证码
     */
    @RequestMapping(value = "/getVerify")
    @ResponseBody
    public void getVerify(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.setContentType("image/jpeg");//设置相应类型,告诉浏览器输出的内容为图片
            response.setHeader("Pragma", "No-cache");//设置响应头信息，告诉浏览器不要缓存此内容
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expire", 0);
            RandomValidateCodeUtil randomValidateCode = new RandomValidateCodeUtil();
            randomValidateCode.getRandcode(request, response);//输出验证码图片方法
        } catch (Exception e) {
            System.out.println("获取验证码失败>>>> "+ e.getMessage());
        }
    }

    /**
     * 忘记密码页面校验验证码
     */
    @RequestMapping(value = "/checkVerify", method = RequestMethod.POST, headers = "Accept=application/json")
    @ResponseBody
    public boolean checkVerify(String captcha, HttpSession session) {
        try{
            //从session中获取随机数
            String random = (String) session.getAttribute("RANDOMVALIDATECODEKEY");
            if (random == null) {
                return false;
            }
            if (random.equals(captcha)) {
                return true;
            } else {
                return false;
            }
        }catch (Exception e){
            System.out.println("验证码校验失败"+e);
            return false;
        }
    }


}
