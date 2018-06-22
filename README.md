# Trapease Project

Trapease is a code generator tool that helps you generate model classes, rest resources, client code, documentation and 
even a command line client for your REST API.

## Trapease API

The Trapease API has the annotations that you will use to configure your model.
Main annotations are:

```@Model```  - Add model configuration

```@Resource```  - Add it if you want a resource to be generated from that model class.

Maven depedency
```xml
<dependency>
  <groupId>org.tomitribe</groupId>
  <artifactId>trapease-api</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## Trapease Maven Plugin
The Trapease Maven Plugin is used to generate all the code looking into your configured model. See below how each part of the generation work:

### Model

Trapease uses Java code to generate more Java code. For a REST API Model, Trapease generates different objects for each 
REST operation, Read, Create and Update.

Create a simple class to use as template for Trapease. This class is not required to be used in the API. Trapease will 
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

The ```Model``` suffix in AccountModel tells Trapease that it should look into this Java source and generate a Model from it. By default the following classes are generated:

* Account - This is used for reading an Account.
* CreateAccount - This is used for creating an Account.
* UpdateAccount - This is used for updating an Account.
* Accounts - This is used for the response of ```readAll```.
* AccountSummary - This is used for returning only the summary and not the full Account.
* BulkAccountResult - This is used in the result of bulk operations.
* AccountFilter - This is used to return the used filters in the search so the user knows.

A field or a class without ```operation``` specified in the @Model will enable all operations.

### Resources

To generate REST endpoints Trapease looks for the ```@Resource``` annotation in the Model classes. Also it relies in the operations of ```@Model``` to generated the methods based on the enabled operations.
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
To generate the client you need to add the following in the plugin ```<configuration>```.

* resourcePackage - Make sure to add the package to your resources.
* generateClient - Add ```true``` to generate the client.
* clientName - Add a new name if you don't want the default to be ```ResourceClient```.

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

### Command line

This is not ready.

### Setup

To setup Trapease, you just need to add the trapease maven plugin as following :

```xml
<plugin>
<groupId>org.tomitribe</groupId>
<artifactId>trapease-maven-plugin</artifactId>
<version>${version.trapease}</version>
<configuration>
  <modelPackage>yourpackage.model</modelPackage>
  <resourcePackage>yourpackage.rest</resourcePackage>
  <!--<generateModel>true</generateModel> -->
  <!--<generateResources>true</generateResources> -->
  <!--<generateClient>true</generateClient> -->
  <!--<modelSuffix>Entity</modelSuffix>-->
  <!--<resourceSuffix>Service</resourceSuffix>-->
  <!--<clientName>YourClientName</clientName>-->
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

And point the plugin property ```modelPackage``` to the package where the model classes reside. The 
```resourcePackage``` is the package used by Trapease to generate the REST Resources.

The ```modelSuffix``` is "<class-name>Model" by default, but you can change it for "<class-name>Entity" for example.
The ```resourceSuffix``` is "<class-name>Resource" by default, but you can change it for "<class-name>Service" for example.



