#### Github Actions [待详细补充]



<img src="https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-04-06-155635.png" alt="image-20240406235632838" style="height:600px" />



<img src="https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-04-06-155622.png" alt="image-20240406235538963" style="width:800px" />











#### Maven Central Deploy [待详细补充]



注册账户,可以用GitHub账号, 这样空间审核比较简答

https://central.sonatype.com/publishing/deployments





Mac OS 本地安装GPG,  生成密钥,  并上传到可认证服务器上

```shell
brew install gpg
# gpg, 用户名、密码生成密钥
gpg --gen-key
# 如果忘记密钥,可以通过如下命令查看
gpg --list-keys
# 将密钥上传认证服务器上
sudo gpg --keyserver hkp://185.125.188.27:11371 --send-keys PUBXXXXXXXX
```



Maven settting.xml 配置

```xml
<profile>
    <id>gpg</id>
    <properties>
        <gpg.executable>gpg</gpg.executable>
        <gpg.keyname>{{your.gpg.username}}</gpg.keyname>
        <gpg.passphrase>{{your.gpg.password}}</gpg.passphrase>
        <gpg.useagent>true</gpg.useagent>
    </properties>
</profile>


<servers>
    <server>
      <id>central</id>
      <username>{{your.sonatype.keyname}}</username>
      <password>{{your.sonatype.key}}</password>
    </server>
</servers>
```

其中 {{your.gpg.xxx}} 就是 gpg --gen-key 创建密钥时的 用户名、密码

其中 {{your.sonatype.xxxx}} 参考如下, 首先登录/注册 https://central.sonatype.com/publishing/deployments 的账号,创建后右侧点击 View Account



![image-20240406235119248](https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-04-06-155122.png)

 点击 Generate User Token 进行生成即可

![image-20240406235359378](https://ipman-blog-1304583208.cos.ap-nanjing.myqcloud.com/rpcman/2024-04-06-155402.png)





POM 上传规范

```java
    <licenses>
        <license>
            <name>The Apache Software License, Version 2.0</name>
            <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>

    <issueManagement>
        <system>github</system>
        <url>https://github.com/ipipman/rpcman/issues</url>
    </issueManagement>

    <scm>
        <connection>scm:git:https://github.com/ipipman/rpcman.git</connection>
        <developerConnection>scm:git:https://github.com/ipipman/rpcman.git</developerConnection>
        <url>https://github.com/ipipman/rpcman</url>
    </scm>

    <developers>
        <developer>
            <name>ipman</name>
            <email>ipipman@163.com</email>
            <url>https://github.com/ipipman</url>
        </developer>
    </developers>

```



Maven Plugins

```java
    <build>
        <plugins>
            <plugin>
      
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>3.1.0</version>
                <inherited>true</inherited>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <excludeResources>true</excludeResources>
                    <useDefaultExcludes>true</useDefaultExcludes>
                </configuration>
            </plugin>


            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>3.1.0</version>
                <inherited>true</inherited>
                <executions>
                    <execution>
                        <id>bundle-sources</id>
                        <phase>package</phase>
                        <goals>
                            <goal>jar</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <maxmemory>1024</maxmemory>
                    <encoding>UTF-8</encoding>
                    <show>protected</show>
                    <notree>true</notree>

                    <!-- Avoid running into Java 8's very restrictive doclint issues -->
                    <failOnError>false</failOnError>
                    <doclint>none</doclint>
                </configuration>
            </plugin>
        </plugins>
    </build>
```



Maven Profiles GPG

```java

    <profiles>
        <profile>
            <id>release</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-gpg-plugin</artifactId>
                        <version>1.6</version>
                        <executions>
                            <execution>
                                <id>sign-artifacts</id>
                                <phase>verify</phase>
                                <goals>
                                    <goal>sign</goal>
                                </goals>
                            </execution>
                        </executions>
                    </plugin>

                    <!--                    配置方式：https://central.sonatype.org/publish/publish-portal-maven/#deploymentname -->
                    <plugin>
                        <groupId>org.sonatype.central</groupId>
                        <artifactId>central-publishing-maven-plugin</artifactId>
                        <version>0.4.0</version>
                        <extensions>true</extensions>
                        <configuration>
                            <publishingServerId>central</publishingServerId>
                            <tokenAuth>true</tokenAuth>
                            <autoPublish>true</autoPublish>
                            <excludeArtifacts>
                                <!--         <artifact>rpcman-core</artifact>-->
                                <excludeArtifact>rpcman-demo-api</excludeArtifact>
                                <excludeArtifact>rpcman-demo-provider</excludeArtifact>
                                <excludeArtifact>rpcman-demo-consumer</excludeArtifact>
                            </excludeArtifacts>
                        </configuration>
                    </plugin>

                </plugins>
            </build>
        </profile>
    </profiles>
```



Maven Deploy

```java
export GPG_TTY=$(tty)
mvn clean install deploy -P release
```

