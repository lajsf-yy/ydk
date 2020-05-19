#ydk

## 所有原生功能将以包的形式暴露出来，代码放在 packages 里，所有功能均要写测试代码，测试代码在 example 下

## 如果引入第三方依赖 则新建个 native module，如果只是 react native 相关 可以直接放到 ydk-react 下

# 本地调试 ydk

在 ydk 仓库中执行 yarn link

在项目中将 ydk 替换成 "ydk": "link:../ydk"

执行 yarn
