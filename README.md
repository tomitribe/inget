# Inget Project

Inget is a code generator tool that helps you generate model classes, rest resources, client code, documentation and 
even a command line client for your REST API.

## Inget API

The Inget API has the annotations that you will use to configure your model.
Main annotations are:

```@Model```  - Add model configuration

```@Resource```  - Add it if you want a resource to be generated from that model class.

Maven depedency
```xml
<dependency>
  <groupId>org.tomitribe.inget</groupId>
  <artifactId>inget-api</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## Inget Maven Plugin
The Inget Maven Plugin is used to generate all the code.

There are some pre-requisites that must be respected and we'll improve it over time:

1. Model, resources and client needs to have their own module. The java client jar must not have the jax-rs resources and it needs the model classes. Having the resources in the java client will make the them to be deployable in other application servers.

2. Each module must build a jar with .java files, as inget will read the .java files.
```xml
<build>
  <resources>
      <resource>
          <directory>src/main/java</directory>
          <includes>
              <include>**/*.java</include>
          </includes>
      </resource>
  </resources>
</build>
```

To setup the plugin, you just need to add the inget maven plugin as following :

```xml
<plugin>
<groupId>org.tomitribe.inget</groupId>
<artifactId>inget-maven-plugin</artifactId>
<version>${version.inget}</version>
<configuration>
  <modelPackage>yourpackage.model</modelPackage>
  <resourcePackage>yourpackage.rest</resourcePackage>
  <generateModel>true</generateModel> 
  <generateResources>true</generateResources>
  <generateClient>true</generateClient> 
  <modelSuffix>Entity</modelSuffix>
  <resourceSuffix>Service</resourceSuffix>
  <clientName>YourClientName</clientName>
  <generateCli>true</generateCli>
  <cmdLineName>baby</cmdLineName>
  <programFile>baby</programFile>
  <authentication>basic</authentication>
</configuration>
<executions>
  <execution>
    <phase>generate-sources</phase>
    <goals>
      <goal>generate</goal>
    </goals>
  </execution>
</executions>
</plugin>
``` 

| Configuration | Required | Description |
| ------------- |:-------------:|-----|
| modelPackage |All| The package where your model is located. |
| resourcePackage     |Resources, Client, CLI| The package where your resources are located. |
| modelSuffix |No|Model suffix, in FoodDao, Dao would be the modelSuffix.|
| resourceSuffix |No |Resource suffix, in FoodService, Service would be the resourceSuffix.|
| clientName | Client, CLI | Name of the main class for the java client.|
| cmdLineName | CLI | Name of the main command in the CLI.|
| cmdFileName | CLI | Name of the file generated for the CLI. |
| generateModel |No     |Flag to generate the model.|
| generateResources |No     |Flag to generate the resources.|
| generateClient |Client| Flag to generate the client.|
| generateCli | CLI | Flag to generate the CLI. |
| authentication | No | Authentication type for the CLI. Supported types: 'basic' and 'signature'

See below how each part of the generation:

### Model
Inget uses Java code to generate more Java code. For a REST API Model, Inget generates amazon-style objects for each 
REST operation, Read, Create and Update.

To generate the model you need to add the required configuration as previously stated in the maven plugin table.

Create a simple class to use as template for Inget. This class is not required to be used in the API. Inget will 
generate specific objects with the information gathered from this class: 

```java
@Model
@Resource
class AccountModel {
       @Model(
            id = true, /* True if the property is an id. */
            operation = {Model.Operation.CREATE, Model.Operation.READ}, /* You can control the generation this in a class or field. A field marked with ```Model.Operation.READ``` will only be generated for the Read version of the class.*/
            summary = true, /* This will add the field to the summary class. This class will only be created if at least one field is summary. */
            filter = @Filter( /* This will add the field to the filter class. This class will only be created if at least one field is filter.*/
                    name = "usernames", /* Custom name for the filter.*/
                    multiple = true))  /* This will make the filter a collection. */
        private String username;

        @Model(operation = {Model.Operation.READ})
        private Date createdDate;

        @Model(summary = true)
        private String fullname;

        @Model(summary = true)
        private String description;

        @Model(summary = true)
        private String email;

        private Collection<String> groups;

        private Collection<String> roles;

        private Collection<String> labels;
}
``` 

The ```Model``` suffix in AccountModel tells Inget that it should look into this Java source and generate a Model from it. By default the following classes are generated:

* Account - This is used for reading an Account.
* CreateAccount - This is used for creating an Account.
* UpdateAccount - This is used for updating an Account.
* Accounts - This is used for the response of ```readAll```.
* AccountSummary - This is used for returning only the summary and not the full Account.
* BulkAccountResult - This is used in the result of bulk operations.
* AccountFilter - This is used to return the used filters in the search so the user knows.

A field or a class without ```operation``` specified in the @Model will enable all operations.

### Resources

Jax-rs resources can optionally be generated by inget. To generate the resources you need to add the required configuration as previously stated in the maven plugin table.

The resources are usually part of modules with *war* packaging, for the client generation to be able to read them, you need to make sure you generate a jar as well with the build. Then in the client generation you will point to the classifier *resources*.

```xml
<plugins>
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-jar-plugin</artifactId>
    <version>${maven.plugin.version}</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>jar</goal>
            </goals>
            <configuration>
                <classifier>resources</classifier>
            </configuration>
        </execution>
    </executions>
  </plugin>
  ...
