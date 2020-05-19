package com.yryz.network.http.token

enum class TokenEnum(var code: Int) {

    /**
     * token无效
     */
    CODE_101(101),
    /**
     * 短TOKEN过期
     */
    CODE_102(102),

    /**
     * 长token过期  退出登录，清空用户信息
     */
    CODE_103(103),

    /**
     * 用户被冻结 退出登录，清空用户信息
     */
    CODE_104(104),

    CODE_200(200);

    companion object {
        fun codeToValuOf(value: String): TokenEnum {
            values().forEach {
                if (it.code == value.toInt()) {
                    return it
                }
            }
            return CODE_200
        }
    }
}