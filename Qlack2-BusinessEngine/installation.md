# Installation of QBE 

## Prerequisites

* JDK 1.8.x
* Karaf 3.0.2
* MySQL 5.7.10


## Maven Settings
in settings.xml add  the following profile

```
<profile>
    <id>qbe</id>
    <properties>
      <liquibase.driver.groupId>mysql</liquibase.driver.groupId>
      <liquibase.driver.artifactId>mysql-connector-java</liquibase.driver.artifactId>
      <liquibase.driver.version>5.1.29</liquibase.driver.version>

      <liquibase.driver>com.mysql.jdbc.Driver</liquibase.driver>
      <liquibase.url>jdbc:mysql://localhost/qbe?useUnicode=true&amp;characterEncoding=UTF-8&amp;characterSetResults=utf8&amp;connectionCollation=utf8_general_ci</liquibase.url>
      <liquibase.username>root</liquibase.username>
      <liquibase.password>ioannis</liquibase.password>
    </properties>
</profile>
```
### Install QBE and liquibase profile

Go to terminal navigate to the ..\QLACK1.5\Qlack2 and execute the following commands

`mvn clean install -DskipTests=true`
`mvn liquibase:update -Pqbe`

## Installation on Karaf

### Install QBE Karaf features

`feature:repo-add cxf 2.7.18` <br>
`feature:repo-add mvn:com.eurodyn.qlack2.util/util-karaf-features/1.7.0/xml/features`<br> 
`feature:repo-add mvn:com.eurodyn.qlack2.webdesktop/webdesktop-karaf-features/1.3.0/xml/features`<br>
`feature:repo-add mvn:com.eurodyn.qlack2.webdesktop.apps/webdesktop-apps-karaf-features/1.3.0/xml/features`<br>
`feature:repo-add mvn:com.eurodyn.qlack2.common/common-karaf-features/1.4.2/xml/features`<br>
`feature:repo-add mvn:com.eurodyn.qlack2.util/repack-karaf-features/1.7.0/xml/features`<br>
`feature:repo-add mvn:com.eurodyn.qlack2.fuse/fuse-karaf-features/1.6.2/xml/features`<br>
`feature:repo-add mvn:com.eurodyn.qlack2.be/be-karaf-features/1.3.0/xml/features`<br>
`feature:install qlack-util-datasource-mysql-xa`<br>
`feature:install hibernate/4.2.15.Final`<br>
### Configure the QBE datasource
At your local karaf folder navigate to the ..\apache-karaf-3.0.2\etc

* Open  the com.eurodyn.qlack2.util.datasourcegeneric.cfg file and update your database credentials 
* create a new file with name org.apache.cxf.osgi.cfg and add the following content:

`org.apache.cxf.servlet.context = /api`<br>
`org.apache.cxf.servlet.service-list-path = /myservices`<br>
`org.apache.cxf.servlet.hide-service-list-page = false`<br>

### Install the qlack-qbe feature
 Go to your Karaf console and execute: 
 
` start datasource-generic`<br>
`feature:install qlack-qbe`

## Creating a defaut user

The ESCP users database comes empty, so you need a default user to login with.
Go to your Karaf console and execute:

    qlack:aaa-user-add admin 1234 true 1

You now have a user with:
username: admin
password: 1234


