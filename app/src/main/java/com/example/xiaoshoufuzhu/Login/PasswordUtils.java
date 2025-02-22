package com.example.xiaoshoufuzhu.Login;

import android.util.Base64;
import android.util.Log;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;

public class PasswordUtils {
    public static boolean checkPassword(String inputPassword, String storedHash) {
        try {
            String[] parts = storedHash.split("\\$");
            if (parts.length != 4) {
                Log.e("PasswordUtils", "无效的哈希格式");
                return false;
            }

            // 提取参数
            int iterations = Integer.parseInt(parts[1]);
            String salt = parts[2];
            String originalHash = parts[3];

            // 修正密钥长度
            KeySpec spec = new PBEKeySpec(
                    inputPassword.toCharArray(),
                    salt.getBytes(StandardCharsets.UTF_8),
                    iterations,
                    256 // 关键修正：这里使用256位（32字节）
            );

            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            byte[] hashBytes = factory.generateSecret(spec).getEncoded();

            // 生成32字节的哈希
            String generatedHash = Base64.encodeToString(hashBytes, Base64.NO_WRAP);

            Log.d("PasswordUtils", "数据库哈希: " + originalHash);
            Log.d("PasswordUtils", "生成哈希: " + generatedHash);

            return generatedHash.equals(originalHash);
        } catch (Exception e) {
            Log.e("PasswordUtils", "验证错误", e);
            return false;
        }
    }
}