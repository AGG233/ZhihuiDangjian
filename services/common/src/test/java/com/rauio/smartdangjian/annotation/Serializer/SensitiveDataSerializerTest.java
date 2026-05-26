package com.rauio.smartdangjian.annotation.Serializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.rauio.smartdangjian.annotation.validation.Sensitive;

@ExtendWith(MockitoExtension.class)
class SensitiveDataSerializerTest {

    @Mock
    private JsonGenerator gen;

    @Mock
    private SerializerProvider provider;

    private final SensitiveDataSerializer serializer = new SensitiveDataSerializer();

    @Test
    @DisplayName("no-arg constructor creates valid instance")
    void noArgConstructorCreatesInstance() {
        SensitiveDataSerializer s = new SensitiveDataSerializer();
        assertThat(s).isNotNull();
    }

    @Test
    @DisplayName("desensitizePhone with null returns null via reflection")
    void desensitizePhoneNullReturnsNull() throws Exception {
        var method = SensitiveDataSerializer.class.getDeclaredMethod("desensitizePhone", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(serializer, new Object[] {null});
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("desensitizeIdCard with null returns null via reflection")
    void desensitizeIdCardNullReturnsNull() throws Exception {
        var method = SensitiveDataSerializer.class.getDeclaredMethod("desensitizeIdCard", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(serializer, new Object[] {null});
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("desensitizeBankCard with null returns null via reflection")
    void desensitizeBankCardNullReturnsNull() throws Exception {
        var method = SensitiveDataSerializer.class.getDeclaredMethod("desensitizeBankCard", String.class);
        method.setAccessible(true);
        String result = (String) method.invoke(serializer, new Object[] {null});
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("value 为 null 时写入 null")
    void serializeNullWritesNull() throws IOException {
        SensitiveDataSerializer phoneSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PHONE);
        phoneSerializer.serialize(null, gen, provider);

        verify(gen).writeNull();
    }

    @Test
    @DisplayName("PHONE 类型脱敏：133****3333")
    void serializePhone() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PHONE);
        serializer.serialize("13333333333", gen, provider);

        verify(gen).writeString("133****3333");
    }

    @Test
    @DisplayName("PHONE 长度不足 11 位时不脱敏")
    void serializeShortPhone() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PHONE);
        serializer.serialize("12345", gen, provider);

        verify(gen).writeString("12345");
    }