</plugins>
```

Inget looks for the ```@Resource``` annotation in the Model classes. Also it relies in the operations of ```@Model``` to generated the methods based on the enabled operations.
```java
@Model(operation = {
        Model.Operation.CREATE,
        Model.Operation.UPDATE,
        Model.Operation.DELETE,
        Model.Operation.READ,
        Model.Operation.READ_ALL,
        Model.Operation.BULK_CREATE,
        Model.Operation.BULK_UPDATE,
        Model.Operation.BULK_DELETE})
```

The generator will generate two REST endpoint interfaces for each Model like the following:

**AccountResource**

Path: /account
```java
    Response read(@PathParam("username") final String username);
    Response create(final CreateAccount account);
    Response update(@PathParam("username") final String username, final UpdateAccount account);
    Response delete(@PathParam("username") final String username);
```

**AccountsResource**

 Path: /accounts
```java
    Response readAll();
    Response bulkCreate(final List<CreateAccount> accounts);
    Response bulkUpdate(final List<UpdateAccount> accounts);
    Response bulkDelete(final List<String> usernames);
```


### Client
To generate the client you need to add the required configuration as previously stated in the maven plugin table.

After the generation you will be able to call the resources directly using the generated client. See the following example:

```java
   final ClientConfiguration clientConfiguration =
                ClientConfiguration.builder().url(base).verbose(true).build();
        final ResourceClient resourceClient =
                new ResourceClient(clientConfiguration);

        Account account = resourceClient.account().create(CreateAccount.builder()
                                                            .username("naruto")
                                                            .fullname("Naruto")
                                                            .build());
```

### Command Line Interface (CLI)

To generate the client you need to add the required configuration as previously stated in the maven plugin table. Also, you will need to add the shade plugin your build project will generate a fatjar to be used by the CLI anywhere.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>${maven.shade.version}</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <transformers>
                    <transformer
                            implementation="org.apache.maven.plugins.shade.resource.ServicesResourceTransformer"/>
                    <transformer
                            implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                        <mainClass>yourpackage.MainCli</mainClass>
                    </transformer>
                    <transformer
                            implementation="org.apache.maven.plugins.shade.resource.AppendingTransformer">
                        <resource>META-INF/cxf/bus-extensions.txt</resource>
                    </transformer>
                </transformers>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <filters>
                    <filter>
                        <artifact>*:*</artifact>
                        <excludes>
                            <exclude>META-INF/*.SF</exclude>
                            <exclude>META-INF/*.DSA</exclude>
                            <exclude>META-INF/*.RSA</exclude>
                        </excludes>
                    </filter>
                </filters>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Also in the inget-maven-plugin you will need to add the execution goal *executable* in the phase *package*, see the example below. This will make the bash script generation for your CLI.

<plugin>
  <groupId>org.tomitribe.inget</groupId>
  <artifactId>inget-maven-plugin</artifactId>
  <version>${version.inget}</version>
  <configuration>
      <modelPackage>io.superbiz.baby.model</modelPackage>
      <resourcePackage>io.superbiz.baby.rest</resourcePackage>
      <generateCmd>true</generateCmd>
      <clientName>BabyClient</clientName>
      <resourceSuffix>Resource</resourceSuffix>
      <cmdLineName>appmanager</cmdLineName>
      <programFile>appmanager</programFile>
  </configuration>
  <executions>
      <execution>
          <id>gen</id>
          <phase>generate-sources</phase>
          <goals>
              <goal>generate</goal>
          </goals>
      </execution>
      <execution>
          <id>pkg</id>
          <phase>package</phase>
          <goals>
              <goal>executable</goal>
          </goals>
      </execution>
  </executions>
</plugin>

After the build you will see in the target folder that it generated a file called *appmanager* that you configured in the *programFile* pom property. Now you will be able to execute it from anywhere.

This command will show you the commands for account.
./appmanager help account

This command will create a new account. 
./appmanager --url http://localhost:8080/api account create --fullname "Steve Jobs" --username steve 

The url is only required in the first time you execute it.
