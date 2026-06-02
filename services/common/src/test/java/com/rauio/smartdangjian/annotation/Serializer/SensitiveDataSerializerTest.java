package com.rauio.smartdangjian.annotation.Serializer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.rauio.smartdangjian.annotation.validation.Sensitive;

class SensitiveDataSerializerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ---------- Inner classes for each sensitive type ----------

    static class PhoneBean {
        @Sensitive(type = Sensitive.SensitiveType.PHONE)
        public String value;
    }

    static class IdCardBean {
        @Sensitive(type = Sensitive.SensitiveType.ID_CARD)
        public String value;
    }

    static class BankCardBean {
        @Sensitive(type = Sensitive.SensitiveType.BANK_CARD)
        public String value;
    }

    static class PasswordBean {
        @Sensitive(type = Sensitive.SensitiveType.PASSWORD)
        public String value;
    }

    static class EmailBean {
        @Sensitive(type = Sensitive.SensitiveType.EMAIL)
        public String value;
    }

    static class NoAnnotationBean {
        public String value;
    }

    // ---------- Constructor ----------

    @Test
    @DisplayName("no-arg constructor creates valid instance")
    void noArgConstructorCreatesInstance() {
        assertThat(new SensitiveDataSerializer()).isNotNull();
    }

    // ---------- Null ----------

    @Test
    @DisplayName("value 为 null 时写入 null")
    void serializeNullWritesNull() throws Exception {
        PhoneBean bean = new PhoneBean();
        bean.value = null;
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":null}");
    }

    // ---------- PHONE ----------

    @Test
    @DisplayName("PHONE 类型脱敏：133****3333")
    void serializePhone() throws Exception {
        PhoneBean bean = new PhoneBean();
        bean.value = "13333333333";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"133****3333\"}");
    }

    @Test
    @DisplayName("PHONE 长度不足 11 位时不脱敏")
    void serializeShortPhone() throws Exception {
        PhoneBean bean = new PhoneBean();
        bean.value = "12345";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"12345\"}");
    }

    @Test
    @DisplayName("PHONE 长度大于 11 位时不脱敏")
    void serializeLongPhone() throws Exception {
        PhoneBean bean = new PhoneBean();
        bean.value = "123456789012";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"123456789012\"}");
    }

    @Test
    @DisplayName("PHONE 长度不等于 11 位时原样返回")
    void serializePhoneLengthNotEqual11() throws Exception {
        PhoneBean bean = new PhoneBean();
        bean.value = "1234";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"1234\"}");
    }

    // ---------- ID_CARD ----------

    @Test
    @DisplayName("ID_CARD 类型脱敏：前3后2")
    void serializeIdCard() throws Exception {
        IdCardBean bean = new IdCardBean();
        bean.value = "340123199001011234";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"340*************34\"}");
    }

    @Test
    @DisplayName("ID_CARD 长度不足 10 位时不脱敏")
    void serializeShortIdCard() throws Exception {
        IdCardBean bean = new IdCardBean();
        bean.value = "12345";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"12345\"}");
    }

    // ---------- BANK_CARD ----------

    @Test
    @DisplayName("BANK_CARD 类型脱敏：前6后4")
    void serializeBankCard() throws Exception {
        BankCardBean bean = new BankCardBean();
        bean.value = "1234567890123456";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"123456******3456\"}");
    }

    @Test
    @DisplayName("BANK_CARD 长度不足 10 位时不脱敏")
    void serializeShortBankCard() throws Exception {
        BankCardBean bean = new BankCardBean();
        bean.value = "12345";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"12345\"}");
    }

    @Test
    @DisplayName("BANK_CARD 脱敏：正确的 card number")
    void serializeBankCardCorrect() throws Exception {
        BankCardBean bean = new BankCardBean();
        bean.value = "6222021234567890";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"622202******7890\"}");
    }

    // ---------- PASSWORD ----------

    @Test
    @DisplayName("PASSWORD 类型脱敏返回空字符串")
    void serializePassword() throws Exception {
        PasswordBean bean = new PasswordBean();
        bean.value = "anyPassword";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"\"}");
    }

    // ---------- EMAIL ----------

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名大于3位时保留前2后1")
    void serializeEmailLongUsername() throws Exception {
        EmailBean bean = new EmailBean();
        bean.value = "testuser@example.com";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"te*r@example.com\"}");
    }

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名等于3位时保留前2")
    void serializeEmailThreeCharUsername() throws Exception {
        EmailBean bean = new EmailBean();
        bean.value = "abc@example.com";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"ab*@example.com\"}");
    }

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名小于等于2位时保留第1位")
    void serializeEmailShortUsername() throws Exception {
        EmailBean bean = new EmailBean();
        bean.value = "ab@example.com";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"a*@example.com\"}");
    }

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名为单字符时保留该字符")
    void serializeEmailOneCharUsername() throws Exception {
        EmailBean bean = new EmailBean();
        bean.value = "a@example.com";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"a*@example.com\"}");
    }

    @Test
    @DisplayName("EMAIL 用户名长度为2时脱敏")
    void serializeEmailUsernameLength2() throws Exception {
        EmailBean bean = new EmailBean();
        bean.value = "xy@test.com";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"x*@test.com\"}");
    }

    @Test
    @DisplayName("EMAIL 用户名长度为3时脱敏")
    void serializeEmailUsernameLength3() throws Exception {
        EmailBean bean = new EmailBean();
        bean.value = "abc@test.com";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"ab*@test.com\"}");
    }

    // ---------- ContextualSerializer ----------

    @Test
    @DisplayName("createContextual 字段有 Sensitive 注解时返回正确的序列化器")
    void createContextualWithAnnotation() throws Exception {
        PhoneBean bean = new PhoneBean();
        bean.value = "13333333333";
        String result = mapper.writeValueAsString(bean);
        assertThat(result).contains("133****3333");
    }

    @Test
    @DisplayName("createContextual property 为 null 时返回空值序列化器")
    void createContextualWithNullProperty() throws Exception {
        // 将 SensitiveDataSerializer 注册为 String 的默认序列化器
        // 当序列化根级值时 BeanProperty 为 null，触发 property == null 分支
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule module = new SimpleModule();
        module.addSerializer(String.class, new SensitiveDataSerializer());
        mapper.registerModule(module);

        // 根级 String 没有 BeanProperty 上下文
        // createContextual(prov, null) 返回 findNullValueSerializer → NullSerializer
        // NullSerializer 始终写入 null
        String result = mapper.writeValueAsString("anything");
        assertThat(result).isEqualTo("null");
    }

    @Test
    @DisplayName("createContextual 字段无 Sensitive 注解时使用默认序列化器")
    void createContextualWithoutAnnotation() throws Exception {
        NoAnnotationBean bean = new NoAnnotationBean();
        bean.value = "hello";
        assertThat(mapper.writeValueAsString(bean)).isEqualTo("{\"value\":\"hello\"}");
    }
}
