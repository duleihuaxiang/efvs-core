EFVS 离线编译/运行包（Windows / JDK8）
======================================

目录结构
  build.bat               一键离线编译
  run.bat                 直接运行已构建好的 jar
  settings.xml            编译用 Maven 设置（离线，无镜像）
  apache-maven-3.6.3/     随包 Maven 3.6.3（无需另装）
  offline-repo/           离线 Maven 仓库（约 340MB，已含编译所需全部依赖与插件）
  efvs-thin/              工程源码（含 maven-repo、.mvn；不含 .svn）
  dist/yudao-server.jar   已构建好的可运行 Spring Boot fat jar

环境要求
  - Windows x64
  - JDK 8（1.8），并已设置 JAVA_HOME
  - 无需安装 Maven，无需联网

编译（一条命令）
  双击 build.bat，或在 cmd 中执行：
    build.bat

  等价命令（先进入 efvs-thin 目录）：
    ..\apache-maven-3.6.3\bin\mvn.cmd -o -B -s ..\settings.xml -Dmaven.repo.local=..\offline-repo -Dmaven.test.skip=true clean package

  settings.xml 里已写死 localRepository=本包 offline-repo，所以也可以不带 -Dmaven.repo.local：
    ..\apache-maven-3.6.3\bin\mvn.cmd -o -B -s ..\settings.xml -Dmaven.test.skip=true clean package
  注意：如果包被移动到别的路径，请同步修改 settings.xml 中 localRepository 的路径。

  产物：efvs-thin\yudao-server\target\yudao-server.jar

运行
  run.bat（等价于 "%JAVA_HOME%\bin\java.exe" -jar dist\yudao-server.jar）
  数据库等运行配置在 jar 内的 application*.yaml 中，可按项目既有方式用外部配置文件覆盖。

说明
  - offline-repo 中的构件已登记为本地安装（_remote.repositories 内为 >= 条目），请勿删除该文件。
  - 离线仓库按本工程实际编译过程裁剪，不要加 -U 参数，不要联网。
  - 包可放在任意路径，脚本通过 %~dp0 自动定位，不依赖固定盘符。
