package com.example.xiaoshoufuzhu;


public class User {
    private int id;             // 用户ID
    private String username;    // 登录账号
    private String password;    // 登录密码（通常不序列化）
    private String name;        // 真实姓名
    private String sex;         // 性别
    private Integer age;        // 年龄（使用包装类处理null值）
    private String email;       // 电子邮箱
    private String mobile;      // 手机号码
    private int userType;       // 用户角色类型 0-3
    private String address;     // 联系地址

    public User(int id, String username, String name, String sex,
                int age, String email, String mobile, int userType) {
        this(id, username, name, sex, age, email, mobile, userType, null);
    }

    // 全参数构造函数（用于数据库映射）
    public User(int id, String username, String name, String sex,
                Integer age, String email, String mobile,
                int userType, String address) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.sex = sex;
        this.age = age;
        this.email = email;
        this.mobile = mobile;
        this.userType = userType;
        this.address = address;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSex() {
        return sex != null ? sex : "未设置";
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public Integer getAge() {
        return age != null ? age : 0;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getEmail() {
        return email != null ? email : "未绑定";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getAddress() {
        return address != null ? address : "未填写";
    }

    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * 获取角色名称（中文显示）
     */
    public String getRoleName() {
        switch (userType) {
            case 0: return "系统管理员";
            case 1: return "经销商";
            case 2: return "销售员";
            case 3: return "采购员";
            default: return "未定义角色";
        }
    }

    /**
     * 获取脱敏手机号（保护隐私）
     */
    public String getSafeMobile() {
        if (mobile != null && mobile.length() == 11) {
            return mobile.substring(0, 3) + "****" + mobile.substring(7);
        }
        return mobile;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", role=" + getRoleName() +
                '}';
    }
}