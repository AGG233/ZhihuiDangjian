package com.rauio.smartdangjian.pojo.response;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResultTest {

    @Test
    @DisplayName("无参构造器 code 为 200，message 为 OK")
    void defaultConstructorSetsOk() {
        Result<Void> result = new Result<>();

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("单参构造器 code 为 200，message 为 OK，data 正确设置")
    void dataConstructorSetsData() {
        Result<String> result = new Result<>("hello");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getData()).isEqualTo("hello");
    }

    @Test
    @DisplayName("ok(data) 工厂方法返回 code=200 message=OK")
    void okWithDataReturns200() {
        Result<String> result = Result.ok("测试数据");

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getData()).isEqualTo("测试数据");
    }

    @Test
    @DisplayName("ok(data) null data 也正确返回")
    void okWithNullDataReturns200() {
        Result<Object> result = Result.ok(null);

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("ok(code, message, data) 工厂方法返回自定义 code 和 message")
    void okWithCustomCodeMessage() {
        Result<String> result = Result.ok("201", "创建成功", "new-item");

        assertThat(result.getCode()).isEqualTo("201");
        assertThat(result.getMessage()).isEqualTo("创建成功");
        assertThat(result.getData()).isEqualTo("new-item");
    }

    @Test
    @DisplayName("error(code, message) 工厂方法返回自定义错误码和信息")
    void errorReturnsCustomCodeMessage() {
        Result<Void> result = Result.error("400", "请求参数错误");

        assertThat(result.getCode()).isEqualTo("400");
        assertThat(result.getMessage()).isEqualTo("请求参数错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("internalError(code, message) 委托 error 方法")
    void internalErrorDelegatesToError() {
        Result<Void> result = Result.internalError("500", "服务器错误");

        assertThat(result.getCode()).isEqualTo("500");
        assertThat(result.getMessage()).isEqualTo("服务器错误");
        assertThat(result.getData()).isNull();
    }

    @Test
    @DisplayName("Builder 模式构建 Result 设置所有字段")
    void builderSetsAllFields() {
        Result<String> result = Result.<String>builder()
                .code("200")
                .message("OK")
                .data("builder-data")
                .build();

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
        assertThat(result.getData()).isEqualTo("builder-data");
    }

    @Test
    @DisplayName("Builder 默认 code 为 200，message 为 OK")
    void builderDefaults() {
        Result<?> result = Result.builder().build();

        assertThat(result.getCode()).isEqualTo("200");
        assertThat(result.getMessage()).isEqualTo("OK");
    }

    @Test
    @DisplayName("setter 设置字段后 getter 正确返回")
    void settersAndGettersWork() {
        Result<String> result = new Result<>();

        result.setCode("500");
        result.setMessage("服务器错误");
        result.setData("error-data");

        assertThat(result.getCode()).isEqualTo("500");
        assertThat(result.getMessage()).isEqualTo("服务器错误");
        assertThat(result.getData()).isEqualTo("error-data");
    }
}
