package com.kang.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.kang.common.AppException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.request.HttpRequest;
import com.mashape.unirest.request.HttpRequestWithBody;

/**
 * @author kang
 */
public class HttpUtil {

    public static String doRequest(HttpRequest request) {
        try {
            HttpResponse<String> response = request.header("accept", "application/json").asString();
            CommonRes commonRes = JSONObject.parseObject(response.getBody(), new TypeReference<CommonRes>() {
            });
            return commonRes != null && commonRes.getStatus() == 1 ? commonRes.getData() : null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(String.format("Fail to query : %s", e.getMessage()));
        }

    }

    public static String doRequest(HttpRequestWithBody request, JSONObject body) {
        try {
            HttpResponse<String> response = request.header("accept", "application/json").header("Content-Type", "application/json").body(body.toJSONString()).asString();
            CommonRes commonRes = JSONObject.parseObject(response.getBody(), new TypeReference<CommonRes>() {
            });
            return commonRes != null && commonRes.getStatus() == 1 ? commonRes.getData() : null;
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppException(String.format("Fail to query : %s", e.getMessage()));
        }
    }

    private static class CommonRes {

        private int status;
        private String data;

        public CommonRes() {
        }

        public int getStatus() {
            return this.status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getData() {
            return this.data;
        }

        public void setData(String data) {
            this.data = data;
        }
    }
}
