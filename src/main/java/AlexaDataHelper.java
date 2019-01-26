import com.alibaba.fastjson.JSONObject;
import net.dongliu.requests.Requests;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ciel on 2018/11/23
 */
public class AlexaDataHelper {
    private volatile static AlexaDataHelper instance = null;

    public static AlexaDataHelper getInstance(){
        if (instance == null) {
            synchronized(AlexaDataHelper.class) {
                if (instance == null) {
                    instance = new AlexaDataHelper();
                }
            }
        }
        return instance;
    }
    private AlexaDataHelper(){
    }

    private String getToken(String domain) {
        String url = "http://www.alexa.cn/traffic/" + domain;
        Map<String, String> headers = SpiderUtil.setHeaders("Accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "Accept-Language: zh-CN,zh;q=0.9\n" +
                "Cache-Control: no-cache\n" +
                "Connection: keep-alive\n" +
                "Cookie: to paste" +
                "Host: www.alexa.cn\n" +
                "Pragma: no-cache\n" +
                "Upgrade-Insecure-Requests: 1\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36");
        String response = Requests.get(url).headers(headers).timeout(20000).send().readToText();
        Pattern pattern = Pattern.compile("token : '(.*?)'");
        Matcher matcher =  pattern.matcher(response);
        if (!matcher.find()) {
            return null;
        }
        return matcher.group(1);
    }

    public Map<String, Integer> getTraffic(String domain) {
        String token = getToken(domain);
        if (token == null) {
            return null;
        }
        String url = "http://www.alexa.cn/api/alexa/charge?token=" + token + "&url=" + domain;
        Map<String, String> headers = SpiderUtil.setHeaders("Accept: application/json, text/javascript, */*; q=0.01\n" +
                "Accept-Encoding: gzip, deflate\n" +
                "Accept-Language: zh-CN,zh;q=0.9\n" +
                "Cache-Control: no-cache\n" +
                "Connection: keep-alive\n" +
                "Cookie: PHPSESSID=ppc09thpsssdhvsnsu2sr7u491; exi_users=-C5V7nSei-HIEK7UCcBEJ1lcI1KYGh9R8WbHRy4aZLKmVwDwa9BbyYIObaqulAEIRTtnod7xma4Q0IKjM-CqhVJjDtRTQTT9UNHNTNfiMI9wYM9V-CyUhK2dH2JzA4tVScpU8W-HKwVgNNonTtMPf-CFNW3779CsJTgIXBZGJ9RvRecY1d7IQcKJI5rT4auyIXV2FHuHQmc9k-C6S-HaTDfkakn-CZIPfiNx-HZBE7WoUCSXgvv39h6YCjD-H61i-HvF0-Cluyu5eesO8OqS9DrwKZzRB1gABfxnOiXPRHINLR1LuUW7XeVfF8l-CmDkU-HJ3GlzV0dPIvPP3UN5FE1nYfNO-HwxCxhsMAwB2kMNav2sCXD812ZyBeX7qlnA9ZFxFQGxaMpr22Qc5rlL06sEwXTC-CobaSDBbLbLSQlTH2W77I1Pp67UMUxxViAkz1NQbIAjQj0UYFFLVlyRCf0rqOeUmXIYI4uzWJg-L-L; exi_query_history=Pv-Hx1-CbuwRSf3bqcpTO6zTQtmJdMvbXca0b58WNLGmrPyktlG9QCsnEpwKVpUyEw-C0MF-Hxq0R2vaZe8ssyRAMEUiG8sL3ErZF50JKzR-CSrr9KBVNJHOJeEsdn1cxIxQy7EeHEqOoUnQuWrj-C2S-Ckc2NF2Vkj9V5VeHqzOgSytxLl6Sou3hppUPOtshXaHgXJ7Nk8pL5HSzeaegY7wIAWnw-K-K\n" +
                "Host: www.alexa.cn\n" +
                "Pragma: no-cache\n" +
                "Referer: http://www.alexa.cn/traffic/gopro.com\n" +
                "User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36\n" +
                "X-Requested-With: XMLHttpRequest");
        String response;
        while (true) {
            try {
                response = Requests.get(url).headers(headers).timeout(30000).send().readToText();
                break;
            } catch (Exception e) {
                try {
                    Thread.sleep(20000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
        JSONObject jsonObject = JSONObject.parseObject(response);
        int uv = (int)(jsonObject.getJSONObject("data").getJSONObject("reach").getJSONObject("day").getFloatValue("million") * 3200);
        int pv = Math.round(uv * jsonObject.getJSONObject("data").getJSONObject("reach").getJSONObject("day").getFloatValue("pv_per_user"));
        Map<String, Integer> map = new HashMap<>();
        map.put("uv", uv);
        map.put("pv",pv);
        return map;
    }

    public static void main(String[] args) {
        System.out.println(AlexaDataHelper.getInstance().getTraffic("gopro.com"));
    }
}