    @Test
    @DisplayName("ID_CARD 类型脱敏：前3后2")
    void serializeIdCard() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.ID_CARD);
        serializer.serialize("340123199001011234", gen, provider);

        verify(gen).writeString("340*************34");
    }

    @Test
    @DisplayName("ID_CARD 长度不足 10 位时不脱敏")
    void serializeShortIdCard() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.ID_CARD);
        serializer.serialize("12345", gen, provider);

        verify(gen).writeString("12345");
    }

    @Test
    @DisplayName("BANK_CARD 类型脱敏：前6后4")
    void serializeBankCard() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.BANK_CARD);
        serializer.serialize("1234567890123456", gen, provider);

        verify(gen).writeString("123456******3456");
    }

    @Test
    @DisplayName("BANK_CARD 长度不足 10 位时不脱敏")
    void serializeShortBankCard() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.BANK_CARD);
        serializer.serialize("12345", gen, provider);

        verify(gen).writeString("12345");
    }

    @Test
    @DisplayName("PASSWORD 类型脱敏返回空字符串")
    void serializePassword() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PASSWORD);
        serializer.serialize("anyPassword", gen, provider);

        verify(gen).writeString("");
    }

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名大于3位时保留前2后1")
    void serializeEmailLongUsername() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.EMAIL);
        serializer.serialize("testuser@example.com", gen, provider);

        verify(gen).writeString("te*r@example.com");
    }

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名等于3位时保留前2")
    void serializeEmailThreeCharUsername() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.EMAIL);
        serializer.serialize("abc@example.com", gen, provider);

        verify(gen).writeString("ab*@example.com");
    }

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名小于等于2位时保留第1位")
    void serializeEmailShortUsername() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.EMAIL);
        serializer.serialize("ab@example.com", gen, provider);

        verify(gen).writeString("a*@example.com");
    }

    @Test
    @DisplayName("createContextual property 为 null 时返回 null 值序列化器")
    void createContextualNullProperty() throws JsonMappingException {
        when(provider.findNullValueSerializer(null)).thenReturn(null);

        var result = serializer.createContextual(provider, null);

        assertThat(result).isNull();
    }

    @Test
    @DisplayName("createContextual 字段有 Sensitive 注解时返回正确的序列化器")
    void createContextualWithAnnotation() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        Sensitive sensitive = mock(Sensitive.class);
        when(sensitive.type()).thenReturn(Sensitive.SensitiveType.PHONE);
        when(property.getAnnotation(Sensitive.class)).thenReturn(sensitive);

        var result = serializer.createContextual(provider, property);

        assertThat(result).isInstanceOf(SensitiveDataSerializer.class);
    }

    @Test
    @DisplayName("createContextual 字段无 Sensitive 注解时委托给 provider")
    void createContextualWithoutAnnotation() throws Exception {
        BeanProperty property = mock(BeanProperty.class);
        when(property.getAnnotation(Sensitive.class)).thenReturn(null);

        serializer.createContextual(provider, property);

        verify(provider).findValueSerializer(property.getType(), property);
    }

    @Test
    @DisplayName("PHONE 类型脱敏：长度为 null 时不脱敏")
    void serializeNullPhone() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PHONE);
        serializer.serialize(null, gen, provider);

        verify(gen).writeNull();
    }

    @Test
    @DisplayName("ID_CARD 长度为 null 时不脱敏")
    void serializeNullIdCard() throws IOException {
        SensitiveDataSerializer idCardSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.ID_CARD);
        idCardSerializer.serialize(null, gen, provider);

        verify(gen).writeNull();
    }

    @Test
    @DisplayName("BANK_CARD 脱敏：正确的 card number")
    void serializeBankCardCorrect() throws IOException {
        SensitiveDataSerializer serializer = new SensitiveDataSerializer(Sensitive.SensitiveType.BANK_CARD);
        serializer.serialize("6222021234567890", gen, provider);

        verify(gen).writeString("622202******7890");
    }

    @Test
    @DisplayName("BANK_CARD 长度为 null 时不脱敏")
    void serializeNullBankCard() throws IOException {
        SensitiveDataSerializer bankCardSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.BANK_CARD);
        bankCardSerializer.serialize(null, gen, provider);

        verify(gen).writeNull();
    }

    @Test
    @DisplayName("EMAIL 长度为 null 时不脱敏")
    void serializeNullEmail() throws IOException {
        SensitiveDataSerializer emailSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.EMAIL);
        emailSerializer.serialize(null, gen, provider);

        verify(gen).writeNull();
    }

    @Test
    @DisplayName("PHONE 类型脱敏：电话号码为 null 时不脱敏")
    void serializePhoneNullValue() throws IOException {
        SensitiveDataSerializer phoneSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PHONE);
        phoneSerializer.serialize(null, gen, provider);

        verify(gen).writeNull();
    }

    @Test
    @DisplayName("PHONE 长度大于 11 位时不脱敏")
    void serializeLongPhone() throws IOException {
        SensitiveDataSerializer phoneSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PHONE);
        phoneSerializer.serialize("123456789012", gen, provider);

        verify(gen).writeString("123456789012");
    }

    @Test
    @DisplayName("EMAIL 类型脱敏：用户名为单字符时保留该字符")
    void serializeEmailOneCharUsername() throws IOException {
        SensitiveDataSerializer emailSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.EMAIL);
        emailSerializer.serialize("a@example.com", gen, provider);

        verify(gen).writeString("a*@example.com");
    }

    @Test
    @DisplayName("PHONE 长度不等于11位时原样返回")
    void serializePhoneLengthNotEqual11() throws IOException {
        SensitiveDataSerializer phoneSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.PHONE);
        phoneSerializer.serialize("1234", gen, provider);

        verify(gen).writeString("1234");
    }

    @Test
    @DisplayName("EMAIL 用户名长度为2时脱敏")
    void serializeEmailUsernameLength2() throws IOException {
        SensitiveDataSerializer emailSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.EMAIL);
        emailSerializer.serialize("xy@test.com", gen, provider);

        verify(gen).writeString("x*@test.com");
    }

    @Test
    @DisplayName("EMAIL 用户名长度为3时脱敏")
    void serializeEmailUsernameLength3() throws IOException {
        SensitiveDataSerializer emailSerializer = new SensitiveDataSerializer(Sensitive.SensitiveType.EMAIL);
        emailSerializer.serialize("abc@test.com", gen, provider);

        verify(gen).writeString("ab*@test.com");
    }
}
