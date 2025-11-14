package com.rauio.ZhihuiDangjiang.annotation.Serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.rauio.ZhihuiDangjiang.annotation.validation.Sensitive;

import java.io.IOException;

public class SensitiveDataSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private Sensitive.SensitiveType type;
    public SensitiveDataSerializer() {}

    public SensitiveDataSerializer(final Sensitive.SensitiveType type) {
        this.type = type;
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        switch (this.type) {
            case PHONE:
                gen.writeString(desensitizePhone(value));
                break;
            case ID_CARD:
                gen.writeString(desensitizeIdCard(value));
                break;
            case BANK_CARD:
                gen.writeString(desensitizeBankCard(value));
                break;
            case PASSWORD:
                gen.writeString(desensitizePassword(value));
                break;
            case EMAIL:
                gen.writeString(desensitizeEmail(value));
                break;
            default:
                gen.writeString(value);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property == null) {
            return prov.findNullValueSerializer(null);
        }
        Sensitive sensitive = property.getAnnotation(Sensitive.class);

        if (sensitive != null) {
            return new SensitiveDataSerializer(sensitive.type());
        }

        return prov.findValueSerializer(property.getType(), property);
    }
    /**
     * 手机号脱敏：保留前三位和后四位
     * 例如：13333333333 -> 133****3333
     */
    private String desensitizePhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 身份证号脱敏：保留前六位和后四位
     * 例如：340123199001011234 -> 340123********1234
     */
    private String desensitizeIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        return idCard.substring(0, 3) + "*************" + idCard.substring(idCard.length() - 2);
    }

    /**
     * 银行卡号脱敏：保留前六位和后四位
     */
    private String desensitizeBankCard(String cardNum) {
        if (cardNum == null || cardNum.length() < 10) {
            return cardNum;
        }
        return cardNum.substring(0, 6) + "******" + cardNum.substring(cardNum.length() - 4);
    }

    /**
     * 密码脱敏：返回空值
     */
    private String desensitizePassword(String password) {
        return "";
    }
    
    /**
     * 邮箱脱敏：保留用户名的前两位和后一位，以及完整的域名
     * 例如：<EMAIL> -> <EMAIL>
     * <EMAIL> -> <EMAIL>
     */
    private String desensitizeEmail(String email) {
        int atIndex = email.indexOf('@');

        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (username.length() <= 2) {
            return username.charAt(0) + "*" + domain;
        } else if (username.length() == 3) {
            return username.substring(0, 2) + "*" + domain;
        } else {
            return username.substring(0, 2) + "*" + username.substring(username.length() - 1) + domain;
        }
    }
}